import type { DecisionTree } from '../types';

export const unknownDeviceTree: DecisionTree = {
  symptomId: 'unknown-device',
  rootNodeId: 'check-scanner',
  nodes: {
    'check-scanner': {
      type: 'dynamic-network-check',
      id: 'check-scanner',
      fallbackPrompt: '¿Es sobre un dispositivo que has visto en el Escaner de Red?',
      options: [
        { label: 'Si, es sobre eso, quiero revisarlo', nextNodeId: 'know-what-it-is' },
        { label: 'No es de ahi, es otra cosa', nextNodeId: 'not-in-scanner' },
      ],
    },
    'know-what-it-is': {
      type: 'question',
      id: 'know-what-it-is',
      prompt: '¿Reconoces a quien podria pertenecer? (el movil de un familiar, un altavoz inteligente que instalaste hace poco...)',
      options: [
        { label: 'Si, creo que se de que es', nextNodeId: 'recognize-in-scanner' },
        { label: 'No, no tengo ni idea de que es', nextNodeId: 'unrecognized-device' },
      ],
    },
    'recognize-in-scanner': {
      type: 'recommendation',
      id: 'recognize-in-scanner',
      title: 'Solo tienes que marcarlo como reconocido',
      tone: 'tranquilizador',
      summary: 'Si sabes de que dispositivo se trata, no hay ningun riesgo: solo falta decirselo a NETKNIFE.',
      steps: [
        {
          text: "Entra en el Escaner de Red, busca ese dispositivo y pulsa 'Lo reconozco', poniendole un nombre que lo identifique.",
          requiresConfirmation: true,
        },
      ],
      relatedTool: { toolId: 'network-scanner', label: 'Ir al Escaner de Red' },
    },
    'unrecognized-device': {
      type: 'recommendation',
      id: 'unrecognized-device',
      title: 'Actua para sacarlo de tu red',
      tone: 'urgente',
      summary:
        'Un dispositivo que no reconoces conectado a tu red puede significar que alguien esta usando tu WiFi sin ' +
        'permiso. Cambiar la contrasena resuelve el problema en la mayoria de los casos.',
      steps: [
        {
          text: 'Cambia la contrasena de tu WiFi desde los ajustes del router. Esto desconecta a todos los dispositivos, incluido el intruso.',
          requiresConfirmation: true,
        },
        { text: 'Vuelve a conectar solo tus dispositivos de confianza con la nueva contrasena.', requiresConfirmation: true },
        { text: 'Comprueba que el cifrado de tu WiFi sea WPA2 o superior (nunca WEP ni sin cifrado).', requiresConfirmation: true },
        { text: 'Vuelve a escanear tu red para confirmar que el dispositivo ya no aparece.', requiresConfirmation: false },
      ],
      relatedTool: { toolId: 'exposure-checker', label: 'Ejecuta el Comprobador de Exposicion para revisar tambien otros puntos debiles' },
    },
    'not-in-scanner': {
      type: 'recommendation',
      id: 'not-in-scanner',
      title: 'Puede que no sea un dispositivo de red',
      tone: 'tranquilizador',
      summary:
        'Si lo que has visto no viene del Escaner de Red de NETKNIFE (por ejemplo, una notificacion de otra app, ' +
        'una sesion iniciada o un emparejamiento por Bluetooth), puede tratarse de otra cosa distinta.',
      steps: [
        { text: 'Escanea tu red para descartar que tambien haya un dispositivo desconocido conectado.', requiresConfirmation: true },
      ],
      relatedTool: { toolId: 'network-scanner', label: 'Ir al Escaner de Red' },
    },
  },
};
