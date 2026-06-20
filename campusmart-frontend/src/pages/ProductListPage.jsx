import { useEffect, useState } from 'react';
import { categoryAPI, productAPI } from '../services/api';
import ProductGrid from '../components/ProductGrid.jsx';
import EmptyState from '../components/EmptyState.jsx';

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
    <section className="product-list-page">
      <div className="section-intro section-intro-slim">
        <div>
          <p className="eyebrow">Search the marketplace</p>
          <h1>All listings and categories</h1>
          <p className="section-description">Browse products, filter by category, and discover student favourites.</p>
        </div>
      </div>

      <form className="product-search-form" onSubmit={handleSearchSubmit}>
        <input
          type="search"
          className="search-input"
          placeholder="Search products..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <button type="submit" className="market-btn market-btn-primary">
          Search
        </button>
      </form>

      <div className="category-pill-row">
        <button
          type="button"
          className={`category-pill ${selectedCategory === null ? 'category-pill-active' : ''}`}
          onClick={() => handleCategorySelect(null)}
        >
          All Categories
        </button>
        {categories.map((category) => (
          <button
            key={category.id}
            type="button"
            className={`category-pill ${selectedCategory === category.id ? 'category-pill-active' : ''}`}
            onClick={() => handleCategorySelect(category)}
          >
            {category.name}
          </button>
        ))}
      </div>

      {error && <EmptyState title="Unable to load products" description={error} />}

      {loading ? (
        <div className="page-loader" />
      ) : (
        <ProductGrid
          products={products}
          heading="Browse listings"
          description="Find the right product for your campus lifestyle."
          emptyMessage="No products match your filters right now."
        />
      )}

      {totalPages > 1 && (
        <nav className="pagination-nav">
          <ul className="pagination-list">
            <li>
              <button
                type="button"
                className="page-button"
                disabled={page === 0}
                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
              >
                Previous
              </button>
            </li>
            {Array.from({ length: totalPages }).map((_, index) => (
              <li key={index}>
                <button
                  type="button"
                  className={`page-button ${page === index ? 'page-button-active' : ''}`}
                  onClick={() => setPage(index)}
                >
                  {index + 1}
                </button>
              </li>
            ))}
            <li>
              <button
                type="button"
                className="page-button"
                disabled={page === totalPages - 1}
                onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
              >
                Next
              </button>
            </li>
          </ul>
        </nav>
      )}
    </section>
  );
}

export default ProductListPage;
