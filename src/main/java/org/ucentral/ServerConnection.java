package org.ucentral;

import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServerConnection {
    private static ServerConnection instance;
    private Map<Integer, Boolean> conexiones = new HashMap<>();
    private static final int CONNECTION_TIMEOUT = 3000; // Timeout de 3 segundos
    
    // Constructor privado para Singleton
    private ServerConnection() {}
    
    // Método para obtener la instancia única
    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }
    
    // Método para conectar a todos los servidores
    public void conectar() {
        for (ConfigLoader.ServidorConfig servidor : ConfigLoader.getServidores()) {
            conectarAlServidor(servidor);
        }
    }
    
    // Método para conectar a un servidor específico
    private void conectarAlServidor(ConfigLoader.ServidorConfig servidor) {
        // Si ya sabemos que está conectado, no hacer nada
        if (Boolean.TRUE.equals(conexiones.get(servidor.getPuerto()))) {
            return;
        }
        
        try {
            Socket socket = new Socket(servidor.getIp(), servidor.getPuerto());
            socket.close();
            System.out.println("✅ Conexión establecida con el servidor en " + 
                                servidor.getIp() + ":" + servidor.getPuerto());
            conexiones.put(servidor.getPuerto(), true);
        } catch (IOException e) {
            System.out.println("❌ No se pudo conectar al servidor en " + 
                                servidor.getIp() + ":" + servidor.getPuerto());
            conexiones.put(servidor.getPuerto(), false);
        }
    }
    
    // Método para enviar ping a todos los servidores
    public boolean enviarPing() {
        boolean todosActivos = true;
        
        for (ConfigLoader.ServidorConfig servidor : ConfigLoader.getServidores()) {
            boolean estadoServidor = enviarPingAlServidor(servidor);
            todosActivos = todosActivos && estadoServidor;
        }
        
        return todosActivos;
    }
    
    // Método para enviar ping a un servidor específico
    public boolean enviarPingAlServidor(ConfigLoader.ServidorConfig servidor) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(servidor.getIp(), servidor.getPuerto()), CONNECTION_TIMEOUT);
            socket.close();
            conexiones.put(servidor.getPuerto(), true);
            return true;
        } catch (IOException e) {
            System.out.println("❌ El servidor en " + servidor.getIp() + ":" + 
                               servidor.getPuerto() + " no responde al ping.");
            conexiones.put(servidor.getPuerto(), false);
            return false;
        }
    }
    
    // Método para verificar si un servidor específico está activo
    public boolean isServidorActivo(ConfigLoader.ServidorConfig servidor) {
        return Boolean.TRUE.equals(conexiones.get(servidor.getPuerto()));
    }
    
    // Método general para comprobar si todos los servidores están activos
    public boolean isServidorActivo() {
        for (Boolean activo : conexiones.values()) {
            if (Boolean.FALSE.equals(activo)) {
                return false;
            }
        }
        return !conexiones.isEmpty();
    }
    
    // Método para resetear todas las conexiones
    public void resetearConexion() {
        conexiones.clear();
    }
    
    // Método para verificar si un puerto específico ya está en uso
    public boolean isPuertoEnUso(String host, int puerto) {
        try (Socket socket = new Socket()) {
            // Intentamos conectarnos con un timeout específico
            socket.connect(new InetSocketAddress(host, puerto), CONNECTION_TIMEOUT);
            // Si llegamos aquí, el puerto está en uso
            return true;
        } catch (IOException e) {
            // Si hay excepción, el puerto no está en uso
            return false;
        }
    }
}
