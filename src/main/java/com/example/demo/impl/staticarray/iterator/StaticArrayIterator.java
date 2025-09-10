package com.example.demo.impl.staticarray.iterator;

import com.example.demo.impl.staticarray.orderlevel.OrderLevel;
import com.example.demo.core.PrimitiveOrder;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class StaticArrayIterator implements Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> {
    private final OrderLevel[] levels;
    private final boolean isBid;
    private int currentIndex;

    public StaticArrayIterator(OrderLevel[] levels, boolean isBid) {
        this.levels = levels;
        this.isBid = isBid;
        this.currentIndex = isBid ? levels.length - 1 : 0;
    }

    @Override
    public boolean hasNext() {
        if (isBid) {
            while (currentIndex >= 0 && levels[currentIndex] == null) {
                currentIndex--;
            }
            return currentIndex >= 0;
        } else {
            return currentIndex < levels.length;
        }
    }

    @Override
    public Map.Entry<Long, ? extends Collection<PrimitiveOrder>> next() {
        if (!hasNext()) {
            throw new java.util.NoSuchElementException();
        }
        // Cần lấy giá đã scale từ index, nhưng tạm thời bỏ qua phần đó để đơn giản hóa
        long price = currentIndex;
        OrderLevel level = levels[currentIndex];

        if (isBid) {
            currentIndex--;
        } else {
            currentIndex++;
        }
        return new AbstractMap.SimpleImmutableEntry<>(price, level);
    }
}