# NETKNIFE

## 1. ¿Qué problema resuelve?

La ciberseguridad doméstica tiene un problema de acceso: existen herramientas profesionales excelentes para saber si tu red o tu equipo están expuestos (Wireshark, Nmap, escáneres de vulnerabilidades...), pero están pensadas para técnicos. Un usuario normal — alguien que quiere saber si un vecino se ha colado en su WiFi, o si el "email raro" que acaba de abrir es peligroso — no sabe instalarlas, ni interpretar lo que muestran.

**NETKNIFE traduce ese mundo técnico a algo que cualquiera puede usar**: mismos principios que emplean los profesionales de "equipo rojo" (atacar/detectar fallos) y "equipo azul" (defender/vigilar), pero con botones claros, explicaciones en lenguaje llano y sin necesidad de saber qué es un puerto TCP.

## 2. ¿Qué hace exactamente?

La aplicación reúne **11 herramientas** organizadas en tres bloques:

### Herramientas generales
- **Escáner de Red Doméstica**: descubre qué aparatos están conectados a tu WiFi, avisa si aparece uno nuevo y desconocido, y permite cortarle la conexión con un clic si no lo reconoces.
- **Comprobador de Exposición**: revisa de un vistazo si tu WiFi está bien cifrado, si hay puertos peligrosos abiertos en tu ordenador y si una función de tu router (UPnP) te deja vulnerable.
- **Simulador de Phishing**: envía un correo trampa educativo a tu propia familia (con permiso explícito) para practicar cómo detectar un engaño real, sin ningún riesgo.

### Equipo rojo — "encuentra tus puntos débiles antes que otro lo haga"
- **Huella Digital**: mira qué información oculta llevan tus archivos (quién los creó, con qué programa, dónde se hizo la foto) y qué expone tu dominio en internet.
- **Radar de Puertos**: comprueba qué "puertas" de tu equipo están abiertas al exterior y explica el riesgo de cada una sin jerga.
- **Auditor Wi-Fi/Router**: revisa si tu router sigue usando la contraseña de fábrica y el estado de configuraciones inseguras conocidas.
- **Informe de Hallazgos**: junta los resultados de las tres herramientas anteriores en un único semáforo (verde/amarillo/rojo) fácil de leer.

### Equipo azul — "vigila y reacciona"
- **Checklist de Higiene**: un chequeo tipo lista de la compra de tu propia seguridad (firewall, antivirus, copias de seguridad...).
- **Guía de Respuesta a Incidentes**: un árbol de preguntas ("¿qué ha pasado?") que te lleva paso a paso hasta qué hacer, con datos reales de tu red si los tiene.
- **Analizador de Tráfico**: muestra en directo con qué se está comunicando tu ordenador, útil para detectar algo raro.
- **Detector de Cryptojacking**: vigila en segundo plano si algún proceso oculto está usando tu ordenador para minar criptomonedas sin tu permiso, y te deja pararlo.

Un principio se repite en toda la app: **si algo no se puede comprobar de forma fiable, se dice explícitamente** en lugar de fingir un resultado "todo bien" falso.

## 3. ¿Cómo está construido? (explicado sin necesidad de saber programar)

Pensarlo como una casa con habitaciones independientes ayuda: cada una de las 11 herramientas es una "habitación" completa con su propia parte de "cocina" (backend, la lógica que hace el trabajo) y su propia parte de "salón" (frontend, lo que ves en pantalla). No están mezcladas entre sí, así que se puede añadir una herramienta nueva sin tocar las demás.

- **La lógica interna (backend)** está escrita en **Java**, usando un framework llamado **Spring Boot**, muy usado en el mundo profesional para aplicaciones robustas. Todo se guarda en una base de datos ligera que vive en el propio ordenador (SQLite), así que la app funciona sin internet — salvo la parte de Huella Digital que consulta datos de un dominio, que sí necesita conexión.
- **La interfaz visual (frontend)** está hecha con **React**, la tecnología más usada hoy para construir interfaces web interactivas, con **TypeScript** (que añade seguridad al código) y **Tailwind** para el diseño.
- **Docker** permite arrancar toda la aplicación con un solo comando, sin tener que instalar nada más en el ordenador salvo el propio Docker.
- Para el uso cotidiano existe además una **app de escritorio nativa para Windows** (hecha con una tecnología llamada Tauri), que se abre como cualquier otro programa, con su icono y sin ventanas de navegador de por medio.

## 4. Decisiones de diseño destacables

- **Nada de falsos positivos tranquilizadores**: cuando una comprobación no se puede hacer de forma fiable en un sistema (por ejemplo, el cifrado WiFi en macOS), la app lo marca como "No verificable" en lugar de mostrar un verde falso.
- **Permisos elevados solo cuando hacen falta**: la app pide permisos de administrador porque dos de sus funciones (cortar la conexión a un dispositivo y analizar el tráfico en directo) necesitan acceso de bajo nivel a la red. El resto de funciones no lo necesitaría, pero se pide todo junto para no interrumpir al usuario a mitad de uso.
- **Ampliable sin fricción**: las herramientas de equipo rojo comparten una misma interfaz de programación interna, así que el Informe de Hallazgos las detecta automáticamente en cuanto se añade una nueva, sin tener que modificar su propio código.

## 5. Cómo probarlo

El instalador `.exe` de NETKNIFE se encuentra en `src-tauri/target/release/bundle/nsis/` tras generarlo con `npm run desktop:build` (o ya generado, en la carpeta que se haya distribuido junto al proyecto). Se ejecuta como cualquier instalador de Windows, no pide permisos de administrador para instalarse y deja un acceso directo de **NETKNIFE** en el escritorio y en el menú Inicio.

**Requisitos para poder usarla:**

- **Windows** (la app de escritorio nativa solo está preparada para este sistema).
- **Java 21 o superior instalado y en el PATH** del ordenador: la app lanza su propio backend con `java -jar`, así que sin esto no arranca.
- **[Npcap](https://npcap.com)**, con la opción *"WinPcap API-compatible Mode"* marcada durante su instalación — solo necesario si se quieren usar el bloqueo de dispositivos no reconocidos y el Analizador de Tráfico. El propio instalador de NETKNIFE ofrece descargarlo e instalarlo automáticamente si no lo detecta. Sin Npcap, el resto de la app funciona con normalidad; solo esas dos funciones quedan deshabilitadas con un aviso explicando qué falta.
- **Permisos de administrador al abrir la app**: NETKNIFE los pide cada vez que se ejecuta (aparece el aviso de Windows/UAC), porque el bloqueo de dispositivos y el Analizador de Tráfico necesitan privilegios elevados para enviar y capturar paquetes de red.
- Opcionalmente, una **cuenta SMTP propia** (por ejemplo Gmail con "contraseña de aplicación") si se quiere usar el Simulador de Phishing; sin ella, el resto de la app funciona igual.

Una vez abierta, basta con pulsar **"Escanear ahora"** en la pantalla del Escáner de Red para empezar a usarla.

## 6. Limitaciones conocidas

- Dentro de Docker en Windows y macOS, el escaneo de red no ve la red real de casa salvo que se active una opción experimental de Docker Desktop, por las capas de virtualización que usa ese sistema. La alternativa es ejecutar el backend directamente con Java, sin Docker.
- El bloqueo de dispositivos y el análisis de tráfico en directo solo funcionan por ahora en Windows, porque dependen de una librería (Npcap) que ahí es la única probada.
- La app de escritorio necesita tener Java 21 instalado en el ordenador; incluir un Java propio dentro del instalador queda como mejora pendiente.

## 7. Conclusión

NETKNIFE no inventa técnicas nuevas de ciberseguridad: coge procesos que ya usan los profesionales de equipo rojo y azul y los convierte en algo que una persona sin formación técnica puede entender y usar en su propia red doméstica, con explicaciones en su idioma en vez de en jerga, y siendo siempre honesta sobre lo que puede y no puede comprobar.
