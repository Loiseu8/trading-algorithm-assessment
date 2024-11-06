package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    //Queue to store 5 recent bid prices on the market:
    private final Queue<Long> bidPricesOverTime = new LinkedList<>();
    private static final int maxPricesStored = 5;  // max number of prices needed to calculate SMA
    private double SMA = 0; //Simple Moving Average of the most recent best bid prices.

    private long fixedEntryPrice = 0;  // fixing entry price to calculate stop-loss and profit.
    private double totalProfit = 0.0;  // running total to track cumulative profit over trades.

    @Override
    public Action evaluate(SimpleAlgoState state) {

        // Retrieve and log the current state of the order book first
        var orderBookAsString = Util.orderBookToString(state);
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        //Getting the highest bid price and best ask price from the order book
        final BidLevel highestBidPrice = state.getBidAt(0);
        final AskLevel bestAskPrice = state.getAskAt(0);
        long quantity = 75; // Fixed quantity that algo places for buy and sell orders
        long bestBidPrice = highestBidPrice.price;  // Current best bid that algo uses to decide trades

        // formulas for the stop-loss and profit target
        double stopLossPrice = fixedEntryPrice * 0.98; // Stop loss must kick in at 2% below entry
        double profitTarget = fixedEntryPrice * 1.00033; // Profit target is fixed at 0.033% profit target
        double profit = 0.0; // Temporary variable to store profit for the trade made

        logger.info("[MYALGO] In Algo Logic....");

        // First, update the bid prices queue with latest best bids to trigger SMA calculation
        if (bidPricesOverTime.size() >= maxPricesStored) {
            bidPricesOverTime.remove();  // Remove the oldest price
        }
        bidPricesOverTime.add(bestBidPrice); // Add the new price

        // Calculate the SMA if enough data points is available
        if (bidPricesOverTime.size() == maxPricesStored) {
            long startSMATime = System.nanoTime();

            double sum = 0;
            for (long price : bidPricesOverTime) {
                sum += price;
            }
            SMA = sum / maxPricesStored;
            logger.info("[MYALGO] Calculated SMA: " + SMA);
            long stopSMATime = (System.nanoTime() - startSMATime) / 1000000;
            logger.info("[PERFORMANCE] Time Algo Took to Calculate SMA: " + stopSMATime + " ms");

        } else {
            logger.info("[MYALGO] Not enough prices to calculate SMA.");
            return NoAction.NoAction;
        }

        // Check for active child orders
        var totalOrderCount = state.getChildOrders().size();
        final var activeOrders = state.getActiveChildOrders();
        logger.info("[MYALGO] Active orders count: " + activeOrders.size());

        // Exit condition: algo stops if total order count exceeds 20 or no bid levels (edge case)
        if (totalOrderCount >= 20 || state.getBidLevels() == 0) {
            logger.info("[MYALGO] Exit condition met. No further actions.");
            return NoAction.NoAction;
        }

        // Buy logic: place buy order if best bid is equal to or above SMA and there are less than 3 active orders
        if (bestBidPrice >= SMA && activeOrders.size() < 3) {
            long startBuyTime = System.nanoTime();

            logger.info("[MYALGO] Placing buy order for " + quantity + " @ " + bestBidPrice + " (Price >= SMA: " + SMA + ")");

            // Set fixed entry price if this is the first buy
            if (fixedEntryPrice == 0) {
                fixedEntryPrice = bestBidPrice;
            }

            long stopBuyTime = (System.nanoTime() - startBuyTime) / 1000000;
            logger.info("[PERFORMANCE] Time Algo Took to Create Buy Order: " + stopBuyTime + " ms");
            return new CreateChildOrder(Side.BUY, quantity, bestBidPrice);
        }

        // Sell logic: place sell order at best bid price if the profit target is reached
        if (!activeOrders.isEmpty() && bestBidPrice >= profitTarget) {
            long startSellTime = System.nanoTime();

            // Calculating the profit for the current trade and add it to totalProfit
            profit = (bestBidPrice - fixedEntryPrice) * quantity;
            totalProfit += profit;

            logger.info("[MYALGO] Selling to take profit at " + bestBidPrice);
            logger.info("[MYALGO] Profit from this trade: " + profit + ". Total profit so far: " + totalProfit);

            long stopSellTime = (System.nanoTime() - startSellTime) / 1000000;
            logger.info("[PERFORMANCE] Time Algo Took to Execute a Sell For Profit: " + stopSellTime + " ms");
            return new CreateChildOrder(Side.SELL, quantity, bestBidPrice);
        }

        // Stop-loss logic: if best bid falls below stop-loss price, cancel the first active order.
        // algo repeats this cancellation as long as this condition holds and there are active orders
        if (!activeOrders.isEmpty()) {
            long startStopLossTime = System.nanoTime();
            var firstOrder = activeOrders.stream().findFirst().orElse(null);
            if (firstOrder != null && bestBidPrice <= stopLossPrice) {
                logger.info("[MYALGO] Stop-loss triggered at " + stopLossPrice + ". Cancelling order: " + firstOrder);

                logger.info("[MYALGO] Profit from this trade: " + profit + ". Total profit made from this round of trades: " + totalProfit);

                long stopLossExecutionTime = (System.nanoTime() - startStopLossTime) / 1000000;
                logger.info("[PERFORMANCE] Stop-Loss Execution Time: " + stopLossExecutionTime + " ms");
                return new CancelChildOrder(firstOrder);
            }
        }

        return NoAction.NoAction;
    }

}


