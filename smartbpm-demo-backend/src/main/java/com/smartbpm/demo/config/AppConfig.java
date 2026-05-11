package com.smartbpm.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartbpm.demo.application.clients.AiWorkerClient;
import com.smartbpm.demo.application.clients.TransformClient;
import com.smartbpm.demo.application.clients.ValidatorClient;
import com.smartbpm.demo.application.clients.local.LocalAiWorkerClient;
import com.smartbpm.demo.application.clients.local.LocalTransformClient;
import com.smartbpm.demo.application.clients.local.LocalValidatorClient;
import com.smartbpm.demo.service.ai.AiGateway;
import com.smartbpm.demo.service.ai.OpenAiGateway;
import com.smartbpm.demo.service.compiler.BpmnCompilerService;
import com.smartbpm.demo.service.storage.FileSystemStorageService;
import com.smartbpm.demo.service.storage.S3StorageService;
import com.smartbpm.demo.service.storage.StorageService;
import com.smartbpm.demo.service.validation.ProcessValidationService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(SmartBpmProperties.class)
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public WebClient webClient(SmartBpmProperties properties) {
        return WebClient.builder().baseUrl(properties.getAi().getOpenaiBaseUrl()).build();
    }

    @Bean
    public AiGateway aiGateway(SmartBpmProperties properties, WebClient webClient, ObjectMapper objectMapper) {
        return new OpenAiGateway(webClient, objectMapper, properties);
    }

    @Bean
    public StorageService storageService(SmartBpmProperties properties) {
        if ("s3".equalsIgnoreCase(properties.getStorage().getMode())) {
            Region region = Region.of(properties.getAws().getRegion());
            return new S3StorageService(S3Client.builder().region(region).build(), properties);
        }
        return new FileSystemStorageService(properties);
    }

    @Bean
    public LambdaClient lambdaClient(SmartBpmProperties properties) {
        return LambdaClient.builder().region(Region.of(properties.getAws().getRegion())).build();
    }

    @Bean
    public AiWorkerClient aiWorkerClient(
            com.smartbpm.demo.service.ai.AiWorkerService aiWorkerService) {
        return new LocalAiWorkerClient(aiWorkerService);
    }

    @Bean
    public TransformClient transformClient(BpmnCompilerService compilerService) {
        return new LocalTransformClient(compilerService);
    }

    @Bean
    public ValidatorClient validatorClient(ProcessValidationService validationService) {
        return new LocalValidatorClient(validationService);
    }
}
