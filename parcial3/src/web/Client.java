package web;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class Client extends Thread {

	/**
	 * Entero que indica al caballo al que se le realiz� la apuesta.
	 */
	int caballo;

	/**
	 * Decimal que indica la cantidad de dinero apostado.
	 */
	double apuesta;

	/**
	 * Arreglo que contiene el progreso de cada caballo durante la carrera.
	 */
	String[] avance;

	/**
	 * Separador horizontal utilizado para dar formato a la salida de la informaci�n
	 * durante la carrera
	 */
	String linea;

	/**
	 * Stream utilizado para pasar la informaci�n recibida a la linea de salida.
	 */
	AudioInputStream audioInputStream;

	/**
	 * Linea de audio utilizada para reproducir el sonido.
	 */
	SourceDataLine sourceDataLine;

	/**
	 * Constructor de la clase, inicializa el arreglo que contendra le progreso de
	 * los caballos.
	 */
	public Client() {

		avance = new String[6];
		for (int i = 0; i < 6; i++) {
			avance[i] = "";
		}

	}

	/**
	 * Realiza la solicitud de conexi�n con el servidor y maneja todo el proceso de
	 * envi� y recepci�n de informaci�n.
	 */
	public void conectar() {

		System.out.println("Conectando con el servidor...");

		System.setProperty("javax.net.ssl.trustStore", "keystore.jks");

		SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();

		try (Socket socket = sf.createSocket("localhost", 11234)) {

			PrintWriter sOut = new PrintWriter(socket.getOutputStream(), true);

			BufferedReader sIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String lin = "";

			try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {

				boolean primero = true;
				String id = "";
				String password = "";

				// Permite realizar varios intentos de v�lidaci�n en el sistema.
				do {

					if (!primero) {
						System.out.println("Datos incorrectos, intente nuevamente");
					}

					System.out.print("Ingrese su usuario: ");
					id = in.readLine();
					System.out.print("Ingrese su password: ");
					password = in.readLine();

					sOut.println(id + " " + password);

					System.out.println("Data send");

					primero = false;

				} while (!(lin = sIn.readLine()).equals("OK"));

				System.out.println(sIn.readLine());

				System.out.print("Indique el caballo al que desea apostarle: ");
				caballo = Integer.parseInt(in.readLine());
				System.out.print("Indique la cantidad de dinero a apostar: ");
				apuesta = Double.parseDouble(in.readLine());

			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Env�a la informaci�n de la apusta al servidor.
			sOut.println(caballo + " " + apuesta);

			System.out.println(sIn.readLine());
			System.out.println(sIn.readLine());

			System.out.println(sIn.readLine());

			// Incia la reproducci�n de la transmisi�n de la narraci�n.
			iniciarTransmision();

			System.out.println(sIn.readLine());

			// Muestra el progreso de la carrera.
			while (!(lin = sIn.readLine()).equals("Fin carrera")) {
				System.out.print(mostrarCarrera(lin));
			}

			System.out.print("Carrera terminada. ");

			int ganador = Integer.parseInt(sIn.readLine());

			System.out.print("El caballo ganador fue: " + ganador + ". ");

			System.out.print(ganador == caballo ? "Felicitaciones." : "Gracias por participar.");

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Da formato a la informaci�n enviada por el servidor.
	 * 
	 * @param datos
	 *            - Informaci�n de la carrera en curso.
	 * @return
	 */
	public String mostrarCarrera(String datos) {

		if (datos.equals("null")) {
			return "";
		}

		StringBuilder carrera = new StringBuilder();

		String[] valores = datos.substring(1, datos.length() - 1).split(",");

		if (linea == null) {
			iniciarPatrones();
		}

		carrera.append('\n');
		carrera.append(linea);

		for (int i = 0; i < 6; i++) {

			int longitud = Integer.parseInt(valores[i].trim());

			while (avance[i].length() < longitud) {
				avance[i] += "|";
			}

			if (i == caballo - 1) {
				if (i != 0) {
					carrera.append(linea);
				}
				carrera.append(String.format("�<%d>�", i + 1));
				carrera.append(String.format(" %-" + (Server.TIEMPO_OPERACION * 2 / 1000) + "s �", avance[i]));
				carrera.append('\n');
				if (i != 5) {
					carrera.append(linea);
				}
			} else {

				carrera.append(String.format("� %d �", i + 1));
				carrera.append(String.format(" %-" + (Server.TIEMPO_OPERACION * 2 / 1000) + "s �", avance[i]));
				carrera.append('\n');

			}

		}

		carrera.append(linea);

		return carrera.toString();
	}

	/**
	 * Incia el separador horizontal, teniendo en cuenta la duraci�n de la carrera.
	 */
	public void iniciarPatrones() {

		StringBuilder sb = new StringBuilder();

		sb.append("+---+");

		for (int i = 0; i < Server.TIEMPO_OPERACION / 1000 * 2 + 2; i++) {
			sb.append("-");
		}

		sb.append('+');

		sb.append('\n');

		linea = sb.toString();

	}

	/**
	 * M�todo heredado de la clase Thread que realiza un llamado al m�todo conectar.
	 */
	@Override
	public void run() {
		conectar();
	}

	/**
	 * Reproduce el audio de la narraci�n.
	 */
	private void playAudio() {
		byte[] buffer = new byte[10000];
		try {
			int count;
			while ((count = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
				if (count > 0) {
					sourceDataLine.write(buffer, 0, count);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Crea e inicia el hilo encargado del proceso de recepci�n de la transmisi�n.
	 */
	public void iniciarTransmision() {
		HiloTransmision hT = new HiloTransmision();
		hT.start();
	}

	/**
	 * Clase encargada del proceso de recibir y reproducir la narraci�n.
	 * 
	 * @author IvanD94
	 *
	 */
	class HiloTransmision extends Thread {
		
		/**
		 * Crea la conexi�n con el servidor y realiza el proceso de recibir los paquetes.
		 */
		public void recibirTransmision() {

			try (MulticastSocket multicastSocket = new MulticastSocket(9877)) {

				InetAddress inetAddress = InetAddress.getByName("228.5.6.7");
				multicastSocket.joinGroup(inetAddress);

				byte[] audioBuffer = new byte[10000];

				while (true) {

					DatagramPacket packet = new DatagramPacket(audioBuffer, audioBuffer.length);
					multicastSocket.receive(packet);

					try {

						byte audioData[] = packet.getData();
						InputStream byteInputStream = new ByteArrayInputStream(audioData);
						AudioFormat audioFormat = new AudioFormat(8000, 16, 2, true, false);
						audioInputStream = new AudioInputStream(byteInputStream, audioFormat,
								audioData.length / audioFormat.getFrameSize());
						DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
						sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
						sourceDataLine.open(audioFormat);
						sourceDataLine.start();
						playAudio();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		/**
		 * M�todo heredado de la clase Thread que permite la ejecuci�n en paralelo del proceso de transmisi�n.
		 */
		@Override
		public void run() {
			recibirTransmision();
		}

	}

	/**
	 * M�todo principal, crea una nueva instancia de la clase Client e inicia todo el proceso.
	 * @param args
	 */
	public static void main(String[] args) {

		Client c = new Client();
		c.start();

	}

}
