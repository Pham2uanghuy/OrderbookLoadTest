
## Introduction
In an OrderBook, we need to keep track of **active orders** for matching.  
By default, we often use `HashMap<Long, Order>`. However, since `Long` is not a primitive, the JVM must wrap and unbox `long` values into `Long` objects.

To reduce this overhead, we experimented with `LongObjectHashMap` (a primitive-specialized map).  
The expectation: **lower latency and higher throughput**.  
The results, however, provide interesting insights that differ across scenarios.

## Results

### Scenario A – Small range (~10k), clustered

| Data Structure                | Throughput (10k) | Throughput (50k) | Avg Latency (10k) | Avg Latency (50k) | P99 (10k) | P99 (50k) | Memory (10k) | Memory (50k) |
|-------------------------------|-----------------:|-----------------:|------------------:|------------------:|----------:|----------:|-------------:|-------------:|
| **LongObjectArrayDequeOrderBook** | 278,817 ops/s    | 77,223 ops/s     | 3.66 µs/op        | 13.26 µs/op       | 28.50 µs  | 118.80 µs | 94 MB        | 196 MB       |
| **ArrayDequeOrderBook**           | 157,820 ops/s    | 89,902 ops/s     | 6.56 µs/op        | 11.26 µs/op       | 58.60 µs  | 103.60 µs | 41 MB        | 146 MB       |
| **LongObjectLinkedListOrderBook** | 139,182 ops/s    | 37,350 ops/s     | 8.80 µs/op        | 27.61 µs/op       | 89.50 µs  | 316.70 µs | 40 MB        | 231 MB       |
| **LinkedListOrderBook**           | 81,672 ops/s     | 40,717 ops/s     | 12.60 µs/op       | 25.49 µs/op       | 125.00 µs | 259.60 µs | 47 MB        | 175 MB       |


### Scenario B – Small range (~10k), spread out

| Data Structure                | Throughput (10k) | Throughput (50k) | Avg Latency (10k) | Avg Latency (50k) | P99 (10k) | P99 (50k) | Memory (10k) | Memory (50k) |
|-------------------------------|-----------------:|-----------------:|------------------:|------------------:|----------:|----------:|-------------:|-------------:|
| **LongObjectArrayDequeOrderBook** | 229,552 ops/s    | 33,527 ops/s     | 4.42 µs/op        | 30.51 µs/op       | 37.30 µs  | 363.50 µs | 47 MB        | 114 MB       |
| **ArrayDequeOrderBook**           | 189,265 ops/s    | 48,847 ops/s     | 5.39 µs/op        | 21.02 µs/op       | 41.80 µs  | 189.20 µs | 110 MB       | 266 MB       |
| **LongObjectLinkedListOrderBook** | 123,211 ops/s    | 16,910 ops/s     | 9.28 µs/op        | 62.85 µs/op       | 71.30 µs  | 791.70 µs | 230 MB       | 244 MB       |
| **LinkedListOrderBook**           | 119,436 ops/s    | 13,333 ops/s     | 9.92 µs/op        | 81.92 µs/op       | 65.80 µs  | 902.00 µs | 64 MB        | 310 MB       |


### Scenario C – Large range (~1M), clustered

| Data Structure                | Throughput (10k) | Throughput (50k) | Avg Latency (10k) | Avg Latency (50k) | P99 (10k) | P99 (50k) | Memory (10k) | Memory (50k) |
|-------------------------------|-----------------:|-----------------:|------------------:|------------------:|----------:|----------:|-------------:|-------------:|
| **LongObjectArrayDequeOrderBook** | 192,608 ops/s    | 19,753 ops/s     | 5.24 µs/op        | 51.15 µs/op       | 44.30 µs  | 514.20 µs | 159 MB       | 210 MB       |
| **ArrayDequeOrderBook**           | 187,249 ops/s    | 16,050 ops/s     | 5.41 µs/op        | 65.55 µs/op       | 43.80 µs  | 686.40 µs | 143 MB       | 336 MB       |
| **LongObjectLinkedListOrderBook** | 103,661 ops/s    | 10,084 ops/s     | 10.19 µs/op       | 110.82 µs/op      | 97.50 µs  | 1455.70 µs| 84 MB        | 233 MB       |
| **LinkedListOrderBook**           | 101,492 ops/s    | 8,431 ops/s      | 10.18 µs/op       | 126.76 µs/op      | 103.30 µs | 1560.90 µs| 39 MB        | 392 MB       |


### Scenario D – Large range (~1M), spread out

| Data Structure                | Throughput (10k) | Throughput (50k) | Avg Latency (10k) | Avg Latency (50k) | P99 (10k) | P99 (50k) | Memory (10k) | Memory (50k) |
|-------------------------------|-----------------:|-----------------:|------------------:|------------------:|----------:|----------:|-------------:|-------------:|
| **LongObjectArrayDequeOrderBook** | 150,473 ops/s    | 9,782 ops/s      | 6.95 µs/op        | 113.27 µs/op      | 65.40 µs  | 1174.00 µs | 21 MB        | 308 MB       |
| **ArrayDequeOrderBook**           | 190,281 ops/s    | 14,622 ops/s     | 5.39 µs/op        | 68.71 µs/op       | 42.20 µs  | 801.70 µs  | 198 MB       | 420 MB       |
| **LongObjectLinkedListOrderBook** | 133,956 ops/s    | 7,632 ops/s      | 7.86 µs/op        | 139.28 µs/op      | 77.50 µs  | 1753.80 µs | 100 MB       | 80 MB        |
| **LinkedListOrderBook**           | 92,817 ops/s     | 9,282 ops/s      | 121.44 µs/op      | 121.44 µs/op      | 1248.40 µs| 1248.40 µs | 450 MB       | 450 MB       |

## Observations
1. **ArrayDeque** consistently outperforms LinkedList in both throughput and latency
2. **LongObjectHashMap** helps at **10k orders**, showing lower latency and higher throughput, but becomes less stable at **50k orders**.
3. Memory usage is not linear: in some cases, `LongObject` variants consume more memory due to internal structures.
4. **LinkedList** is the worst choice in all scenarios, both in terms of performance and memory footprint.  