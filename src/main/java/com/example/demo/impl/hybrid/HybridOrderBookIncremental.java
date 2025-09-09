package com.example.demo.impl.hybrid;

import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;
import com.example.demo.impl.arraydeque.ArrayDequeOrderBook;
import com.example.demo.impl.staticarray.StaticArrayOrderBook;

import java.util.*;

public class HybridOrderBookIncremental implements OrderBook {
    private final ArrayDequeOrderBook coreBook;
    private final StaticArrayOrderBook cacheBook;
    private final int topLevels;

    // Cache giữ top N levels
    private final NavigableMap<Long, Collection<PrimitiveOrder>> cachedBids;
    private final NavigableMap<Long, Collection<PrimitiveOrder>> cachedAsks;

    public HybridOrderBookIncremental(double minPrice, double maxPrice, double priceStep, int topLevels) {
        this.coreBook = new ArrayDequeOrderBook();
        this.cacheBook = new StaticArrayOrderBook(minPrice, maxPrice, priceStep);
        this.topLevels = topLevels;
        this.cachedBids = new TreeMap<>(Comparator.reverseOrder()); // bid: highest first
        this.cachedAsks = new TreeMap<>(); // ask: lowest first
    }

    @Override
    public void addOrder(PrimitiveOrder order) {
        coreBook.addOrder(order);
        updateCacheOnAdd(order);
    }

    @Override
    public void removeOrder(PrimitiveOrder order) {
        coreBook.removeOrder(order);
        updateCacheOnRemove(order);
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
        cachedBids.clear();
        cachedAsks.clear();
    }

    @Override
    public void removeOrderFromLookUpMap(long orderId) {
        coreBook.removeOrderFromLookUpMap(orderId);
        cacheBook.removeOrderFromLookUpMap(orderId);
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getBidLevelsIterator() {
        return (Iterator) cachedBids.entrySet().iterator();
    }

    @Override
    public Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> getAskLevelsIterator() {
        return (Iterator) cachedAsks.entrySet().iterator();
    }

    // ========== Private Helpers ==========

    private void updateCacheOnAdd(PrimitiveOrder order) {
        if (isWithinTopLevels(order.price, order.side)) {
            cacheBook.addOrder(order);
            NavigableMap<Long, Collection<PrimitiveOrder>> target =
                    (order.side == PrimitiveOrder.SIDE_BUY) ? cachedBids : cachedAsks;

            target.computeIfAbsent(order.price, k -> new ArrayList<>()).add(order);
            trimCache(target);
        }
    }

    private void updateCacheOnRemove(PrimitiveOrder order) {
        cacheBook.removeOrder(order);

        NavigableMap<Long, Collection<PrimitiveOrder>> target =
                (order.side == PrimitiveOrder.SIDE_BUY) ? cachedBids : cachedAsks;

        Collection<PrimitiveOrder> level = target.get(order.price);
        if (level != null) {
            level.remove(order);
            if (level.isEmpty()) {
                target.remove(order.price);
            }
        }
    }

    private boolean isWithinTopLevels(long price, int side) {
        NavigableMap<Long, Collection<PrimitiveOrder>> target =
                (side == PrimitiveOrder.SIDE_BUY) ? cachedBids : cachedAsks;

        // Nếu bộ đệm chưa đầy, đơn hàng mới luôn được thêm vào.
        if (target.size() < topLevels) {
            return true;
        }

        // So sánh giá của đơn hàng mới với mức giá tệ nhất trong cache.
        if (side == PrimitiveOrder.SIDE_BUY) {
            return price >= target.lastKey(); // Đơn hàng mua có giá tốt hơn hoặc bằng mức giá thấp nhất trong cache.
        } else {
            return price <= target.lastKey(); // Đơn hàng bán có giá tốt hơn hoặc bằng mức giá cao nhất trong cache.
        }
    }

    private void trimCache(NavigableMap<Long, Collection<PrimitiveOrder>> map) {
        while (map.size() > topLevels) {
            map.pollLastEntry();
        }
    }
}
