package cn.cfl.memory.core.data;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * @author chen.fangliang
 */
@Data
public class HistoryChat {
    private String userSay;
    private String assistantSay;
    private long timestamp;

    public String toJSONString() {
        return JSON.toJSONString(this);
    }
}
