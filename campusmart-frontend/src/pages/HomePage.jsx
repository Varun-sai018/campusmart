import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { categoryAPI, productAPI } from '../services/api';
import HeroBanner from '../components/HeroBanner.jsx';
import CategoryGrid from '../components/CategoryGrid.jsx';
import ProductGrid from '../components/ProductGrid.jsx';
import EmptyState from '../components/EmptyState.jsx';

function HomePage() {
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadMarketplace = async () => {
      setLoading(true);
      setError(null);
      try {
        const [categoryData, productData] = await Promise.all([
          categoryAPI.getAllCategories(),
          productAPI.searchProducts(undefined, selectedCategory || undefined, undefined, 0, 12),
        ]);

        setCategories(categoryData);
        setProducts(productData.content || []);
      } catch (err) {
        setError('Unable to load marketplace content. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    loadMarketplace();
  }, [selectedCategory]);

  const featuredProducts = products.slice(0, 6);
  const trendingProducts = products.slice(6, 12);

  return (
    <section className="home-page">
      <HeroBanner />

      <div className="section-intro">
        <div>
          <p className="eyebrow">Discover campus deals</p>
          <h2>Marketplace design inspired by modern e-commerce startups.</h2>
          <p className="section-description">
            Browse curated categories, trending products, and fast student-friendly listings.
          </p>
        </div>
        <Link to="/products" className="market-btn market-btn-primary">
          Explore all products
        </Link>
      </div>

      <div className="market-section">
        <div className="section-heading">
          <div>
            <h2>Shop by category</h2>
            <p>Explore top categories and filter the marketplace instantly.</p>
          </div>
          <Link to="/products" className="market-btn market-btn-outline">
            View categories
          </Link>
        </div>

        {loading ? (
          <div className="page-loader" />
        ) : error ? (
          <EmptyState title="Unable to load categories" description={error} />
        ) : categories.length > 0 ? (
          <CategoryGrid categories={categories} selectedCategory={selectedCategory} onSelect={setSelectedCategory} />
        ) : (
          <EmptyState title="No categories found" description="Try refreshing the page or checking back soon." />
        )}
      </div>

      <div className="market-section">
        <ProductGrid
          products={featuredProducts}
          heading="Featured products"
          description="Handpicked picks for campus life and student essentials."
          emptyMessage="No featured products available yet."
        />
      </div>

      <div className="market-section">
        <ProductGrid
          products={trendingProducts}
          heading="Trending now"
          description="Products that students are viewing most this week."
          emptyMessage="No trending products available yet."
        />
      </div>
    </section>
  );
}

export default HomePage;

