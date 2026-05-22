# Incidencias FoodTracks

## INC-001 (Relacionada con PF-005)
- **Problema:** Validación en el campo Username. El sistema normaliza y guarda usuarios con nombres compuestos por caracteres especiales no válidos en lugar de rechazar el registro.
- **Prioridad:** Media
- **Estado:** Resuelto
- **Causa:** El método de normalización aplica expresiones regulares y limpieza de espacios de forma automática, validando positivamente la entrada tras la limpieza en lugar de obligar al usuario a corregirla.
- **Solución:** Se añadió una normalización en StringUtils que asegura mediante Regex que el Username solo contenga caracteres alfanuméricos válidos, en el caso de tenerlos, los elimina y mantiene solo las letras y números sin espacios.

<img width="689" height="368" alt="image" src="https://github.com/user-attachments/assets/c3f9823c-6c9b-438d-ba34-1b598068a005" />

## INC-002 (Relacionada con PF-027)
- **Problema:** Usuario borra su cuenta y su rastro, pero se mantiene su Auth de Firebase.
- **Prioridad:** Alta
- **Estado:** Resuelto
- **Causa:** No se aplica el borrado de la BD con el borrado de la sesión de Auth registrada en Firebase.
- **Solución:** Se añadió el proceso de borrado de Auth tras el borrado exitoso del rastro del usuario, a través de FirebaseUser y el método delete().

<img width="610" height="329" alt="image" src="https://github.com/user-attachments/assets/31b49520-92c6-4ed0-89da-45656916223e" />
