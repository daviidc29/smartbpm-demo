/// <reference types="vite/client" />
import { ProcessViewResponse } from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081/api'

export async function generateProcess(narrative: string): Promise<ProcessViewResponse> {
  const response = await fetch(`${API_BASE_URL}/processes/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ narrative })
  })
  if (!response.ok) {
    throw new Error(await response.text())
  }
  return response.json()
}


export async function optimizeProcess(processId: string): Promise<ProcessViewResponse> {
  const response = await fetch(`${API_BASE_URL}/processes/${processId}/optimize`, { method: 'POST' })
  if (!response.ok) {
    throw new Error(await response.text())
  }
  return response.json()
}

export async function getProcess(processId: string): Promise<ProcessViewResponse> {
  const response = await fetch(`${API_BASE_URL}/processes/${processId}`)
  if (!response.ok) {
    throw new Error(await response.text())
  }
  return response.json()
}

export async function updateBpmn(processId: string, xml: string): Promise<ProcessViewResponse> {
  const response = await fetch(`${API_BASE_URL}/processes/${processId}/bpmn`, {
    method: 'PUT',
    headers: { 'Content-Type': 'text/plain' },
    body: xml
  })
  if (!response.ok) {
    throw new Error(await response.text())
  }
  return response.json()
}

export async function persistPdf(processId: string, fileName: string, base64Pdf: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/processes/${processId}/artifacts/pdf`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ fileName, base64Pdf })
  })
  if (!response.ok) {
    throw new Error(await response.text())
  }
}

