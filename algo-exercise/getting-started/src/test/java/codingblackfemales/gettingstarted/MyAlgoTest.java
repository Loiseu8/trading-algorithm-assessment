package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import org.junit.Test;
import codingblackfemales.action.Action;
import codingblackfemales.sotw.SimpleAlgoState;
import messages.order.Side;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

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
        //this adds your algo logic to the container classes
        return new MyAlgoLogic();
    }


    @Test
    public void testAlgoBuysWhenMarketBidPriceCrossesAboveEMA() throws Exception {

        //create a sample market data tick....
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());

        //simple assert to check that my algo creates - and fulfills 1 orders
        assertEquals(container.getState().getChildOrders().size(), 1);
    }

    @Test
    public void testAlgoSellsWhenMarketBidPriceReachesProfitTarget() throws Exception {
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());

        assertEquals(container.getState().getChildOrders().size(), 0);

    }

    @Test
    public void testAlgoCancelsExistingBuyOrdersWhenStopLossIsTriggered() throws Exception {
        //stop loss should do two things: cancel existing buy orders and sell any it might have immediately
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());

        assertEquals(container.getState().getChildOrders().size(), 0);
    }

    @Test
    public void testAlgoDoesntExceedMaxOrderCount() throws Exception {
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());

        assertEquals(container.getState().getChildOrders().size(), 0);
    }
}


