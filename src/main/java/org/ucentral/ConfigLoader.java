package org.ucentral;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static Properties propiedades = new Properties();

    static {
        try {
            //Cargar archivo de propiedades
            FileInputStream fis = new FileInputStream("config.monitor.properties");
            propiedades.load(fis);
            fis.close();
        } catch (IOException e) {
            System.err.println("Error al cargar archivo de propiedades: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Metodos para obtener los valores de configuaracion
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
}
