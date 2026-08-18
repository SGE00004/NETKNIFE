# NETKNIFE

Traduce procesos profesionales de ciberseguridad (equipo rojo/azul) a acciones que
cualquier persona sin formación técnica puede entender y aplicar en su red doméstica
o de pequeña oficina.

El catálogo completo de herramientas ya está implementado (11 en total), cada una en
su propia carpeta autocontenida y agrupada por color/equipo igual que en la barra de
pestañas de la app (`shared/config/tools.ts`):

**Generales**

- **Escáner de Red Doméstica**: detecta qué dispositivos están conectados a tu red,
  recuerda cuáles reconoces y te avisa si aparece alguno nuevo desde tu último escaneo.
  Para los que no reconozcas, puedes **cortarles la conexión** de verdad (ARP spoofing)
  con un clic y restaurarla cuando quieras — ver [requisitos y limitaciones](#bloqueo-de-dispositivos-no-reconocidos).
- **Comprobador de Exposición**: revisa el cifrado de tu WiFi, puertos peligrosos
  escuchando en tu equipo y si UPnP está activo en tu router.
- **Simulador de Phishing**: envía un email de prueba educativo (con consentimiento
  explícito y límite de 5 destinatarios) para practicar en familia cómo reconocer un
  intento de phishing real.

**Equipo Rojo — detecta tu exposición**

- **Huella Digital**: analiza los metadatos ocultos de un archivo que subas (autor,
  software, ubicación GPS en fotos, rutas de red...) con Apache Tika, y por separado
  analiza un dominio combinando descubrimiento de subdominios sensibles (lista curada,
  no fuerza bruta), WHOIS y geolocalización IP — esta segunda parte necesita conexión a
  internet.
- **Radar de Puertos**: escanea una lista curada de puertos TCP conocidos contra un host
  (tu propio equipo por defecto), intenta capturar el "banner" de cada servicio abierto
  y traduce el riesgo a lenguaje llano.
- **Auditor Wi-Fi/Router**: combina cifrado WiFi, credenciales de fábrica del panel de
  tu router (solo verificable si el panel usa autenticación HTTP Basic; si usa un
  formulario propio se marca "No verificable" con guía manual) y estado de WPS (siempre
  "No verificable" por software, con guía manual para desactivarlo).
- **Informe de Hallazgos**: agrega en un único semáforo los últimos resultados ya
  guardados de Huella Digital, Radar de Puertos y Auditor Wi-Fi/Router, con enlace
  directo a la Guía de Incidentes ante cualquier hallazgo en rojo. No dispara escaneos
  nuevos por sí mismo, solo lee lo último que ya se ejecutó en cada herramienta.

**Equipo Azul — vigila y responde**

- **Checklist de Higiene**: comprobaciones activas de tu propio equipo (firewall,
  antivirus, cifrado de disco) más preguntas manuales para lo que no se puede detectar
  automáticamente (actualizaciones, copias de seguridad).
- **Guía de Respuesta a Incidentes**: árbol de decisión paso a paso desde "algo va mal"
  hasta acciones concretas, conectado con datos reales del Escáner de Red.
- **Analizador de Tráfico**: muestra en tiempo real con qué habla tu equipo, cruzando
  captura de paquetes (Npcap) con el proceso responsable de cada conexión (`netstat`) y
  la geolocalización del destino — mismos requisitos que el bloqueo de dispositivos, ver
  más abajo.
- **Detector de Cryptojacking**: vigilancia continua en segundo plano (arranca sola al
  abrir la app, sin privilegios de administrador) que sondea el uso de CPU por proceso
  cada pocos segundos y cruza nombre de ejecutable conocido de minero + CPU sostenida
  sin ventana visible. Permite matar el proceso sospechoso desde la UI, con
  guardarraíles en el backend: nunca un PID arbitrario, nunca el propio NETKNIFE, nunca
  un proceso crítico del sistema.

Ninguna comprobación de seguridad finge un resultado que no puede verificar: cuando
algo no se puede comprobar de forma fiable en tu sistema operativo, la app lo dice
explícitamente en vez de dar un falso "todo en orden".

## Arquitectura

Cada herramienta vive en su propia carpeta en la raíz del repo, con su backend y su
frontend juntos (`<herramienta>/backend/`, `<herramienta>/frontend/`) — nada de
herramientas mezcladas dentro de una carpeta `backend/`/`frontend/` compartida.

- **Backend**: Java 21 + Spring Boot 3, Maven multi-módulo: un módulo por herramienta
  (`network-scanner/backend/`, `exposure-checker/backend/`, `hygiene-checklist/backend/`,
  `phishing-simulator/backend/`, `port-radar/backend/`, `wifi-router-audit/backend/`,
  `digital-footprint/backend/`, `findings-report/backend/`,
  `cryptojacking-detector/backend/`, `traffic-analyzer/backend/`; `incident-guide` no
  tiene backend propio), más `platform/common/` (manejo de errores, DTOs y
  configuración compartida) y `platform/app/` (la clase principal de Spring Boot, que
  ensambla todas las herramientas en un único `.jar` ejecutable). Persistencia local en
  SQLite (sin base de datos externa, la app funciona 100% offline salvo Huella Digital,
  que necesita internet para su parte de dominio: WHOIS y geolocalización IP). Las
  herramientas de equipo rojo (Huella Digital, Radar de Puertos, Auditor Wi-Fi/Router)
  implementan la interfaz `FindingsSource` de `platform/common/`; `findings-report` la
  consume por inyección de Spring sobre todos los beans que la implementen, así que una
  herramienta roja nueva solo necesita implementarla para aparecer en el informe
  agregado, sin tocar `findings-report`.
- **Frontend**: React 18 + TypeScript + Vite + Tailwind, un único bundle (SPA) que
  compila código repartido entre `shared/` (componentes/API/tipos compartidos, más el
  "shell" de la app: `App.tsx`, `main.tsx`) y la carpeta `frontend/` de cada
  herramienta. Estado de servidor gestionado con React Query.
- **Docker**: `docker-compose.yml` levanta ambos servicios con un solo comando (usa
  `Dockerfile.backend` y `Dockerfile.frontend` en la raíz del repo). No necesitas
  instalar Java ni Node en tu máquina, solo Docker Desktop.
- **App de escritorio**: `src-tauri/` empaqueta el frontend con
  [Tauri](https://tauri.app/), que lanza el backend como proceso hijo. El resultado es
  una app nativa (`NETKNIFE.exe` + instalador), sin navegador ni pestañas visibles.

## App de escritorio (Windows) — la forma recomendada de usarla

Ya hay un acceso directo **NETKNIFE** en tu escritorio que abre la app nativa
directamente: sin Docker, sin `localhost` visible, con su propia ventana e icono en la
barra de tareas. Al arrancar, la app lanza el backend en segundo plano y espera a que
esté listo antes de mostrar la ventana.

La app quedó instalada en `%LOCALAPPDATA%\NETKNIFE\` (instalación por usuario, sin
necesitar permisos de administrador) junto con un `uninstall.exe` para desinstalarla, y
tiene también accesos directos en el menú Inicio.

**Requisito**: la app nativa lanza el backend con `java -jar`, por lo que necesita
**Java 21 o superior instalado y en el PATH** de la máquina donde se ejecute. Esto es
distinto de la vía Docker (que no requiere Java en el host). Empaquetar un runtime de
Java propio dentro del instalador (para que funcione sin depender de que el usuario
tenga Java) queda pendiente como mejora futura.

**La app pide permisos de administrador (UAC) cada vez que se abre.** Esto es
intencional: el manifiesto de Windows (`src-tauri/windows-app-manifest.xml`) declara
`requireAdministrator` porque el bloqueo de dispositivos y el Analizador de Tráfico (ver
más abajo) necesitan privilegios elevados para enviar/capturar paquetes crudos. El resto
de herramientas no lo necesita, pero se eleva la app entera para no tener que relanzar
todo el proceso a mitad de sesión solo cuando se use alguna de esas dos funciones.

### Bloqueo de dispositivos no reconocidos

Desde el detalle de un dispositivo no reconocido puedes pulsar **"Cortar conexión"**:
NETKNIFE usa ARP spoofing (la misma técnica que herramientas como NetCut) para
impedirle hablar con tu router hasta que pulses "Restaurar conexión" o cierres la app
(se restaura automáticamente al salir). Requisitos, solo en Windows por ahora:

- **[Npcap](https://npcap.com)** instalado, con la opción *"WinPcap API-compatible
  Mode"* marcada durante su instalación. El instalador NSIS de NETKNIFE
  (`..._x64-setup.exe`) te ofrece descargarlo e instalarlo automáticamente si no lo
  detecta; si usas el `.msi` en su lugar, tendrás que instalarlo tú mismo desde
  [npcap.com](https://npcap.com/#download).
- Ejecutar NETKNIFE como administrador (ya cubierto por el punto anterior: la app lo
  pide sola al abrirse).

Si falta cualquiera de los dos, el resto de la app funciona igual con normalidad; solo
el botón de bloqueo se deshabilita, con un mensaje explicando exactamente qué falta.
Esta función no está disponible ejecutando el backend vía Docker/Linux ni en macOS: la
app lo detecta y lo indica en vez de fingir que funciona.

El **Analizador de Tráfico** comparte exactamente estos mismos requisitos (Windows +
Npcap + administrador) para poder capturar paquetes; si falta alguno, muestra el motivo
concreto en vez de una lista vacía sin explicación.

### Configurar el Simulador de Phishing (opcional)

NETKNIFE **no trae credenciales de correo embebidas**. Para poder enviar simulaciones
necesitas tu propia cuenta SMTP (por ejemplo una cuenta de Gmail con una "contraseña de
aplicación", Outlook, o cualquier proveedor SMTP). Sin esto, el resto de la app funciona
igual; solo fallará el envío de simulaciones, con un mensaje claro explicando por qué.

- **Con Docker**: rellena `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` y
  `SMTP_FROM` en tu `.env` (ver `.env.example`) antes de `docker compose up`.
- **Con la app de escritorio nativa**: el proceso Java hijo hereda las variables de
  entorno del sistema operativo, **no lee el archivo `.env`** (ese archivo solo lo usa
  Docker). Define esas mismas variables como variables de entorno de usuario de Windows
  (Panel de control → Sistema → Variables de entorno, o `setx SMTP_HOST "..."` en una
  terminal) y vuelve a abrir NETKNIFE para que las recoja.

### Reconstruir el instalador tras hacer cambios

```
npm run desktop:build
```

Esto compila el reactor Maven completo (todos los módulos de todas las herramientas),
copia el jar ejecutable resultante a `src-tauri/resources/`, y genera de nuevo el
instalador en `src-tauri/target/release/bundle/` (hay versión `.exe` NSIS y `.msi`).
Para solo probar en modo desarrollo sin generar el instalador: `npm run desktop:dev`.

## Cómo levantarlo con Docker (alternativa multiplataforma, menos de 5 pasos)

1. Instala [Docker Desktop](https://www.docker.com/products/docker-desktop/) si no lo tienes.
2. Copia el archivo de variables de entorno de ejemplo:
   ```
   cp .env.example .env
   ```
3. Levanta todo con un solo comando:
   ```
   docker compose up --build
   ```
4. Abre tu navegador en **http://localhost:3000**.
5. Pulsa "Escanear ahora" en la pantalla del Escáner de Red.

Para parar la app: `docker compose down` (tus dispositivos guardados persisten en un
volumen Docker; `docker compose down -v` los borraría).

## Cómo probarlo

Usa la barra de pestañas de la cabecera para moverte entre las 11 herramientas (se
desplaza con arrastre horizontal o con la rueda del ratón).

### Escáner de Red

1. Con la app levantada, entra en http://localhost:3000.
2. Pulsa **"Escanear ahora"**. El escaneo puede tardar varios segundos (barre toda tu
   subred con ping); el botón te lo indica mientras carga.
3. Verás la lista de dispositivos detectados: IP, MAC (si se pudo obtener), fabricante
   estimado y nombre de red si está disponible.
4. Todo dispositivo **nunca visto antes** aparece marcado como "Nuevo" (rojo); uno ya
   visto pero sin reconocer aparece como "Desconocido" (amarillo), hasta que lo marques
   como **"Lo reconozco"**.
5. Haz clic en un dispositivo para ver su detalle traducido a lenguaje llano y ponerle
   un nombre personalizado (ej. "Móvil de Ana").
6. Vuelve a escanear en otro momento: los ya reconocidos se mantienen así, y solo se
   resaltan en rojo los que sean realmente nuevos desde ese escaneo.

```
curl -X POST http://localhost:8080/api/network/scan
curl http://localhost:8080/api/network/devices
curl http://localhost:8080/api/network/blocking/capability
curl -X POST http://localhost:8080/api/network/devices/1/block
curl -X POST http://localhost:8080/api/network/devices/1/unblock
```

### Comprobador de Exposición

Pulsa **"Comprobar mi exposición"**. Verás un semáforo por categoría (cifrado WiFi,
puertos peligrosos, UPnP) más uno general, cada uno con una sección desplegable "Cómo
solucionarlo" si no está en verde. Si tu sistema operativo no permite comprobar algo de
forma fiable (ej. cifrado WiFi en macOS), lo verás marcado como "No verificable" en vez
de un falso verde.

```
curl -X POST http://localhost:8080/api/exposure/check
```

### Huella Digital

Tiene dos pestañas independientes. En **"Analizar archivo"**, sube un PDF/Word/imagen y
verás los metadatos ocultos que contiene (autor, software, GPS si es una foto, rutas de
red...) clasificados como OK/Atención/Peligro. En **"Analizar dominio"**, escribe un
dominio y verás qué subdominios sensibles responden (`admin`, `vpn`, `git`...), datos
WHOIS y la geolocalización IP — esta parte necesita conexión a internet.

```
curl -X POST http://localhost:8080/api/digital-footprint/analyze-file -F "file=@informe.pdf"
curl http://localhost:8080/api/digital-footprint/last-file-report
curl -X POST http://localhost:8080/api/digital-footprint/analyze-domain -H "Content-Type: application/json" -d "{\"domain\":\"ejemplo.com\"}"
curl http://localhost:8080/api/digital-footprint/last-domain-report
```

### Radar de Puertos

Pulsa **"Escanear"** (por defecto contra `127.0.0.1`, tu propio equipo). Verás la lista
de puertos abiertos de una lista curada de puertos conocidos, con el banner capturado
del servicio si se pudo obtener y una explicación en lenguaje llano de por qué ese
puerto abierto es o no un riesgo.

```
curl -X POST http://localhost:8080/api/port-radar/scan -H "Content-Type: application/json" -d "{\"target\":\"127.0.0.1\"}"
curl http://localhost:8080/api/port-radar/last-report
```

### Auditor Wi-Fi/Router

Pulsa **"Auditar"**. Si no indicas la IP del router, se autodetecta el gateway. Verás
tres resultados: cifrado WiFi, credenciales de fábrica del panel (solo verificable si el
panel usa autenticación HTTP Basic) y estado de WPS (siempre "No verificable" por
software, con guía manual).

```
curl -X POST http://localhost:8080/api/wifi-router-audit/check
curl -X POST http://localhost:8080/api/wifi-router-audit/check -H "Content-Type: application/json" -d "{\"routerAddress\":\"192.168.1.1\"}"
curl http://localhost:8080/api/wifi-router-audit/last-report
```

### Informe de Hallazgos

No tiene botón de escaneo propio: agrega automáticamente el último informe ya guardado
de Huella Digital, Radar de Puertos y Auditor Wi-Fi/Router en un semáforo único. Si
alguna de esas herramientas todavía no se ha ejecutado nunca, aparece listada aparte
como "módulo sin datos" con un botón para ir a ejecutarla.

```
curl http://localhost:8080/api/findings-report
```

### Checklist de Higiene

Pulsa **"Revisar mi equipo"**. Verás la puntuación global ("X de Y en orden") y cada
ítem con su icono de estado. Los ítems automáticos (firewall, antivirus, cifrado de
disco) se comprueban solos; los manuales (actualizaciones, copia de seguridad) tienen
botones **Sí/No** para que respondas tú, y tu respuesta se recuerda entre sesiones.

```
curl -X POST http://localhost:8080/api/hygiene/check
curl -X PATCH http://localhost:8080/api/hygiene/checklist/backup -H "Content-Type: application/json" -d "{\"status\":\"OK\"}"
```

### Guía de Respuesta a Incidentes

Elige un síntoma (los dos completos son *"Recibí un email o mensaje raro y actué sobre
él"* y *"Veo un dispositivo desconocido en mi red"*; el resto aparece como "Próximamente").
Contesta las preguntas paso a paso hasta llegar a una recomendación final con pasos
accionables (algunos con checkbox de confirmación). Si antes has hecho un escaneo de
red con dispositivos nuevos, el árbol de "dispositivo desconocido" lo mencionará
directamente citando cuántos hay.

### Analizador de Tráfico

Necesita los mismos [requisitos que el bloqueo de dispositivos](#bloqueo-de-dispositivos-no-reconocidos)
(Windows, Npcap y administrador). Pulsa **"Iniciar captura"** y verás en tiempo real las
conexiones activas de tu equipo: proceso responsable, IP/dominio remoto, si va cifrado y
su geolocalización, marcando como "nueva" cualquier conexión aparecida en los últimos 10
segundos.

```
curl http://localhost:8080/api/traffic-analyzer/capability
curl -X POST http://localhost:8080/api/traffic-analyzer/capture/start
curl http://localhost:8080/api/traffic-analyzer/connections
curl -X POST http://localhost:8080/api/traffic-analyzer/capture/stop
```

### Detector de Cryptojacking

No requiere pulsar nada: empieza a vigilar solo al arrancar la app, sondeando el uso de
CPU por proceso cada pocos segundos. Si detecta un proceso sospechoso (nombre de minero
conocido, o CPU alta y sostenida sin ventana visible), aparece como alerta activa con la
opción de **matar el proceso** tras confirmar en un diálogo.

```
curl http://localhost:8080/api/cryptojacking-detector/status
curl http://localhost:8080/api/cryptojacking-detector/history?limit=50
```

### Simulador de Phishing

Necesita [SMTP configurado](#configurar-el-simulador-de-phishing-opcional) para enviar
de verdad. Añade uno o más destinatarios (máx. 5, con tu propio email para probarlo es
lo más sencillo), elige una plantilla, revisa la vista previa, marca la casilla de
consentimiento obligatoria y pulsa "Enviar simulación". Si abres el correo recibido y
haces clic en el enlace, te llevará a una página educativa de NETKNIFE explicando las
señales de esa plantilla, y verás el resultado ("Hizo clic · fecha") en la lista de
simulaciones enviadas en menos de 15 segundos.

```
curl http://localhost:8080/api/phishing/templates
```

## Limitaciones conocidas del escaneo dentro de Docker

El escaneo necesita ver la red LAN física del host (para hacer ping y leer la tabla
ARP), lo cual depende de la configuración de red del contenedor:

- **Linux**: `network_mode: host` (usado en `docker-compose.yml`) comparte el espacio
  de red del host directamente. El backend ve tu red LAN real sin restricciones.
  Funciona de forma nativa y sin pasos adicionales.

- **Windows y macOS (Docker Desktop)**: Docker Desktop ejecuta los contenedores dentro
  de una máquina virtual ligera, por lo que `network_mode: host` **no da acceso real a
  tu LAN física** salvo que actives el soporte experimental de red de host disponible
  en versiones recientes de Docker Desktop (Settings → Resources → Network → *Enable
  host networking*, en beta y con soporte limitado). Sin esa opción activada, el
  escaneo solo verá la red virtual interna de Docker, no los dispositivos reales de tu
  casa/oficina, y el resultado será prácticamente vacío o inútil.

  **Alternativa recomendada en Windows/Mac** si necesitas escaneo real de tu LAN: ejecutar
  el backend de forma nativa fuera de Docker (requiere JDK 21 y Maven instalados). Al ser
  un reactor multi-módulo, la primera vez hay que instalar todos los módulos en tu
  repositorio Maven local antes de poder arrancar solo `platform/app`:
  ```
  ./mvnw install -DskipTests
  cd platform/app
  ../../mvnw spring-boot:run
  ```
  (repite solo `../../mvnw spring-boot:run` en arranques posteriores; solo hace falta
  repetir el `install` si cambias código de otro módulo, como `platform/common` o
  alguna herramienta). Sigue usando el frontend en Docker (o también en local con
  `npm run dev`), apuntando `VITE_API_BASE_URL` a `http://localhost:8080/api`.

- En cualquier sistema, el contenedor del backend necesita la capacidad `NET_RAW`
  (ya incluida en `docker-compose.yml`) para poder enviar paquetes ICMP (ping).

## Estructura del proyecto

Cada herramienta es una carpeta independiente en la raíz del repo, con su backend y su
frontend juntos dentro. Lo que no pertenece a ninguna herramienta concreta (código
compartido, el "shell" de la app, Tauri, Docker, el reactor Maven, el proyecto npm)
vive en `platform/`, `shared/`, o directamente en la raíz.

```
NETKNIFE/
├── pom.xml                          # agregador Maven (reactor multi-modulo)
├── mvnw, mvnw.cmd, .mvn/
├── package.json, vite.config.ts, tsconfig.json, tailwind.config.js, index.html
├── scripts/build-backend.mjs        # compila el reactor Maven y copia el jar para Tauri
├── Dockerfile.backend, Dockerfile.frontend, nginx.conf
├── docker-compose.yml, .env.example
├── src-tauri/                       # app de escritorio nativa (Rust + Tauri)
│   ├── windows-app-manifest.xml     # pide privilegios de administrador (requireAdministrator)
│   └── installer-hooks.nsh          # el instalador NSIS ofrece instalar Npcap si falta
├── platform/
│   ├── common/                      # modulo Maven: manejo de errores, DTOs, config CORS compartida,
│   │                                 # interfaz FindingsSource que consume findings-report
│   └── app/                         # modulo Maven: NetknifeApplication, apagado ordenado;
│                                     # depende de common + todas las herramientas
├── shared/                          # componentes/API/tipos compartidos del frontend
│   ├── App.tsx, main.tsx, index.css # "shell" de la app (pestañas entre herramientas)
│   └── api/, components/, config/, theme/, types/, utils/
├── network-scanner/
│   ├── backend/                     # modulo Maven propio (com.netknife.tools.networkscanner)
│   │   ├── scan/                    # descubrimiento pasivo (ping + tabla ARP), sin deps nativas
│   │   └── blocking/                # bloqueo de dispositivos por ARP spoofing (pcap4j, solo Windows)
│   └── frontend/
├── exposure-checker/
│   ├── backend/
│   └── frontend/
├── digital-footprint/               # equipo rojo: metadatos de archivos (Apache Tika) + subdominios/WHOIS/geo de dominios
│   ├── backend/
│   └── frontend/
├── port-radar/                      # equipo rojo: escaneo TCP de una lista curada de puertos
│   ├── backend/
│   └── frontend/
├── wifi-router-audit/               # equipo rojo: credenciales de fabrica del router + estado WPS
│   ├── backend/
│   └── frontend/
├── findings-report/                 # equipo rojo: agrega los hallazgos ya guardados de las 3 herramientas anteriores
│   ├── backend/                     # sin persistencia propia, consume FindingsSource por inyeccion de Spring
│   └── frontend/
├── hygiene-checklist/
│   ├── backend/
│   └── frontend/
├── incident-guide/
│   └── frontend/                    # arboles de decision como datos TS, sin backend propio
├── traffic-analyzer/                # equipo azul: trafico de red en tiempo real (Npcap + netstat), solo Windows
│   ├── backend/
│   └── frontend/
├── cryptojacking-detector/          # equipo azul: vigilancia continua de CPU por proceso, mata procesos sospechosos
│   ├── backend/
│   └── frontend/
└── phishing-simulator/
    ├── backend/
    └── frontend/
```

Cada herramienta nueva se añade como una carpeta autocontenida más en la raíz, con su
propio `<herramienta>/backend/pom.xml` (dependiendo de `platform/common`) y su propia
`<herramienta>/frontend/`, siguiendo el mismo patrón que ya siguen las herramientas
actuales. Hay que sumarla también a `<modules>` en el `pom.xml` raíz, a las dependencias
de `platform/app/pom.xml`, y a `shared/config/tools.ts` (id, label y categoría de
color). Si es una herramienta de equipo rojo que detecta hallazgos puntuales, basta con
implementar la interfaz `FindingsSource` de `platform/common/` para que aparezca
automáticamente en el Informe de Hallazgos, sin tocar `findings-report`.
