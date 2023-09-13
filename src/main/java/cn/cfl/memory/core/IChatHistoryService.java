package cn.cfl.memory.core;

import cn.cfl.memory.core.data.HistoryChat;

import java.util.List;

/**
 * 聊天记录
 *
 * @author chen.fangliang
 */
public interface IChatHistoryService {

    /**
     * 保存聊天记录
     *
     * @param historyChat 对话
     * @return 唯一id（如果有才返回）
     */
    String storeHistory(String conversationId, HistoryChat historyChat);

    /**
     * 查询最新的聊天记录
     *
     * @param conversationId 对话id
     * @param topN           返回最新的N条
     */
    List<HistoryChat> findLastHistory(String conversationId, Integer topN);
}
