package com.example.demo.engine;

import com.example.demo.core.*;

import javax.naming.OperationNotSupportedException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class MatchingEngine {

    private final OrderBook orderBook;

    public MatchingEngine(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    public void handleNewOrder(PrimitiveOrder newOrder) {
        orderBook.addOrder(newOrder);
        processOrder(newOrder);
    }

    private void processOrder(PrimitiveOrder aggressorOrder) {


        if (aggressorOrder.side == PrimitiveOrder.SIDE_BUY) {
            processBuyOrder(aggressorOrder);
        } else {
            processSellOrder(aggressorOrder);
        }

        if ( aggressorOrder.status == PrimitiveOrder.STATUS_PARTIALLY_FILLED || aggressorOrder.status == PrimitiveOrder.STATUS_OPEN) {
            orderBook.addOrder(aggressorOrder);
        }
    }

    private void processBuyOrder(PrimitiveOrder aggressorOrder) {
        Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> askIter = orderBook.getAskLevelsIterator();

        while (aggressorOrder.remainingQty > 0 && askIter.hasNext()) {
            Map.Entry<Long, ? extends Collection<PrimitiveOrder>> askLevel = askIter.next();
            long askPrice = askLevel.getKey();
            Collection<PrimitiveOrder> restingAsks = askLevel.getValue();

            if (aggressorOrder.price >= askPrice) {
                processLevel(aggressorOrder, restingAsks, PrimitiveOrder.SIDE_BUY);

                if ( restingAsks.isEmpty()) {
                    askIter.remove();
                }
            } else {
                break;
            }
        }
    }

    private void processSellOrder(PrimitiveOrder aggressorOrder) {
        Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> bidIter = orderBook.getBidLevelsIterator();

        while (aggressorOrder.remainingQty > 0 && bidIter.hasNext()) {
            Map.Entry<Long, ? extends Collection<PrimitiveOrder>> bidLevel = bidIter.next();
            long bidPrice = bidLevel.getKey();
            Collection<PrimitiveOrder> restingBids = bidLevel.getValue();

            if (aggressorOrder.price <= bidPrice) {
                processLevel(aggressorOrder, restingBids, PrimitiveOrder.SIDE_SELL);

                if (restingBids.isEmpty()) {
                    bidIter.remove();
                }
            } else {
                break;
            }
        }
    }

    private void processLevel(PrimitiveOrder aggressorOrder, Collection<PrimitiveOrder> restingOrders, byte aggressorSide) {
        Iterator<PrimitiveOrder> restingIter = restingOrders.iterator();

        while (aggressorOrder.remainingQty > 0 && restingIter.hasNext()) {
            PrimitiveOrder restingOrder = restingIter.next();

            if (restingOrder == null) {
                restingIter.remove();
                continue;
            }

            long tradeQty = Math.min(aggressorOrder.remainingQty, restingOrder.remainingQty);

            if (tradeQty > 0) {
                aggressorOrder.fill(tradeQty);
                restingOrder.fill(tradeQty);
                if (restingOrder.status == PrimitiveOrder.STATUS_FILLED) {
                    restingIter.remove();
                    orderBook.removeOrderFromLookUpMap(restingOrder.orderId);
                }
                if (aggressorOrder.status == PrimitiveOrder.STATUS_FILLED) {
                    break;
                }
            }
        }
    }
}
