import { useMemo, useRef, useState } from 'react'
import jsPDF from 'jspdf'
import { svg2pdf } from 'svg2pdf.js'
import { generateProcess, optimizeProcess, persistPdf, updateBpmn } from './api'
import BpmnCanvas, { BpmnCanvasHandle } from './components/BpmnCanvas'
import OptimizationPanel from './components/OptimizationPanel'
import TracePanel from './components/TracePanel'
import ValidationPanel from './components/ValidationPanel'
import { examples } from './examples'
import { ProcessViewResponse } from './types'

function downloadText(fileName: string, content: string, contentType: string) {
  const blob = new Blob([content], { type: contentType })
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = fileName
  anchor.click()
  URL.revokeObjectURL(url)
}

function toBase64(bytes: Uint8Array): string {
  let binary = ''
  bytes.forEach((byte) => { binary += String.fromCharCode(byte) })
  return btoa(binary)
}

export default function App() {
  const [narrative, setNarrative] = useState(examples[0].narrative)
  const [processView, setProcessView] = useState<ProcessViewResponse | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [loadingState, setLoadingState] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [showTechnical, setShowTechnical] = useState(false)

  const originalRef = useRef<BpmnCanvasHandle>(null)
  const optimizedRef = useRef<BpmnCanvasHandle>(null)

  const canRunActions = Boolean(processView?.processId)

  const selectedExample = useMemo(
    () => examples.find((example) => example.narrative === narrative)?.id ?? '',
    [narrative]
  )

  async function handleGenerate() {
    setIsLoading(true)
    setLoadingState('generando')
    setError(null)
    try {
      const response = await generateProcess(narrative)
      setProcessView(response)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unexpected error')
    } finally {
      setIsLoading(false)
      setLoadingState(null)
    }
  }


  async function handleOptimize() {
    if (!processView) return
    setIsLoading(true)
    setLoadingState('optimizando')
    setError(null)
    try {
      const xml = await originalRef.current?.getXml()
      if (xml && xml !== processView.bpmnXml) {
        await updateBpmn(processView.processId, xml)
      }
      const response = await optimizeProcess(processView.processId)
      setProcessView(response)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unexpected error')
    } finally {
      setIsLoading(false)
      setLoadingState(null)
    }
  }

  async function handleExportBpmn() {
    const xml = processView?.optimizedBpmnXml ?? processView?.bpmnXml
    if (!xml) return
    downloadText('smartbpm-process.bpmn', xml, 'application/xml')
  }

  async function handleExportJson() {
    const json = processView?.optimizedIntermediateJson ?? processView?.intermediateJson
    if (!json) return
    downloadText('smartbpm-process.json', json, 'application/json')
  }

  async function handleExportPdf() {
    if (!processView) return
    const canvas = processView.optimizedBpmnXml ? optimizedRef.current : originalRef.current
    const svg = await canvas?.getSvg()
    if (!svg) return

    const container = document.createElement('div')
    container.innerHTML = svg
    const svgElement = container.querySelector('svg')
    if (!svgElement) return

    const width = Number(svgElement.getAttribute('width') ?? 1200)
    const height = Number(svgElement.getAttribute('height') ?? 600)
    const pdf = new jsPDF({
      orientation: width > height ? 'landscape' : 'portrait',
      unit: 'pt',
      format: [width, height]
    })

    await svg2pdf(svgElement, pdf, { x: 0, y: 0 })
    const arrayBuffer = pdf.output('arraybuffer')
    const bytes = new Uint8Array(arrayBuffer)
    const base64 = toBase64(bytes)
    pdf.save('smartbpm-process.pdf')
    await persistPdf(processView.processId, 'smartbpm-process.pdf', base64)
  }

  return (
    <div className="app-shell">
      <header className="hero">
        <div>
          <h1>SmartBPM Demo</h1>
          <p>
            Demo académico para transformar una narrativa en un proceso BPMN, validarlo,
            optimizarlo y dejar trazabilidad del flujo completo.
          </p>
        </div>
      </header>

      <section className="panel input-panel">
        <div className="toolbar">
          <label>
            Ejemplo
            <select
              value={selectedExample}
              onChange={(event) => {
                const example = examples.find((item) => item.id === event.target.value)
                if (example) {
                  setNarrative(example.narrative)
                }
              }}
            >
              {examples.map((example) => (
                <option key={example.id} value={example.id}>{example.label}</option>
              ))}
            </select>
          </label>

          <div className="button-row">
            <button onClick={handleGenerate} disabled={isLoading}>
              {loadingState === 'generando' ? 'Generando...' : 'Generar'}
            </button>
            <button onClick={handleOptimize} disabled={!canRunActions || isLoading}>
              {loadingState === 'optimizando' ? 'Optimizando...' : 'Optimizar'}
            </button>
            <button onClick={handleExportBpmn} disabled={!canRunActions}>Exportar BPMN</button>
            <button onClick={handleExportJson} disabled={!canRunActions}>Exportar JSON</button>
            <button onClick={handleExportPdf} disabled={!canRunActions}>Exportar PDF</button>
          </div>
        </div>

        <textarea
          value={narrative}
          onChange={(event) => setNarrative(event.target.value)}
          rows={7}
          placeholder="Describe el proceso en lenguaje natural..."
        />

        {error && <div className="error-banner">{error}</div>}

        <div className="status-banner">
          <span className="status-badge" data-status={processView?.status ?? 'READY'}>
            Estado: {processView?.status ?? 'LISTO'}
          </span>
          {processView?.processId && <span className="status-id">ID: {processView.processId}</span>}
        </div>
      </section>

      <section className="grid two-col">
        <section className="panel">
          <h3>Diagrama original</h3>
          <BpmnCanvas ref={originalRef} xml={processView?.bpmnXml ?? null} />
        </section>

        <section className="panel">
          <h3>Diagrama optimizado</h3>
          <BpmnCanvas ref={optimizedRef} xml={processView?.optimizedBpmnXml ?? processView?.bpmnXml ?? null} />
          {!processView?.optimizedBpmnXml && (
            <p className="muted">La versión optimizada aparecerá después de usar “Optimizar”.</p>
          )}
        </section>
      </section>

      <section className="grid two-col">
        <ValidationPanel title="Validación original" report={processView?.validationReport ?? null} />
        <OptimizationPanel changes={processView?.optimizationChanges ?? []} />
      </section>

      <section className="grid two-col">
        <TracePanel events={processView?.traceEvents ?? []} />
        <section className="panel">
          <h3>Resumen del pipeline</h3>
          <ol className="pipeline-list">
            <li>Entrada en lenguaje natural</li>
            <li>RAG-lite con documentos de apoyo</li>
            <li>Llamada al worker de IA</li>
            <li>JSON intermedio controlado</li>
            <li>Compilación determinista a BPMN XML</li>
            <li>Validación estructural, de consistencia, XML, semántica y visual</li>
            <li>Visualización y comparación</li>
            <li>Exportación y persistencia de artefactos</li>
          </ol>
        </section>
      </section>

      <div className="collapsible-section">
        <button className="collapsible-trigger" onClick={() => setShowTechnical(!showTechnical)}>
          {showTechnical ? 'Ocultar detalles técnicos' : 'Ver más detalles técnicos (JSON, XML, RAG)'}
        </button>

        {showTechnical && (
          <>
            <section className="grid two-col">
              <section className="panel">
                <h3>JSON intermedio</h3>
                <pre>{processView?.optimizedIntermediateJson ?? processView?.intermediateJson ?? 'No JSON generado yet.'}</pre>
              </section>

              <section className="panel">
                <h3>BPMN XML</h3>
                <pre>{processView?.optimizedBpmnXml ?? processView?.bpmnXml ?? 'No BPMN generado yet.'}</pre>
              </section>
            </section>

            <section className="grid two-col">
              <section className="panel">
                <h3>Contexto RAG</h3>
                <pre>{processView?.ragContextText ?? 'No context generated yet.'}</pre>
              </section>
              <section className="panel">
                <h3>Prompt final</h3>
                <pre>{processView?.finalPrompt ?? 'No prompt generated yet.'}</pre>
              </section>
            </section>
          </>
        )}
      </div>
    </div>
  )
}
