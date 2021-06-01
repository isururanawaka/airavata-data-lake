package org.apache.airavata.drms.custos.synchronizer;

import org.apache.airavata.drms.custos.synchronizer.datafetcher.CustosDataFetchingJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustosSynchronizer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustosSynchronizer.class);
    private static String configFilePath;


    public static void main(String[] args) {
        SpringApplication.run(CustosSynchronizer.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Starting Custos synchronizer ...");
        if (args.length > 0) {
            configFilePath = args[0];
        }
        configFilePath = "/Users/isururanawaka/Documents/Airavata_Repository/airavata-data-lake" +
                "/data-resource-management-service/drms-custos-synchronizer/src/main/resources/config.yml";

        LOGGER.info("Configuring scheduler ...");
        Utils.initializeConnectors(Utils.loadConfiguration(configFilePath));
        configureScheduler(configFilePath);

    }


    private void configureScheduler(String configPath) throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        Configuration configuration = Utils.loadConfig(configPath);
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("custosDataFetcher", "synchronizer1")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds((int) configuration.getPollingInterval())
                        .repeatForever())
                .build();

        JobDetail job = JobBuilder.newJob(CustosDataFetchingJob.class)
                .withIdentity("myJob", "group1")
                .usingJobData("configurationPath", configPath)
                .build();
        scheduler.start();
        scheduler.scheduleJob(job, trigger);
    }


}