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
    <section>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1 className="h2 mb-1">Product Details</h1>
          <p className="text-muted mb-0">Explore the listing, seller details, and customer feedback.</p>
        </div>
        <button className="btn btn-outline-primary" onClick={() => navigate(-1)}>
          Back to marketplace
        </button>
      </div>

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading product...</span>
          </div>
        </div>
      ) : error ? (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      ) : (
        product && (
          <div className="row g-4">
            <div className="col-12 col-lg-7">
              <div className="card shadow-sm">
                <img
                  src={imageUrl || fallbackImage}
                  alt={product.title}
                  className="card-img-top object-fit-cover"
                  style={{ maxHeight: '420px' }}
                  onError={(event) => {
                    if (event.currentTarget.src !== fallbackImage) {
                      event.currentTarget.src = fallbackImage;
                    }
                  }}
                />
                <div className="card-body">
                  <h2 className="h4 fw-bold">{product.title}</h2>
                  <div className="mb-3 d-flex flex-wrap gap-2 align-items-center">
                    <span className="badge bg-primary">{product.categoryName || 'Uncategorized'}</span>
                    <span className="badge bg-secondary">{product.condition}</span>
                    <span className={`badge ${product.status === 'ACTIVE' ? 'bg-success' : 'bg-warning'}`}>
                      {product.status}
                    </span>
                  </div>
                  <p className="fs-4 fw-semibold text-dark mb-3">{formatCurrency(product.price)}</p>
                  <p className="mb-4 text-muted">{product.description || 'No description available.'}</p>

                  <div className="row g-3 mb-4">
                    <div className="col-6">
                      <div className="small text-uppercase text-muted">Seller</div>
                      <p className="mb-0">{product.sellerName}</p>
                    </div>
                    <div className="col-6">
                      <div className="small text-uppercase text-muted">Category</div>
                      <p className="mb-0">{product.categoryName || 'Unknown'}</p>
                    </div>
                  </div>

                  <div className="d-flex flex-wrap gap-3 align-items-center">
                    <div>
                      <div className="small text-uppercase text-muted">Rating</div>
                      <div className="d-flex align-items-center gap-2">
                        <RatingStars rating={ratingSummary?.averageRating ?? 0} />
                        <span className="text-muted">({ratingSummary?.reviewCount ?? 0} reviews)</span>
                      </div>
                    </div>
                    <div>
                      <div className="small text-uppercase text-muted">Listed</div>
                      <p className="mb-0">{new Date(product.createdAt).toLocaleDateString()}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="card shadow-sm mt-4">
                <div className="card-body">
                  <h3 className="h5 mb-3">Customer Reviews</h3>
                  <ReviewList reviews={reviews} />
                </div>
              </div>
            </div>

            <div className="col-12 col-lg-5">
              <div className="card shadow-sm p-4">
                <h3 className="h5 mb-3">Quick Facts</h3>
                <ul className="list-group list-group-flush">
                  <li className="list-group-item px-0 d-flex justify-content-between">
                    <span>Condition</span>
                    <strong>{product.condition}</strong>
                  </li>
                  <li className="list-group-item px-0 d-flex justify-content-between">
                    <span>Status</span>
                    <strong>{product.status}</strong>
                  </li>
                  <li className="list-group-item px-0 d-flex justify-content-between">
                    <span>Seller</span>
                    <strong>{product.sellerName}</strong>
                  </li>
                  <li className="list-group-item px-0 d-flex justify-content-between">
                    <span>Category</span>
                    <strong>{product.categoryName || '—'}</strong>
                  </li>
                </ul>
                <Link to="/" className="btn btn-primary w-100 mt-4">
                  Browse more listings
                </Link>
              </div>
            </div>
          </div>
        )
      )}
    </section>
  );
}

export default ProductDetailsPage;
