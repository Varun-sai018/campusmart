import { useEffect, useState } from 'react';

function CartItemCard({ item, onUpdateQuantity, onRemove }) {
  const [quantity, setQuantity] = useState(item.quantity);

  useEffect(() => {
    setQuantity(item.quantity);
  }, [item.quantity]);

  const handleChange = (event) => {
    const value = Number(event.target.value.replace(/[^0-9]/g, '')); 
    setQuantity(value > 0 ? value : 1);
  };

  const handleUpdate = () => {
    onUpdateQuantity(item.productId, quantity);
  };

  const formatCurrency = (value) => {
    if (value == null) return '-';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value));
  };

  return (
    <div className="card shadow-sm mb-3">
      <div className="card-body">
        <div className="row align-items-center gy-3">
          <div className="col-md-6">
            <h3 className="h6 mb-1">{item.productTitle}</h3>
            <p className="mb-2 text-muted">Price: {formatCurrency(item.productPrice)}</p>
            <p className="mb-0 small text-muted">Updated: {new Date(item.updatedAt).toLocaleDateString()}</p>
          </div>

          <div className="col-md-3 d-flex flex-column gap-2">
            <label className="form-label mb-1">Quantity</label>
            <div className="input-group">
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => setQuantity((prev) => Math.max(prev - 1, 1))}
              >
                -
              </button>
              <input
                type="number"
                className="form-control"
                value={quantity}
                min="1"
                onChange={handleChange}
              />
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => setQuantity((prev) => prev + 1)}
              >
                +
              </button>
            </div>
            <button className="btn btn-sm btn-outline-primary mt-2" onClick={handleUpdate}>
              Update quantity
            </button>
          </div>

          <div className="col-md-3 d-flex flex-column gap-2 text-end">
            <p className="mb-1">Total</p>
            <p className="h5 mb-2">{formatCurrency(item.totalPrice)}</p>
            <button className="btn btn-outline-danger btn-sm" onClick={() => onRemove(item.productId)}>
              Remove
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default CartItemCard;
