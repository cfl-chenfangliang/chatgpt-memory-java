package cn.cfl.memory.core.enums;

import lombok.Getter;

/**
 * @author chen.fangliang
 */
public enum GptRole {

    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    @Getter
    private final String role;

    GptRole(String system) {
        this.role = system;
    }
}
