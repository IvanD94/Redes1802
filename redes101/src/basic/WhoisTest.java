package basic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class WhoisTest {

	public static void main(String[] args) throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		String id;

		System.out.print("URL a consultar (Digite FIN para terminar): ");
		while ((id = in.readLine()) != null && !id.isEmpty() && !id.equalsIgnoreCase("FIN")) {

			System.out.print("URL a consultar (Digite FIN para terminar): ");
		}

		System.out.println("Adios!");

	}
	
	static void buscar(String str) throws IOException {
		int c;
		
		str = str+"\n";
		
		Socket s = new Socket("whois.internic.net", 43);
		
		InputStream in = s.getInputStream();
		OutputStream out = s.getOutputStream();
		
		byte buf[] = new byte[str.length()];
		
		buf = str.getBytes();
		
		out.write(buf);
		
		while ((c = in.read()) != -1) {
			System.out.print((char)c);
		}
		
		s.close();
		
	}

}
