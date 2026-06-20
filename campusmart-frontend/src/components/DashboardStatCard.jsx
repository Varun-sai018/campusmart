export default function DashboardStatCard({ label, value, description, icon: Icon }) {
  return (
    <div className="dashboard-stat-card">
      <div className="dashboard-stat-card-icon">
        {Icon && <Icon size={20} />}
      </div>
      <div>
        <p className="dashboard-stat-label">{label}</p>
        <h3>{value}</h3>
        {description && <p className="dashboard-stat-description">{description}</p>}
      </div>
    </div>
  );
}
