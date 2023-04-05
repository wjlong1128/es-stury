package com.wjl.hotel3.model.query;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/5
 */

import lombok.Data;

@Data
public class QueryParams {
    private String key;
    /**
     * 三种类型 default/price/score
     */
    private String sortBy;
    private Integer page;
    private Integer size;

     private String brand;
     private String city;
     private String starName;
     private Integer maxPrice;
     private Integer minPrice;
     private String location;
}
