package multiThreadServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorBienvenidaThread {
	

	public static void main(String[] args) {

		ServerSocket servidor = null;

		try {
			servidor = new ServerSocket(8030);
			while (true) {
				Socket c = servidor.accept();
				HiloServidorBienvenida hilo = new HiloServidorBienvenida(c);
				hilo.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				servidor.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
