package eu.hansolo.fx.tilesfxstockdemo;

/**
 * Created by hansolo on 24.03.17.
 */
public class QuoteEvent {
    private StockQuote stockQuote;


    public QuoteEvent(final StockQuote STOCK_QUOTE) {
        stockQuote = STOCK_QUOTE;
    }


    public StockQuote getStockQuote() { return stockQuote; }
}
