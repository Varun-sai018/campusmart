import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { categoryAPI, productAPI } from '../services/api';
import { useSeller } from '../hooks/useSeller';
import SellerProductForm from '../components/SellerProductForm.jsx';

function EditProductPage() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const { updateProduct, loadingSubmit } = useSeller();
  const [product, setProduct] = useState(null);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      setError(null);
      try {
        const [productData, categoryData] = await Promise.all([
          productAPI.getProductById(productId),
          categoryAPI.getAllCategories(),
        ]);

        setProduct(productData);
        setCategories(categoryData);
      } catch (err) {
        setError('Unable to load product details.');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [productId]);

  const handleSubmit = async (productData) => {
    const updated = await updateProduct(productId, productData);
    if (updated) {
      navigate('/seller/products');
    }
  };

  if (loading) {
    return (
      <section>
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading product...</span>
          </div>
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section>
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      </section>
    );
  }

  return (
    <section>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1 className="h2 mb-1">Edit Product</h1>
          <p className="text-muted mb-0">Update listing details, pricing, and status.</p>
        </div>
      </div>

      {product ? (
        <SellerProductForm
          product={product}
          categories={categories}
          onSubmit={handleSubmit}
          submitLabel="Save Changes"
          showStatus={true}
          loading={loadingSubmit}
        />
      ) : (
        <div className="alert alert-secondary">No product details available.</div>
      )}
    </section>
  );
}

export default EditProductPage;
