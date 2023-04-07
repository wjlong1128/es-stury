package cn.itcast.hotel.controller;

import cn.itcast.hotel.model.pojo.HotelDoc;
import cn.itcast.hotel.model.query.QueryParams;
import cn.itcast.hotel.model.result.PageResult;
import cn.itcast.hotel.service.IHotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/5
 */
@RequiredArgsConstructor
@RequestMapping("/hotel")
@RestController
public class HotelController {

    private final IHotelService service;

    @RequestMapping({"list"})
    public PageResult<HotelDoc> search(@RequestBody QueryParams params) throws IOException {
        return service.search(params);
    }

    @RequestMapping("filters")
    public Map<String, List<String>> filters(@RequestBody QueryParams params) throws IOException {
       return service.filters(params);
    }

    @RequestMapping("suggestion")
    public List<String> suggestion(String key) throws IOException {
       return service.suggestion(key);
    }

}
