import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useSeller } from '../hooks/useSeller';
import SellerStatsCard from '../components/SellerStatsCard.jsx';

function SellerDashboardPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { products, loadingProducts, fetchSellerProducts } = useSeller();

  useEffect(() => {
    if (user?.id) {
      fetchSellerProducts(user.id);
    }
  }, [user, fetchSellerProducts]);

  const activeProducts = products.filter((product) => product.status === 'AVAILABLE').length;

  return (
    <section>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1 className="h2 mb-1">Seller Dashboard</h1>
          <p className="text-muted mb-0">Manage your listings, track performance, and create new products.</p>
        </div>
        <div className="d-flex gap-2">
          <button className="btn btn-outline-primary" onClick={() => navigate('/seller/products')}>
            My Products
          </button>
          <button className="btn btn-primary" onClick={() => navigate('/seller/products/add')}>
            Add Product
          </button>
        </div>
      </div>

      <div className="row gy-3 mb-4">
        <div className="col-12 col-md-4">
          <SellerStatsCard label="Total Listings" value={products.length} variant="primary" />
        </div>
        <div className="col-12 col-md-4">
          <SellerStatsCard label="Active Listings" value={activeProducts} variant="success" />
        </div>
        <div className="col-12 col-md-4">
          <SellerStatsCard label="Seller" value={`${user?.firstName ?? ''} ${user?.lastName ?? ''}`} variant="secondary" />
        </div>
      </div>

      <div className="card shadow-sm">
        <div className="card-body">
          <h2 className="h5 mb-3">Quick Actions</h2>
          <div className="row g-3">
            <div className="col-12 col-lg-6">
              <div className="border rounded-3 p-4 h-100">
                <h3 className="h6">Manage Products</h3>
                <p className="text-muted mb-3">View and edit all of your current marketplace listings.</p>
                <button className="btn btn-outline-primary" onClick={() => navigate('/seller/products')}>
                  Go to product manager
                </button>
              </div>
            </div>
            <div className="col-12 col-lg-6">
              <div className="border rounded-3 p-4 h-100">
                <h3 className="h6">Create a new listing</h3>
                <p className="text-muted mb-3">Add a product with pricing, category, and condition details.</p>
                <button className="btn btn-primary" onClick={() => navigate('/seller/products/add')}>
                  Add a product
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {loadingProducts && (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading seller data...</span>
          </div>
        </div>
      )}
    </section>
  );
}

export default SellerDashboardPage;
