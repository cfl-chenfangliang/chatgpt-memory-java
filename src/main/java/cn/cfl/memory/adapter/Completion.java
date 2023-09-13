package cn.cfl.memory.adapter;

import lombok.Data;

/**
 * @author chen.fangliang
 */
@Data
public class Completion {

    /**
     * 用户标识
     */
    private String userId;
    /**
     * 用户的输入
     */
    private String input;
}
