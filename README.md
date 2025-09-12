# Java Order Book Implementations

This repository explores multiple **single-threaded, in-memory Order Book** implementations in Java.  
The main goal is to **compare performance and trade-offs** between different data structures and to demonstrate a simple **Matching Engine workflow**.

---

## Core Components

### 1. Order Book

An **Order Book** is a real-time ledger of all outstanding buy and sell orders for a financial instrument.

- **Bid Side (Buy orders):**  
  Sorted **descending by price** → highest price = *best bid*.

- **Ask Side (Sell orders):**  
  Sorted **ascending by price** → lowest price = *best ask*.

#### Data Structures

- **Price Levels** → `NavigableMap<Long, List<PrimitiveOrder>>`  
  - Key: Price level  
  - Value: FIFO list of orders at that price  

- **Order Lookup** → `Map<Long, PrimitiveOrder>`  
  - Key: `orderId`  
  - Value: Order reference (for O(1) cancellation & modification)

---

### 2. Matching Logic

The **Matching Logic** determines whether a new order can be executed against resting orders.

#### Price-Time Priority

1. **Price Priority:**  
   - Higher-priced buys match first  
   - Lower-priced sells match first  

2. **Time Priority:**  
   - If same price → earliest order gets matched first  

#### Workflow

1. **Receive New Order** (aggressor)  
2. **Scan for Matches**  
   - Buy → check ask side (lowest price first)  
   - Sell → check bid side (highest price first)  
3. **Execute Trade** if prices cross  
   - Trade size = `min(buyQty, sellQty)`  
4. **Update Orders**  
   - Reduce remaining quantity  
   - Remove fully filled orders  

---

## Scenarios (Benchmark Cases)

The benchmark covers **4 scenarios**, each representing a different distribution of orders across price ranges:

- **Scenario A – Small range, clustered**
    - Price range: narrow (~10,000 price levels)
    - Orders concentrated around the mid-price
    - Goal: simulate a liquid market where most trades happen around a central price

- **Scenario B – Small range, spread out**
    - Price range: narrow (~10,000)
    - Orders uniformly spread
    - Goal: simulate a liquid market with distributed liquidity

- **Scenario C – Large range, clustered**
    - Price range: wide (~1M price levels)
    - Orders concentrated around the mid-price
    - Goal: simulate a wide market range but with trading activity focused near the center

- **Scenario D – Large range, spread out**
    - Price range: wide (~1M)
    - Orders spread across the entire range
    - Goal: simulate a market with dispersed orders and thin liquidity

---

## Methodology

- **Pre-generated orders**
    - Clustered: Gaussian distribution around mid-price
    - Spread: Uniform random distribution across the range

- **Warm-up rounds**
    - 3 iterations to let JVM JIT optimize

- **Measurement rounds**
    - 5 iterations per scenario

- **Metrics collected**
    - Throughput (ops/sec)
    - Avg latency (µs/op)
    - Min, P50, P99, P99.9, Max latency
    - GC count/time and heap delta
