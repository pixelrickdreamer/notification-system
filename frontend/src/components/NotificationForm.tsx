import { useState } from 'react';
import type { NotificationRequest } from '../types';

interface Props {
  onSubmit: (request: NotificationRequest) => void;
}

export function NotificationForm({ onSubmit }: Props) {
  const [userId, setUserId] = useState('');
  const [type, setType] = useState('info');
  const [message, setMessage] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!userId.trim() || !message.trim()) return;

    onSubmit({ userId, type, message });
    setMessage('');
  };

  return (
    <form onSubmit={handleSubmit} className="notification-form">
      <h2>Send Notification</h2>

      <div className="form-group">
        <label htmlFor="userId">User ID</label>
        <input
          id="userId"
          type="text"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          placeholder="Enter user ID"
          required
        />
      </div>

      <div className="form-group">
        <label htmlFor="type">Type</label>
        <select
          id="type"
          value={type}
          onChange={(e) => setType(e.target.value)}
        >
          <option value="info">Info</option>
          <option value="warning">Warning</option>
          <option value="error">Error</option>
          <option value="success">Success</option>
        </select>
      </div>

      <div className="form-group">
        <label htmlFor="message">Message</label>
        <textarea
          id="message"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="Enter notification message"
          rows={3}
          required
        />
      </div>

      <button type="submit">Send Notification</button>
    </form>
  );
}
