# OrderBook Benchmark Results

## 1. Introduction

As arraydeque get better performance in previous benchmark ( see part 1). In this part, we continue with 2 implementation

- **Implementations compared**
    - `ArrayDequeOrderBook` – uses **ArrayDeque** to store orders at each price level
    - `StaticArrayOrderBook` – uses **preallocated static arrays** for order storage

---

## 2. Expected Results

### ArrayDeque vs StaticArray

- **StaticArray**
    - Highest performance when the price range is small
    - O(1) access due to fixed index mapping
    - Very low latency and almost no GC overhead

- **ArrayDeque**
    - Easier to implement and flexible (dynamic growth)
    - Slower than StaticArray in tight benchmarks due to extra indirection and GC overhead
    - Still useful when price space is sparse or unbounded

### Clustered vs Spread

- **Clustered**: Higher throughput, lower latency (fewer active price levels, better cache locality)
- **Spread**: Lower throughput, higher latency (many distinct price levels, fragmented memory access)

### Small vs Large range

- **Small (~10k)**: Higher throughput, less memory usage
- **Large (~1M)**: Lower throughput, higher memory usage and more GC pressure

---

## 3. Summary Results

#### Average per scenario

| Scenario | Implementation       | Throughput (ops/sec) | Avg Lat (µs) | P99 Lat (µs) | P50 (µs) | Max (µs)   | Memory (MB) | GC Count (times/round) | GC Time (ms) |
|----------|----------------------|----------------------|--------------|--------------|----------|------------|-------------|------------------------|--------------|
| A. Small range (~10k), clustered | ArrayDequeOrderBook  | 48,804.11    | 21.01        | 188.20   | 0.40       | 4,338.50    | 165         | 4–5                    | ~13–20       |
|                                      | StaticArrayOrderBook | **2,171,249.63** | **0.46**    | **0.90**    | 0.80       | 359.90     | 113         | 0                      | 0            |
| B. Small range (~10k), spread out   | ArrayDequeOrderBook  | 23,439.86    | 44.10        | 466.40   | 0.90       | 11,859.90   | 202         | 8–9                    | ~29–34       |
|                                      | StaticArrayOrderBook | **10,891,370.69** | **0.09**    | **0.20**    | 0.10       | 58.60      | 222         | 0                      | 0            |
| C. Large range (~1M), clustered     | ArrayDequeOrderBook  | **5,122.44** | **196.39**   | **1,991.70** | 1.40–1.50  | 23,122.90  | 383         | 20–21                  | ~28–40       |
|                                      | StaticArrayOrderBook | 1,582.24     | 632.38       | 3,220.40 | 92.60–96.20 | 157,416.80 | 357         | ~259–260               | ~264–277     |
| D. Large range (~1M), spread out    | ArrayDequeOrderBook  | **4,745.27** | **212.60**   | **2,031.30** | 1.40       | 21,669.10  | 114         | 27–46                  | ~30–55       |
|                                      | StaticArrayOrderBook | 1,210.07     | 828.59       | 3,747.00 | 2.30–3.40  | 122,034.70 | 139         | ~356–357               | ~321–378     |


#### Scenario A – Small range (~10k), clustered

| Round | Implementation        | Throughput (ops/sec) | Avg Lat (µs) | P99 Lat (µs) | Min (µs) | P50 (µs) | Max (µs)  | Memory (MB) | GC Count | GC Time (ms) | Heap Δ (MB) |
|-------|-----------------------|----------------------|--------------|--------------|----------|----------|-----------|-------------|----------|--------------|-------------|
| 1     | ArrayDequeOrderBook   | 61,342.04            | 16.30        | 143.20       | 0.00     | 0.40     | 4,338.50  | 165 | 4 | 15 | +78.40 |
|       | StaticArrayOrderBook  | 2,161,531.23         | 0.46         | 1.20         | 0.00     | 0.80     | 57.70     | 113 | 0 | 0  | +6.00 |
| 2     | ArrayDequeOrderBook   | 53,488.80            | 18.70        | 165.30       | 0.00     | 0.40     | 4,694.60  | 165 | 5 | 20 | -69.98 |
|       | StaticArrayOrderBook  | 2,200,031.24         | 0.45         | 0.90         | 0.00     | 0.80     | 48.50     | 113 | 0 | 0  | +7.00 |
| 3     | ArrayDequeOrderBook   | 47,047.02            | 21.26        | 188.20       | 0.00     | 0.40     | 4,200.60  | 165 | 4 | 13 | +75.53 |
|       | StaticArrayOrderBook  | 2,225,575.42         | 0.45         | 0.90         | 0.00     | 0.80     | 45.30     | 113 | 0 | 0  | +7.00 |
| 4     | ArrayDequeOrderBook   | 43,142.35            | 23.18        | 203.90       | 0.00     | 0.40     | 4,067.40  | 165 | 5 | 17 | -74.11 |
|       | StaticArrayOrderBook  | 2,049,654.94         | 0.49         | 1.80         | 0.00     | 0.80     | 359.90    | 113 | 0 | 0  | +7.00 |
| 5     | ArrayDequeOrderBook   | 39,000.33            | 25.64        | 225.60       | 0.00     | 0.50     | 4,202.30  | 165 | 4 | 14 | +77.51 |
|       | StaticArrayOrderBook  | 2,219,455.30         | 0.45         | 0.90         | 0.00     | 0.80     | 20.70     | 113 | 0 | 0  | +6.00 |
| **Avg** | ArrayDequeOrderBook | **48,804.11**        | **21.01**    | **188.20**   | 0.00     | 0.40     | 4,338.50  | 165 | 4 | 16 | ±17.07 |
|       | StaticArrayOrderBook  | **2,171,249.63**     | **0.46**     | **0.90**     | 0.00     | 0.80     | 359.90    | 113 | 0 | 0  | +6.60 |


#### Scenario B – Small range (~10k), spread out

| Round | Implementation        | Throughput (ops/sec) | Avg Lat (µs) | P99 Lat (µs) | Min (µs) | P50 (µs) | Max (µs)   | Memory (MB) | GC Count | GC Time (ms) | Heap Δ (MB) |
|-------|-----------------------|----------------------|--------------|--------------|----------|----------|------------|-------------|----------|--------------|-------------|
| 1     | ArrayDequeOrderBook   | 30,750.99            | 32.52        | 348.60       | 0.00     | 0.90     | 5,840.90   | 202 | 9 | 34 | -58.78 |
|       | StaticArrayOrderBook  | 9,954,606.99         | 0.10         | 0.20         | 0.00     | 0.10     | 17.00      | 222 | 0 | 0  | +18.00 |
| 2     | ArrayDequeOrderBook   | 25,428.64            | 39.33        | 419.90       | 0.00     | 1.00     | 4,259.20   | 202 | 9 | 29 | -62.51 |
|       | StaticArrayOrderBook  | 11,214,408.27        | 0.09         | 0.20         | 0.00     | 0.10     | 13.70      | 222 | 0 | 0  | +19.00 |
| 3     | ArrayDequeOrderBook   | 22,570.08            | 44.31        | 466.40       | 0.00     | 1.00     | 4,665.00   | 202 | 8 | 30 | +91.87 |
|       | StaticArrayOrderBook  | 11,096,685.42        | 0.09         | 0.20         | 0.00     | 0.10     | 15.50      | 222 | 0 | 0  | +18.00 |
| 4     | ArrayDequeOrderBook   | 20,285.38            | 49.30        | 514.00       | 0.00     | 1.00     | 4,902.20   | 202 | 9 | 33 | -63.89 |
|       | StaticArrayOrderBook  | 10,975,864.07        | 0.09         | 0.20         | 0.00     | 0.10     | 58.60      | 222 | 0 | 0  | +18.00 |
| 5     | ArrayDequeOrderBook   | 18,164.18            | 55.05        | 572.80       | 0.00     | 1.00     | 11,859.90  | 202 | 8 | 30 | +91.50 |
|       | StaticArrayOrderBook  | 11,215,288.68        | 0.09         | 0.20         | 0.00     | 0.10     | 11.20      | 222 | 0 | 0  | +18.00 |
| **Avg** | ArrayDequeOrderBook | **23,439.86**        | **44.10**    | **466.40**   | 0.00     | 1.00     | 11,859.90  | 202 | 9 | 31 | +11.04 |
|       | StaticArrayOrderBook  | **10,891,370.69**    | **0.09**     | **0.20**     | 0.00     | 0.10     | 58.60      | 222 | 0 | 0  | +18.20 |



#### Scenario C – Large range (~1M), clustered

| Round | Implementation        | Throughput (ops/sec) | Avg Lat (µs) | P99 Lat (µs) | Min (µs) | P50 (µs) | Max (µs)    | Memory (MB) | GC Count | GC Time (ms) | Heap Δ (MB) |
|-------|-----------------------|----------------------|--------------|--------------|----------|----------|-------------|-------------|----------|--------------|-------------|
| 1     | ArrayDequeOrderBook   | 5,912.63             | 169.13       | 1650.40      | 0.00     | 1.40     | 11,957.90   | 383 | 20 | 28 | +212.10 |
|       | StaticArrayOrderBook  | 1,581.69             | 632.24       | 3186.10      | 0.00     | 95.60    | 14,090.40   | 357 | 259 | 277 | +80.00 |
| 2     | ArrayDequeOrderBook   | 5,128.12             | 195.00       | 2030.00      | 0.10     | 1.50     | 20,797.10   | 383 | 21 | 40 | -66.57 |
|       | StaticArrayOrderBook  | 1,524.43             | 655.98       | 3527.30      | 0.00     | 96.20    | 157,416.80  | 357 | 259 | 274 | +80.00 |
| 3     | ArrayDequeOrderBook   | 4,964.30             | 201.44       | 1991.70      | 0.00     | 1.50     | 12,841.90   | 383 | 21 | 40 | -67.58 |
|       | StaticArrayOrderBook  | 1,577.90             | 633.75       | 3238.40      | 0.00     | 95.70    | 21,098.20   | 357 | 260 | 271 | -186.00 |
| 4     | ArrayDequeOrderBook   | 4,772.76             | 209.52       | 2078.40      | 0.10     | 1.50     | 23,122.90   | 383 | 21 | 39 | -67.95 |
|       | StaticArrayOrderBook  | 1,582.10             | 632.07       | 3220.40      | 0.00     | 92.60    | 17,149.10   | 357 | 259 | 272 | +80.00 |
| 5     | ArrayDequeOrderBook   | 4,834.38             | 206.85       | 1979.90      | 0.10     | 1.40     | 6,764.50    | 383 | 20 | 31 | +212.02 |
|       | StaticArrayOrderBook  | 1,645.06             | 607.88       | 2879.40      | 0.00     | 95.50    | 18,393.50   | 357 | 259 | 264 | +80.00 |
| **Avg** | ArrayDequeOrderBook | **5,122.44**         | **196.39**   | **1991.70**  | 0.00     | 1.50     | 23,122.90   | 383 | 21 | 36 | +44.80 |
|       | StaticArrayOrderBook  | **1,582.24**         | **632.38**   | **3220.40**  | 0.00     | 95.72    | 157,416.80  | 357 | 259 | 272 | +26.80 |



#### Scenario D – Large range (~1M), spread out

| Round | Implementation        | Throughput (ops/sec) | Avg Lat (µs) | P99 Lat (µs) | Min (µs) | P50 (µs) | Max (µs)    | Memory (MB) | GC Count | GC Time (ms) | Heap Δ (MB) |
|-------|-----------------------|----------------------|--------------|--------------|----------|----------|-------------|-------------|----------|--------------|-------------|
| 1     | ArrayDequeOrderBook   | 5,404.63             | 185.03       | 1765.50      | 0.00     | 1.40     | 10,010.70   | 114 | 27 | 30 | -95.99 |
|       | StaticArrayOrderBook  | 1,129.50             | 885.35       | 4609.60      | 0.00     | 2.90     | 25,141.80   | 139 | 357 | 378 | -174.00 |
| 2     | ArrayDequeOrderBook   | 5,061.24             | 197.58       | 1887.30      | 0.10     | 1.40     | 13,886.10   | 114 | 27 | 32 | +96.11 |
|       | StaticArrayOrderBook  | 1,141.78             | 875.82       | 4484.50      | 0.00     | 3.40     | 8,873.10    | 139 | 356 | 369 | +59.00 |
| 3     | ArrayDequeOrderBook   | 4,696.30             | 212.93       | 2031.30      | 0.00     | 1.40     | 17,682.50   | 114 | 31 | 34 | -225.92 |
|       | StaticArrayOrderBook  | 1,262.28             | 792.22       | 3553.30      | 0.00     | 2.30     | 18,586.70   | 139 | 356 | 323 | +60.00 |
| 4     | ArrayDequeOrderBook   | 4,405.11             | 227.01       | 2196.60      | 0.10     | 1.40     | 16,161.60   | 114 | 45 | 48 | +66.06 |
|       | StaticArrayOrderBook  | 1,243.91             | 803.91       | 3747.00      | 0.00     | 2.40     | 122,034.70  | 139 | 356 | 321 | +59.01 |
| 5     | ArrayDequeOrderBook   | 4,159.08             | 240.44       | 2309.20      | 0.00     | 1.40     | 21,669.10   | 114 | 46 | 55 | -69.00 |
|       | StaticArrayOrderBook  | 1,272.86             | 785.63       | 3493.00      | 0.00     | 2.40     | 12,112.80   | 139 | 357 | 323 | -175.00 |
| **Avg** | ArrayDequeOrderBook | **4,745.27**         | **212.60**   | **2031.30**  | 0.00     | 1.40     | 21,669.10   | 114 | 35 | 40 | -45.75 |
|       | StaticArrayOrderBook  | **1,210.07**         | **828.59**   | **3747.00**  | 0.00     | 2.68     | 122,034.70  | 139 | 356 | 343 | -42.80 |


## 4. Observations

### Scenario A – Small range (~10k), clustered
- **ArrayDeque**: ~48K ops/sec, P99 latency ~188 µs
- **StaticArray**: ~2.1M ops/sec, P99 latency ~0.9 µs
- StaticArray is ~44× faster, ultra-low latency, no GC overhead.
- ArrayDeque slowed down due to GC and object management.

### Scenario B – Small range (~10k), spread out
- **ArrayDeque**: ~23K ops/sec, P99 latency ~466 µs, memory ~202 MB
- **StaticArray**: ~10.9M ops/sec, P99 latency ~0.2 µs, memory ~222 MB
- StaticArray remains extremely fast, handles random access efficiently.
- ArrayDeque performance degrades significantly.

### Scenario C – Large range (~1M), clustered
- **ArrayDeque**: ~5.1K ops/sec, P99 latency ~1991 µs, memory ~383 MB
- **StaticArray**: ~1.5K ops/sec, P99 latency ~3220 µs, memory ~357 MB, 259+ GC cycles
- ArrayDeque clearly outperforms StaticArray in this setup.
- StaticArray wastes memory and suffers from heavy GC pressure.

### Scenario D – Large range (~1M), spread out
- **ArrayDeque**: ~4.7K ops/sec, P99 latency ~2031 µs, memory ~114 MB
- **StaticArray**: ~1.2K ops/sec, P99 latency ~3747 µs, memory ~139 MB, hundreds of GCs
- ArrayDeque is much more practical here.
- StaticArray suffers from catastrophic inefficiency.

---

## 5. Overall Summary

- **StaticArrayOrderBook**
    - Excellent for **small price ranges** (clustered or spread)
    - Achieves **millions of ops/sec** with **sub-microsecond latency**
    - Not suitable for **large ranges** → memory explosion + GC churn

- **ArrayDequeOrderBook**
    - Much slower for small ranges
    - Scales better for large ranges
    - More memory efficient, fewer catastrophic GC issues

### Trade-off
- **StaticArray** = raw speed, best for constrained tick ranges (e.g. crypto futures).
- **ArrayDeque** = safer choice for production with wide price ranges (e.g. equities, FX).