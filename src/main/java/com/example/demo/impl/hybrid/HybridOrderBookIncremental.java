package com.example.demo.impl.hybrid;

import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;
import com.example.demo.impl.staticarray.orderlevel.OrderLevel;

import java.util.*;


public class HybridOrderBookIncremental implements OrderBook {
    // Core book lưu trữ TẤT CẢ các lệnh. Sử dụng TreeMap để linh hoạt.
    private final NavigableMap<Long, ArrayDeque<PrimitiveOrder>> coreBidLevels;
    private final NavigableMap<Long, ArrayDeque<PrimitiveOrder>> coreAskLevels;

    // Cache book lưu trữ BẢN SAO của các lệnh top levels. Sử dụng mảng tĩnh để truy cập O(1).
    private final OrderLevel[] cacheBidLevels;
    private final OrderLevel[] cacheAskLevels;
    private final long minPriceScaled;
    private final long priceRange;

    // Map duy nhất để tra cứu mọi lệnh theo ID trong O(1).
    private final HashMap<Long, PrimitiveOrder> ordersById;

    // Cache của các mức giá tốt nhất để trả về nhanh chóng cho iterator
    private final NavigableMap<Long, Collection<PrimitiveOrder>> cachedBids;
    private final NavigableMap<Long, Collection<PrimitiveOrder>> cachedAsks;

    private final int topLevels;

    public HybridOrderBookIncremental(double minPrice, double maxPrice, double priceStep, int topLevels) {
        this.coreBidLevels = new TreeMap<>(Comparator.reverseOrder());
        this.coreAskLevels = new TreeMap<>();
        this.ordersById = new HashMap<>();

        this.minPriceScaled = (long) (minPrice / priceStep);
        this.priceRange = (long) ((maxPrice - minPrice) / priceStep) + 1;
        this.cacheBidLevels = new OrderLevel[(int) priceRange];
        this.cacheAskLevels = new OrderLevel[(int) priceRange];

        this.topLevels = topLevels;
        this.cachedBids = new TreeMap<>(Comparator.reverseOrder());
        this.cachedAsks = new TreeMap<>();
    }

    // Helper để chuyển đổi giá sang index
    private int getScaledIndex(long priceScaled) {
        return (int) (priceScaled - minPriceScaled);
    }

    // Helper để lấy giá tốt nhất trong cache
    private long getWorstCachedPrice(int side) {
        if (side == PrimitiveOrder.SIDE_BUY) {
            return cachedBids.isEmpty() ? -1 : cachedBids.lastKey();
        } else {
            return cachedAsks.isEmpty() ? -1 : cachedAsks.lastKey();
        }
    }

    @Override
    public void addOrder(PrimitiveOrder order) {
        long worstCachedPrice = getWorstCachedPrice(order.side);
        boolean isWithinCache = (order.side == PrimitiveOrder.SIDE_BUY) ?
                order.price >= worstCachedPrice || cachedBids.size() < topLevels :
                order.price <= worstCachedPrice || cachedAsks.size() < topLevels;

        if (isWithinCache) {
            // LỆNH CÓ KHẢ NĂNG NẰM TRONG CACHE:
            // Thêm lệnh vào core book và cache book. Thao tác này nhanh hơn vì chủ yếu là thêm.
            addOrderToBook(order, coreBidLevels, coreAskLevels);
            addOrderToBook(order, cacheBidLevels, cacheAskLevels, true);

            // Cập nhật TreeMap cache
            NavigableMap<Long, Collection<PrimitiveOrder>> target =
                    (order.side == PrimitiveOrder.SIDE_BUY) ? cachedBids : cachedAsks;
            target.computeIfAbsent(order.price, k -> new ArrayList<>()).add(order);
            trimCache(target);
        } else {
            // LỆNH KHÔNG NẰM TRONG CACHE:
            // Chỉ thêm vào core book. Bỏ qua cache book và TreeMap cache.
            addOrderToBook(order, coreBidLevels, coreAskLevels);
        }

        // Luôn cập nhật lookup map
        ordersById.put(order.orderId, order);
    }

    @Override
    public void removeOrder(PrimitiveOrder order) {
        // Luôn xóa khỏi map lookup trước
        ordersById.remove(order.orderId);

        long worstCachedPrice = getWorstCachedPrice(order.side);
        boolean isWithinCache = (order.side == PrimitiveOrder.SIDE_BUY) ?
                order.price >= worstCachedPrice || cachedBids.containsKey(order.price) :
                order.price <= worstCachedPrice || cachedAsks.containsKey(order.price);

        if (isWithinCache) {
            // LỆNH ĐƯỢC CACHE: Xóa khỏi cả hai cuốn sổ
            removeOrderFromBook(order, coreBidLevels, coreAskLevels);
            removeOrderFromBook(order, cacheBidLevels, cacheAskLevels, true);

            // Cập nhật TreeMap cache
            NavigableMap<Long, Collection<PrimitiveOrder>> target =
                    (order.side == PrimitiveOrder.SIDE_BUY) ? cachedBids : cachedAsks;
            Collection<PrimitiveOrder> level = target.get(order.price);
            if (level != null) {
                level.remove(order);
                if (level.isEmpty()) {
                    target.remove(order.price);
                }
            }
        } else {
            // LỆNH KHÔNG ĐƯỢC CACHE: Chỉ xóa khỏi core book
            removeOrderFromBook(order, coreBidLevels, coreAskLevels);
        }
    }

    @Override
    public PrimitiveOrder getOrderDetail(long orderId) {
        // Truy cập duy nhất một map lookup
        return ordersById.get(orderId);
    }

    @Override
    public void clear() {
        coreBidLevels.clear();
        coreAskLevels.clear();
        Arrays.fill(cacheBidLevels, null);
        Arrays.fill(cacheAskLevels, null);
        ordersById.clear();
        cachedBids.clear();
        cachedAsks.clear();
    }

    @Override
    public void removeOrderFromLookUpMap(long orderId) {
        // Không cần làm gì, vì các thao tác add/remove chính đã xử lý map này.
        // Phương thức này có thể được loại bỏ hoặc giữ lại để đảm bảo interface.
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

    private void addOrderToBook(PrimitiveOrder order, NavigableMap<Long, ArrayDeque<PrimitiveOrder>> bidBook, NavigableMap<Long, ArrayDeque<PrimitiveOrder>> askBook) {
        NavigableMap<Long, ArrayDeque<PrimitiveOrder>> targetBook =
                (order.side == PrimitiveOrder.SIDE_BUY) ? bidBook : askBook;
        targetBook.computeIfAbsent(order.price, k -> new ArrayDeque<>()).addLast(order);
    }

    private void addOrderToBook(PrimitiveOrder order, OrderLevel[] bidLevels, OrderLevel[] askLevels, boolean isStaticArray) {
        int index = getScaledIndex(order.price);
        OrderLevel[] targetLevels = (order.side == PrimitiveOrder.SIDE_BUY) ? bidLevels : askLevels;
        if (index < 0 || index >= priceRange) {
            return; // Giá ngoài phạm vi, bỏ qua cache book.
        }
        if (targetLevels[index] == null) {
            targetLevels[index] = new OrderLevel();
        }
        targetLevels[index].add(order);
    }

    private void removeOrderFromBook(PrimitiveOrder order, NavigableMap<Long, ArrayDeque<PrimitiveOrder>> bidBook, NavigableMap<Long, ArrayDeque<PrimitiveOrder>> askBook) {
        NavigableMap<Long, ArrayDeque<PrimitiveOrder>> targetBook =
                (order.side == PrimitiveOrder.SIDE_BUY) ? bidBook : askBook;
        ArrayDeque<PrimitiveOrder> ordersAtLevel = targetBook.get(order.price);
        if (ordersAtLevel != null) {
            ordersAtLevel.remove(order);
            if (ordersAtLevel.isEmpty()) {
                targetBook.remove(order.price);
            }
        }
    }

    private void removeOrderFromBook(PrimitiveOrder order, OrderLevel[] bidLevels, OrderLevel[] askLevels, boolean isStaticArray) {
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

    private void trimCache(NavigableMap<Long, Collection<PrimitiveOrder>> map) {
        while (map.size() > topLevels) {
            map.pollLastEntry();
        }
    }
}