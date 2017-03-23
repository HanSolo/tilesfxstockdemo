package eu.hansolo.fx.tilesfxstockdemo;

import eu.hansolo.fx.dotmatrix.DotMatrix;
import eu.hansolo.fx.dotmatrix.DotMatrixBuilder;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.Tile.TileColor;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.util.Locale;


/**
 * Created by hansolo on 23.03.17.
 */
public class Main extends Application {
    private static final double         TILE_SIZE         = 500;
    private static final String         ORCL_STOCK_SYMBOL = "ORCL";
    private static final String         MSFT_STOCK_SYMBOL = "MSFT";

    private              RestClient     orclRestClient;
    private              Tile           orclStockTile;
    private              StockQuote     orclStockQuote;

    private              RestClient     msftRestClient;
    private              Tile           msftStockTile;
    private              StockQuote     msftStockQuote;

    private              DotMatrix      matrix;
    private              int            color = DotMatrix.convertToInt(TileColor.ORANGE.color);
    private              int            x;
    private              String         text;
    private              int            textLength;
    private              int            textLengthInPixel;

    private              long           lastStockCall;
    private              long           lastTimerCall;
    private              AnimationTimer timer;


    // ******************** Application Lifecycle *****************************
    @Override public void init() {
        orclRestClient = new RestClient(ORCL_STOCK_SYMBOL);
        orclStockTile  = createTile();
        Task<JSONObject> orclTask = orclRestClient.createTask(ORCL_STOCK_SYMBOL);
        orclTask.setOnSucceeded(event -> {
            orclStockQuote = new StockQuote((JSONObject) event.getSource().getValue());
            orclStockTile.setTitle(new StringBuilder(orclStockQuote.getSymbol()).append(" (").append(orclStockQuote.getName()).append(")").toString());
            orclStockTile.setReferenceValue(orclStockQuote.getPreviousClose());
        });
        new Thread(orclTask).start();

        msftRestClient = new RestClient(MSFT_STOCK_SYMBOL);
        msftStockTile  = createTile();
        Task<JSONObject> msftTask = msftRestClient.createTask(MSFT_STOCK_SYMBOL);
        msftTask.setOnSucceeded(event -> {
            msftStockQuote = new StockQuote((JSONObject) event.getSource().getValue());
            msftStockTile.setTitle(new StringBuilder(msftStockQuote.getSymbol()).append(" (").append(msftStockQuote.getName()).append(")").toString());
            msftStockTile.setReferenceValue(msftStockQuote.getPreviousClose());
        });
        new Thread(msftTask).start();

        matrix = createMatrix();
        GridPane.setColumnSpan(matrix, 2);

        x                 = matrix.getCols() + 7;
        text              = "";
        textLength        = text.length();
        textLengthInPixel = textLength * 8;

        lastStockCall = System.nanoTime();
        lastTimerCall = lastStockCall;
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 24_000_000l) {     // update Dot Matrix display every 24 ms
                    if (x < -textLengthInPixel) { x = matrix.getCols() + 7; }
                    for (int i = 0 ; i < textLength ; i++) {
                        if (text.charAt(i) == 79) { color = orclStockQuote.getColorValue(); }
                        if (text.charAt(i) == 77) { color = msftStockQuote.getColorValue(); }
                        matrix.setCharAt(text.charAt(i), x + i * 8, 1, color);
                    }
                    x--;
                    lastTimerCall = now;
                }
                if (now > lastStockCall + 60_000_000_000l) { // update stock quotes every 1 min
                    orclRestClient.updateQuote();
                    msftRestClient.updateQuote();
                    lastStockCall = now;
                }
            }
        };

        orclRestClient.quoteProperty().addListener(o -> {
            orclStockQuote.update(orclRestClient.getQuote());
            orclStockTile.setValue(orclStockQuote.getLastPrice());
            updateTickerText();
        });

        msftRestClient.quoteProperty().addListener(o -> {
            msftStockQuote.update(msftRestClient.getQuote());
            msftStockTile.setValue(msftStockQuote.getLastPrice());
            updateTickerText();
        });
    }

    @Override public void start(Stage stage) {
        GridPane pane = new GridPane();
        pane.setHgap(5);
        pane.setVgap(5);
        pane.setPadding(new Insets(5));
        pane.setBackground(new Background(new BackgroundFill(Tile.BACKGROUND.darker(), CornerRadii.EMPTY, Insets.EMPTY)));
        pane.add(orclStockTile, 0, 0);
        pane.add(msftStockTile, 1, 0);
        pane.add(matrix, 0, 1);

        Scene scene = new Scene(pane);

        stage.setTitle("TilesFX Stock Demo");
        stage.setScene(scene);
        stage.show();

        // Initial update of quotes
        orclRestClient.updateQuote();
        msftRestClient.updateQuote();

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }


    // ******************** Methods *******************************************
    private void updateTickerText() {
        text = new StringBuilder().append(ORCL_STOCK_SYMBOL).append(format(orclStockQuote.getLastPrice())).append(" ").append(format(orclStockQuote.getChange()))
                                  .append("  ")
                                  .append(MSFT_STOCK_SYMBOL).append(format(msftStockQuote.getLastPrice())).append(" ").append(format(msftStockQuote.getChange()))
                                  .toString();
        textLength        = text.length();
        textLengthInPixel = textLength * 8;
    }

    private Tile createTile() {
        return TileBuilder.create()
                          .skinType(SkinType.STOCK)
                          .prefSize(TILE_SIZE, TILE_SIZE)
                          .averagingPeriod(120)
                          .animated(false)
                          .build();
    }

    private DotMatrix createMatrix() {
        return DotMatrixBuilder.create()
                               .prefSize(2 * TILE_SIZE, TILE_SIZE * 0.2)
                               .colsAndRows(100, 9)
                               .dotOnColor(Tile.GREEN)
                               .dotOffColor(Tile.BACKGROUND.brighter())
                               .build();
    }

    private String format(final double VALUE) { return String.format(Locale.US, " %.2f", VALUE); }


    public static void main(String[] args) {
        launch(args);
    }
}
