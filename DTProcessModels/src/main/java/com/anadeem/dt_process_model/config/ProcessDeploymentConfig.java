package com.anadeem.dt_process_model.config;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessDeploymentConfig {

    @Bean
    public CommandLineRunner deployProcess(RepositoryService repositoryService) {
        return args -> {
            // Deploy standard workflow
            Deployment standardDeployment = repositoryService.createDeployment()
                    .addClasspathResource("processes/StandardRepairProcess.bpmn")
                    .name("Standard Repair Workflow Deployment")
                    .deploy();

            System.out.println("Deployed Standard Workflow ID: " + standardDeployment.getId());

            // Deploy express workflow
            Deployment expressDeployment = repositoryService.createDeployment()
                    .addClasspathResource("processes/ExpressRepairProcess.bpmn")
                    .name("Express Repair Workflow Deployment")
                    .deploy();

            System.out.println("Deployed Express Workflow ID: " + expressDeployment.getId());

            // Deploy emergency workflow
            Deployment emergencyDeployment = repositoryService.createDeployment()
                    .addClasspathResource("processes/EmergencyRepairProcess.bpmn")
                    .name("Emergency Repair Workflow Deployment")
                    .deploy();

            System.out.println("Deployed Emergency Workflow ID: " + emergencyDeployment.getId());
        };
    }
}