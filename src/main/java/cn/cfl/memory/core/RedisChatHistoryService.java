package cn.cfl.memory.core;

import cn.cfl.memory.core.config.Config;
import cn.cfl.memory.core.data.HistoryChat;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import redis.clients.jedis.UnifiedJedis;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chen.fangliang
 */
@Slf4j
public class RedisChatHistoryService implements IChatHistoryService {

    private final Config config;
    private final UnifiedJedis jedis;

    public RedisChatHistoryService(Config config, UnifiedJedis jedis) {
        this.jedis = jedis;
        this.config = config;
    }

    /**
     * 只是简单的保存，不做去重、并发处理，更好的办法是用lua脚本
     */
    @Override
    public String storeHistory(String conversationId, HistoryChat historyChat) {
        // 使用zset保存
        long timestamp = historyChat.getTimestamp();
        if (timestamp <= 0) {
            timestamp = System.currentTimeMillis();
        }
        String chatKey = chatKey(conversationId);

        Integer storeMaxChatHistory = config.getStoreMaxChatHistory();
        long chatLen = jedis.zcard(chatKey);
        if (chatLen > storeMaxChatHistory) {
            jedis.zremrangeByRank(chatKey, 0, chatLen - storeMaxChatHistory + 1);
        }
        // 先判断长度
        jedis.zadd(chatKey, timestamp, historyChat.toJSONString());
        return null;
    }

    @Override
    public List<HistoryChat> findLastHistory(String conversationId, Integer topN) {
        String chatKey = chatKey(conversationId);
        List<String> lastHistory = jedis.zrevrange(chatKey, 0, topN);

        if (CollectionUtils.isNotEmpty(lastHistory)) {
            try {
                return lastHistory.stream().map(item -> JSON.parseObject(item, HistoryChat.class)).collect(Collectors.toList());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    private String chatKey(String conversationId) {
        return "CHAT_" + conversationId;
    }
}
