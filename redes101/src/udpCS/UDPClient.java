package udpCS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {

	public UDPClient() {

		System.out.println("UDP client inicialized");

		Scanner sc = new Scanner(System.in);

		try (DatagramSocket clientSocket = new DatagramSocket(9001)) {

			InetAddress inetAddress = InetAddress.getLocalHost();

			byte[] sendMessage;

			while (true) {
				System.out.print("Enter a message: ");
				String message = sc.nextLine();
				if ("quit".equalsIgnoreCase(message)) {
					break;
				}

				sendMessage = message.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendMessage, sendMessage.length, inetAddress, 9003);
				clientSocket.send(sendPacket);

				byte[] receiveMessage = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				clientSocket.receive(receivePacket);
				String receivedSentence = new String(receivePacket.getData()).trim();
				System.out.println("Received from server [" + receivedSentence + "]");
				System.out.println("from " + receivePacket.getSocketAddress());
				
			}

			sc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		UDPClient client = new UDPClient();
	}

}
