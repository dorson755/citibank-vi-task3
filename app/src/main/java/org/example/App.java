package org.example;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class App {
    private static final String ALPHA_VANTAGE_API_KEY = "C29W07NX5HQL49UI"; // Replace with your actual API key
    private static final String SYMBOL = "DOW"; // Dow Jones Industrial Average symbol
    private static final Queue<StockData> stockDataQueue = new LinkedList<>();

    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    queryStockPrice();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 5000); // Query every 5 seconds
    }

    private static void queryStockPrice() throws Exception {
        String url = String.format("https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=1min&apikey=%s", SYMBOL, ALPHA_VANTAGE_API_KEY);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        // Check for response code
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.out.println("HTTP GET request failed with code: " + responseCode);
            BufferedReader errorStream = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorStream.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            errorStream.close();
            System.out.println("Error Response: " + errorResponse.toString());
            return;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Print the full JSON response for debugging
        System.out.println("Full JSON Response: " + response.toString());

        JSONObject json = new JSONObject(response.toString());

        // Print the keys to check available data
        System.out.println("JSON Keys: " + json.keySet());

        if (json.has("Time Series (1min)")) {
            JSONObject timeSeries = json.getJSONObject("Time Series (1min)");
            String latestTime = timeSeries.keys().next();
            double price = timeSeries.getJSONObject(latestTime).getDouble("1. open");
            LocalDateTime timestamp = LocalDateTime.now();
            stockDataQueue.add(new StockData(price, timestamp));
            System.out.println("Queried at " + timestamp + ": " + price);
        } else {
            System.out.println("Key 'Time Series (1min)' not found in the response.");
        }
    }

    private static class StockData {
        double price;
        LocalDateTime timestamp;

        StockData(double price, LocalDateTime timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "StockData{" +
                    "price=" + price +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
