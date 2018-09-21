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

			//Conexion con el servidor
			Socket client = new Socket(InetAddress.getLocalHost(), 1248);
			System.out.println("Conectado al servidor");

			//Lectura de datos desde la conexion con el servidor 
			BufferedReader cIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			//Escritura de datos por la conexion con el servidor
			PrintWriter cOut = new PrintWriter(client.getOutputStream(), true);

			String comando = "";
			System.out.print("Ingrese su nombre de usuario: ");
			String user = in.readLine();
			cOut.println(user);

			//TODO En el server, mostrar lista de usuarios 
			//System.out.println("Para consultar la lista de usuarios disponibles, digite el comando: Usuarios");
			System.out.println("Para enviar un mensaje utilice el siguiente formato: <usuario de destino>:<mensaje>");
			
			while (!comando.equalsIgnoreCase("close")) {

				//Valida si hay algo disponible en consola para leer
				if (in.ready()) {
					comando = in.readLine();
					if (comando.equalsIgnoreCase("close")) {
						break;
					} else {
						//Envia la informacion a la conexion
						cOut.println(comando);
					}
				}

				//Valida si hay algo disponible en la conexion
				if (cIn.ready()) {
					System.out.println(cIn.readLine());
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
