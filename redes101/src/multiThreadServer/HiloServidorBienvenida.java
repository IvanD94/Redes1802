package multiThreadServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HiloServidorBienvenida extends Thread {
	
	Socket cliente;
	BufferedReader lectorHS;
	PrintWriter escritorHS;
	
	public HiloServidorBienvenida(Socket solicitud) {
		super();
		cliente = solicitud;
	}
	
	@Override
	public void run() {
		
		try {
			
			lectorHS = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			escritorHS = new PrintWriter(cliente.getOutputStream(), true);
			
			String nombre = lectorHS.readLine();
			String host = lectorHS.readLine();
			
			String mensaje = "Hola " + nombre + " en " + host + ", bienvenido!!";
			escritorHS.println(mensaje);
			
		} catch (IOException e) {
			e.printStackTrace();

		}
		
	}

}
