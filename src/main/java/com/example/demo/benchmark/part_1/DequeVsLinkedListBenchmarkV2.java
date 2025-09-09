package com.example.demo.benchmark.part_1;

import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;
import com.example.demo.engine.MatchingEngine;
import com.example.demo.impl.arraydeque.ArrayDequeOrderBook;
import com.example.demo.impl.arraydeque.LongObjectArrayDequeOrderBook;
import com.example.demo.impl.linkedlist.LinkedListOrderBook;
import com.example.demo.impl.linkedlist.LongObjectLinkedListOrderBook;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class DequeVsLinkedListBenchmarkV2 {

    private static final int WARMUP_ROUNDS = 3;
    private static final int MEASURE_ROUNDS = 5;

    public static void main(String[] args) {
        int[] loadSizes = {50_000};
        int btcPriceStep = 1;

        for (int loadSize : loadSizes) {
            System.out.println("Benchmark with " + loadSize + " orders");

            // Scenario A: small range, clustered
            System.out.println(">>> Scenario A: Small price range (~10k prices), orders clustered around mid-price");
            benchmarkOrderBook("ArrayDequeOrderBook_SMALL_CLUSTERED", new ArrayDequeOrderBook(), loadSize, 20000, 30000, btcPriceStep, "clustered");
            benchmarkOrderBook("LinkedListOrderBook_SMALL_CLUSTERED", new LinkedListOrderBook(), loadSize, 20000, 30000, btcPriceStep, "clustered");
            benchmarkOrderBook("LongObjectArrayDequeOrderBook_SMALL_CLUSTERED", new LongObjectArrayDequeOrderBook(), loadSize, 20000, 30000, btcPriceStep, "clustered");
            benchmarkOrderBook("LongObjectLinkedListOrderBook_SMALL_CLUSTERED", new LongObjectLinkedListOrderBook(), loadSize, 20000, 30000, btcPriceStep, "clustered");

            // Scenario B: small range, spread
            System.out.println(">>> Scenario B: Small price range (~10k prices), orders spread out");
            benchmarkOrderBook("ArrayDequeOrderBook_SMALL_SPREAD", new ArrayDequeOrderBook(), loadSize, 20000, 30000, btcPriceStep, "spread");
            benchmarkOrderBook("LinkedListOrderBook_SMALL_SPREAD", new LinkedListOrderBook(), loadSize, 20000, 30000, btcPriceStep, "spread");
            benchmarkOrderBook("LongObjectArrayDequeOrderBook_SMALL_SPREAD", new LongObjectArrayDequeOrderBook(), loadSize, 20000, 30000, btcPriceStep, "spread");
            benchmarkOrderBook("LongObjectLinkedListOrderBook_SMALL_SPREAD", new LongObjectLinkedListOrderBook(), loadSize, 20000, 30000, btcPriceStep, "spread");

            // Scenario C: large range, clustered
            benchmarkOrderBook("ArrayDequeOrderBook_LARGE_CLUSTERED", new ArrayDequeOrderBook(), loadSize, 1000, 1_001_000, btcPriceStep, "clustered");
            benchmarkOrderBook("LinkedListOrderBook_LARGE_CLUSTERED", new LinkedListOrderBook(), loadSize, 1000, 1_001_000, btcPriceStep, "clustered");
            System.out.println(">>> Scenario C: Large price range (~1M prices), orders clustered around mid-price");
            benchmarkOrderBook("LongObjectArrayDequeOrderBook_LARGE_CLUSTERED", new LongObjectArrayDequeOrderBook(), loadSize, 1000, 1_001_000, btcPriceStep, "clustered");
            benchmarkOrderBook("LongObjectLinkedListOrderBook_LARGE_CLUSTERED", new LongObjectLinkedListOrderBook(), loadSize, 1000, 1_001_000, btcPriceStep, "clustered");

            // Scenario D: large range, spread
            System.out.println(">>> Scenario D: Large price range (~1M prices), orders spread out");
            benchmarkOrderBook("ArrayDequeOrderBook_LARGE_SPREAD", new ArrayDequeOrderBook(), loadSize, 1000, 1_001_000, btcPriceStep, "spread");
            benchmarkOrderBook("LinkedListOrderBook_LARGE_SPREAD", new LinkedListOrderBook(), loadSize, 1000, 1_001_000, btcPriceStep, "spread");
            benchmarkOrderBook("LongObjectArrayDequeOrderBook_LARGE_SPREAD", new LongObjectArrayDequeOrderBook(), loadSize, 1000, 1_001_000, btcPriceStep, "spread");
            benchmarkOrderBook("LongObjectLinkedListOrderBook_LARGE_SPREAD", new LongObjectLinkedListOrderBook(), loadSize, 1000, 1_001_000, btcPriceStep, "spread");
        }
    }

    private static void benchmarkOrderBook(String name, OrderBook orderBook, int loadSize,
                                           int minPrice, int maxPrice, int step, String distribution) {
        MatchingEngine engine = new MatchingEngine(orderBook);
        PrimitiveOrder[] orders = preGenerateOrders(loadSize, minPrice, maxPrice, step, distribution);

        // Warm-up
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            runSingleRound(engine, orders, false);
        }

        // Measurement
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

        long usedMB = getUsedHeap() / (1024 * 1024);
        System.out.printf(" Memory usage after test: %d MB%n", usedMB);
        System.out.println("------------------------------------------------------");
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
        double p99 = latenciesNs[(int) (loadSize * 0.99)] / 1000.0;
        double p999 = latenciesNs[(int) (loadSize * 0.999)] / 1000.0;
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

    private static PrimitiveOrder[] preGenerateOrders(int loadSize, int minPrice, int maxPrice, int step, String distribution) {
        PrimitiveOrder[] orders = new PrimitiveOrder[loadSize];
        long orderId = 0;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Random gaussianRnd = new Random();
        int midPrice = minPrice + (maxPrice - minPrice) / 2;
        int spreadRange = (maxPrice - minPrice) / 10;

        for (int i = 0; i < loadSize; i++) {
            long price;
            if (distribution.equals("clustered")) {
                double gaussianValue = gaussianRnd.nextGaussian() * (spreadRange / 3.0);
                price = (long) (midPrice + gaussianValue);
                price = Math.max(minPrice, Math.min(maxPrice, price));
            } else {
                price = minPrice + rnd.nextLong((maxPrice - minPrice) / step) * step;
            }

            orders[i] = new PrimitiveOrder(
                    orderId++,
                    rnd.nextInt(1, 1000),
                    1,
                    price,
                    rnd.nextInt(1, 5),
                    (orderId % 2 == 0) ? PrimitiveOrder.SIDE_BUY : PrimitiveOrder.SIDE_SELL
            );
        }
        return orders;
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

        BenchmarkResult(double throughputOpsPerSec, double avgLatencyMicros, double p50, double p99,
                        double p999, double min, double max) {
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

