package com.wjl.hotel3;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.wjl.hotel3.mapper.HotelMapper;
import com.wjl.hotel3.model.pojo.Hotel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class Hotel3ApplicationTests {

    @Autowired
    private ElasticsearchClient client;

    @Test
    void testIndex() throws IOException {
        CreateIndexResponse yyyy = client.indices().create(c -> c.index("yyyy"));
        System.out.println(yyyy);
    }

    @Autowired
    private HotelMapper hotelMapper;

    @Test
    void testJdbc() {
        for (Hotel so : hotelMapper.findAll()) {
            System.out.println(so);
        }
    }

    @Test
    void getOne() {
       Hotel hotel =  hotelMapper.findOneByName("速8酒店(上海赤峰路店)");
        System.out.println(hotel);


    }
}
