package multiThreadServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteBienvenida {
	
	public static void main(String[] args) {
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		Socket cliente;
		BufferedReader lectorC;
		PrintWriter escritorC;
		String nombre, respuesta, host;
		int puerto;
		
		try {
			
			cliente = new Socket("127.0.0.1", 8030);
			lectorC = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			escritorC = new PrintWriter(cliente.getOutputStream(), true);
			
			host = cliente.getLocalAddress().getHostName();
			puerto = cliente.getLocalPort();
			
			System.out.print("Digite su nombre: ");
			nombre = in.readLine();
			
			System.out.println(puerto);
			
			escritorC.println(nombre);
			escritorC.println(host + "(puerto:" + puerto + ")");
			
			respuesta = lectorC.readLine();
			System.out.println(respuesta);
			
			lectorC.close();
			escritorC.close();
			cliente.close();	
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
