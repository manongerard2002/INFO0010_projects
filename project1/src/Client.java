import java.io.*;
import java.net.*;

public class Client {
    private int port = 53;
    private int timeout = 5000; // 5 secondes timeout
    private String typeRecord = "A"; // by default A
    private String IPaddress;
    private String hostName;

    public Client(String args[]) throws WrongArgumentException {
        //assigning the arguments (args)
        int length = args.length;
        if (length < 2 || length > 3)
            throw new WrongArgumentException("Not the right amount of arguments provided !");
        
        IPaddress = args[0];
        hostName = args[1];
        if (length == 3) {
            if (! args[2].equals("A") && ! args[2].equals("TXT"))
                throw new WrongArgumentException("Wrong type of record type");
            typeRecord = args[2];
        }
    }

    public static void main(String[] args) {
        try
        {
            Client client = new Client(args);
            client.query();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void query() throws IOException {
        System.out.println("Question (NS=" + IPaddress + ", NAME=" + hostName + ", TYPE=" + typeRecord + ")");
        try {
            //Initiate a new TCP connection with a Socket
            InetAddress inetaddress = InetAddress.getByName(IPaddress);
            Socket socket = new Socket(inetaddress, port);
            OutputStream socketOut = socket.getOutputStream();
            InputStream socketIn = socket.getInputStream();
            socket.setSoTimeout(timeout);

            // Send a query in the form of a byte array
            Query request = new Query(hostName, typeRecord);
            byte[] requestBytes = request.getQuery();
            socketOut.write(requestBytes);
            socketOut.flush();

            // Retrieve the response length, as described in RFC 1035 (4.2.2 TCP usage)
            byte[] lengthBuffer = new byte[2];
            socketIn.read(lengthBuffer);

            // Convert bytes to length (data snet over the network is always big-endian)
            int length = ((lengthBuffer[0] & 0xff) << 8) | (lengthBuffer[1] & 0xff);

            // Retrieve the full response
            byte[] responseBuffer = new byte[length];
            socketIn.read(responseBuffer);
            
            socket.close();

            // Parse the response
            Response response = new Response(responseBuffer, requestBytes.length - 2, typeRecord); // -2 : the 2bytes for the size
            response.printResponse();
        }
        catch (SocketException e) {
            System.out.println("The socket could not be created");
        }
        catch (UnknownHostException e ) {
            System.out.println("Invalid IP address");
        }
        catch (SocketTimeoutException e) {
            System.out.println("Socket Timeout");
        }
    }
}
