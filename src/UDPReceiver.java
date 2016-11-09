import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.NClob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.print.attribute.standard.Severity;

/**
 * Cette classe permet la reception d'un paquet UDP sur le port de reception
 * UDP/DNS. Elle analyse le paquet et extrait le hostname
 * 
 * Il s'agit d'un Thread qui ecoute en permanance pour ne pas affecter le
 * deroulement du programme
 * 
 * @author Max
 *
 */

public class UDPReceiver extends Thread {
	/**
	 * Les champs d'un Packet UDP -------------------------- En-tete (12
	 * octects) Question : l'adresse demande Reponse : l'adresse IP Autorite :
	 * info sur le serveur d'autorite Additionnel : information supplementaire
	 */

	/**
	 * Definition de l'En-tete d'un Packet UDP
	 * --------------------------------------- Identifiant Parametres QDcount
	 * Ancount NScount ARcount
	 * 
	 * L'identifiant est un entier permettant d'identifier la requete.
	 * parametres contient les champs suivant : QR (1 bit) : indique si le
	 * message est une question (0) ou une reponse (1). OPCODE (4 bits) : type
	 * de la requete (0000 pour une requete simple). AA (1 bit) : le serveur qui
	 * a fourni la reponse a-t-il autorite sur le domaine? TC (1 bit) : indique
	 * si le message est tronque. RD (1 bit) : demande d'une requete recursive.
	 * RA (1 bit) : indique que le serveur peut faire une demande recursive.
	 * UNUSED, AD, CD (1 bit chacun) : non utilises. RCODE (4 bits) : code de
	 * retour. 0 : OK, 1 : erreur sur le format de la requete, 2: probleme du
	 * serveur, 3 : nom de domaine non trouve (valide seulement si AA), 4 :
	 * requete non supportee, 5 : le serveur refuse de repondre (raisons de
	 * s�ecurite ou autres). QDCount : nombre de questions. ANCount, NSCount,
	 * ARCount : nombre d�entrees dans les champs �Reponse�, Autorite,
	 * Additionnel.
	 */

	protected final static int BUF_SIZE = 1024;
	protected String SERVER_DNS = null;// serveur de redirection (ip)
	protected int portRedirect = 53; // port de redirection (par defaut)
	protected int port; // port de r�ception
	private String adrIP = null; // bind ip d'ecoute
	private String DomainName = "none";
	private String DNSFile = null;
	private boolean RedirectionSeulement = false;

	private int qr;
	private int opcode;
	private int idRequest;

	private class ClientInfo { // quick container
		public String client_ip = null;
		public int client_port = 0;

		public ClientInfo(String ip, int port) {
			client_ip = ip;
			client_port = port;
		}

		public String getClientIp() {
			return client_ip;
		}

		public int getClientPort() {
			return client_port;
		}

	}

	private HashMap<Integer, ClientInfo> Clients = new HashMap<>();

	private boolean stop = false;

	public UDPReceiver() {
	}

	public UDPReceiver(String SERVER_DNS, int Port) {
		this.SERVER_DNS = SERVER_DNS;
		this.port = Port;
	}

	public void setport(int p) {
		this.port = p;
	}

	public void setRedirectionSeulement(boolean b) {
		this.RedirectionSeulement = b;
	}

	public String gethostNameFromPacket() {
		return DomainName;
	}

	public String getAdrIP() {
		return adrIP;
	}

	private void setAdrIP(String ip) {
		adrIP = ip;
	}

	public String getSERVER_DNS() {
		return SERVER_DNS;
	}

	public void setSERVER_DNS(String server_dns) {
		this.SERVER_DNS = server_dns;
	}

	public void setDNSFile(String filename) {
		DNSFile = filename;
	}

	public void run() {
		try {
			DatagramSocket serveur = new DatagramSocket(this.port); // *Creation
																	// d'un
																	// socket
																	// UDP

			// *Boucle infinie de recpetion
			while (!this.stop) {
				byte[] buff = new byte[0xFF];
				DatagramPacket paquetRecu = new DatagramPacket(buff, buff.length);
				//System.out.println("Serveur DNS  " + serveur.getLocalAddress() + "  en attente sur le port: "
						//+ serveur.getLocalPort());

				// *Reception d'un paquet UDP via le socket
				serveur.receive(paquetRecu);

				//System.out.println("paquet recu du  " + paquetRecu.getAddress() + "  du port: " + paquetRecu.getPort());

				// *Creation d'un DataInputStream ou ByteArrayInputStream pour
				// manipuler les bytes du paquet

				ByteArrayInputStream tabInputStream = new ByteArrayInputStream(paquetRecu.getData());

				DNSpacket dnsPacket = new DNSpacket(tabInputStream);

				// ****** Dans le cas d'un paquet requete *****
				if (dnsPacket.getQr() == DNSpacket.REQUETE) {

					ClientInfo client = new ClientInfo(paquetRecu.getAddress().getHostAddress(), paquetRecu.getPort());

					Clients.put(dnsPacket.getId(), client);

					if (RedirectionSeulement) {
						dnsPacket.printInfo();
						System.out.println("Redirection...");
						UDPSender udpSender = new UDPSender(SERVER_DNS, 53, serveur);
						udpSender.SendPacketNow(paquetRecu);// send to google

					} else {
						// TODO
						List<String> listAdresse = new QueryFinder(DNSFile).StartResearch(dnsPacket.getqName());

						if (listAdresse.isEmpty()) {
							//dnsPacket.printInfo();
							UDPSender udpSender = new UDPSender(SERVER_DNS, 53, serveur);
							udpSender.SendPacketNow(paquetRecu);// send to
																// google
						} else {
							UDPAnswerPacketCreator udpAnswerPacketCreator = UDPAnswerPacketCreator.getInstance();
							byte[] reponseArray = udpAnswerPacketCreator.CreateAnswerPacket(buff, listAdresse);
							DatagramPacket sendToclient = new DatagramPacket(reponseArray, reponseArray.length);

							int idDns = dnsPacket.getId();
							int portClient = Clients.get(idDns).client_port;
							String adresseClient = Clients.get(idDns).client_ip;

							UDPSender udpSender = new UDPSender(adresseClient, portClient, serveur);
							udpSender.SendPacketNow(sendToclient);
						}
					}

				} else if (dnsPacket.getQr() == DNSpacket.REPONSE) {

					if (dnsPacket.getrDLength() == 4) {
						System.out.println();
						System.out.println("######### REPONSE DNS #########");
						dnsPacket.printInfo();
						new AnswerRecorder(DNSFile).StartRecord(dnsPacket.getqName(), dnsPacket.getRdata());
						
						int idDns = dnsPacket.getId();
						int portClient = Clients.get(idDns).client_port;
						String adresseClient = Clients.get(idDns).client_ip;

						UDPSender udpSender = new UDPSender(adresseClient, portClient, serveur);
						udpSender.SendPacketNow(paquetRecu);
						Clients.remove(idDns);
						

					}else{
						System.out.println("\nREPONSE INVALIDE POUR: "+dnsPacket.getqName()+"\n");
					}


				}

				// *Sauvegarde de l'adresse, du port et de l'identifiant de la
				// requete

				// *Si le mode est redirection seulement
				// *Rediriger le paquet vers le serveur DNS
				// *Sinon
				// *Rechercher l'adresse IP associe au Query Domain name
				// dans le fichier de correspondance de ce serveur

				// UDPSender sender = new UDPSender(redirectionIp, portRedirect,
				// null);
				// List<String> listeAdresse = null;
				// if (RedirectionSeulement) {
				// sender.SendPacketNow(paquetRecu);
				// }else{
				// QueryFinder finder = new QueryFinder(DNSFile);
				// listeAdresse = finder.StartResearch(DomainName);
				// }

				// *Si la correspondance n'est pas trouvee
				// *Rediriger le paquet vers le serveur DNS
				// *Sinon
				// *Creer le paquet de reponse a l'aide du
				// UDPAnswerPaquetCreator
				// *Placer ce paquet dans le socket
				// *Envoyer le paquet

				// if (listeAdresse.size()==0) {
				// sender.SendPacketNow(paquetRecu);
				// }else{
				// //crééer paquet!!!!!!!!
				// //TODOOOOOO
				// }

				// ****** Dans le cas d'un paquet reponse *****
				// *Lecture du Query Domain name, a partir du 13 byte

				// *Passe par dessus Type et Class

				// *Passe par dessus les premiers champs du ressource record
				// pour arriver au ressource data qui contient l'adresse IP
				// associe
				// au hostname (dans le fond saut de 16 bytes)

				// *Capture de ou des adresse(s) IP (ANCOUNT est le nombre
				// de r�ponses retourn�es)

				// *Ajouter la ou les correspondance(s) dans le fichier DNS
				// si elles ne y sont pas deja

				// *Faire parvenir le paquet reponse au demandeur original,
				// ayant emis une requete avec cet identifiant
				// *Placer ce paquet dans le socket
				// *Envoyer le paquet
			}
			// serveur.close(); //closing server
		} catch (Exception e) {
			System.err.println("Probl�me � l'ex�cution :");
			e.printStackTrace(System.err);
		}
	}
	
}
