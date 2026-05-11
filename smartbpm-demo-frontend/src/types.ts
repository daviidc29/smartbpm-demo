export type EventRef = {
  id: string
  name: string
}

export type ProcessTask = {
  id: string
  name: string
  role: string
  order: number
  automated: boolean
}

export type DecisionBranch = {
  id: string
  condition: string
  targetRef: string
}

export type ProcessDecision = {
  id: string
  name: string
  order: number
  type: string
  mergeId: string
  branches: DecisionBranch[]
}

export type SequenceFlowRef = {
  id: string
  sourceRef: string
  targetRef: string
  conditionLabel?: string | null
}

export type IntermediateProcess = {
  processName: string
  roles: string[]
  startEvent: EventRef
  endEvent: EventRef
  tasks: ProcessTask[]
  decisions: ProcessDecision[]
  sequenceFlows: SequenceFlowRef[]
  assumptions: string[]
  warnings: string[]
}

export type ValidationIssue = {
  layer: string
  severity: string
  message: string
}

export type ValidationReport = {
  valid: boolean
  issues: ValidationIssue[]
}

export type OptimizationChange = {
  title: string
  before: string
  after: string
  reason: string
}

export type TraceEventView = {
  stage: string
  status: string
  message: string
  createdAt: string
}

export type ProcessViewResponse = {
  processId: string
  status: string
  originalText: string
  ragContextText: string
  finalPrompt: string
  llmRawResponse: string
  intermediateJson: string
  intermediateProcess: IntermediateProcess
  bpmnXml: string
  validationReport: ValidationReport
  optimizedIntermediateJson?: string | null
  optimizedProcess?: IntermediateProcess | null
  optimizedBpmnXml?: string | null
  optimizedValidationReport?: ValidationReport | null
  optimizationChanges: OptimizationChange[]
  artifactKeys: Record<string, string>
  traceEvents: TraceEventView[]
  createdAt: string
  updatedAt: string
}
