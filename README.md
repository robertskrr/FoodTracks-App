# 🥗 FoodTracks App

**FoodTracks** es una aplicación móvil nativa para Android diseñada para conectar de manera eficiente a personas con limitaciones alimenticias (dietas vegetarianas, veganas, celiaquía u otras intolerancias) con los establecimientos gastronómicos que satisfacen sus necesidades.

<img src="https://github.com/user-attachments/assets/e69cabc3-e838-4b80-9950-b4c045c97358" width="200" />
<img src="https://github.com/user-attachments/assets/a2dc21f7-093e-4633-ac2d-c4ab239f5158" width="200" />
<img src="https://github.com/user-attachments/assets/ed256140-b2db-4f3e-ba92-811dee445d9a" width="200" />
<img src="https://github.com/user-attachments/assets/df273189-60ae-4b85-bc3f-b665e17ea760" width="200" />

## 🎯 Propósito del Proyecto
En la actualidad, encontrar opciones gastronómicas seguras para personas con restricciones alimentarias puede ser una tarea frustrante debido a la falta de filtros precisos en plataformas generalistas y a la fragmentación de aplicaciones específicas. **FoodTracks** unifica estas necesidades, permitiendo cruzar múltiples preferencias en una sola búsqueda y aportando una capa de validación de la misma comunidad.

## 👥 Roles de Usuario
La plataforma implementa un sistema de gestión multiperfil con 4 niveles de acceso, cada uno con permisos definidos:

* **👀 Invitado (Guest):** `Modo de solo lectura.`

    Puede visualizar publicaciones, perfiles de otros usuarios y locales, y utilizar el buscador. Sin embargo, no tiene acceso a interacciones sociales (subir contenido, dar likes, valorar) hasta que se registre.

* **🧑‍🤝‍🧑 Cliente:** `Usuario estándar de la comunidad.`
    * Crear, subir y visualizar **publicaciones**.
    * **Ver y editar** su propia información de perfil.
    * Visitar el perfil de otros usuarios y locales.
    * Buscar usuarios, locales y utilizar los filtros avanzados para encontrar establecimientos.
    * **Valorar y puntuar** locales gastronómicos.
    * **Eliminar** su propia cuenta.
* **🏪 Local (Establecimiento):** `Perfil orientado a los negocios hosteleros.`
    * Posee las mismas capacidades de interacción social que el cliente, **excepto la opción de valorar a otros locales**.
    * **Ubicación precisa:** Su perfil renderiza un mapa interactivo exacto con la ubicación geográfica del establecimiento.
    * **Dashboard Estadístico:** Acceso exclusivo a un panel de control privado con métricas sobre visitas a su perfil, puntuación media, número total de valoraciones recibidas y más datos interesantes.
* **🛡️ Administrador (Admin):** `Encargado de la moderación de contenido y mantenimiento de la comunidad.`
    * Posee todas las funciones básicas de navegación e interacción de un cliente.
    * Capacidad para ver y **eliminar cualquier publicación** de la plataforma (exceptuando las creadas por otros administradores).
    * Capacidad para ver y **eliminar de forma permanente cualquier perfil** de usuario o local (exceptuando a otros administradores).
    * **Panel de Administrador:** Interfaz dedicada que muestra un historial de auditoría con todos los registros de borrado llevados a cabo en la plataforma. 
    * *Seguridad y Auditoría:* Al ejecutar el borrado de una publicación o cuenta, el sistema le exige introducir obligatoriamente un **motivo de eliminación**, el cual queda registrado en la base de datos.

## 🛠️ Tecnologías Utilizadas
* **Lenguaje y Entorno:** Java (JDK 21), Android Studio.
* **Base de datos y Autenticación:** Firebase Firestore (Base de datos NoSQL en tiempo real) y Firebase Authentication.
* **Consumo de APIs:** **Retrofit** (para la gestión de peticiones REST hacia servicios externos).
* **Mapas y Geolocalización:** osmdroid (mapas interactivos open-source) y Geocoder.
* **Gestión Multimedia:** ImageKit API (almacenamiento en la nube) y Glide (renderizado de imágenes).
* **Librerías Adicionales:** Lombok (reducción de código repetitivo) y Spotless (formateo y limpieza de código).

## 🏗️ Arquitectura del Sistema
El proyecto se ha desarrollado siguiendo una **arquitectura en capas**, respetando el Principio de Responsabilidad Única para separar la lógica de negocio, el acceso a datos y la interfaz gráfica:

* **Capa de Modelos (Models):** Entidades básicas que definen la estructura de datos, integradas con las anotaciones de Firebase y Lombok.
* **Capa de Repositorios (Repositories):** Módulos encargados de la persistencia y operaciones CRUD directas con Firebase Firestore y la API de ImageKit.
* **Capa de Servicios (Services):** Contiene la lógica de negocio compleja (ej. recálculo automático de medias o borrado de datos en cascada). Son instanciados a través de un patrón `ServiceFactory`.
* **Capa de Interfaz de Usuario (UI):** Implementación de una arquitectura *Single-Activity* basada en el uso de **Fragments**. Este diseño garantiza transiciones instantáneas, evita la recarga de vistas pesadas y ofrece una experiencia de usuario mucho más fluida.

### 📂 Estructura del Proyecto

```text
com.foodtracks.app
├── activities/             # Activities principales y controladores por rol
│   ├── admin/         
│   ├── cliente/
│   └── local/            
├── adapters/               # Adaptadores para la gestión de listas (RecyclerView)
├── api/                    # Configuración de clientes API externos
│   └── imagekit/           # Configuración de Retrofit para ImageKit
├── fragments/              # Vistas modulares de la interfaz (UI)
│   ├── admin/              # Fragmentos exclusivos del administrador
│   ├── cliente/            # Fragmentos específicos del cliente
│   └── local/              # Fragmentos específicos del local
├── models/                 # Entidades de datos
├── repositories/           # Capa de acceso a datos (Firestore)
│   └── interfaces/         # Contratos de acceso a datos (Patrón Repository)
├── services/               # Lógica de negocio (Capa de servicios)
│   ├── exceptions/         # Excepciones personalizadas del dominio
│   └── interfaces/         # Contratos de servicios (Patrón Service)
└── utils/                  # Clases de utilidad (String, Fecha, Geolocalización)
```

## 🔗 Enlaces
* **Repositorio en GitHub:** [FoodTracks-App](https://github.com/robertskrr/FoodTracks-App)
