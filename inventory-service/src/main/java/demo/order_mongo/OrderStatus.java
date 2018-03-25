package demo.order_mongo;

import demo.order.Order;

/**
 * Describes the state of an {@link Order}.
 *
 * @author Kenny Bastani
 */
public enum OrderStatus {
    PURCHASED,
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED
}
