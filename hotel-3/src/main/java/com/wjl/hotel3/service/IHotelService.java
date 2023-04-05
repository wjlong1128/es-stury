package com.wjl.hotel3.service;

import com.wjl.hotel3.model.pojo.Hotel;
import com.wjl.hotel3.model.pojo.HotelDoc;
import com.wjl.hotel3.model.query.QueryParams;
import com.wjl.hotel3.model.result.PageResult;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IHotelService {
    Optional<Hotel> findHotelById(long l);

   List<HotelDoc> list();

    PageResult<HotelDoc> search(QueryParams params) throws IOException;
}
