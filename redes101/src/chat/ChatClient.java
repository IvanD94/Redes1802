package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * 
 * @author Ivan Chacon
 * 
 */
public class ChatClient {

	public static void main(String[] args) {

		// Lectura de datos desde la consola.
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try {

			// Conexion con el servidor
			Socket client = new Socket(InetAddress.getLocalHost(), 1248);
			System.out.println("Conectado al servidor");

			// Lectura de datos desde la conexion con el servidor
			BufferedReader cIn = new BufferedReader(new InputStreamReader(client.getInputStream()));

			// Escritura de datos por la conexion con el servidor
			PrintWriter cOut = new PrintWriter(client.getOutputStream(), true);

			String comando = "";

			boolean valido = false;

			String user = "";
			while (!valido) {
				System.out.print("Ingrese su nombre de usuario: ");
				user = in.readLine();
				cOut.println(user);
				valido = "Valido".equals(cIn.readLine());
				if (!valido) {
					System.out.println("Usuario no válido");
				}
			}

			System.out.println("\nBienvenid@ " + user + "\n");
			System.out.println("Comandos disponibles:");
			System.out.println("\t Consultar la lista de usuarios disponibles: u");
			System.out.println("\t Realizar un broadcast a todos los usuarios: b <mensaje>");
			System.out.println("\t Enviar un mensaje privado: m <usuario de destino>: <mensaje>");
			System.out.println("\t Cerrar conexion: c\n");
			
			while (!comando.equalsIgnoreCase("close")) {

				// Valida si hay algo disponible en consola para leer
				if (in.ready()) {
					comando = in.readLine();
					if (comando.equalsIgnoreCase("close")) {
						cOut.println(comando);
						break;
					} else {
						// Envia la informacion a la conexion
						cOut.println(comando);
					}
				}

				// Valida si hay algo disponible en la conexion
				if (cIn.ready()) {
					String llegada = cIn.readLine();
					if (llegada.equals("Kill")) {
						System.out.println("El servidor se ha cerrado");
						cOut.println("c");
						break;
					} else {
						System.out.println(llegada);
					}
				}

			}

			cIn.close();
			cOut.close();
			client.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
