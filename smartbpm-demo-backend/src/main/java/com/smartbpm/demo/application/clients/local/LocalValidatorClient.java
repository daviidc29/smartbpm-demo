package com.smartbpm.demo.application.clients.local;

import com.smartbpm.demo.application.clients.ValidatorClient;
import com.smartbpm.demo.domain.model.IntermediateProcess;
import com.smartbpm.demo.domain.model.ValidationReport;
import com.smartbpm.demo.service.validation.ProcessValidationService;

public class LocalValidatorClient implements ValidatorClient {

    private final ProcessValidationService validationService;

    public LocalValidatorClient(ProcessValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public ValidationReport validate(String narrative, IntermediateProcess process, String bpmnXml) {
        return validationService.validate(narrative, process, bpmnXml);
    }
}
