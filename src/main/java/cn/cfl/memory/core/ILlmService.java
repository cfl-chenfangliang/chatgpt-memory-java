package cn.cfl.memory.core;


import cn.cfl.memory.core.data.HistoryChat;
import cn.cfl.memory.core.data.MemoryChat;

import java.util.List;

/**
 * @author chen.fangliang
 */
public interface ILlmService {

    /**
     * 询问大语言模型
     * <p>
     * 这里区分了历史对话和记忆，是为了更好的聊天效果
     *
     * @param prompt       llm prompt
     * @param userAsk      当前用户的提问
     * @param historyChats 历史对话消息
     * @param memoryChats  历史记忆消息
     * @return llm的响应
     */
    String askLlm(String prompt, String userAsk, List<HistoryChat> historyChats, List<MemoryChat> memoryChats);

    /**
     * 总结当前对话
     */
    String summaryChatRound(String prompt, String userSay, String llmSay);

    byte[] embedding(String input);
}
