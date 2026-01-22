import { useEffect, useState } from 'react';
import { NotificationForm } from './components/NotificationForm';
import { NotificationList } from './components/NotificationList';
import { RuleList } from './components/RuleList';
import { RuleForm } from './components/RuleForm';
import type { Notification, NotificationRequest, FraudRule, OperatorOption, ActionOption, Stats } from './types';
import './App.css';

const API_URL = 'http://localhost:8081/api';

type Tab = 'notifications' | 'rules' | 'audit';

function App() {
  const [activeTab, setActiveTab] = useState<Tab>('rules');
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Rules state
  const [rules, setRules] = useState<FraudRule[]>([]);
  const [operators, setOperators] = useState<OperatorOption[]>([]);
  const [actions, setActions] = useState<ActionOption[]>([]);
  const [editingRule, setEditingRule] = useState<FraudRule | null>(null);
  const [showRuleForm, setShowRuleForm] = useState(false);

  // Stats state
  const [stats, setStats] = useState<Stats | null>(null);

  // SSE connection for notifications
  useEffect(() => {
    const eventSource = new EventSource(`${API_URL}/notifications/stream`);

    eventSource.onopen = () => {
      setConnected(true);
      setError(null);
    };

    eventSource.addEventListener('notification', (event) => {
      const notification: Notification = JSON.parse(event.data);
      setNotifications((prev) => {
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

  // Load rules and options
  useEffect(() => {
    loadRules();
    loadOperators();
    loadActions();
    loadStats();
  }, []);

  const loadRules = async () => {
    try {
      const response = await fetch(`${API_URL}/rules`);
      if (response.ok) {
        setRules(await response.json());
      }
    } catch (err) {
      console.error('Failed to load rules:', err);
    }
  };

  const loadOperators = async () => {
    try {
      const response = await fetch(`${API_URL}/rules/operators`);
      if (response.ok) {
        setOperators(await response.json());
      }
    } catch (err) {
      console.error('Failed to load operators:', err);
    }
  };

  const loadActions = async () => {
    try {
      const response = await fetch(`${API_URL}/rules/actions`);
      if (response.ok) {
        setActions(await response.json());
      }
    } catch (err) {
      console.error('Failed to load actions:', err);
    }
  };

  const loadStats = async () => {
    try {
      const response = await fetch(`${API_URL}/audit/stats`);
      if (response.ok) {
        setStats(await response.json());
      }
    } catch (err) {
      console.error('Failed to load stats:', err);
    }
  };

  const handleNotificationSubmit = async (request: NotificationRequest) => {
    try {
      const response = await fetch(`${API_URL}/notifications`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
      });
      if (!response.ok) {
        throw new Error('Failed to send notification');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to send notification');
    }
  };

  const handleRuleToggle = async (id: number) => {
    try {
      const response = await fetch(`${API_URL}/rules/${id}/toggle`, { method: 'PATCH' });
      if (response.ok) {
        loadRules();
      }
    } catch (err) {
      console.error('Failed to toggle rule:', err);
    }
  };

  const handleRuleSubmit = async (rule: FraudRule) => {
    try {
      const method = rule.id ? 'PUT' : 'POST';
      const url = rule.id ? `${API_URL}/rules/${rule.id}` : `${API_URL}/rules`;

      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(rule),
      });

      if (response.ok) {
        loadRules();
        setShowRuleForm(false);
        setEditingRule(null);
      }
    } catch (err) {
      console.error('Failed to save rule:', err);
    }
  };

  const handleRuleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this rule?')) return;

    try {
      const response = await fetch(`${API_URL}/rules/${id}`, { method: 'DELETE' });
      if (response.ok) {
        loadRules();
      }
    } catch (err) {
      console.error('Failed to delete rule:', err);
    }
  };

  const handleEditRule = (rule: FraudRule) => {
    setEditingRule(rule);
    setShowRuleForm(true);
  };

  return (
    <div className="app">
      <header>
        <h1>Fraud Detection Gateway</h1>
        <div className={`status ${connected ? 'connected' : 'disconnected'}`}>
          {connected ? 'Connected' : 'Disconnected'}
        </div>
      </header>

      {error && <div className="error-banner">{error}</div>}

      {stats && (
        <div className="stats-bar">
          <div className="stat">
            <span className="stat-value">{stats.total}</span>
            <span className="stat-label">Total</span>
          </div>
          <div className="stat">
            <span className="stat-value">{stats.last24Hours}</span>
            <span className="stat-label">Last 24h</span>
          </div>
          <div className="stat stat-clean">
            <span className="stat-value">{stats.clean}</span>
            <span className="stat-label">Clean</span>
          </div>
          <div className="stat stat-flagged">
            <span className="stat-value">{stats.flagged}</span>
            <span className="stat-label">Flagged</span>
          </div>
          <div className="stat stat-blocked">
            <span className="stat-value">{stats.blocked}</span>
            <span className="stat-label">Blocked</span>
          </div>
          <div className="stat">
            <span className="stat-value">{stats.flagRate.toFixed(1)}%</span>
            <span className="stat-label">Flag Rate</span>
          </div>
        </div>
      )}

      <nav className="tabs">
        <button
          className={activeTab === 'rules' ? 'active' : ''}
          onClick={() => setActiveTab('rules')}
        >
          Rules
        </button>
        <button
          className={activeTab === 'notifications' ? 'active' : ''}
          onClick={() => setActiveTab('notifications')}
        >
          Live Feed
        </button>
      </nav>

      <main>
        {activeTab === 'notifications' && (
          <div className="notifications-view">
            <div className="panel">
              <NotificationForm onSubmit={handleNotificationSubmit} />
            </div>
            <div className="panel">
              <NotificationList notifications={notifications} />
            </div>
          </div>
        )}

        {activeTab === 'rules' && (
          <div className="rules-view">
            {showRuleForm ? (
              <div className="panel">
                <RuleForm
                  rule={editingRule}
                  operators={operators}
                  actions={actions}
                  onSubmit={handleRuleSubmit}
                  onCancel={() => {
                    setShowRuleForm(false);
                    setEditingRule(null);
                  }}
                />
              </div>
            ) : (
              <>
                <div className="panel-header">
                  <button className="btn-create" onClick={() => setShowRuleForm(true)}>
                    + Create Rule
                  </button>
                </div>
                <div className="panel">
                  <RuleList
                    rules={rules}
                    onToggle={handleRuleToggle}
                    onEdit={handleEditRule}
                    onDelete={handleRuleDelete}
                  />
                </div>
              </>
            )}
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
