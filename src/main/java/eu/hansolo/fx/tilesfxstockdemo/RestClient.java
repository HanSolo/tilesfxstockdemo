package eu.hansolo.fx.tilesfxstockdemo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
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
 * Created by hansolo on 12.03.17.
 */
public class RestClient {
    private static final String                     REST_URL = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=";
    private              String                     companySymbol;
    private              ObjectProperty<JSONObject> quote;


    RestClient(final String COMPANY_SYMBOL) {
        companySymbol = COMPANY_SYMBOL;
        quote         = new ObjectPropertyBase<JSONObject>(new JSONObject()) {
            @Override public Object getBean() { return RestClient.this; }
            @Override public String getName() { return "quote"; }
        };
    }


    // ******************** Methods ************************************
    public String getCompanySymbol() { return companySymbol; }
    public void setCompanySymbol(final String COMPANY_SYMBOL) { companySymbol = COMPANY_SYMBOL; }

    public JSONObject getQuote() { return quote.get(); }
    public ReadOnlyObjectProperty<JSONObject> quoteProperty() { return quote; }
    /**
     * Updates the current quote which is dependent on the companySymbol property.
     * This method is non-blocking !!!
     */
    public void updateQuote() {
        Task<JSONObject> task = new Task<JSONObject>() {
            @Override protected JSONObject call() throws Exception {
                try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                    HttpGet get = new HttpGet(REST_URL + companySymbol);
                    get.addHeader("accept", "application/json");

                    CloseableHttpResponse response = httpClient.execute(get);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 200) {
                        //throw new RuntimeException("Failed: HTTP error code: " + statusCode);
                        System.out.println("Failed: HTTP error code " + statusCode);
                        return new JSONObject();
                    }
                    return (JSONObject) JSONValue.parse(getFromResponse(response));
                } catch (IOException e) {
                    return new JSONObject();
                }
            }
        };
        task.setOnSucceeded(event -> quote.set((JSONObject) event.getSource().getValue()));
        new Thread(task).start();
    }

    /**
     * Returns the current Quote for the given Company Stock Ticker Symbol (e.g. AAPL -> Apple Inc.)
     * This method is blocking!!!
     * @param COMPANY_SYMBOL
     * @return the current Quote for the given Company Stock Ticker Symbol (e.g. AAPL -> Apple Inc.)
     */
    public JSONObject getQuoteFor(final String COMPANY_SYMBOL) {
        try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet get = new HttpGet(REST_URL + COMPANY_SYMBOL);
            get.addHeader("accept", "application/json");

            CloseableHttpResponse response = httpClient.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                //throw new RuntimeException("Failed: HTTP error code: " + statusCode);
                return new JSONObject();
            }
            JSONObject jsonObject = (JSONObject) JSONValue.parse(getFromResponse(response));
            return jsonObject;
        } catch (IOException e) {
            return new JSONObject();
        }
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
