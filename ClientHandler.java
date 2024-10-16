import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.units.project.expression.Parser;
import it.units.project.expression.Node;
import it.units.project.expression.Variable;

/**
 * The ClientHandler class manages communication with a single client in a separate thread.
 * It receives requests, processes them, and returns results. It supports both statistical
 * and computation requests, and keeps the connection open until it receives the "BYE" command.
 */
public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private static final String QUIT_REQUEST = "BYE"; // Command to close the connection

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    /**
     * Main loop to handle client requests.
     * Continuously receives requests and processes them until the "BYE" command is received.
     */
    @Override
    public void run() {
        try {
            // Setup streams for communication with the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String request;
            // Read requests and handle null requests
            while (true) {
                request = in.readLine();

                // Check if the request is null
                if (request == null) {
                    // Print an error and continue waiting for new requests
                    System.err.println("Error: null request received from client");
                    continue;
                }

                // Close the connection only when "BYE" is received
                if (request.equals(QUIT_REQUEST)) {
                    break;
                }

                // Process the request
                long requestStartTime = System.currentTimeMillis();
                String response = handleRequest(request);

                // Increment request count
                server.incrementRequestCount();

                double elapsedTime = (System.currentTimeMillis() - requestStartTime) / 1000.0;
                server.addResponseTime(elapsedTime); // Update statistics

                // Send the response to the client with the time taken
                out.println(response + ";" + String.format("%.3f", elapsedTime));
            }
        } catch (IOException e) {
            System.err.printf("I/O error: %s%n", e.getMessage());
        } finally {
            try {
                clientSocket.close(); // Close the connection at the end
            } catch (IOException e) {
                System.err.printf("Error closing socket: %s%n", e.getMessage());
            }
            // Log the disconnection
            System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
        }

        System.out.println("Connection closed");
    }

    // Handles client requests
    private String handleRequest(String request) {
        try {
            if (request.startsWith("STAT_")) {
                return handleStatRequest(request); // Statistical requests
            } else {
                return handleComputationRequest(request); // Computation requests
            }
        } catch (Exception e) {
            return "ERR;" + e.getMessage(); // Return an error in case of exception
        }
    }

    // Handles statistical requests
    private String handleStatRequest(String request) {
        switch (request) {
            case "STAT_REQS":
                long totalRequests = server.getTotalRequests(); // Total number of requests
                return String.format("OK;%d", totalRequests);

            case "STAT_AVG_TIME":
                double averageResponseTime = server.getAverageResponseTime(); // Average response time
                return String.format("OK;%.3f", averageResponseTime);

            case "STAT_MAX_TIME":
                double maxResponseTime = server.getMaxResponseTime(); // Maximum response time
                return String.format("OK;%.3f", maxResponseTime);

            default:
                return "ERR;Invalid STAT request"; // Error message for invalid requests
        }
    }

    // Handles computation requests
    private String handleComputationRequest(String request) throws Exception {
        // The format is: "ComputationKind_ValuesKind;VariableValuesFunction;Expressions"
        String[] parts = request.split(";");
        if (parts.length != 3) {
            throw new Exception("Invalid computation request format");
        }

        // Extract computation type and values type
        String[] computationParts = parts[0].split("_");
        if (computationParts.length != 2) {
            throw new Exception("Invalid computation type format");
        }

        String computationKindStr = computationParts[0];
        String valuesKindStr = computationParts[1];
        String variableValuesFunction = parts[1];
        String expressionsStr = parts[2];

        // Parse computation type
        ComputationEngine.ComputationKind computationKind;
        try {
            computationKind = ComputationEngine.ComputationKind.valueOf(computationKindStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid computation type: " + computationKindStr, e);
        }

        // Parse variables (using the static method)
        Map<String, double[]> variableValues = VariableParser.parseVariableValues(variableValuesFunction);

        // Call static methods from TupleManager to build value tuples
        List<Map<Variable, Double>> tuples = TupleManager.buildValueTuples(variableValues, valuesKindStr);

        // Parse expressions
        List<Node> parsedExpressions = parseExpressions(expressionsStr);

        // Create an instance of ComputationEngine and perform the computation
        ComputationEngine engine = new ComputationEngine(computationKind, parsedExpressions, tuples);
        double result = engine.compute();
        return Double.toString(result); // Return the result of the computation
    }

    /**
     * Parses a string of mathematical expressions separated by commas and converts them into a list of nodes.
     * Each expression is transformed into a node representing the syntax tree of the expression.
     *
     * @param expressions The string of expressions to parse, separated by commas.
     * @return A list of {@link Node} nodes representing the parsed expressions.
     */
    private List<Node> parseExpressions(String expressions) {
        String[] expressionTokens = expressions.split(",");
        List<Node> nodes = new ArrayList<>();

        for (String token : expressionTokens) {
            try {
                Parser parser = new Parser(token);
                Node node = parser.parse();
                nodes.add(node);
            } catch (Exception e) {
                System.err.println("Error parsing expression: " + token);
                // Add any specific error handling here if needed
            }
        }

        return nodes;
    }
}
