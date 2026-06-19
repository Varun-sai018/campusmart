function OrderCard({ order, expanded, onToggle }) {
  const formatCurrency = (value) => {
    if (value == null) return '-';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value));
  };

  return (
    <div className="card shadow-sm mb-3">
      <div className="card-body">
        <div className="d-flex flex-column flex-md-row justify-content-between gap-3 align-items-start">
          <div>
            <h3 className="h5 mb-1">Order #{order.id}</h3>
            <p className="mb-1 text-muted">Placed on {new Date(order.createdAt).toLocaleDateString()}</p>
            <span className={`badge ${order.orderStatus === 'COMPLETED' ? 'bg-success' : order.orderStatus === 'PROCESSING' ? 'bg-warning text-dark' : 'bg-secondary'}`}>
              {order.orderStatus}
            </span>
          </div>
          <div className="text-end">
            <p className="mb-1 text-muted">Items: {order.orderItems.length}</p>
            <p className="h5 mb-0">{formatCurrency(order.totalAmount)}</p>
          </div>
        </div>

        <button className="btn btn-outline-primary btn-sm mt-3" onClick={() => onToggle(order.id)}>
          {expanded ? 'Hide details' : 'View details'}
        </button>

        {expanded && (
          <div className="mt-4">
            <div className="list-group list-group-flush">
              {order.orderItems.map((item) => (
                <div key={item.id} className="list-group-item px-0 border-0 bg-transparent">
                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <h4 className="h6 mb-1">{item.productTitle}</h4>
                      <p className="mb-1 text-muted">Quantity: {item.quantity}</p>
                    </div>
                    <div className="text-end">
                      <p className="mb-1">{formatCurrency(item.priceAtPurchase)}</p>
                      <p className="small text-muted">Seller #{item.sellerId}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default OrderCard;
