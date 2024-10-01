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
import messages.marketdata.BookUpdateEncoder;


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
    public void testAlgoBuysWhenMarketBidPriceCrossesAboveSMA() throws Exception {

        //create a sample market data tick to set up scenario where latest bid price is higher than my SMA
        send(createTick()); //first tick below SMA
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5()); //last tick crosses above SMA

       //simple assert to check that my algo creates - and fulfills 1 orders
        assertEquals(container.getState().getChildOrders().size(), 1);
    }

    /**
    @Test
    public void testAlgoSellsWhenMarketBidPriceReachesProfitTarget() throws Exception {


        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5()); //this tick must trigger buy action

        //hitting profit target
       // send(createTickWithBidPrice(102)); // Should reach the profit target

        assertEquals(container.getState().getChildOrders().size(), 0);

    }

    @Test
    public void testAlgoCancelsExistingBuyOrdersWhenStopLossIsTriggered() throws Exception {
        //stop loss should do two things: cancel existing buy orders and sell any it might have immediately
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5()); //buy action triggered again

        //now stop-loss kicks in
        //send(createTickWithBidPrice(94)); //bid price is below stop-loss threshold

        //assertion that this order was canceled after buying
        assertEquals(container.getState().getChildOrders().size(), 0);
    }

    @Test //this test might not be necessary cos the SMA logic would have canceled anyway?
    public void testAlgoSellsExistingPositionsWhenStopLossIsTriggered() throws Exception {

        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5()); // triggers buy action

        //then stop-loss sells any stock it might still have too

        assertEquals(container.getState().getChildOrders().size(), 0);
    }

*/
}


