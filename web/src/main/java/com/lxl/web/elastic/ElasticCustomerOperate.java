package com.lxl.web.elastic;

import com.alibaba.fastjson.JSONObject;
import com.lxl.common.entity.BaseEntity;
import com.lxl.utils.config.ConfUtil;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticCustomerOperate extends ElasticsearchRestTemplate {
    public ElasticCustomerOperate(RestHighLevelClient client) {
        super(client);
    }

    public boolean createAndPutMapping(Class<? extends BaseEntity> cls) {
        indexOps(IndexCoordinates.of(getIndexName(cls))).create();
        IndexOperations indexOperations = indexOps(cls);
        return indexOperations.putMapping(indexOperations.createMapping());
    }

    private UpdateResponse custUpdate(UpdateQuery query, ScriptType scriptType, Class<? extends BaseEntity> baseEntityClass) {
        UpdateRequest request = getRequest(query, getIndexName(baseEntityClass), scriptType);
        UpdateResponse.Result result = UpdateResponse.Result
                .valueOf(execute(client -> client.update(request, RequestOptions.DEFAULT)).getResult().name());
        return new UpdateResponse(result);
    }

    public boolean updateByField(Class entityClass, Long id, JSONObject jsonObject) {
        UpdateQuery query = UpdateQuery.builder(id + "")
                .withDocument(org.springframework.data.elasticsearch.core.document.Document.from(jsonObject))
                .build();
        return custUpdate(query, null, entityClass).getResult().equals(UpdateResponse.Result.UPDATED);
    }

    private String getIndexName(Class<? extends BaseEntity> entityClass) {
        if (entityClass.isAnnotationPresent(Document.class)) {
            Document annotation = entityClass.getAnnotation(Document.class);
            String indexName = annotation.indexName();
            String[] split = indexName.split("#");
            return split[0] + ConfUtil.getPropertyOrDefault("spring.elasticsearch.indexSuffix", "");
        }
        return null;
    }

    private boolean doUpdate(Class<? extends BaseEntity> entityClass, Long id, String script,
                             ScriptType scriptType, Map<String, Object> params) {
        UpdateQuery updateQuery = UpdateQuery.builder(id + "")
                .withParams(params)
                .withScript(script).build();
        UpdateResponse updateResponse = custUpdate(updateQuery, scriptType, entityClass);
        return updateResponse.getResult().equals(UpdateResponse.Result.UPDATED);
    }

    public boolean updateByScriptInline(Class<? extends BaseEntity> entityClass, Long id, String script) {
        return doUpdate(entityClass, id, script);
    }

    private boolean doUpdate(Class<? extends BaseEntity> entityClass, Long id, String script) {
        return doUpdate(entityClass, id, script, ScriptType.INLINE, null);
    }

    private UpdateRequest getRequest(UpdateQuery query, String indexName, ScriptType scriptType) {
        UpdateRequest updateRequest = new UpdateRequest(indexName, query.getId());
        if (query.getScript() != null) {
            Map<String, Object> params = query.getParams();

            if (params == null) {
                params = new HashMap<>();
            }
            Script script;
            if (scriptType.equals(ScriptType.STORED)) {
                script = new Script(scriptType, null, query.getScript(), params);
            } else {
                script = new Script(scriptType, query.getLang(), query.getScript(), params);
            }
            updateRequest.script(script);
        }

        if (query.getDocument() != null) {
            updateRequest.doc(query.getDocument());
        }

        if (query.getUpsert() != null) {
            updateRequest.upsert(query.getUpsert());
        }

        if (query.getRouting() != null) {
            updateRequest.routing(query.getRouting());
        }

        if (query.getScriptedUpsert() != null) {
            updateRequest.scriptedUpsert(query.getScriptedUpsert());
        }

        if (query.getDocAsUpsert() != null) {
            updateRequest.docAsUpsert(query.getDocAsUpsert());
        }

        if (query.getFetchSource() != null) {
            updateRequest.fetchSource(query.getFetchSource());
        }

        if (query.getFetchSourceIncludes() != null || query.getFetchSourceExcludes() != null) {
            List<String> includes = query.getFetchSourceIncludes() != null ? query.getFetchSourceIncludes()
                    : Collections.emptyList();
            List<String> excludes = query.getFetchSourceExcludes() != null ? query.getFetchSourceExcludes()
                    : Collections.emptyList();
            updateRequest.fetchSource(includes.toArray(new String[0]), excludes.toArray(new String[0]));
        }

        if (query.getIfSeqNo() != null) {
            updateRequest.setIfSeqNo(query.getIfSeqNo());
        }

        if (query.getIfPrimaryTerm() != null) {
            updateRequest.setIfPrimaryTerm(query.getIfPrimaryTerm());
        }

        if (query.getRefresh() != null) {
            updateRequest.setRefreshPolicy(query.getRefresh().name().toLowerCase());
        }

        if (query.getRetryOnConflict() != null) {
            updateRequest.retryOnConflict(query.getRetryOnConflict());
        }

        if (query.getTimeout() != null) {
            updateRequest.timeout(query.getTimeout());
        }

        if (query.getWaitForActiveShards() != null) {
            updateRequest.waitForActiveShards(ActiveShardCount.parseString(query.getWaitForActiveShards()));
        }

        return updateRequest;
    }

}
