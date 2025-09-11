package com.example.demo.impl.staticarray.orderlevel;

import com.example.demo.core.PrimitiveOrder;

import java.util.*;

import java.util.Collection;
import java.util.Iterator;

public class OrderLevel implements Collection<PrimitiveOrder> {
    private static final int MAX_ORDERS_PER_LEVEL = 1000;
    private final PrimitiveOrder[] orders = new PrimitiveOrder[MAX_ORDERS_PER_LEVEL];
    private int size = 0;

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(orders[i], o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(orders, size);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return (T[]) Arrays.copyOf(orders, size, a.getClass());
    }

    @Override
    public boolean add(PrimitiveOrder primitiveOrder) {
        if (size >= MAX_ORDERS_PER_LEVEL) {
            throw new IllegalStateException("Order level capacity reached.");
        }
        orders[size++] = primitiveOrder;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(orders[i], o)) {
                System.arraycopy(orders, i + 1, orders, i, size - i - 1);
                orders[--size] = null;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object item : c) {
            if (!contains(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends PrimitiveOrder> c) {
        boolean modified = false;
        for (PrimitiveOrder item : c) {
            if (add(item)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object item : c) {
            if (remove(item)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        for (int i = 0; i < size; i++) {
            if (!c.contains(orders[i])) {
                remove(orders[i--]);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            orders[i] = null;
        }
        size = 0;
    }

    @Override
    public Iterator<PrimitiveOrder> iterator() {
        return new Iterator<>() {
            private int currentIndex = 0;
            private int lastReturnedIndex = -1; // Theo dõi chỉ mục của phần tử được trả về

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public PrimitiveOrder next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                lastReturnedIndex = currentIndex; // Cập nhật chỉ mục của phần tử vừa trả về
                return orders[currentIndex++];
            }

            @Override
            public void remove() {
                if (lastReturnedIndex < 0) {
                    throw new IllegalStateException("next() has not yet been called, or remove() has already been called after the last call to next().");
                }

                // Sử dụng phương thức remove của OrderLevel để xóa phần tử
                // Sau đó, điều chỉnh currentIndex để không bỏ sót phần tử nào
                OrderLevel.this.remove(orders[lastReturnedIndex]);
                currentIndex = lastReturnedIndex;
                lastReturnedIndex = -1;
            }
        };
    }
}
