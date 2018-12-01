package gestion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Cliente {
	
	public static void main(String[] args) {
		
		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			Socket cliente = new Socket(InetAddress.getLocalHost(), 11235);

			BufferedReader cIn = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			PrintWriter cOut = new PrintWriter(cliente.getOutputStream(), true);

			boolean terminado = false;

			while (!terminado) {
				if (cIn.ready()) {
					terminado = "Apuestas cerradas".equals(cIn.readLine());
				}
				if (!terminado && in.ready()) {
					cOut.println(in.readLine());
				}
			}
			
			System.out.println("Cerradas");
			
			cIn.close();
			cOut.close();
			cliente.close();
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public int apostar(String ruta) {

		try {

			BufferedReader in = new BufferedReader(new FileReader(ruta));

			Socket cliente = new Socket(InetAddress.getLocalHost(), 11235);

			BufferedReader cIn = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			PrintWriter cOut = new PrintWriter(cliente.getOutputStream(), true);

			boolean terminado = false;
			
			int i = 0;

			while (!terminado) {
				if (cIn.ready()) {
					terminado = "Apuestas cerradas".equals(cIn.readLine());
				}
				if (!terminado && in.ready()) {
					cOut.println(in.readLine());
					i++;
				}
			}
			
			cIn.close();
			cOut.close();
			cliente.close();
			in.close();

			return i;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return -1;

	}

}
