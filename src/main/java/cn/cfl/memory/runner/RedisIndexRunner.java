package cn.cfl.memory.runner;

import cn.cfl.memory.core.IndexManage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author chen.fangliang
 */
@Slf4j
@Component
public class RedisIndexRunner implements CommandLineRunner {

    @Resource
    private IndexManage indexManage;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化索引");
        indexManage.createIndex();
    }
}
