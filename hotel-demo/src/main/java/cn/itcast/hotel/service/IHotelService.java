package cn.itcast.hotel.service;

import cn.itcast.hotel.model.pojo.Hotel;
import cn.itcast.hotel.model.pojo.HotelDoc;
import cn.itcast.hotel.model.query.QueryParams;
import cn.itcast.hotel.model.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;

public interface IHotelService extends IService<Hotel> {
    PageResult <HotelDoc> search(QueryParams params) throws IOException;
}
