package cn.cfl.memory.core;

import cn.cfl.memory.core.data.SearchData;
import cn.cfl.memory.core.data.SearchResult;

import java.util.List;

/**
 * @author chen.fangliang
 */
public interface IVectorSearch {

    /**
     * 保存聊天向量
     *
     * @param searchData 聊天数据
     * @return 数据的唯一id
     */
    String storeVector(SearchData searchData);

    /**
     * 向量搜索
     *
     * @param conversationId 对话id
     * @param vecParam       向量byte数据
     * @param topN           返回最相似的N个数据
     * @return 结果集合，根据topN返回多个结果
     */
    List<SearchResult> search(String conversationId, byte[] vecParam, Integer topN);

    /**
     * 判断是否是相似向量，用于判断对话是否类似
     *
     * @param conversationId 对话id
     * @param vecParam       向量byte数据
     * @return 相似返回true
     */
    boolean similarityMemory(String conversationId, byte[] vecParam);
}
