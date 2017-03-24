package eu.hansolo.fx.tilesfxstockdemo;

import eu.hansolo.fx.dotmatrix.DotMatrix;
import eu.hansolo.fx.dotmatrix.DotMatrix.DotShape;
import eu.hansolo.fx.dotmatrix.DotMatrixBuilder;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.Tile.TileColor;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Locale;


/**
 * Created by hansolo on 23.03.17.
 */
public class Main extends Application {
    private static final double                  TILE_SIZE         = 500;
    private static final String                  ORCL_STOCK_SYMBOL = "ORCL";
    private static final String                  IBM_STOCK_SYMBOL  = "IBM";

    private              Tile                    orclStockTile;
    private              StockQuote              orclStockQuote;

    private              Tile                    ibmStockTile;
    private              StockQuote              ibmStockQuote;

    private              DotMatrix               matrix;
    private              int                     color = DotMatrix.convertToInt(TileColor.ORANGE.color);
    private              int                     x;
    private              String                  text;
    private              int                     textLength;
    private              int                     textLengthInPixel;

    private              long                    lastTimerCall;
    private              AnimationTimer          timer;


    // ******************** Application Lifecycle *****************************
    @Override public void init() {
        orclStockQuote = new StockQuote(ORCL_STOCK_SYMBOL);
        ibmStockQuote  = new StockQuote(IBM_STOCK_SYMBOL);

        orclStockTile  = createTile();
        ibmStockTile  = createTile();

        matrix = createMatrix();
        GridPane.setColumnSpan(matrix, 2);

        x                 = matrix.getCols() + 7;
        text              = "";
        textLength        = text.length();
        textLengthInPixel = textLength * 8;

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 24_000_000l) {     // update Dot Matrix display every 24 ms
                    if (x < -textLengthInPixel) { x = matrix.getCols() + 7; }
                    for (int i = 0 ; i < textLength ; i++) {
                        if (text.charAt(i) == 79) { color = orclStockQuote.getColorValue(); }
                        if (text.charAt(i) == 73) { color = ibmStockQuote.getColorValue(); }
                        matrix.setCharAt(text.charAt(i), x + i * 8, 1, color);
                    }
                    x--;
                    lastTimerCall = now;
                }
            }
        };

        orclStockQuote.addQuoteEventListener(e -> {
            orclStockTile.setReferenceValue(e.getStockQuote().getPreviousClose());
            orclStockTile.setValue(e.getStockQuote().getLastPrice());
            updateTickerText();
        });


        ibmStockQuote.addQuoteEventListener(e -> {
            ibmStockTile.setReferenceValue(e.getStockQuote().getPreviousClose());
            ibmStockTile.setValue(e.getStockQuote().getLastPrice());
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
        pane.add(ibmStockTile, 1, 0);
        pane.add(matrix, 0, 1);

        Scene scene = new Scene(pane);

        stage.setTitle("TilesFX Stock Demo");
        stage.setScene(scene);
        stage.show();

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }


    // ******************** Methods *******************************************
    private void updateTickerText() {
        text = new StringBuilder().append(orclStockQuote.toString())
                                  .append("  ")
                                  .append(ibmStockQuote.toString())
                                  .toString();
        textLength        = text.length();
        textLengthInPixel = textLength * 8;
    }

    private Tile createTile() {
        return TileBuilder.create()
                          .skinType(SkinType.STOCK)
                          .prefSize(TILE_SIZE, TILE_SIZE)
                          .maxValue(1000)
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
                               .dotShape(DotShape.SQUARE)
                               .build();
    }

    private String format(final double VALUE) { return String.format(Locale.US, " %.2f", VALUE); }


    public static void main(String[] args) {
        launch(args);
    }
}
