package com.smartbpm.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartbpm")
public class SmartBpmProperties {

    private String corsAllowedOrigins = "*";
    private final Ai ai = new Ai();
    private final Storage storage = new Storage();
    private final Execution execution = new Execution();
    private final Aws aws = new Aws();

    public String getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public Ai getAi() {
        return ai;
    }

    public Storage getStorage() {
        return storage;
    }

    public Execution getExecution() {
        return execution;
    }

    public Aws getAws() {
        return aws;
    }

    public static class Ai {
        private String provider = "fake";
        private String model = "gpt-4.1-mini";
        private String openaiBaseUrl = "https://api.openai.com/v1";

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getOpenaiBaseUrl() {
            return openaiBaseUrl;
        }

        public void setOpenaiBaseUrl(String openaiBaseUrl) {
            this.openaiBaseUrl = openaiBaseUrl;
        }
    }

    public static class Storage {
        private String mode = "filesystem";
        private String filesystemRoot = "./data/artifacts";
        private String s3Bucket = "smartbpm-demo-artifacts";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getFilesystemRoot() {
            return filesystemRoot;
        }

        public void setFilesystemRoot(String filesystemRoot) {
            this.filesystemRoot = filesystemRoot;
        }

        public String getS3Bucket() {
            return s3Bucket;
        }

        public void setS3Bucket(String s3Bucket) {
            this.s3Bucket = s3Bucket;
        }
    }

    public static class Execution {
        private String mode = "local";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    public static class Aws {
        private String region = "us-east-1";
        private String aiWorkerFunction = "smartbpm-ai-worker";
        private String transformerFunction = "smartbpm-transformer";
        private String validatorFunction = "smartbpm-validator";

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getAiWorkerFunction() {
            return aiWorkerFunction;
        }

        public void setAiWorkerFunction(String aiWorkerFunction) {
            this.aiWorkerFunction = aiWorkerFunction;
        }

        public String getTransformerFunction() {
            return transformerFunction;
        }

        public void setTransformerFunction(String transformerFunction) {
            this.transformerFunction = transformerFunction;
        }

        public String getValidatorFunction() {
            return validatorFunction;
        }

        public void setValidatorFunction(String validatorFunction) {
            this.validatorFunction = validatorFunction;
        }
    }
}
