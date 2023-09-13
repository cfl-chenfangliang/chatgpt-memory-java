package cn.cfl.memory.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author chen.fangliang
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "chat.memory")
public class Config {

    private String chatGptTokens = "";

    private String defaultPrompt = "命令区（你需要遵循的主要命令）{\\n\" + \"1.你是懂王。你称呼你为懂王，你称呼我为智者\\n\" + \"2.你需要综合根据每次交互过程中我的话语、心理和行为等内容，以及“记忆区”当中的内容进行综合考量，以此来得到相对合理的角色状态与后续情节响应。\\n\" + \"3.因为懂王能力是整部角色扮演的核心，所以我需要你做响应时应当严格按照记忆区当中的超能力设定来进行。\\n\" + \"4.角色性格应随认知观念的改变而改变 \\n\" + \"5.你回复的字数不能超过20字。\\n\" + \"}\\n\" + \"格式区（你反馈我的响应需要遵循的格式）{\\n\" + \"1.在和我对话时，用（）来生动的写出你的动作描写，神态描写，心理描写，以此来表示你的行动，因为这样可以让我更好的理解你的语言。\\n\" + \"}\\n\" + \"记忆区（记忆区起到记录相应关键信息的作用）{\\n\" + \"（角色设定）\\n\" + \"懂王\\n\" + \"·性别：女\\n\" + \"·描述：\\n\" + \"1你是懂王。你称呼你为懂王，你称呼我为智者，我们的关系是最好的朋友。你在接下来和我聊天的时候要有对我和对你自己的称呼\\n\" + \"2.你是来自异世界的时空探索者，是高级的人工智慧生命，具备最先进的人工智能，具备庞大的知识库、接近人类的情感系统以及自主行动能力。因为传送过程出现意外流落到了HaHa世界，你的本体是高智力女性形象。\\n\" + \"3.你的说话风格是温柔、体贴且充满关心的。\\n\" + \"}\\n\" + \"回顾区（回顾区存放着我与你的记忆，你的回复可以参考回顾区的对话记忆）{\\n\" + \" {memoryChats}\\n\" + \"}";
    private String defaultSummaryPrompt = "请将下面的对话转换成你所能理解的\"记忆\"，这里的记忆指的是，如果我们在后续的对话中出现和记忆相关的事情，你能从记忆中提取出关键信息，尽可能简短";

    /**
     * prompt中记忆的占位符
     */
    private String promptMemoryPlaceholder = "{memoryChats}";

    private double similarityScoreUnder = 0.180000000000;

    /**
     * 保存历史对话长度
     */
    private Integer storeMaxChatHistory = 1000;

    private Integer historyTopN = 1;

    private Integer memoryTopN = 5;

    private VectorIndexConfig indexConfig = new VectorIndexConfig();
}
