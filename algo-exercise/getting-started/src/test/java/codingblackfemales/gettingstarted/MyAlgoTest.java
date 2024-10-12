package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import org.junit.Assert;
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

    public void testAlgoNeverExceedsMaxOrderCount() throws Exception {

        // Sending more than 20 ticks to trigger multiple buy actions

        for (int i = 0; i <= 20; i++) { //as long as number of orders is below 20, create buy tick

            send(createTickBuy());

        }

        // Check that the number of orders does not exceed 20

        assertTrue(container.getState().getChildOrders().size() <= 20);

    }

    @Test
    public void testAlgoBuysWhenMarketBidPriceCrossesAboveSMA() throws Exception {

        //create a sample market data tick to set up scenario where latest bid price is higher than my SMA
        for (int i = 0; i < 6; i++) { //for loop to ensure my algo gets needed number of orders for SMA
            //create a sample market data tick....
            send(createTickBuy());
        }

        //simple assert to check that my algo creates - and fulfills 1 orders
        assertEquals(container.getState().getChildOrders().size(), 1);
    }

     @Test
     public void testAlgoSellsWhenMarketBidPriceReachesProfitTarget() throws Exception {

         for (int i = 0; i < 6; i++) {

             send(createTickSell());
         }

     assertEquals(1, container.getState().getChildOrders().stream()
             .filter(childOrder -> childOrder.getSide() == Side.SELL)
             .count());

     }

     @Test
     public void testAlgoCancelsExistingBuyOrdersWhenStopLossIsTriggered() throws Exception {
     //stop loss should do two things: cancel existing buy orders and sell any it might have immediately
         //Using a for loop to avoid sending multiple ticks using a loop
         for (int i = 0; i < 6; i++) {
             send(createTickStopLoss());
         }
     //assertion that this order was canceled after buying
     assertEquals(0, container.getState().getChildOrders().size());
     }


}

