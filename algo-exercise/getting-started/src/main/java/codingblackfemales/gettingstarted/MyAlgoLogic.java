package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.NoAction;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        // Count the total number of child orders
        var totalOrderCount = state.getChildOrders().size();

        // If there are active orders, cancel the first one
        if (totalOrderCount > 0) {
            ChildOrder orderToCancel = state.getActiveChildOrders().stream().findFirst().orElse(null);
            if (orderToCancel != null) {
                logger.info("[MYALGO] Cancelling order: " + orderToCancel);

                //Remove the order from the state to ensure that state reflects cancellation

                //return the new CancelChildOrder action:
                return new CancelChildOrder(orderToCancel);
            }
        }


        // If there are no active orders, create a new one
        if (totalOrderCount == 0) {
            logger.info("[MYALGO] No active orders found, creating a new order.");

            // Create a new child order based on market data
            long price = 100L; // Placeholder price (can replace this with logic to get a real price)
            long quantity = 10L; // Placeholder quantity (adjustable)

            logger.info("[MYALGO] Adding new child order for quantity " + quantity + " at price " + price);
            return new CreateChildOrder(Side.BUY, quantity, price);
        }

        // If no action is needed, return NoAction
        return NoAction.NoAction;
    }
}
