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

	//Lista de usuarios disponibles
	static HashSet<String> usuarios;
	
	//Mensajes pendientes por entregar por usuario
	static HashMap<String, ArrayDeque<String>> mensajes;

	public static void main(String[] args) {

		usuarios = new HashSet<>();
		mensajes = new HashMap<>();
		ServerSocket server = null;

		try {

			//Servidor que va a estar pendiente de las solicitudes
			server = new ServerSocket(1248);
			System.out.println("Servidor listo para recibir solicitudes");

			while (true) {
				//Servidor a la espera de una solicitud (cuando encuentra una solicitud continua)
				Socket client = server.accept();
				System.out.println("Solicitud recibida");
				//Crea hilo que atendera la solicitud recibida
				CharThread hilo = new CharThread(client);
				//Se inicia el hilo correspondiente
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

	/*
	 * Agrega sincronizadamente un usuario dado a la lista de usuaros disponibles
	 */
	static synchronized void agregarUsuario(String nombre) {
		usuarios.add(nombre);
		mensajes.put(nombre, new ArrayDeque<>());
	}
	
	/*
	 * Retorna una cadena con la lista de los usuarios disponibles
	 */
	static synchronized String usuariosDisponibles() {
		String disponibles = "";
		
		disponibles += "Usuarios conectados:\n";
		
		for (String user : usuarios) {
			disponibles += user + "\n";
		}
		
		return disponibles;
	}

	/*
	 * Imprime sincronicamente los mensajes en la consola del servidor
	 */
	static synchronized void print(String lin) {
		System.out.println(lin);
	}

	/*
	 * Agrega un mensaje a la lista de mensajes pendientes correspondientes
	 * PRE: el usuario esta en linea
	 */
	static synchronized void agregarMensaje(String user, String mensaje) {
		mensajes.get(user).add(mensaje);
	}

	/*
	 * Verifica si existen mensajes pendientes de entregar para el usuario dado
	 * PRE: el usuario esta en linea
	 */
	static synchronized boolean mensajesPendientes(String user) {
		return !mensajes.get(user).isEmpty();
	}
	
	/*
	 * Retorna el primer mensaje pendiente para el usuario dado 
	 * PRE: el usuario tiene mensajes pendientes
	 * PRE: el usuario esta en linea
	 */
	static synchronized String getMensaje(String user) {
		return mensajes.get(user).remove();
	}

	/*
	 * Verifica si el usuario se encuentra en linea
	 */
	static synchronized boolean verificarUsuario(String user) {
		return usuarios.contains(user);
	}

	/*
	 * Hilo encargado de atender las solicitudes de cada cliente
	 */
	static class CharThread extends Thread {

		Socket client;
		BufferedReader cIn;
		PrintWriter cOut;
		String user;

		public CharThread(Socket client) {

			this.client = client;
			try {
				//Lector de la conexcion
				cIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
				//Escritor de la conexion
				cOut = new PrintWriter(client.getOutputStream(), true);
				user = cIn.readLine();
				agregarUsuario(user);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {

			// Lector de consola
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			try {

				String comando = "";

				while (!comando.equalsIgnoreCase("close")) {

					//TODO Manejar mensajes del servidor
					//Verifica si hay algo pendiente por leer en consola
					if (in.ready()) {
						comando = in.readLine();
						if (comando.equalsIgnoreCase("close")) {
							break;
						} else {
							cOut.println(comando);
						}
					}
					
					//Verifica si el usuario tienen mensajes pendientes por enviar
					if (mensajesPendientes(user)) {
						cOut.println(getMensaje(user));
					}

					//Verifica si hay algo por leer en la conexion
					if (cIn.ready()) {
						String linea = cIn.readLine();
						String[] mensaje = linea.split(":");
						print(user + " dice: " + mensaje[1] + ", a: " + mensaje[0]);
						//TODO verficar existencia de usuario
						//TODO mostrar lista de usuarios
						//TODO manejar excepcion usuario no existe
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
