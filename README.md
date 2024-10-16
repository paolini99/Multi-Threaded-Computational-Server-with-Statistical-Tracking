# Multi-Threaded-Computational-Server-with-Statistical-Tracking
The program implements a multi-threaded server that manages client connections, processes computation requests, and provides real-time statistics. Below is an explanation of how the program works and its main components:

### 1. Server Startup
The program begins with the `Main` class, which serves as the server's entry point. When the user starts the server by providing a port as an argument, the server begins listening for incoming connections on that port.

- If no port is specified, the program returns an error message and terminates.
- If an invalid port is provided, an error is displayed, and the server does not start.
- Once successfully started, the server creates an instance of the `Server` class.

### 2. Client Connection Management
The `Server` class handles the actual management of client connections. It uses a thread pool to handle multiple connections simultaneously, creating a number of threads equal to the number of processors available on the machine.

- When a client connects, the server accepts the connection and creates a new `ClientHandler` to manage that specific connection.
- Each connection is handled independently on a dedicated thread from the pool, allowing the server to process requests from multiple clients concurrently.

### 3. Client Request Handling
The `ClientHandler` is the component responsible for receiving and processing requests from the client. The program supports two main types of requests:

- **Statistical requests**: The client can request information about the server's performance, such as the total number of requests handled, the average response time, or the maximum response time.
- **Computation requests**: The client can send mathematical expressions that the server processes. These expressions can include variables whose values are passed by the client, and the server performs operations such as sum, minimum, maximum, or average on the results.

For each request, the `ClientHandler`:
- Records the start time of the request processing.
- Processes the request (statistical or computational).
- Increments the request count and updates the response time statistics.
- Returns the processing result and the time taken to the client.

### 4. Expression and Variable Processing
Computation requests include mathematical expressions that are processed by a computation engine (`ComputationEngine`). This engine can:

- Interpret variables and their values.
- Calculate the result of expressions for a set of variables.
- Support various calculation modes, such as minimum, maximum, average, or counting the results.

The program also provides flexible variable management through the `VariableParser` class, which converts variables into data structures that the server can easily manipulate.

### 5. Real-Time Statistics
The server gathers information on all received requests. The statistics are updated in a synchronized manner to avoid concurrency issues between different threads. The statistics include:

- The total number of requests handled.
- The average response time calculated across all requests.
- The maximum response time recorded.

These statistics can be accessed by clients through specific requests.

### 6. Connection Termination
The connection between the client and the server remains open until the client sends the "BYE" command. Once this command is received, the `ClientHandler` closes the connection, and the server continues to listen for other incoming connections.
