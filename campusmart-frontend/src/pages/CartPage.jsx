import { useEffect } from 'react';
import { useBuyer } from '../hooks/useBuyer';
import CartItemCard from '../components/CartItemCard.jsx';

function CartPage() {
  const { cartSummary, loadingCart, errorCart, fetchCart, updateCartItemQuantity, removeCartItem, clearCart, placeOrder } = useBuyer();

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const subtotal = cartSummary?.totalAmount ?? 0;

  const formatCurrency = (value) => {
    if (value == null) return '-';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value));
  };

  return (
    <section>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1 className="h2 mb-1">Your Cart</h1>
          <p className="text-muted mb-0">Review your selected items and place your order.</p>
        </div>
      </div>

      {loadingCart ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading cart...</span>
          </div>
        </div>
      ) : errorCart ? (
        <div className="alert alert-danger">{errorCart}</div>
      ) : !cartSummary || cartSummary.cartItems.length === 0 ? (
        <div className="alert alert-secondary">Your cart is empty. Add items to continue.</div>
      ) : (
        <div className="row gy-4">
          <div className="col-12 col-lg-8">
            {cartSummary.cartItems.map((item) => (
              <CartItemCard
                key={item.id}
                item={item}
                onUpdateQuantity={updateCartItemQuantity}
                onRemove={removeCartItem}
              />
            ))}
          </div>

          <div className="col-12 col-lg-4">
            <div className="card shadow-sm">
              <div className="card-body">
                <h3 className="h5 mb-3">Order Summary</h3>
                <div className="d-flex justify-content-between mb-2">
                  <span>Items</span>
                  <strong>{cartSummary.totalItems}</strong>
                </div>
                <div className="d-flex justify-content-between mb-2">
                  <span>Total quantity</span>
                  <strong>{cartSummary.totalQuantity}</strong>
                </div>
                <div className="d-flex justify-content-between mb-4">
                  <span>Subtotal</span>
                  <strong>{formatCurrency(subtotal)}</strong>
                </div>
                <button className="btn btn-primary w-100 mb-2" onClick={placeOrder}>
                  Place Order
                </button>
                <button className="btn btn-outline-danger w-100" onClick={clearCart}>
                  Clear Cart
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

export default CartPage;
