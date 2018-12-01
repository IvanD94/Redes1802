package gestion;

import java.io.FileNotFoundException;

public class LanzadorDeClientes {
	
	public static void main(String[] args) throws FileNotFoundException {
		for (int i = 0; i < 10; i++) {
			Cliente c = new Cliente();
			Lanzador l = new Lanzador(c, "./data/" + i + ".txt", i);
			l.start();
		}
	}

	static synchronized void print(String lin) {
		System.out.println(lin);
	}
	
	static class Lanzador extends Thread{
		
		Cliente c;
		String ruta;
		int id;
		
		public Lanzador(Cliente c, String ruta, int id) {
			this.c = c;
			this.ruta = ruta;
			this.id = id;
		}
		
		@Override
		public void run() {
			int val = c.apostar(ruta);
			print(id+ " " + val);
		}
		
	}
	
}
