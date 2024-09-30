package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit implements AutoCloseable {

    private static Connection connection;
    private static Properties config;

    public AlertRabbit() {
        init();
    }

    private void init() {
        try (InputStream input = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config = new Properties();
            config.load(input);
            Class.forName(config.getProperty("driver_class"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    private static int getInterval() {
        return Integer.parseInt(config.getProperty("rabbit.interval"));
    }

    public static void main(String[] args) {
        AlertRabbit rabbit = new AlertRabbit();
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("store", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(getInterval())
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    public static class Rabbit implements Job {
        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            try (PreparedStatement statement = (
                    (Connection) context.getJobDetail().getJobDataMap().get("store")
            ).prepareStatement(
                    "INSERT INTO rabbit(created_date) VALUES(?)"
            )) {
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().withNano(0)));
                statement.execute();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }
}
