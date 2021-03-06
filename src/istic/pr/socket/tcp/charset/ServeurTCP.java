package istic.pr.socket.tcp.charset;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ServeurTCP {

	public static void main(String[] args) {
		// Attente des connexions sur le port 9999
		int portEcoute = 9999;
		Charset cs;
		
		try {
			cs = Charset.forName(args[0]);
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("Error charset not defined in parameters\nCharset set as: UTF-8");
		} catch(UnsupportedCharsetException e) {
			System.out.println("Unsupported charset\nCharset set as: UTF-8");
		} finally {
			cs = Charset.forName("UTF-8");
		}
		
		try {
			ServerSocket socketServeur = new ServerSocket(portEcoute);
			try {
				System.out.println(
						"Server open with ip: " + socketServeur.getInetAddress() + " port: " + socketServeur.getLocalPort());
				// Dans une boucle, pour chaque socket clientes, appeler traiterSocketCliente
				while (true) {
					Socket socketVersUnClient = socketServeur.accept();
					traiterSocketCliente(socketVersUnClient, cs);
				}
			} finally {
				socketServeur.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private static void traiterSocketCliente(Socket socketVersUnClient, Charset cs) throws IOException {
		// Cree printer et reader
		PrintWriter printer = creerPrinter(cs, socketVersUnClient);
		BufferedReader reader = creerReader(cs, socketVersUnClient);
		
		try {
			String msg;
			String name = avoirNom(reader);
			if(name == null) {
				envoyerMessage(printer, "Erreur envoi du nom invalide");
			}
			else {
				// Tant qu’il y’a un message à lire via recevoirMessage
				while((msg = recevoirMessage(reader)) != null) {
					System.out.println("Msg received: " + msg);
					// Envoyer message au client via envoyerMessage
					envoyerMessage(printer,name+"> "+ msg);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			socketVersUnClient.close();
			printer.close();
			reader.close();
		}
		// Si plus de ligne a lire fermer socket cliente
		socketVersUnClient.close();
		printer.close();
		reader.close();
	}
	//------Obsol�te, version sans Charset

/*
	public static void traiterSocketCliente(Socket socketVersUnClient) throws IOException {
		// Créer printer et reader
		PrintWriter printer = creerPrinter(socketVersUnClient);
		BufferedReader reader = creerReader(socketVersUnClient);

		// G�rer nom client
		String nom = avoirNom(reader);
		if (!nom.startsWith("NAME:")) {// si n'a pas le pr�fixe pr�vu
			envoyerMessage(printer, "nom non re�u");// TODO am�lioration: redemander un nom au client
			nom = "Anon";
		} else {
			nom = nom.substring(5);
		} // on retire le pr�fixe
		System.out.println("New user: " + nom);

		String msg;
		// Tant qu’il y’a un message à lire via recevoirMessage
		while (!(msg = recevoirMessage(reader)).equalsIgnoreCase("fin")) {
			System.out.println("Msg received: " + msg);
			// Envoyer message au client via envoyerMessage
			envoyerMessage(printer, msg);
		}

		// Si plus de ligne à lire fermer socket cliente
		socketVersUnClient.close();
	}

		public static BufferedReader creerReader(Socket socketVersUnClient) throws IOException {
		// créé un BufferedReader associé à la Socket
		return new BufferedReader(new InputStreamReader(socketVersUnClient.getInputStream()));
	}

	public static PrintWriter creerPrinter(Socket socketVersUnClient) throws IOException {
		// créé un PrintWriter associé à la Socket
		return new PrintWriter(new OutputStreamWriter(socketVersUnClient.getOutputStream()));
	}
	*/
	
	public static String avoirNom(BufferedReader reader) throws IOException {
		// Retourne le nom du client (en utilisant split de la classe String par exemple)
		try {
			String[] parts = reader.readLine().split(":");
			if(parts.length == 2) {
				if(parts[0].equals("NAME")) {
					return parts[1];
				}
				else {
					System.out.println("Error cmd NAME not found");
					return null;
				}
			}
			else {
				System.out.println("Error not cmd found");
				return null;
			}
		} catch(NullPointerException e) {
			return null;
		}
	}

	public static BufferedReader creerReader(Charset charset, Socket socketVersUnClient) throws IOException {
		// Cree un BufferedReader associe a la Socket
		return new BufferedReader(new InputStreamReader(socketVersUnClient.getInputStream(), charset));
	}

	public static PrintWriter creerPrinter(Charset charset, Socket socketVersUnClient) throws IOException {
		// Cree un PrintWriter associe a la Socket
		return new PrintWriter(new OutputStreamWriter(socketVersUnClient.getOutputStream(), charset));
	}
	
	public static String recevoirMessage(BufferedReader reader) throws IOException {
		// Recupérer une ligne
		return reader.readLine();
		// Retourner la ligne lue ou null si aucune ligne a lire.
	}

	public static void envoyerMessage(PrintWriter printer, String message) throws IOException {
		printer.println(message);
		printer.flush();
		// Envoyer le message vers le client
	}
}