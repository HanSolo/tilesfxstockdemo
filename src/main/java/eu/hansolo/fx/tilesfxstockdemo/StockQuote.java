package eu.hansolo.fx.tilesfxstockdemo;

import eu.hansolo.fx.dotmatrix.DotMatrix;
import eu.hansolo.tilesfx.Tile.TileColor;
import org.json.simple.JSONObject;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


/**
 * Created by hansolo on 23.03.17.
 */
public class StockQuote {
    private static final int               GREEN     = DotMatrix.convertToInt(TileColor.LIGHT_GREEN.color);
    private static final int               RED       = DotMatrix.convertToInt(TileColor.LIGHT_RED.color);
    private static final int               ORANGE    = DotMatrix.convertToInt(TileColor.ORANGE.color);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zXXX yyyy", Locale.US);
    private              String            symbol;
    private              String            name;
    private              ZonedDateTime     timestamp;
    private              double            previousClose;
    private              double            open;
    private              double            lastPrice;
    private              double            high;
    private              double            low;
    private              double            change;
    private              double            changePercent;
    private              int               colorValue;


    // ******************** Constructor ***************************************
    public StockQuote(final JSONObject JSON_QUOTE) {
        symbol        = JSON_QUOTE.get("Symbol").toString();
        name          = JSON_QUOTE.get("Name").toString();
        timestamp     = ZonedDateTime.parse(JSON_QUOTE.get("Timestamp").toString().replace("UTC", "EDT"), FORMATTER);
        open          = Double.parseDouble(JSON_QUOTE.get("Open").toString());
        lastPrice     = Double.parseDouble(JSON_QUOTE.get("LastPrice").toString());
        high          = Double.parseDouble(JSON_QUOTE.get("High").toString());
        low           = Double.parseDouble(JSON_QUOTE.get("Low").toString());
        change        = Double.parseDouble(JSON_QUOTE.get("Change").toString());
        changePercent = Double.parseDouble(JSON_QUOTE.get("ChangePercent").toString());
        previousClose = lastPrice - change;
        colorValue    = change < 0 ? RED : change > 0 ? GREEN : ORANGE;
    }
    public StockQuote(final String SYMBOL) {
        symbol        = SYMBOL;
        name          = "";
        timestamp     = ZonedDateTime.now();
        previousClose = 0;
        open          = 0;
        lastPrice     = 0;
        high          = 0;
        low           = 0;
        change        = 0;
        changePercent = 0;
    }


    // ******************** Methods *******************************************
    public String getSymbol() { return symbol; }
    public void setSymbol(final String SYMBOL) { symbol = SYMBOL; }

    public String getName() { return name; }
    public void setName(final String NAME) { name = NAME; }

    public ZonedDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(final ZonedDateTime TIMESTAMP) { timestamp = TIMESTAMP; }

    public double getPreviousClose() { return previousClose; }
    public void setPreviousClose(final double PREVIOUS_CLOSE) { previousClose = PREVIOUS_CLOSE; }

    public double getOpen() { return open; }
    public void setOpen(final double OPEN) { open = OPEN; }

    public double getLastPrice() { return lastPrice; }
    public void setLastPrice(final double LAST_PRICE) {
        lastPrice = LAST_PRICE;
        setPreviousClose(getLastPrice() - change);
    }

    public double getHigh() { return high; }
    public void setHigh(final double HIGH) { high = HIGH; }

    public double getLow() { return low; }
    public void setLow(final double LOW) { low = LOW; }

    public double getChange() { return change; }
    public void setChange(final double CHANGE) {
        change = CHANGE;
        setPreviousClose(getLastPrice() - change);
        setColorValue(change < 0 ? RED : change > 0 ? GREEN : ORANGE);
    }

    public double getChangePercent() { return changePercent; }
    public void setChangePercent(final double CHANGE_PERCENT) { changePercent = CHANGE_PERCENT; }

    public int getColorValue() { return colorValue; }
    public void setColorValue(final int COLOR_VALUE) { colorValue = COLOR_VALUE; }

    public void update(final JSONObject JSON_QUOTE) {
        symbol        = JSON_QUOTE.get("Symbol").toString();
        name          = JSON_QUOTE.get("Name").toString();
        timestamp     = ZonedDateTime.parse(JSON_QUOTE.get("Timestamp").toString().replace("UTC", "EDT"), FORMATTER);
        open          = Double.parseDouble(JSON_QUOTE.get("Open").toString());
        lastPrice     = Double.parseDouble(JSON_QUOTE.get("LastPrice").toString());
        high          = Double.parseDouble(JSON_QUOTE.get("High").toString());
        low           = Double.parseDouble(JSON_QUOTE.get("Low").toString());
        change        = Double.parseDouble(JSON_QUOTE.get("Change").toString());
        changePercent = Double.parseDouble(JSON_QUOTE.get("ChangePercent").toString());
        previousClose = lastPrice - change;
        colorValue    = change < 0 ? RED : change > 0 ? GREEN : ORANGE;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Symbol", getSymbol());
        jsonObject.put("Name", getName());
        jsonObject.put("Timestamp", FORMATTER.format(getTimestamp()));
        jsonObject.put("PreviousClose", getPreviousClose());
        jsonObject.put("Open", getOpen());
        jsonObject.put("LastPrice", getLastPrice());
        jsonObject.put("High", getHigh());
        jsonObject.put("Low", getLow());
        jsonObject.put("Change", getChange());
        jsonObject.put("ChangePercent", getChangePercent());
        return jsonObject;
    }

    @Override public String toString() { return toJSON().toJSONString(); }
}
