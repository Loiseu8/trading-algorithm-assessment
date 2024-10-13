package codingblackfemales.algo;

import codingblackfemales.container.Actioner;
import codingblackfemales.container.AlgoContainer;
import codingblackfemales.container.RunTrigger;
import codingblackfemales.sequencer.DefaultSequencer;
import codingblackfemales.sequencer.Sequencer;
import codingblackfemales.sequencer.consumer.LoggingConsumer;
import codingblackfemales.sequencer.marketdata.SequencerTestCase;
import codingblackfemales.sequencer.net.TestNetwork;
import codingblackfemales.service.MarketDataService;
import codingblackfemales.service.OrderService;
import messages.marketdata.*;
import org.agrona.concurrent.UnsafeBuffer; //memory buffer used to handle data sent n received efficiently
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PassiveAlgoTest extends SequencerTestCase {

    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final BookUpdateEncoder encoder = new BookUpdateEncoder();

    private AlgoContainer container;

    @Override
    public Sequencer getSequencer() {
        final TestNetwork network = new TestNetwork();
        final Sequencer sequencer = new DefaultSequencer(network);

        final RunTrigger runTrigger = new RunTrigger();
        final Actioner actioner = new Actioner(sequencer);

        container = new AlgoContainer(new MarketDataService(runTrigger), new OrderService(runTrigger), runTrigger, actioner);
        //set my algo logic
        container.setLogic(new PassiveAlgoLogic());

        network.addConsumer(new LoggingConsumer());
        network.addConsumer(container.getMarketDataService());
        network.addConsumer(container.getOrderService());
        network.addConsumer(container);

        return sequencer;
    }

    private UnsafeBuffer createSampleMarketDataTickCreate(){ //should trigger my algo's buy logic
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        //write the encoded output to the direct buffer
        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        //set the fields to desired values
        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);

        encoder.askBookCount(3)
                .next().price(100L).size(101L)
                .next().price(110L).size(200L)
                .next().price(115L).size(5000L);

        encoder.bidBookCount(3)
                .next().price(98L).size(100L) //i could leave as is since algo would create sma from sma and then buy if >= sma
                .next().price(95L).size(200L)
                .next().price(91L).size(300L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }

    private UnsafeBuffer createSampleMarketDataTickSell(){ //should trigger algo's profitability
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        //write the encoded output to the direct buffer
        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        //set the fields to desired values
        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);

        encoder.askBookCount(3)
                .next().price(100L).size(101L)
                .next().price(110L).size(200L)
                .next().price(115L).size(5000L);

        encoder.bidBookCount(3)
                .next().price(200L).size(100L) //Changed this to trigger algo to sell for profit
                .next().price(95L).size(200L)
                .next().price(91L).size(300L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }

    private UnsafeBuffer createSampleMarketDataTickCancel(){ //should trigger algo's stoploss mechanism
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        //write the encoded output to the direct buffer
        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        //set the fields to desired values
        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);

        encoder.askBookCount(3)
                .next().price(100L).size(101L)
                .next().price(110L).size(200L)
                .next().price(115L).size(5000L);

        encoder.bidBookCount(3)
                .next().price(80L).size(100L) //changed bid prices to simulate bear mkt
                .next().price(78L).size(200L)
                .next().price(72L).size(300L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);
        encoder.source(Source.STREAM);

        return directBuffer;
    }

    @Test

    public void testAlgoNeverExceedsMaxOrderCount() throws Exception {

        // Sending more than 20 ticks to trigger multiple buy actions

        for (int i = 0; i <= 20; i++) { //as long as number of orders is below 20, create buy tick

            send(createSampleMarketDataTickCreate());

        }

        // Check that the number of orders does not exceed 20

        assertTrue(container.getState().getChildOrders().size() <= 20);

    }

    @Test
    public void testAlgoCreatesChildOrders() throws Exception {

        for (int i = 0; i < 6; i++) { //for loop to ensure my algo gets needed number of orders for SMA
            //create a sample market data tick....
            send(createSampleMarketDataTickCreate());
        }
        //simple assert to check we had 3 orders created
        assertEquals(3, container.getState().getChildOrders().size());
    }

    @Test
    public void testAlgoSellsForProfit() throws Exception {

        for (int i = 0; i < 6; i++) {

            send(createSampleMarketDataTickSell());
        }

        assertEquals(3, container.getState().getChildOrders().size(), 0);
    }



    @Test
    public void testAlgoCancelsOrdersWhenStopLossIsTriggered() throws Exception {
        //Using a for loop to avoid sending multiple ticks using a loop
        for (int i = 0; i < 6; i++) {
            send(createSampleMarketDataTickCancel());
        }
        // Stop-loss should sell any stock it has, assert no child orders remain
        assertEquals(0, container.getState().getChildOrders().size());
    }

}
