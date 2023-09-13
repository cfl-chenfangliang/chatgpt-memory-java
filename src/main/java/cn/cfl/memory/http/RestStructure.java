package cn.cfl.memory.http;

import lombok.Data;

import java.io.Serializable;

/**
 * @author chen.fangliang
 */
@Data
public class RestStructure<T> implements Serializable {

    private Integer code;

    private String message;

    private T data;

    public RestStructure() {

    }

    public static <T> RestStructure<T> success(T data) {
        return new RestStructure<>(data);
    }

    public RestStructure(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public RestStructure(T data) {
        this.code = 0;
        this.data = data;
    }

    public String getMsg() {
        return message;
    }

}
