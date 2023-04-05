package cn.itcast.hotel.es;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static cn.itcast.hotel.consts.HotelConst.HOTEL_INDEX;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/5
 */
@SpringBootTest
public class HotelAggTest {

    @Autowired
    private RestHighLevelClient client;


    @Test
    void bucket() throws IOException {
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        request.source()
                .size(0)
                .aggregation(
                AggregationBuilders
                        .terms("brand_agg")
                        .field("brand")
                        .size(20)
        );
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // Aggregations aggregations = response.getAggregations();
        // 这里用子接口接收 org.elasticsearch.search.aggregations.bucket.terms.Terms
        Terms brandAgg = response.getAggregations().get("brand_agg");
        List<? extends Terms.Bucket> buckets = brandAgg.getBuckets();
        buckets.forEach(c-> {
            String brandName = c.getKeyAsString();
            System.out.println(brandName); // brandName
            long docCount = c.getDocCount();
            System.out.println(docCount);
        });
    }


}
