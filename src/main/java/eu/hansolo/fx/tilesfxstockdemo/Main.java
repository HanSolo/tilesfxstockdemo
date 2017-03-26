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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by hansolo on 23.03.17.
 */
public class Main extends Application {
    private static final List<String>            SYMBOLS;
    static {
        SYMBOLS = new ArrayList<>();
        SYMBOLS.add("IBM");
        SYMBOLS.add("ORCL");
        SYMBOLS.add("MSFT");
    }

    private static final double                  TILE_SIZE = 250;

    private              Map<String, StockQuote> quotes;
    private              Map<String, Tile>       tiles;

    private              DotMatrix               matrix;
    private              int                     color;
    private              int                     counter;
    private              int                     x;

    private              int                     textLengthInPixel;
    private              Map<String, String>     texts;

    private              long                    lastTimerCall;
    private              AnimationTimer          timer;


    // ******************** Application Lifecycle *****************************
    @Override public void init() {
        quotes = new HashMap<>();
        SYMBOLS.forEach(symbol -> quotes.put(symbol, new StockQuote(symbol)));

        tiles = new HashMap<>(quotes.size());
        quotes.forEach((symbol, quote) -> tiles.put(symbol, createTile()));

        matrix            = createMatrix();
        GridPane.setColumnSpan(matrix, quotes.size());
        color             = DotMatrix.convertToInt(TileColor.ORANGE.color);
        x                 = matrix.getCols() + 7;
        counter           = 0;
        textLengthInPixel = 0;
        texts             = new HashMap<>(quotes.size());

        lastTimerCall     = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 24_000_000l) {     // update Dot Matrix display every 24 ms
                    if (x < -textLengthInPixel) { x = matrix.getCols() + 7; }
                    counter = 0;
                    texts.forEach((symbol, text) -> {
                        color = quotes.get(symbol).getColorValue();
                        for (int i = 0 ; i < text.length() ; i++) {
                            matrix.setCharAt(text.charAt(i), x + counter * 8, 1, color);
                            counter++;
                        }
                    });
                    x--;
                    lastTimerCall = now;
                }
            }
        };

        quotes.forEach((symbol, quote) -> quote.addQuoteEventListener(e -> {
            tiles.get(symbol).setTitle(e.getStockQuote().getInfoText());
            tiles.get(symbol).setReferenceValue(e.getStockQuote().getPreviousClose());
            tiles.get(symbol).setValue(e.getStockQuote().getLastPrice());
            updateTickerText();
        }));
    }

    @Override public void start(Stage stage) {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5));
        grid.setBackground(new Background(new BackgroundFill(Tile.BACKGROUND.darker(), CornerRadii.EMPTY, Insets.EMPTY)));


        List<Tile> nodes = new ArrayList<>(tiles.values());
        for (int i = 0 ; i < nodes.size() ; i++) { grid.add(nodes.get(i), i, 0); }

        grid.add(matrix, 0, 1);

        Scene scene = new Scene(grid);

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
        textLengthInPixel = 0;
        quotes.forEach((symbol, quote) -> {
            String text = String.join("", quote.toString(), "  ");
            texts.put(symbol, text);
            textLengthInPixel += (text.length() * 8);
        });
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
                               .prefSize(quotes.size() * TILE_SIZE, TILE_SIZE * 0.3)
                               .colsAndRows(33 * quotes.size(), 9)
                               .dotOnColor(Tile.GREEN)
                               .dotOffColor(Tile.BACKGROUND.brighter())
                               .dotShape(DotShape.SQUARE)
                               .build();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
