import { useEffect } from 'react';
import { useBuyer } from '../hooks/useBuyer';
import WishlistItemCard from '../components/WishlistItemCard.jsx';

function WishlistPage() {
  const { wishlist, loadingWishlist, errorWishlist, fetchWishlist, removeFromWishlist } = useBuyer();

  useEffect(() => {
    fetchWishlist();
  }, [fetchWishlist]);

  return (
    <section>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1 className="h2 mb-1">Your Wishlist</h1>
          <p className="text-muted mb-0">Save items you want to revisit later.</p>
        </div>
      </div>

      {loadingWishlist ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading wishlist...</span>
          </div>
        </div>
      ) : errorWishlist ? (
        <div className="alert alert-danger">{errorWishlist}</div>
      ) : wishlist.length === 0 ? (
        <div className="alert alert-secondary">Your wishlist is empty. Add some items to save them for later.</div>
      ) : (
        wishlist.map((item) => (
          <WishlistItemCard key={item.id} item={item} onRemove={removeFromWishlist} />
        ))
      )}
    </section>
  );
}

export default WishlistPage;
