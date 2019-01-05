package spysock;

import java.io.*;
import java.net.Socket;

// Initiates a client socket connection, including the construction of IO
// buffers, and when run, accepts and handles packets from the client using
// SpysockProtocol.
public class SpysockConnection implements Runnable {

  private Socket clientSocket = null;
  private PrintWriter outputPrinter;
  private DataInputStream inputStream;

  public SpysockConnection(Socket clientSocket) {
    this.clientSocket = clientSocket;
    try {
      this.outputPrinter = new PrintWriter(clientSocket.getOutputStream(), true);
      this.inputStream = new DataInputStream(clientSocket.getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void run() {
    SpysockProtocol protocol = new SpysockProtocol();
    try {
      while (true) {
        // TODO(Amir): Why 10
        byte[] packet = new byte[10];
        inputStream.read(packet);
        if (protocol.processInput(packet) == SpysockProtocol.State.TERMINATED) {
          break;
        }
      }
      clientSocket.close();
    } catch (IOException e) {
      // TODO(Amir): Handle e
    }
  }

}
