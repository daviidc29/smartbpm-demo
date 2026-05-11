package com.smartbpm.demo.api;

import com.smartbpm.demo.api.dto.GenerateProcessRequest;
import com.smartbpm.demo.api.dto.PersistPdfRequest;
import com.smartbpm.demo.api.dto.ProcessViewResponse;
import com.smartbpm.demo.application.ProcessOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/processes")
public class ProcessController {

    private final ProcessOrchestratorService orchestratorService;

    public ProcessController(ProcessOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/generate")
    public ProcessViewResponse generate(@Valid @RequestBody GenerateProcessRequest request) {
        return orchestratorService.generate(request);
    }


    @PostMapping("/{processId}/optimize")
    public ProcessViewResponse optimize(@PathVariable("processId") String processId) {
        return orchestratorService.optimize(processId);
    }

    @GetMapping("/{processId}")
    public ProcessViewResponse get(@PathVariable("processId") String processId) {
        return orchestratorService.get(processId);
    }

    @PutMapping(value = "/{processId}/bpmn", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ProcessViewResponse updateBpmn(@PathVariable("processId") String processId, @RequestBody String bpmnXml) {
        return orchestratorService.updateBpmn(processId, bpmnXml);
    }

    @PostMapping("/{processId}/artifacts/pdf")
    public ResponseEntity<Void> uploadPdf(@PathVariable("processId") String processId, @Valid @RequestBody PersistPdfRequest request) {
        orchestratorService.persistPdf(processId, request.fileName(), request.base64Pdf());
        return ResponseEntity.accepted().build();
    }

}
