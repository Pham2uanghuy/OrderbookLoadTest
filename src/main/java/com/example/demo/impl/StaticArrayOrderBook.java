package com.example.demo.impl;

import com.example.demo.core.OrderBook;
import com.example.demo.core.OrderLevel;
import com.example.demo.core.PrimitiveOrder;
import com.example.demo.impl.iterator.StaticArrayIterator;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StaticArrayOrderBook implements OrderBook {

    private final long priceRange;
    private final long minPriceScaled;

    private final OrderLevel[] bidLevels;
    private final OrderLevel[] askLevels;

    private final LongObjectHashMap<PrimitiveOrder> ordersById;

    public StaticArrayOrderBook(double minPrice, double maxPrice, double priceStep) {
        this.minPriceScaled = (long) (minPrice / priceStep);
        this.priceRange = (long) ((maxPrice - minPrice) / priceStep) + 1;

        this.bidLevels = new OrderLevel[(int) priceRange];
        this.askLevels = new OrderLevel[(int) priceRange];

        this.ordersById = new LongObjectHashMap<>();
    }

    private int getScaledIndex(long priceScaled) {
        return (int) (priceScaled - minPriceScaled);
    }

    private long getScaledPrice(int index) {
        return index + minPriceScaled;
    }


    @Override
    public void addOrder(PrimitiveOrder order) {
        int index = getScaledIndex(order.price);
        OrderLevel[] targetLevels = (order.side == PrimitiveOrder.SIDE_BUY) ? bidLevels : askLevels;

        if (index < 0 || index >= priceRange) {
            throw new IllegalArgumentException("Price out of range: " + order.price);
        }

        if (targetLevels[index] == null) {
            targetLevels[index] = new OrderLevel();
        }
        targetLevels[index].add(order);
        ordersById.put(order.orderId, order);
    }


    public void updateOrder(PrimitiveOrder order) {
        PrimitiveOrder existingOrder = ordersById.get(order.orderId);
        if (existingOrder == null) {
            addOrder(order);
            return;
        }
    }

    @Override
    public void removeOrder(PrimitiveOrder order) {

    }

    @Override
    public PrimitiveOrder getOrderDetail(long orderId) {
        return ordersById.get(orderId);
    }

    @Override
    public void clear() {

    }

    @Override
    public void removeOrderFromLookUpMap(long orderId) {
        ordersById.remove(orderId);
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getBidLevelsIterator() {
        return new StaticArrayIterator(bidLevels, true);
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getAskLevelsIterator() {
        return new StaticArrayIterator(askLevels, false);
    }
}
