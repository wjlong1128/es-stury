package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.model.pojo.Hotel;
import cn.itcast.hotel.model.pojo.HotelDoc;
import cn.itcast.hotel.model.query.QueryParams;
import cn.itcast.hotel.model.result.PageResult;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.itcast.hotel.consts.HotelConst.HOTEL_INDEX;

@RequiredArgsConstructor
@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    private final RestHighLevelClient restHighLevelClient;

    @Override
    public PageResult<HotelDoc> search(QueryParams params) throws IOException {
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        Integer page = params.getPage();
        Integer size = params.getSize();
        String location = params.getLocation();
        // 判断是否存在
        FunctionScoreQueryBuilder isADQuery = getFunctionScoreQueryBuilder(params);

        // 根据地图字段排序
        request
                .source()
                .query(isADQuery)
                .from((page - 1) * size)
                .size(size);
        if (!StringUtils.isBlank(location)) {
            request.source()
                    .sort(
                            SortBuilders
                                    .geoDistanceSort("location", new GeoPoint(location))
                                    .order(SortOrder.ASC)
                                    .unit(DistanceUnit.KILOMETERS)
                    );
        }
        if (!StringUtils.isBlank(params.getKey())) {
            request.source().highlighter(
                    new HighlightBuilder().field("name").requireFieldMatch(false)
                            .preTags("<em style=\"color:red;\">").postTags("</em>")
            );
        }

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        List<HotelDoc> docList = Arrays.stream(hits.getHits()).map(hit -> {
            HotelDoc hotelDoc = JSON.parseObject(hit.getSourceAsString(), HotelDoc.class);
            // 定位排序之后返回的排序字段就是位置值
            if (ObjectUtils.isEmpty(hit.getSortValues()) && hit.getSortValues().length > 0) {
                Object sortValue = hit.getSortValues()[0];
                hotelDoc.setDistance(sortValue);
            }
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                String name = highlightFields.get("name").getFragments()[0].string();
                hotelDoc.setName(name);
            }
            return hotelDoc;
        }).collect(Collectors.toList());
        PageResult<HotelDoc> result = new PageResult<>();
        result.setSize(size);
        result.setPage(page);
        result.setTotal(hits.getTotalHits().value);
        result.setHotels(docList);

        return result;
    }


    /**
     * 分组查询信息
     *
     * @return
     */
    @Override
    public Map<String, List<String>> filters(QueryParams params) throws IOException {
        HashMap<String, List<String>> result = new HashMap<>();
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        FunctionScoreQueryBuilder query = getFunctionScoreQueryBuilder(params);
        request.source()
                .query(query)
                .size(0)
                .aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(100))
                .aggregation(AggregationBuilders.terms("starAgg").field("starName").size(100))
                .aggregation(AggregationBuilders.terms("cityAgg").field("city").size(100));

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        List<String> brandAgg = getAggByName(response, "brandAgg");
        result.put("brand", brandAgg);
        List<String> cityAgg = getAggByName(response, "cityAgg");
        result.put("city", cityAgg);
        List<String> starAgg = getAggByName(response, "starAgg");
        result.put("starName", starAgg);
        return result;
    }

    @Override
    public List<String> suggestion(String key) throws IOException {
        if (StringUtils.isBlank(key)) {
            return Collections.emptyList();
        }
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggestion");
        suggestion.size(10).prefix(key).skipDuplicates(true);
        suggestBuilder.addSuggestion("mySuggestion", suggestion);
        request.source().suggest(suggestBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        CompletionSuggestion mySuggestion = response.getSuggest().getSuggestion("mySuggestion");
        List<String> options = mySuggestion.getOptions().stream().map(option -> option.getText().string()).collect(Collectors.toList());
        return options;
    }

    @Override
    public void deleteById(Long id) {
        try {
            restHighLevelClient.delete(new DeleteRequest(HOTEL_INDEX).id(id.toString()), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("删除es数据失败", e);
        }
    }

    @Override
    public void insertById(Long id) {
        // 这里实际远程调用
        Hotel hotel = this.getById(id);
        try {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            restHighLevelClient.index(new IndexRequest(HOTEL_INDEX).id(id.toString()).source(JSON.toJSONString(hotelDoc), XContentType.JSON), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("es添加或者更新失败 id:" + id, e);
        }
    }

    private List<String> getAggByName(SearchResponse response, String aggName) {
        Terms agg = response.getAggregations().get(aggName);
        List<? extends Terms.Bucket> buckets = agg.getBuckets();
        ArrayList<String> aggList = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            aggList.add(bucket.getKeyAsString());
        }
        return aggList;
    }


    private static FunctionScoreQueryBuilder getFunctionScoreQueryBuilder(QueryParams params) {
        String key = params.getKey();
        String city = params.getCity();
        String brand = params.getBrand();
        String starName = params.getStarName();
        Integer maxPrice = params.getMaxPrice();
        Integer minPrice = params.getMinPrice();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        QueryBuilder queryBuilder;
        if (StringUtils.isBlank(key)) {
            queryBuilder = QueryBuilders.matchAllQuery();
        } else {
            queryBuilder = QueryBuilders.matchQuery("all", key);
        }

        boolQueryBuilder.must(queryBuilder);
        if (!StringUtils.isBlank(city)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("city", city));
        }

        if (!StringUtils.isBlank(brand)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand", brand));
        }

        if (!StringUtils.isBlank(starName)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("starName", starName));
        }

        if (minPrice != null && maxPrice != null) {
            // 小于等于maxprice
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(maxPrice).gte(minPrice));
        }

        FunctionScoreQueryBuilder isADQuery =
                QueryBuilders.functionScoreQuery(
                        // 原始查询
                        boolQueryBuilder,
                        // 函数查询 过滤条件 满足条件的算分
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                        QueryBuilders.termQuery("isAD", true),
                                        ScoreFunctionBuilders.weightFactorFunction(10))
                        }
                ).scoreMode(FunctionScoreQuery.ScoreMode.MULTIPLY);
        return isADQuery;
    }


}
