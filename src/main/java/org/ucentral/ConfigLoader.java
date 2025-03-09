package org.ucentral;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class ConfigLoader {
    private static Properties propiedades = new Properties();
    private static List<ServidorConfig> servidores = new ArrayList<>();

    static {
        try {
            // Cargar archivo de propiedades
            FileInputStream fis = new FileInputStream("config.monitor.properties");
            propiedades.load(fis);
            fis.close();
            
            // Cargar configuración de servidores desde JSON
            cargarConfigServidores();
        } catch (IOException e) {
            System.err.println("Error al cargar archivo de propiedades: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void cargarConfigServidores() {
        try (JsonReader reader = Json.createReader(new FileReader("config.json"))) {
            JsonObject jsonObject = reader.readObject();
            JsonArray jsonServidores = jsonObject.getJsonArray("servidores");
            
            for (int i = 0; i < jsonServidores.size(); i++) {
                JsonObject servidor = jsonServidores.getJsonObject(i);
                String ip = servidor.getString("ip");
                int puerto = servidor.getInt("puerto");
                servidores.add(new ServidorConfig(ip, puerto));
            }
            
            System.out.println("Configuración cargada: " + servidores.size() + " servidores");
        } catch (IOException e) {
            System.err.println("Error al cargar configuración JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Métodos para obtener los valores de configuración
    public static int getIntervalo() {
        return Integer.parseInt(propiedades.getProperty("intervalo"));
    }

    public static String getHost() {
        return propiedades.getProperty("host");
    }

    public static int getPort() {
        return Integer.parseInt(propiedades.getProperty("puerto"));
    }

    public static String getServidorJar() {
        return propiedades.getProperty("servidor_jar");
    }
    
    public static List<ServidorConfig> getServidores() {
        return servidores;
    }
    
    // Clase interna para representar la configuración de un servidor
    public static class ServidorConfig {
        private String ip;
        private int puerto;
        
        public ServidorConfig(String ip, int puerto) {
            this.ip = ip;
            this.puerto = puerto;
        }
        
        public String getIp() {
            return ip;
        }
        
        public int getPuerto() {
            return puerto;
        }
        
        @Override
        public String toString() {
            return "ServidorConfig{ip='" + ip + "', puerto=" + puerto + "}";
        }
    }
}
