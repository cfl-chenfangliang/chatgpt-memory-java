
package cn.cfl.memory.configuration;

import cn.cfl.memory.core.*;
import cn.cfl.memory.core.config.Config;
import cn.cfl.memory.sdk.ExtOpenAiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import redis.clients.jedis.UnifiedJedis;

import javax.annotation.Resource;

/**
 * @author chen.fangliang
 */
@Configuration
public class ServiceConfig {

    @Resource
    private Config config;
    @Resource
    private UnifiedJedis jedis;

    @Bean
    public ILlmService llmService(@Autowired ExtOpenAiClient openAiClient) {
        return new ChatGptService(config, openAiClient);
    }

    @Bean
    public RedisVectorSearch redisVectorSearch() {
        return new RedisVectorSearch(config, jedis);
    }

    @Bean
    public IVectorSearch vectorSearch(@Autowired RedisVectorSearch redisVectorSearch) {
        return redisVectorSearch;
    }

    @Bean
    public IndexManage indexManage(@Autowired RedisVectorSearch redisVectorSearch) {
        return redisVectorSearch;
    }

    @Bean
    public IChatHistoryService chatHistoryService() {
        return new RedisChatHistoryService(config, jedis);
    }

}
