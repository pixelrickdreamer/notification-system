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
