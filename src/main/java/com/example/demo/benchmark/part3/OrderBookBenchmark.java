package com.example.demo.benchmark.part3;

import com.example.demo.core.OrderBook;
import com.example.demo.core.PrimitiveOrder;
import com.example.demo.engine.MatchingEngine;
import com.example.demo.impl.staticarray.StaticArrayOrderBook;
import com.example.demo.impl.staticarray.StaticArrayOrderBookV2;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;


@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
public class OrderBookBenchmark {

    private MatchingEngine matchingEngineV1;
    private MatchingEngine matchingEngineV2;
    private static final int INSTRUMENT_ID = 1;
    private static final int USER_ID = 1;
    private static final int NUM_ORDERS_PREFILL = 5000;
    private static final int NUM_ORDERS_BENCHMARK = 1000;

    // Tạo các mảng lệnh cho mỗi benchmark để đảm bảo chúng không dùng chung
    private PrimitiveOrder[] benchmarkOrdersV1;
    private PrimitiveOrder[] benchmarkOrdersV2;

    @Setup(Level.Trial)
    public void setup() {
        // Cài đặt cho V1 và V2
        OrderBook orderBookV1 = new StaticArrayOrderBook(1, 20000, 0.01); // Tăng dung lượng tổng thể
        matchingEngineV1 = new MatchingEngine(orderBookV1);

        OrderBook orderBookV2 = new StaticArrayOrderBookV2(1, 20000, 0.01); // Tăng dung lượng tổng thể
        matchingEngineV2 = new MatchingEngine(orderBookV2);

        // Điền trước sổ lệnh để mô phỏng một thị trường bận rộn.
        // Lệnh được tạo với giá ngẫu nhiên để phân tán chúng trên nhiều mức giá, tránh lỗi dung lượng.
        for (int i = 0; i < NUM_ORDERS_PREFILL; i++) {
            matchingEngineV1.handleNewOrder(createRandomPriceOrder(i, PrimitiveOrder.SIDE_BUY));
            matchingEngineV2.handleNewOrder(createRandomPriceOrder(i, PrimitiveOrder.SIDE_SELL));
        }

        // Tạo tập hợp lệnh cho các benchmark chạy sau đó.
        benchmarkOrdersV1 = new PrimitiveOrder[NUM_ORDERS_BENCHMARK];
        benchmarkOrdersV2 = new PrimitiveOrder[NUM_ORDERS_BENCHMARK];
        for (int i = 0; i < NUM_ORDERS_BENCHMARK; i++) {
            // Tạo lệnh với giá ngẫu nhiên để kiểm tra hiệu năng tìm kiếm/chèn
            benchmarkOrdersV1[i] = createRandomPriceOrder(i + NUM_ORDERS_PREFILL, PrimitiveOrder.SIDE_BUY);
            benchmarkOrdersV2[i] = createRandomPriceOrder(i + NUM_ORDERS_PREFILL, PrimitiveOrder.SIDE_BUY);
        }
    }

    // Phương thức trợ giúp để tạo các lệnh có giá ngẫu nhiên trong một phạm vi.
    private PrimitiveOrder createRandomPriceOrder(long id, byte side) {
        // Tạo giá ngẫu nhiên trong một dải hẹp để có thể tạo ra các mức giá trùng lặp,
        // nhưng vẫn đủ rộng để không làm đầy một mức giá duy nhất.
        long price = 500_000 + (ThreadLocalRandom.current().nextInt(50) - 25) * 100;
        return new PrimitiveOrder(id, USER_ID, INSTRUMENT_ID, price, 10, side);
    }

    // Phương thức trợ giúp để tạo lệnh khớp
    private PrimitiveOrder createAggressorOrder(long id, long aggressorPrice, byte side, int quantity) {
        return new PrimitiveOrder(id, USER_ID, INSTRUMENT_ID, aggressorPrice, quantity, side);
    }

    // Benchmark: Thêm lệnh trong môi trường sổ lệnh đã bận rộn
    // Mục tiêu: Đo hiệu năng chèn lệnh khi sổ lệnh đã có nhiều lệnh chờ khớp
    @Benchmark
    public void benchmarkAddOrdersToBusyBookV1(Blackhole bh) {
        for (int i = 0; i < NUM_ORDERS_BENCHMARK; i++) {
            matchingEngineV1.handleNewOrder(benchmarkOrdersV1[i]);
        }
        bh.consume(matchingEngineV1);
    }

    @Benchmark
    public void benchmarkAddOrdersToBusyBookV2(Blackhole bh) {
        for (int i = 0; i < NUM_ORDERS_BENCHMARK; i++) {
            matchingEngineV2.handleNewOrder(benchmarkOrdersV2[i]);
        }
        bh.consume(matchingEngineV2);
    }

    // Benchmark: Thêm & Khớp lệnh trong môi trường tần suất cao
    // Mục tiêu: Đo hiệu năng chèn và khớp lệnh khi có cả lệnh chờ và lệnh khớp
    @Benchmark
    public void benchmarkHighFrequencyMixV1(Blackhole bh) {
        for (int i = 0; i < NUM_ORDERS_BENCHMARK; i++) {
            PrimitiveOrder order = createRandomPriceOrder(i + 2*NUM_ORDERS_PREFILL, (ThreadLocalRandom.current().nextDouble() > 0.5) ? PrimitiveOrder.SIDE_BUY : PrimitiveOrder.SIDE_SELL);
            matchingEngineV1.handleNewOrder(order);
        }
        bh.consume(matchingEngineV1);
    }

    @Benchmark
    public void benchmarkHighFrequencyMixV2(Blackhole bh) {
        for (int i = 0; i < NUM_ORDERS_BENCHMARK; i++) {
            PrimitiveOrder order = createRandomPriceOrder(i + 2*NUM_ORDERS_PREFILL, (ThreadLocalRandom.current().nextDouble() > 0.5) ? PrimitiveOrder.SIDE_BUY : PrimitiveOrder.SIDE_SELL);
            matchingEngineV2.handleNewOrder(order);
        }
        bh.consume(matchingEngineV2);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}