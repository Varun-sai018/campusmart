import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { productAPI, productImageAPI, reviewAPI } from '../services/api';
import RatingStars from '../components/RatingStars.jsx';
import ReviewList from '../components/ReviewList.jsx';

function ProductDetailsPage() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [images, setImages] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [ratingSummary, setRatingSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadProductDetails = async () => {
      setLoading(true);
      setError(null);

      try {
        const [productData, productImages, productReviews, productRating] = await Promise.all([
          productAPI.getProductById(productId),
          productImageAPI.getProductImages(productId),
          reviewAPI.getProductReviews(productId),
          reviewAPI.getRatingSummary(productId),
        ]);

        setProduct(productData);
        setImages(productImages);
        setReviews(productReviews);
        setRatingSummary(productRating);
      } catch (err) {
        setError('Unable to load product details. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    loadProductDetails();
  }, [productId]);

  const imageUrl = images.length > 0 ? images[0].imageUrl : null;
  const fallbackImage = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='800' height='420' viewBox='0 0 800 420'%3E%3Crect width='800' height='420' fill='%23f0f0f0'/%3E%3Ctext x='50%25' y='45%25' dominant-baseline='middle' text-anchor='middle' fill='%23999' font-family='Arial,Helvetica,sans-serif' font-size='32'%3EImage not available%3C/text%3E%3Ctext x='50%25' y='60%25' dominant-baseline='middle' text-anchor='middle' fill='%23bbb' font-family='Arial,Helvetica,sans-serif' font-size='18'%3ECampusMart%3C/text%3E%3C/svg%3E";

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

  return (
    <section className="page-section page-section-detail">
      <div className="section-header">
        <div>
          <p className="eyebrow">Product details</p>
          <h1>{product?.title || 'Product details'}</h1>
          <p className="section-description">Explore the selected listing, seller information, and customer reviews.</p>
        </div>
        <button className="market-btn market-btn-outline" onClick={() => navigate(-1)}>
          Back to marketplace
        </button>
      </div>

      {loading ? (
        <div className="page-loader" />
      ) : error ? (
        <EmptyState title="Unable to load product" description={error} />
      ) : (
        product && (
          <div className="product-detail-grid">
            <div className="product-detail-main">
              <div className="product-detail-card">
                <div className="product-detail-image">
                  <img
                    src={imageUrl || fallbackImage}
                    alt={product.title}
                    onError={(event) => {
                      if (event.currentTarget.src !== fallbackImage) {
                        event.currentTarget.src = fallbackImage;
                      }
                    }}
                  />
                </div>
                <div className="product-detail-summary">
                  <div className="badge-row mb-3">
                    <span className="product-badge product-badge-soft">{product.categoryName || 'Uncategorized'}</span>
                    <span className="product-badge">{product.condition}</span>
                    <span className={`product-status ${product.status === 'ACTIVE' ? 'status-active' : 'status-inactive'}`}>
                      {product.status}
                    </span>
                  </div>
                  <h2>{product.title}</h2>
                  <p className="product-detail-price">{formatCurrency(product.price)}</p>
                  <p className="product-detail-copy">{product.description || 'No description available.'}</p>

                  <div className="product-detail-meta">
                    <div>
                      <span className="meta-label">Seller</span>
                      <p>{product.sellerName}</p>
                    </div>
                    <div>
                      <span className="meta-label">Listed</span>
                      <p>{new Date(product.createdAt).toLocaleDateString()}</p>
                    </div>
                    <div>
                      <span className="meta-label">Rating</span>
                      <div className="d-flex align-items-center gap-2">
                        <RatingStars rating={ratingSummary?.averageRating ?? 0} />
                        <span className="meta-small">({ratingSummary?.reviewCount ?? 0})</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="product-detail-card mt-4">
                <div className="section-heading mb-3">
                  <h2>Customer reviews</h2>
                </div>
                <ReviewList reviews={reviews} />
              </div>
            </div>

            <aside className="product-detail-sidebar">
              <div className="product-detail-card sidebar-card">
                <h3>Quick facts</h3>
                <div className="sidebar-fact">
                  <span>Condition</span>
                  <strong>{product.condition}</strong>
                </div>
                <div className="sidebar-fact">
                  <span>Status</span>
                  <strong>{product.status}</strong>
                </div>
                <div className="sidebar-fact">
                  <span>Seller</span>
                  <strong>{product.sellerName}</strong>
                </div>
                <div className="sidebar-fact">
                  <span>Category</span>
                  <strong>{product.categoryName || '—'}</strong>
                </div>
                <Link to="/products" className="market-btn market-btn-primary w-100 mt-3">
                  Browse more listings
                </Link>
              </div>
            </aside>
          </div>
        )
      )}
    </section>
  );
}

export default ProductDetailsPage;
