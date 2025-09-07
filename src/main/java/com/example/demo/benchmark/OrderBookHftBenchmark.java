package com.example.demo.benchmark;

import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;
import com.example.demo.engine.MatchingEngine;
import com.example.demo.impl.ArrayDequeOrderBook;
import com.example.demo.impl.StaticArrayOrderBook;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Arrays;


import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class OrderBookHftBenchmark {

    private static final int WARMUP_ROUNDS = 3;
    private static final int MEASURE_ROUNDS = 5;

    public static void main(String[] args) {
        int[] loadSizes = {50_000};

        // Cấu hình giá thực tế4
        double minPrice = 100.0;   // giá tối thiểu instrument
        double maxPrice = 200.0;   // giá tối đa instrument
        double priceStep = 0.01;   // bước giá cents

        for (int loadSize : loadSizes) {
            System.out.println("===== Benchmark with " + loadSize + " orders =====");

            // StaticArrayOrderBook
            StaticArrayOrderBook staticOrderBook = new StaticArrayOrderBook(minPrice, maxPrice, priceStep);
            benchmarkOrderBook("StaticArrayOrderBook", staticOrderBook, loadSize, minPrice, maxPrice, priceStep);

            // ArrayDequeOrderBook
            ArrayDequeOrderBook arrayDequeOrderBook = new ArrayDequeOrderBook();
            benchmarkOrderBook("ArrayDequeOrderBook", arrayDequeOrderBook, loadSize, minPrice, maxPrice, priceStep);

            // LongObjectArrayDeque
        }
    }

    private static void benchmarkOrderBook(String name, OrderBook orderBook, int loadSize,
                                           double minPrice, double maxPrice, double priceStep) {
        MatchingEngine engine = new MatchingEngine(orderBook);
        PrimitiveOrder[] orders = preGenerateOrders(loadSize, minPrice, maxPrice, priceStep);

        // Warm-up rounds
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            runSingleRound(engine, orders, false);
        }

        // Measurement rounds
        double totalThroughput = 0;
        double totalAvgLatency = 0;
        double[] p99Values = new double[MEASURE_ROUNDS];

        for (int i = 0; i < MEASURE_ROUNDS; i++) {
            BenchmarkResult result = runSingleRound(engine, orders, true);
            totalThroughput += result.throughputOpsPerSec;
            totalAvgLatency += result.avgLatencyMicros;
            p99Values[i] = result.p99;
        }

        System.out.printf("[%s] Average over %d rounds:%n", name, MEASURE_ROUNDS);
        System.out.printf(" Throughput: %.2f ops/sec%n", totalThroughput / MEASURE_ROUNDS);
        System.out.printf(" Avg Latency: %.2f µs/op%n", totalAvgLatency / MEASURE_ROUNDS);
        Arrays.sort(p99Values);
        System.out.printf(" P99 latency: %.2f µs%n", p99Values[MEASURE_ROUNDS / 2]);
        System.out.println();
    }

    private static PrimitiveOrder[] preGenerateOrders(int loadSize, double minPrice, double maxPrice, double priceStep) {
        PrimitiveOrder[] orders = new PrimitiveOrder[loadSize];
        long orderId = 0;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < loadSize; i++) {
            double price = minPrice + rnd.nextDouble() * (maxPrice - minPrice);
            long scaledPrice = Math.round(price / priceStep); // scale giá cho StaticArrayOrderBook
            orders[i] = new PrimitiveOrder(
                    orderId++,
                    rnd.nextInt(1, 1000),     // quantity
                    1,                         // instrumentId
                    scaledPrice,               // price scaled
                    rnd.nextInt(1, 5),         // userId
                    (orderId % 2 == 0) ? PrimitiveOrder.SIDE_BUY : PrimitiveOrder.SIDE_SELL
            );
        }
        return orders;
    }

    private static BenchmarkResult runSingleRound(MatchingEngine engine, PrimitiveOrder[] orders, boolean measure) {
        int loadSize = orders.length;

        long beforeGCCount = getTotalGcCount();
        long beforeGCTime = getTotalGcTime();
        long beforeHeapUsed = getUsedHeap();

        long[] latenciesNs = measure ? new long[loadSize] : null;

        long start = System.nanoTime();
        for (int i = 0; i < loadSize; i++) {
            if (measure) {
                long t0 = System.nanoTime();
                engine.handleNewOrder(orders[i]);
                latenciesNs[i] = System.nanoTime() - t0;
            } else {
                engine.handleNewOrder(orders[i]);
            }
        }
        long end = System.nanoTime();

        long afterGCCount = getTotalGcCount();
        long afterGCTime = getTotalGcTime();
        long afterHeapUsed = getUsedHeap();

        if (!measure) return null;

        long durationNs = end - start;
        double throughputOpsPerSec = loadSize / (durationNs / 1_000_000_000.0);
        double avgLatencyMicros = (durationNs / 1000.0) / loadSize;

        Arrays.sort(latenciesNs);
        double p50 = latenciesNs[loadSize / 2] / 1000.0;
        double p99 = latenciesNs[(int)(loadSize * 0.99)] / 1000.0;
        double p999 = latenciesNs[(int)(loadSize * 0.999)] / 1000.0;
        double minLatency = latenciesNs[0] / 1000.0;
        double maxLatency = latenciesNs[loadSize - 1] / 1000.0;

        System.out.printf(" Processed %,d orders in %.2f ms%n", loadSize, durationNs / 1_000_000.0);
        System.out.printf(" Throughput: %.2f ops/sec, Avg Latency: %.2f µs/op%n", throughputOpsPerSec, avgLatencyMicros);
        System.out.printf(" Min: %.2f µs, P50: %.2f µs, P99: %.2f µs, P99.9: %.2f µs, Max: %.2f µs%n",
                minLatency, p50, p99, p999, maxLatency);
        System.out.printf(" GC count: %d, GC time: %d ms, Heap delta: %.2f MB%n",
                (afterGCCount - beforeGCCount), (afterGCTime - beforeGCTime),
                (afterHeapUsed - beforeHeapUsed) / (1024.0 * 1024.0));
        System.out.println();

        return new BenchmarkResult(throughputOpsPerSec, avgLatencyMicros, p50, p99, p999, minLatency, maxLatency);
    }

    private static long getTotalGcCount() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
                .mapToLong(gc -> gc.getCollectionCount() != -1 ? gc.getCollectionCount() : 0)
                .sum();
    }

    private static long getTotalGcTime() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
                .mapToLong(gc -> gc.getCollectionTime() != -1 ? gc.getCollectionTime() : 0)
                .sum();
    }

    private static long getUsedHeap() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    private static class BenchmarkResult {
        double throughputOpsPerSec;
        double avgLatencyMicros;
        double p50, p99, p999, min, max;

        BenchmarkResult(double throughputOpsPerSec, double avgLatencyMicros, double p50, double p99, double p999, double min, double max) {
            this.throughputOpsPerSec = throughputOpsPerSec;
            this.avgLatencyMicros = avgLatencyMicros;
            this.p50 = p50;
            this.p99 = p99;
            this.p999 = p999;
            this.min = min;
            this.max = max;
        }
    }
}

