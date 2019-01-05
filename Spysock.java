package spysock;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Spysock {
  final static int MAX_NUM_OF_CLIENTS = 20;
  final static int NEW_CLIENTS_PORT = 8080;

  private static void processNewClients(Executor executor) {
    try (ServerSocket serverSocket = new ServerSocket(NEW_CLIENTS_PORT)) {
      while (true) {
        Socket clientSocket = serverSocket.accept();
        executor.execute(new SpysockConnection(clientSocket));
      }
    } catch (IOException e) {
      // TODO(Amir): Handle gracefully the case where we can't listen on
      // NEW_CLIENTS_PORT port (for example, if the port is already being used).
    }
  }

  public static void main(String[] unusedArgs) {
    Executor executor = Executors.newFixedThreadPool(MAX_NUM_OF_CLIENTS);
    processNewClients(executor);
  }
}
