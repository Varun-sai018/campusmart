import { Link } from 'react-router-dom';
import { Heart } from 'lucide-react';
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
  const fallbackImage = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='300' viewBox='0 0 400 300'%3E%3Crect width='400' height='300' fill='%23f0f0f0'/%3E%3Ctext x='50%25' y='45%25' dominant-baseline='middle' text-anchor='middle' fill='%23999' font-family='Arial,Helvetica,sans-serif' font-size='24'%3EImage not available%3C/text%3E%3Ctext x='50%25' y='60%25' dominant-baseline='middle' text-anchor='middle' fill='%23bbb' font-family='Arial,Helvetica,sans-serif' font-size='16'%3ECampusMart%3C/text%3E%3C/svg%3E";
  const imgSrc = product.primaryImageUrl || fallbackImage;

  return (
    <article className="product-card">
      <Link to={`/products/${product.id}`} className="product-card-link">
        <div className="product-card-media">
          <img
            src={imgSrc}
            alt={product.title}
            onError={(event) => {
              if (event.currentTarget.src !== fallbackImage) {
                event.currentTarget.src = fallbackImage;
              }
            }}
          />
          <div className="product-favorite-badge">
            <Heart size={18} />
          </div>
        </div>

        <div className="product-card-body">
          <div className="badge-row">
            <span className="product-badge product-badge-soft">{product.categoryName || 'Category'}</span>
            <span className="product-badge">{product.condition?.toLowerCase() || 'New'}</span>
          </div>

          <h3>{product.title}</h3>
          <p>{product.description || 'No description available.'}</p>

          <div className="product-card-footer">
            <div>
              <span className="product-price">{formatCurrency(product.price)}</span>
              <RatingStars rating={product.rating ?? 0} />
            </div>
            <span className={`product-status ${product.status === 'ACTIVE' ? 'status-active' : 'status-inactive'}`}>
              {product.status}
            </span>
          </div>
        </div>
      </Link>
    </article>
  );
}

export default ProductCard;
