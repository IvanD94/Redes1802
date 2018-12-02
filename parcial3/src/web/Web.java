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

public class Web {
	
	HashMap<String, Usuario> usuarios;

	public Web() {		
		
		cargarDatos();

		try (ServerSocket serverSocket = new ServerSocket(80);) {

			System.out.println("Servidor listo");

			while (true) {

				Socket socketCliente = serverSocket.accept();
				System.out.println("Conexion establecida");

				Client client = new Client(socketCliente);
				client.start();

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class Client extends Thread {

		Socket socket;

		public Client(Socket socket) {
			this.socket = socket;
		}

		public void recibirSolicitud() {

			try (BufferedReader sIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

				String[] dat = sIn.readLine().split(" ");

				System.out.println(Arrays.toString(dat));

				if (dat[0].equals("GET")) {

					StringBuilder responseBuffer = new StringBuilder();

					String str = "";
					String contentType = "text/html";

					if (dat[1].equals("/")) {

						BufferedReader in = new BufferedReader(new FileReader("./web/index.html"));

						while ((str = in.readLine()) != null) {
							responseBuffer.append(str);
						}

						in.close();
						sendResponse(socket, 200, contentType, responseBuffer.toString());

					} else if (dat[1].equals("/favicon.ico")) {

						PrintStream out = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));

						out.print("HTTP/1.0 200 OK\r\nContent-type: image/x-icon\r\n\r\n");

						FileInputStream f = new FileInputStream("./web/favicon.ico");

						byte[] a = new byte[4096];
						int n = 0;

						while ((n = f.read(a)) > 0) {
							out.write(a, 0, n);
						}
						out.close();
						f.close();

					}else if (dat[1].charAt(1) == '?' ) {
						
						dat = dat[1].substring(2, dat[1].length()).split("&");
						
						String id = dat[0].split("=")[1];
						String password = dat[1].split("=")[1];
										
						if (usuarios.containsKey(id)) {
							
							Usuario usuario = usuarios.get(id);
							
							if (usuario.password.equals(password)) {
								
								BufferedReader in = new BufferedReader(new FileReader("./web/apuestas.html"));

								while ((str = in.readLine()) != null) {
									
									if (str.trim().equals("</table>")){
										
										if (usuario.races.isEmpty()) {
											responseBuffer.append("<h2>No hay apuestas realizadas</h2>");											
										}else {
											for (Apuesta ap: usuario.races) {
												
												String [] datos = ap.toString().split(";");
												
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
												responseBuffer.append(datos[2].equals("false")?"Perdio":"Gano");
												responseBuffer.append("</td>\n");
												responseBuffer.append("</tr>\n");
												
											}
										}
						
									}
									responseBuffer.append(str+"\n");
								}

								in.close();
								
								sendResponse(socket, 200, contentType, responseBuffer.toString());
								
							}else {
								
								System.out.println("Contraseña incorrecta");
								
								BufferedReader in = new BufferedReader(new FileReader("./web/index.html"));

								while ((str = in.readLine()) != null) {
									
									if (str.trim().equals("</head>")){
										
										responseBuffer.append("<script>");
										responseBuffer.append("window.onload=function() {\n");
										responseBuffer.append("alert('Contrasena incorrecta');\n");
										responseBuffer.append("}\n");
										responseBuffer.append("</script>");
						
									}
									responseBuffer.append(str+"\n");
								}

								in.close();
								
								sendResponse(socket, 200, contentType, responseBuffer.toString());
								
							}
							
						} else {
							
							System.out.println("Usuario incorrecto");
							
							BufferedReader in = new BufferedReader(new FileReader("./web/index.html"));

							while ((str = in.readLine()) != null) {
								
								if (str.trim().equals("</head>")){
									
									responseBuffer.append("<script>");
									responseBuffer.append("window.onload=function() {\n");
									responseBuffer.append("alert('Usuario incorrecto');\n");
									responseBuffer.append("}\n");
									responseBuffer.append("</script>");
									
								}
								responseBuffer.append(str+"\n");
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

	class Apuesta {

		int caballo;
		double monto;
		boolean gano;
		Calendar fecha;

		public Apuesta(int caballo, double monto, boolean gano) {

			this.caballo = caballo;
			this.monto = monto;
			this.gano = gano;
			fecha = Calendar.getInstance(TimeZone.getTimeZone("GMT-5"));

		}

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

		@Override
		public String toString() {
			return caballo + ";" + monto + ";" + gano + ";" + Server.DATE_FORMAT.format(fecha.getTime());
		}

	}
	
	class Usuario {

		String id;
		String password;
		ArrayList<Apuesta> races;

		public Usuario(String id, String password) {
			this.id = id;
			this.password = password;
			races = new ArrayList<>();
		}

		@Override
		public String toString() {
			return id + ";" + password;
		}

		public String apuestas() {
			String apuestasUsuario = "";

			for (Apuesta a : races) {

				apuestasUsuario += id + ";" + a + "\n";
			}

			return apuestasUsuario;
		}

	}

	public static void main(String[] args) {
		new Web();
	}

}
