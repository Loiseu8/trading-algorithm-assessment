package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.orderbook.OrderBook;
import codingblackfemales.orderbook.order.Order;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import messages.order.Side;
import org.junit.Test;
import messages.marketdata.*;
import org.agrona.concurrent.UnsafeBuffer; // Memory buffer used to handle data sent and received efficiently
import org.junit.Test;

import static org.junit.Assert.*;

public class MyAlgoTest extends AbstractAlgoTest {

    @Override
    public AlgoLogic createAlgoLogic() {

        return new MyAlgoLogic();

    }

    // Test method to validate the Add and Cancel logic of the algorithm
    @Test
    public void testDispatchThroughSequencer() throws Exception {

        SimpleAlgoState state = container.getState();

        // Send a sample market data tick to simulate a market update
        send(createTick());

        // Assert that one child order is created after processing the first tick
        assertEquals("Should add one new child order after first tick", 1, container.getState().getChildOrders().size());

        // Send another market data tick to trigger a cancel action
        send(createTick());

        // Assert that the previously created child order has been canceled and no active orders remain
        assertEquals("Should have zero child orders after cancel", 1, state.getCancelledChildOrders().size());

    }
}
