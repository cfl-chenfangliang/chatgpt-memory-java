
package cn.cfl.memory.configuration;

import cn.cfl.memory.sdk.DynamicConfigOpenAiAuthInterceptor;
import cn.cfl.memory.sdk.ExtOpenAiClient;
import cn.cfl.memory.sdk.KeyRateLimitStrategy;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class ChatGptConfig {

    public static final HttpLoggingInterceptor HTTP_LOGGING_INTERCEPTOR = new HttpLoggingInterceptor(new OpenAILogger());

    static {
        HTTP_LOGGING_INTERCEPTOR.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient
            .Builder()
            .addInterceptor(HTTP_LOGGING_INTERCEPTOR)//自定义日志
            .connectTimeout(120, TimeUnit.SECONDS)//自定义超时时间
            .writeTimeout(120, TimeUnit.SECONDS)//自定义超时时间
            .readTimeout(120, TimeUnit.SECONDS)//自定义超时时间
            .build();

    @Bean
    public ExtOpenAiClient extOpenAiClient() {
        List<String> tokens = new ArrayList<>();
        tokens.add("111");
        // 动态获取api keys
        DynamicConfigOpenAiAuthInterceptor authInterceptor = new DynamicConfigOpenAiAuthInterceptor();
        authInterceptor.setGetApiKeysSupplier(() -> tokens);

        return new ExtOpenAiClient.Builder()
                .authInterceptor(authInterceptor)
                .apiKey(tokens)
                .keyStrategy(new KeyRateLimitStrategy())
                .okHttpClient(OK_HTTP_CLIENT).build();
    }
}
