function NotificationItem({ notification, onRead, onDelete }) {
  return (
    <div className={`card shadow-sm mb-3 ${notification.read ? 'bg-light' : 'bg-white'}`}>
      <div className="card-body d-flex flex-column flex-md-row justify-content-between gap-3 align-items-start">
        <div>
          <div className="d-flex align-items-center gap-2 mb-2">
            <span className={`badge ${notification.read ? 'bg-secondary' : 'bg-primary'}`}>
              {notification.type}
            </span>
            <span className="small text-muted">{new Date(notification.createdAt).toLocaleString()}</span>
          </div>
          <p className="mb-0">{notification.message}</p>
        </div>
        <div className="d-flex gap-2">
          {!notification.read && (
            <button className="btn btn-outline-success btn-sm" onClick={() => onRead(notification.id)}>
              Mark read
            </button>
          )}
          <button className="btn btn-outline-danger btn-sm" onClick={() => onDelete(notification.id)}>
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}

export default NotificationItem;
