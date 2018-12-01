package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Server extends Thread {

	public static final long TIEMPO_OPERACION = 20 * 1000;
	public static final long TIEMPO_ACTUALIZACION = 500;

	long t0;
	double[] apuestas;
	ThreadGroup hilos;

	volatile int[] avance;
	volatile boolean finalizado;
	volatile int ganador;

	private final byte audioBuffer[] = new byte[10000];
	private TargetDataLine targetDataLine;

	public Server() {

		apuestas = new double[6];
		hilos = new ThreadGroup("clientes");
		t0 = System.currentTimeMillis();

	}

	public void recibirConexiones() {

		try (ServerSocket serverSocket = new ServerSocket(11234)) {

			System.out.println("Servidor listo");

			int i = 1;

			while (true) {
				Socket solicitud = serverSocket.accept();
				HiloCliente hilo = new HiloCliente(hilos, "c" + i, solicitud);
				hilo.start();
				i++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	synchronized long getT0() {
		return t0;
	}

	synchronized void apostar(int caballo, double amount) {
		apuestas[caballo - 1] += amount;
	}

	synchronized String darTotal() {
		DecimalFormat df = new DecimalFormat("0.00");

		String mensaje = "";

		int i = 1;
		for (double d : apuestas) {
			mensaje += "Caballo " + i + ": " + df.format(d) + "\n";
			i++;
		}

		return mensaje;
	}

	@Override
	public void run() {
		recibirConexiones();
	}

	public void iniciarCarrera() {

		HiloCarrera hC = new HiloCarrera();
		hC.start();

	}

	public void iniciarTransmision() {

		HiloTransmision hT = new HiloTransmision();
		hT.start();

	}

	class HiloCliente extends Thread {

		BufferedReader in;
		PrintWriter out;
		Socket solicitud;

		public HiloCliente(ThreadGroup group, String name, Socket solicitud) {

			super(group, name);

			System.out.println("conexion recibida");

			this.solicitud = solicitud;

			try {
				in = new BufferedReader(new InputStreamReader(solicitud.getInputStream()));
				out = new PrintWriter(solicitud.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {

			try {

				String[] info = in.readLine().split(" ");

				if ((System.currentTimeMillis() - getT0()) < TIEMPO_OPERACION) {

					out.println("Apuesta recibida\nTiempo restante:"
							+ (TIEMPO_OPERACION - System.currentTimeMillis() + getT0()) / 1000 + "s.");

					apostar(Integer.parseInt(info[0]), Double.parseDouble(info[1]));

					System.out.println("Apuesta recibida: " + info[0] + " - " + info[1]);

					Thread.sleep(TIEMPO_OPERACION - (System.currentTimeMillis() - getT0()));

					out.println("Apuestas cerradas.");

					out.println("Incia la carrera.");

					while (!finalizado) {

						out.println(Arrays.toString(avance));
						Thread.sleep(TIEMPO_ACTUALIZACION);

					}

					out.println(Arrays.toString(avance));

					out.println("Fin carrera");

					out.println(ganador + "");

					if (hilos.activeCount() == 1) {
						System.out.println(darTotal());
					}

				} else {
					out.println("La realizacion de apuestas no esta disponible\n\n");
				}

				in.close();
				out.close();
				solicitud.close();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	class HiloCarrera extends Thread {

		public HiloCarrera() {

		}

		@Override
		public void run() {
			iniciar();
		}

		private void iniciar() {

			Random rd = new Random();

			avance = new int[6];
			ganador = rd.nextInt(6) + 1;

			try {

				while (System.currentTimeMillis() - getT0() < 2 * TIEMPO_OPERACION - TIEMPO_ACTUALIZACION) {

					avance[ganador - 1]++;

					for (int i = 0; i < 6; i++) {
						if (i != ganador - 1 && rd.nextDouble() < rd.nextInt(20) / 100.0 + 0.8) {
							avance[i]++;
						}
					}

					Thread.sleep(TIEMPO_ACTUALIZACION);

				}

				avance[ganador - 1]++;

				finalizado = true;

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	class HiloTransmision extends Thread {

		public void transmitir() {

			try {
				AudioFormat audioFormat = new AudioFormat(8000, 16, 2, true, false);
				DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
				targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
				targetDataLine.open(audioFormat);
				targetDataLine.start();
			} catch (LineUnavailableException e1) {
				e1.printStackTrace();
			}

			try (MulticastSocket multicastSocket = new MulticastSocket()) {

				InetAddress inetAddress = InetAddress.getByName("228.5.6.7");
				multicastSocket.joinGroup(inetAddress);

				DatagramPacket packet;

				while (true) {
					
					int count = targetDataLine.read(audioBuffer, 0, audioBuffer.length);
					if (count > 0) {
						packet = new DatagramPacket(audioBuffer, audioBuffer.length, inetAddress, 9877);
						multicastSocket.send(packet);
					}

				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			transmitir();
		}

	}

	public static void main(String[] args) throws InterruptedException {

		Server s = new Server();

		s.start();
		Thread.sleep(TIEMPO_OPERACION);
		System.out.println("Apuestas cerradas");

		s.iniciarCarrera();
		s.iniciarTransmision();

	}

}
