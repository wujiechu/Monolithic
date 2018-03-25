package demo.order_mongo;

import demo.order_mongo.OrderEvent;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;


public interface OrderEventRepository extends PagingAndSortingRepository<OrderEvent, String> {
    Stream<OrderEvent> findOrderEventsByOrderId(@Param("orderId") String orderId);
}
