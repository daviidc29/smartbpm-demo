import { ValidationReport } from '../types'

type Props = {
  report?: ValidationReport | null
  title: string
}

export default function ValidationPanel({ report, title }: Props) {
  return (
    <section className="panel">
      <h3>{title}</h3>
      {!report ? (
        <p className="muted">No validation available yet.</p>
      ) : (
        <>
          <div className={`pill ${report.valid ? 'pill-ok' : 'pill-warn'}`}>
            {report.valid ? 'Valid' : 'Needs attention'}
          </div>
          <ul className="issue-list">
            {report.issues.length === 0 ? (
              <li>No findings.</li>
            ) : (
              report.issues.map((issue, index) => (
                <li key={`${issue.layer}-${index}`}>
                  <strong>[{issue.severity}] {issue.layer}</strong>: {issue.message}
                </li>
              ))
            )}
          </ul>
        </>
      )}
    </section>
  )
}
