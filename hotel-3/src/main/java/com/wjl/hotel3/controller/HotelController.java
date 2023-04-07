package com.wjl.hotel3.controller;

import com.wjl.hotel3.model.pojo.HotelDoc;
import com.wjl.hotel3.model.query.QueryParams;
import com.wjl.hotel3.model.result.PageResult;
import com.wjl.hotel3.service.IHotelService;
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
    public Map<String,List<String>> filters(@RequestBody QueryParams params) throws IOException {
       return service.filters(params);
    }

}
