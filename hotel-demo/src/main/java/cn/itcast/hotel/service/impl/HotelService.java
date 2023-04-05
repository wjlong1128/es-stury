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
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static cn.itcast.hotel.consts.HotelConst.HOTEL_INDEX;

@RequiredArgsConstructor
@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    private final RestHighLevelClient restHighLevelClient;

    @Override
    public PageResult<HotelDoc> search(QueryParams params) throws IOException {
        SearchRequest request = new SearchRequest(HOTEL_INDEX);
        // 判断是否存在
        String key = params.getKey();
        Integer page = params.getPage();
        Integer size = params.getSize();
        String city = params.getCity();
        String brand = params.getBrand();
        String starName = params.getStarName();
        Integer maxPrice = params.getMaxPrice();
        Integer minPrice = params.getMinPrice();
        String location = params.getLocation();

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


        request
                .source()
                //.query(boolQueryBuilder)
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

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        List<HotelDoc> docList = Arrays.stream(hits.getHits()).map(hit -> {
            HotelDoc hotelDoc = JSON.parseObject(hit.getSourceAsString(), HotelDoc.class);
            // 定位排序之后返回的排序字段就是位置值
            if (ObjectUtils.isEmpty(hit.getSortValues()) && hit.getSortValues().length > 0) {
                Object sortValue = hit.getSortValues()[0];
                hotelDoc.setDistance(sortValue);
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

}
