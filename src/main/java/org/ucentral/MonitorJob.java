package org.ucentral;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MonitorJob implements Job {
    // Nombre del archivo JAR del servidor - fácil de modificar si cambia

    private static final String SERVER_JAR_NAME = ConfigLoader.getServidorJar();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("Ejecutando MonitorJob...");

        // Obtener la instancia UNICA de ServerConnection
        ServerConnection serverConnection = ServerConnection.getInstance();

        // Intentar conectarse solo si no hay una conexión activa
        serverConnection.conectar();

        // Enviar ping para verificar si el servidor responde
        boolean pingExitoso = serverConnection.enviarPing();

        if (!pingExitoso) {
            System.err.println("El servidor no respondió al ping. Se procederá a reiniciar el servidor.");
            // Se reinicia el servidor
            restartServer();
            // Se resetea la conexión para que en la siguiente ejecución se intente reconectar
            serverConnection.resetearConexion();
        } else {
            System.out.println("\n----------- El servidor está activo y respondió correctamente al ping -----------------");
        }
    }

    // Metodo para reiniciar el servidor mediante la ejecución del JAR
    private void restartServer() {
        try {
            // Comando para ejecutar el JAR del servidor que está en la misma carpeta que el monitor
            //para hacer las pruebas el archivo del servidor junto con el env estan en la carpeta del proyecto
            String command = "java -jar ../" + SERVER_JAR_NAME;

            System.out.println("Ejecutando comando para reiniciar el servidor: " + command);

            // Se utiliza ProcessBuilder para ejecutar el comando del sistema
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            // Establece el directorio de trabajo al directorio actual
            // Esto asegura que el comando se ejecute desde donde está el JAR del monitor
            processBuilder.start();

        } catch (Exception e) {
            System.err.println("Error al intentar reiniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}