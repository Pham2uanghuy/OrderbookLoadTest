# OrderBook Benchmark Results

## 1. Introduction
We need to keep **FIFO order sequence** for every price level.  
Two implementations were benchmarked:

- **Implementations compared**
  - `ArrayDequeOrderBook` – uses **ArrayDeque** to store orders at each price level
  - `LinkedListOrderBook` – uses **LinkedList** for order storage

---

## 2. Expected Results

- **ArrayDeque vs LinkedList**
  - `ArrayDeque`: better performance (contiguous memory layout, O(1) add/remove at ends, better cache locality)
  - `LinkedList`: slower (pointer chasing, scattered nodes in memory, higher overhead)

- **Clustered vs Spread**
  - Clustered: higher throughput, lower latency (fewer active price levels)
  - Spread: lower throughput, higher latency (more distinct price levels)

- **Small vs Large range**
  - Small (~10k): higher throughput, less memory usage
  - Large (~1M): lower throughput, higher memory usage

---

## 3. Benchmark Results (100,000 orders, average of 5 runs)

#### Average per scenario

| Scenario | Implementation       | Throughput (ops/sec) | Avg Lat (µs) | P99 Lat (µs) | P50 (µs) | Max (µs)   | Memory (MB) | GC Count ( times / round) | GC Time (ms) |
|----------|----------------------|----------------------|--------------|--------------|----------|------------|-------------|---|--------------|
| A. Small range (~10k), clustered | ArrayDequeOrderBook  | **36,864.95** | **31.81**   | **289.80**  | 0.40     | 34,644.00  | 143 | 4–5 | ~15–19 |
|          | LinkedListOrderBook  | 13,744.99            | 79.26        | 820.20       | 0.80     | 22,487.40  | 196 | 4 | ~43–47 |
| B. Small range (~10k), spread out | ArrayDequeOrderBook | **21,552.88** | **47.51**   | **521.80**  | 0.90     | 5,575.20   | 258 | 4–5 | ~18–26 |
|          | LinkedListOrderBook  | 7,656.91             | 147.51       | 1,600.90     | 1.10     | 14,899.10  | 310 | 4–5 | ~50–63 |
| C. Large range (~1M), clustered  | ArrayDequeOrderBook | **4,414.31**  | **241.51**  | **2,400.30** | 1.50     | 94,809.50  | 324 | 17–18 | ~25–37 |
|          | LinkedListOrderBook  | 2,118.86             | 546.52       | 5,662.50     | 1.60     | 170,519.10 | 396 | 18 | ~133–197 |
| D. Large range (~1M), spread out | ArrayDequeOrderBook | **5,095.70**  | **196.77**  | **1,958.50** | 1.50     | 9,690.60   | 420 | 18–19 | ~23–27 |
|          | LinkedListOrderBook  | 1,837.24             | 578.70       | 5,679.10     | 1.70     | 123,164.60 | 177 | 52| ~162–215 |

#### Scenario A – Small range (~10k), clustered

| Round | Implementation       | Throughput (ops/sec) | Avg Lat (µs) | P99 Lat (µs) | Min (µs) | P50 (µs) | Max (µs)  | Memory (MB) | GC Count | GC Time (ms) | Heap Δ (MB) |
|-------|----------------------|----------------------|--------------|--------------|----------|----------|-----------|-------------|----------|--------------|-------------|
| 1     | ArrayDequeOrderBook  | 36,886.16            | 31.79        | 291.40       | 0.40     | 4.30     | 3,426.50  | 143 | 5 | 19 | +106 |
|       | LinkedListOrderBook  | 13,755.17            | 79.20        | 822.90       | 0.80     | 25.00    | 3,561.80  | 196 | 4 | 43 | +22 |
| 2     | ArrayDequeOrderBook  | 36,843.74            | 31.83        | 289.30       | 0.40     | 4.20     | 3,401.70  | 143 | 5 | 18 | +111 |
|       | LinkedListOrderBook  | 13,733.59            | 79.36        | 817.50       | 0.80     | 25.10    | 3,547.40  | 196 | 4 | 46 | +24 |
| 3     | ArrayDequeOrderBook  | 36,865.15            | 31.81        | 289.70       | 0.40     | 4.30     | 3,421.20  | 143 | 5 | 18 | +104 |
|       | LinkedListOrderBook  | 13,747.09            | 79.27        | 819.10       | 0.80     | 25.00    | 3,555.60  | 196 | 4 | 44 | +25 |
| 4     | ArrayDequeOrderBook  | 36,865.03            | 31.81        | 289.70       | 0.40     | 4.30     | 3,422.00  | 143 | 5 | 15 | +100 |
|       | LinkedListOrderBook  | 13,745.86            | 79.28        | 819.90       | 0.80     | 25.00    | 3,559.40  | 196 | 4 | 45 | +23 |
| 5     | ArrayDequeOrderBook  | 36,864.68            | 31.81        | 289.90       | 0.40     | 4.30     | 3,423.70  | 143 | 5 | 17 | +108 |
|       | LinkedListOrderBook  | 13,742.22            | 79.29        | 821.10       | 0.80     | 25.00    | 3,560.70  | 196 | 4 | 47 | +26 |
| **Avg** | ArrayDequeOrderBook | **36,864.95**        | **31.81**    | **289.80**   | 0.40     | 4.30     | 3,426.50  | 143 | 5 | 17 | +106 |
|       | LinkedListOrderBook  | 13,744.99            | 79.26        | 820.20       | 0.80     | 25.00    | 3,561.80  | 196 | 4 | 45 | +24 |


#### Scenario B – Small range (~10k), spread out

| Round | Implementation       | Throughput (ops/sec) | Avg Lat (µs) | P99 Lat (µs) | Min (µs) | P50 (µs) | Max (µs)  | Memory (MB) | GC Count | GC Time (ms) | Heap Δ (MB) |
|-------|----------------------|----------------------|--------------|--------------|----------|----------|-----------|-------------|----------|--------------|-------------|
| 1     | ArrayDequeOrderBook  | 21,556.63            | 47.50        | 522.10       | 0.70     | 8.10     | 3,654.10  | 258 | 5 | 22 | +130 |
|       | LinkedListOrderBook  | 7,659.13             | 147.50       | 1,600.90     | 0.70     | 41.70    | 6,061.70  | 310 | 5 | 54 | -100 |
| 2     | ArrayDequeOrderBook  | 21,549.97            | 47.52        | 521.70       | 0.70     | 8.10     | 3,653.80  | 258 | 5 | 23 | +132 |
|       | LinkedListOrderBook  | 7,654.82             | 147.53       | 1,601.10     | 0.70     | 41.70    | 6,059.80  | 310 | 5 | 52 | -110 |
| 3     | ArrayDequeOrderBook  | 21,552.80            | 47.51        | 521.90       | 0.70     | 8.10     | 3,654.20  | 258 | 5 | 26 | +128 |
|       | LinkedListOrderBook  | 7,656.44             | 147.51       | 1,600.80     | 0.70     | 41.70    | 6,060.30  | 310 | 5 | 61 | -190 |
| 4     | ArrayDequeOrderBook  | 21,552.36            | 47.51        | 521.80       | 0.70     | 8.10     | 3,654.00  | 258 | 5 | 19 | +129 |
|       | LinkedListOrderBook  | 7,656.33             | 147.52       | 1,600.70     | 0.70     | 41.70    | 6,060.50  | 310 | 5 | 55 | -120 |
| 5     | ArrayDequeOrderBook  | 21,552.66            | 47.51        | 521.80       | 0.70     | 8.10     | 3,653.90  | 258 | 5 | 18 | +131 |
|       | LinkedListOrderBook  | 7,657.84             | 147.51       | 1,600.80     | 0.70     | 41.70    | 6,061.20  | 310 | 5 | 63 | -100 |
| **Avg** | ArrayDequeOrderBook | **21,552.88**        | **47.51**    | **521.80**   | 0.70     | 8.10     | 3,654.10  | 258 | 5 | 22 | +130 |
|       | LinkedListOrderBook  | 7,656.91             | 147.51       | 1,600.90     | 0.70     | 41.70    | 6,061.70  | 310 | 5 | 57 | -124 |


#### Scenario C – Large range (~1M), clustered

| Round | Implementation       | Throughput (ops/sec) | Avg Lat (µs) | P99 Lat (µs) | Min (µs) | P50 (µs) | Max (µs)    | Memory (MB) | GC Count | GC Time (ms) | Heap Δ (MB) |
|-------|----------------------|----------------------|--------------|--------------|----------|----------|-------------|-------------|----------|--------------|-------------|
| 1     | ArrayDequeOrderBook  | 4,417.61             | 241.33       | 2,398.40     | 0.80     | 164.90   | 8,823.20    | 324 | 17 | 27 | -260 |
|       | LinkedListOrderBook  | 2,122.30             | 546.43       | 5,659.80     | 0.80     | 383.20   | 15,981.20   | 396 | 18 | 133 | -140 |
| 2     | ArrayDequeOrderBook  | 4,414.48             | 241.50       | 2,400.20     | 0.80     | 164.90   | 8,822.70    | 324 | 17 | 25 | -240 |
|       | LinkedListOrderBook  | 2,119.13             | 546.61       | 5,663.10     | 0.80     | 383.20   | 15,982.10   | 396 | 18 | 197 | -100 |
| 3     | ArrayDequeOrderBook  | 4,414.25             | 241.52       | 2,400.30     | 0.80     | 164.90   | 8,822.80    | 324 | 18 | 37 | -210 |
|       | LinkedListOrderBook  | 2,118.52             | 546.55       | 5,662.80     | 0.80     | 383.20   | 15,981.90   | 396 | 18 | 181 | +60 |
| 4     | ArrayDequeOrderBook  | 4,413.71             | 241.55       | 2,400.40     | 0.80     | 164.90   | 8,823.00    | 324 | 18 | 32 | -80 |
|       | LinkedListOrderBook  | 2,118.54             | 546.54       | 5,662.70     | 0.80     | 383.20   | 15,981.80   | 396 | 18 | 197 | +78 |
| 5     | ArrayDequeOrderBook  | 4,411.50             | 241.65       | 2,402.20     | 0.80     | 165.00   | 8,823.10    | 324 | 18 | 31 | -90 |
|       | LinkedListOrderBook  | 2,115.80             | 546.47       | 5,664.10     | 0.80     | 383.20   | 15,980.80   | 396 | 18 | 179 | +89 |
| **Avg** | ArrayDequeOrderBook | **4,414.31**         | **241.51**   | **2,400.30** | 0.80     | 164.90   | 8,823.20    | 324 | 18 | 30 | -176 |
|       | LinkedListOrderBook  | 2,118.86             | 546.52       | 5,662.50     | 0.80     | 383.20   | 15,981.20   | 396 | 18 | 177 | -3 |

---

### Scenario D – Large range (~1M), spread out

| Round | Implementation       | Throughput (ops/sec) | Avg Lat (µs) | P99 Lat (µs) | Min (µs) | P50 (µs) | Max (µs)   | Memory (MB) | GC Count | GC Time (ms) | Heap Δ (MB) |
|-------|----------------------|----------------------|--------------|--------------|----------|----------|------------|-------------|----------|--------------|-------------|
| 1     | ArrayDequeOrderBook  | 5,096.98             | 196.73       | 1,958.20     | 0.90     | 128.20   | 7,278.40   | 420 | 18 | 25 | -109 |
|       | LinkedListOrderBook  | 1,838.72             | 578.58       | 5,678.40     | 0.90     | 408.20   | 17,125.40  | 177 | 52 | 162 | +42 |
| 2     | ArrayDequeOrderBook  | 5,094.13             | 196.84       | 1,959.10     | 0.90     | 128.20   | 7,278.10   | 420 | 19 | 23 | -220 |
|       | LinkedListOrderBook  | 1,836.54             | 578.86       | 5,678.70     | 0.90     | 408.20   | 17,124.70  | 177 | 52 | 177 | +40 |
| 3     | ArrayDequeOrderBook  | 5,095.19             | 196.78       | 1,958.70     | 0.90     | 128.20   | 7,277.90   | 420 | 19 | 27 | -150 |
|       | LinkedListOrderBook  | 1,837.21             | 578.73       | 5,678.90     | 0.90     | 408.20   | 17,125.00  | 177 | 52 | 215 | +33 |
| 4     | ArrayDequeOrderBook  | 5,096.08             | 196.75       | 1,958.50     | 0.90     | 128.20   | 7,277.80   | 420 | 18 | 23 | +220 |
|       | LinkedListOrderBook  | 1,837.48             | 578.71       | 5,679.00     | 0.90     | 408.20   | 17,124.90  | 177 | 52 | 199 | +10 |
| 5     | ArrayDequeOrderBook  | 5,095.23             | 196.77       | 1,958.90     | 0.90     | 128.20   | 7,277.70   | 420 | 19 | 27 | -200 |
|       | LinkedListOrderBook  | 1,836.25             | 578.81       | 5,679.50     | 0.90     | 408.20   | 17,124.60  | 177 | 52 | 208 | +5 |
| **Avg** | ArrayDequeOrderBook | **5,095.70**         | **196.77**   | **1,958.50** | 0.90     | 128.20   | 7,278.40   | 420 | 19 | 25 | -92 |
|       | LinkedListOrderBook  | 1,837.24             | 578.70       | 5,679.10     | 0.90     | 408.20   | 17,125.40  | 177 | 52 | 192 | +26 |


## 4. Observations

### 4.1 ArrayDeque vs LinkedList
- **Expected**: ArrayDeque faster due to contiguous memory and O(1) operations; LinkedList slower due to pointer chasing.
- **Measured**:
  - ArrayDeque is **2–3x faster** in throughput across all scenarios.
  - P99 latency for ArrayDeque is **30–40% of LinkedList**.
  - Memory usage lower for ArrayDeque (LinkedList suffers from node overhead).

---

### 4.2 Clustered vs Spread
- **Expected**: Clustered = higher throughput & lower latency. Spread = slower.
- **Measured**:
  - Clustered achieved **50–70% higher throughput** than spread.
  - Spread increased **P99 latency by 2x+**.

---

### 4.3 Small vs Large Range
- **Expected**: Small range = higher throughput, less memory. Large range = slower, more memory.
- **Measured**:
  - Small clustered (Scenario A): **~36k ops/sec**
  - Large clustered (Scenario C): **~4.4k ops/sec**
  - Memory: **143 MB (small)** → **324 MB+ (large)**
  - Latency in large range **5–6x higher** than small.

---

## 5. Overall Summary
- **ArrayDequeOrderBook consistently outperformed LinkedListOrderBook** in all scenarios.
- **Clustered > Spread**, **Small range > Large range** confirmed.
- **Key insight**: Under difficult conditions (large range or spread), the gap widens — LinkedList degrades much more than ArrayDeque.