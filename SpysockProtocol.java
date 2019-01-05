package spysock;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

// Processes a single client packet.
public class SpysockProtocol {
  private Socket destSocket = null;
  private Socket clientScoket = null;

  private static byte REQUEST_GRANTED = 0x5a;
  private static byte REQUEST_REJECTED = 0x5b;
  private static byte REQUEST_REJECTED_FAILED_TO_CONNECT = 0x5c;
  private static byte SOCKS4_PROTOCOL = 0x00;
  private static int TWO_SECONDS = 4000;

  private Optional<TcpPacket> getTcpPacketContent(final byte[] packet) {
    InetAddress destIp = null;
    // TODO(Amir): Extract all constants to class members.
    if (packet[0] != 4) {
      // TODO(Amir): We should understand what error to return in case the VN is not 4.
      return Optional.empty();
    }

    if (packet[1] != 1) {
      // We don't support requests != CONNECT.
      return Optional.empty();
    }

    int destPort = ByteBuffer.wrap(packet, /* offset */ 2, /* length */ 2)
                             .getInt();

    try {
      destIp = InetAddress.getByAddress(Arrays.copyOfRange(packet, /* from */
      4, /* to */ 8));
    } catch (UnknownHostException e) {
      // TODO: Handle exception.
    }
    return Optional.of(new TcpPacket(destPort, destIp));
  }

  //Function for building and sending the correct packet to the client.
  private void sendReplayPacket(byte resultCode, TcpPacket tcpPacket) {

    ByteBuffer response = ByteBuffer.allocate(8);
    response.putInt(0x00); // the SOCKS4 version of the reply code - should be 0.
    response.put(resultCode); // 90 or 91 or 92 or 93

    if (destSocket != null) {
      response.putShort((short) tcpPacket.getDestPort());
      response.put(tcpPacket.getDestIp().getAddress()); // TODO: how can i get the ip here
    } else {
      byte[] destNotWorking = {0, 0, 0, 0, 0, 0}; // we use only 0's to
      // symbols a non working\existing dest.
      response.put(destNotWorking);
    }
    try {
      DataOutputStream outToClient = new DataOutputStream(clientScoket.getOutputStream()); //TODO: maybe not clientSocket but destSocket
      outToClient.write(response.array());
      outToClient.flush();
    } catch (IOException e) {
      //System.err.println("Connection Error:" + e);
    }
  }

  private void relay (Socket Client, Socket Destenation) {
    byte [] data = null;
    try {
      DataOutputStream outStreamToClient = new DataOutputStream(Client
          .getOutputStream());

      DataInputStream inputStreamToClient = new DataInputStream(Destenation
              .getInputStream());

      data = new byte [150000];
      int length = 0;
      length = inputStreamToClient.read(data);

      if (length > 0) {
        outStreamToClient.write(data, 0, length);
        outStreamToClient.flush();
      }
    } catch (IOException e) {
      //System.err.println("Connection Error:" + e); // TODO: handel errors
    }

  }

  public State processInput(final byte[] packet) {
    State state = State.SUCCESS;
    if (destSocket == null) {
      Optional<TcpPacket> content = getTcpPacketContent(packet);
      if (content.isPresent()) {
        try {
          destSocket.connect(new InetSocketAddress(content.get().getDestIp(), content.get().getDestPort()));
          sendReplayPacket(REQUEST_GRANTED);
          System.out.println("Successful connection from" + clientScoket.+":"+clientScoket.getPort()+"to" +content.get().getDestIp()+":"+content.get().getDestPort());
        } catch (IOException e){
        sendReplayPacket(REQUEST_REJECTED);
          System.out.println("Connection error: while parsing request: Unsupported SOCKS protocol version (got (" + packet[0] + ")).");
         }
      }
      else {
        sendReplayPacket(REQUEST_REJECTED_FAILED_TO_CONNECT);
        System.out.println("Connection error: while parsing request: Unsupported SOCKS protocol version (got (" + packet[0] + ")).");
      }
    }
    else {
      Optional<TcpPacket> content = getTcpPacketContent(packet);
      try{
        sendReplayPacket(REQUEST_GRANTED);}
      catch (IOException e) {
        sendReplayPacket(REQUEST_REJECTED_FAILED_TO_CONNECT);
        //System.err.println("Connection Error:" + e); // TODO: handel errors
      }
      // TODO:
      // 1. Check whether the current packet contains a user name and password
      //    and act accordingly.

    }

    return state;
  }

  public enum State {SUCCESS, TERMINATED}

  private class TcpPacket {
    private int destPort;
    private InetAddress destIp;

    public TcpPacket(int destPort, InetAddress destIp) {
      this.destPort = destPort;
      this.destIp = destIp;
    }

    public int getDestPort() {
      return destPort;
    }

    public InetAddress getDestIp() {
      return destIp;
    }
  }
}
