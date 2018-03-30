import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ProxyCache {
	
	/**
	 * Socket for client connections
	 */
	private static ServerSocket socket;
	
	/**
	 * Create the ProxyCache object and the socket
	 */
	private static void init(int p) {
		try {
			socket = new ServerSocket(p);
		} catch (IOException e) {
			System.out.println("Error creating socket: " + e);
			System.exit(-1);
		}
	}
	
	private static void handle(Socket client) {
		Socket server;
		HttpRequest request;
		HttpResponse response;
		
		/* Process request. If there are any exceptions, then simply
		 * return and end this request. This unfortunately means the
		 * client will hang for a while, until it timeouts. */
		
		/* Read request */
		try {
			BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			request = new HttpRequest(fromClient);
		} catch (IOException e) {
			System.out.println("Error reading request from client: " + e);
			return;
			
		}
		
		/* Send request to server */
		try {
			/* Open socket and write request to socket */
			server = new Socket(request.getHost(), request.getPort());
			DataOutputStream toServer = new DataOutputStream(server.getOutputStream());
			toServer.writeBytes(request.toString());
		} catch (UnknownHostException e) {
			System.out.println("Unknown host: " + request.getHost());
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.out.println("Error writing request to server: " + e);
			return;
		}
		
		/* Read response and forward it to client */
		try {
			DataInputStream fromServer = new DataInputStream(server.getInputStream());
			response = new HttpResponse(fromServer);
			
			DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
			toClient.writeBytes(response.toString());
			toClient.write(response.body);
			
			/* Write response to client. First headers, then body */
			client.close();
			server.close();
		} catch (IOException e) {
			System.out.println("Error writing response to client: " + e);
		}
	}
	
	/* Read command line arguments and start proxy */
	public static void main(String args[]) {
		int myPort = 0;
		
		try {
			myPort = Integer.parseInt(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Need port number as argument");
			System.exit(-1);
		} catch (NumberFormatException e) {
			System.out.println("Please give port number as integer.");
			System.exit(-1);
		}
		
		init(myPort);
		
		Socket client;
		
		while (true) {
			try {
				client = socket.accept();
				System.out.println("Connection Established " + client);
				handle(client);
			} catch (IOException e) {
				System.out.println("Error reading request from client: " + e);
			}
		}
	}
	
}
