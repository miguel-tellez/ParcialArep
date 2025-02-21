package edu.escuelaing.arep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ReflexCalculator {
    public static void main(String[] args) throws Exception {
        int port = 36000;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor calculadora escuchando en el puerto " + port);
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    String command = getCommand(in);

                    if (command != null) {
                        String operation = removeParams(command);
                        List params = getParams(command);
                        String result = executeOperation(operation, params);
                        sendResponse(out, "{\"resultado\": " + result + "}");
                    }
                }
            }
        }
    }

    private static String getCommand(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println("Recibí: " + line);
            if (line.startsWith("GET /compreflex?comando=")) {
                return line.split("=")[1].split(" ")[0];
            }
            if (!in.ready()) {
                break;
            }
        }
        return null;
    }

    private static String removeParams(String input) {
        return input.substring(0, input.indexOf('('));
    }

    private static List getParams(String input) {
        List params = new ArrayList<>();
        String paramStr = input.substring(input.indexOf('(') + 1, input.indexOf(')'));
        if (!paramStr.isEmpty()) {
            for (String param : paramStr.split(",")) {
                params.add(Double.parseDouble(param.trim()));
            }
        }
        return params;
    }

    private static String executeOperation(String operation, List params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if ("pi".equals(operation)) {
            return String.valueOf(Math.PI);
        } else if ("bbl".equals(operation)) {
            return bubbleSort(params).toString();
        } else {
            return executeMathOperation(operation, params);
        }
    }

    private static String executeMathOperation(String operation, List params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method;
        Class<?> mathClass = Math.class;
        if (params.size() == 1) {
            method = mathClass.getMethod(operation, double.class);
            return String.valueOf(method.invoke(null, params.get(0)));
        } else if (params.size() == 2) {
            method = mathClass.getMethod(operation, double.class, double.class);
            return String.valueOf(method.invoke(null, params.get(0), params.get(1)));
        } else {
            throw new IllegalArgumentException("Número incorrecto de parámetros");
        }
    }

    private static void sendResponse(PrintWriter out, String body) {
        out.println("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + body);
    }

    public static List bubbleSort(List list) {
        int n = list.size();
        boolean buble = true;

        while (buble) {
            buble = false;
            for (int j = 0; j < n - 1; j++) {
                if (list.get(j) > list.get(j + 1)) {
                    double temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                    buble= true;
                }
            }
        }
        return list;
    }

}
