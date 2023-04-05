package cn.itcast.hotel.es;

import cn.itcast.hotel.model.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;

import static cn.itcast.hotel.consts.HotelConst.HOTEL_INDEX;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/5
 */
@SpringBootTest
public class HotelDSLTest {

    @Autowired
    private RestHighLevelClient client;

    void printResponse(SearchResponse response) {
        SearchHits hits = response.getHits();
        long count = hits.getTotalHits().value;
        System.out.println("一共" + count + "条数据");
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField name = highlightFields.get("name");
                String string = name.getFragments()[0].string();
                hotelDoc.setName(string);
            }
            System.out.println(hotelDoc);
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
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        request.source().query(QueryBuilders.matchAllQuery());
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
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
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        request.source()
                .query(QueryBuilders.matchQuery("name", "如家"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        printResponse(response);
    }

    /*
GET /hotel/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "term": {
            "city": {
              "value": "上海"
            }
          }
        }
      ],
      "filter": {
        "range": {
          "price": {
            "lte": 250
          }
        }
      }
    }
  }
}
     */
    @Test
    void boolQuery() throws IOException {
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        // boolQuery
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termQuery("city", "上海"))
                .filter(QueryBuilders.rangeQuery("price").lte(250));

        request.source().query(query);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        printResponse(response);
    }


    @Test
    void pageAndSort() throws IOException {
        int page = 3;
        int size = 5;
        page = (page - 1) * size;
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        request.source().query(QueryBuilders.matchAllQuery())
                .from(page)
                .size(size)
                .sort("price", SortOrder.ASC)
        ;
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        printResponse(response);
    }


    @Test
    void highlighter() throws IOException {
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        request.source()
                .query(QueryBuilders.matchQuery("name", "如家"))
                // field字段一定要写
                .highlighter(new HighlightBuilder().field("name").preTags("<li>").postTags("</li>").requireFieldMatch(true));
        // 这个空指针异常
        //.highlighter().requireFieldMatch(false).preTags("<li>").postTags("</li>");
        /// 高亮字段是否与查询字段匹配
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        printResponse(response);
    }

    @Test
    void funcQuery() throws IOException {
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        // 过滤条件 和 加权
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] prices =
                QueryBuilders
                        .functionScoreQuery(QueryBuilders.rangeQuery("price").lte(500), ScoreFunctionBuilders.weightFactorFunction(20))
                        .filterFunctionBuilders();


        FunctionScoreQueryBuilder query = QueryBuilders
                .functionScoreQuery(QueryBuilders.matchQuery("name", "如家"), prices)
                .scoreMode(FunctionScoreQuery.ScoreMode.SUM);
        request.source().query(query);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits().value);
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
            System.out.println(hit.getScore());
        }
    }
}
