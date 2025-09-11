package com.example.demo.benchmark.part3;

import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;
import com.example.demo.engine.MatchingEngine;
import com.example.demo.impl.staticarray.StaticArrayOrderBook;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class StaticArrayTest {
    public static void main(String[] args) {
        // 1. Khởi tạo OrderBook và MatchingEngine
        // Giả sử giá có bước nhảy là 1.0, từ 100.0 đến 200.0
        OrderBook orderBook = new StaticArrayOrderBook(100.0, 200.0, 1.0);
        MatchingEngine matchingEngine = new MatchingEngine(orderBook);

        // 2. Tạo các lệnh chờ (Resting Orders)
        // Các tham số userId và instrumentId được thêm vào
        int userId = 101;
        int instrumentId = 123;

        // Lệnh Bán (asks)
        PrimitiveOrder ask1 = new PrimitiveOrder(1, userId, instrumentId, 150L, 100L, PrimitiveOrder.SIDE_SELL);
        PrimitiveOrder ask2 = new PrimitiveOrder(2, userId, instrumentId, 151L, 50L, PrimitiveOrder.SIDE_SELL);
        PrimitiveOrder ask3 = new PrimitiveOrder(3, userId, instrumentId, 152L, 20L, PrimitiveOrder.SIDE_SELL);

        matchingEngine.handleNewOrder(ask1);
        matchingEngine.handleNewOrder(ask2);
        matchingEngine.handleNewOrder(ask3);


        System.out.println("--- Sau khi thêm các lệnh bán chờ ---");
        printOrderBook(orderBook);

        // Lệnh Mua (bids)
        PrimitiveOrder bid1 = new PrimitiveOrder(4, userId, instrumentId, 148L, 80L, PrimitiveOrder.SIDE_BUY);
        PrimitiveOrder bid2 = new PrimitiveOrder(5, userId, instrumentId, 147L, 70L, PrimitiveOrder.SIDE_BUY);

        matchingEngine.handleNewOrder(bid1);
        matchingEngine.handleNewOrder(bid2);

        System.out.println("\n--- Sau khi thêm các lệnh mua chờ ---");
        printOrderBook(orderBook);

        // 3. Gửi một lệnh chủ động để khớp (Aggressor Order)
        // Lệnh mua mới với số lượng 120, giá 152
        PrimitiveOrder aggressorBuyOrder = new PrimitiveOrder(6, userId, instrumentId, 152L, 120L, PrimitiveOrder.SIDE_BUY);
        System.out.println("\n--- Xử lý lệnh mua chủ động: " + aggressorBuyOrder.toString() + " ---");
        matchingEngine.handleNewOrder(aggressorBuyOrder);

        // 4. Kiểm tra kết quả
        System.out.println("\n--- Sổ lệnh sau khi khớp ---");
        printOrderBook(orderBook);

        System.out.println("\n--- Trạng thái của các lệnh ---");
        System.out.println("Lệnh 1 (Bán): " + orderBook.getOrderDetail(1));
        System.out.println("Lệnh 2 (Bán): " + orderBook.getOrderDetail(2));
        System.out.println("Lệnh 3 (Bán): " + orderBook.getOrderDetail(3));
        System.out.println("Lệnh 6 (Mua): " + aggressorBuyOrder);
    }

    private static void printOrderBook(OrderBook orderBook) {
        System.out.println("BID Side:");
        Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> bidIter = orderBook.getBidLevelsIterator();
        while (bidIter.hasNext()) {
            Map.Entry<Long, ? extends Collection<PrimitiveOrder>> level = bidIter.next();
            System.out.printf("  Giá %d: %s%n", level.getKey(), level.getValue().toString());
        }

        System.out.println("ASK Side:");
        Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> askIter = orderBook.getAskLevelsIterator();
        while (askIter.hasNext()) {
            Map.Entry<Long, ? extends Collection<PrimitiveOrder>> level = askIter.next();
            System.out.printf("  Giá %d: %s%n", level.getKey(), level.getValue().toString());
        }
    }
}
