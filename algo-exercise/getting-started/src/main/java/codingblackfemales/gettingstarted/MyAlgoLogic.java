package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.NoAction;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import codingblackfemales.util.Util;

import java.util.LinkedList;
import java.util.Queue;

//MyAlgoLogic will implement AlgoLogic interface like the samples have done
public class MyAlgoLogic implements AlgoLogic {
    // Creating a logger to keep track of the algorithm's decisions and actions
    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    // Using a queue to store the latest 5 bid prices for calculating the SMA

    //bidPricesOverTime assumes that each data point stored is the highest bid price for each day (total of 5 days)
    private Queue<Long> bidPricesOverTime = new LinkedList<>();
    private static final int maxPricesStored = 5;
    private double currentSMA = 0;

    //setting variables to track current trade positions
    private double entryPrice = 0;
    private double stopLossPrice = 0;
    private static final int maxOrderCount = 20;

    //The main method that evaluates the market state and decides the action to take
    @Override
    public Action evaluate(SimpleAlgoState state) {

        //Logging d current state of d order book
        final String book = Util.orderBookToString(state);
        logger.info("[MYALGO] Algo Sees Book as:\n" + book);

        // Combined check for max orders and bid levels
        if (state.getChildOrders().size() > maxOrderCount || state.getBidLevels() == 0) {
            return NoAction.NoAction;
        }

        // Get the highest (closing) bid price available (best price buyers are willing to pay)
        long bestBidPrice = state.getBidAt(0).getPrice();

        // Maintain queue size, removing oldest if necessary
        if (bidPricesOverTime.size() >= maxPricesStored) {
            bidPricesOverTime.remove(); // Removes the oldest price
        }
        // CHANGED: Now, we always add the latest price to the queue
        bidPricesOverTime.add(bestBidPrice); // Adds the latest price

        // Calculate SMA manually if algo has enough prices
        if (bidPricesOverTime.size() == maxPricesStored) {
            double sum = 0;
            for (long price : bidPricesOverTime) {
                sum += price;
            }
            currentSMA = sum / maxPricesStored;
            logger.info("[MYALGO] Calculated SMA: " + currentSMA);

            // CHANGED: Only proceed with decision-making when the SMA is calculated
            return decideTrade(bestBidPrice, state);
        } else {
            // CHANGED: Log when there aren't enough prices and return NoAction
            logger.info("[MYALGO] Not enough prices to calculate SMA. No action taken.");
            return NoAction.NoAction; // No decision can be made yet
        }
    }

    // Method for algo to decide whether to buy, sell, or take no action based on the currentSMA and market state
    private Action decideTrade(long bestBidPrice, SimpleAlgoState state) {

        //Algo checks if the current price is higher than the SMA and algo doesn't have an active position
        if (bestBidPrice >= currentSMA && entryPrice == 0) {
            entryPrice = bestBidPrice;
            stopLossPrice = entryPrice * 0.98;
            BidLevel topBid = state.getBidAt(0);

            logger.info("[MYALGO] Bullish signal detected. Placing buy order at " + bestBidPrice);
            logger.info("[MYALGO] Order book after buy order looks like this:\n" + Util.orderBookToString(state));

            return new CreateChildOrder(Side.BUY, topBid.getQuantity(), topBid.getPrice());
        }

        if (entryPrice > 0) {
            double profitTarget = entryPrice * 1.00033; //my profit target is 0.033% (btw 1 and 10% each month if successful)

            if (bestBidPrice >= profitTarget) {
                AskLevel topAsk = state.getAskAt(0);

                logger.info("[MYALGO] Profit target reached. Selling to take profit at " + bestBidPrice);
                logger.info("[MYALGO] Order book after sell order looks like this:\n" + Util.orderBookToString(state));

                entryPrice = 0;
                return new CreateChildOrder(Side.SELL, topAsk.getQuantity(), topAsk.getPrice());
            }

            if (bestBidPrice <= stopLossPrice) {
                logger.info("[MYALGO] Stop-loss triggered at " + stopLossPrice + ". Cancelling existing order and selling existing positions at market price.");

                entryPrice = 0;

                //cancels any existing buy order
                CancelChildOrder cancelOrder = new CancelChildOrder(state.getChildOrders().stream().findFirst().orElse(null));

                //sells any active position (any stock it might have already bought)
                if (state.getAskLevels() > 0) {
                    AskLevel topAsk = state.getAskAt(0);
                    return new CreateChildOrder(Side.SELL, topAsk.getQuantity(), topAsk.getPrice());
                } else {
                    logger.info("[MYALGO] No ask levels available, unable to execute sell order at this time.");
                    return cancelOrder;
                }
            }
        }

        return NoAction.NoAction;
    }
}