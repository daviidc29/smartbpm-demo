import { TraceEventView } from '../types'

type Props = {
  events: TraceEventView[]
}

export default function TracePanel({ events }: Props) {
  return (
    <section className="panel">
      <h3>Trazabilidad</h3>
      <div className="trace-list">
        {events.length === 0 ? (
          <p className="muted">No trace events yet.</p>
        ) : (
          events.map((event, index) => (
            <div key={`${event.stage}-${index}`} className="trace-item">
              <div className="trace-head">
                <strong>{event.stage}</strong>
                <span className={`pill ${event.status === 'OK' ? 'pill-ok' : 'pill-warn'}`}>{event.status}</span>
              </div>
              <div>{event.message}</div>
              <small>{new Date(event.createdAt).toLocaleString()}</small>
            </div>
          ))
        )}
      </div>
    </section>
  )
}
