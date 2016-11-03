/**
 * Auteur: Nicolas Lengyel, Vuong Viet VU
 * Date: 1 novembre 2016
 * Dernière modification: 2 novembre 2016
 * 
 * La classe TcpClient implémente un client Tcp.
 * 
 */

package tcp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClient {

	public static void main(String[] args) throws IOException {

		// instanciation du InputStreamReader et BufferedReader pour lire les
		// caratères envoie a la console
		InputStreamReader r = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(r);

		// l'adresse de destination du server et son numéro de port
		String hostname = "localhost";
		int port = 6789;

		// Connexion au serveur Tcp
		Socket clientSocket = new Socket(hostname, port);

		// instanciation du PrintWriter et BufferedReader pour recevoir et
		// envoyer des données au serveur
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		// Boucle infini
		while (true) {

			// Le prompt
			System.out.print("CLIENT: ");

			// on lit ce que le client a ecrit à la console
			String command = br.readLine();

			// si le caratere q ou Q est lu
			if (command.equalsIgnoreCase("q")) {
				System.out.println("Closing client...");

				// on sort de la boucle infini
				break;
				
			//sinon
			} else {
				
				//on envoi le message au serveur et on flush le stream
				out.write(command + "\n");
				out.flush();
				
				//On lit la message envoyer par le serveur
				String message = in.readLine();
				
				//On affiche le message recu
				System.out.println("SERVER: " + message);

			}

		}

		//On ferme le socket du client, les Bufferedreader et le Printreader
		clientSocket.close();
		in.close();
		out.close();
		br.close();
		
		//On affiche un message de confirmation de fermeture du client
		System.out.println("Client closed successfully");

	}

}
