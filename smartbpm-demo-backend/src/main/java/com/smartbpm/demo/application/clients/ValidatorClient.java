package com.smartbpm.demo.application.clients;

import com.smartbpm.demo.domain.model.IntermediateProcess;
import com.smartbpm.demo.domain.model.ValidationReport;

public interface ValidatorClient {
    ValidationReport validate(String narrative, IntermediateProcess process, String bpmnXml);
}
