package cn.cfl.memory.sdk;

import cn.cfl.memory.utils.SpringContextUtils;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.SetParams;

/**
 * @author chen.fangliang
 */
public class RateLimitRedisHelper {

    @Setter
    public static UnifiedJedis DEFAULT_JEDIS = null;

    public static void tokenRateLimit(String apiToken) {
        if (StringUtils.isEmpty(apiToken)) {
            return;
        }
        String cacheKey = getCacheKey(apiToken);
        UnifiedJedis redisUtil = getRedisUtil();
        boolean exists = redisUtil.exists(cacheKey);
        if (exists) {
            return;
        }
        redisUtil.set(cacheKey, "true", SetParams.setParams().ex(60L));
    }

    public static boolean isRateLimit(String apiToken) {
        String cacheKey = getCacheKey(apiToken);
        UnifiedJedis redisUtil = getRedisUtil();
        return redisUtil.exists(cacheKey);
    }

    private static String getCacheKey(String apiToken) {
        return "gpt_token_rate_limit:" + apiToken;
    }


    private static UnifiedJedis getRedisUtil() {
        if (DEFAULT_JEDIS != null) {
            return DEFAULT_JEDIS;
        }
        return SpringContextUtils.getBean(UnifiedJedis.class);
    }
}
