package com.wjl.hotel3.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import com.wjl.hotel3.mapper.HotelMapper;
import com.wjl.hotel3.model.pojo.Hotel;
import com.wjl.hotel3.model.pojo.HotelDoc;
import com.wjl.hotel3.model.query.QueryParams;
import com.wjl.hotel3.model.result.PageResult;
import com.wjl.hotel3.service.IHotelService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.wjl.hotel3.consts.HotelConst.HOTEL_INDEX;

@RequiredArgsConstructor
@Service
public class HotelService implements IHotelService {

    private final HotelMapper hotelMapper;
    private final ElasticsearchClient client;

    @Override
    public Optional<Hotel> findHotelById(long id) {
        return hotelMapper.findById(id);
    }

    @Override
    public List<HotelDoc> list() {
        List<Hotel> all = hotelMapper.findAll();
        return all.stream().map(HotelDoc::new).collect(Collectors.toList());
    }

    @Override
    public PageResult<HotelDoc> search(QueryParams params) throws IOException {
        String city = params.getCity();
        String key = params.getKey();
        Integer page = params.getPage();
        Integer size = params.getSize();
        String brand = params.getBrand();
        String starName = params.getStarName();
        Integer maxPrice = params.getMaxPrice();
        Integer minPrice = params.getMinPrice();
        String sortBy = params.getSortBy();
        String location = params.getLocation();

        // 这里由于要组合条件 使用的是BuilderAPI 而不是惯用
        Query basicQuery = null;
        if (StringUtils.isBlank(key)) {
            basicQuery = QueryBuilders.matchAll().build()._toQuery();
        } else {
            basicQuery = QueryBuilders.match(v -> v.field("all").query(key));
        }

        BoolQuery.Builder boolQueryBuilder = QueryBuilders
                .bool()
                .must(basicQuery);
        if (!StringUtils.isBlank(city)) {
            boolQueryBuilder.filter(r -> r.term(fn -> fn.field("city").value(city)));
        }
        if (!StringUtils.isBlank(brand)) {
            boolQueryBuilder.filter(f -> f.term(fn -> fn.field("brand").value(brand)));
        }
        if (!StringUtils.isBlank(starName)) {
            boolQueryBuilder.filter(f -> f.term(fn -> fn.field("starName").value(starName)));
        }
        if (!ObjectUtils.isEmpty(minPrice) && !ObjectUtils.isEmpty(maxPrice)) {
            boolQueryBuilder.filter(fn -> fn.range(f -> f.field("price").lte(JsonData.of(maxPrice)).gte(JsonData.of(minPrice))));
        }

        Query boolQuery = boolQueryBuilder.build()._toQuery();

        // 函数算分查询 将标记广告的记录移动到前面
        Query query = QueryBuilders.functionScore(
                // 基础查询
                q -> q.query(boolQuery)
                        .functions(fn -> fn.filter(f -> f.term(t -> t.field("isAD").value(true))).weight(10D))
                        .scoreMode(FunctionScoreMode.Multiply)
        );
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(HOTEL_INDEX)
                .query(query)
                .from((page - 1) * size)
                .size(size);


        if (!StringUtils.isBlank(location)) {
            builder.sort(new SortOptions(
                    new GeoDistanceSort.Builder()
                            .field("location")
                            .location(new GeoLocation.Builder().text(location).build())
                            .unit(DistanceUnit.Kilometers)
                            .order(SortOrder.Asc)
                            .build())
            );
        }
        SearchRequest request = builder.build();
        SearchResponse<HotelDoc> response = client.search(request, HotelDoc.class);

        PageResult<HotelDoc> result = new PageResult<>();
        HitsMetadata<HotelDoc> hits = response.hits();
        result.setTotal(hits.total().value());
        List<HotelDoc> docList = hits.hits().stream().map(hit -> {
            HotelDoc doc = hit.source();
            // 条件判断地理位置 根据地理距离位置排序后，排序值就是地理位置距离
            List<FieldValue> sort = hit.sort();
            if (!CollectionUtils.isEmpty(sort)) {
                FieldValue fieldValue = sort.get(0);
                String distance = fieldValue._toJsonString();
                doc.setDistance(distance);
            }
            return doc;
        }).collect(Collectors.toList());
        result.setHotels(docList);
        return result;
    }
}
