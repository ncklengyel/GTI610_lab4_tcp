/**
 * Auteur: Nicolas Lengyel, Vuong Viet VU
 * Date: 1 novembre 2016
 * Dernière modification: 2 novembre 2016
 * 
 * La classe TcpServer implémente un serveur Tcp multithread.
 * 
 * Afin d'implémenter les threads au serveur, notre équipe c'est basé sur le code suivant:
 * https://www.tutorialspoint.com/javaexamples/net_multisoc.htm
 */

package tcp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;



public class TcpServer implements Runnable {

	//le socket pour connexion entre client et serveur
	Socket clientSocket;

	//Constructeur privé
	private TcpServer(Socket socket) {
		clientSocket = socket;
	}
	
	//Le main de la classe
	public static void main(String[] args) throws IOException {
		
		//On démarre le serveur TCP sur le port passé en paramètre (6789)
		ServerSocket serverSocket = new ServerSocket(6789);
		
		System.out.println("TCP server is now online");
		
		//Boucle infini
		while(true){
			
			//on accepte les connexions clientes au serveur TCP
			Socket socket = serverSocket.accept();
			
			//Lorsqu'un client se connecte au créer une nouvelle instance du serveur dans un thread
			new Thread(new TcpServer(socket)).start();
			
		}	

	}

	@Override 
	//Lorsque .start() est applé, ceci est la méthode qui est executer dans le nouveau thread
	public void run() {
		
		System.out.println("New client connexion on thread "+ Thread.currentThread().getId());
		
		//Initialisation de PrintWriter et BufferedReader
		PrintWriter out = null;
		BufferedReader in = null;
		
		try {
			//instanciation du PrintWriter pour qu'il puisse envoyer des données au client 
			out = new PrintWriter(clientSocket.getOutputStream());
			
			//instanciation du BufferedReader pour qu'il puisse recevoir des données du client 
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//Boucle infini
		while (true) {

		//message recu du client, on l'initialise
		String message = "";
		
		try {
			
			//lecture du message envoyé par le client
			message = in.readLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		//si le message n'est pas null
		if(message!=null){
		
		//On affiche à la console le message recu du client, son adresse IP et son Port
		System.out
				.println("Message: " + message + " IP: " + clientSocket.getInetAddress() + " port: " + clientSocket.getPort() + " Thread ID: " + Thread.currentThread().getId());

		//On transforme le message pour qu'il soit en majuscule
		message = message.toUpperCase();

		//On envoie les message transformé au client
		out.write(message + "\n");
		
		//On flush le stream d'écriture
		out.flush();
		
		}else{
			//affiche client deconnection sur le thread ID
			System.out.println("Client deconnexion on thread " + Thread.currentThread().getId());
			
			//on sort de la boucle infini
			break;
		}
		
		}

		
	}

}
