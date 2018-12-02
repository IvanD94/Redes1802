package web;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import web.Server.Apuesta;

/**
 * Clase encargada de proveer un servicio web para que un usuario puede ver su
 * historial de apuestas.
 * 
 * @author IvanD94
 *
 */
public class Web {

	/**
	 * Colección que contiene los usuarios registrados en el sistema.
	 */
	HashMap<String, Usuario> usuarios;

	/**
	 * Constructor de la clase que se encarga de solicitar el proceso de carga de
	 * datos y de iniciar el servidor en el puerto 80.
	 */
	public Web() {

		cargarDatos();

		try (ServerSocket serverSocket = new ServerSocket(80);) {

			System.out.println("Servidor listo");

			while (true) {

				Socket socketCliente = serverSocket.accept();
				System.out.println("Conexion establecida");

				// Se genera un hilo por cada solicitud.
				Client client = new Client(socketCliente);
				client.start();

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clase encagarda de realizar el manejo de las solicitudes de los usuarios.
	 * 
	 * @author IvanD94
	 *
	 */
	class Client extends Thread {

		/**
		 * Socket por el que llega la solicitud del cliente.
		 */
		Socket socket;

		/**
		 * Constructor de la clase, asigna los valores ingresados por parametro.
		 * 
		 * @param socket
		 *            - Socket por el que llega la solicitud del cliente.
		 */
		public Client(Socket socket) {
			this.socket = socket;
		}

		/**
		 * Método que se encarga de realizar el manejo de las solicitudes.
		 */
		public void recibirSolicitud() {

			try (BufferedReader sIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

				String[] dat = sIn.readLine().split(" ");

				System.out.println(Arrays.toString(dat));

				// Accesos a un página en especifico.
				if (dat[0].equals("GET")) {

					StringBuilder responseBuffer = new StringBuilder();

					String str = "";
					// Respuesta por defecto
					String contentType = "text/html";

					// Página inicial. Se lee y se envía el archivo index.html
					if (dat[1].equals("/")) {

						BufferedReader in = new BufferedReader(new FileReader("./web/index.html"));

						while ((str = in.readLine()) != null) {
							responseBuffer.append(str);
						}

						in.close();
						sendResponse(socket, 200, contentType, responseBuffer.toString());

						// Icono de la página
					} else if (dat[1].equals("/favicon.ico")) {

						// Se realiza un proceso de envío independiente diferente a las otras.
						PrintStream out = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));

						out.print("HTTP/1.0 200 OK\r\nContent-type: image/x-icon\r\n\r\n");

						// Se crea un lector para la imagen.
						FileInputStream f = new FileInputStream("./web/favicon.ico");

						byte[] a = new byte[4096];
						int n = 0;

						// Se envía a medida que se va leyendo el archivo.
						while ((n = f.read(a)) > 0) {
							out.write(a, 0, n);
						}
						out.close();
						f.close();

						// Solicitud con parametros
					} else if (dat[1].charAt(1) == '?') {

						// Se separan los parametros
						dat = dat[1].substring(2, dat[1].length()).split("&");

						// Se toma la información de cada parametro
						String id = dat[0].split("=")[1];
						String password = dat[1].split("=")[1];

						if (usuarios.containsKey(id)) {

							Usuario usuario = usuarios.get(id);

							if (usuario.password.equals(password)) {

								BufferedReader in = new BufferedReader(new FileReader("./web/apuestas.html"));

								while ((str = in.readLine()) != null) {

									// Identifica el final de la tabla en el archivo para realizar la inserción de
									// la información.
									if (str.trim().equals("</table>")) {

										if (usuario.races.isEmpty()) {
											responseBuffer.append("<h2>No hay apuestas realizadas</h2>");
										} else {
											for (Apuesta ap : usuario.races) {

												String[] datos = ap.toString().split(";");

												responseBuffer.append("<tr>\n");
												responseBuffer.append("<td>");
												responseBuffer.append(datos[3]);
												responseBuffer.append("</td>\n");
												responseBuffer.append("<td>");
												responseBuffer.append(datos[0]);
												responseBuffer.append("</td>\n");
												responseBuffer.append("<td>");
												responseBuffer.append(datos[1]);
												responseBuffer.append("</td>\n");
												responseBuffer.append("<td>");
												responseBuffer.append(datos[2].equals("false") ? "Perdio" : "Gano");
												responseBuffer.append("</td>\n");
												responseBuffer.append("</tr>\n");

											}
										}

									}
									responseBuffer.append(str + "\n");
								}

								in.close();

								sendResponse(socket, 200, contentType, responseBuffer.toString());

							} else {

								System.out.println("Contraseña incorrecta");

								BufferedReader in = new BufferedReader(new FileReader("./web/index.html"));

								while ((str = in.readLine()) != null) {

									// Identifica el final del head para hacer la inserción de un script.
									if (str.trim().equals("</head>")) {

										// Inserta un script que se llama al cargar la página que muestra una alerta.
										responseBuffer.append("<script>");
										responseBuffer.append("window.onload=function() {\n");
										responseBuffer.append("alert('Contrasena incorrecta');\n");
										responseBuffer.append("}\n");
										responseBuffer.append("</script>");

									}
									responseBuffer.append(str + "\n");
								}

								in.close();

								sendResponse(socket, 200, contentType, responseBuffer.toString());

							}

						} else {

							System.out.println("Usuario incorrecto");

							BufferedReader in = new BufferedReader(new FileReader("./web/index.html"));

							while ((str = in.readLine()) != null) {

								// Identifica el final del head para hacer la inserción de un script.
								if (str.trim().equals("</head>")) {

									// Inserta un script que se llama al cargar la página que muestra una alerta.
									responseBuffer.append("<script>");
									responseBuffer.append("window.onload=function() {\n");
									responseBuffer.append("alert('Usuario incorrecto');\n");
									responseBuffer.append("}\n");
									responseBuffer.append("</script>");

								}
								responseBuffer.append(str + "\n");
							}

							in.close();

							sendResponse(socket, 200, contentType, responseBuffer.toString());
						}
					}

				} else {

					System.out.println("The HTTP method is not recognized");
					sendResponse(socket, 405, "text/html", "Method Not Allowed");

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Adiciona el encabezado correspondiente a las respuestas y las envía.
		 * 
		 * @param socket
		 *            - Socket por el que se realiza la conexión.
		 * @param statusCode
		 *            - Código de respuesta de la solicitud.
		 * @param contentType
		 *            - Tipo MIME de respuesta.
		 * @param responseString
		 *            - Cuerpo de la respuesta.
		 */
		public void sendResponse(Socket socket, int statusCode, String contentType, String responseString) {

			try (DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

				if (statusCode == 200) {

					out.writeBytes("HTTP/1.0 200 OK" + "\r\n");
					out.writeBytes("Server: WebServer\r\n");
					out.writeBytes("Content-Type: " + contentType + "\r\n");
					out.writeBytes("Content-Length: " + responseString.length() + "\r\n");
					out.writeBytes("\r\n");
					out.writeBytes(responseString);

				} else if (statusCode == 405) {

					out.writeBytes("HTTP/1.0 405 Method Not Allowed" + "\r\n");
					out.writeBytes("\r\n");

				} else {

					out.writeBytes("HTTP/1.0 405 Method Not Allowed" + "\r\n");
					out.writeBytes("\r\n");

				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			recibirSolicitud();
		}

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
				fecha.setTime(Server.DATE_FORMAT.parse(date));
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
			return caballo + ";" + monto + ";" + gano + ";" + Server.DATE_FORMAT.format(fecha.getTime());
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

}
