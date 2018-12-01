package gestion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class Verificador {

	public static void main(String[] args) throws IOException {
		double [] apuestas = new double[6];
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		for (int i = 0; i < 10; i++) {
			String dat[] = in.readLine().split(" ");
			
			int arch = Integer.parseInt(dat[0]);
			int ap = Integer.parseInt(dat[1]);
			
			BufferedReader aIn = new BufferedReader(new FileReader("./data/" + arch + ".txt"));
			
			for (int j = 0; j < ap; j++) {
				String[] info = aIn.readLine().split(" ");
				apuestas[Integer.parseInt(info[0])] += Double.parseDouble(info[1]);
			}
			
			aIn.close();
			
		}
		
		DecimalFormat df = new DecimalFormat("0.00");
		
		int i = 0;
		for (double d: apuestas) {
			
			System.out.println("Caballo " + i + ": " + df.format(d));
			i++;
		}

	}

}
