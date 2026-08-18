import type { DecisionTree } from '../types';

export const phishingActedTree: DecisionTree = {
  symptomId: 'phishing-acted',
  rootNodeId: 'what-did-you-do',
  nodes: {
    'what-did-you-do': {
      type: 'question',
      id: 'what-did-you-do',
      prompt: '¿Que hiciste exactamente con ese email o mensaje?',
      options: [
        { label: 'Hice clic en un enlace', nextNodeId: 'clicked-link' },
        { label: 'Introduje mi contrasena o datos personales en una pagina', nextNodeId: 'entered-credentials' },
        { label: 'Descargue y abri un archivo adjunto', nextNodeId: 'opened-attachment' },
        { label: 'Solo lo abri para leerlo, no hice clic en nada', nextNodeId: 'just-opened' },
      ],
    },
    'just-opened': {
      type: 'recommendation',
      id: 'just-opened',
      title: 'Tranquilo, con solo abrirlo no pasa nada grave',
      tone: 'tranquilizador',
      summary:
        'Leer un email o mensaje, sin hacer clic en nada ni descargar archivos, no suele tener consecuencias. ' +
        'La inmensa mayoria de los ataques necesitan que hagas clic o introduzcas datos para funcionar.',
      steps: [
        { text: 'No hagas clic en ningun enlace ni descargues ningun archivo de ese mensaje.', requiresConfirmation: true },
        { text: 'Marca el mensaje como spam o phishing en tu correo para que no te vuelva a llegar.', requiresConfirmation: true },
      ],
    },
    'clicked-link': {
      type: 'question',
      id: 'clicked-link',
      prompt: '¿La pagina a la que te llevo el enlace te pedia contrasena, datos bancarios o algo similar?',
      options: [
        { label: 'Si, y llegue a introducirlos', nextNodeId: 'entered-credentials' },
        { label: 'No, o cerre la pagina antes de escribir nada', nextNodeId: 'clicked-link-safe' },
      ],
    },
    'clicked-link-safe': {
      type: 'recommendation',
      id: 'clicked-link-safe',
      title: 'Bien hecho al no introducir tus datos',
      tone: 'tranquilizador',
      summary:
        'Hacer clic en un enlace, sin introducir ningun dato despues, casi nunca compromete tu cuenta. ' +
        'Aun asi conviene tomar un par de precauciones.',
      steps: [
        { text: 'No vuelvas a introducir tus datos si ves esa pagina de nuevo.', requiresConfirmation: true },
        { text: 'Marca el mensaje original como spam o phishing en tu correo.', requiresConfirmation: true },
        { text: 'Si notas que se descargo algo automaticamente, pasa un analisis con tu antivirus.', requiresConfirmation: false },
      ],
      relatedTool: { toolId: 'hygiene-checklist', label: 'Revisa que tu antivirus este activo en el Checklist de Higiene' },
    },
    'entered-credentials': {
      type: 'recommendation',
      id: 'entered-credentials',
      title: 'Cambia esa contrasena ahora mismo',
      tone: 'urgente',
      summary:
        'Si has introducido tu contrasena u otros datos en una pagina falsa, alguien mas puede tenerlos ya. ' +
        'Actuar en los proximos minutos reduce mucho el riesgo real.',
      steps: [
        {
          text: 'Cambia la contrasena de esa cuenta ahora mismo, entrando directamente desde la web o app oficial (nunca desde el enlace del mensaje).',
          requiresConfirmation: true,
        },
        {
          text: 'Si usabas esa misma contrasena en otras cuentas, cambiala tambien en todas ellas.',
          requiresConfirmation: true,
        },
        { text: 'Activa la verificacion en dos pasos en esa cuenta si esta disponible.', requiresConfirmation: true },
        { text: 'Vigila esa cuenta los proximos dias por si ves alguna actividad que no reconoces.', requiresConfirmation: false },
      ],
      relatedTool: { toolId: 'hygiene-checklist', label: 'Revisa tu Checklist de Higiene digital' },
    },
    'opened-attachment': {
      type: 'recommendation',
      id: 'opened-attachment',
      title: 'Analiza tu equipo cuanto antes',
      tone: 'urgente',
      summary:
        'Un archivo adjunto puede contener malware que se instale solo con abrirlo. Conviene revisarlo sin demora, ' +
        'pero sin entrar en panico: la mayoria de antivirus lo detectan si actuas pronto.',
      steps: [
        { text: 'Desconecta el equipo de internet (WiFi o cable) mientras lo revisas, para limitar cualquier dano.', requiresConfirmation: true },
        { text: 'Ejecuta un analisis completo con tu antivirus.', requiresConfirmation: true },
        {
          text: 'Si el analisis encuentra algo, sigue sus instrucciones para eliminarlo; si no estas seguro, busca ayuda de un profesional.',
          requiresConfirmation: false,
        },
        { text: 'Cuando termines, cambia las contrasenas que tuvieras guardadas en ese equipo, por precaucion.', requiresConfirmation: true },
      ],
      relatedTool: { toolId: 'hygiene-checklist', label: 'Comprueba tu antivirus en el Checklist de Higiene' },
    },
  },
};
