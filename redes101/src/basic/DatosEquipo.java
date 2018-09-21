package basic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DatosEquipo {

	static char claseIP(byte[] ip) {
		int hightByte = 0xff & ip[0];

		char type = ' ';

		if (hightByte < 128) {
			type = 'A';
		} else if (hightByte < 192) {
			type = 'B';
		} else if (hightByte < 224) {
			type = 'C';
		} else if (hightByte < 240) {
			type = 'D';
		} else {
			type = 'E';
		}

		return type;

	}

	static void direccionLocal() {
		try {
			InetAddress local = InetAddress.getLocalHost();
			System.out.println("Nombre del equipo local: " + local.getHostName());
			System.out.println("Dirección IP: " + local.getHostAddress());
			System.out.println("Clase de la direccion IP: " + claseIP(local.getAddress()));
		} catch (UnknownHostException e) {
			System.out.println("No se pudo encontrar el equipo local");
			e.printStackTrace();
		}
	}
	
	static void direccionRemota(String nombre) {
		try {
			
			System.out.println("Buscando " + nombre + "...");
			InetAddress[] direcciones = InetAddress.getAllByName(nombre);
			
			for ( InetAddress direccion : direcciones) {
				System.out.println("Nombre del Host: " + direccion.getHostName());
				System.out.println("IP del Host: " + direccion.getHostAddress());
				System.out.println("Clase de la direccion IP del Host: " + claseIP(direccion.getAddress()));				
			}
			
		} catch (UnknownHostException e) {
			System.out.println("No se pudo encontrar a " + nombre);
		}
	}

	public static void main(String[] args) throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		direccionLocal();
		
		String id;
		
		System.out.print("ID del computador (Digite FIN para terminar): ");
		while ((id = in.readLine()) != null && !id.isEmpty() && !id.equalsIgnoreCase("FIN")) {
			direccionRemota(id);
			System.out.print("ID del computador (Digite FIN para terminar): ");
		}
		
		System.out.println("Adios!");
		
	}

}
