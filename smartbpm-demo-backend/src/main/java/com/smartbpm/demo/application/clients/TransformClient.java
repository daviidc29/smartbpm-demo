package com.smartbpm.demo.application.clients;

import com.smartbpm.demo.domain.model.IntermediateProcess;
import com.smartbpm.demo.domain.model.TransformResult;

public interface TransformClient {
    TransformResult transform(IntermediateProcess process);
}
