package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Ivan Chacon
 *
 */
public class ChatServer {

	// Lista de usuarios disponibles
	static Set<String> usuarios;

	// Mensajes pendientes por entregar por usuario
	static Map<String, ArrayDeque<String>> mensajes;

	static Set<CharThread> hilosActivos;

	static ServerSocket server;
	static boolean enEjecucion;

	public static void main(String[] args) {

		usuarios = Collections.synchronizedSet(new HashSet<>());
		mensajes = Collections.synchronizedMap(new HashMap<>());
		hilosActivos = Collections.synchronizedSet(new HashSet<>());
		enEjecucion = true;
		
		try {

			// Servidor que va a estar pendiente de las solicitudes
			server = new ServerSocket(1248);
			System.out.println("Servidor listo para recibir solicitudes");

			while (enEjecucion) {
				// Servidor a la espera de una solicitud (cuando encuentra una solicitud
				// continua)
				Socket client = server.accept();
				System.out.println("Solicitud recibida");
				// Crea hilo que atendera la solicitud recibida
				CharThread hilo = new CharThread(client);
				// Se inicia el hilo correspondiente
				hilosActivos.add(hilo);
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

	static synchronized void desconectarUsuario(String user) {
		usuarios.remove(user);
		mensajes.remove(user);
	}

	static synchronized void crearBroadcast(String user, String mensaje) {
		for (String us : usuarios) {
			if (!us.equals(user)) {
				mensajes.get(us).add(mensaje);
			}
		}
	}

	/*
	 * Agrega sincronizadamente un usuario dado a la lista de usuaros disponibles
	 */
	static synchronized void agregarUsuario(String nombre) {
		usuarios.add(nombre);
		mensajes.put(nombre, new ArrayDeque<>());
		print(nombre + " ha iniciado sesion");
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
	 * Agrega un mensaje a la lista de mensajes pendientes correspondientes PRE: el
	 * usuario esta en linea
	 */
	static synchronized void agregarMensaje(String user, String mensaje) {
		mensajes.get(user).add(mensaje);
	}

	/*
	 * Verifica si existen mensajes pendientes de entregar para el usuario dado PRE:
	 * el usuario esta en linea
	 */
	static synchronized boolean mensajesPendientes(String user) {
		return !mensajes.get(user).isEmpty();
	}
	
	static synchronized void retirarHilo(CharThread hilo) {
		hilosActivos.remove(hilo);
	}

	/*
	 * Retorna el primer mensaje pendiente para el usuario dado PRE: el usuario
	 * tiene mensajes pendientes PRE: el usuario esta en linea
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

	static synchronized void closeAll() {
		
		print("Cerrando conexiones");
		crearBroadcast("SERVER", "Kill");

		for (CharThread hilo: hilosActivos) {
			hilo.interrupt();
		}
		
		enEjecucion = false;
		print("El servidor se ha cerrado");
	}

	/*
	 * Hilo encargado de atender las solicitudes de cada cliente
	 */
	static class CharThread extends Thread {

		Socket client;
		BufferedReader cIn;
		PrintWriter cOut;
		String user;
		boolean enEjecucion;
		
		public CharThread(Socket client) {

			this.client = client;
			enEjecucion = true;
			
			try {
				// Lector de la conexcion
				cIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
				// Escritor de la conexion
				cOut = new PrintWriter(client.getOutputStream(), true);

				boolean usuarioValido = false;

				while (!usuarioValido) {
					user = cIn.readLine();
					if (!verificarUsuario(user)) {
						usuarioValido = true;
					} else {
						cOut.println("Invalido");
					}
				}

				agregarUsuario(user);
				cOut.println("Valido");

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		public void desconectar() {
			enEjecucion = false;
		}

		@Override
		public void run() {

			// Lector de consola
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			try {

				String comando = "";

				while (enEjecucion) {

					// TODO Manejar mensajes del servidor
					// Verifica si hay algo pendiente por leer en consola
					if (in.ready()) {
						comando = in.readLine();
						if (comando.equalsIgnoreCase("close")) {
							closeAll();
						} else {
							cOut.println(comando);
						}
					}

					// Verifica si el usuario tienen mensajes pendientes por enviar
					if (mensajesPendientes(user)) {
						cOut.println(getMensaje(user));
					}

					// Verifica si hay algo por leer en la conexion
					if (cIn.ready()) {

						String entrada = cIn.readLine();

						print(user + ": a enviado " + entrada);

						char operacion = entrada.charAt(0);

						if (operacion == 'u') {
							cOut.println(usuariosDisponibles());
						} else if (operacion == 'b') {
							String mensaje = entrada.substring(2, entrada.length());
							crearBroadcast(user, user + ": " + mensaje);
							print(user + " ha enviado \"" + mensaje + "\" a todos los usuarios");
						} else if (operacion == 'm') {
							String[] valores = entrada.split(":");
							String destinatario = valores[0].substring(2, valores[0].length());
							String mensaje = valores[1];

							if (verificarUsuario(destinatario)) {
								print(user + " dice: " + mensaje + ", a: " + destinatario);
								agregarMensaje(destinatario, user + ": " + mensaje);
							} else {
								cOut.println("Usuario " + destinatario + " no encontrado");
							}

						} else if (operacion == 'c') {
							break;
						} else {
							cOut.println("Comando no reconocido");
						}

					}

				}

				desconectarUsuario(user);
				print("usuario: " + user + " se ha desconectado");

				cIn.close();
				cOut.close();
				client.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
