package com.example.demo.core;

public final class PrimitiveOrder implements Resettable {
    public long orderId;
    public int userId;
    public int instrumentId;
    public long price;         // scaled price
    public long originalQty;   // scaled quantity
    public long remainingQty;  // scaled quantity
    public long filledQty;     // scaled quantity
    public byte side;
    public byte status;
    public long timestamp;

    // Constants cho side
    public static final byte SIDE_BUY = 0;
    public static final byte SIDE_SELL = 1;

    // Constants cho status
    public static final byte STATUS_OPEN = 0;
    public static final byte STATUS_PARTIALLY_FILLED = 1;
    public static final byte STATUS_FILLED = 2;
    public static final byte STATUS_CANCELED = 3;

    // ===== Constructors =====

    // Constructor mặc định (dùng cho pool)
    public PrimitiveOrder() {}

    // Constructor đầy đủ
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

    // Constructor rút gọn (mặc định remainingQty = originalQty, filledQty = 0, status = OPEN)
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
        this.userId = other.userId;
        this.instrumentId = other.instrumentId;
        this.price = other.price;
        this.originalQty = other.originalQty;
        this.remainingQty = other.remainingQty;
        this.filledQty = other.filledQty;
        this.side = other.side;
        this.status = other.status;
        this.timestamp = other.timestamp;
    }

    // ===== Methods =====

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

    public void reset() {
        orderId = 0;
        userId = 0;
        instrumentId = 0;
        price = 0;
        originalQty = 0;
        remainingQty = 0;
        filledQty = 0;
        side = 0;
        status = STATUS_OPEN;
        timestamp = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "OrderPrimitive{id=%d, side=%d, price=%d, qty=%d, remaining=%d, filled=%d, status=%d}",
                orderId, side, price, originalQty, remainingQty, filledQty, status);
    }
}
