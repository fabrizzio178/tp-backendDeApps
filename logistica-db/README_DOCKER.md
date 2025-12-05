# TPI Backend - Docker Compose

## Descripción
Este proyecto utiliza Docker Compose para orquestar todos los microservicios del TPI Backend en contenedores.

## Servicios

### Base de datos y autenticación
- **PostgreSQL 18** (puerto 5450): Base de datos compartida para todos los microservicios
- **Keycloak 24** (puerto 8080): Servidor de autenticación y autorización

### Microservicios
- **Gateway** (puerto 8087): API Gateway que enruta las peticiones
- **MS-Contenedores** (puerto 8081): Gestión de contenedores
- **MS-Transporte** (puerto 8082): Gestión de transportes y rutas
- **MS-Usuarios** (puerto 8083): Gestión de usuarios
- **MS-Tarifas** (puerto 8084): Gestión de tarifas

## Requisitos

- Docker y Docker Compose instalados
- Al menos 4GB de RAM disponible para los contenedores

## Instrucciones de Uso

### Iniciar todos los servicios

```bash
cd logistica-db
docker-compose up --build
```

O usar el script helper:

```bash
cd logistica-db
bash start.sh
```

### Detener todos los servicios

```bash
cd logistica-db
docker-compose down
```

### Ver estado de los servicios

```bash
cd logistica-db
docker-compose ps
```

### Ver logs de un servicio específico

```bash
cd logistica-db
docker-compose logs -f gateway      # Para ver logs del gateway
docker-compose logs -f ms-contenedores
docker-compose logs -f ms-transporte
docker-compose logs -f ms-usuarios
docker-compose logs -f ms-tarifas
```

## Primera ejecución

La primera vez que ejecutas `docker-compose up --build`, los contenedores compilarán todos los microservicios con Maven. Esto puede tomar **15-30 minutos** dependiendo de tu conexión a internet y velocidad de máquina.

En compilaciones posteriores, Docker usará caché de capas, por lo que será mucho más rápido.

## Características

- ✅ Compilación automática con Maven multi-stage
- ✅ Alpine Linux (imágenes ligeras)
- ✅ URLs internas usando nombres de servicio Docker
- ✅ Health checks en servicios críticos
- ✅ Orden de startup automático con `depends_on`
- ✅ Networking automático entre contenedores
- ✅ Base de datos persistente con volumen

## Configuración

### Acceso a servicios

| Servicio | URL | Propósito |
|----------|-----|----------|
| Gateway | http://localhost:8087 | Punto de entrada API |
| Keycloak | http://localhost:8080 | Admin y autenticación |
| PostgreSQL | localhost:5450 | Base de datos |

### Credenciales por defecto

**PostgreSQL:**
- Usuario: `utn`
- Contraseña: `utn123`
- Base de datos: `logistica`

**Keycloak:**
- Usuario: `admin`
- Contraseña: `admin123`

## Solución de problemas

### "docker: command not found"
- Instala Docker Desktop desde https://www.docker.com/products/docker-desktop

### Contenedores terminan inmediatamente
- Revisa los logs: `docker-compose logs`
- Verifica que los puertos no estén en uso

### Build muy lento
- Es normal en la primera compilación. Tarda 15-30 minutos
- Usa `docker-compose logs -f` para ver el progreso en tiempo real

### "Port already in use"
- Un servicio en ese puerto ya está en ejecución
- Detén otros servicios: `docker-compose down`
- O cambia los puertos en `docker-compose.yml`

## Estructura de archivos

```
tpi-backend/
├── docker/
│   ├── Dockerfile.gateway
│   ├── Dockerfile.ms-contenedores
│   ├── Dockerfile.ms-tarifas
│   ├── Dockerfile.ms-transporte
│   └── Dockerfile.ms-usuarios
├── logistica-db/
│   ├── docker-compose.yml          # Orquestación principal
│   ├── start.sh                    # Script para iniciar
│   ├── status.sh                   # Script para ver estado
│   ├── init/
│   │   └── logistica.sql           # Script de inicialización BD
│   └── keycloak/
│       └── tpi-backend-realm.json  # Configuración Keycloak
├── gateway/
├── ms-contenedores/
├── ms-tarifas/
├── ms-transporte/
└── ms-usuarios/
```

## URLs de conexión entre servicios (Docker)

Dentro de Docker, los servicios se comunican usando nombres de servicio:

- ms-contenedores:8081
- ms-transporte:8082
- ms-usuarios:8083
- ms-tarifas:8084
- logistica-db:5432 (PostgreSQL)
- keycloak:8080

Estos están configurados automáticamente en los `application.yml` de cada microservicio.
