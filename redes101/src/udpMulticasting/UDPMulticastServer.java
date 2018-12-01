package udpMulticasting;

import java.io.*;
import java.net.*;
import java.util.*;

public class UDPMulticastServer {

	public UDPMulticastServer() {
		System.out.println("UDP Multicast Time Server Started");
		try (MulticastSocket multicastSocket = new MulticastSocket()) {

			InetAddress inetAddress = InetAddress.getByName("228.5.6.7");
			multicastSocket.joinGroup(inetAddress);

			byte[] data;
			DatagramPacket packet;

			while (true) {
				Thread.sleep(1000);
				String message = (new Date()).toString();
				System.out.println("Sending: [" + message + "]");
				data = message.getBytes();
				packet = new DatagramPacket(data, message.length(), inetAddress, 9877);
				multicastSocket.send(packet);
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("UDP Multicast Time Server Terminated");
		
	}

	public static void main(String args[]) {
		new UDPMulticastServer();
	}

}
