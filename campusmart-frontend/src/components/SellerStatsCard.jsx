function SellerStatsCard({ label, value, variant = 'primary' }) {
  return (
    <div className={`card border-${variant} shadow-sm text-${variant} bg-white mb-3`}>
      <div className="card-body">
        <p className="small text-uppercase mb-1">{label}</p>
        <h3 className="mb-0">{value}</h3>
      </div>
    </div>
  );
}

export default SellerStatsCard;
