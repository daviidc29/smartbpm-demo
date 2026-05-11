package com.smartbpm.demo.lambda;

import com.smartbpm.demo.domain.model.IntermediateProcess;
import com.smartbpm.demo.domain.model.ValidationReport;
import com.smartbpm.demo.service.validation.ProcessValidationService;

public class ValidatorLambdaHandler {

    private final ProcessValidationService validationService;

    public ValidatorLambdaHandler(ProcessValidationService validationService) {
        this.validationService = validationService;
    }

    public ValidationReport handleRequest(String narrative, IntermediateProcess process, String xml) {
        return validationService.validate(narrative, process, xml);
    }
}
