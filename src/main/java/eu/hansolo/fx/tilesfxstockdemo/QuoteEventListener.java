package eu.hansolo.fx.tilesfxstockdemo;

/**
 * Created by hansolo on 24.03.17.
 */
@FunctionalInterface
public interface QuoteEventListener {
    void onStockQuoteEvent(QuoteEvent event);
}
