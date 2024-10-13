package codingblackfemales.algo;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

import static codingblackfemales.action.NoAction.NoAction;

public class PassiveAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(PassiveAlgoLogic.class);

    //bidPricesOverTime assumes that each data point stored is the highest bid price for each day (total of 5 days)
    private Queue<Long> bestBidsOverTime = new LinkedList<>();
    private static final int maxPricesStored = 5;
    private double currentSMA = 0;
    private double stopLossPrice = 0;
    private static final int maxOrderCount = 20;

    @Override
    public Action evaluate(SimpleAlgoState state) {

        logger.info("[MYALGO] In Algo Logic....");

        final String book = Util.orderBookToString(state);

        logger.info("[MYALGO] Algo Sees Book as:\n" + book);

        final BidLevel bestBidPrice = state.getBidAt(0); //best bid price (algo will sell at this price once its over SMA; (nearTouch)
        final AskLevel bestAskPrice = state.getAskAt(0); //best ask price (Algo will buy at this price first to create child orders----lowest price ppl want to sell at)

        long quantity = 75; //qty algo will buy

        //algo takes in these for later decision
        long bestSellingPrice = bestAskPrice.price;
        long highestBidPrice = bestBidPrice.price;

        //Create a SMA method separately

        // Maintain queue size, removing oldest if necessary
        if (bestBidsOverTime.size() >= maxPricesStored) {
            bestBidsOverTime.remove(); // Removes the oldest price
        }
        // CHANGED: Now, we always add the latest price to the queue
        bestBidsOverTime.add(highestBidPrice); // Adds the latest price

        //Next, algo first calculates SMA manually if algo has enough prices
        if (bestBidsOverTime.size() == maxPricesStored) {
            double sum = 0;
            for (long eachPrice : bestBidsOverTime) {
                sum += eachPrice;
            }
            currentSMA = sum / maxPricesStored;
            logger.info("[MYALGO] Calculated SMA: " + currentSMA);

// CHANGED:Algo can only proceed with decision-making when the SMA is calculated
            return decideTrade(highestBidPrice, state);
        } else {
            // CHANGED: Log when there aren't enough prices and return NoAction
            logger.info("[MYALGO] Not enough prices to calculate SMA. No action taken.");
            return NoAction.NoAction; // No decision can be made yet
        }
    }


    // Method for algo to decide whether to buy, sell, or take no action based on the currentSMA and market state
    private Action decideTrade(long highestBidPrice, SimpleAlgoState state) {

        //Algo checks if the current price is higher than the SMA and algo doesn't have an active position
        long entryPrice = highestBidPrice;
        stopLossPrice = entryPrice * 0.98;
        BidLevel topBid = state.getBidAt(0);
        long quantity = 75; //qty algo will buy

        // Combined check for max orders and bid levels (MOVED MY MAX ORDER COUNT UNDER HERE)
        if (state.getChildOrders().size() > maxOrderCount || state.getBidLevels() == 0) {
            logger.info("Number of child orders in the order book: " + state.getChildOrders().size());
            return NoAction;
        }

        //buy logic
        if (highestBidPrice >= currentSMA && entryPrice == 0) {

                logger.info("[MYALGO] Bullish signal detected. Placing buy order at " + highestBidPrice);
                logger.info("[MYALGO] Order book after buy order looks like this:\n" + Util.orderBookToString(state));

             //  return new CreateChildOrder(Side.BUY, quantity, topBid.getPrice());

                //until we have three child orders....
               if (state.getChildOrders().size() < 3) {

                    //then keep creating a new one on buy side cos thats what algo joins
                    logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, want 3, joining buy side to obtain: " + quantity + " @ " + highestBidPrice);
                    return new CreateChildOrder(Side.BUY, quantity, highestBidPrice);
                } else {
                    logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, want 3, done.");
                    return NoAction;
                }
            }

            //FOR MY ALGO SELL ACTION:
            if (entryPrice > 0) {
                double profitTarget = entryPrice * 1.00033; //my profit target is 0.033% (btw 1 and 10% each month if successful)

                if (highestBidPrice >= profitTarget) { //when ppl want to buy at a price higher than what i bought stock for
                    AskLevel topAsk = state.getAskAt(0);

                    logger.info("[MYALGO] Profit target reached. Selling: " + quantity + " children at " + highestBidPrice + " to take profit!");
                    logger.info("[MYALGO] Order book after sell order looks like this:\n" + Util.orderBookToString(state));

                    return new CreateChildOrder(Side.SELL, topAsk.getQuantity(), topAsk.getPrice()); //algo creates a sell order to realise profits
                }


               // STOPLOSS MECHANISM
                if (highestBidPrice <= stopLossPrice) {
                    logger.info("[MYALGO] Stop-loss triggered at " + stopLossPrice + ". Cancelling any existing order");

                    final var activeOrders = state.getActiveChildOrders();

                    if (activeOrders.size() > 0) { // if there's any...

                        //it finds wc is the first:
                        final var option = activeOrders.stream().findFirst();

                        //algo cancels an active child order if it's present
                        if (option.isPresent()) {
                            var childOrder = option.get();

                            //algo cancels any existing buy order
                            return new CancelChildOrder(childOrder);

                            /**sells any active position (any stock it might have already bought)
                             if (state.getAskLevels() > 0) {
                             AskLevel topAsk = state.getAskAt(0);
                             return new CreateChildOrder(Side.SELL, topAsk.getQuantity(), topAsk.getPrice());
                             } else {
                             logger.info("[MYALGO] No ask levels available, unable to execute sell order at this time.");
                             return cancelOrder;
                             } */
                        }

                        return codingblackfemales.action.NoAction.NoAction;
                    }
                }

                return NoAction;
            }


        return NoAction;


    }
}
