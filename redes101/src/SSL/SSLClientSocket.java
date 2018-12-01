package SSL;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.net.ssl.*;

public class SSLClientSocket {

	public static void main(String[] args) throws Exception {
		
		System.out.println("SSLClientSocket Started");
		
		System.setProperty("javax.net.ssl.trustStore", "keystore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		
		SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
		
		try (Socket socket = sf.createSocket("localhost", 8000);
				
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
			
			Scanner scanner = new Scanner(System.in);
			
			while (true) {
				System.out.print("Enter text: ");
				String inputLine = scanner.nextLine();
				if ("quit".equalsIgnoreCase(inputLine)) {
					break;
				}
				out.println(inputLine);
				System.out.println("Server response: " + br.readLine());
			}
			
			scanner.close();
			System.out.println("SSLServerSocket Terminated");
		}
	}

}
