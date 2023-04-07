package cn.itcast.hotel.mq;

import cn.itcast.hotel.consts.MQConstants;
import cn.itcast.hotel.service.IHotelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/7
 */
//@RabbitListener(
//        bindings = {
//                @QueueBinding(
//                        exchange = @Exchange(type = ExchangeTypes.TOPIC, value = MQConstants.HOTEL_EXCHANGE, durable = "true", autoDelete = "false"),
//                        value = @Queue(value = MQConstants.HOTEL_INSERT_QUEUE, durable = "true", autoDelete = "false"),
//                        key = MQConstants.HOTEL_INSERT_KEY
//                ),
//                @QueueBinding(
//                        exchange = @Exchange(type = ExchangeTypes.TOPIC, value = MQConstants.HOTEL_EXCHANGE, durable = "true", autoDelete = "false"),
//                        value = @Queue(value = MQConstants.HOTEL_DELETE_QUEUE, durable = "true", autoDelete = "false"),
//                        key = MQConstants.HOTEL_DELETE_KEY
//                )
//        }
//)
@Slf4j
@Component
public class HotelListener {

    @Autowired
    private IHotelService hotelService;

    @RabbitListener(queues = MQConstants.HOTEL_INSERT_QUEUE)
    public void listenInsertOrUpdate(Long id) {
        hotelService.insertById(id);
    }

    @RabbitListener(queues = MQConstants.HOTEL_DELETE_QUEUE)
    public void listenDelete(Long id) {
        log.debug("{} 接收消息 删除id:{}", MQConstants.HOTEL_DELETE_QUEUE, id);
        hotelService.deleteById(id);
    }

}
