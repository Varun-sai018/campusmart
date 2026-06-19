import { useEffect } from 'react';
import { useBuyer } from '../hooks/useBuyer';
import NotificationItem from '../components/NotificationItem.jsx';

function NotificationsPage() {
  const {
    notifications,
    loadingNotifications,
    errorNotifications,
    fetchNotifications,
    markNotificationRead,
    markAllNotificationsRead,
    deleteNotification,
  } = useBuyer();

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  return (
    <section>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1 className="h2 mb-1">Notifications</h1>
          <p className="text-muted mb-0">Stay informed about order updates and account activity.</p>
        </div>
        <button className="btn btn-outline-primary" onClick={markAllNotificationsRead}>
          Mark all as read
        </button>
      </div>

      {loadingNotifications ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading notifications...</span>
          </div>
        </div>
      ) : errorNotifications ? (
        <div className="alert alert-danger">{errorNotifications}</div>
      ) : notifications.length === 0 ? (
        <div className="alert alert-secondary">No notifications yet. Check back later.</div>
      ) : (
        notifications.map((notification) => (
          <NotificationItem
            key={notification.id}
            notification={notification}
            onRead={markNotificationRead}
            onDelete={deleteNotification}
          />
        ))
      )}
    </section>
  );
}

export default NotificationsPage;
