import type { FraudRule } from '../types';

interface Props {
  rules: FraudRule[];
  onToggle: (id: number) => void;
  onEdit: (rule: FraudRule) => void;
  onDelete: (id: number) => void;
}

export function RuleList({ rules, onToggle, onEdit, onDelete }: Props) {
  const getActionBadgeClass = (action: string) => {
    switch (action) {
      case 'FLAG': return 'badge-warning';
      case 'BLOCK': return 'badge-error';
      case 'ROUTE': return 'badge-info';
      case 'ENRICH': return 'badge-success';
      default: return '';
    }
  };

  const formatOperator = (op: string) => {
    const labels: Record<string, string> = {
      'EQUALS': '=',
      'NOT_EQUALS': '≠',
      'CONTAINS': 'contains',
      'NOT_CONTAINS': '!contains',
      'GREATER_THAN': '>',
      'LESS_THAN': '<',
      'GREATER_THAN_OR_EQUALS': '≥',
      'LESS_THAN_OR_EQUALS': '≤',
      'REGEX': 'regex',
      'IN_LIST': 'in',
      'NOT_IN_LIST': 'not in',
      'IS_NULL': 'is null',
      'IS_NOT_NULL': 'is not null',
    };
    return labels[op] || op;
  };

  return (
    <div className="rule-list">
      <h2>Fraud Detection Rules ({rules.length})</h2>

      {rules.length === 0 ? (
        <p className="no-rules">No rules configured. Create one to get started!</p>
      ) : (
        <ul>
          {rules.map((rule) => (
            <li key={rule.id} className={`rule-item ${!rule.enabled ? 'disabled' : ''}`}>
              <div className="rule-header">
                <div className="rule-title">
                  <span className="rule-priority">#{rule.priority}</span>
                  <strong>{rule.name}</strong>
                  <span className={`badge ${getActionBadgeClass(rule.actionType)}`}>
                    {rule.actionType}
                  </span>
                </div>
                <label className="toggle">
                  <input
                    type="checkbox"
                    checked={rule.enabled}
                    onChange={() => rule.id && onToggle(rule.id)}
                  />
                  <span className="toggle-slider"></span>
                </label>
              </div>

              <div className="rule-condition">
                <code>
                  {rule.fieldPath} {formatOperator(rule.operator)} {rule.value}
                </code>
              </div>

              {rule.description && (
                <p className="rule-description">{rule.description}</p>
              )}

              <div className="rule-actions">
                <button onClick={() => onEdit(rule)} className="btn-edit">
                  Edit
                </button>
                <button onClick={() => rule.id && onDelete(rule.id)} className="btn-delete">
                  Delete
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
