package multiThreadServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MultiCliente {

	public static void main(String[] args) {

		for (int i = 0; i < 100; i++) {
			HiloLanzador hl = new HiloLanzador("127.0.0.1", 8030);
			System.out.println("Cliente " + hl.nombre + " generado");
			hl.start();
		}

	}

	static class HiloLanzador extends Thread {

		String nombre;
		int puerto;
		String host;

		public HiloLanzador(String host, int puerto) {

			nombre = generarNombre((int) (Math.random() * 20) + 1);
			this.host = host;
			this.puerto = puerto;

		}

		private String generarNombre(int length) {

			String nombre = "";

			for (int i = 0; i < length; i++) {
				nombre += (char) ((int) (Math.random() * 26) + 'a');
			}

			return nombre;
		}

		@Override
		public void run() {

			try {

				Socket cliente = new Socket("127.0.0.1", 8030);
				BufferedReader lectorC = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
				PrintWriter escritorC = new PrintWriter(cliente.getOutputStream(), true);

				host = cliente.getLocalAddress().getHostName();
				puerto = cliente.getLocalPort();

				System.out.println(puerto);

				escritorC.println(nombre);
				escritorC.println(host + "(puerto:" + puerto + ")");

				String respuesta = lectorC.readLine();
				System.out.println(respuesta);

				lectorC.close();
				escritorC.close();
				cliente.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
