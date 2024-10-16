import java.io.IOException;

/**
 * The Main class is the entry point of the server. It checks that a port has been provided
 * as an argument and starts the server on that port. It also handles exceptions related
 * to argument conversion and problems during server startup.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        // Check if the port is specified as an argument
        if (args.length < 1) {
            System.out.println("Usage: java -jar Server.jar <port>");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            // Handles the case where the argument is not a valid number
            System.err.println("Invalid port number. Please provide a valid integer.");
            return;
        }

        // Create and start the server on the specified port
        Server server = new Server(port);
        try {
            server.start();
        } catch (IOException e) {
            // Handle the error during connection acceptance
            System.err.printf("Cannot accept connection due to: %s%n", e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected exceptions
            System.err.printf("An unexpected error occurred: %s%n", e.getMessage());
        }
    }
}
