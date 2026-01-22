export interface Notification {
  id: string;
  userId: string;
  type: string;
  message: string;
  timestamp: string;
}

export interface NotificationRequest {
  userId: string;
  type: string;
  message: string;
}

export type RuleOperator =
  | 'EQUALS'
  | 'NOT_EQUALS'
  | 'CONTAINS'
  | 'NOT_CONTAINS'
  | 'GREATER_THAN'
  | 'LESS_THAN'
  | 'GREATER_THAN_OR_EQUALS'
  | 'LESS_THAN_OR_EQUALS'
  | 'REGEX'
  | 'IN_LIST'
  | 'NOT_IN_LIST'
  | 'IS_NULL'
  | 'IS_NOT_NULL';

export type RuleAction = 'FLAG' | 'BLOCK' | 'ROUTE' | 'ENRICH';

export interface FraudRule {
  id?: number;
  name: string;
  description?: string;
  enabled: boolean;
  priority: number;
  fieldPath: string;
  operator: RuleOperator;
  value: string;
  actionType: RuleAction;
  actionConfig?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuditLog {
  id: number;
  applicationId: string;
  applicationType: string;
  sourceSystem: string;
  rulesEvaluated: number;
  rulesMatched: number;
  matchedRuleIds: string;
  matchedRuleNames: string;
  finalAction: RuleAction | null;
  actionDetails: string;
  processedAt: string;
}

export interface Stats {
  total: number;
  last24Hours: number;
  flagged: number;
  clean: number;
  blocked: number;
  flagRate: number;
}

export interface OperatorOption {
  value: RuleOperator;
  label: string;
}

export interface ActionOption {
  value: RuleAction;
  label: string;
}
