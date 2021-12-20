package racoi.Scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import racoi.Job.JobConfiguration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
/*
@Slf4j
@Component
public class JobScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobConfiguration jobConfiguration;


    @Scheduled(cron = "0 0 0/1 * * *")
    public void runInternet() {

        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(LocalDateTime.now().toString()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(jobConfiguration.internet_job(), jobParameters);

        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException | org.springframework.batch.core.repository.JobRestartException e) {

            log.error(e.getMessage());
        }
    }



    @Scheduled(cron = "0 0 0/1 * * *")
    public void runCompre() {

        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(LocalDateTime.now().toString()));
        JobParameters jobParameters = new JobParameters(confMap);

        try {
            jobLauncher.run(jobConfiguration.compre_job(), jobParameters);

        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException | org.springframework.batch.core.repository.JobRestartException e) {

            log.error(e.getMessage());
        }
    }

}*/


