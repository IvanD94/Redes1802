package multiThread;

import java.util.Arrays;

public class Master {

	public static void main(String[] args) {

		pruebaPrioridad();

	}

	static void pruebaPrioridad() {

		Process[] trans = new Process[100];

		for (int i = 0; i < 100; i++) {

			trans[i] = new Process((int) (Math.random() * 10) + 1);
			trans[i].start();

		}

		for (int i = 0; i < trans.length; i++) {
			try {
				trans[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Arrays.sort(trans);

		int[] cant = new int[10];
		long[] espera = new long[10];
		long[] duracion = new long[10];

		for (int i = 0; i < trans.length; i++) {

			int pos = trans[i].getPriority() - 1;

			cant[pos]++;

			espera[pos] += trans[i].iniciado - trans[i].creado;

			duracion[pos] += trans[i].ejecutado - trans[i].iniciado;

			System.out.println(trans[i] + "");
		}
		
		System.out.println("");	
		
		for (int i = 1; i < 11; i++) {
			System.out.println( i + ") " + cant[i-1] + ", " + espera[i-1]/cant[i-1] + ", " + duracion[i-1]/cant[i-1]);
		}
		
	}

}
