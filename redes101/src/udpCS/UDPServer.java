package udpCS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

	DatagramSocket serverSocket;

	public UDPServer() {

		System.out.println("Creando servidor UDP");

		try {
			int port = 9003;
			serverSocket = new DatagramSocket(port);
			System.out.println("Servidor creado en puerto: " + port);
		} catch (IOException e) {
			System.out.println("Imposible crear sevidor");
		}

	}

	public DatagramPacket recibirPaquete(int length) {

		DatagramPacket receivePacket = null;

		try {
			receivePacket = new DatagramPacket(new byte[length], length);
			serverSocket.receive(receivePacket);
			System.out.println("Paquete recibido");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return receivePacket;

	}

	public void leerMensaje(DatagramPacket receivePacket) {

		String message = new String(receivePacket.getData()).trim();
		System.out.println("Received from client: [" + message + "]");
		System.out.println("From: " + receivePacket.getAddress() + ":" + receivePacket.getPort());

	}

	public byte[] generarRespuesta(DatagramPacket receivePacket) {
		
		byte[] sendMessage;
		sendMessage = receivePacket.getData();
		return sendMessage;
		
	}

	public void responder(InetAddress inetAddress, int port, byte[] sendMessage) {

		try {
			DatagramPacket sendPacket = new DatagramPacket(sendMessage, sendMessage.length, inetAddress, port);
			serverSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		UDPServer server = new UDPServer();
		
		while (true) {
			DatagramPacket receivePacket = server.recibirPaquete(1024);
			server.leerMensaje(receivePacket);
			byte[] sendMessage = server.generarRespuesta(receivePacket);
			InetAddress inetAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			server.responder(inetAddress, port, sendMessage);
			
		}
	}

}
