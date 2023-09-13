package cn.cfl.memory.sdk;

import cn.hutool.core.util.RandomUtil;
import com.unfbx.chatgpt.function.KeyStrategyFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chen.fangliang
 */
@Slf4j
public class KeyRateLimitStrategy implements KeyStrategyFunction<List<String>, String> {

    @Override
    public String apply(List<String> apiKeys) {
        if (CollectionUtils.isEmpty(apiKeys)) {
            return null;
        }

        List<String> tmpArr = new ArrayList<>(apiKeys);
        String key;
        do {
            key = getKey(tmpArr);
            if (StringUtils.isEmpty(key) && CollectionUtils.isEmpty(tmpArr)) {
                log.info("所有key都失效，随机选一个");
                key = RandomUtil.randomEle(apiKeys);
            }
        } while (StringUtils.isEmpty(key));
        log.info("使用api key: {}", key);
        return key;
    }

    private String getKey(List<String> apiKeys) {
        String randomKey = RandomUtil.randomEle(apiKeys);
        boolean rateLimit = RateLimitRedisHelper.isRateLimit(randomKey);
        if (!rateLimit) {
            return randomKey;
        }
        apiKeys.remove(randomKey);
        log.info("gpt token: {} 被限流", randomKey);
        return null;
    }

}
