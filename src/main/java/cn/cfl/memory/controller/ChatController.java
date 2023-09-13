package cn.cfl.memory.controller;

import cn.cfl.memory.adapter.ChatAdapter;
import cn.cfl.memory.adapter.Completion;
import cn.cfl.memory.http.RestStructure;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chen.fangliang
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatAdapter chatAdapter;

    @PostMapping("/completions")
    public RestStructure<String> simpleCompletions(Completion completion) {
        String gprSay = chatAdapter.simpleCompletions(completion);
        return RestStructure.success(gprSay);
    }

}
