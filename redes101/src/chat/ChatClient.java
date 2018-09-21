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

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try {

			Socket client = new Socket(InetAddress.getLocalHost(), 1248);
			
			System.out.println("Conectado al servidor");

			BufferedReader cIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter cOut = new PrintWriter(client.getOutputStream(), true);

			String comando = "";
			System.out.print("Ingrese su nombre de usuario: ");
			String user = in.readLine();
			cOut.println(user);

			while (!comando.equalsIgnoreCase("close")) {

				if (in.ready()) {
					comando = in.readLine();
					if (comando.equalsIgnoreCase("close")) {
						break;
					} else {
						cOut.println(comando);
					}
				}

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

	}

}
