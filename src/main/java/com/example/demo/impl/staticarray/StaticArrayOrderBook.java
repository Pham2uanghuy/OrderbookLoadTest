package com.example.demo.impl.staticarray;

import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;
import com.example.demo.impl.staticarray.iterator.StaticArrayIterator;
import com.example.demo.impl.staticarray.orderlevel.OrderLevel;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import java.util.*;

public class StaticArrayOrderBook implements OrderBook {

    private final long priceRange;
    private final long minPriceScaled;

    private final OrderLevel[] bidLevels;
    private final OrderLevel[] askLevels;

    private final HashMap<Long, PrimitiveOrder> ordersById;

    public StaticArrayOrderBook(double minPrice, double maxPrice, double priceStep) {
        this.minPriceScaled = (long) (minPrice / priceStep);
        this.priceRange = (long) ((maxPrice - minPrice) / priceStep) + 1;

        this.bidLevels = new OrderLevel[(int) priceRange];
        this.askLevels = new OrderLevel[(int) priceRange];

        this.ordersById = new HashMap<>();
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


    @Override
    public void removeOrder(PrimitiveOrder order) {
        int index = getScaledIndex(order.price);
        OrderLevel[] targetLevels = (order.side == PrimitiveOrder.SIDE_BUY) ? bidLevels : askLevels;

        if (index < 0 || index >= priceRange || targetLevels[index] == null) {
            return;
        }

        targetLevels[index].remove(order);
        if (targetLevels[index].isEmpty()) {
            targetLevels[index] = null;
        }
    }

    @Override
    public PrimitiveOrder getOrderDetail(long orderId) {
        return ordersById.get(orderId);
    }

    @Override
    public void clear() {
        Arrays.fill(bidLevels, null);
        Arrays.fill(askLevels, null);
        ordersById.clear();
    }

    @Override
    public void removeOrderFromLookUpMap(long orderId) {
        ordersById.remove(orderId);
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getBidLevelsIterator() {
        return new StaticArrayIterator(bidLevels, true, minPriceScaled);
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getAskLevelsIterator() {
        return new StaticArrayIterator(askLevels, false, minPriceScaled);
    }
}