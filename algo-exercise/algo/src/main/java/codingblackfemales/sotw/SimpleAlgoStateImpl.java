package codingblackfemales.sotw;

import codingblackfemales.service.MarketDataService;
import codingblackfemales.service.OrderService;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import messages.marketdata.BookUpdateDecoder;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleAlgoStateImpl implements SimpleAlgoState {

    public final MarketDataService marketDataService;
    public final OrderService orderService;

    public SimpleAlgoStateImpl(final MarketDataService marketDataService, final OrderService orderService) {
        this.marketDataService = marketDataService;
        this.orderService = orderService;
    }

    @Override
    public void refreshState() { //adding this method to force update state after buying but still didn't work
        // Optionally iterate through child orders to update their states, if this is necessary.
        orderService.children().forEach(childOrder -> {
            if (childOrder.getState() == OrderState.PENDING) {
                childOrder.setState(OrderState.ACKED); // Example: Update PENDING orders to ACKED.
            }
        });

        // Logger for refreshed state just in case
        System.out.println("State has been refreshed. Active orders count: " + getActiveChildOrders().size());
    }


    @Override
    public long getInstrumentId() {
        return marketDataService.getInstrumentId();
    }

    @Override
    public String getSymbol() {
        return null;
    }

    @Override
    public int getBidLevels() {
        return marketDataService.getBidLength();
    }

    @Override
    public int getAskLevels() {
        return marketDataService.getAskLength();
    }

    @Override
    public BidLevel getBidAt(int index) {
        return marketDataService.getBidLevel(index);
    }

    @Override
    public AskLevel getAskAt(int index) {
        return marketDataService.getAskLevel(index);
    }

    @Override
    public List<ChildOrder> getChildOrders() {
        return orderService.children();
    }

    @Override
    public List<ChildOrder> getActiveChildOrders() {
        return orderService.children().stream().filter(order -> order.getState() != OrderState.CANCELLED).collect(Collectors.toList());
    }
}
