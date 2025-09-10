package com.example.demo.impl.staticarray.orderlevel;

import com.example.demo.core.PrimitiveOrder;

import java.util.*;

public class OrderLevelV2 implements Collection<PrimitiveOrder> {
    private static final int MAX_ORDERS_PER_LEVEL = 100;
    private final PrimitiveOrder[] orders = new PrimitiveOrder[MAX_ORDERS_PER_LEVEL];
    private final int[] nextIndex = new int[MAX_ORDERS_PER_LEVEL]; // linked list indices in array
    private int head = -1;
    private int tail = -1;
    private final Queue<Integer> freeSlots = new ArrayDeque<>();
    private int size = 0;

    public OrderLevelV2() {
        for (int i = 0; i < MAX_ORDERS_PER_LEVEL; i++) {
            nextIndex[i] = -1;
            freeSlots.add(i);
        }
    }

    @Override
    public boolean add(PrimitiveOrder order) {
        if (freeSlots.isEmpty()) throw new IllegalStateException("OrderLevel full");
        int index = freeSlots.poll();
        orders[index] = order;
        nextIndex[index] = 1;

        if (tail == -1) {
            head = tail = index;
        } else {
            nextIndex[tail] = index;
            tail = index;
        }
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        int prev = -1;
        int curr = head;
        while (curr != -1) {
            if (orders[curr] != null && orders[curr].equals(o)) {
                // remove from linked list
                if (prev == -1) head = nextIndex[curr];
                else nextIndex[prev] = nextIndex[curr];

                if (curr == tail) tail = prev;

                orders[curr] = null;
                nextIndex[curr] = -1;
                freeSlots.add(curr);
                size--;
                return true;
            }
            prev = curr;
            curr = nextIndex[curr];
        }
        return false;
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        Arrays.fill(orders, null);
        Arrays.fill(nextIndex, -1);
        freeSlots.clear();
        for (int i = 0; i < MAX_ORDERS_PER_LEVEL; i++) freeSlots.add(i);
        head = tail = -1;
        size = 0;
    }

    // Các phương thức không dùng
    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends PrimitiveOrder> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    private void unlink(int curr, int prev) {
        if (prev == -1) head = nextIndex[curr];
        else nextIndex[prev] = nextIndex[curr];

        if (curr == tail) tail = prev;

        orders[curr] = null;
        nextIndex[curr] = -1;
        freeSlots.add(curr);
        size--;
    }

    @Override
    public Iterator<PrimitiveOrder> iterator() {
        return new Iterator<>() {
            private int current = head;
            private int lastReturned = -1;
            private int lastReturnedPrev = -1;

            @Override
            public boolean hasNext() {
                return current != -1;
            }

            @Override
            public PrimitiveOrder next() {
                if (!hasNext()) throw new NoSuchElementException();
                lastReturned = current;
                PrimitiveOrder order = orders[current];
                lastReturnedPrev = findPrev(current);
                current = nextIndex[current];
                return order;
            }

            @Override
            public void remove() {
                if (lastReturned == -1) throw new IllegalStateException("next() chưa được gọi hoặc đã remove rồi");
                unlink(lastReturned, lastReturnedPrev);
                lastReturned = -1;
            }

            private int findPrev(int idx) {
                if (idx == head) return -1;
                int p = head;
                while (p != -1 && nextIndex[p] != idx) {
                    p = nextIndex[p];
                }
                return p;
            }
        };
    }


}
