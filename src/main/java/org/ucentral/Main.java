package org.ucentral;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args) {
        System.out.println("------------- MONITOR -------------");
        try {
            // 1️⃣ Iniciar el servidor RMI en un hilo separado
            new Thread(() -> iniciarRMI()).start();

            // Se crea el Scheduler usando la fábrica de StdSchedulerFactory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // Se define el JobDetail que indica qué Job se ejecutará
            JobDetail job = JobBuilder.newJob(MonitorJob.class)
                    .withIdentity("monitorJob", "group1")
                    .build();

            //Obtener intervalo de properties
            int intervalo = ConfigLoader.getIntervalo();

            // Se define un Trigger para que el Job se ejecute ahora y luego cada X segundos
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("monitorTrigger", "group1")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalo) //usar intervalo de properties
                            .repeatForever())
                    .build();

            // Se programa el Job en el Scheduler con el Trigger definido
            scheduler.scheduleJob(job, trigger);

            // Se inicia el Scheduler
            scheduler.start();

            System.out.println("Monitor de servidor iniciado. Se realizará una comprobación cada " + intervalo + " segundos.");

        } catch (SchedulerException e) {
            System.err.println("Error al iniciar el scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void iniciarRMI() {
        try {
            Monitor monitor = new Monitor();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MonitorService", (Remote) monitor);
            System.out.println("✅ Servidor RMI del Monitor en ejecución...");
        } catch (RemoteException e) {
            System.err.println("❌ Error al iniciar el servidor RMI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}