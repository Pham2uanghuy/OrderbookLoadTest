package com.example.demo.core;

public final class PrimitiveOrder {
    public long orderId;
    public int instrumentId;
    public long price;         // scaled price
    public long originalQty;   // scaled quantity
    public long remainingQty;  // scaled quantity
    public long filledQty;     // scaled quantity
    public byte side;
    public byte status;
    public long timestamp;
    public int userId;

    // Side Constants
    public static final byte SIDE_BUY = 0;
    public static final byte SIDE_SELL = 1;

    // Status Constants
    public static final byte STATUS_OPEN = 0;
    public static final byte STATUS_PARTIALLY_FILLED = 1;
    public static final byte STATUS_FILLED = 2;
    public static final byte STATUS_CANCELED = 3;

    // No-arg constructor for serialization frameworks
    public PrimitiveOrder() {}

    /**
     * The primary constructor that initializes all fields.
     */
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
        this.userId = userId;
        this.instrumentId = instrumentId;
        this.price = price;
        this.originalQty = originalQty;
        this.remainingQty = remainingQty;
        this.filledQty = filledQty;
        this.side = side;
        this.status = status;
        this.timestamp = timestamp;
    }

    /**
     * A convenient constructor for creating a new, open order.
     * This constructor delegates to the primary constructor with default values for remainingQty, filledQty, status, and timestamp.
     */
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

    /**
     * Copy constructor.
     */
    public PrimitiveOrder(PrimitiveOrder other) {
        this(other.orderId, other.userId, other.instrumentId,
                other.price, other.originalQty, other.remainingQty,
                other.filledQty, other.side, other.status, other.timestamp);
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