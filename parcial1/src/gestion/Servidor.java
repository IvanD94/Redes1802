package gestion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;

public class Servidor {

	static long t0;
	static double [] apuestas;
	static ThreadGroup hilos;
	
	public static void main(String[] args) {

		ServerSocket servidor = null;
		apuestas = new double[6];
		hilos = new ThreadGroup("clientes");
		
		try {
			
			servidor = new ServerSocket(11235);
			
			System.out.println("Servidor listo");
			
			t0 = System.currentTimeMillis();
			int i = 0;
			while (true) {
				Socket solicitud = servidor.accept();
				Hilo hilo = new Hilo(hilos, "c" + i, solicitud);
				hilo.start();
				i++;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				servidor.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static synchronized void apostar (int caballo, double amount) {
		apuestas[caballo] += amount;
	}
	
	static synchronized void mostrarTotal() {
		DecimalFormat df = new DecimalFormat("0.00");
		
		int i = 0;
		for (double d: apuestas) {
			System.out.println("Caballo " + i + ": " + df.format(d));
			i++;
		}
	}
	
	static class Hilo extends Thread{
		
		BufferedReader in;
		PrintWriter out;
		Socket solicitud;
		
		public Hilo(ThreadGroup group, String name, Socket solicitud) {
			super(group, name);
			
			this.solicitud = solicitud;
			try {
				in = new BufferedReader(new InputStreamReader(solicitud.getInputStream()));
				out = new PrintWriter(solicitud.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			
			try {
				while (System.currentTimeMillis() - t0 <= 10000) {
					if (in.ready()) {
						String[] info = in.readLine().split(" ");
						apostar(Integer.parseInt(info[0]), Double.parseDouble(info[1]));
					}
				}
				
				out.println("Apuestas cerradas");
				
				in.close();
				out.close();
				solicitud.close();
				
				if (hilos.activeCount() == 1) {
					mostrarTotal();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		
	}
}
