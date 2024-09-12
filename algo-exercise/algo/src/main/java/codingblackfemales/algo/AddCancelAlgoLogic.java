package codingblackfemales.algo;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//AlgoLogic requires that any class implementing it
// provide d evaluate method (wc is below) to define d algo's actions
public class AddCancelAlgoLogic implements AlgoLogic {

// sets up a logger (tool to track wats happening in d prog n also for troubleshooting)
    private static final Logger logger = LoggerFactory.getLogger(AddCancelAlgoLogic.class);


    // "evaluate" contains the logic for the algo
    // here the logic is either doing nothing(if over max order count),
    // adding or canceling child orders


    @Override
    //evaluate takes in a SimpleAlgoState obj (state)
    // wc contains MKT DATA AND ORDER INFOS.
    public Action evaluate(SimpleAlgoState state) {

        logger.info("[ADDCANCELALGO] In Algo Logic...."); //logger msg for when algo starts working

        final String book = Util.orderBookToString(state); //converts state to readable string
        logger.info("[ADDCANCELALGO] Algo Sees Book as:\n" + book);
        //logger above then shows algo is seeing the current state of the order book (mkt)

        // algo counts how many child orders and ensures it never exceeds 20
        var totalOrderCount = state.getChildOrders().size();

        //make sure we have an exit condition...
        //shouldn't make too many orders at once (so as not to disturb the mkt)
        if (totalOrderCount > 20) {
            return NoAction.NoAction; //does nothing if existing child orders size is over 20
        }

        //d ffg checks if there are active child orders:
        // this can help me manage RISKS
        // e.g., when mkt conditions have changed since order was placed
        // or esp when the mkt turns volatile
        final var activeOrders = state.getActiveChildOrders();

        if (activeOrders.size() > 0) { // if there's any...

            //it finds wc is the first:
            final var option = activeOrders.stream().findFirst();


            //algo cancels an active child order if it's present
            if (option.isPresent()) {
                var childOrder = option.get();

                // and sends logger msg that it's doing this:
                logger.info("[ADDCANCELALGO] Cancelling order:" + childOrder);

                //returns a CancelChildOrder action to cancel the order
                return new CancelChildOrder(childOrder);
            }
            else{
                return NoAction.NoAction; //algo does nothing if no active child orders
            }


            //lastly, when the algo has made sure there's no active order, it creates a new one:
            // It checks the market/orderbook for the top bid price:
        } else {
            BidLevel level = state.getBidAt(0);

            //it then collects the price and qty at that level:
            final long price = level.price;
            final long quantity = level.quantity;

            //it sends logger msg to say that it's creating a new child order
            //and states what price and qty it's setting for the order:
            logger.info("[ADDCANCELALGO] Adding order for" + quantity + "@" + price);

            //then it creates a new CreateChildOrder instance
            // to "invoke the BUY method" for the order at d price and qty it wants
            return new CreateChildOrder(Side.BUY, quantity, price);
        }

        //In summary, this simple logic adds or cancels child orders as needed wc is the primary goal of this project.
        // Next, I'll be modifying this greatly to implement new algo logic that is profitable
        //Then, I'll backtest this logic to see how it would have fared if used on old market data
        //Market data service (a json file) already exists in this project to provide the mkt data i need.
        //I intend to add and use break points in my code so I can truly measure the full extent of existing code and functions I can use or call on

    }
}
