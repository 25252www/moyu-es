package com.example.moyues.service;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BlogService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    // 搜索功能
    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws Exception {
        SearchRequest searchRequest = new SearchRequest("moyublog");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 分页
        searchSourceBuilder.from(pageNo*pageSize);
        searchSourceBuilder.size(pageSize);

        // 多字段搜索
        searchSourceBuilder.query(QueryBuilders
                .multiMatchQuery(keyword, "title", "content"));
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 查询结果过滤，节省带宽
        String[] excludes = {"content"};
        searchSourceBuilder.fetchSource(null, excludes);

        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        // 执行搜索
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
            // 把title, content中的高亮内容替换
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            if (title != null) {
                Text[] fragments = title.fragments();
                String n_title = "";
                for (Text fragment : fragments) {
                    n_title += fragment;
                }
                searchHit.getSourceAsMap().put("title", n_title);
            }
            HighlightField content = highlightFields.get("content");
            if (content != null) {
                Text[] fragments = content.fragments();
                String n_content = "";
                for (Text fragment : fragments) {
                    n_content += fragment;
                }
                searchHit.getSourceAsMap().put("content", n_content);
            }
            list.add(searchHit.getSourceAsMap());
        }
        return list;
    }
}
