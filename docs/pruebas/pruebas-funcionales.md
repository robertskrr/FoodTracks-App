# Pruebas funcionales FoodTracks

## Registro de nuevo usuario (Cliente)

### Caso PF-001: Validar email
- **Acción:** Registro con email sin "@" (robertgmail.com).
- **Resultado esperado:** Error: "Formato no válido". No avanza.
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-002: Validar contraseña
- **Acción:** Registro con contraseña corta (1234).
- **Resultado esperado:** Error: Pide mínimo 8 caracteres. No avanza.
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-003: Validar confirmación contraseña
- **Acción:** Se introducen contraseñas que no coinciden.
- **Resultado esperado:** Error: "Las contraseñas no coinciden". No avanza.
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-004: Username único
- **Acción:** Registro con username ya existente (@robertskrr).
- **Resultado esperado:** Error: "Este usuario ya existe". No avanza.
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-005: Username no válido
- **Acción:** Registro con username con espacios, empieza con "@" y otros carácteres (@prueba cliente--$#).
- **Resultado esperado:** Normaliza los datos y guarda el nombre eliminando los caracteres especiales que no sean números y los espacios.
- **Resultado obtenido:** Elimina los caracteres especiales y guarda el usuario.
- **Estado:** NOK (Genera incidencia INC-001)


### Caso PF-006: Validar otra preferencia vacía
- **Acción:** Selecciona "Otra preferencia" pero no escribe nada.
- **Resultado esperado:** Guarda esa preferencia como false, como si no la hubiera seleccionado.
- **Resultado obtenido:** Se guarda como false correctamente.
- **Estado:** OK

### Caso PF-007: Registro exitoso
- **Acción:** Registro con todos los datos correctos.
- **Resultado esperado:** Crea el usuario en BD, sube su foto (si hay adjunta) y entra al MainCliente.
- **Resultado obtenido:** Usuario creado y redirige a MainCliente.
- **Estado:** OK

---

## Registro de nuevo usuario (Local)

### Caso PF-008: Validar teléfono (Longitud)
- **Acción:** Registro con teléfono de 8 dígitos (12345678).
- **Resultado esperado:** Error: Pide 9 dígitos. No avanza.
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-009: Validar teléfono (Formato)
- **Acción:** Registro con teléfono con números y letras (12345678A).
- **Resultado esperado:** No deja escribir letras en el campo, y si se insertaran, lanzaría un error pidiendo 9 dígitos. No avanza.
- **Resultado obtenido:** No deja escribir letras.
- **Estado:** OK

### Caso PF-010: Validar dirección real
- **Acción:** Registro con calle y ciudad no existentes (abcdefg, abcd).
- **Resultado esperado:** Error: "No hemos podido localizar la dirección". No avanza.
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-011: Registro exitoso (Local)
- **Acción:** Registro con todos los datos correctos.
- **Resultado esperado:** Crea el usuario en BD, sube su foto (si hay adjunta) y entra al MainLocal.
- **Resultado obtenido:** Usuario creado y redirige a MainCliente/MainLocal.
- **Estado:** OK

---

## Gestión de publicaciones

### Caso PF-012: Crear publicación vacía
- **Acción:** Enviar campo de texto en blanco ("" ó " ").
- **Resultado esperado:** Error: "El texto no puede estar vacío". No avanza.
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-013: Límite de caracteres
- **Acción:** Enviar texto con 505 caracteres.
- **Resultado esperado:** Error: "Máximo 500 caracteres". No avanza.
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-014: Crear publicación válida
- **Acción:** Enviar texto con 100 caracteres y foto adjunta.
- **Resultado esperado:** Publicación creada con éxito.
- **Resultado obtenido:** Publicación creada con éxito.
- **Estado:** OK

### Caso PF-015: Editar publicación vacía
- **Acción:** Intenta actualizar una publicación borrando todo el texto.
- **Resultado esperado:** Error: "El texto no puede estar vacío". No avanza.
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-016: Borrar publicación
- **Acción:** Intenta borrar una publicación.
- **Resultado esperado:** Borra la imagen del repositorio de ImageKit, los registros de likes y la publicación de la BD.
- **Resultado obtenido:** Se borran los datos correctamente.
- **Estado:** OK

---

## Sistema de valoraciones

### Caso PF-017: Puntuación mínima
- **Acción:** Enviar valoración de 0.0 estrellas.
- **Resultado esperado:** Error alertando de puntuación inválida (mínimo 0.5).
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-018: Puntuación máxima
- **Acción:** Enviar valoración de 5.5 estrellas.
- **Resultado esperado:** Error alertando puntuación inválida (máximo 5.0).
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-019: Primera valoración
- **Acción:** Cliente vota 4.0 a un local sin votos.
- **Resultado esperado:** El local pasa a tener media 4.0 y 1 valoración total.
- **Resultado obtenido:** Cálculos actualizados en BD.
- **Estado:** OK

### Caso PF-020: Actualizar valoración
- **Acción:** El mismo cliente cambia su voto a 5.0.
- **Resultado esperado:** Se ajusta la nota media del local sin sumar un voto extra.
- **Resultado obtenido:** Media recalculada con éxito.
- **Estado:** OK

---

## Interacciones sociales

### Caso PF-021: Dar primer like
- **Acción:** Click en el corazón de una publicación.
- **Resultado esperado:** Se guarda el registro en BD y la publicación incrementa +1 en num_likes.
- **Resultado obtenido:** Contador actualizado.
- **Estado:** OK

### Caso PF-022: Dar like como invitado
- **Acción:** Click en el corazón de una publicación siendo usuario invitado.
- **Resultado esperado:** Error: "Inicia sesión para usar esto".
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-023: Evitar like duplicado
- **Acción:** Intentar enviar otro like en la misma publicación.
- **Resultado esperado:** Error de like duplicado.
- **Resultado obtenido:** Muestra error correctamente.
- **Estado:** OK

### Caso PF-024: Quitar like
- **Acción:** Click en el like ya marcado.
- **Resultado esperado:** Se borra el registro de BD y el contador de la publicación decrementa -1 like.
- **Resultado obtenido:** Contador actualizado.
- **Estado:** OK

### Caso PF-025: Like no encontrado
- **Acción:** Forzar borrado de un like que no existe.
- **Resultado esperado:** Error de tipo FoodTracksNotFoundException.
- **Resultado obtenido:** Operación controlada.
- **Estado:** OK

### Caso PF-026: Incremento de visitas al perfil
- **Acción:** Visita el perfil de un local con cualquier rol exceptuando el mismo local.
- **Resultado esperado:** Incrementa el contador de visitas al perfil y se guarda en BD.
- **Resultado obtenido:** Contador actualizado.
- **Estado:** OK

---

## Gestión de usuarios

### Caso PF-027: Usuario borra su propia cuenta
- **Acción:** Usuario de cualquier tipo borra su cuenta confirmando con su contraseña actual.
- **Resultado esperado:** Su cuenta y todo su rastro de BD es eliminado, a la par que su Auth de Firebase.
- **Resultado obtenido:** Borrado exitoso.
- **Estado:** OK (Aunque se resolvió previamente, genera incidencia INC-002 para hacer hincapié a este proceso)

### Caso PF-028: Administrador borra publicación
- **Acción:** Administrador elimina publicación sin indicar motivo.
- **Resultado esperado:** La publicación es eliminada, se adjunta el motivo por defecto "Incumplimiento de normas de la comunidad" y se guarda la auditoría en BD.
- **Resultado obtenido:** Borrado exitoso.
- **Estado:** OK

### Caso PF-029: Administrador borra usuario
- **Acción:** Administrador borra un usuario registrando el motivo.
- **Resultado esperado:** El usuario y todo su rastro es eliminado de BD, se guarda el registro de auditoría, teniendo en cuenta que no es posible borrar el Auth de Firebase en este proceso.
- **Resultado obtenido:** Borrado exitoso.
- **Anotaciones:** El borrado del Auth del usuario borrado se hará en la consola de Firebase gracias a los registros de auditoría generados, ya que puede localizarse este usuario borrado a través del UID.
- **Estado:** OK
