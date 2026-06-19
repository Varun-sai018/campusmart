import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { categoryAPI } from '../services/api';
import { useAuth } from '../hooks/useAuth';
import { useSeller } from '../hooks/useSeller';
import SellerProductForm from '../components/SellerProductForm.jsx';

function AddProductPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { createProduct, loadingSubmit } = useSeller();
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadCategories = async () => {
      try {
        const categoryData = await categoryAPI.getAllCategories();
        setCategories(categoryData);
      } catch (err) {
        setError('Unable to load categories.');
      }
    };

    loadCategories();
  }, []);

  const handleSubmit = async (productData) => {
    if (!user?.id) {
      setError('Unable to determine seller account.');
      return;
    }

    const payload = {
      ...productData,
      sellerId: user.id,
    };

    const created = await createProduct(payload);
    if (created) {
      navigate('/seller/products');
    }
  };

  return (
    <section>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1 className="h2 mb-1">Add Product</h1>
          <p className="text-muted mb-0">Create a new listing for your seller storefront.</p>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      <SellerProductForm
        categories={categories}
        onSubmit={handleSubmit}
        submitLabel="Create Product"
        showStatus={false}
        loading={loadingSubmit}
      />
    </section>
  );
}

export default AddProductPage;
