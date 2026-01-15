import type { Notification } from '../types';

interface Props {
  notifications: Notification[];
}

export function NotificationList({ notifications }: Props) {
  const formatTime = (timestamp: string) => {
    return new Date(timestamp).toLocaleTimeString();
  };

  const getTypeClass = (type: string) => {
    return `notification-item notification-${type}`;
  };

  return (
    <div className="notification-list">
      <h2>Live Notifications ({notifications.length})</h2>

      {notifications.length === 0 ? (
        <p className="no-notifications">No notifications yet. Send one to see it appear here!</p>
      ) : (
        <ul>
          {[...notifications].reverse().map((notification) => (
            <li key={notification.id} className={getTypeClass(notification.type)}>
              <div className="notification-header">
                <span className="notification-type">{notification.type.toUpperCase()}</span>
                <span className="notification-time">{formatTime(notification.timestamp)}</span>
              </div>
              <div className="notification-body">
                <strong>User: {notification.userId}</strong>
                <p>{notification.message}</p>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
