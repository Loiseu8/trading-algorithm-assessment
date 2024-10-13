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

// MyAlgoLogic will implement AlgoLogic interface like the samples have done
public class MyAlgoLogic implements AlgoLogic {
    // Creating a logger to keep track of the algorithm's decisions and actions
    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    // Using a queue to store the latest 5 bid prices for calculating the SMA
    private Queue<Long> bidPricesOverTime = new LinkedList<>();
    private static final int maxPricesStored = 5;
    private double currentSMA = 0;

    // Setting variables to track current trade positions
   private double entryPrice = 0;
   private double stopLossPrice = 0;
    private static final int maxOrderCount = 20;

    // The main method that evaluates the market state and decides the action to take
    @Override
    public Action evaluate(SimpleAlgoState state) {

        // Logging the current state of the order book
        final String book = Util.orderBookToString(state);
        logger.info("[MYALGO] Algo Sees Book as:\n" + book);

        // Combined check for max orders and bid levels
        if (state.getChildOrders().size() > maxOrderCount || state.getBidLevels() == 0) {
            return NoAction.NoAction;
        }

        // Get the highest (closing) bid price available (best price buyers are willing to pay)
        long bestBidPrice = state.getBidAt(0).getPrice();

        // Maintain queue size, removing oldest as needed
        if (bidPricesOverTime.size() >= maxPricesStored) {
            bidPricesOverTime.remove(); // Removes the oldest price
        }

        // Now, always add the latest price to the queue
        bidPricesOverTime.add(bestBidPrice); // Adds the latest price

        // Calculate SMA manually if algo has enough prices
        if (bidPricesOverTime.size() == maxPricesStored) {
            double sum = 0;
            for (long price : bidPricesOverTime) {
                sum += price;
            }
            currentSMA = sum / maxPricesStored;
            logger.info("[MYALGO] Calculated SMA: " + currentSMA);

        } else {
            logger.info("[MYALGO] Not enough prices to calculate SMA. No action taken.");
            return NoAction.NoAction;
        }

        return NoAction.NoAction;  // Default return for evaluate
    }

    // Method for algo to decide whether to buy based on the current SMA and market state
        public Action createBuyOrder ( long bestBidPrice, SimpleAlgoState state){
            if (bestBidPrice >= currentSMA) {
                BidLevel level = state.getBidAt(0);

                //it then collects the price and qty at that level:
             final long entryPrice = level.price;
                final long quantity = level.quantity;

              //  stopLossPrice = entryPrice * 0.98;
                //BidLevel topBid = state.getBidAt(0);

                logger.info("[MYALGO] Bullish signal detected. Placing buy order for " + quantity + " @ " + bestBidPrice);
                logger.info("[MYALGO] Order book after buy order looks like this:\n" + Util.orderBookToString(state));

                return new CreateChildOrder(Side.BUY, quantity, entryPrice);

            } else {
                return NoAction.NoAction;
            }
        }

    // Method for selling based on profit target
    private Action createSellOrder(long bestBidPrice, SimpleAlgoState state) {
        BidLevel level = state.getBidAt(0);

        //it then collects the price and qty at that level:
         long entryPrice = level.price;

        double profitTarget = entryPrice * 1.00033; // Profit target is 0.033%

        if (entryPrice > 0 && bestBidPrice >= profitTarget) {
            AskLevel topAsk = state.getAskAt(0);

            logger.info("[MYALGO] Profit target reached. Selling to take profit at " + bestBidPrice);
            logger.info("[MYALGO] Order book after sell order looks like this:\n" + Util.orderBookToString(state));

            entryPrice = 0; // Reset entry price after selling
            return new CreateChildOrder(Side.SELL, topAsk.getQuantity(), topAsk.getPrice());
        } else {
            return NoAction.NoAction;
        }
    }


    // Method to trigger stop-loss and cancel active orders if necessary
    private Action cancelForStopLoss(long bestBidPrice, SimpleAlgoState state) {
        double stopLossPrice;
        BidLevel level = state.getBidAt(0);

        //it then collects the price and qty at that level:
        final long entryPrice = level.price;
        stopLossPrice = entryPrice * 0.98;

        if (entryPrice > 0 && bestBidPrice <= stopLossPrice) {


            logger.info("[MYALGO] Stop-loss triggered at " + stopLossPrice + ". Cancelling any existing buy order.");

            var activeOrders = state.getActiveChildOrders();
            if (!activeOrders.isEmpty()) {
                var childOrder = activeOrders.stream().findFirst();
                if (childOrder.isPresent()) {
                    return new CancelChildOrder(childOrder.get());
                }
            }
        }
        return NoAction.NoAction;
    }
}
