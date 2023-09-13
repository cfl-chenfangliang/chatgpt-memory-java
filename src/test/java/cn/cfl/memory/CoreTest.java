package cn.cfl.memory;

import cn.cfl.memory.core.*;
import cn.cfl.memory.core.config.Config;
import cn.cfl.memory.core.config.VectorIndexConfig;
import cn.cfl.memory.core.data.HistoryChat;
import cn.cfl.memory.core.data.MemoryChat;
import cn.cfl.memory.core.data.SearchData;
import cn.cfl.memory.core.data.SearchResult;
import cn.cfl.memory.sdk.ExtOpenAiClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Slf4j
class CoreTest {

    @Test
    public void test() throws Exception {
        UnifiedJedis jedis = new UnifiedJedis(HostAndPort.from("127.0.0.1:6379"));

        VectorIndexConfig vectorIndexConfig = new VectorIndexConfig();
        Config config = new Config();
        config.setIndexConfig(vectorIndexConfig);

        RedisVectorSearch redisVectorSearch = new RedisVectorSearch(config, jedis);
        @SuppressWarnings("all")
        IVectorSearch vectorSearch = redisVectorSearch;
        @SuppressWarnings("all")
        IndexManage indexManage = redisVectorSearch;

        IChatHistoryService chatHistoryService = new RedisChatHistoryService(config, jedis);

        // 创建索引
        indexManage.createIndex();

        // gpt服务
        ChatGptService gptService = new ChatGptService(config, getOpenApiClient());

        String conversationId = "conversation_1";

        Scanner scanner = new Scanner(System.in);

        System.out.print("【请输入】：");
        boolean isFirst = true;
        while (scanner.hasNext()) {
            if (!isFirst) {
                System.out.print("【请输入】：");
            }
            isFirst = false;
            String input = scanner.next();
            if (StringUtils.isEmpty(input)) {
                continue;
            }
            // 1.先查询历史对话，这里查询历史对话的原因是，由于提问需要embedding，把部分历史聊天记录一起embedding，会增加提问的准确性，因为大部分情况提问总是和上文相关的
            Integer historyTopN = config.getHistoryTopN();
            List<HistoryChat> lastHistory = null;
            StringBuilder inputEmbeddingQuery = new StringBuilder("Human:" + input);
            if (historyTopN > 0) {
                lastHistory = chatHistoryService.findLastHistory(conversationId, historyTopN);
                if (CollectionUtils.isNotEmpty(lastHistory)) {
                    List<String> concat = new ArrayList<>();
                    for (HistoryChat historyChat : lastHistory) {
                        concat.add("Human:" + historyChat.getUserSay() + ", Assistant:" + historyChat.getAssistantSay());
                        inputEmbeddingQuery.append(";").append(StringUtils.join(concat, ";"));
                    }
                }
            }
            log.info("用户提问时，embedding的输入：{}", inputEmbeddingQuery);

            // 2.查询记忆
            byte[] userSayEmbedding = gptService.embedding(inputEmbeddingQuery.toString());
            List<SearchResult> search = vectorSearch.search(conversationId, userSayEmbedding, config.getMemoryTopN());
            List<MemoryChat> memoryChats = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search)) {
                for (SearchResult result : search) {
                    String summary = result.getSummary();
                    log.info("查询到的记忆总结：{}", summary);
                    MemoryChat memoryChat = new MemoryChat();
                    memoryChat.setSummary(summary);
                    memoryChats.add(memoryChat);
                }
            }

            // 3.向gpt提问
            String gptSay = gptService.askLlm(config.getDefaultPrompt(), input, lastHistory, memoryChats);

            // 4.总结本次对话
            String summaryChat = gptService.summaryChatRound(config.getDefaultSummaryPrompt(), input, gptSay);
            log.info("总结当前对话：{}", summaryChat);
            byte[] summaryEmbedding = gptService.embedding(summaryChat);
            boolean similarityMemory = vectorSearch.similarityMemory(conversationId, summaryEmbedding);
            if (!similarityMemory) {
                // 5.保存当前记忆
                log.info("记忆有效，保存记忆");
                SearchData searchData = new SearchData();
                searchData.setEmbedding(summaryEmbedding);
                searchData.setConversationId(conversationId);
                searchData.setUserSay(input);
                searchData.setAssistantSay(gptSay);
                searchData.setSummary(summaryChat);
                vectorSearch.storeVector(searchData);
            }
            // 6.保存聊天记录
            HistoryChat historyChatSave = new HistoryChat();
            historyChatSave.setUserSay(input);
            historyChatSave.setAssistantSay(gptSay);
            historyChatSave.setTimestamp(System.currentTimeMillis());
            chatHistoryService.storeHistory(conversationId, historyChatSave);
            System.out.println("【LLM回复】：" + gptSay);
        }
    }

    private static OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            // .addInterceptor(HTTP_LOGGING_INTERCEPTOR)//自定义日志
            .connectTimeout(120, TimeUnit.SECONDS)//自定义超时时间
            .writeTimeout(120, TimeUnit.SECONDS)//自定义超时时间
            .readTimeout(120, TimeUnit.SECONDS)//自定义超时时间
            .build();

    private static ExtOpenAiClient getOpenApiClient() {
        List<String> tokens = new ArrayList<>();
        tokens.add("xxxxx");
        return new ExtOpenAiClient.Builder().apiKey(tokens).okHttpClient(OK_HTTP_CLIENT).build();
    }
}
