package edu.escuelaing.arep;

import java.net.*;
import java.io.*;

public class ServiceFacade {
    public static void main(String[] args) throws IOException {
        int port = 35000;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor de fachada escuchando en el puerto " + port);
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    String command = sacarComando(in);

                    if (command != null) {
                        String response = enviarSolicitudCalculo(command);
                        enviarRespuesta(out, "application/json", response);
                    }
                }
            }
        }
    }

    private static String sacarComando(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println("Recibí: " + line);
            if (line.startsWith("GET /computar?comando=")) {
                return line.split("=")[1].split(" ")[0];
            }
            if (!in.ready()) {
                break;
            }
        }
        return null;
    }

    private static String enviarSolicitudCalculo(String command) throws IOException {
        URL calcServiceUrl = new URL("http://localhost:36000/compreflex?comando=" + command);
        HttpURLConnection connection = (HttpURLConnection) calcServiceUrl.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader calcIn = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return calcIn.readLine();
        }
    }

    private static void enviarRespuesta(PrintWriter out, String contentType, String body) {
        out.println("HTTP/1.1 200 OK\r\n" + "Content-Type: " + contentType + "\r\n\r\n" + body);
    }


}
