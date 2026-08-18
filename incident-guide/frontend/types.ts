import type { ToolId } from '../../shared/types/toolId';

export interface TreeOption {
  label: string;
  nextNodeId: string;
}

export interface QuestionNode {
  type: 'question';
  id: string;
  prompt: string;
  options: TreeOption[];
}

/**
 * Igual que QuestionNode pero el texto de la pregunta se calcula en tiempo de
 * ejecucion a partir de datos reales de otro modulo (ej. el Escaner de Red),
 * en vez de venir fijo en el arbol. El componente que renderiza los pasos
 * decide como resolver fallbackPrompt en un texto real.
 */
export interface DynamicNetworkCheckNode {
  type: 'dynamic-network-check';
  id: string;
  fallbackPrompt: string;
  options: TreeOption[];
}

export interface ActionStep {
  text: string;
  /** true si es un paso critico que el usuario debe confirmar explicitamente, no solo leer. */
  requiresConfirmation: boolean;
}

export interface RecommendationNode {
  type: 'recommendation';
  id: string;
  title: string;
  tone: 'tranquilizador' | 'urgente';
  summary: string;
  steps: ActionStep[];
  relatedTool?: {
    toolId: ToolId;
    label: string;
  };
}

export type TreeNode = QuestionNode | DynamicNetworkCheckNode | RecommendationNode;

export interface DecisionTree {
  symptomId: string;
  rootNodeId: string;
  nodes: Record<string, TreeNode>;
}

export interface IncidentSymptom {
  id: string;
  title: string;
  description: string;
  tree: DecisionTree | null;
}
