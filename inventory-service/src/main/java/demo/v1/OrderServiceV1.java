package demo.v1;

import demo.account.AccountRepository;
import demo.account_mongo.Account;
import demo.address_c.Address;
import demo.address_c.AddressType;
import demo.order_mongo.LineItem;
import demo.order_mongo.Order;
import demo.order_mongo.OrderEvent;
import demo.order_mongo.OrderEventType;
import demo.order_mongo.OrderEventRepository;
import demo.order_mongo.OrderRepository;
import demo.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceV1 {

    private AccountServiceV1 accountServiceV1;
    private OrderRepository orderRepository;
    private OrderEventRepository orderEventRepository;
    private OAuth2RestTemplate oAuth2RestTemplate;

    @Autowired
    public OrderServiceV1(OrderRepository orderRepository,
                          OrderEventRepository orderEventRepository,
                          AccountServiceV1 accountServiceV1,
                          @LoadBalanced OAuth2RestTemplate oAuth2RestTemplate) {
        this.accountServiceV1 = accountServiceV1;
        this.orderRepository = orderRepository;
        this.orderEventRepository = orderEventRepository;
        this.oAuth2RestTemplate = oAuth2RestTemplate;
    }

    public Order createOrder(List<LineItem> lineItems) {
        //Account[] accounts = oAuth2RestTemplate.getForObject("http://account-service/v1/accounts", Account[].class);
        List<demo.account.Account> accountList = accountServiceV1.getUserAccounts();
        demo.account.Account[] accounts = accountList.toArray(new demo.account.Account[0]);
        demo.account.Account defaultAccount = Arrays.asList(accounts).stream()
                .filter(demo.account.Account::getDefaultAccount)
                .findFirst().orElse(null);

        if (defaultAccount == null) {
            return null;
        }

        Set<demo.address_m.Address> addressSet = defaultAccount.getAddresses();
        Iterator<demo.address_m.Address> iterator = addressSet.iterator();
        Address address = null;
        while(iterator.hasNext()){
            demo.address_m.Address ad = iterator.next();
            if(ad.getAddressType() == demo.address_m.AddressType.SHIPPING){
                address = new Address();
                address.setAddressType(AddressType.SHIPPING);
                address.setCity(ad.getCity());
                address.setCountry(ad.getCountry());
                address.setState(ad.getState());
                address.setStreet1(ad.getStreet1());
                address.setStreet2(ad.getStreet2());
                address.setZipCode(ad.getZipCode());
                break;
            }
        }
        if(address==null){
            throw new RuntimeException("Default account does not have a shipping address");
        }
        Order newOrder = new Order(defaultAccount.getAccountNumber(),address);

        newOrder.setLineItems(lineItems);

        newOrder = orderRepository.save(newOrder);

        return newOrder;
    }

    public Boolean addOrderEvent(OrderEvent orderEvent, Boolean validate) throws Exception {
        // Get the order for the event
        Order order = orderRepository.findOne(orderEvent.getOrderId());

        if (validate) {
            // Validate the account number of the event's order belongs to the user
            validateAccountNumber(order.getAccountNumber());
        }

        // Save the order event
        orderEventRepository.save(orderEvent);

        return true;
    }

    public Order getOrder(String orderId, Boolean validate) {
        // Get the order for the event
        Order order = orderRepository.findOne(orderId);

        if (validate) {
            try {
                // Validate the account number of the event's order belongs to the user
                validateAccountNumber(order.getAccountNumber());
            } catch (Exception ex) {
                return null;
            }
        }

        Flux<OrderEvent> orderEvents =
                Flux.fromStream(orderEventRepository.findOrderEventsByOrderId(order.getOrderId()));

        // Aggregate the state of order
        return orderEvents
                .takeWhile(orderEvent -> orderEvent.getType() != OrderEventType.DELIVERED)
                .reduceWith(() -> order, Order::incorporate)
                .get();
    }

    public List<Order> getOrdersForAccount(String accountNumber) throws Exception {
        List<Order> orders;
        validateAccountNumber(accountNumber);

        orders = orderRepository.findByAccountNumber(accountNumber);

        return orders.stream()
                .map(order -> getOrder(order.getOrderId(), true))
                .filter(order -> order != null)
                .collect(Collectors.toList());
    }

    public boolean validateAccountNumber(String accountNumber) throws Exception {
        //Account[] accounts = oAuth2RestTemplate.getForObject("http://account-service/v1/accounts", Account[].class);
        List<demo.account.Account> accountList = accountServiceV1.getUserAccounts();
        demo.account.Account[] accounts = accountList.toArray(new demo.account.Account[0]);
        // Ensure account number is owned by the authenticated user
        if (accounts != null &&
                !Arrays.asList(accounts).stream().anyMatch(acct ->
                        Objects.equals(acct.getAccountNumber(), accountNumber))) {
            throw new Exception("Account number invalid");
        }

        return true;
    }
}
