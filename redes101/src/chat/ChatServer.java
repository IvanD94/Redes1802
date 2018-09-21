package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 
 * @author Ivan Chacon
 *
 */
public class ChatServer {

	static HashSet<String> usuarios;
	static HashMap<String, ArrayDeque<String>> mensajes;

	public static void main(String[] args) {

		usuarios = new HashSet<>();
		mensajes = new HashMap<>();
		ServerSocket server = null;

		try {

			server = new ServerSocket(1248);
			System.out.println("Servidor listo para recibir solicitudes");

			while (true) {
				Socket client = server.accept();
				System.out.println("Solicitud recibida");
				CharThread hilo = new CharThread(client);
				hilo.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				server.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}

	}

	static synchronized void agregarUsuario(String nombre) {
		usuarios.add(nombre);
		mensajes.put(nombre, new ArrayDeque<>());
		print("Usuarios conectados:");
		for (String user : usuarios) {
			print(user);
		}
	}

	static synchronized void print(String lin) {
		System.out.println(lin);
	}

	/*
	 * PRE: el usuario esta en linea
	 */
	static synchronized void agregarMensaje(String user, String mensaje) {
		mensajes.get(user).add(mensaje);
	}

	static synchronized boolean mensajesPendientes(String user) {
		return !mensajes.get(user).isEmpty();
	}
	
	/*
	 * PRE: el usuario tiene mensajes pendientes
	 */
	static synchronized String getMensaje(String user) {
		return mensajes.get(user).remove();
	}

	static synchronized boolean verificarUsuario(String user) {
		return usuarios.contains(user);
	}

	static class CharThread extends Thread {

		Socket client;
		BufferedReader cIn;
		PrintWriter cOut;
		String user;

		public CharThread(Socket client) {

			this.client = client;
			try {
				cIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
				cOut = new PrintWriter(client.getOutputStream(), true);
				user = cIn.readLine();
				agregarUsuario(user);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {

			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			try {

				String comando = "";

				while (!comando.equalsIgnoreCase("close")) {

					//TODO Manejar mensajes del servidor
					if (in.ready()) {
						comando = in.readLine();
						if (comando.equalsIgnoreCase("close")) {
							break;
						} else {
							cOut.println(comando);
						}
					}
					
					if (mensajesPendientes(user)) {
						cOut.println(getMensaje(user));
					}

					if (cIn.ready()) {
						String linea = cIn.readLine();
						String[] mensaje = linea.split(":");
						print(user + " dice: " + mensaje[1] + ", a: " + mensaje[0]);
						//TODO verficar existencia de usuario
						agregarMensaje(mensaje[0], user + ": " + mensaje[1]);
					}

				}

				cIn.close();
				cOut.close();
				client.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
