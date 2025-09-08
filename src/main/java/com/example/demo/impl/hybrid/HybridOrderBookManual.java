package com.example.demo.impl.hybrid;

import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;
import com.example.demo.impl.arraydeque.LongObjectArrayDequeOrderBook;
import com.example.demo.impl.staticarray.StaticArrayOrderBook;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class HybridOrderBookManual implements OrderBook {
    private final LongObjectArrayDequeOrderBook coreBook;
    private final StaticArrayOrderBook cacheBook;
    private final int topLevels;

    public HybridOrderBookManual(double minPrice, double maxPrice, double priceStep, int topLevels) {
        this.coreBook = new LongObjectArrayDequeOrderBook();
        this.cacheBook = new StaticArrayOrderBook(minPrice, maxPrice, priceStep);
        this.topLevels = topLevels;
    }

    @Override
    public void addOrder(PrimitiveOrder order) {
        coreBook.addOrder(order);
        if (isWithinCacheRange(order)) {
            cacheBook.addOrder(order);
        }
    }

    @Override
    public void removeOrder(PrimitiveOrder order) {
        coreBook.removeOrder(order);
        cacheBook.removeOrder(order);
    }

    @Override
    public PrimitiveOrder getOrderDetail(long orderId) {
        PrimitiveOrder cached = cacheBook.getOrderDetail(orderId);
        if (cached != null) return cached;
        return coreBook.getOrderDetail(orderId);
    }

    @Override
    public void clear() {
        coreBook.clear();
        cacheBook.clear();
    }

    @Override
    public void removeOrderFromLookUpMap(long orderId) {
        coreBook.removeOrderFromLookUpMap(orderId);
        cacheBook.removeOrderFromLookUpMap(orderId);
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getBidLevelsIterator() {
        return cacheBook.getBidLevelsIterator();
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getAskLevelsIterator() {
        return cacheBook.getAskLevelsIterator();
    }

    // ========== Manual Trigger ==========

    public void refreshCache() {
        cacheBook.clear();

        // rebuild bid cache
        Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> bidIt = coreBook.getBidLevelsIterator();
        int count = 0;
        while (bidIt.hasNext() && count < topLevels) {
            Map.Entry<Long, ? extends Collection<PrimitiveOrder>> level = bidIt.next();
            for (PrimitiveOrder o : level.getValue()) {
                cacheBook.addOrder(o);
            }
            count++;
        }

        // rebuild ask cache
        Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> askIt = coreBook.getAskLevelsIterator();
        count = 0;
        while (askIt.hasNext() && count < topLevels) {
            Map.Entry<Long, ? extends Collection<PrimitiveOrder>> level = askIt.next();
            for (PrimitiveOrder o : level.getValue()) {
                cacheBook.addOrder(o);
            }
            count++;
        }
    }

    private boolean isWithinCacheRange(PrimitiveOrder order) {
        long bestBid = coreBook.getBidLevelsIterator().hasNext()
                ? coreBook.getBidLevelsIterator().next().getKey()
                : Long.MIN_VALUE;
        long bestAsk = coreBook.getAskLevelsIterator().hasNext()
                ? coreBook.getAskLevelsIterator().next().getKey()
                : Long.MAX_VALUE;

        if (order.side == PrimitiveOrder.SIDE_BUY) {
            return order.price >= bestBid - topLevels;
        } else {
            return order.price <= bestAsk + topLevels;
        }
    }
}
