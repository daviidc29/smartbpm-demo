import { forwardRef, useEffect, useImperativeHandle, useRef } from 'react'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'

export type BpmnCanvasHandle = {
  getXml: () => Promise<string>
  getSvg: () => Promise<string>
}

type Props = {
  xml?: string | null
}

const emptyDiagram = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="Definitions_1"
  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="false">
    <bpmn:startEvent id="StartEvent_1" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
        <dc:Bounds x="173" y="102" width="36" height="36" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`

const BpmnCanvas = forwardRef<BpmnCanvasHandle, Props>(({ xml }, ref) => {
  const containerRef = useRef<HTMLDivElement | null>(null)
  const modelerRef = useRef<BpmnModeler | null>(null)

  useEffect(() => {
    if (!containerRef.current) return
    const modeler = new BpmnModeler({ container: containerRef.current })
    modelerRef.current = modeler
    
    return () => {
      modeler.destroy()
      modelerRef.current = null
    }
  }, [])

  useEffect(() => {
    const modeler = modelerRef.current
    if (!modeler) return
    const diagram = xml || emptyDiagram
    
    modeler.importXML(diagram).catch(err => {
      console.warn("bpmn-js importXML abort/error:", err)
    })
  }, [xml])

  useImperativeHandle(ref, () => ({
    async getXml() {
      const result = await modelerRef.current?.saveXML({ format: true })
      return result?.xml ?? ''
    },
    async getSvg() {
      const result = await modelerRef.current?.saveSVG()
      return result?.svg ?? ''
    }
  }))

  return <div className="bpmn-canvas" ref={containerRef} />
})

export default BpmnCanvas
