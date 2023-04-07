package cn.itcast.hotel.service;

import cn.itcast.hotel.model.pojo.Hotel;
import cn.itcast.hotel.model.pojo.HotelDoc;
import cn.itcast.hotel.model.query.QueryParams;
import cn.itcast.hotel.model.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IHotelService extends IService<Hotel> {
    PageResult <HotelDoc> search(QueryParams params) throws IOException;

    Map<String, List<String>> filters(QueryParams params) throws IOException;

    List<String> suggestion(String key) throws IOException;

    void deleteById(Long id);

    void insertById(Long id);
}
