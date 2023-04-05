package com.wjl.hotel3.model.result;

import lombok.Data;

import java.util.List;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/5
 */
@Data
public class PageResult<T> {
    private Long total;
    private Integer page;
    private Integer size;
    private List<T> hotels;

}
