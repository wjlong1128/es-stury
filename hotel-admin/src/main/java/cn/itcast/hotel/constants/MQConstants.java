package cn.itcast.hotel.constants;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/7
 */
public class MQConstants {

    public static final String HOTEL_EXCHANGE = "hotel.topic";
    public static final String HOTEL_INSERT_QUEUE = "hotel.insert.queue";
    public static final String HOTEL_DELETE_QUEUE = "hotel.delete.queue";

    public static final String HOTEL_INSERT_KEY = "hotel.insert";
    public static final String HOTEL_DELETE_KEY = "hotel.delete";
}
