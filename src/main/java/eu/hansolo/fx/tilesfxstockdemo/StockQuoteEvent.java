package eu.hansolo.fx.tilesfxstockdemo;

/**
 * Created by hansolo on 24.03.17.
 */
public class StockQuoteEvent {
    private StockQuote stockQuote;


    public StockQuoteEvent(final StockQuote STOCK_QUOTE) {
        stockQuote = STOCK_QUOTE;
    }


    public StockQuote getStockQuote() { return stockQuote; }
}
