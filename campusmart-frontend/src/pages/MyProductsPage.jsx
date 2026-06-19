import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useSeller } from '../hooks/useSeller';
import ProductTable from '../components/ProductTable.jsx';

function MyProductsPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { products, loadingProducts, errorProducts, fetchSellerProducts, deleteProduct } = useSeller();

  useEffect(() => {
    if (user?.id) {
      fetchSellerProducts(user.id);
    }
  }, [user, fetchSellerProducts]);

  return (
    <section>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1 className="h2 mb-1">My Products</h1>
          <p className="text-muted mb-0">Review and manage active listings for your store.</p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/seller/products/add')}>
          Add New Product
        </button>
      </div>

      {errorProducts && (
        <div className="alert alert-danger" role="alert">
          {errorProducts}
        </div>
      )}

      {loadingProducts ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading products...</span>
          </div>
        </div>
      ) : products.length === 0 ? (
        <div className="alert alert-secondary" role="alert">
          No products found. Create a new listing to get started.
        </div>
      ) : (
        <ProductTable
          products={products}
          onEdit={(productId) => navigate(`/seller/products/${productId}/edit`)}
          onDelete={(productId) => deleteProduct(productId, user.id)}
        />
      )}
    </section>
  );
}

export default MyProductsPage;
