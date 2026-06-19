import { Link } from 'react-router-dom';

function WishlistItemCard({ item, onRemove }) {
  return (
    <div className="card shadow-sm mb-3">
      <div className="card-body d-flex flex-column flex-md-row justify-content-between gap-3 align-items-start">
        <div className="flex-grow-1">
          <h3 className="h5 mb-2">
            <Link to={`/products/${item.productId}`} className="text-decoration-none text-dark">
              {item.productTitle}
            </Link>
          </h3>
          <p className="mb-1 text-muted">Added to wishlist on {new Date(item.createdAt).toLocaleDateString()}</p>
          <span className={`badge ${item.productActive ? 'bg-success' : 'bg-secondary'}`}>
            {item.productActive ? 'Active' : 'Inactive'}
          </span>
        </div>

        <div className="d-flex align-items-center gap-2">
          <button className="btn btn-outline-danger" onClick={() => onRemove(item.productId)}>
            Remove
          </button>
          <Link to={`/products/${item.productId}`} className="btn btn-primary">
            View
          </Link>
        </div>
      </div>
    </div>
  );
}

export default WishlistItemCard;
