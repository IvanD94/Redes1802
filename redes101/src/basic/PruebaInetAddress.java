package basic;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PruebaInetAddress {

	public static void main(String[] args) {

		try {

			InetAddress localAdress = InetAddress.getLocalHost();
			System.out.println(localAdress);

			InetAddress NasaAdress = InetAddress.getByName("www.NASA.gov");
			System.out.println(NasaAdress);

			InetAddress[] YahooAdresses = InetAddress.getAllByName("www.yahoo.com");
			for (InetAddress adress : YahooAdresses) {
				System.out.println(adress);
			}

			byte[] dir = NasaAdress.getAddress(); // Byte en C2

			for (byte by : dir) {
				System.out.print(by + ".");
			}

			System.out.println();
			
			for (byte by : dir) {
				System.out.print( (0xff & by) + "."); //0xff = 11111111 -> int 
			}

		} catch (UnknownHostException e) {
			System.out.println("Error ubicando al host");
		}

	}

}
