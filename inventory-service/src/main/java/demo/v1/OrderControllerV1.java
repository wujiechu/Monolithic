package demo.v1;

import demo.order_mongo.LineItem;
import demo.order_mongo.Order;
import demo.order_mongo.OrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping(path = "/order")
public class OrderControllerV1 {

    private OrderServiceV1 orderService;

    @Autowired
    public OrderControllerV1(OrderServiceV1 orderService) {
        this.orderService = orderService;
    }

    @RequestMapping(path = "/v1/accounts/{accountNumber}/orders")
    public ResponseEntity getOrders(@PathVariable("accountNumber") String accountNumber) throws Exception {
        return Optional.ofNullable(orderService.getOrdersForAccount(accountNumber))
                .map(a -> new ResponseEntity<List<Order>>(a, HttpStatus.OK))
                .orElseThrow(() -> new Exception("Accounts for user do not exist"));
    }

    @RequestMapping(path = "/v1/orders/{orderId}/events", method = RequestMethod.POST)
    public ResponseEntity addOrderEvent(@RequestBody OrderEvent orderEvent,
                                        @PathVariable("orderId") String orderId) throws Exception {
        assert orderEvent != null;
        assert orderId != null;
        assert !Objects.equals(orderId, orderEvent.getOrderId());
        return Optional.ofNullable(orderService.addOrderEvent(orderEvent, true))
                .map(a -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
                .orElseThrow(() -> new Exception("Order event could not be applied to order"));
    }

    @RequestMapping(path = "/v1/orders/{orderId}")
    public ResponseEntity getOrder(@PathVariable("orderId") String orderId) throws Exception {
        assert orderId != null;
        return Optional.ofNullable(orderService.getOrder(orderId, true))
                .map(a -> new ResponseEntity<Order>(a, HttpStatus.OK))
                .orElseThrow(() -> new Exception("Could not retrieve order"));
    }

    @RequestMapping(path = "/v1/orders", method = RequestMethod.POST)
    public ResponseEntity createOrder(@RequestBody List<LineItem> lineItems) throws Exception {
        assert lineItems != null;
        assert lineItems.size() > 0;
        return Optional.ofNullable(orderService.createOrder(lineItems))
                .map(a -> new ResponseEntity<>(a, HttpStatus.OK))
                .orElseThrow(() -> new Exception("Could not create the order"));
    }
}
