package demo.order_mongo;

import demo.order_mongo.Order;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface OrderRepository extends PagingAndSortingRepository<Order, String> {
    List<Order> findByAccountNumber(String accountNumber);
}
