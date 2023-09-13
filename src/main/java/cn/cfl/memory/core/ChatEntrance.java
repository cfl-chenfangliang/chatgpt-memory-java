package cn.cfl.memory.core;

import cn.cfl.memory.core.config.Config;
import cn.cfl.memory.core.data.HistoryChat;
import cn.cfl.memory.core.data.MemoryChat;
import cn.cfl.memory.core.data.SearchData;
import cn.cfl.memory.core.data.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天入口
 *
 * @author chen.fangliang
 */
@Slf4j
@Service
public class ChatEntrance {

    @Resource
    private IVectorSearch vectorSearch;
    @Resource
    private ILlmService gptService;
    @Resource
    private Config config;
    @Resource
    private IChatHistoryService chatHistoryService;

    public String chat(String conversationId, String input) {
        return chat(conversationId, input, config.getDefaultPrompt(), config.getDefaultSummaryPrompt());
    }

    public String chat(String conversationId, String input, String prompt, String summaryPrompt) {
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
        String gptSay = gptService.askLlm(prompt, input, lastHistory, memoryChats);

        // 4.总结本次对话
        String summaryChat = gptService.summaryChatRound(summaryPrompt, input, gptSay);
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
        return gptSay;
    }
}
