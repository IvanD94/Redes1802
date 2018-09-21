package clientServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SaludoCliente {
	
	public static void main(String[] args) {
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.print("Digite su nombre: ");
		
		try {
			
			String nombre = in.readLine();
			
			Socket socket = new Socket(InetAddress.getLocalHost(), 1234);
			
			BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
			
			escritor.println(nombre);
			System.out.println(lector.readLine());
			
			lector.close();
			escritor.close();
			socket.close();
			
		} catch (IOException e) {
			System.out.println("Ocurrio un error");
			e.printStackTrace();
		}
		
	}

}
