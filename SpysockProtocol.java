package spysock;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

// Processes a single client packet.
public class SpysockProtocol {
  TcpPacket ourPacket = getTcpPacketContent();
  private Socket destSocket = null; //

  private Optional<TcpPacket> getTcpPacketContent(final byte[] packet) {
    // TODO(Amir): Extract all constants to class members.
    if (packet[0] != 4) {
      // TODO(Amir): We should understand what error to return in case the VN is
      // not 4.
      return Optional.empty();
    }

    if (packet[1] != 1) {
      // We don't support requests != CONNECT.
      return Optional.empty();
    }

    int destPort = ByteBuffer.wrap(packet, /* offset */ 2, /* length */ 2)
                             .getInt();
    InetAddress destIp = null;
    try {
      destIp = InetAddress.getByAddress(Arrays.copyOfRange(packet, /* from */
      4, /* to */ 8));
    } catch (UnknownHostException e) {
      // TODO: Handle exception.
    }
    return Optional.of(new TcpPacket(destPort, destIp));
  }

  //Function for building and sending the correct packet to the client
  private void sendToClient(byte resultCode) {

    ByteBuffer response = ByteBuffer.allocate(8);
    response.putInt(0x00); // the SOCKS4 version of the reply code - should
    // be 0.
    response.put(resultCode);

    if (destSocket != null) {
      response.putInt(destSocket.getPort()); // maybe diffrent put?
      response.put(.getAddress()); // TODO: how can i get the ip here
    } else {
      byte[] destNotWorking = {0, 0, 0, 0, 0, 0}; // we use only 0's to
      // symbols a non working\existing dest.
      response.put(destNotWorking);
    }

    try {
      DataOutputStream outStreamToClient = new DataOutputStream(destSocket
          .getOutputStream());

      outStreamToClient.write(response.array());
      outStreamToClient.flush();
    } catch (IOException e) {
      //System.err.println("Connection Error:" + e); // TODO: handel eror
    }

  }

  public State processInput(final byte[] packet) {
    State state = State.SUCCESS;
    if (destSocket == null) {
      Optional<TcpPacket> content = getTcpPacketContent(packet);
      // TODO:
      // 1. Handle errors in the content creation.
      // 2. In case there were no errors, initialize the destSocket after
      //    connecting to the dest socket.
      // 3. Return a corresponding packet to the client according to the
      //    instructions.
    } else {
      // TODO:
      // 1. Check whether the current packet contains a user name and password
      //    and act accordingly.
      // 2. Send the packet to the dest server and return its response to the
      //    client.
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
