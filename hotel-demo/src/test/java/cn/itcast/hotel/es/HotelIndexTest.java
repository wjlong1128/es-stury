package cn.itcast.hotel.es;

import cn.itcast.hotel.consts.HotelConst;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/4
 */
@SpringBootTest
public class HotelIndexTest {

     @Autowired
    private RestHighLevelClient client;


   // @BeforeEach
    void setup() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://localhost:9200")
        ));
    }

    /**
     * 创建索引库
     * PUT /hotel
     * {
     *
     * }
     */
    @SneakyThrows
    @Test
    void createIndex() {
        CreateIndexRequest request = new CreateIndexRequest(HotelConst.HOTEL_INDEX);
        request.source(HotelConst.MAPPING, XContentType.JSON);
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }

    /**
     * 判断是否存在
     */
    @Test
    void indexExists() throws IOException {
        GetIndexRequest request = new GetIndexRequest(HotelConst.HOTEL_INDEX);
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }


    /**
     * 删除索引库
     * DELETE /hotel
     */
    @SneakyThrows
    @Test
    void deleteIndex() {
        DeleteIndexRequest request = new DeleteIndexRequest(HotelConst.HOTEL_INDEX);
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }

    // @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }


}
