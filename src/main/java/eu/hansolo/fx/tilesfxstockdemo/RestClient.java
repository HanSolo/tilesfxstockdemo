package eu.hansolo.fx.tilesfxstockdemo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Task;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by hansolo on 23.03.17.
 */
public class RestClient {
    private static final String                     REST_URL = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=";
    private              String                     stockSymbol;
    private              ObjectProperty<JSONObject> quote;


    // ******************** Constructor ***************************************
    RestClient(final String STOCK_SYMBOL) {
        stockSymbol = STOCK_SYMBOL;
        quote       = new ObjectPropertyBase<JSONObject>(new JSONObject()) {
            @Override public Object getBean() { return RestClient.this; }
            @Override public String getName() { return "quote"; }
        };
    }


    // ******************** Methods *******************************************
    public String getStockSymbol() { return stockSymbol; }
    public void setStockSymbol(final String STOCK_SYMBOL) { stockSymbol = STOCK_SYMBOL; }

    public JSONObject getQuote() { return quote.get(); }
    public ReadOnlyObjectProperty<JSONObject> quoteProperty() { return quote; }

    public void updateQuote() {
        Task<JSONObject> task = createTask(stockSymbol);
        task.setOnSucceeded(event -> quote.set((JSONObject) event.getSource().getValue()));
        new Thread(task).start();
    }

    public JSONObject getQuoteFor(final String STOCK_SYMBOL) {
        try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet get = new HttpGet(String.join("", REST_URL, STOCK_SYMBOL));
            get.addHeader("accept", "application/json");

            CloseableHttpResponse response = httpClient.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) { return new JSONObject(); }
            JSONObject jsonObject = (JSONObject) JSONValue.parse(getFromResponse(response));
            return jsonObject;
        } catch (IOException e) {
            return new JSONObject();
        }
    }

    public Task createTask(final String STOCK_SYMBOL) {
        Task<JSONObject> task = new Task<JSONObject>() {
            @Override protected JSONObject call() throws Exception {
                try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                    HttpGet get = new HttpGet(String.join("", REST_URL, STOCK_SYMBOL));
                    get.addHeader("accept", "application/json");

                    CloseableHttpResponse response = httpClient.execute(get);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 200) { return new JSONObject(); }
                    return (JSONObject) JSONValue.parse(getFromResponse(response));
                } catch (IOException e) {
                    return new JSONObject();
                }
            }
        };
        return task;
    }

    private String getFromResponse(final CloseableHttpResponse RESPONSE) {
        final StringBuilder OUTPUT = new StringBuilder();
        try (BufferedReader br     = new BufferedReader(new InputStreamReader(RESPONSE.getEntity().getContent()))) {
            String line;
            while ((line = br.readLine()) != null) { OUTPUT.append(line); }
        } catch(IOException exception) {
            System.out.println("Error: " + exception);
        }
        return OUTPUT.toString();
    }
}
