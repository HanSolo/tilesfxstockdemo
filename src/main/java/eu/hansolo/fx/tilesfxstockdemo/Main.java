package eu.hansolo.fx.tilesfxstockdemo;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.tools.FlowGridPane;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.stage.Stage;


/**
 * User: hansolo
 * Date: 12.03.17
 * Time: 11:58
 */
public class Main extends Application {
    private static final double         TILE_SIZE = 300;
    private              RestClient     aaplRestClient;
    private              RestClient     msftRestClient;
    private              RestClient     googRestClient;
    private              RestClient     orclRestClient;
    private              Tile           appleStockTile;
    private              Tile           microsoftStockTile;
    private              Tile           googleStockTile;
    private              Tile           oracleStockTile;
    private              long           lastStockCall;
    private              AnimationTimer timer;


    @Override public void init() {
        aaplRestClient = new RestClient("AAPL");
        msftRestClient = new RestClient("MSFT");
        googRestClient = new RestClient("GOOG");
        orclRestClient = new RestClient("ORCL");

        appleStockTile     = createTile("AAPL (Apple)");
        microsoftStockTile = createTile("MSFT (Microsoft)");
        googleStockTile    = createTile("GOOG (Google)");
        oracleStockTile    = createTile("ORCL (Oracle)");

        Platform.runLater(() -> {
            appleStockTile.setValue(139.14);
            microsoftStockTile.setValue(64.93);
            googleStockTile.setValue(861.41);
            oracleStockTile.setValue(42.68);
        });

        lastStockCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastStockCall + 600_000_000_000l) { // update every 10 minutes
                    aaplRestClient.updateQuote();
                    msftRestClient.updateQuote();
                    googRestClient.updateQuote();
                    orclRestClient.updateQuote();
                    lastStockCall = now;
                }
            }
        };

        aaplRestClient.quoteProperty().addListener(o -> appleStockTile.setValue(Double.parseDouble(aaplRestClient.getQuote().get("LastPrice").toString())));
        msftRestClient.quoteProperty().addListener(o -> microsoftStockTile.setValue(Double.parseDouble(msftRestClient.getQuote().get("LastPrice").toString())));
        googRestClient.quoteProperty().addListener(o -> googleStockTile.setValue(Double.parseDouble(googRestClient.getQuote().get("LastPrice").toString())));
        orclRestClient.quoteProperty().addListener(o -> oracleStockTile.setValue(Double.parseDouble(orclRestClient.getQuote().get("LastPrice").toString())));
    }

    @Override public void start(Stage stage) {
        FlowGridPane pane = new FlowGridPane(2, 2, appleStockTile, microsoftStockTile, googleStockTile, oracleStockTile);
        pane.setHgap(5);
        pane.setVgap(5);
        pane.setPadding(new Insets(5));
        pane.setBackground(new Background(new BackgroundFill(Tile.BACKGROUND.darker(), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);

        stage.setTitle("TilesFX Stock Demo");
        stage.setScene(scene);
        stage.show();

        timer.start();

        // Initial update of quotes
        aaplRestClient.updateQuote();
        msftRestClient.updateQuote();
        googRestClient.updateQuote();
        orclRestClient.updateQuote();
    }

    @Override public void stop() {
        System.exit(0);
    }

    private Tile createTile(final String STOCK_TICKER_SYMBOL) {
        return TileBuilder.create()
                          .skinType(SkinType.STOCK)
                          .prefSize(TILE_SIZE, TILE_SIZE)
                          .maxValue(1000)
                          .title(STOCK_TICKER_SYMBOL)
                          .averagingPeriod(39) // update every 10min for 6.5h (trading hours NYSE)
                          .build();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
