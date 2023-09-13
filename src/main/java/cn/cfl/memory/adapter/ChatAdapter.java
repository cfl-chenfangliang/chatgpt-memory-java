package cn.cfl.memory.adapter;

import cn.cfl.memory.core.ChatEntrance;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author chen.fangliang
 */
@Service
public class ChatAdapter {

    @Resource
    private ChatEntrance chatEntrance;

    public String simpleCompletions(Completion completion) {
        String conversationId = getConversationId(completion.getUserId());
        return chatEntrance.chat(conversationId, completion.getInput());
    }

    private String getConversationId(String userId) {
        if (userId == null) {
            userId = UUID.randomUUID().toString().replace("-", "");
        }
        return "CID_" + userId;
    }
}
