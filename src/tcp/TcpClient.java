package tcp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClient {

	public static void main(String[] args) throws IOException {

		InputStreamReader r = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(r);

		String hostname = "localhost";
		int port = 6789;

		Socket clientSocket = new Socket(hostname, port);

		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		while (true) {

			System.out.print("CLIENT: ");
			String command = br.readLine();

			if (command.equalsIgnoreCase("q")) {
				System.out.println("Closing client...");
				out.write(command + "\n");
				break;
			}

			out.write(command + "\n");
			out.flush();
			String message = in.readLine();
			System.out.println("SERVER: " + message);

		}

		clientSocket.close();
		in.close();
		out.close();
		System.out.println("Client closed succesfully");

	}

}
