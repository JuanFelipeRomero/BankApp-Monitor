package org.ucentral;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class Main {
    public static void main(String[] args) {
        System.out.println("------------- MONITOR -------------");
        try {
            // Se crea el Scheduler usando la fábrica de StdSchedulerFactory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // Se define el JobDetail que indica qué Job se ejecutará
            JobDetail job = JobBuilder.newJob(MonitorJob.class)
                    .withIdentity("monitorJob", "group1")
                    .build();

            // Se define un Trigger para que el Job se ejecute ahora y luego cada 5 segundos
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("monitorTrigger", "group1")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(10)
                            .repeatForever())
                    .build();

            // Se programa el Job en el Scheduler con el Trigger definido
            scheduler.scheduleJob(job, trigger);

            // Se inicia el Scheduler
            scheduler.start();

            System.out.println("Monitor de servidor iniciado. Se realizará una comprobación cada 10 segundos.");

        } catch (SchedulerException e) {
            System.err.println("Error al iniciar el scheduler: " + e.getMessage());
            e.printStackTrace();
        }

    }
}