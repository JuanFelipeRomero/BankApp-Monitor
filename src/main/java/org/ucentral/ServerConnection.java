package org.ucentral;

import java.io.*;
import java.net.Socket;

public class ServerConnection {
    private static ServerConnection instance; // Instancia única
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean servidorActivo;
    private static final int PORT = ConfigLoader.getPort(); // Ajusta el puerto según tu aplicación
    private static final String HOST = ConfigLoader.getHost();

    // Metodo para obtener la instancia única
    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    // Constructor privado para el patrón singleton
    private ServerConnection() {
        this.servidorActivo = false;
    }

    // Devuelve el estado actual de la conexión
    public boolean isServidorActivo() {
        return servidorActivo;
    }

    // Metodo para conectarse; solo intenta conectar si no hay conexión activa
    public void conectar() {
        if (!servidorActivo) {
            try {
                if (socket == null || socket.isClosed()) {
                    socket = new Socket(HOST, PORT); // Conexión al servidor
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    servidorActivo = true;
                    System.out.println("Conexión al servidor establecida.");
                }
            } catch (IOException e) {
                servidorActivo = false;
                System.err.println("\n------------ Fallo en el servidor ----------- ");
                System.err.println("No se pudo conectar al servidor: " + e.getMessage());
            }
        } else {
            System.out.println("La conexión ya está activa.");
        }
    }

    // Enviar ping utilizando la conexión actual
    public boolean enviarPing() {
        if (servidorActivo && out != null && in != null) {
            try {
                String mensajePing = "{\"tipoOperacion\":\"ping\"}";
                out.println(mensajePing);
                System.out.println("Ping enviado al servidor: " + mensajePing);

                String respuesta = in.readLine();
                if (respuesta != null && respuesta.contains("pong")) {
                    System.out.println("Respuesta de ping recibida: " + respuesta);
                    return true;
                } else {
                    System.err.println("No se recibió una respuesta de ping válida.");
                }
            } catch (IOException e) {
                System.err.println("Error al enviar/recibir el ping: " + e.getMessage());
                servidorActivo = false;
                // Cerrar el socket para permitir una reconexión limpia
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            System.err.println("No hay conexión activa para enviar el ping.");
        }
        return false;
    }

    // Metodo para resetear la conexión
    public void resetearConexion() {
        servidorActivo = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
        out = null;
        in = null;
    }
}
