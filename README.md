# TPI Backend - Docker Deployment & Flujo de API Completo

## Descripción General

Sistema de logística basado en microservicios desplegado con Docker Compose. Implementa todos los requerimientos funcionales mínimos del TPI.

**Servicios**:
- **Gateway**: API Gateway (Spring Cloud Gateway, puerto 8087)
- **ms-contenedores**: Gestión de solicitudes y contenedores (puerto 8081)
- **ms-transporte**: Gestión de camiones, rutas y tramos (puerto 8082)
- **ms-usuarios**: Gestión de clientes y transportistas (puerto 8083)
- **ms-tarifas**: Cálculo de tarifas y costos (puerto 8084)
- **Keycloak**: Autenticación OAuth2/JWT (puerto 8080)
- **PostgreSQL**: Base de datos (puerto 5450 en host → 5432 en contenedor)


## Inicio Rápido

### 1. Iniciar todos los servicios

**Tiempo esperado**: ~2-3 minutos (primera ejecución)

# FLUJO COMPLETO DE ENDPOINTS

## PASO 1: Registrar Cliente (OPTIONAL - si no existe)

**Endpoint**: `POST http://localhost:8087/clientes`

**Caso de uso**: Si el cliente NO existe previamente, se crea automáticamente en PASO 9 con la solicitud. Alternativamente, crear aquí.

**Headers**:
```
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "nombre": "Importadora ABC S.A.",
  "razonSocial": "ABC Importaciones",
  "cuit": "30-70123456-9",
  "domicilio": "Av. Córdoba 1500, CABA",
  "email": "contacto@abcimportaciones.com",
  "telefono": "+5491120456789"
}
```

**Response** (HTTP 201 Created):
```json
{
  "id": 1,
  "nombre": "Importadora ABC S.A.",
  "razonSocial": "ABC Importaciones",
  "cuit": "30-70123456-9",
  "domicilio": "Av. Córdoba 1500, CABA",
  "email": "contacto@abcimportaciones.com",
  "telefono": "+5491120456789"
}
```

**Precondiciones**: Token de usuario autenticado (cualquier rol)

**Controlador**: `ClienteController.crearCliente()` (ms-usuarios)

**Guardar**: `id: 1` (usaremos en PASO 9)

---

## PASO 2: Registrar Transportista

**Endpoint**: `POST http://localhost:8087/transportistas`

**Headers**:
```
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "nombre": "Transportes Rápidos SA",
  "razonSocial": "TRANRAPIA",
  "cuit": "30-61234567-0",
  "domicilio": "Ruta 5 km 10, Buenos Aires",
  "email": "admin@tranrapia.com",
  "telefono": "+5491134567890"
}
```

**Response** (HTTP 201 Created):
```json
{
  "id": 1,
  "nombre": "Transportes Rápidos SA",
  "razonSocial": "TRANRAPIA",
  "cuit": "30-61234567-0",
  "domicilio": "Ruta 5 km 10, Buenos Aires",
  "email": "admin@tranrapia.com",
  "telefono": "+5491134567890"
}
```

**Precondiciones**: Token válido

**Controlador**: `TransportistaController.crearTransportista()` (ms-usuarios)

---

## PASO 3: Registrar Camiones

**Endpoint**: `POST http://localhost:8087/camiones`

**Headers**:
```
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "dominio": "ABC-123",
  "marca": "Volvo",
  "modelo": "FH16",
  "capacidadToneladas": 25.0,
  "patente": "ABC123",
  "anio": 2022
}
```

**Response** (HTTP 200 OK):
```json
{
  "dominio": "ABC-123",
  "marca": "Volvo",
  "modelo": "FH16",
  "capacidadToneladas": 25.0,
  "patente": "ABC123",
  "anio": 2022,
  "contenedor": null,
  "idContenedor": null
}
```

**Precondiciones**: Token válido, capacidad >= peso del contenedor

**Controlador**: `CamionController.registrarCamion()` (ms-transporte)

**Guardar**: `dominio: "ABC-123"` (usaremos en PASO 11)

**Nota**: Validación de capacidad en PASO 11 al asignar contenedor

---

## PASO 4: Crear Puntos (Origen y Destino)

**Endpoint**: `POST http://localhost:8087/puntos`

**Headers**:
```
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body (Origen)**:
```json
{
  "nombre": "Puerto Buenos Aires",
  "latitud": -34.6123,
  "longitud": -58.3656,
  "tipo": "PUERTO"
}
```

**Response** (HTTP 200 OK):
```json
{
  "id": 1,
  "nombre": "Puerto Buenos Aires",
  "latitud": -34.6123,
  "longitud": -58.3656,
  "tipo": "PUERTO"
}
```

**Guardar**: `id: 1` (puntoOrigen)

**Repetir para Destino**:
```json
{
  "nombre": "Centro Distribución CABA",
  "latitud": -34.6037,
  "longitud": -58.3816,
  "tipo": "CENTRO_DISTRIBUCION"
}
```

**Response**: `id: 2` (puntoDestino)

**Controlador**: `PuntoController` (ms-transporte)

---

## PASO 5: Crear Tramos

**Endpoint**: `POST http://localhost:8087/tramos`

**Headers**:
```
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "puntoOrigen": {
    "id": 1
  },
  "puntoDestino": {
    "id": 2
  },
  "distanciaKm": 15.5,
  "tiempoEstimadoMinutos": 45
}
```

**Response** (HTTP 200 OK):
```json
{
  "id": 1,
  "puntoOrigen": {
    "id": 1,
    "nombre": "Puerto Buenos Aires"
  },
  "puntoDestino": {
    "id": 2,
    "nombre": "Centro Distribución CABA"
  },
  "distanciaKm": 15.5,
  "tiempoEstimadoMinutos": 45,
  "estado": "PENDIENTE",
  "fechaCreacion": "2025-11-17T10:30:00Z"
}
```

**Precondiciones**: Puntos deben existir (PASO 4)

**Controlador**: `TramoController` (ms-transporte)

**Guardar**: `id: 1` (tramoId)

---

## PASO 6: Crear Ruta

**Endpoint**: `POST http://localhost:8087/rutas`

**Headers**:
```
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "nombre": "Ruta Puerto a CABA",
  "distanciaTotal": 15.5,
  "tiempoEstimado": 45,
  "estado": "DISPONIBLE",
  "tramos": [
    {
      "id": 1
    }
  ]
}
```

**Response** (HTTP 200 OK):
```json
{
  "id": 1,
  "nombre": "Ruta Puerto a CABA",
  "distanciaTotal": 15.5,
  "tiempoEstimado": 45,
  "estado": "DISPONIBLE",
  "tramos": [
    {
      "id": 1,
      "distanciaKm": 15.5,
      "tiempoEstimadoMinutos": 45
    }
  ],
  "fechaCreacion": "2025-11-17T10:30:00Z"
}
```

**Precondiciones**: Tramos deben existir (PASO 5)

**Controlador**: `RutaController.crearRuta()` (ms-transporte)

**Guardar**: `id: 1` (rutaId)

---

## PASO 7: Consultar Rutas Tentativas

**Endpoint**: `GET http://localhost:8087/rutas/ruta-tentativa/1`

**Headers**:
```
Authorization: Bearer <TOKEN>
```

**Response** (HTTP 200 OK):
```json
{
  "id": 1,
  "nombre": "Ruta Puerto a CABA",
  "distanciaTotal": 15.5,
  "tiempoEstimado": 45,
  "costoEstimado": 6550.0,
  "tramos": [
    {
      "id": 1,
      "puntoOrigen": "Puerto Buenos Aires",
      "puntoDestino": "Centro Distribución CABA",
      "distanciaKm": 15.5,
      "tiempoEstimadoMinutos": 45
    }
  ]
}
```

**Notas**: 
- Este endpoint devuelve la ruta con cálculos de costo estimado
- Se usa para mostrar al cliente/operador las opciones disponibles

**Controlador**: `RutaController.obtenerRutaTentativa()` (ms-transporte)

---

## PASO 8: Crear Tarifa

**Endpoint**: `POST http://localhost:8087/tarifas`

**Headers**:
```
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "idRuta": 1,
  "precioBase": 5000.0,
  "precioKm": 100.0,
  "precioHora": 200.0,
  "descripcion": "Tarifa estándar Puerto-CABA"
}
```

**Response** (HTTP 201 Created):
```json
{
  "id": 1,
  "idRuta": 1,
  "precioBase": 5000.0,
  "precioKm": 100.0,
  "precioHora": 200.0,
  "descripcion": "Tarifa estándar Puerto-CABA",
  "costoPredictivo": 6550.0
}
```

**Cálculo**:
- costoPredictivo = precioBase + (distanciaKm * precioKm) + (tiempoEstimado * precioHora)
- = 5000 + (15.5 * 100) + (0.75 * 200) = 6550

**Controlador**: `TarifaController.registrarTarifa()` (ms-tarifas)

**Guardar**: `id: 1` (tarifaId)

---

## PASO 9: Crear Solicitud de Transporte

**Endpoint**: `POST http://localhost:8087/solicitudes`

**Headers**:
```
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "solicitud": {
    "idCliente": 1,
    "idTarifa": null,
    "observaciones": "Envío urgente"
  },
  "coordenadas": {
    "latOrigen": -34.6037,
    "lonOrigen": -58.3816,
    "latDestino": -34.7023,
    "lonDestino": -58.4430
  }
}

si cliente no existe:

{
  "solicitud": {
    "idCliente": null,
    "observaciones": "Tercera solicitud sin cliente"
  },
  "coordenadas": {
    "latOrigen": -31.4201,
    "lonOrigen": -64.1888,
    "latDestino": -32.6184,
    "lonDestino": -63.2608
  },
  "cliente": {
    "nombre": "Fabrizzio",
    "apellido": "Sana",
    "email": "fabri.perez@gmail.com",
    "telefono": "+54 11 5535 5555"
  }
}
```

**Precondiciones**:
- Si cliente NO existe: se crea automáticamente
- Contenedor se crea automáticamente en esta solicitud
- Ruta debe existir (PASO 6)

**Estados posibles**: BORRADOR → PROGRAMADA → EN_TRÁNSITO → ENTREGADA

**Controlador**: `SolicitudController.registrarSolicitud()` (ms-contenedores)

**Guardar**: `id: 1` (solicitudId)

---

## PASO 10: Asignar Ruta a Solicitud (Operador/Admin)

**Endpoint**: `POST /solicitudes/1/asignar-ruta` (endpoint implícito en el flujo)

**Notas**: La ruta se asigna en PASO 9. Si necesitas cambiarla, actualiza la solicitud.

**Cambiar estado a PROGRAMADA**:
```
PUT /solicitudes/1/estado
Content: { "estado": "PROGRAMADA" }
```

---

## PASO 11: Asignar Contenedor a Camión

**Endpoint**: `PUT http://localhost:8087/camiones/ABC-123/asignar-contenedor/1`

**Headers**:
```
Authorization: Bearer <TOKEN>
```

**Response** (HTTP 200 OK):
```json
{
  "dominio": "ABC-123",
  "marca": "Volvo",
  "modelo": "FH16",
  "capacidadToneladas": 25.0,
  "patente": "ABC123",
  "anio": 2022,
  "contenedor": {
    "id": 1,
    "numero": "CONT-001",
    "peso": 2500.0,
    "volumen": 33.2
  },
  "idContenedor": 1
}
```

**Validación**: 
- Peso del contenedor (2500 kg) <= Capacidad del camión (25000 kg) ✓
- Error si no cumple: `HTTP 400 Bad Request`

**Controlador**: `CamionController.asignarContenedor()` (ms-transporte)

---

## PASO 12: Asignar Camión a Tramo (Operador/Admin)

**Endpoint**: `POST http://localhost:8087/tramos/1/camion`

**Headers**:
```
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "dominioCamion": "ABC-123"
}
```

**Response** (HTTP 200 OK):
```json
{
  "id": 1,
  "puntoOrigen": {
    "id": 1,
    "nombre": "Puerto Buenos Aires"
  },
  "puntoDestino": {
    "id": 2,
    "nombre": "Centro Distribución CABA"
  },
  "distanciaKm": 15.5,
  "tiempoEstimadoMinutos": 45,
  "camion": {
    "dominio": "ABC-123",
    "marca": "Volvo"
  },
  "estado": "EN_PREPARACION"
}
```

**Precondiciones**: 
- Camión debe tener contenedor asignado (PASO 11)
- Contenedor debe corresponder a la solicitud/ruta

**Controlador**: `TramoController.asignarCamion()` (ms-transporte)

---

## PASO 13: Iniciar Tramo (Transportista)

**Endpoint**: `POST http://localhost:8087/tramos/1/inicio`

**Headers**:
```
Authorization: Bearer <TOKEN>
```

**Response** (HTTP 200 OK):
```json
{
  "id": 1,
  "puntoOrigen": {
    "id": 1,
    "nombre": "Puerto Buenos Aires"
  },
  "puntoDestino": {
    "id": 2,
    "nombre": "Centro Distribución CABA"
  },
  "distanciaKm": 15.5,
  "tiempoEstimadoMinutos": 45,
  "camion": {
    "dominio": "ABC-123"
  },
  "estado": "EN_TRANSITO",
  "fechaInicio": "2025-11-17T11:00:00Z",
  "horaInicio": "11:00:00"
}
```

**Efectos**:
- Tramo pasa a estado "EN_TRÁNSITO"
- Solicitud asociada pasa a "EN_TRÁNSITO"
- Se registra `fechaInicio` y `horaInicio`

**Controlador**: `TramoController.iniciarTramo()` (ms-transporte)

---

## PASO 14: Finalizar Tramo (Transportista)

**Endpoint**: `POST http://localhost:8087/tramos/1/fin`

**Headers**:
```
Authorization: Bearer <TOKEN>
```

**Response** (HTTP 200 OK):
```json
{
  "id": 1,
  "puntoOrigen": {
    "id": 1,
    "nombre": "Puerto Buenos Aires"
  },
  "puntoDestino": {
    "id": 2,
    "nombre": "Centro Distribución CABA"
  },
  "distanciaKm": 15.5,
  "tiempoEstimadoMinutos": 45,
  "camion": {
    "dominio": "ABC-123"
  },
  "estado": "COMPLETADO",
  "fechaInicio": "2025-11-17T11:00:00Z",
  "fechaFin": "2025-11-17T11:50:00Z",
  "tiempoRealMinutos": 50,
  "costoRealDia": 150.0,
  "estadiaDepositos": 0
}
```

**Cálculos**:
- tiempoRealMinutos = 50 (tiempo real desde inicio a fin)
- costoRealDia = 150.0 (tarifa por hora de estadía)
- estadiaDepositos = calculado como diferencia entre entrada y salida

**Precondiciones**: Tramo debe estar en estado "EN_TRÁNSITO"

**Controlador**: `TramoController.finalizarTramo()` (ms-transporte)

---

## PASO 15: Consultar Estado de Contenedor

**Endpoint**: `GET http://localhost:8087/contenedores/1/estado`

**Headers**:
```
Authorization: Bearer <TOKEN>
```

**Response** (HTTP 200 OK):
```json
{
  "id": 1,
  "numero": "CONT-001",
  "estado": "EN_TRANSITO",
  "ubicacionActual": "Centro Distribución CABA",
  "fechaUltimaActualizacion": "2025-11-17T11:50:00Z",
  "solicitud": {
    "id": 1,
    "estado": "EN_TRÁNSITO"
  }
}
```

**Usos**: Cliente puede consultar en tiempo real dónde está su contenedor

**Controlador**: `ContenedorController.obtenerEstadoContenedor()` (ms-contenedores)

---

## PASO 16: Consultar Contenedores Pendientes de Entrega

**Endpoint**: `GET http://localhost:8087/contenedores?estado=EN_TRANSITO`

**Headers**:
```
Authorization: Bearer <TOKEN>
```

**Response** (HTTP 200 OK):
```json
[
  {
    "id": 1,
    "numero": "CONT-001",
    "tipo": "20HC",
    "peso": 2500.0,
    "estado": "EN_TRANSITO",
    "ubicacion": "Centro Distribución CABA",
    "fechaUltimaActualizacion": "2025-11-17T11:50:00Z",
    "cliente": {
      "id": 1,
      "nombre": "Importadora ABC S.A."
    }
  },
  {
    "id": 2,
    "numero": "CONT-002",
    "tipo": "40HC",
    "peso": 3500.0,
    "estado": "EN_TRANSITO",
    "ubicacion": "Ruta 5 km 20",
    "cliente": {
      "id": 2,
      "nombre": "Logística XYZ"
    }
  }
]
```

**Filtros disponibles**: `estado=BORRADOR|PROGRAMADA|EN_TRANSITO|ENTREGADA`

**Precondiciones**: Rol OPERADOR o ADMIN

**Controlador**: `ContenedorController.obtenerContenedoresPorEstado()` (ms-contenedores)

---

## PASO 17: Registrar Cálculo de Costos Reales

**Endpoint**: `POST http://localhost:8087/solicitudes/1/calculo-real`

**Headers**:
```
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body** (automáticamente calculado en PASO 14):
```json
{
  "tiempoRealMinutos": 50,
  "costoReal": 6600.0,
  "estadiaDepositos": 100.0
}
```

**Response** (HTTP 200 OK):
```json
{
  "id": 1,
  "cliente": {
    "id": 1,
    "nombre": "Importadora ABC S.A."
  },
  "contenedor": {
    "id": 1,
    "numero": "CONT-001"
  },
  "ruta": {
    "id": 1,
    "nombre": "Ruta Puerto a CABA"
  },
  "estado": "ENTREGADA",
  "costoPredictivo": 6550.0,
  "costoReal": 6600.0,
  "tiempoEstimado": 45,
  "tiempoReal": 50,
  "detallesCosto": {
    "precioBase": 5000.0,
    "costoKm": 1550.0,
    "costoHora": 50.0,
    "estadiaDepositos": 100.0
  }
}
```

**Cálculo detallado**:
- costoReal = precioBase + (distancia * precioKm) + (tiempoReal * precioHora) + estadiaDepositos
- = 5000 + 1550 + 50 + 100 = 6600.0

**Controlador**: `SolicitudController.registrarCalculoReal()` (ms-contenedores)

---

## PASO 18: Actualizar Estado de Solicitud a ENTREGADA

**Endpoint**: `POST http://localhost:8087/solicitudes/estado`

**Headers**:
```
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "idRuta": 1,
  "estado": "ENTREGADA"
}
```

**Response** (HTTP 200 OK):
```
{ "success": true }
```

**Efectos**:
- Todas las solicitudes asociadas a la ruta pasan a "ENTREGADA"
- Contenedores pasan a "ENTREGADO"
- Se registran costos reales y tiempos reales

**Controlador**: `SolicitudController.actualizarEstadoPorRuta()` (ms-contenedores)

---

# ENDPOINTS ADICIONALES DE GESTIÓN

## Registrar/Actualizar Depósitos

**Endpoint**: `POST http://localhost:8087/depositos`

**Request Body**:
```json
{
  "nombre": "Depósito Centro",
  "ubicacion": "Av. Córdoba 1500",
  "capacidadContenedores": 100
}
```

**Controlador**: `DepositoController` (ms-transporte)

---

## Registrar/Actualizar Tarifas

**Endpoint**: `PUT http://localhost:8087/tarifas/1`

**Request Body**:
```json
{
  "precioBase": 5500.0,
  "precioKm": 110.0,
  "precioHora": 220.0,
  "descripcion": "Tarifa actualizada"
}
```

**Controlador**: `TarifaController` (ms-tarifas)

---

## Obtener Costos de Ruta

**Endpoint**: `GET http://localhost:8087/tarifas/ruta/1/costos`

**Response**:
```json
{
  "idRuta": 1,
  "nombreRuta": "Ruta Puerto a CABA",
  "distanciaTotal": 15.5,
  "tiempoEstimado": 45,
  "tarifas": [
    {
      "id": 1,
      "precioBase": 5000.0,
      "precioKm": 100.0,
      "precioHora": 200.0,
      "costoPredictivo": 6550.0
    }
  ]
}
```

**Controlador**: `TarifaController.obtenerCostosPorRuta()` (ms-tarifas)

---

## Validar Capacidad de Camión

**Endpoint**: `GET http://localhost:8087/camiones/ABC-123/validar`

**Response**:
```json
{
  "dominio": "ABC-123",
  "capacidadToneladas": 25.0,
  "pesoActual": 2.5,
  "espacioDisponible": 22.5,
  "valido": true,
  "mensaje": "Camión puede transportar más contenedores"
}
```

**Controlador**: `CamionController.validarCapacidad()` (ms-transporte)

---

## Listar Tramos por Ruta

**Endpoint**: `GET http://localhost:8087/tramos/ruta/1`

**Response**:
```json
[
  {
    "id": 1,
    "puntoOrigen": { "id": 1, "nombre": "Puerto Buenos Aires" },
    "puntoDestino": { "id": 2, "nombre": "Centro Distribución CABA" },
    "distanciaKm": 15.5,
    "tiempoEstimadoMinutos": 45,
    "estado": "COMPLETADO"
  }
]
```

**Controlador**: `TramoController.obtenerTramosPorRuta()` (ms-transporte)

---

## Obtener Tiempo Real de Tramo

**Endpoint**: `GET http://localhost:8087/tramos/1/tiempo-real`

**Response**:
```json
{
  "idTramo": 1,
  "tiempoEstimadoMinutos": 45,
  "tiempoRealMinutos": 50,
  "diferencia": 5,
  "porcentajeDevio": 11.11,
  "estado": "COMPLETADO"
}
```

**Controlador**: `TramoController.obtenerTiempoReal()` (ms-transporte)

---

## Obtener Costo Real de Tramo

**Endpoint**: `GET http://localhost:8087/tramos/1/costo-real`

**Response**:
```json
{
  "idTramo": 1,
  "costoEstimado": 1550.0,
  "costoReal": 1650.0,
  "diferencia": 100.0,
  "detalles": {
    "precioKm": 1550.0,
    "estadiaDepositos": 100.0,
    "otrosCostos": 0.0
  }
}
```

**Controlador**: `TramoController.obtenerCostoReal()` (ms-transporte)

---

# FLUJO RÁPIDO (BASH SCRIPT)

```bash
#!/bin/bash

# 1. Obtener token
TOKEN=$(curl -s -X POST "http://localhost:8080/realms/tpi-backend/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=ms-usuarios&username=tinchoadmin&password=tincho123&client_secret=R7vmLnCGKylgg2pkLsKuZXw7OmV1vYHh" | jq -r '.access_token')

echo "=== Token obtenido ==="

# 2. Crear cliente
CLIENT_ID=$(curl -s -X POST "http://localhost:8087/clientes" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test Cliente",
    "razonSocial": "TEST SA",
    "cuit": "30-12345678-9",
    "domicilio": "Calle Test 123",
    "email": "test@test.com",
    "telefono": "1234567890"
  }' | jq '.id')

echo "=== Cliente ID: $CLIENT_ID ==="

# 3. Crear ruta
RUTA_ID=$(curl -s -X POST "http://localhost:8087/rutas" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Ruta Test",
    "distanciaTotal": 100.0,
    "tiempoEstimado": 120,
    "estado": "DISPONIBLE",
    "tramos": []
  }' | jq '.id')

echo "=== Ruta ID: $RUTA_ID ==="

# 4. Crear solicitud
SOLICITUD_ID=$(curl -s -X POST "http://localhost:8087/solicitudes" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cliente": { "id": '"$CLIENT_ID"' },
    "contenedor": {
      "numero": "TEST-001",
      "tipo": "20HC",
      "peso": 2000.0,
      "volumen": 30.0
    },
    "ruta": { "id": '"$RUTA_ID"' },
    "estado": "BORRADOR",
    "notas": "Solicitud de prueba"
  }' | jq '.id')

echo "=== Solicitud ID: $SOLICITUD_ID ==="

# 5. Listar solicitudes
echo "=== Todas las solicitudes ==="
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8087/solicitudes" | jq '.'
```

---

# TROUBLESHOOTING

## Error: "issuer does not match"

**Causa**: Token de Keycloak con issuer `http://localhost:8080/realms/tpi-backend` pero Gateway espera `http://keycloak:8080/realms/tpi-backend`

**Solución**:
```bash
# Verificar issuer de Keycloak
curl -s http://localhost:8080/realms/tpi-backend/.well-known/openid-configuration | jq '.issuer'

# Debería ser: "http://keycloak:8080/realms/tpi-backend"
```

## Error: "Connection refused on http://ms-contenedores:8081"

**Causa**: Servicios no están corriendo o no pueden comunicarse

**Solución**:
```bash
docker-compose logs ms-contenedores | tail -50
docker-compose ps
```

## Error: "Cliente no encontrado"

**Causa**: ID de cliente incorrecto o cliente no existe

**Solución**: Crear cliente primero (PASO 1) o usar cliente existente

```bash
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8087/clientes" | jq '.[].id'
```

---

# NOTAS IMPORTANTES

1. **Estados de Solicitud**: BORRADOR → PROGRAMADA → EN_TRÁNSITO → ENTREGADA
2. **Estados de Contenedor**: DISPONIBLE → EN_USO → EN_TRANSITO → ENTREGADO
3. **Validación de Capacidad**: Un camión no puede superar su capacidad máxima en peso ni volumen
4. **Cálculo de Costos**:
   - Predictivo: Se calcula antes (PASO 8)
   - Real: Se calcula después (PASO 17)
5. **Autorización**: 
   - CLIENTE: Crear solicitudes, consultar estado
   - OPERADOR: Asignar rutas, tarifas, camiones
   - TRANSPORTISTA: Iniciar/finalizar tramos
   - ADMIN: Acceso a todo
6. **Rutas sin token**: `ms-transporte` no expone Spring Security; `/api/rutas/calcular` se consume internamente vía Gateway/ms-contenedores y queda abierto dentro de la red Docker.

---

**Versión**: 2.0  
**Fecha**: 17 de Noviembre, 2025  
**Implementación Completa**: ✓ Todos los requerimientos funcionales listos.
