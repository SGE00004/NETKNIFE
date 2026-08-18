import type { IncidentSymptom } from './types';
import { phishingActedTree } from './trees/phishingActedTree';
import { unknownDeviceTree } from './trees/unknownDeviceTree';

export const symptoms: IncidentSymptom[] = [
  {
    id: 'slow-computer',
    title: 'Mi ordenador va lento y no se por que',
    description: 'Rendimiento anormal, ventiladores a tope, procesos raros.',
    tree: null,
  },
  {
    id: 'phishing-acted',
    title: 'Recibi un email o mensaje raro y actue sobre el',
    description: 'Hiciste clic, introdujiste datos o abriste un archivo adjunto sospechoso.',
    tree: phishingActedTree,
  },
  {
    id: 'unknown-device',
    title: 'Veo un dispositivo desconocido en mi red',
    description: 'Alguien o algo que no reconoces aparece conectado a tu WiFi.',
    tree: unknownDeviceTree,
  },
  {
    id: 'stolen-password',
    title: 'Creo que me han robado una contrasena',
    description: 'Una filtracion, un aviso de tu gestor de contrasenas, o simple sospecha.',
    tree: null,
  },
  {
    id: 'weird-router',
    title: 'Mi router se comporta de forma extrana',
    description: 'Luces raras, se reinicia solo, conexion mas lenta de lo normal.',
    tree: null,
  },
];
