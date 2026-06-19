import RatingStars from './RatingStars.jsx';

const formatCurrency = (value) => {
  if (value == null) return '-';
  const amount = Number(value);
  return Number.isNaN(amount)
    ? String(value)
    : new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
      }).format(amount);
};

function ProductCard({ product }) {
  return (
    <div className="card h-100 shadow-sm border-0">
      <div className="card-body d-flex flex-column">
        <div className="mb-3">
          <div className="d-flex justify-content-between align-items-start gap-2 mb-2">
            <span className="badge bg-primary">{product.categoryName || 'Category'}</span>
            <span className="badge bg-outline-secondary text-capitalize text-muted">{product.condition?.toLowerCase()}</span>
          </div>
          <h3 className="h5 card-title mb-2 text-truncate">{product.title}</h3>
          <p className="text-muted small mb-0 text-truncate">{product.description || 'No description available.'}</p>
        </div>

        <div className="mt-auto">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <strong>{formatCurrency(product.price)}</strong>
            <span className={`badge ${product.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'}`}>{product.status}</span>
          </div>
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <div className="small text-muted">Seller</div>
              <div className="small">{product.sellerName}</div>
            </div>
            <RatingStars rating={Math.round(product.rating ?? 0)} />
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductCard;
