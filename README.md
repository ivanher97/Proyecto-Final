#  Aplicación para la Optimización de Informes Road Survey

##  Introducción
Durante mi experiencia laboral en **Viatres Ingenieros, S.L.**, pude identificar oportunidades de mejora en la generación de informes **Road Survey**, 
utilizados para analizar rutas y maniobras de transporte especial de componentes eólicos y otros materiales voluminosos.

El objetivo de este proyecto es **reducir los tiempos de elaboración de dichos informes**, automatizando la búsqueda y generación de planos, así como 
la consulta de dimensiones máximas de los vehículos utilizados previamente en rutas conflictivas.

---

##  Descripción del problema
La empresa realiza análisis de rutas y maniobras mediante planos creados en **AutoCAD**, apoyándose en imágenes aéreas (por ejemplo, obtenidas con Iberpix).  
Cada estudio requiere analizar puntos conflictivos en las rutas y crear archivos **DWG** y **PDF** con los planos correspondientes.  
Aunque este proceso permite obtener información detallada, **es poco eficiente** cuando se requiere buscar estudios anteriores o reutilizar información existente.

Algunos de los principales inconvenientes detectados fueron:
- Dificultad para localizar planos y archivos DWG antiguos.
- Tiempo invertido en repetir análisis ya realizados.
- Necesidad de revisar manualmente información de diferentes proyectos.

---

## 💡 Descripción de la solución
Para resolver los problemas identificados, se desarrolló una **aplicación de escritorio** con dos funcionalidades principales:

### 1️ Generación automática de planos e informes
- La aplicación permite **buscar automáticamente los planos necesarios** a partir de una base de datos centralizada.
- Se pueden reutilizar estudios previos, reduciendo el tiempo necesario para crear nuevos informes Road Survey.
- El sistema detecta carreteras de una ruta creada en **Google Maps**, permitiendo obtener los estudios y planos asociados.
- Esta automatización **reduce drásticamente el tiempo de entrega** y permite a la empresa manejar un volumen mayor de proyectos.

### 2️ Consulta de dimensiones máximas de vehículos
- Permite realizar **búsquedas rápidas y sencillas** sobre las dimensiones máximas de los transportes especiales que ya han circulado por puntos conflictivos específicos.
- Facilita la **resolución de dudas** de los clientes sobre maniobras y medidas de los vehículos empleados.
- La búsqueda se realiza por carretera, punto kilométrico y dirección (Madrid, Barcelona, Castellón...).

---

##  Arquitectura del proyecto
El proyecto está compuesto por los siguientes módulos principales:

| Módulo | Descripción |
|--------|--------------|
| `connection` | Maneja la conexión con la base de datos MySQL. |
| `controllers` | Contiene la lógica de negocio y controladores de la aplicación. |
| `resources` | Archivos de configuración, estilos y recursos auxiliares. |
| `BBDD` | Carpeta destinada a los scripts y backups de la base de datos. |

---

##  Base de datos
La base de datos almacena información sobre:
- **Estudios** (planos, rutas, fechas)
- **Carreteras y puntos conflictivos**
- **Transportes y dimensiones**
- **Planos reutilizables** (DWG y PDF)

Con esta estructura se consigue una **gestión más eficiente y centralizada** de toda la información técnica generada.

---

##  Tecnologías utilizadas
- **JavaFX** – interfaz gráfica de usuario
- **MySQL** – base de datos relacional
- **AutoCAD / AutoTURN** – generación y compatibilidad de planos
- **Google Maps API** – detección y trazado de rutas
- **Iberpix** – obtención de imágenes aéreas georreferenciadas
- **Maven** – gestión de dependencias

---

##  Seguridad
Por motivos de seguridad, las credenciales de conexión a la base de datos se gestionan mediante un archivo local `config.properties`, el cual **no se sube al repositorio** gracias a la configuración de `.gitignore`.

Ejemplo de estructura del archivo:
```properties
db.url=jdbc:mysql://your-host:3306/BBDD_CONS
db.user=your_user
db.password=your_password
