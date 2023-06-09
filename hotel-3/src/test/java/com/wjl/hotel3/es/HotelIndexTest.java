package com.wjl.hotel3.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.wjl.hotel3.consts.HotelConst;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/5
 */
@SpringBootTest
public class HotelIndexTest {

    @Autowired
    private ElasticsearchClient client;

    /**
     * 创建索引库
     * PUT /hotel
     * {
     * "mappings":{}
     * }
     */
    @SneakyThrows
    @Test
    void createIndex() throws IOException {
        /*
        Map<String, Property> propertyMap = new HashMap<>();
        propertyMap.put("id",Property.of(p -> p.keyword(KeywordProperty.of(k -> k.index(true)))));
        propertyMap.put("name",Property.of(p -> p.text(TextProperty.of(t -> t.index(true).analyzer("ik_max_word")))));
        propertyMap.put("address",Property.of(p -> p.keyword(KeywordProperty.of(k-> k.index(false)))));
        propertyMap.put("price",Property.of(p -> p.integer(IntegerNumberProperty.of(i-> i.index(true)))));
        propertyMap.put("score",Property.of(p -> p.integer(IntegerNumberProperty.of(i-> i.index(true)))));
        propertyMap.put("brand",Property.of(p -> p.keyword(KeywordProperty.of(k -> k.index(true).copyTo("all")))));
        propertyMap.put("city",Property.of(p-> p.keyword(k->k.index(true))));
        propertyMap.put("starName",Property.of(p -> p.keyword(k->k.index(true))));
        propertyMap.put("business",Property.of(p->p.keyword(k->k.index(true).copyTo("all"))));
        propertyMap.put("location",Property.of(p -> p.geoPoint(g -> g)));
        propertyMap.put("pic",Property.of(p -> p.text(t->t.index(false))));
        propertyMap.put("all",Property.of(p -> p.text(t -> t.index(true).analyzer("ik_max_word"))));
        CreateIndexResponse response = client.indices().create(c -> c.index(HotelConst.HOTEL_INDEX).mappings(m -> m.properties(propertyMap)));
        // CreateIndexResponse: {"index":"hotel","shards_acknowledged":true,"acknowledged":true}
        System.out.println(response);
         */
        // 方式2 基于json流创建
        CreateIndexRequest.Builder builder = new CreateIndexRequest.Builder();
        ByteArrayInputStream sbis = new ByteArrayInputStream(HotelConst.MAPPING_WITH_SUGG.getBytes(StandardCharsets.UTF_8));
        // TODO: 这里无法创建索引库 可能是8版本的自定义分词器
        builder.index(HotelConst.HOTEL_INDEX).withJson(sbis);
        CreateIndexResponse response = client.indices().create(builder.build());
        System.out.println(response);
        sbis.close();
    }


    /**
     * 判断是否存在
     */
    @Test
    void indexExists() throws IOException {
        BooleanResponse exists = client.indices().exists(e -> e.index(HotelConst.HOTEL_INDEX));
        System.out.println(exists.value());
    }


    /**
     * 删除索引库
     * DELETE /hotel
     */
    @Test
    void deleteIndex() throws IOException {
        DeleteIndexResponse response = client.indices().delete(d -> d.index(HotelConst.HOTEL_INDEX));
        System.out.println(response.acknowledged());
    }


}
