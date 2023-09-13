package cn.cfl.memory.core.data;

import lombok.Data;

/**
 * @author chen.fangliang
 */
@Data
public class SearchResult extends MemoryChat {

    private String conversationId;
    /**
     * 得分，越大或者越小，视使用数据库而定，像使用redis search，则代表距离，越小越接近
     */
    private double score;
}
