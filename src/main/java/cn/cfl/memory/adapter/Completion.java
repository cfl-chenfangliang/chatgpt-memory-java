package cn.cfl.memory.adapter;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author chen.fangliang
 */
@Data
public class Completion {

    /**
     * 用户标识
     */
    @NotNull
    private String userId;
    /**
     * 用户的输入
     */
    @NotNull
    private String input;
}
