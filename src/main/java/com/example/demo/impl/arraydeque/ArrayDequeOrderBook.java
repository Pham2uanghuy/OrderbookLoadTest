package com.example.demo.impl.arraydeque;

import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;

import java.util.*;

public class ArrayDequeOrderBook implements OrderBook {

    // Map to store bid and ask levels (key = scaled price in long)
    private final NavigableMap<Long, ArrayDeque<PrimitiveOrder>> bidLevels;
    private final NavigableMap<Long, ArrayDeque<PrimitiveOrder>> askLevels;

    // Map to quickly find an order by its ID
    private final HashMap<Long, PrimitiveOrder> ordersById;

    public ArrayDequeOrderBook() {
        // Bids: sorted descending by price
        this.bidLevels = new TreeMap<>(Comparator.reverseOrder());
        // Asks: sorted ascending by price
        this.askLevels = new TreeMap<>();
        this.ordersById = new HashMap<>();
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
            ordersAtLevel.remove(order); // O(n)
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
