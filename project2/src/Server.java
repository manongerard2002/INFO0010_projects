import java.io.*;
import java.net.*;

public class Server {
    private static int port = 53;
    private int timeout = 5000; // 5 secondes timeout
    ServerSocket serverSocket;
    private boolean finished = false;
    private String ownedDomain;

    public Server(String args[]) throws IOException, WrongArgumentException {
        int length = args.length;
        if (length != 1)
            throw new WrongArgumentException("Not the right amount of arguments provided !");
        ownedDomain = args[0];

        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setSoTimeout(timeout);
    }

    public static void main(String[] args) {
        try
        {
            Server server = new Server(args);
            server.getResponse();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private void getResponse() throws IOException {
        while(! isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
                String IPaddress = clientSocket.getInetAddress().toString().substring(1);
                Runnable runnable = new RunnableServer(clientSocket, ownedDomain, IPaddress);
                new Thread(runnable).start();
            }
            catch (IOException e) {
                finished = true;
                if(isStopped()) {
                    System.out.println("Server Stopped.");
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
        }
        serverSocket.close();
    }

    private boolean isStopped() {
        return finished;
    }
}
