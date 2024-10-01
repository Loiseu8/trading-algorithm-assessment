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

    // Using a queue to store the latest 5 bid prices for calculating the EMA
    private Queue<Long> bidPricesOverTime = new LinkedList<>();
    private int maxPricesStored = 5; // The maximum number of prices we want to track
    private double currentEMA = 0; // This will store our calculated EMA, starting at 0 initially

    // Variables to track current trade positions
    private double entryPrice = 0; // The price at which we bought a stock (starts at 0 since no stock is bought yet)
    private double stopLossPrice = 0; // The price at which we'll sell to minimize losses
    private final int maxOrderCount = 20; // Limit to how many orders we can have at once

    //The main method that evaluates the market state and decides the action to take
    @Override
    public Action evaluate(SimpleAlgoState state) {

        //Logging d current state of d order book
        final String book = Util.orderBookToString(state);
        logger.info("[MYALGO] Algo Sees Book as:\n" + book);

        // Check if we have too many active orders; if so, take no action
        if (state.getChildOrders().size() > maxOrderCount) return NoAction.NoAction;

        // Check if there are any bid prices available in the market
        if (state.getBidLevels() > 0) {
            // Get the highest bid price available (best price buyers are willing to pay)
            long bestBidPrice = state.getBidAt(0).getPrice();

            // If queue has reached the max size, it removes the oldest bid price to keep it up to date
            if (bidPricesOverTime.size() >= maxPricesStored) bidPricesOverTime.remove();
            //Then, add the latest bid price to our queue
            bidPricesOverTime.add(bestBidPrice);

            // Check if there are enough bid prices to calculate initial EMA
            if (bidPricesOverTime.size() == maxPricesStored && currentEMA == 0) {
                currentEMA = calculatedInitialEMA(); // Calculate the initial average and set it as currentEMA
                logger.info("[MYALGO] Initial EMA has been calculated: " + currentEMA);
            } else {
                // If the initial EMA is already set, update it using the new bid price
                currentEMA = calculateEMA(currentEMA, bestBidPrice);
                logger.info("[MYALGO] Calculated EMA: " + currentEMA);
            }

            // Decide whether to buy, sell, or take no action based on the currentEMA
            return makeTradingDecision(bestBidPrice, state);
        }

        // If there are no bid prices, no action is needed
        return NoAction.NoAction;
    }

    // Method to calculate the initial EMA using the first 5 bid prices
    private double calculatedInitialEMA() {
        double sum = 0;
        // Add up all the bid prices in the queue
        for (long price : bidPricesOverTime) {
            sum += price;
        }
        // Divide by the number of prices to get the average (initial EMA)
        return sum / maxPricesStored;
    }

    // Method to calculate the ongoing EMA based on the current price and previous EMA
    private double calculateEMA(double previousEMA, double currentPrice) {
        // Using a smoothing factor to weigh recent prices more heavily
        double smoothingFactor = 2.0 / (maxPricesStored + 1);
        // Update the EMA using the standard formula
        return (currentPrice - previousEMA) * smoothingFactor + previousEMA;
    }

    // Method to decide whether to buy, sell, or take no action based on the currentEMA and market state
    private Action makeTradingDecision(long bestBidPrice, SimpleAlgoState state) {
        // Check if the current price is higher than our EMA and we don't have an active position
        if (bestBidPrice > currentEMA && entryPrice == 0) {
            // Buy at the current price and set our entry price
            entryPrice = bestBidPrice;
            stopLossPrice = entryPrice * 0.98; // Set a stop-loss at 2% below the entry price to manage risk
            BidLevel topBid = state.getBidAt(0); // Get the details of the top bid

            logger.info("[MYALGO] Bullish signal detected. Placing buy order at " + bestBidPrice);
            logger.info("[MYALGO] Order book after buy order looks like this:\n" + Util.orderBookToString(state));

            // Create and place a buy order
            return new CreateChildOrder(Side.BUY, topBid.getQuantity(), topBid.getPrice());
        }

        // If we already have a position (entryPrice > 0), check if we need to sell
        if (entryPrice > 0) {
            double profitTarget = entryPrice * 1.00033; // Setting a profit target 0.033% above our entry price

            // If the current price has reached or exceeded our profit target, sell
            if (bestBidPrice >= profitTarget) {
                AskLevel topAsk = state.getAskAt(0); // Get the details of the top ask price

                logger.info("[MYALGO] Profit target reached. Selling to take profit at " + bestBidPrice);
                logger.info("[MYALGO] Order book after sell order looks like this:\n" + Util.orderBookToString(state));

                // Reset entryPrice to indicate we're no longer holding a position
                entryPrice = 0;
                return new CreateChildOrder(Side.SELL, topAsk.getQuantity(), topAsk.getPrice());
            }

            // If the price has fallen to or below the stop-loss, algo cancels the order to prevent further loss
            if (bestBidPrice <= stopLossPrice) {
                logger.info("[MYALGO] Stop-loss triggered at " + stopLossPrice + ". Cancelling existing order and selling existing positions at market price.");

                // Reset entry price to indicate the algo is no longer holding a position
                entryPrice = 0;

                // Cancel the existing child order
                CancelChildOrder cancelOrder = new CancelChildOrder(state.getChildOrders().stream().findFirst().orElse(null));

                // Check if there is an ask level available to execute a sell order
                if (state.getAskLevels() > 0) {
                    AskLevel topAsk = state.getAskAt(0);
                    return new CreateChildOrder(Side.SELL, topAsk.getQuantity(), topAsk.getPrice());
                } else {
                    logger.info("[MYALGO] No ask levels available, unable to execute sell order at this time.");
                    return cancelOrder;
                }
            }

        }

        // If no buy, sell, or stop-loss condition is met, take no action
        return NoAction.NoAction;
    }
}