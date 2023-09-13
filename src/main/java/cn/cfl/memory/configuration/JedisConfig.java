
package cn.cfl.memory.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

@Configuration
public class JedisConfig {

    // TODO
    @Bean
    public UnifiedJedis UnifiedJedis() {
        return new UnifiedJedis(HostAndPort.from("127.0.0.1:6379"));
    }
}
