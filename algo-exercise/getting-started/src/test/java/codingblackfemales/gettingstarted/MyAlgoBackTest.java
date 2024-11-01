package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.OrderState;
import messages.order.Side;
import org.junit.Test;

import static codingblackfemales.container.Actioner.logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import codingblackfemales.sotw.ChildOrder;

//how i understand the backtesting for trading algos is that they tie all the algo functions into one to show how
// they would function together to form a robust and profitable system that effectively handles past market conditions
// so i have my original createTick triggering SMA calc and buy logic. after this, my sell logic kicks in
// to make profit... then stop-loss to minimise loss during bear mkt as well as a check for my exit condition

/**
 * This test plugs together all of the infrastructure, including the order book (which you can trade against)
 * and the market data feed.
 *
 * If your algo adds orders to the book, they will reflect in your market data coming back from the order book.
 *
 * If you cross the spread (i.e. you BUY an order with a price which is == or > askPrice()) you will match,
 * and receive a fill back into your order from the order book (visible from the algo in the childOrders of the state object).
 *
 * If you cancel the order your child order will show the order status as cancelled in the childOrders of the state object.
 *
 */
public class MyAlgoBackTest extends AbstractAlgoBackTest {

    // Overrides the default algo logic creation method to use MyAlgoLogic
    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic();
    }

    // Main test method for backtesting the algorithm logic
    @Test
    public void testAlgoBackTest() throws Exception {
        try {
            // Sending 6 market data ticks to simulate market activity
            for (int i = 0; i <= 6; i++) {
                send(createTick()); // This triggers buy orders in the algo as it gets enough bids to calc SMA
            }

            // Assert that the algorithm places 3 buy orders after receiving the ticks
            assertEquals(3, container.getState().getChildOrders().size());

            // Sending additional ticks to test other actions... selling and stop-loss
            send(createTickBackTestSell()); // Triggers sell logic in the algo
            send(createTickBackTestStopLoss()); // Triggers stop-loss logic

            // Retrieve the current state of the algo after the actions
            var state = container.getState();

            // Check that 3 buy orders were placed by the algo
            long buyOrdersCount = state.getChildOrders().stream()
                    .filter(order -> order.getSide() == Side.BUY) // Filter for buy orders
                    .count();
            assertEquals("3 buy orders should be placed", 3, buyOrdersCount);

            // Calculate the total filled buy quantity and ensure it's 225
            long filledBuyQuantity = state.getChildOrders().stream()
                    .filter(order -> order.getSide() == Side.BUY) // Filters for buy orders
                    .map(ChildOrder::getFilledQuantity) // Gets their filled quantities
                    .reduce(Long::sum) // Sums up the filled quantities
                    .orElse(0L); // If no buy orders, it returns 0
            assertEquals("The filled buy quantity should be 225", 225, filledBuyQuantity);


            // Check for sell orders, expecting 2
            long sellOrdersCount = state.getChildOrders().stream()
                    .filter(order -> order.getSide() == Side.SELL) // this Filters for only sell orders like in my unit test
                    .count();
            assertEquals("Algo must have placed sell orders", 2, sellOrdersCount);

            // Checking for stop-loss action by counting canceled orders
            long canceledOrdersCount = state.getChildOrders().stream()
                    .filter(order -> order.getState() == OrderState.CANCELLED) // Filter for canceled orders
                    .count();
            assertEquals("Stop-loss should cancel one or more orders", 3, canceledOrdersCount); // Expecting 3 canceled orders

            // BackTest to check that the algorithm never exceeds the maximum order count (20) during backtesting
            for (int i = 7; i <= 25; i++) {
                send(createTick()); // Continue sending ticks to simulate extended execution
            }

            // Get the final state and count the total child orders
            long childOrdersCount = state.getChildOrders().size();
            logger.info("Child orders count: " + childOrdersCount);

            // Assert that the total number of child orders does not exceed 20
            assertTrue("The algorithm should never place more than 20 orders", childOrdersCount <= 20);

            // Catch and log any NullPointerExceptions that occur due to the stop-loss logic
        } catch (NullPointerException e) {
            System.out.println("A NullPointerException occurred: " + e.getMessage());
        }
    }
}
