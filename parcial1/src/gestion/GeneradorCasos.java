package gestion;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class GeneradorCasos {
	
	public static void main(String[] args) throws IOException {
		
		DecimalFormat df = new DecimalFormat("0.00");
		
		for (int i = 0; i < 10; i++) {
			BufferedWriter out = new BufferedWriter(new FileWriter("./data/" + i + ".txt"));
			
			int apuestas = (int)(Math.random()*500) + 500;
			
			for (int j = 0; j < apuestas; j++) {
				int caballo = (int)(Math.random()*6);
				double apuesta = Math.random()*10000;
				out.write(caballo + " " + df.format(apuesta).replaceAll(",", ".") + "\n");
			}
			
			out.close();
			
		}
	}

}
