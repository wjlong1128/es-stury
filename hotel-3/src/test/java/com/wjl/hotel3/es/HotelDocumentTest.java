package com.wjl.hotel3.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.wjl.hotel3.model.pojo.Hotel;
import com.wjl.hotel3.model.pojo.HotelDoc;
import com.wjl.hotel3.service.impl.HotelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.wjl.hotel3.consts.HotelConst.HOTEL_INDEX;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/5
 */
@SpringBootTest
public class HotelDocumentTest {


    @Autowired
    private ElasticsearchClient client;

    @Autowired
    private HotelService hotelService;

    /**
     * 向索引库添加数据(文档)
     * POST /hotel/_doc/{id}
     * {
     *
     * }
     */
    @Test
    void addDocument() throws IOException {
        Optional<Hotel> hotel = hotelService.findHotelById(38609L);
        // this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
        HotelDoc doc = new HotelDoc(hotel.get());
        IndexResponse response = client.index(index -> index.index(HOTEL_INDEX).id(doc.getId().toString()).document(doc));
        // IndexResponse: {"_id":"38609","_index":"hotel","_primary_term":1,"result":"created","_seq_no":0,"_shards":{"failed":0.0,"successful":1.0,"total":2.0},"_version":1}
        System.out.println(response);
    }


    /**
     * 根据id查询索引库文档
     * GET /hotel/_doc/{id}
     */
    @Test
    void getDocumentById() throws IOException {
        Long id = 38609L;
        GetResponse<HotelDoc> response = client.get(r -> r.index(HOTEL_INDEX).id(id.toString()), HotelDoc.class);
        System.out.println(response.source());
    }


    /**
     * POST /hotel/_update/{id}
     * {
     *      "doc":{}
     * }
     * 局部更新文档
     */
    @Test
    void updateDocDocument() throws IOException {
        Long id = 38609L;
        HotelDoc hotelDoc = new HotelDoc();
        hotelDoc.setPrice(250);
        UpdateResponse<? extends HotelDoc> response = client.update(request -> request.index(HOTEL_INDEX).id(id.toString()).doc(hotelDoc), hotelDoc.getClass());
        System.out.println(response);
    }


    /**
     * 根据id删除文档
     * DELETE /hotel/_doc/{id}
     */
    @Test
    void deleteDocument() throws IOException {
        Long id = 38609L;
        DeleteResponse response = client.delete(request -> request.index(HOTEL_INDEX).id(id.toString()));
        System.out.println(response);
    }

    /**
     * 向索引库批量添加数据(文档)
     * http://localhost:9201/hotel/_search
     * POST /_bulk POST /<index>/_bulk {"action": {"metadata"}} {"data"}
     */
    @Test
    void addDocuments() throws IOException {
        List<HotelDoc> list = hotelService.list();
        ArrayList<BulkOperation> operations = new ArrayList<>();
        for (HotelDoc doc : list) {
            BulkOperation operation = new BulkOperation.Builder().create(d -> d.index(HOTEL_INDEX).id(doc.getId().toString()).document(doc)).build();
            operations.add(operation);
        }
        BulkRequest request = new BulkRequest.Builder().operations(operations).build();
        BulkResponse response = client.bulk(request);
        System.out.println(response.errors());
    }


    @Test
    void updateDocument() throws IOException {
        HotelDoc doc = new HotelDoc();
        doc.setIsAD(true);
        UpdateRequest<HotelDoc, HotelDoc> request = new UpdateRequest.Builder<HotelDoc, HotelDoc>()
                .index(HOTEL_INDEX)
                .id("47478")
                .doc(doc)
                .build();
        UpdateResponse response = client.update(request, HotelDoc.class);
        System.out.println(response.toString());
    }

}
