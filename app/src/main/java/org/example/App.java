package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

public class App extends Application {
    private static final String ALPHA_VANTAGE_API_KEY = "C29W07NX5HQL49UI";
    private static final String STOCK_SYMBOL = "DOW";
    private static final String API_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=" + STOCK_SYMBOL + "&interval=1min&apikey=" + ALPHA_VANTAGE_API_KEY;

    private XYChart.Series<Number, Number> series = new XYChart.Series<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Live Stock Price Chart");

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Stock Price");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Stock Price Over Time");

        series.setName("Stock Price");
        lineChart.getData().add(series);

        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.show();

        // Timer to query stock price every 5 seconds
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                queryStockPrice();
            }
        }, 0, 5000);
    }

    private void queryStockPrice() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject timeSeries = jsonResponse.getJSONObject("Time Series (1min)");

            // Extract the most recent data point
            String mostRecentTime = timeSeries.keys().next();
            JSONObject mostRecentData = timeSeries.getJSONObject(mostRecentTime);
            double price = mostRecentData.getDouble("1. open");

            // Update chart
            javafx.application.Platform.runLater(() -> {
                series.getData().add(new XYChart.Data<>(Instant.now().getEpochSecond(), price));
                if (series.getData().size() > 100) {
                    series.getData().remove(0); // Keep only the last 100 data points
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
