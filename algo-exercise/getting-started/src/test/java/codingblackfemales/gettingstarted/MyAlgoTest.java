package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import messages.order.Side;
import org.junit.Test;

import static codingblackfemales.container.Actioner.logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;

/**
 * This test is designed to check your algo behavior in isolation of the order book.
 *
 * You can tick in market data messages by creating new versions of createTick() (ex. createTick2, createTickMore etc..)
 *
 * You should then add behaviour to your algo to respond to that market data by creating or cancelling child orders.
 *
 * When you are comfortable you algo does what you expect, then you can move on to creating the MyAlgoBackTest.
 *
 */


public class MyAlgoTest extends AbstractAlgoTest {

        @Override
        public AlgoLogic createAlgoLogic() {
            return new MyAlgoLogic();
        }

        @Test
        public void testAlgoNeverExceedsMaxOrderCount() throws Exception {
            for (int i = 0; i <= 25; i++) {
                send(createTick());
            }
            assertTrue(container.getState().getChildOrders().size() <= 20);
        }

    @Test
    public void testAlgoCreatesBuyOrder() throws Exception {
        // Send market data ticks that should trigger buy orders.
        for (int i = 0; i <= 6; i++) {
            send(createTick());

        }
            // Refresh the state to ensure order states are updated as expected.
            SimpleAlgoState state = container.getState();
            state.refreshState();

            List<ChildOrder> activeOrders = state.getActiveChildOrders();
            assertEquals("Expected 3 active buy orders, but found " + activeOrders.size(), 3, activeOrders.size());

        }

    /** @Test
    public void testAlgoExecutesStopLoss() throws Exception {
        // Send ticks to trigger stop-loss logic
        for (int i = 0; i < 6; i++) {
            send(createTickStopLoss());
        }

        // Retrieve the state after sending the stop-loss tick
        SimpleAlgoState state = container.getState();

        List<ChildOrder> activeOrders = state.getActiveChildOrders();
        assertTrue("There should be no active orders after cancellation due to stop-loss", activeOrders.size() == 0);
        logger.info("Active orders count: " + activeOrders.size());

    }

    @Test
        public void testAlgoCreatesSellOrder() throws Exception {
            for (int i = 0; i <= 6; i++) {
                send(createTickSell());
            }
            assertEquals(3, container.getState().getChildOrders().stream()
                    .filter(childOrder -> childOrder.getSide() == Side.SELL)
                    .count());
        } */
    }

