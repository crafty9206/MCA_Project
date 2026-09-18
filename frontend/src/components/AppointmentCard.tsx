export function AppointmentCard() {
  return (
    <section className="panel appointment-panel">
      <div className="panel-heading"><div><p className="section-label">Coming up</p><h2>Next appointment</h2></div><span className="more-button">•••</span></div>
      <div className="appointment-detail"><div className="date-block"><b>17</b><span>SEP</span></div><div><strong>Mid-pregnancy checkup</strong><span>Thursday · 10:30 AM – 11:15 AM</span><span>Dr. Meera Shah · Sunrise Women’s Clinic</span></div></div>
      <button className="primary-button" type="button">Prepare questions <span>→</span></button>
    </section>
  )
}
