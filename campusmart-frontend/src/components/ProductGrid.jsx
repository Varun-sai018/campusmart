import ProductCard from './ProductCard.jsx';
import EmptyState from './EmptyState.jsx';

export default function ProductGrid({ products, heading, description, emptyMessage }) {
  if (!products || products.length === 0) {
    return <EmptyState title="No products found" description={emptyMessage || 'Try refining your search or return later for new items.'} />;
  }

  return (
    <section className="product-grid-section">
      {heading && (
        <div className="section-heading">
          <h2>{heading}</h2>
          {description && <p>{description}</p>}
        </div>
      )}
      <div className="product-grid">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </section>
  );
}
