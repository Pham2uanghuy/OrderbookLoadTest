package com.example.demo.impl.staticarray.iterator;

import com.example.demo.impl.staticarray.orderlevel.OrderLevel;
import com.example.demo.core.PrimitiveOrder;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import java.util.*;

import java.util.*;

public class StaticArrayIterator implements Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> {
    private final OrderLevel[] levels;
    private final boolean isBid;
    private int currentIndex;
    private int lastReturnedIndex = -1; // Thêm để hỗ trợ remove()
    private final long minPriceScaled;

    public StaticArrayIterator(OrderLevel[] levels, boolean isBid, long minPriceScaled) {
        this.levels = levels;
        this.isBid = isBid;
        this.minPriceScaled = minPriceScaled;
        this.currentIndex = isBid ? levels.length - 1 : 0;
    }

    @Override
    public boolean hasNext() {
        if (isBid) {
            while (currentIndex >= 0 && (levels[currentIndex] == null || levels[currentIndex].isEmpty())) {
                currentIndex--;
            }
            return currentIndex >= 0;
        } else {
            while (currentIndex < levels.length && (levels[currentIndex] == null || levels[currentIndex].isEmpty())) {
                currentIndex++;
            }
            return currentIndex < levels.length;
        }
    }

    @Override
    public Map.Entry<Long, ? extends Collection<PrimitiveOrder>> next() {
        if (!hasNext()) {
            throw new java.util.NoSuchElementException();
        }
        lastReturnedIndex = currentIndex; // Lưu chỉ mục của phần tử được trả về
        long price = getScaledPrice(currentIndex);
        OrderLevel level = levels[currentIndex];

        if (isBid) {
            currentIndex--;
        } else {
            currentIndex++;
        }
        return new AbstractMap.SimpleImmutableEntry<>(price, level);
    }

    private long getScaledPrice(int index) {
        return index + minPriceScaled;
    }

    @Override
    public void remove() {
        if (lastReturnedIndex < 0) {
            throw new IllegalStateException("next() has not yet been called, or remove() has already been called after the last call to next().");
        }

        // Đặt OrderLevel tại chỉ mục này thành null
        levels[lastReturnedIndex] = null;
        lastReturnedIndex = -1; // Reset để tránh xóa hai lần
    }
}