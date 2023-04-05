package cn.itcast.hotel.es;

import cn.itcast.hotel.model.pojo.Hotel;
import cn.itcast.hotel.model.pojo.HotelDoc;
import cn.itcast.hotel.service.impl.HotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static cn.itcast.hotel.consts.HotelConst.HOTEL_INDEX;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/5
 */
@SpringBootTest
public class HotelDocumentTest {


    private RestHighLevelClient client;

    @Autowired
    private HotelService hotelService;

    @BeforeEach
    void setup() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://localhost:9200")
        ));
    }

    /**
     * 向索引库添加数据(文档)
     * POST /hotel/_doc/{id}
     */
    @Test
    void addDocument() throws IOException {
        Hotel hotel = hotelService.getById(38609L);
        // this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
        HotelDoc hotelDoc = new HotelDoc(hotel);
        IndexRequest request = new IndexRequest(HOTEL_INDEX);
        request.id(hotelDoc.getId().toString()).source(JSON.toJSONString(hotelDoc), XContentType.JSON);

        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    /**
     * 根据id查询索引库文档
     * GET /hotel/_doc/id
     */
    @Test
    void getDocumentById() throws IOException {
        Long id = 38609L;
        GetRequest request = new GetRequest(HOTEL_INDEX, id.toString());
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        System.out.println(JSON.parseObject(json, HotelDoc.class));
    }

    /**
     * 局部更新文档
     * POST /hotel/_update/{id}
     * {
     *     "doc":{}
     * }
     */
    @Test
    void updateDocDocument() throws IOException {
        Long id = 38609L;
        UpdateRequest request = new UpdateRequest(HOTEL_INDEX, id.toString());
        request.doc(
                "price","250"
        );
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    /**
     *  根据id删除文档
     *  DELETE /hotel/_doc/{id}
     */
    @Test
    void deleteDocument() throws IOException {
        Long id = 38609L;
        DeleteRequest request = new DeleteRequest(HOTEL_INDEX, id.toString());
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    /**
     * 向索引库批量添加数据(文档)
     * http://localhost:9200/hotel/_search
     */
    @Test
    void addDocuments() throws IOException {
        List<Hotel> hotels = hotelService.list();
        // this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
        List<HotelDoc> hotelDocs = hotels.stream().map(HotelDoc::new).collect(Collectors.toList());
        BulkRequest request = new BulkRequest();
        for (HotelDoc doc : hotelDocs) {
            IndexRequest document = new IndexRequest(HOTEL_INDEX);
            document.id(doc.getId().toString()).source(JSON.toJSONString(doc),XContentType.JSON);
            request.add(document);
        }
        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }

    @Test
    void updateDocument() {
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }


}
