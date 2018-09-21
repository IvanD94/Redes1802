package clientServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SaludoServer {

	public static void main(String[] args) {
		
		ServerSocket servidor = null;
		int i = 1;
		try {
			
			servidor = new ServerSocket(1234);
			
			while (true) {
				System.out.println("Esperando...");
				Socket conexion = servidor.accept();
				System.out.println("Solicitud encontrada");
				BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
				PrintWriter escritor = new PrintWriter(conexion.getOutputStream(), true);
				System.out.println("Solicitud " + i + " atendida");
				String nombre = lector.readLine();
				escritor.println("Buen dia " + nombre);
				lector.close();
				escritor.close();
				conexion.close();
				i++;
			}
			
		} catch (IOException e) {
			System.out.println("Ocurrio excepción");
			e.printStackTrace();
		} finally {
			try {
				servidor.close();
			} catch (IOException e2) {
				System.out.println("Error cerrando servidor");
			}
		}

	}

}
