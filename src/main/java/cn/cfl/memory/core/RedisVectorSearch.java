package cn.cfl.memory.core;

import cn.cfl.memory.core.config.Config;
import cn.cfl.memory.core.config.VectorIndexConfig;
import cn.cfl.memory.core.data.SearchData;
import cn.cfl.memory.core.data.SearchResult;
import cn.cfl.memory.core.utils.ByteUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.schemafields.TagField;
import redis.clients.jedis.search.schemafields.TextField;
import redis.clients.jedis.search.schemafields.VectorField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 基于redis search的向量数据库
 *
 * @author chen.fangliang
 */
@Slf4j
public class RedisVectorSearch implements IndexManage, IVectorSearch {

    private static final String FIELD_CONVERSATION_ID = "conversation_id";
    private static final String FIELD_USER_SAY = "user_say";
    private static final String FIELD_ASSISTANT_SAY = "assistant_say";
    private static final String FIELD_SUMMARY = "summary";
    private static final String FIELD_VECTOR_SCORE = "vector_score";

    private final UnifiedJedis jedis;

    @SuppressWarnings("all")
    private final Config config;

    private final VectorIndexConfig indexConfig;

    public RedisVectorSearch(Config config, UnifiedJedis jedis) {
        this.jedis = jedis;
        this.config = config;
        this.indexConfig = config.getIndexConfig();
    }

    @Override
    public String storeVector(SearchData searchData) {
        String uniqueId = UUID.randomUUID().toString().replace("-", "");

        Map<byte[], byte[]> saveMap = new HashMap<>(5);

        saveMap.put(ByteUtil.getBytes(indexConfig.getVectorField()), searchData.getEmbedding());
        saveMap.put(ByteUtil.getBytes(FIELD_CONVERSATION_ID), ByteUtil.getBytes(searchData.getConversationId()));
        saveMap.put(ByteUtil.getBytes(FIELD_USER_SAY), ByteUtil.getBytes(searchData.getUserSay()));
        saveMap.put(ByteUtil.getBytes(FIELD_ASSISTANT_SAY), ByteUtil.getBytes(searchData.getAssistantSay()));
        saveMap.put(ByteUtil.getBytes(FIELD_SUMMARY), ByteUtil.getBytes(searchData.getSummary()));
        jedis.hset(ByteUtil.getBytes(uniqueId), saveMap);
        return uniqueId;
    }

    @Override
    public List<SearchResult> search(String conversationId, byte[] vecParam, Integer topN) {
        // redis search语法
        Query q = new Query("(@" + FIELD_CONVERSATION_ID + ":{" + conversationId + "})=>[KNN " + topN + " @" + indexConfig.getVectorField() + " $vec_param AS " + FIELD_VECTOR_SCORE + "]");
        q.setSortBy(FIELD_VECTOR_SCORE, true);
        q.limit(0, topN);
        q.returnFields(FIELD_CONVERSATION_ID, FIELD_VECTOR_SCORE, FIELD_SUMMARY, FIELD_USER_SAY, FIELD_ASSISTANT_SAY);
        q.dialect(2);
        q.addParam("vec_param", vecParam);

        redis.clients.jedis.search.SearchResult searchResult = jedis.ftSearch(indexConfig.getIndexName(), q);
        List<Document> documents = searchResult.getDocuments();

        log.info("搜索历史对话，查询结果长度：{}", CollectionUtils.isNotEmpty(documents) ? documents.size() : 0);
        if (CollectionUtils.isNotEmpty(documents)) {
            return documents.stream().map(item -> {
                SearchResult result = new SearchResult();
                result.setConversationId(item.getString(FIELD_CONVERSATION_ID));
                result.setUserSay(item.getString(FIELD_USER_SAY));
                result.setAssistantSay(item.getString(FIELD_ASSISTANT_SAY));
                result.setSummary(item.getString(FIELD_SUMMARY));
                result.setScore(Double.parseDouble(item.getString(FIELD_VECTOR_SCORE)));
                return result;
            }).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public boolean similarityMemory(String conversationId, byte[] vecParam) {
        List<SearchResult> searchResults = search(conversationId, vecParam, 1);
        if (CollectionUtils.isNotEmpty(searchResults)) {
            SearchResult result = searchResults.get(0);
            // 越小越相似
            if (result.getScore() < config.getSimilarityScoreUnder()) {
                log.info("相似文本，得分：{}", result.getScore());
                return true;
            }
        }
        return false;
    }

    @Override
    public void createIndex() throws Exception {
        String indexName = indexConfig.getIndexName();
        try {
            Map<String, Object> idx = jedis.ftInfo(indexName);
            if (MapUtils.isNotEmpty(idx)) {
                log.info("索引已存在");
                return;
            }
        } catch (Exception e) {
            // ignore
        }

        VectorField vectorField = VectorField.builder()
                .fieldName(indexConfig.getVectorField())
                .algorithm(VectorField.VectorAlgorithm.HNSW)
                .addAttribute("TYPE", indexConfig.getType())
                .addAttribute("DIM", indexConfig.getDim())
                .addAttribute("DISTANCE_METRIC", indexConfig.getDistanceMetric())
                .addAttribute("INITIAL_CAP", indexConfig.getInitialCap())
                .addAttribute("M", indexConfig.getM())
                .addAttribute("EF_CONSTRUCTION", indexConfig.getEfConstruction())
                .build();

        TextField userSayField = TextField.of(FIELD_USER_SAY);
        TextField assistantSayField = TextField.of(FIELD_ASSISTANT_SAY);
        TextField summaryField = TextField.of(FIELD_SUMMARY);
        TagField tagField = TagField.of(FIELD_CONVERSATION_ID);

        jedis.ftCreate(indexName, vectorField, summaryField, userSayField, tagField, assistantSayField);
        log.info("创建索引【{}】成功", indexName);
    }
}
