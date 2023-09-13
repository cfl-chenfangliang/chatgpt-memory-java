package cn.cfl.memory.core.data;

import lombok.Data;

/**
 * @author chen.fangliang
 */
@Data
public class SearchData extends MemoryChat {
    private byte[] embedding;
    private String conversationId;
}
