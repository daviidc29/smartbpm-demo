package com.smartbpm.demo.lambda;

import com.smartbpm.demo.domain.model.IntermediateProcess;
import com.smartbpm.demo.domain.model.TransformResult;
import com.smartbpm.demo.service.compiler.BpmnCompilerService;

public class TransformerLambdaHandler {

    private final BpmnCompilerService compilerService;

    public TransformerLambdaHandler(BpmnCompilerService compilerService) {
        this.compilerService = compilerService;
    }

    public TransformResult handleRequest(IntermediateProcess process) {
        return compilerService.compile(process);
    }
}
