package com.example.demo.core;

import java.math.BigDecimal;
import java.time.Instant;
import java.math.RoundingMode;

public class Order {
    private String orderId;
    private String userId;
    private String instrumentId;
    private BigDecimal price;
    private BigDecimal originalQuantity;
    private BigDecimal filledQuantity;
    private BigDecimal remainingQuantity;
    private OrderSide side;
    private OrderStatus status;
    private Instant timestamp;

    // Scale mặc định (số chữ số thập phân giữ lại khi convert sang long)
    public static final int PRICE_SCALE = 8;    // ví dụ: crypto price (1e-8)
    public static final int QTY_SCALE = 8;      // ví dụ: crypto qty (1e-8)

    public Order() {
        this.filledQuantity = BigDecimal.ZERO.setScale(QTY_SCALE, RoundingMode.DOWN);
    }

    public Order(String orderId, String userId, String instrumentId, OrderSide side,
                 BigDecimal price, BigDecimal quantity, OrderStatus status, Instant timestamp) {
        this.orderId = orderId;
        this.userId = userId;
        this.instrumentId = instrumentId;
        this.side = side;

        // Chuẩn hóa scale để an toàn khi convert sang long
        this.price = price.setScale(PRICE_SCALE, RoundingMode.DOWN);
        this.originalQuantity = quantity.setScale(QTY_SCALE, RoundingMode.DOWN);

        this.remainingQuantity = this.originalQuantity;
        this.status = status;
        this.timestamp = timestamp;
        this.filledQuantity = BigDecimal.ZERO.setScale(QTY_SCALE, RoundingMode.DOWN);
    }

    public void fill(BigDecimal fillAmount) {
        if (fillAmount == null || fillAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal normalizedFill = fillAmount.setScale(QTY_SCALE, RoundingMode.DOWN);

        this.filledQuantity = this.filledQuantity.add(normalizedFill);
        this.remainingQuantity = this.remainingQuantity.subtract(normalizedFill);

        if (this.remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = OrderStatus.FILLED;
            this.remainingQuantity = BigDecimal.ZERO.setScale(QTY_SCALE, RoundingMode.DOWN);
        } else {
            this.status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    // Convert sang dạng primitive (cho engine / pooling)
    public PrimitiveOrder toPrimitive(long numericOrderId, int numericUserId, int numericInstrumentId) {
        PrimitiveOrder p = new PrimitiveOrder();
        p.orderId = numericOrderId;
        p.userId = numericUserId;
        p.instrumentId = numericInstrumentId;

        // Convert BigDecimal → long với scale và kiểm tra tràn số
        p.price = this.price.movePointRight(PRICE_SCALE).longValueExact();
        p.originalQty = this.originalQuantity.movePointRight(QTY_SCALE).longValueExact();
        p.remainingQty = this.remainingQuantity.movePointRight(QTY_SCALE).longValueExact();
        p.filledQty = this.filledQuantity.movePointRight(QTY_SCALE).longValueExact();

        // Side mapping an toàn
        p.side = (byte) (this.side == OrderSide.BUY ? PrimitiveOrder.SIDE_BUY : PrimitiveOrder.SIDE_SELL);

        // Status mapping an toàn (không phụ thuộc ordinal)
        p.status = switch (this.status) {
            case OPEN -> PrimitiveOrder.STATUS_OPEN;
            case PARTIALLY_FILLED -> PrimitiveOrder.STATUS_PARTIALLY_FILLED;
            case FILLED -> PrimitiveOrder.STATUS_FILLED;
            case CANCELED -> PrimitiveOrder.STATUS_CANCELED;
        };

        p.timestamp = this.timestamp.toEpochMilli();
        return p;
    }

    // Getter/Setter
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getInstrumentId() { return instrumentId; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price.setScale(PRICE_SCALE, RoundingMode.DOWN); }

    public BigDecimal getOriginalQuantity() { return originalQuantity; }
    public void setOriginalQuantity(BigDecimal originalQuantity) {
        this.originalQuantity = originalQuantity.setScale(QTY_SCALE, RoundingMode.DOWN);
    }

    public BigDecimal getFilledQuantity() { return filledQuantity; }
    public void setFilledQuantity(BigDecimal filledQuantity) {
        this.filledQuantity = filledQuantity.setScale(QTY_SCALE, RoundingMode.DOWN);
    }

    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(BigDecimal remainingQuantity) {
        this.remainingQuantity = remainingQuantity.setScale(QTY_SCALE, RoundingMode.DOWN);
    }

    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("Order{id=%s, side=%s, price=%s, qty=%s, remaining=%s, status=%s}",
                orderId, side, price, originalQuantity, remainingQuantity, status);
    }
}
