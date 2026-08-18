; NETKNIFE necesita Npcap instalado para poder cortar la conexion de dispositivos
; no reconocidos (ARP spoofing via pcap4j). No se redistribuye el instalador de
; Npcap dentro del propio instalador de NETKNIFE (evitar bundling/instalacion
; silenciosa nos ahorra necesitar una licencia Npcap OEM de pago): en su lugar,
; tras instalar NETKNIFE, se ofrece descargar el instalador OFICIAL directamente
; desde npcap.com y lanzarlo para que el usuario vea y acepte la licencia de
; Npcap por si mismo, como con cualquier otra descarga manual normal.
;
; Version de Npcap a descargar: revisar https://npcap.com/#download de vez en
; cuando y actualizar si hay una version mas reciente. Si la descarga falla por
; cualquier motivo (sin internet, URL caducada...), se cae con gracia a abrir la
; pagina de descarga en el navegador para que el usuario lo haga manualmente.
!define NPCAP_DOWNLOAD_URL "https://npcap.com/dist/npcap-1.88.exe"
!define NPCAP_DOWNLOAD_PAGE "https://npcap.com/#download"

!macro NSIS_HOOK_POSTINSTALL
  IfFileExists "$SYSDIR\Npcap\wpcap.dll" npcap_already_installed

  MessageBox MB_YESNO|MB_ICONQUESTION "NETKNIFE necesita Npcap para poder cortar la conexion de dispositivos no reconocidos en tu red, y no esta instalado todavia. Quieres descargarlo e instalarlo ahora desde la web oficial (npcap.com)?" IDYES npcap_download IDNO npcap_skip

  npcap_download:
    DetailPrint "Descargando el instalador oficial de Npcap..."
    NSISdl::download "${NPCAP_DOWNLOAD_URL}" "$TEMP\npcap-installer.exe"
    Pop $0
    StrCmp $0 "success" npcap_run npcap_download_failed

  npcap_run:
    DetailPrint "Ejecutando el instalador de Npcap (sigue sus instrucciones para completarlo)..."
    ExecWait '"$TEMP\npcap-installer.exe"'
    Delete "$TEMP\npcap-installer.exe"
    Goto npcap_already_installed

  npcap_download_failed:
    MessageBox MB_OK|MB_ICONEXCLAMATION "No se ha podido descargar Npcap automaticamente (comprueba tu conexion a internet). Se abrira la pagina oficial para que lo instales manualmente cuando quieras."
    ExecShell "open" "${NPCAP_DOWNLOAD_PAGE}"
    Goto npcap_already_installed

  npcap_skip:
    DetailPrint "Npcap no se instalara ahora. El bloqueo de dispositivos no estara disponible hasta que lo instales desde npcap.com."

  npcap_already_installed:
!macroend
