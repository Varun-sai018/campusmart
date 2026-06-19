import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { categoryAPI, productAPI } from '../services/api';
import ProductCard from '../components/ProductCard';

const PAGE_SIZE = 12;

function ProductListPage({ categoryId: initialCategoryId }) {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState(initialCategoryId || null);
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    setSelectedCategory(initialCategoryId || null);
    setPage(0);
  }, [initialCategoryId]);

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

  useEffect(() => {
    const loadProducts = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await productAPI.searchProducts(
          searchTerm || undefined,
          selectedCategory || undefined,
          undefined,
          page,
          PAGE_SIZE,
        );

        setProducts(response.content || []);
        setTotalPages(response.totalPages ?? 0);
      } catch (err) {
        setError('Unable to load products. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    loadProducts();
  }, [searchTerm, selectedCategory, page]);

  const handleSearchSubmit = (event) => {
    event.preventDefault();
    setPage(0);
  };

  const handleCategorySelect = (category) => {
    setSelectedCategory(category ? category.id : null);
    setPage(0);
  };

  return (
    <section>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1 className="h2 mb-1">CampusMart Marketplace</h1>
          <p className="text-muted mb-0">Browse student listings, filter by category, and explore featured products.</p>
        </div>

        <form className="d-flex w-100 w-md-auto" onSubmit={handleSearchSubmit}>
          <input
            type="search"
            className="form-control me-2"
            placeholder="Search products..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">Search</button>
        </form>
      </div>

      <div className="mb-4">
        <div className="d-flex flex-wrap gap-2 align-items-center">
          <button
            type="button"
            className={`btn btn-sm ${selectedCategory === null ? 'btn-primary' : 'btn-outline-primary'}`}
            onClick={() => handleCategorySelect(null)}
          >
            All Categories
          </button>
          {categories.map((category) => (
            <button
              key={category.id}
              type="button"
              className={`btn btn-sm ${selectedCategory === category.id ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={() => handleCategorySelect(category)}
            >
              {category.name}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading products...</span>
          </div>
        </div>
      ) : (
        <>
          {products.length === 0 ? (
            <div className="alert alert-secondary" role="alert">
              No products found. Try a different search or category.
            </div>
          ) : (
            <div className="row gy-4">
              {products.map((product) => (
                <div key={product.id} className="col-12 col-md-6 col-xl-4">
                  <Link to={`/products/${product.id}`} className="text-decoration-none text-reset">
                    <ProductCard product={product} />
                  </Link>
                </div>
              ))}
            </div>
          )}

          {totalPages > 1 && (
            <nav className="mt-4">
              <ul className="pagination justify-content-center">
                <li className={`page-item ${page === 0 ? 'disabled' : ''}`}>
                  <button className="page-link" onClick={() => setPage((prev) => Math.max(prev - 1, 0))}>
                    Previous
                  </button>
                </li>
                {Array.from({ length: totalPages }).map((_, index) => (
                  <li key={index} className={`page-item ${page === index ? 'active' : ''}`}>
                    <button className="page-link" onClick={() => setPage(index)}>
                      {index + 1}
                    </button>
                  </li>
                ))}
                <li className={`page-item ${page === totalPages - 1 ? 'disabled' : ''}`}>
                  <button className="page-link" onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}>
                    Next
                  </button>
                </li>
              </ul>
            </nav>
          )}
        </>
      )}
    </section>
  );
}

export default ProductListPage;
