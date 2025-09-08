package com.example.demo.impl.staticarray;

import com.example.demo.core.PrimitiveOrder;

import java.util.Collection;
import java.util.Iterator;

public class OrderLevel implements Collection<PrimitiveOrder> {
    private static final int MAX_ORDERS_PER_LEVEL = 100;
    private final PrimitiveOrder[] orders = new PrimitiveOrder[MAX_ORDERS_PER_LEVEL];
    private int size = 0;


    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < size; i++) {
            if (orders[i] != null && orders[i].equals(o)) {
                System.arraycopy(orders, i + 1, orders, i, size - i - 1);
                orders[--size] = null;
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<PrimitiveOrder> iterator() {
        return new Iterator<>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public PrimitiveOrder next() {
                return orders[currentIndex++];
            }
        };
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(PrimitiveOrder primitiveOrder) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends PrimitiveOrder> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }


}
