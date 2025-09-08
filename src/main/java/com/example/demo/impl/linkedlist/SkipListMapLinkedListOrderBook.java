package com.example.demo.impl.linkedlist;

import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class SkipListMapLinkedListOrderBook implements OrderBook {

    // Map to store bid and ask levels
    private final NavigableMap<Long, List<PrimitiveOrder>> bidLevels;
    private final NavigableMap<Long, List<PrimitiveOrder>> askLevels;

    // Map to quickly find an order by its ID
    private final ConcurrentHashMap<Long, PrimitiveOrder> ordersById;

    public SkipListMapLinkedListOrderBook() {
        this.bidLevels = new ConcurrentSkipListMap<>(Comparator.reverseOrder()); // bids high -> low
        this.askLevels = new ConcurrentSkipListMap<>(); // asks low -> high
        this.ordersById = new ConcurrentHashMap<>();
    }

    @Override
    public void addOrder(PrimitiveOrder order) {
        NavigableMap<Long, List<PrimitiveOrder>> targetBook =
                (order.side == PrimitiveOrder.SIDE_BUY) ? bidLevels : askLevels;

        targetBook
                .computeIfAbsent(order.price, k -> Collections.synchronizedList(new LinkedList<>()))
                .add(order);

        // Store the order by ID for quick lookups and updates
        ordersById.put(order.orderId, order);
    }

    @Override
    public void removeOrder(PrimitiveOrder order) {
        NavigableMap<Long, List<PrimitiveOrder>> targetBook =
                (order.side == PrimitiveOrder.SIDE_BUY) ? bidLevels : askLevels;

        List<PrimitiveOrder> ordersAtLevel = targetBook.get(order.price);
        if (ordersAtLevel != null) {
            ordersAtLevel.remove(order);
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
