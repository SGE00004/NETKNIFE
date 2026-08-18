fn main() {
  // NETKNIFE necesita ejecutarse como administrador para poder cortar la conexion
  // de dispositivos no reconocidos (ver windows-app-manifest.xml). Proporcionar un
  // manifiesto propio reemplaza por completo el que trae tauri-build por defecto,
  // asi que el nuestro incluye tambien su dependencia de Common-Controls original.
  let windows_attributes =
    tauri_build::WindowsAttributes::new().app_manifest(include_str!("windows-app-manifest.xml"));

  tauri_build::try_build(tauri_build::Attributes::new().windows_attributes(windows_attributes))
    .expect("fallo al ejecutar el build script de tauri");
}
