package com.basecode.ksef.notifier.config;

import com.basecode.ksef.notifier.service.KsefCheckTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    public Job ksefCheckJob(JobRepository jobRepository, Step ksefStep) {
        return new JobBuilder("ksefCheckJob", jobRepository)
            .start(ksefStep)
            .build();
    }

    @Bean
    public Step ksefStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, KsefCheckTasklet tasklet) {
        return new StepBuilder("ksefStep", jobRepository)
            .tasklet(tasklet, transactionManager)
            .build();
    }
}
