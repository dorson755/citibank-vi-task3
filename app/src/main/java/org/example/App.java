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
    private static final String ALPHA_VANTAGE_API_KEY = "C29W07NX5HQL49UI";
    private static final String SYMBOL = "DJI"; // Dow Jones Industrial Average symbol
    private static final int QUERY_INTERVAL_MS = 5000; // Query interval in milliseconds
    private static final Queue<StockData> stockDataQueue = new LinkedList<>();

    public static void main(String[] args) {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    queryStockPrice();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, QUERY_INTERVAL_MS);

        // Keep the application running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            while (!stockDataQueue.isEmpty()) {
                StockData data = stockDataQueue.poll();
                System.out.println(data);
            }
        }));
    }

    private static void queryStockPrice() throws Exception {
        String url = String.format("https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=1min&apikey=%s", SYMBOL, ALPHA_VANTAGE_API_KEY);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject json = new JSONObject(response.toString());
        JSONObject timeSeries = json.getJSONObject("Time Series (1min)");
        String latestTime = timeSeries.keys().next();
        double price = timeSeries.getJSONObject(latestTime).getDouble("1. open");
        LocalDateTime timestamp = LocalDateTime.now();
        stockDataQueue.add(new StockData(price, timestamp));
        System.out.println("Queried at " + timestamp + ": " + price);
    }

    private static class StockData {
        private final double price;
        private final LocalDateTime timestamp;

        public StockData(double price, LocalDateTime timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "Price: " + price + ", Timestamp: " + timestamp;
        }
    }
}
