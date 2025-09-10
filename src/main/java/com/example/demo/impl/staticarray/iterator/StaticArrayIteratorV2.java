package com.example.demo.impl.staticarray.iterator;

import com.example.demo.core.PrimitiveOrder;
import com.example.demo.impl.staticarray.orderlevel.OrderLevel;
import com.example.demo.impl.staticarray.orderlevel.OrderLevelV2;

import java.util.*;

public class StaticArrayIteratorV2 implements Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> {

    private final OrderLevelV2[] levels;
    private final boolean isBid;
    private final long minPriceScaled;
    private int currentIndex;   // index của levels
    private Iterator<PrimitiveOrder> levelIterator; // iterator cho từng level

    public StaticArrayIteratorV2(OrderLevelV2[] levels, boolean isBid, long minPriceScaled) {
        this.levels = levels;
        this.isBid = isBid;
        this.minPriceScaled = minPriceScaled;
        this.currentIndex = isBid ? levels.length - 1 : 0;
        this.levelIterator = null;
        moveToNextNonEmptyLevel();
    }

    private void moveToNextNonEmptyLevel() {
        levelIterator = null;
        while (currentIndex >= 0 && currentIndex < levels.length) {
            OrderLevelV2 level = levels[currentIndex];
            if (level != null && !level.isEmpty()) {
                levelIterator = level.iterator();
                break;
            }
            currentIndex += isBid ? -1 : 1;
        }
    }

    @Override
    public boolean hasNext() {
        if (levelIterator != null && levelIterator.hasNext()) {
            return true;
        }
        // tìm level tiếp theo có order
        int tempIndex = currentIndex + (isBid ? -1 : 1);
        while (tempIndex >= 0 && tempIndex < levels.length) {
            OrderLevelV2 level = levels[tempIndex];
            if (level != null && !level.isEmpty()) {
                return true;
            }
            tempIndex += isBid ? -1 : 1;
        }
        return false;
    }

    @Override
    public Map.Entry<Long, ? extends Collection<PrimitiveOrder>> next() {
        if (levelIterator == null || !levelIterator.hasNext()) {
            moveToNextNonEmptyLevel();
        }
        if (levelIterator == null) throw new NoSuchElementException();

        // trả về nguyên level (Collection) theo price
        OrderLevelV2 level = levels[currentIndex];
        long price = currentIndex + minPriceScaled;

        // chuẩn bị cho lần gọi next tiếp theo
        currentIndex += isBid ? -1 : 1;
        levelIterator = null;

        return new AbstractMap.SimpleImmutableEntry<>(price, level);
    }
}