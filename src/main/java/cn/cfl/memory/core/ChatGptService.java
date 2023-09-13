package cn.cfl.memory.core;

import cn.cfl.memory.core.config.Config;
import cn.cfl.memory.core.data.HistoryChat;
import cn.cfl.memory.core.data.MemoryChat;
import cn.cfl.memory.core.enums.GptRole;
import cn.cfl.memory.core.utils.ByteUtil;
import cn.cfl.memory.sdk.ExtOpenAiClient;
import com.alibaba.fastjson.JSON;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.entity.embeddings.EmbeddingResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chen.fangliang
 */
@Slf4j
public class ChatGptService implements ILlmService {

    private final ExtOpenAiClient client;
    private final Config config;

    public ChatGptService(Config config, ExtOpenAiClient client) {
        this.client = client;
        this.config = config;
    }

    @Override
    public String askLlm(String prompt, String userAsk, List<HistoryChat> historyChats, List<MemoryChat> memoryChats) {
        if (CollectionUtils.isNotEmpty(memoryChats)) {
            List<String> historyLine = new ArrayList<>();
            for (MemoryChat historyChat : memoryChats) {
                historyLine.add(historyChat.getSummary());
            }
            String historyStr = StringUtils.join(historyLine, "\n");
            prompt = prompt.replace(config.getPromptMemoryPlaceholder(), historyStr);
        } else {
            // 清空历史记录
            prompt = prompt.replace(config.getPromptMemoryPlaceholder(), "");
        }

        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder().role(GptRole.SYSTEM.getRole()).content(prompt).build());
        if (CollectionUtils.isNotEmpty(historyChats)) {
            for (HistoryChat historyChat : historyChats) {
                messages.add(Message.builder().role(GptRole.USER.getRole()).content(historyChat.getUserSay()).build());
                messages.add(Message.builder().role(GptRole.ASSISTANT.getRole()).content(historyChat.getAssistantSay()).build());
            }
        }

        // TODO 超长截断
        messages.add(Message.builder().role(GptRole.USER.getRole()).content(userAsk).build());
        log.info("to gpt messages: {}", JSON.toJSONString(messages));
        ChatCompletion completionToSummarize = ChatCompletion.builder().messages(messages).build();
        ChatCompletionResponse resp = client.chatCompletion(completionToSummarize);
        return resp.getChoices().get(0).getMessage().getContent();
    }

    @Override
    public String summaryChatRound(String prompt, String userSay, String llmSay) {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder().role(GptRole.SYSTEM.getRole()).content(prompt).build());
        messages.add(Message.builder().role(GptRole.USER.getRole()).content("user：" + userSay + "；assistant：" + llmSay).build());
        ChatCompletion completionToSummarize = ChatCompletion.builder().messages(messages).build();
        ChatCompletionResponse resp = client.chatCompletion(completionToSummarize);
        return resp.getChoices().get(0).getMessage().getContent();
    }

    @Override
    public byte[] embedding(String input) {
        EmbeddingResponse embeddings = client.embeddings(input);
        List<Float> floats = new ArrayList<>();
        for (BigDecimal bigDecimal : embeddings.getData().get(0).getEmbedding()) {
            floats.add(bigDecimal.floatValue());
        }
        floats = floats.subList(0, 1024);
        return ByteUtil.floatArrayToByteArray(floats.toArray(new Float[0]));
    }
}
