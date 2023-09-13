package cn.cfl.memory.core.config;

import lombok.Data;

/**
 * 向量数据库索引配置
 *
 * @author chen.fangliang
 */
@Data
public class VectorIndexConfig {

    private String indexName = "idx";

    /**
     * embedding的字段名
     */
    private String vectorField = "embedding";

    private String type = "FLOAT32";

    private Integer dim = 1024;

    private String distanceMetric = "L2";

    private Integer initialCap = 686;

    private Integer m = 40;

    private Integer efConstruction = 200;

}
