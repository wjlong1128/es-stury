package cn.itcast.hotel.es;

import cn.itcast.hotel.service.IHotelService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
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

    @Autowired
    private IHotelService hotelService;

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
        buckets.forEach(c -> {
            String brandName = c.getKeyAsString();
            System.out.println(brandName); // brandName
            long docCount = c.getDocCount();
            System.out.println(docCount);
        });
    }

    /**
     * GET /hotel/_search
     * {
     *     "suggest": {
     *         "mySuggest": {
     *         "text": "h",
     *         "completion": {
     *                  "field": "suggestion",
     *                 "skip_duplicates":true,
     *                 "size":10
     *             }
     *         }
     *     }
     * }
     *
     * @throws IOException
     */
    @Test
    void suggest() throws IOException {
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        request.source()
                .suggest(
                        new SuggestBuilder()
                                .addSuggestion("mySuggest", new CompletionSuggestionBuilder("suggestion").skipDuplicates(true).size(10).prefix("hz"))
                );
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> mySuggest = response.getSuggest().getSuggestion("mySuggest");

        for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry : mySuggest) {
            for (Suggest.Suggestion.Entry.Option option : entry) {
                System.out.println(option.getText().string());
            }
        }
    }
}
