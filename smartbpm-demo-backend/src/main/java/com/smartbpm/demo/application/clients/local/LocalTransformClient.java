package com.smartbpm.demo.application.clients.local;

import com.smartbpm.demo.application.clients.TransformClient;
import com.smartbpm.demo.domain.model.IntermediateProcess;
import com.smartbpm.demo.domain.model.TransformResult;
import com.smartbpm.demo.service.compiler.BpmnCompilerService;

public class LocalTransformClient implements TransformClient {

    private final BpmnCompilerService compilerService;

    public LocalTransformClient(BpmnCompilerService compilerService) {
        this.compilerService = compilerService;
    }

    @Override
    public TransformResult transform(IntermediateProcess process) {
        return compilerService.compile(process);
    }
}
