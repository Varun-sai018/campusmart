import { useEffect, useState } from 'react';
import { useBuyer } from '../hooks/useBuyer';
import OrderCard from '../components/OrderCard.jsx';

function OrdersPage() {
  const { orders, loadingOrders, errorOrders, fetchOrders } = useBuyer();
  const [expandedOrderId, setExpandedOrderId] = useState(null);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const handleToggle = (orderId) => {
    setExpandedOrderId((current) => (current === orderId ? null : orderId));
  };

  return (
    <section>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1 className="h2 mb-1">Order History</h1>
          <p className="text-muted mb-0">Review past purchases and order details.</p>
        </div>
      </div>

      {loadingOrders ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading orders...</span>
          </div>
        </div>
      ) : errorOrders ? (
        <div className="alert alert-danger">{errorOrders}</div>
      ) : orders.length === 0 ? (
        <div className="alert alert-secondary">You have no orders yet. Place an order from your cart to see it here.</div>
      ) : (
        orders.map((order) => (
          <OrderCard
            key={order.id}
            order={order}
            expanded={expandedOrderId === order.id}
            onToggle={handleToggle}
          />
        ))
      )}
    </section>
  );
}

export default OrdersPage;
