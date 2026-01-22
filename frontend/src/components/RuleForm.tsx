import { useState, useEffect } from 'react';
import type { FraudRule, RuleOperator, RuleAction, OperatorOption, ActionOption } from '../types';

interface Props {
  rule?: FraudRule | null;
  operators: OperatorOption[];
  actions: ActionOption[];
  onSubmit: (rule: FraudRule) => void;
  onCancel: () => void;
}

const defaultRule: FraudRule = {
  name: '',
  description: '',
  enabled: true,
  priority: 100,
  fieldPath: '',
  operator: 'EQUALS',
  value: '',
  actionType: 'FLAG',
  actionConfig: '',
};

export function RuleForm({ rule, operators, actions, onSubmit, onCancel }: Props) {
  const [formData, setFormData] = useState<FraudRule>(defaultRule);
  const [actionConfig, setActionConfig] = useState({ reason: '', topic: '', severity: 'MEDIUM' });

  useEffect(() => {
    if (rule) {
      setFormData(rule);
      if (rule.actionConfig) {
        try {
          setActionConfig(JSON.parse(rule.actionConfig));
        } catch {
          setActionConfig({ reason: '', topic: '', severity: 'MEDIUM' });
        }
      }
    } else {
      setFormData(defaultRule);
      setActionConfig({ reason: '', topic: '', severity: 'MEDIUM' });
    }
  }, [rule]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    let configJson = '';
    if (formData.actionType === 'FLAG') {
      configJson = JSON.stringify({ reason: actionConfig.reason, severity: actionConfig.severity });
    } else if (formData.actionType === 'BLOCK') {
      configJson = JSON.stringify({ reason: actionConfig.reason });
    } else if (formData.actionType === 'ROUTE') {
      configJson = JSON.stringify({ topic: actionConfig.topic });
    }

    onSubmit({ ...formData, actionConfig: configJson });
  };

  const handleChange = (field: keyof FraudRule, value: string | number | boolean) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  return (
    <form onSubmit={handleSubmit} className="rule-form">
      <h2>{rule ? 'Edit Rule' : 'Create Rule'}</h2>

      <div className="form-row">
        <div className="form-group">
          <label htmlFor="name">Rule Name *</label>
          <input
            id="name"
            type="text"
            value={formData.name}
            onChange={(e) => handleChange('name', e.target.value)}
            placeholder="e.g., High Amount Flag"
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="priority">Priority</label>
          <input
            id="priority"
            type="number"
            value={formData.priority}
            onChange={(e) => handleChange('priority', parseInt(e.target.value))}
            min="1"
          />
          <small>Lower number = higher priority</small>
        </div>
      </div>

      <div className="form-group">
        <label htmlFor="description">Description</label>
        <input
          id="description"
          type="text"
          value={formData.description || ''}
          onChange={(e) => handleChange('description', e.target.value)}
          placeholder="What does this rule check for?"
        />
      </div>

      <fieldset>
        <legend>Condition</legend>
        <div className="form-row condition-row">
          <div className="form-group">
            <label htmlFor="fieldPath">Field Path *</label>
            <input
              id="fieldPath"
              type="text"
              value={formData.fieldPath}
              onChange={(e) => handleChange('fieldPath', e.target.value)}
              placeholder="e.g., amount, applicant.name"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="operator">Operator *</label>
            <select
              id="operator"
              value={formData.operator}
              onChange={(e) => handleChange('operator', e.target.value as RuleOperator)}
            >
              {operators.map((op) => (
                <option key={op.value} value={op.value}>
                  {op.label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="value">Value *</label>
            <input
              id="value"
              type="text"
              value={formData.value}
              onChange={(e) => handleChange('value', e.target.value)}
              placeholder="e.g., 10000"
              required={!['IS_NULL', 'IS_NOT_NULL'].includes(formData.operator)}
              disabled={['IS_NULL', 'IS_NOT_NULL'].includes(formData.operator)}
            />
          </div>
        </div>
      </fieldset>

      <fieldset>
        <legend>Action</legend>
        <div className="form-group">
          <label htmlFor="actionType">Action Type *</label>
          <select
            id="actionType"
            value={formData.actionType}
            onChange={(e) => handleChange('actionType', e.target.value as RuleAction)}
          >
            {actions.map((action) => (
              <option key={action.value} value={action.value}>
                {action.label}
              </option>
            ))}
          </select>
        </div>

        {(formData.actionType === 'FLAG' || formData.actionType === 'BLOCK') && (
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="reason">Reason</label>
              <input
                id="reason"
                type="text"
                value={actionConfig.reason || ''}
                onChange={(e) => setActionConfig((prev) => ({ ...prev, reason: e.target.value }))}
                placeholder="Why is this flagged/blocked?"
              />
            </div>
            {formData.actionType === 'FLAG' && (
              <div className="form-group">
                <label htmlFor="severity">Severity</label>
                <select
                  id="severity"
                  value={actionConfig.severity || 'MEDIUM'}
                  onChange={(e) => setActionConfig((prev) => ({ ...prev, severity: e.target.value }))}
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                </select>
              </div>
            )}
          </div>
        )}

        {formData.actionType === 'ROUTE' && (
          <div className="form-group">
            <label htmlFor="topic">Kafka Topic</label>
            <input
              id="topic"
              type="text"
              value={actionConfig.topic || ''}
              onChange={(e) => setActionConfig((prev) => ({ ...prev, topic: e.target.value }))}
              placeholder="e.g., manual-review"
            />
          </div>
        )}
      </fieldset>

      <div className="form-actions">
        <button type="button" onClick={onCancel} className="btn-cancel">
          Cancel
        </button>
        <button type="submit" className="btn-submit">
          {rule ? 'Update Rule' : 'Create Rule'}
        </button>
      </div>
    </form>
  );
}
