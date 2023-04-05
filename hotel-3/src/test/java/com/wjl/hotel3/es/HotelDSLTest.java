package com.wjl.hotel3.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import com.wjl.hotel3.model.pojo.HotelDoc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.wjl.hotel3.consts.HotelConst.HOTEL_INDEX;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/5
 */
@SpringBootTest
public class HotelDSLTest {

    @Autowired
    private ElasticsearchClient client;

    <T> void printResponse(SearchResponse<HotelDoc> response) {
        long total = response.hits().total().value();
        System.out.println("一共搜到" + total + "条数据");
        for (Hit<HotelDoc> hit : response.hits().hits()) {
            Map<String, List<String>> highlight = hit.highlight();
            if (!CollectionUtils.isEmpty(highlight)) {
                List<String> highList = highlight.get("name");
                String name = highList.get(0);
                hit.source().setName(name);
            }
            System.out.println(hit.source());
        }
    }

    /**
     * GET /hotel/_search
     * {
     * "query": {
     * "match_all": {}
     * }
     * }
     */
    @Test
    void matchAll() throws IOException {
        MatchAllQuery.of(v -> v);
        // SearchRequest request = SearchRequest.of(fn -> fn.query(Query.of(q -> q.matchAll(v -> v))));
        // SearchRequest request = new SearchRequest.Builder().query().build();
        // SearchRequest request = SearchRequest.of(s -> s.index(HOTEL_INDEX).query(MatchAllQuery.of(v -> v)._toQuery()));
        SearchRequest request = SearchRequest.of(s -> s.index(HOTEL_INDEX).query(QueryBuilders.matchAll(v -> v)));
        SearchResponse<HotelDoc> response = client.search(request, HotelDoc.class);
        printResponse(response);
    }


    /**
     * GET /hotel/_search
     * {
     * "query": {
     * "match": {
     * "all": "如家"
     * }
     * }
     * }
     */
    @Test
    void match() throws IOException {
        SearchRequest request = SearchRequest.of(s -> s.index(HOTEL_INDEX).query(QueryBuilders.match(m -> m.field("all").query("如家"))));
        SearchResponse<HotelDoc> response = client.search(request, HotelDoc.class);
        printResponse(response);
    }

    /**
     * GET /hotel/_search
     * {
     * "query": {
     * "bool": {
     * "must": [
     * {
     * "term": {
     * "city": {
     * "value": "上海"
     * }
     * }
     * }
     * ],
     * "filter": {
     * "range": {
     * "price": {
     * "lte": 250
     * }
     * }
     * }
     * }
     * }
     * }
     */
    @Test
    void boolQuery() throws IOException {
        Query bool = QueryBuilders.bool(builder ->
                builder
                        .must(QueryBuilders.match(m -> m.field("city").query("上海")))
                        .filter(QueryBuilders.range(r -> r.field("price").lte(JsonData.of(250))))
        );
        // 你妈的 真费劲
        SearchRequest request = SearchRequest.of(s -> s.index(HOTEL_INDEX).query(bool));
        SearchResponse<HotelDoc> response = client.search(request, HotelDoc.class);
        printResponse(response);
    }


    @Test
    void pageAndSort() throws IOException {
        int page = 3;
        int size = 5;
        page = (page - 1) * size;
        int finalPage = page;
        SearchRequest request = SearchRequest.of(
                s ->
                        s.index(HOTEL_INDEX).query(QueryBuilders.matchAll().build()._toQuery())
                                .sort(
                                        SortOptions.of(
                                                t -> t.field(
                                                        FieldSort.of(r -> r.field("price").order(SortOrder.Asc)))
                                        )
                                )
                                .from(finalPage)
                                .size(size)
        );
        SearchResponse<HotelDoc> response = client.search(request, HotelDoc.class);
        printResponse(response);
    }


    @Test
    void highlighter() throws IOException {
        SearchRequest request = SearchRequest.of(
                s -> s.index(HOTEL_INDEX)
                        .query(QueryBuilders.match(q -> q.field("name").query("如家")))
                        .highlight(
                                builder ->
                                        builder.fields("name", high -> high.preTags("<li>").postTags("</li>"))
                        )
        );
        SearchResponse<HotelDoc> response = client.search(request, HotelDoc.class);
        printResponse(response);
    }


    @Test
    void funcQuery() throws IOException {
        SearchRequest request = SearchRequest.of(
                s -> s.index(HOTEL_INDEX).query(
                        QueryBuilders
                                // 定义函数查询
                                .functionScore(
                                        // 定义计算函数
                                        q -> q // 主要查询条件
                                                .query(
                                                        QueryBuilders
                                                                .match(m -> m.field("name").query("如家"))
                                                )
                                                // 函数
                                                .functions(
                                                        builder ->
                                                                builder // 过滤条件 价格小于159的加权重
                                                                        //.filter(fn -> fn.range(r -> r.field("price").lte(JsonData.of(150))))
                                                                        .filter(fn -> fn.range(RangeQuery.of(v -> v.field("price").lte(JsonData.of(159)))))
                                                                        // 算分函数
                                                                        .weight(100D)
                                                )
                                                .scoreMode(FunctionScoreMode.Sum)
                                )
                ));
        SearchResponse<HotelDoc> response = client.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hits = response.hits();
        System.out.println(hits.total().value() + "条");
        for (Hit<HotelDoc> hit : hits.hits()) {
            System.out.println(hit.source());
            System.out.println(hit.score() + "分");
        }
    }
}
