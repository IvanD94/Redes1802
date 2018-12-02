package web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;
import java.util.TimeZone;

import javax.net.ssl.SSLServerSocketFactory;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * Clase que ofrece los servicios necesarios para la realización de las carreras
 * de caballos con su respectivas apuestas y narración.
 * 
 * @author IvanD94
 *
 */
public class Server extends Thread {

	/**
	 * Constante que determina la duración del periodo de apuestas y de la carrera.
	 * En milisegundos.
	 */
	public static final long TIEMPO_OPERACION = 40 * 1000;

	/**
	 * Constante que determina el intervalo de tiempo en el que se envía información
	 * a los clientes durante la carrera.
	 */
	public static final long TIEMPO_ACTUALIZACION = 500;

	/**
	 * Tiempo en el que se inicia el servidor
	 */
	long t0;

	/**
	 * Arreglo que contiene el total de las apuestas realizadas a cada caballo
	 * durante una carrera.
	 */
	double[] apuestas;

	/**
	 * Conjunto de hilos de los cliente.
	 */
	ThreadGroup hilos;

	/*
	 * Las siguientes variables se declaran volatile para que las actualizaciones se
	 * realicen en memoria principal y no sólo en caché, así el acceso por
	 * diferentes hilos conserva la misma información.
	 */

	/**
	 * Arreglo que contiene el progreso de cada caballo durante la carrera.
	 */
	volatile int[] avance;

	/**
	 * Booleano que indica cuando ha finalizado la carrera.
	 */
	volatile boolean finalizado;

	/**
	 * Entero que indica el caballo que gano la carrera
	 */
	volatile int ganador;

	/**
	 * Buffer usado para la transferencia de la narración.
	 */
	private final byte audioBuffer[] = new byte[10000];

	/**
	 * Linea que permite leer audio desde el microfono.
	 */
	private TargetDataLine targetDataLine;

	/**
	 * Formato de fecha usado para la consistencia en el almacenamiento y lectura de
	 * las fechas de las carreras.
	 */
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	/**
	 * Colleccion con los usuarios registrados en el sistema. id --> Usuario
	 */
	HashMap<String, Usuario> usuarios;

	/**
	 * Constructor de la clase principal, inicializa el arreglo donde se almacenaran
	 * las apuestas, el grupo de hilos y fija el tiempo inicial al actual.
	 */
	public Server() {

		apuestas = new double[6];
		hilos = new ThreadGroup("clientes");
		t0 = System.currentTimeMillis();

	}

	/**
	 * Inicia el servidor en localhost con el puerto 11234 y espera a la recepción
	 * de solicitudes.
	 */
	public void recibirConexiones() {

		// Archivo con las claves
		System.setProperty("javax.net.ssl.keyStore", "keystore.jks");

		// Clave del archivo
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");

		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

		try (ServerSocket serverSocket = ssf.createServerSocket(11234)) {

			System.out.println("Servidor listo");

			int i = 1;

			while (true) {
				Socket solicitud = serverSocket.accept();

				// Genera un cliente para atender cada solicitud
				HiloCliente hilo = new HiloCliente(hilos, "c" + i, solicitud);
				hilo.start();
				i++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @return long t0 - Tiempo de inicio del servidor.
	 */
	synchronized long getT0() {
		return t0;
	}

	/**
	 * Le adiciona al caballo seleccionado el monto indicado.
	 * 
	 * @param caballo
	 *            - Entero que indica el caballo a apostar.
	 * @param amount
	 *            - Monto de dinero a postar por el caballo.
	 */
	synchronized void apostar(int caballo, double amount) {
		apuestas[caballo - 1] += amount;
	}

	/**
	 * Muestra el total de las apuestas realizadas por todos los caballos.
	 * 
	 * @return Cadena con la información dada.
	 */
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

	/**
	 * Realiza la lectura de los archivos y crea las entidades para todos los
	 * usuarios y sus apuestas realizadas.
	 */
	public void cargarDatos() {

		usuarios = new HashMap<>();

		try (BufferedReader inUsuarios = new BufferedReader(new FileReader("./data/users.txt"));
				BufferedReader inRaces = new BufferedReader(new FileReader("./data/races.txt"))) {

			String lin = "";

			while ((lin = inUsuarios.readLine()) != null && !lin.isEmpty()) {
				String[] dat = lin.split(";");
				usuarios.put(dat[0], new Usuario(dat[0], dat[1]));
			}

			while ((lin = inRaces.readLine()) != null && !lin.isEmpty()) {
				String[] dat = lin.split(";");
				usuarios.get(dat[0]).races.add(new Apuesta(Integer.parseInt(dat[1]), Double.parseDouble(dat[2]),
						Boolean.parseBoolean(dat[3]), dat[4]));
			}

			System.out.println("Datos cargados");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Guarda en los archivos los datos de la aplicación.
	 */
	public void guardarDatos() {

		try (BufferedWriter outUsers = new BufferedWriter(new PrintWriter("./data/users.txt"));
				BufferedWriter outRaces = new BufferedWriter(new PrintWriter("./data/races.txt"))) {

			for (String id : usuarios.keySet()) {

				Usuario us = usuarios.get(id);

				outUsers.write(us + "\n");

				outRaces.write(us.apuestas());

			}

			System.out.println("Datos guardados");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Realiza el registro en la clase Usuario de una apuesta realizada por el
	 * mismo.
	 * 
	 * @param id
	 *            - Cadena con el id del usuario.
	 * @param caballo
	 *            - Entero que indica el caballo apostado.
	 * @param monto
	 *            - Decimal que contiene el monto de la apuesta
	 * @param gano
	 *            - Booleano que indica si se gano o no con la apuesta.
	 */
	synchronized void registrarApuesta(String id, int caballo, double monto, boolean gano) {
		usuarios.get(id).races.add(new Apuesta(caballo, monto, gano));
	}

	/**
	 * Realiza el registro en la lista de usuarios de un nuevo Usuario.
	 * 
	 * @param id
	 *            - Cadena con el id del usuario.
	 * @param password
	 *            - Cadena con la contraseña del usuario.
	 */
	synchronized void registrarUsuario(String id, String password) {
		usuarios.put(id, new Usuario(id, password));
	}

	/**
	 * Método heredado de la clase Thread que llama al método recibir conexiones.
	 */
	@Override
	public void run() {
		recibirConexiones();
	}

	/**
	 * Crea e inicia el hilo encargado de simular la carrera.
	 */
	public void iniciarCarrera() {

		HiloCarrera hC = new HiloCarrera();
		hC.start();

	}

	/**
	 * Crea e inicia el hilo encargado de la transmisión de la narracón.
	 */
	public void iniciarTransmision() {

		HiloTransmision hT = new HiloTransmision();
		hT.start();

	}

	/**
	 * Clase que permite manejar individualmente y de manera concurrente (respeto a
	 * otras) una solicitud de un cliente.
	 * 
	 * @author IvanD94
	 *
	 */
	class HiloCliente extends Thread {

		BufferedReader in;
		PrintWriter out;
		Socket solicitud;

		/**
		 * Constructor de la clase.
		 * 
		 * @param group
		 *            - Grupo de hilos al que pertenecera.
		 * @param name
		 *            - Cadena con el nombre del hilo.
		 * @param solicitud
		 *            - Socket asociado a la solicitud a responder.
		 */
		public HiloCliente(ThreadGroup group, String name, Socket solicitud) {

			super(group, name);

			this.solicitud = solicitud;

			try {
				in = new BufferedReader(new InputStreamReader(solicitud.getInputStream()));
				out = new PrintWriter(solicitud.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Método heredado de la clase Thread que realiza el manejo de toda la
		 * interacción con el cliente.
		 */
		@Override
		public void run() {

			try {

				// Verifica que todavía se pueda realizar la apuesta.
				if ((System.currentTimeMillis() - getT0()) < TIEMPO_OPERACION) {

					String[] info;

					String id = "";
					int caballo = 0;
					double monto = 0;

					boolean valido = false;

					// Permite al usuario intentar ingresar más de una vez en caso de equivocarse.
					while (!valido) {

						info = in.readLine().split(" ");
						id = info[0];
						if (usuarios.containsKey(info[0])) {

							if (usuarios.get(id).password.equals(info[1])) {
								out.println("OK");
								out.println("Ingreso exitoso");
								valido = true;
							} else {
								out.println("NO");
							}

						} else {
							registrarUsuario(id, info[1]);
							out.println("OK");
							out.println("Usuario registrado");
							valido = true;
						}

					}

					info = in.readLine().split(" ");

					out.println("Apuesta recibida\nTiempo restante:"
							+ (TIEMPO_OPERACION - System.currentTimeMillis() + getT0()) / 1000 + "s.");

					// Lee la información de la apuesta.
					caballo = Integer.parseInt(info[0]);
					monto = Double.parseDouble(info[1]);

					apostar(caballo, monto);

					System.out.println("Apuesta recibida: " + info[0] + " - " + info[1]);

					// Duerme el hilo el tiempo que falta para iniciar la carrera.
					Thread.sleep(TIEMPO_OPERACION - (System.currentTimeMillis() - getT0()));

					out.println("Apuestas cerradas.");

					out.println("Incia la carrera.");

					// Envía el estado de la carrera en el intervalo dado.
					while (!finalizado) {

						out.println(Arrays.toString(avance));
						Thread.sleep(TIEMPO_ACTUALIZACION);

					}

					out.println(Arrays.toString(avance));

					out.println("Fin carrera");

					registrarApuesta(id, caballo, monto, caballo == ganador);

					out.println(ganador + "");

					// Verifica que sea el último hilo en terminar (para no realizar las acciónes
					// más de una vez).
					if (hilos.activeCount() == 1) {
						System.out.print(darTotal());
						guardarDatos();
					}

				} else {
					out.println("La realizacion de apuestas no esta disponible\n\n\n");
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

	/**
	 * Clase que se encarga de simular la carrera.
	 * 
	 * @author IvanD94
	 *
	 */
	class HiloCarrera extends Thread {

		@Override
		public void run() {
			iniciar();
		}

		/**
		 * Realiza las operaciones para la simulación de la carrera.
		 */
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

	/**
	 * Clase encargada de realizar la transmisión por broadcast de la narración.
	 * 
	 * @author IvanD94
	 *
	 */
	class HiloTransmision extends Thread {

		/**
		 * Realiza la transmisión de la información.
		 */
		public void transmitir() {

			// Inicializa la lectura del microfono.
			try {
				AudioFormat audioFormat = new AudioFormat(8000, 16, 2, true, false);
				DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
				targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
				targetDataLine.open(audioFormat);
				targetDataLine.start();
			} catch (LineUnavailableException e1) {
				e1.printStackTrace();
			}

			// Realiza el envio de los paquetes.
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

	/**
	 * Clase que representa a un usuario del sistema.
	 * 
	 * @author IvanD94
	 *
	 */
	class Usuario {

		/**
		 * Cadena con el identificador del usuario.
		 */
		String id;

		/**
		 * Cadena con la contraseña del usuario.
		 */
		String password;

		/**
		 * Lista con las apuestas realizadas por el usuario.
		 */
		ArrayList<Apuesta> races;

		/**
		 * Constructor de la clase Usuario, asigna los valores ingresados por parametro
		 * e inicializa la lista que contendra las apuestas realizadas por el usuario.
		 * 
		 * @param id
		 *            - Cadena con el identificador del usuario.
		 * @param password
		 *            - Cadena con la contraseña del usuario.
		 */
		public Usuario(String id, String password) {
			this.id = id;
			this.password = password;
			races = new ArrayList<>();
		}

		/**
		 * Retorna una cadena que representa la informacion basica del usuario.
		 */
		@Override
		public String toString() {
			return id + ";" + password;
		}

		/**
		 * Retorna una cadena que contiene todas las apuestas realizadas por el usuario.
		 * 
		 * @return cadena con la información dada.
		 */
		public String apuestas() {
			String apuestasUsuario = "";

			for (Apuesta a : races) {

				apuestasUsuario += id + ";" + a + "\n";
			}

			return apuestasUsuario;
		}

	}

	/**
	 * Clase que representa una apuesta realizada por un usuario.
	 * 
	 * @author IvanD94
	 *
	 */
	class Apuesta {

		/**
		 * Entero que indica al caballo al que se le realizó la apuesta.
		 */
		int caballo;

		/**
		 * Decimal que indica la cantidad de dinero apostado.
		 */
		double monto;

		/**
		 * Booleano que indica si el caballo ganó o no esa carrera.
		 */
		boolean gano;

		/**
		 * Calendario con la fecha en la que se realizó la carrera.
		 */
		Calendar fecha;

		/**
		 * Crea una instancia de Apuesta con los datos ingresados por parametros y la
		 * fecha actual del sistema. Usado para la carrea en curso durante una
		 * ejecución.
		 * 
		 * @param caballo
		 *            - Entero que indica el caballo apostado.
		 * @param monto
		 *            - Decimal que indica la cantidad de dinero apostado.
		 * @param gano
		 *            - Booleano que indica si el caballo ganó o no esa carrera.
		 */
		public Apuesta(int caballo, double monto, boolean gano) {

			this.caballo = caballo;
			this.monto = monto;
			this.gano = gano;
			fecha = Calendar.getInstance(TimeZone.getTimeZone("GMT-5"));

		}

		/**
		 * Crea una instancia de Apuesta con los datos ingresados por parametros. Usado
		 * para la carga de datos.
		 * 
		 * @param caballo
		 *            - Entero que indica el caballo apostado.
		 * @param monto
		 *            - Decimal que indica la cantidad de dinero apostado.
		 * @param gano
		 *            - Booleano que indica si el caballo ganó o no esa carrera.
		 * @param date
		 *            - Cadena que representa la fecha en la que se reaizó la apuesta.
		 */
		public Apuesta(int caballo, double monto, boolean gano, String date) {

			this.caballo = caballo;
			this.monto = monto;
			this.gano = gano;
			fecha = new GregorianCalendar(TimeZone.getTimeZone("GMT-5"));

			try {
				fecha.setTime(DATE_FORMAT.parse(date));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			;

		}

		/**
		 * Cadena que representa una apuesta.
		 */
		@Override
		public String toString() {
			return caballo + ";" + monto + ";" + gano + ";" + DATE_FORMAT.format(fecha.getTime());
		}

	}

	/**
	 * Método principal que hace las veces de controlador del sistema.
	 * @param args - Argumentos del sistema
	 */
	public static void main(String[] args) {

		Server s = new Server();

		s.cargarDatos();

		s.start();
		try {
			Thread.sleep(TIEMPO_OPERACION);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Apuestas cerradas");

		s.iniciarCarrera();
		s.iniciarTransmision();

	}

}
