use std::io;
use std::io::Write;
use std::net::TcpStream;
use std::path::PathBuf;
use std::process::{Child, Command};
use std::sync::Mutex;
use std::time::{Duration, Instant};

use tauri::{Manager, WindowEvent};

const BACKEND_PORT: &str = "8080";
const BACKEND_ADDR: &str = "127.0.0.1:8080";

/// Guarda el proceso hijo del backend para poder matarlo cuando se cierra la ventana.
struct BackendProcess(Mutex<Option<Child>>);

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .setup(|app| {
            if cfg!(debug_assertions) {
                app.handle().plugin(
                    tauri_plugin_log::Builder::default()
                        .level(log::LevelFilter::Info)
                        .build(),
                )?;
            }

            let handle = app.handle().clone();
            let child = spawn_backend(&handle);
            if child.is_none() {
                log::error!("No se ha podido arrancar el backend de NETKNIFE");
            }
            app.manage(BackendProcess(Mutex::new(child)));

            if let Some(window) = app.get_webview_window("main") {
                wait_for_backend_then_show(window);
            }

            Ok(())
        })
        .on_window_event(|window, event| {
            if let WindowEvent::CloseRequested { .. } = event {
                let state = window.app_handle().state::<BackendProcess>();
                let mut guard = state.0.lock().unwrap_or_else(|poisoned| poisoned.into_inner());
                if let Some(mut child) = guard.take() {
                    log::info!("Solicitando un apagado ordenado del backend...");
                    // Si hay algun dispositivo bloqueado (ARP spoofing), este endpoint
                    // restaura su conectividad antes de que el proceso muera: en Windows,
                    // matar el proceso directamente (mas abajo) no dispara los shutdown
                    // hooks de la JVM, asi que no basta con eso como unico mecanismo.
                    request_graceful_shutdown();
                    if !wait_for_exit(&mut child, Duration::from_secs(5)) {
                        log::warn!("El backend no respondio al apagado ordenado a tiempo; forzando el cierre");
                        let _ = child.kill();
                    }
                    let _ = child.wait();
                }
            }
        })
        .run(tauri::generate_context!())
        .expect("error al arrancar NETKNIFE");
}

/// Lanza el jar del backend como proceso hijo, apuntando su base de datos
/// SQLite al directorio de datos de la aplicacion (por usuario, gestionado por el SO).
fn spawn_backend(app: &tauri::AppHandle) -> Option<Child> {
    let jar_path = resolve_backend_jar(app)?;

    let data_dir = app
        .path()
        .app_data_dir()
        .unwrap_or_else(|_| PathBuf::from("."));
    if let Err(e) = std::fs::create_dir_all(&data_dir) {
        log::error!("No se pudo crear el directorio de datos {data_dir:?}: {e}");
    }
    let db_path = data_dir.join("netknife.db");

    let mut command = Command::new("java");
    command
        .arg("-jar")
        .arg(&jar_path)
        .env("SERVER_PORT", BACKEND_PORT)
        .env("DB_PATH", db_path.to_string_lossy().to_string());

    hide_console_window(&mut command);

    match command.spawn() {
        Ok(child) => {
            log::info!("Backend de NETKNIFE arrancado (jar: {jar_path:?}, datos: {db_path:?})");
            Some(child)
        }
        Err(e) => {
            log::error!(
                "No se pudo ejecutar 'java -jar {jar_path:?}': {e}. \
                 Comprueba que Java 21+ esta instalado y disponible en el PATH."
            );
            None
        }
    }
}

/// Resuelve la ruta al jar del backend: primero en el directorio de recursos
/// de la app empaquetada (produccion), y si no existe, en la carpeta local
/// del repositorio (util para "tauri dev" / "cargo run" en desarrollo).
fn resolve_backend_jar(app: &tauri::AppHandle) -> Option<PathBuf> {
    if let Ok(resource_dir) = app.path().resource_dir() {
        for candidate in [
            resource_dir.join("resources").join("netknife-backend.jar"),
            resource_dir.join("netknife-backend.jar"),
        ] {
            if candidate.exists() {
                return Some(candidate);
            }
        }
    }

    let dev_candidate =
        PathBuf::from(env!("CARGO_MANIFEST_DIR")).join("resources/netknife-backend.jar");
    if dev_candidate.exists() {
        return Some(dev_candidate);
    }

    None
}

#[cfg(target_os = "windows")]
fn hide_console_window(command: &mut Command) {
    use std::os::windows::process::CommandExt;
    const CREATE_NO_WINDOW: u32 = 0x08000000;
    command.creation_flags(CREATE_NO_WINDOW);
}

#[cfg(not(target_os = "windows"))]
fn hide_console_window(_command: &mut Command) {}

/// Espera (en un hilo aparte) a que el backend acepte conexiones TCP antes de
/// mostrar la ventana, para no ensenar una pantalla en blanco o con errores
/// de red mientras arranca Spring Boot.
fn wait_for_backend_then_show(window: tauri::WebviewWindow) {
    std::thread::spawn(move || {
        let deadline = Instant::now() + Duration::from_secs(30);
        while Instant::now() < deadline {
            if backend_is_ready() {
                break;
            }
            std::thread::sleep(Duration::from_millis(300));
        }
        let _ = window.show();
        let _ = window.set_focus();
    });
}

fn backend_is_ready() -> bool {
    TcpStream::connect_timeout(
        &BACKEND_ADDR.parse().expect("direccion valida"),
        Duration::from_millis(300),
    )
    .map(|_| true)
    .unwrap_or_else(|_: io::Error| false)
}

/// Pide al backend un apagado ordenado (POST /api/system/shutdown) escribiendo la
/// peticion HTTP a mano sobre un TcpStream, igual que backend_is_ready() comprueba
/// el puerto: evita anadir una dependencia como reqwest solo para esta unica llamada.
fn request_graceful_shutdown() {
    let stream = TcpStream::connect_timeout(
        &BACKEND_ADDR.parse().expect("direccion valida"),
        Duration::from_millis(500),
    );
    let Ok(mut stream) = stream else {
        log::warn!("No se pudo conectar al backend para pedirle un apagado ordenado");
        return;
    };
    let _ = stream.set_write_timeout(Some(Duration::from_millis(500)));
    let request = format!(
        "POST /api/system/shutdown HTTP/1.1\r\nHost: {BACKEND_ADDR}\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
    );
    if let Err(e) = stream.write_all(request.as_bytes()) {
        log::warn!("No se pudo enviar la peticion de apagado ordenado al backend: {e}");
    }
}

/// Espera hasta `timeout` a que el proceso hijo termine por si solo (en respuesta al
/// apagado ordenado), consultando su estado sin bloquear. Devuelve true si termino a
/// tiempo.
fn wait_for_exit(child: &mut Child, timeout: Duration) -> bool {
    let deadline = Instant::now() + timeout;
    while Instant::now() < deadline {
        match child.try_wait() {
            Ok(Some(_status)) => return true,
            Ok(None) => std::thread::sleep(Duration::from_millis(100)),
            Err(_) => return false,
        }
    }
    false
}
