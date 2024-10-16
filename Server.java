import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server class that handles multiple client connections using a thread pool.
 * It uses a thread pool with a maximum number of threads equal to the number of processors available
 * on the machine. The server also collects statistics on requests and response times.
 */
public class Server {
    private final int port;
    private final ExecutorService threadPool; // Thread pool to handle requests
    private final int availableProcessors; // Field for the number of available processors
    private long totalRequests = 0; // Total number of handled requests
    private double totalResponseTime = 0; // Accumulated total response time
    private double maxResponseTime = 0; // Maximum response time

    public Server(int port) {
        this.port = port;
        // Save the number of available processors as a field
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        // Create a thread pool with a maximum number of threads equal to the available processors
        this.threadPool = Executors.newFixedThreadPool(availableProcessors);
    }

    // Starts the server and accepts connections
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                try {
                    // Accept a new client connection
                    Socket socket = serverSocket.accept();
                    System.out.println("New connection from client: " + socket.getRemoteSocketAddress());

                    // Handle the client using the thread pool
                    threadPool.submit(new ClientHandler(socket, this));
                } catch (IOException e) {
                    System.err.printf("Cannot accept connection due to %s%n", e.getMessage());
                }
            }
        }
    }

    // Increments the count of received requests
    public synchronized void incrementRequestCount() {
        totalRequests++;
    }

    // Adds the response time to the accumulated total and updates the maximum
    public synchronized void addResponseTime(double responseTime) {
        totalResponseTime += responseTime;
        if (responseTime > maxResponseTime) {
            maxResponseTime = responseTime;
        }
    }

    // Returns the total number of requests
    public synchronized long getTotalRequests() {
        return totalRequests;
    }

    // Calculates and returns the average response time
    public synchronized double getAverageResponseTime() {
        if (totalRequests == 0) {
            return 0.0;
        }
        return totalResponseTime / totalRequests;
    }

    // Returns the maximum response time
    public synchronized double getMaxResponseTime() {
        return maxResponseTime;
    }
}

