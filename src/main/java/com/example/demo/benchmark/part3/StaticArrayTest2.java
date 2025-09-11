package com.example.demo.benchmark.part3;

import com.example.demo.core.PrimitiveOrder;
import com.example.demo.engine.MatchingEngine;
import com.example.demo.impl.staticarray.StaticArrayOrderBookV2;

public class StaticArrayTest2 {
    // Phương thức main để chạy chương trình
    public static void main(String[] args) {
        System.out.println("--- Bắt đầu kiểm tra MatchingEngine ---");

        // Khởi tạo OrderBook và MatchingEngine
        StaticArrayOrderBookV2 orderBook = new StaticArrayOrderBookV2(100, 200, 1);
        MatchingEngine matchingEngine = new MatchingEngine(orderBook);

        // --- Test Case 1: Khớp lệnh mua một phần ---
        System.out.println("\n--- Test Case 1: Khớp lệnh mua một phần ---");

        // Đặt một lệnh bán nghỉ (resting sell order)
        PrimitiveOrder restingSellOrder = new PrimitiveOrder(1, 100, 1, 150, 10, PrimitiveOrder.SIDE_SELL);
        orderBook.addOrder(restingSellOrder);
        System.out.println("Sổ lệnh: Đặt lệnh bán nghỉ #" + restingSellOrder.orderId + " (Giá: " + restingSellOrder.price + ", Số lượng: " + restingSellOrder.remainingQty + ")");

        // Đặt một lệnh mua công (aggressor buy order) khớp một phần
        PrimitiveOrder newBuyOrder = new PrimitiveOrder(2, 101, 1, 160, 5, PrimitiveOrder.SIDE_BUY);
        System.out.println("Lệnh mới: Đặt lệnh mua công #" + newBuyOrder.orderId + " (Giá: " + newBuyOrder.price + ", Số lượng: " + newBuyOrder.originalQty + ")");
        matchingEngine.handleNewOrder(newBuyOrder);

        // Kiểm tra kết quả
        System.out.println("\n--- Kết quả Test Case 1 ---");
        System.out.println("Lệnh mua #" + newBuyOrder.orderId + " còn lại: " + newBuyOrder.remainingQty + " (Trạng thái: " + newBuyOrder.status + ")");
        System.out.println("Lệnh bán #" + restingSellOrder.orderId + " còn lại: " + restingSellOrder.remainingQty + " (Trạng thái: " + restingSellOrder.status + ")");
        System.out.println("Kiểm tra sổ lệnh: Lệnh mua còn tồn tại? " + (orderBook.getOrderDetail(newBuyOrder.orderId) != null));
        System.out.println("Kiểm tra sổ lệnh: Lệnh bán còn tồn tại? " + (orderBook.getOrderDetail(restingSellOrder.orderId) != null));


        // --- Test Case 2: Khớp lệnh mua hoàn toàn ---
        System.out.println("\n\n--- Test Case 2: Khớp lệnh mua hoàn toàn ---");
        orderBook.clear(); // Xóa sổ lệnh để bắt đầu test mới

        // Đặt một lệnh bán nghỉ
        PrimitiveOrder restingSellOrder2 = new PrimitiveOrder(3, 102, 1, 150, 10, PrimitiveOrder.SIDE_SELL);
        orderBook.addOrder(restingSellOrder2);
        System.out.println("Sổ lệnh: Đặt lệnh bán nghỉ #" + restingSellOrder2.orderId + " (Giá: " + restingSellOrder2.price + ", Số lượng: " + restingSellOrder2.remainingQty + ")");

        // Đặt một lệnh mua công khớp hoàn toàn
        PrimitiveOrder newBuyOrder2 = new PrimitiveOrder(4, 103, 1, 160, 10, PrimitiveOrder.SIDE_BUY);
        System.out.println("Lệnh mới: Đặt lệnh mua công #" + newBuyOrder2.orderId + " (Giá: " + newBuyOrder2.price + ", Số lượng: " + newBuyOrder2.originalQty + ")");
        matchingEngine.handleNewOrder(newBuyOrder2);

        // Kiểm tra kết quả
        System.out.println("\n--- Kết quả Test Case 2 ---");
        System.out.println("Lệnh mua #" + newBuyOrder2.orderId + " còn lại: " + newBuyOrder2.remainingQty + " (Trạng thái: " + newBuyOrder2.status + ")");
        System.out.println("Lệnh bán #" + restingSellOrder2.orderId + " còn lại: " + restingSellOrder2.remainingQty + " (Trạng thái: " + restingSellOrder2.status + ")");
        System.out.println("Kiểm tra sổ lệnh: Lệnh mua còn tồn tại? " + (orderBook.getOrderDetail(newBuyOrder2.orderId) != null));
        System.out.println("Kiểm tra sổ lệnh: Lệnh bán còn tồn tại? " + (orderBook.getOrderDetail(restingSellOrder2.orderId) != null));

        System.out.println("\n--- Kết thúc kiểm tra ---");
    }
}
