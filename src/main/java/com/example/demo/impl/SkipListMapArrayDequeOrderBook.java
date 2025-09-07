package com.example.demo.impl;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;


public class SkipListMapArrayDequeOrderBook implements OrderBook {

    // Map to store bid and ask levels (key = scaled price in long)
    private final NavigableMap<Long, ArrayDeque<PrimitiveOrder>> bidLevels;
    private final NavigableMap<Long, ArrayDeque<PrimitiveOrder>> askLevels;

    // Map to quickly find an order by its ID
    private final ConcurrentHashMap<Long, PrimitiveOrder> ordersById;

    public SkipListMapArrayDequeOrderBook() {
        // Bids: sorted descending by price
        this.bidLevels = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        // Asks: sorted ascending by price
        this.askLevels = new ConcurrentSkipListMap<>();
        this.ordersById = new ConcurrentHashMap<>();
    }

    @Override
    public void addOrder(PrimitiveOrder order) {
        NavigableMap<Long, ArrayDeque<PrimitiveOrder>> targetBook =
                (order.side == PrimitiveOrder.SIDE_BUY) ? bidLevels : askLevels;
        targetBook.computeIfAbsent(order.price, k -> new ArrayDeque<>()).addLast(order);
        ordersById.put(order.orderId, order);
    }

    @Override
    public void removeOrder(PrimitiveOrder order) {
        NavigableMap<Long, ArrayDeque<PrimitiveOrder>> targetBook =
                (order.side == PrimitiveOrder.SIDE_BUY) ? bidLevels : askLevels;

        ArrayDeque<PrimitiveOrder> ordersAtLevel = targetBook.get(order.price);
        if (ordersAtLevel != null) {
            ordersAtLevel.remove(order); // O(n), nhưng số order trên cùng price thường nhỏ
            if (ordersAtLevel.isEmpty()) {
                targetBook.remove(order.price);
            }
        }
        ordersById.remove(order.orderId);
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getBidLevelsIterator() {
        return bidLevels.entrySet()
                .stream()
                .<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>>map(
                        e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), (Collection<PrimitiveOrder>) e.getValue())
                )
                .iterator();
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getAskLevelsIterator() {
        return askLevels.entrySet()
                .stream()
                .<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>>map(
                        e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), (Collection<PrimitiveOrder>) e.getValue())
                )
                .iterator();
    }

    @Override
    public PrimitiveOrder getOrderDetail(long orderId) {
        return ordersById.get(orderId);
    }

    @Override
    public void clear() {
        bidLevels.clear();
        askLevels.clear();
        ordersById.clear();
    }

    @Override
    public void removeOrderFromLookUpMap(long orderId) {
        ordersById.remove(orderId);
    }
}
