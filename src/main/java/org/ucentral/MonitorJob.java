package org.ucentral;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class MonitorJob implements Job {
    // Nombre del archivo JAR del servidor - fácil de modificar si cambia
    private static final String SERVER_JAR_NAME = ConfigLoader.getServidorJar();
    
    // Map para rastrear los últimos reinicios por puerto
    private static final Map<Integer, LocalDateTime> lastRestartAttempts = new HashMap<>();
    
    // Set para mantener registro de los servidores que ya están iniciados
    private static final Set<Integer> servidoresActivos = new HashSet<>();
    
    // Tiempo mínimo entre reinicios del mismo servidor (en segundos)
    private static final int RESTART_COOLDOWN_SECONDS = 30;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("Ejecutando MonitorJob...");

        // Obtener la instancia UNICA de ServerConnection
        ServerConnection serverConnection = ServerConnection.getInstance();

        // Intentar conectarse a todos los servidores
        serverConnection.conectar();

        // Verificar cada servidor individualmente
        for (ConfigLoader.ServidorConfig servidor : ConfigLoader.getServidores()) {
            verificarYReiniciarServidor(serverConnection, servidor);
        }
    }
    
    // Verifica y reinicia un servidor específico si es necesario
    private void verificarYReiniciarServidor(ServerConnection connection, ConfigLoader.ServidorConfig servidor) {
        int puerto = servidor.getPuerto();
        boolean pingExitoso = connection.enviarPingAlServidor(servidor);
        
        // Si el ping fue exitoso, registramos que este servidor está activo
        if (pingExitoso) {
            servidoresActivos.add(puerto);
            System.out.println("\n----------- El servidor " + servidor.getIp() + ":" + 
                              puerto + " está activo y respondió correctamente -----------");
            return;
        }
        
        // Verificar si el puerto ya está en uso aunque el ping fallara
        if (connection.isPuertoEnUso(servidor.getIp(), puerto)) {
            System.out.println("ℹ️ El puerto " + puerto + " ya está en uso, pero no respondió al ping estructurado.");
            servidoresActivos.add(puerto);
            return;
        }
        
        // Verificar si el servidor ya está registrado como activo
        if (servidoresActivos.contains(puerto)) {
            System.out.println("⚠️ El servidor en puerto " + puerto + 
                              " está registrado como activo pero no responde. Verificando nuevamente...");
                              
            // Hacemos una segunda verificación para confirmar que realmente está caído
            if (connection.isPuertoEnUso(servidor.getIp(), puerto)) {
                System.out.println("✅ Confirmado: El servidor en puerto " + puerto + " sigue activo.");
                return;
            } else {
                // Si realmente está caído, lo quitamos de activos para permitir reinicio
                System.out.println("❌ Confirmado: El servidor en puerto " + puerto + " está caído.");
                servidoresActivos.remove(puerto);
            }
        }
        
        System.err.println("El servidor " + servidor.getIp() + ":" + puerto + 
                         " no respondió al ping. Se procederá a reiniciar.");
                         
        // Verificar si el servidor fue reiniciado recientemente
        LocalDateTime lastRestart = lastRestartAttempts.get(puerto);
        LocalDateTime now = LocalDateTime.now();
        
        if (lastRestart != null && lastRestart.plusSeconds(RESTART_COOLDOWN_SECONDS).isAfter(now)) {
            System.out.println("⚠️ El servidor en puerto " + puerto + 
                              " fue reiniciado recientemente. Esperando " + 
                              RESTART_COOLDOWN_SECONDS + " segundos entre intentos.");
            return;  // No reiniciar, todavía en período de enfriamiento
        }
        
        // Se reinicia el servidor y registra el tiempo de reinicio
        boolean reinicioExitoso = restartServer(puerto);
        if (reinicioExitoso) {
            servidoresActivos.add(puerto);
        }
        lastRestartAttempts.put(puerto, now);
    }

    // Método para reiniciar el servidor mediante la ejecución del JAR
    private boolean restartServer(int puerto) {
        try {
            // Obtener directorio actual para construir ruta absoluta
            String currentDir = System.getProperty("user.dir");
            String jarPath = new java.io.File(currentDir + "/" + SERVER_JAR_NAME).getCanonicalPath();
            
            System.out.println("Ejecutando comando para reiniciar el servidor en puerto " + puerto);
            System.out.println("Ruta del JAR: " + jarPath);

            // Se utiliza ProcessBuilder para ejecutar el comando del sistema
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("java", "-jar", jarPath, String.valueOf(puerto));
            
            // Redirigir salida del proceso al monitor para mejor diagnóstico
            processBuilder.redirectErrorStream(true);
            
            // Iniciar el proceso
            Process process = processBuilder.start();
            
            // Dar tiempo al servidor para inicializarse (5 segundos)
            System.out.println("Esperando inicialización del servidor en puerto " + puerto + "...");
            Thread.sleep(5000);
            
            // Verificar si el proceso sigue vivo
            if (process.isAlive()) {
                System.out.println("✅ Servidor iniciado en el puerto " + puerto);
                return true;
            } else {
                int exitCode = process.exitValue();
                System.err.println("❌ El servidor en puerto " + puerto + " falló al iniciar (código: " + exitCode + ")");
                
                // Leer salida del proceso para diagnóstico
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("Servidor [" + puerto + "]: " + line);
                    }
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error al intentar reiniciar el servidor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}