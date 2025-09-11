package com.example.demo.impl.staticarray.orderlevel;

import com.example.demo.core.PrimitiveOrder;

import java.util.*;

public class OrderLevelV2 implements Collection<PrimitiveOrder> {
    private static final int MAX_ORDERS_PER_LEVEL = 1000;
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
        if (freeSlots.isEmpty()) {
            throw new IllegalStateException("OrderLevel full");
        }
        // Get one free index from queue
        int index = freeSlots.poll();
        orders[index] = order;

        // new element point to -1
        nextIndex[index] = -1;

        if (tail == -1) {
            // if list is empty, new element is both head and tail
            head = tail = index;
        } else {
            // if not, link tail to the new element, update tail
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
        // iterate linked list, find object need to be removed
        while (curr != -1) {
            if (orders[curr] != null && orders[curr].equals(o)) {
                // remove from linked list
                // if object is the first elemetn in linkedlist,
                // then remove it from linkedlist
                if (prev == -1) head = nextIndex[curr];
                // if not, skip current element
                else nextIndex[prev] = nextIndex[curr];

                // update tail if the last element was removed
                if (curr == tail) tail = prev;

                orders[curr] = null; // free slot
                nextIndex[curr] = -1; // reset reference
                freeSlots.add(curr); // return index to queue
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
            private int current = head; // current element in linkedlist
            private int prev = -1; // the previous element
            private int lastReturned = -1; // index of the element returned by next()

            @Override
            public boolean hasNext() {
                return current != -1;
            }

            @Override
            public PrimitiveOrder next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                // save index of returned element
                lastReturned = current;

                // update prev adn current for the next return
                prev = current;
                current = nextIndex[current];

                return orders[lastReturned];
            }

            @Override
            public void remove() {
                if (lastReturned == -1) {
                    throw new IllegalStateException("next() chưa được gọi hoặc đã remove rồi");
                }
                // Sử dụng prev và lastReturned để xóa
                unlink(lastReturned, prev);
                lastReturned = -1;
            }
        };
    }


    // Unused methods for matching, no need to implement
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
}
