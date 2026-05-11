# SmartBPM Demo — Frontend

The frontend is a single-page React application that provides the full user interface for the SmartBPM demo. Users describe a business process in plain language, trigger the backend pipeline, and interact with the resulting BPMN diagrams — including validation, optimization, side-by-side comparison, and PDF export — without leaving the browser.

---

## Getting Started

These instructions will get the frontend running on your local machine for development and testing. For notes on building and deploying to production, see the [Deployment](#deployment) section.

### Prerequisites

| Tool | Minimum version |
|------|----------------|
| Node.js | 20.x |
| npm | 10.x |
| SmartBPM backend | running (see `../smartbpm-demo-backend/README.md`) |

### Installing

**1. Install dependencies**

```bash
cd smartbpm-demo-frontend
npm install
```

**2. (Optional) Configure the backend URL**

By default the frontend connects to `http://localhost:8081/api`. If the backend runs elsewhere, create a `.env.local` file:

```bash
VITE_API_BASE_URL=http://<your-backend-host>:<port>/api
```

> `.env.local` is excluded from version control by `.gitignore`. Never commit API keys or private URLs.

**3. Start the development server**

```bash
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) in your browser.

**4. Try a quick demo**

Paste the text below into the input area and click **Generate**:

> *"Una empresa maneja el proceso de aprobación de vacaciones de forma completamente manual y con demasiados participantes. El empleado llena un formulario físico y lo entrega a su jefe inmediato, quien revisa fechas, disponibilidad del área y saldo de vacaciones. Luego el documento pasa al coordinador del área, que vuelve a revisar la misma información y valida que no existan otras vacaciones programadas. Después el formulario llega a recursos humanos, donde un auxiliar registra manualmente los datos en Excel y verifica nuevamente el saldo de días. Posteriormente el analista de nómina revisa otra vez las fechas para validar pagos y descuentos, y envía el documento al director administrativo para firma final. Si alguna firma falta o hay un error mínimo, el documento regresa al empleado para empezar parte del proceso otra vez. Una vez aprobado, recursos humanos actualiza otro archivo Excel diferente y envía correos manuales al empleado, al jefe y a seguridad. Finalmente se archivan copias físicas en varias carpetas distintas"*

The BPMN diagram appears on the left panel. Click **Validate** or **Optimize** to explore additional features.

---

## UI Sections

| Section | Description |
|---------|-------------|
| **Natural language input** | Free-text area for process description |
| **Example selector** | Pre-loaded examples to quickly demo the pipeline |
| **Action buttons** | Generate / Validate / Optimize / Export PDF |
| **BPMN original viewer** | Interactive bpmn-js canvas for the generated diagram |
| **BPMN optimized viewer** | Side-by-side canvas for the optimized diagram |
| **Validation panel** | Structured results from the backend validator |
| **Optimization change log** | Human-readable log of every heuristic applied |
| **Traceability panel** | Audit timeline of pipeline steps |
| **Intermediate JSON panel** | Raw JSON used by the BPMN compiler |
| **BPMN XML panel** | Raw XML output for inspection or copy-paste |
| **RAG context panel** | Knowledge snippets retrieved during generation |
| **Final prompt panel** | Exact prompt sent to the language model |

---

## Running the Tests

This academic demo does not include an automated test suite in the frontend. Manual smoke testing covers:

**End-to-end flow test**

1. Start the backend (`mvn spring-boot:run` in `../smartbpm-demo-backend`).
2. Start the frontend (`npm run dev`).
3. Select an example from the dropdown and click **Generate** — the BPMN canvas must render a diagram.
4. Click **Validate** — the validation panel must show at least one result category.
5. Click **Optimize** — the right-hand canvas must update with the optimized BPMN.
6. Click **Export PDF** — the browser must download a `.pdf` file and the backend must confirm receipt.

**Layout regression test**

After any CSS or canvas change, verify that both BPMN canvases display without overlap or clipped elements at viewport widths 1280 px, 1440 px, and 1920 px.

---

## Deployment

The frontend is built as a static bundle and served by a CDN or any static host.

**Build the production bundle**

```bash
npm run build
```

Output goes to `dist/`. Upload the contents of `dist/` to your static host (e.g., AWS S3 + CloudFront, Vercel, Netlify).

**Environment variable for production**

Set `VITE_API_BASE_URL` to your deployed backend URL at build time:

```bash
VITE_API_BASE_URL=https://your-backend.example.com/api npm run build
```

---

## Stack

| Technology | Version | Role |
|-----------|---------|------|
| [React](https://react.dev) | 18.3 | UI framework |
| [Vite](https://vitejs.dev) | 5.4 | Build tool & dev server |
| [TypeScript](https://www.typescriptlang.org) | 5.6 | Type-safe development |
| [bpmn-js](https://bpmn.io/toolkit/bpmn-js/) | 17 | BPMN 2.0 rendering |
| [jsPDF](https://github.com/parallax/jsPDF) | 2.5 | PDF generation |
| [svg2pdf.js](https://github.com/yWorks/svg2pdf.js) | 2.5 | SVG → PDF conversion |

---

## PDF Export Flow

1. `bpmn-js` exports the active diagram as SVG.
2. `svg2pdf.js` converts the SVG to a PDF page via `jsPDF`.
3. The browser triggers a file download.
4. The frontend encodes the PDF as Base64 and `POST`s it to `POST /api/processes/{id}/artifacts/pdf` so the backend can store it as a trace artifact.

---

## Comparison Mode

The screen keeps two BPMN canvases simultaneously:

- **Left** — original generated process.
- **Right** — optimized process (shown after clicking **Optimize**).

If the optimized version does not yet exist, the right panel mirrors the original diagram and shows an explanatory banner.

---

## Built With

- [React](https://react.dev) — UI framework used
- [Vite](https://vitejs.dev) — Module bundler
- [bpmn-js](https://bpmn.io/toolkit/bpmn-js/) — BPMN diagram renderer
- [jsPDF](https://github.com/parallax/jsPDF) + [svg2pdf.js](https://github.com/yWorks/svg2pdf.js) — PDF export

---

## Contributing

Please read the project-level [`CONTRIBUTING`](../README.md#contributing) guidelines before opening a pull request.

---

## Versioning

We use [SemVer](https://semver.org/) for versioning. For available versions, see the [tags on this repository](https://github.com/daviidc29/smartbpm-demo/tags).

---

## Authors

- **Camilo Andres Fernandez**
- **David Santiago Castro**
- **Juan Jose Mejia**

---

## License

This project is licensed under the MIT License — see the [LICENSE](../LICENSE) file for details.

---

## Acknowledgments

- [bpmn.io](https://bpmn.io/) for the open-source BPMN toolkit that makes browser-based diagram rendering possible.
- The BPMN 2.0 specification for standardized notation.
