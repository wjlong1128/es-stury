package com.wjl.hotel3.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.wjl.hotel3.model.pojo.HotelDoc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/5
 */
@SpringBootTest
public class HotelAggTest {

    @Autowired
    private ElasticsearchClient client;

    @Test
    void bucket() throws IOException {
        SearchRequest request = new SearchRequest.Builder().size(0).aggregations("brand_agg", b -> b.terms(t -> t.field("brand").size(20))).build();
        SearchResponse<HotelDoc> response = client.search(request, HotelDoc.class);
        Map<String, Aggregate> aggregations = response.aggregations();
        StringTermsAggregate brandAgg = aggregations.get("brand_agg").sterms();
        for (StringTermsBucket bucket : brandAgg.buckets().array()) {
            System.out.println(bucket.key().stringValue());
            System.out.println(bucket.docCount());
        }

    }
}
