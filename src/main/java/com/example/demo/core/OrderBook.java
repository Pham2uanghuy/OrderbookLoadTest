package com.example.demo.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public interface OrderBook {
    /**
     * Adds a new order to the order book.
     * @param order The order to be added.
     */
    void addOrder(PrimitiveOrder order);


    /**
     * Removes an order from the order book.
     * @param order The order to be removed.
     */
    void removeOrder(PrimitiveOrder order);

    /**
     * Gets an iterator for bid price levels.
     * Key = scaled price (long), Value = collection of orders at that price.
     * @return An iterator over bid price levels.
     */
    Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getBidLevelsIterator();

    /**
     * Gets an iterator for ask price levels.
     * Key = scaled price (long), Value = collection of orders at that price.
     * @return An iterator over ask price levels.
     */
    Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getAskLevelsIterator();

    /**
     * Gets the details of a specific order by its ID.
     * @param orderId The ID of the order (long).
     * @return The order object, or null if not found.
     */
    PrimitiveOrder getOrderDetail(long orderId);

    /**
     * Clears all orders from the order book.
     */
    void clear();

    /**
     * Remove order from LoopUp Map
     * @param orderId
     */
    void removeOrderFromLookUpMap(long orderId);
}
