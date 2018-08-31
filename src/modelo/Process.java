package modelo;

import java.util.Date;

public class Process extends Thread implements Comparable<Process>{
	
	long creado;
	long iniciado;
	long ejecutado;
	int prioridad;
	
	public Process() {
		this.creado = System.currentTimeMillis();
		this.prioridad = 5;
	}
	
	public Process(int prioridad) {
		this.creado = System.currentTimeMillis();
		this.setPriority(prioridad);
		this.prioridad = prioridad;
	}
	
	public void doSomething() {
		
		this.iniciado = System.currentTimeMillis();
				
		for (int i = 0; i < 10000; i++) {
			String ss = "";
			for (int j = 0; j < 100; j++) {
				ss += j + "";
			}
		}
		
		this.ejecutado = System.currentTimeMillis();
		
	}
	
	@Override
	public void run() {
		
		doSomething();
		
	}
	
	@Override
	public String toString() {
		return this.getPriority() + ", " + new Date(creado) + ", " + (iniciado-creado)  + ", " + (ejecutado - iniciado);
	}
	
	public String info() {
		return "Prioridad: " + this.getPriority() + "\nTiempo de creacion: " + new Date(creado) +"\nTiempo hasta inicio: " + (iniciado-creado) + "\nTiempo ejecucion: " + (ejecutado - iniciado) + "\n";
	}

	@Override
	public int compareTo(Process o) {
		return o.prioridad != this.prioridad? this.prioridad - o.prioridad: (int)(this.iniciado - o.iniciado);
	}
	
}
