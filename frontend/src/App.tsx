import { useEffect, useState } from 'react';
import { NotificationForm } from './components/NotificationForm';
import { NotificationList } from './components/NotificationList';
import type { Notification, NotificationRequest } from './types';
import './App.css';

const API_URL = 'http://localhost:8081/api/notifications';

function App() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const eventSource = new EventSource(`${API_URL}/stream`);

    eventSource.onopen = () => {
      setConnected(true);
      setError(null);
    };

    eventSource.addEventListener('notification', (event) => {
      const notification: Notification = JSON.parse(event.data);
      setNotifications((prev) => {
        // Avoid duplicates
        if (prev.some((n) => n.id === notification.id)) {
          return prev;
        }
        return [...prev, notification];
      });
    });

    eventSource.onerror = () => {
      setConnected(false);
      setError('Connection lost. Make sure the backend is running.');
    };

    return () => {
      eventSource.close();
    };
  }, []);

  const handleSubmit = async (request: NotificationRequest) => {
    try {
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        throw new Error('Failed to send notification');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to send notification');
    }
  };

  return (
    <div className="app">
      <header>
        <h1>Notification System</h1>
        <div className={`status ${connected ? 'connected' : 'disconnected'}`}>
          {connected ? 'Connected' : 'Disconnected'}
        </div>
      </header>

      {error && <div className="error-banner">{error}</div>}

      <main>
        <div className="panel">
          <NotificationForm onSubmit={handleSubmit} />
        </div>
        <div className="panel">
          <NotificationList notifications={notifications} />
        </div>
      </main>
    </div>
  );
}

export default App;
