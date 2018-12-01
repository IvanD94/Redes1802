package SSL;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class SSLServerSocket {

	public static void main(String[] args) {
		
		try {
			
			System.setProperty("javax.net.ssl.keyStore", "keystore.jks");
			System.setProperty("javax.net.ssl.keyStorePassword", "123456");
			
			SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			ServerSocket serverSocket = ssf.createServerSocket(8000);
			
			
			
			System.out.println("SSLServerSocket Started");
			
			try (Socket socket = serverSocket.accept();
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
				System.out.println("Client socket created");
				String line = null;
				while (((line = br.readLine()) != null)) {
					System.out.println(line);
					out.println(line);
				}
				br.close();
				System.out.println("SSLServerSocket Terminated");
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
