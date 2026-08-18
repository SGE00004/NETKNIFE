export interface PhishingTemplate {
  id: string;
  name: string;
  subject: string;
  senderLabel: string;
  bodyHtml: string;
  signals: string[];
  lesson: string;
}

export interface SendSimulationRequest {
  recipients: string[];
  templateId: string;
  consentConfirmed: boolean;
}

export interface SimulationResult {
  id: number;
  templateId: string;
  templateName: string;
  recipientEmail: string;
  sentAt: string;
  clicked: boolean;
  clickedAt: string | null;
}

export const MAX_RECIPIENTS = 5;
