package com.example.demo.core;

public final class PrimitiveOrder{
    public long orderId;
    public int instrumentId;
    public long price;         // scaled price
    public long originalQty;   // scaled quantity
    public long remainingQty;  // scaled quantity
    public long filledQty;     // scaled quantity
    public byte side;
    public byte status;
    public long timestamp;

    // Side Constants
    public static final byte SIDE_BUY = 0;
    public static final byte SIDE_SELL = 1;

    // Status Constants
    public static final byte STATUS_OPEN = 0;
    public static final byte STATUS_PARTIALLY_FILLED = 1;
    public static final byte STATUS_FILLED = 2;
    public static final byte STATUS_CANCELED = 3;


    public PrimitiveOrder() {}


    public PrimitiveOrder(long orderId,
                          int userId,
                          int instrumentId,
                          long price,
                          long originalQty,
                          long remainingQty,
                          long filledQty,
                          byte side,
                          byte status,
                          long timestamp) {
        this.orderId = orderId;
        this.instrumentId = instrumentId;
        this.price = price;
        this.originalQty = originalQty;
        this.remainingQty = remainingQty;
        this.filledQty = filledQty;
        this.side = side;
        this.status = status;
        this.timestamp = timestamp;
    }

    public PrimitiveOrder(long orderId,
                          int userId,
                          int instrumentId,
                          long price,
                          long originalQty,
                          byte side) {
        this(orderId, userId, instrumentId, price,
                originalQty, originalQty, 0,
                side, STATUS_OPEN, System.nanoTime());
    }

    // Constructor copy
    public PrimitiveOrder(PrimitiveOrder other) {
        this.orderId = other.orderId;
        this.instrumentId = other.instrumentId;
        this.price = other.price;
        this.originalQty = other.originalQty;
        this.remainingQty = other.remainingQty;
        this.filledQty = other.filledQty;
        this.side = other.side;
        this.status = other.status;
        this.timestamp = other.timestamp;
    }


    public void fill(long fillAmount) {
        if (fillAmount <= 0) return;

        this.filledQty += fillAmount;
        this.remainingQty -= fillAmount;

        if (this.remainingQty <= 0) {
            this.status = STATUS_FILLED;
            this.remainingQty = 0;
        } else {
            this.status = STATUS_PARTIALLY_FILLED;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "OrderPrimitive{id=%d, side=%d, price=%d, qty=%d, remaining=%d, filled=%d, status=%d}",
                orderId, side, price, originalQty, remainingQty, filledQty, status);
    }
}
