package cn.cfl.memory.sdk;

import cn.hutool.json.JSONUtil;
import com.unfbx.chatgpt.entity.common.OpenAiResponse;
import com.unfbx.chatgpt.exception.BaseException;
import com.unfbx.chatgpt.exception.CommonError;
import com.unfbx.chatgpt.interceptor.OpenAiAuthInterceptor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 增加过期token处理
 *
 * @author chen.fangliang
 */
@Getter
@Slf4j
public class DynamicConfigOpenAiAuthInterceptor extends OpenAiAuthInterceptor {

    /**
     * 账号被封了
     */
    private static final String ACCOUNT_DEACTIVATED = "account_deactivated";

    private static final String RATE_LIMIT = "rate_limit_exceeded";

    /**
     * key不正确
     */
    private static final String INVALID_API_KEY = "invalid_api_key";

    private final List<String> invalidKeys = new ArrayList<>();

    @Setter
    private Supplier<List<String>> getApiKeysSupplier;

    /**
     * 请求头处理
     */
    public DynamicConfigOpenAiAuthInterceptor() {
        this.setWarringConfig(null);
    }

    /**
     * 构造方法
     *
     * @param warringConfig 所有的key都失效后的告警参数配置
     */
    public DynamicConfigOpenAiAuthInterceptor(Map warringConfig) {
        this.setWarringConfig(warringConfig);
    }

    public String getKeyFromSupplier() {
        List<String> allApiKeys = getApiKeysSupplier.get();
        // 过滤无效的
        allApiKeys.removeAll(invalidKeys);
        setApiKey(allApiKeys);
        return super.getKey();
    }

    @Override
    protected List<String> onErrorDealApiKeys(String errorKey) {
        invalidKeys.add(errorKey);
        log.error("--------> 当前ApiKey：[{}] 失效了，移除！", errorKey);
        return getApiKey();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String key = getKeyFromSupplier();
        Request original = chain.request();
        Request request = this.auth(key, original);
        Response response = chain.proceed(request);

        if (!response.isSuccessful()) {
            String errorMsg = response.body().string();
            if (response.code() == CommonError.OPENAI_AUTHENTICATION_ERROR.code()
                    || response.code() == CommonError.OPENAI_LIMIT_ERROR.code()
                    || response.code() == CommonError.OPENAI_SERVER_ERROR.code()) {
                OpenAiResponse openAiResponse = JSONUtil.toBean(errorMsg, OpenAiResponse.class);
                String errorCode = openAiResponse.getError().getCode();
                log.error("--------> 请求openai异常，错误code：{}", errorCode);
                log.error("--------> response.code: {}", response.code());
                log.error("--------> 请求异常：{}", errorMsg);
                //账号被封或者key不正确就移除掉
                if (ACCOUNT_DEACTIVATED.equals(errorCode) || INVALID_API_KEY.equals(errorCode)) {
                    super.setApiKey(this.onErrorDealApiKeys(key));
                }
                if (RATE_LIMIT.equals(errorCode)) {
                    // 限流了
                    RateLimitRedisHelper.tokenRateLimit(key);
                }
                throw new BaseException(openAiResponse.getError().getMessage());
            }
            //非官方定义的错误code
            log.error("--------> 请求异常：{}", errorMsg);
            OpenAiResponse openAiResponse = JSONUtil.toBean(errorMsg, OpenAiResponse.class);
            if (Objects.nonNull(openAiResponse.getError())) {
                log.error(openAiResponse.getError().getMessage());
                throw new BaseException(openAiResponse.getError().getMessage());
            }
            throw new BaseException(CommonError.RETRY_ERROR);
        }
        return response;
    }


    /**
     * 所有的key都失效后，自定义预警配置
     * 不配置直接return
     */
    @Override
    protected void noHaveActiveKeyWarring() {
        log.error("--------> [告警] 没有可用的key！！！");
    }

    @Override
    public Request auth(String key, Request original) {
        return super.auth(key, original);
    }
}

