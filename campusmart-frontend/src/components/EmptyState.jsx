import { Package } from 'lucide-react';

export default function EmptyState({ icon: Icon = Package, title, description, actionLabel, onAction }) {
  return (
    <div className="empty-state-card">
      <div className="empty-state-icon">
        <Icon size={40} />
      </div>
      <div className="empty-state-content">
        <h3>{title}</h3>
        <p>{description}</p>
        {actionLabel && onAction && (
          <button type="button" className="market-btn market-btn-primary" onClick={onAction}>
            {actionLabel}
          </button>
        )}
      </div>
    </div>
  );
}
