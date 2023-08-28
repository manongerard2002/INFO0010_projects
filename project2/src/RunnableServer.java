//import java.lang.*;
import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;

public class RunnableServer implements Runnable {
    private Socket clientSocket;
    private int timeout = 5000; // 5 secondes timeout
    private String ownedDomain, IPaddress;

    RunnableServer(Socket clientSocket, String ownedDomain, String IPaddress) throws IOException {
        this.clientSocket = clientSocket;
        this.ownedDomain = ownedDomain;
        this.IPaddress = IPaddress;
        clientSocket.setSoTimeout(timeout);
    }

    @Override
    public void run() {
        try {
            InputStream socketIn = clientSocket.getInputStream();
            OutputStream socketOut = clientSocket.getOutputStream();

            // Retrieve the request length, as described in RFC 1035 (4.2.2 TCP usage)
            byte[] lengthBuffer = new byte[2];
            socketIn.read(lengthBuffer);

            // Convert bytes to length (data snet over the network is always big-endian)
            int length = ((lengthBuffer[0] & 0xff) << 8) | (lengthBuffer[1] & 0xff);

            // Retrieve the full response
            byte[] requestBuffer = new byte[length];
            socketIn.read(requestBuffer);

            // Parse the request
            Request request = new Request(requestBuffer, length, ownedDomain);

            // Send the dns response
            Response response = new Response(request, IPaddress, length);
            if (! response.wrongDomain()) {
                socketOut.write(response.getResponse());
                socketOut.flush();
            }
            clientSocket.close();
        }
        catch(IOException e)
        {
            throw new RuntimeException("Error in thread");
        }
    }
}