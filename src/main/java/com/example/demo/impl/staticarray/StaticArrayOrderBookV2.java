package com.example.demo.impl.staticarray;

import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;
import com.example.demo.impl.staticarray.iterator.StaticArrayIteratorV2;
import com.example.demo.impl.staticarray.orderlevel.OrderLevelV2;

import java.util.*;

public class StaticArrayOrderBookV2 implements OrderBook {

    private final long priceRange;
    private final long minPriceScaled;

    private final OrderLevelV2[] bidLevels;
    private final OrderLevelV2[] askLevels;

    private final HashMap<Long, PrimitiveOrder> ordersById;

    public StaticArrayOrderBookV2(double minPrice, double maxPrice, double priceStep) {
        this.minPriceScaled = (long) (minPrice / priceStep);
        this.priceRange = (long) ((maxPrice - minPrice) / priceStep) + 1;

        this.bidLevels = new OrderLevelV2[(int) priceRange];
        this.askLevels = new OrderLevelV2[(int) priceRange];

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
        OrderLevelV2[] targetLevels = (order.side == PrimitiveOrder.SIDE_BUY) ? bidLevels : askLevels;

        if (index < 0 || index >= priceRange) {
            throw new IllegalArgumentException("Price out of range: " + order.price);
        }

        if (targetLevels[index] == null) {
            targetLevels[index] = new OrderLevelV2();
        }
        targetLevels[index].add(order);
        ordersById.put(order.orderId, order);
    }

    @Override
    public void removeOrder(PrimitiveOrder order) {
        if (order == null) return;

        int index = getScaledIndex(order.price);
        OrderLevelV2[] targetLevels = (order.side == PrimitiveOrder.SIDE_BUY) ? bidLevels : askLevels;

        if (index < 0 || index >= priceRange) return;
        if (targetLevels[index] == null) return;

        boolean removed = targetLevels[index].remove(order);
        if (removed) {
            ordersById.remove(order.orderId);
            if (targetLevels[index].isEmpty()) {
                targetLevels[index] = null; // optional cleanup
            }
        }
    }

    @Override
    public void removeOrderFromLookUpMap(long orderId) {
        ordersById.remove(orderId);
    }

    @Override
    public PrimitiveOrder getOrderDetail(long orderId) {
        return ordersById.get(orderId);
    }

    public void updateOrder(PrimitiveOrder order) {
        PrimitiveOrder existingOrder = ordersById.get(order.orderId);
        if (existingOrder == null) {
            addOrder(order);
            return;
        }
    }

    @Override
    public void clear() {
        Arrays.fill(bidLevels, null);
        Arrays.fill(askLevels, null);
        ordersById.clear();
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getBidLevelsIterator() {
        return new StaticArrayIteratorV2(bidLevels, true, minPriceScaled);
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getAskLevelsIterator() {
        return new StaticArrayIteratorV2(askLevels, false, minPriceScaled);
    }
}

