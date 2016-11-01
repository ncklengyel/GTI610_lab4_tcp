package tcp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

	public static void main(String[] args) throws IOException {

		int port = 6789;

		System.out.print("TCP server starting...");
		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println(" ok");
		System.out.println("Listening on "+ serverSocket.getLocalPort());
		
		String message = "";

		while (true) {
			
			Socket socket = serverSocket.accept();
			//DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			message = in.readLine();
			
			System.out.println("Message: "+message + " IP: "+socket.getInetAddress() +" port: "+ socket.getPort());
			
			message = message.toUpperCase();
			
			out.write(message + "\n");
			out.flush();
			//out.close();
			
		}

	}

}
