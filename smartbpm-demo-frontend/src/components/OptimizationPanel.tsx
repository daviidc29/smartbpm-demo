import { OptimizationChange } from '../types'

type Props = {
  changes: OptimizationChange[]
}

export default function OptimizationPanel({ changes }: Props) {
  return (
    <section className="panel">
      <h3>Optimización</h3>
      {changes.length === 0 ? (
        <p className="muted">No optimization changes yet.</p>
      ) : (
        <div className="change-list">
          {changes.map((change, index) => (
            <article key={`${change.title}-${index}`} className="change-card">
              <h4>{change.title}</h4>
              <p><strong>Razón:</strong> {change.reason}</p>
              <div className="change-diff">
                <p><strong>Antes:</strong> {change.before}</p>
                <p><strong>Después:</strong> {change.after}</p>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
