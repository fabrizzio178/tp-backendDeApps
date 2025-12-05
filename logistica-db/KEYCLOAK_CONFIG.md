# Configuración Keycloak para TPI Backend

## Problema: "The iss claim is not valid"

Este error ocurre cuando el `issuer` (iss) del token JWT no coincide con lo que el servidor espera.

## Solución: Configurar Keycloak correctamente

### Paso 1: Acceder a Keycloak Admin Console

1. Abre en tu navegador: **http://localhost:8080/admin**
2. Usuario: `admin`
3. Contraseña: `admin123`

### Paso 2: Verificar Realm Settings

1. En la esquina superior izquierda, verifica que esté seleccionado el realm **`tpi-backend`**
2. Si no está, haz clic en el dropdown y selecciónalo
3. Luego entra a **Realm settings** (en el menú izquierdo)

### Paso 3: Configurar URLs Frontales (IMPORTANTE)

En **Realm settings → General → Endpoints**:

Verifica que veas algo como:
```
OpenID Configuration: http://localhost:8080/realms/tpi-backend/.well-known/openid-configuration
Token Endpoint: http://localhost:8080/realms/tpi-backend/protocol/openid-connect/token
```

Si no ves esto, necesitas configurar manualmente:

1. Ve a **Realm settings**
2. Abre la pestaña **Endpoints**
3. Verifica que diga:
   - **Frontend URL**: `http://localhost:8080`
   - O déjalo en blanco para que detecte automáticamente

### Paso 4: Verificar el Token

Una vez que obtengas un token con:
```bash
curl -X POST http://localhost:8080/realms/tpi-backend/protocol/openid-connect/token \
  -d "client_id=tpi-backend-client" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "username=testuser" \
  -d "password=testpass" \
  -d "grant_type=password"
```

**Decodifica el token** en https://jwt.io y verifica:

En la sección **PAYLOAD**, busca el campo `iss` (issuer):

```json
{
  "iss": "http://localhost:8080/realms/tpi-backend",
  "sub": "user-id",
  ...
}
```

### Paso 5: Verificar Client Settings

1. Ve a **Clients** en el menú izquierdo
2. Selecciona `tpi-backend-client` (o el que uses)
3. Ve a la pestaña **Settings**
4. Verifica:
   - **Root URL**: `http://localhost:8087` (el Gateway)
   - **Valid redirect URIs**: incluya `http://localhost:8087/*`
   - **Valid post logout redirect URIs**: incluya `http://localhost:8087/*`

### Paso 6: Test Final

Cuando hagas una request a un endpoint protegido (ej: GET http://localhost:8087/usuarios):

1. Usa el token que obtuviste
2. Envíalo en el header: `Authorization: Bearer <TOKEN>`
3. Si aún da error 401, verifica que:
   - El token no esté expirado
   - El endpoint esté realmente protegido
   - El `iss` en el token sea exactamente `http://localhost:8080/realms/tpi-backend`

## Solución Rápida

Si sigues teniendo problemas:

```bash
# Reinicia Keycloak y todos los servicios
cd logistica-db
docker-compose down
docker-compose up -d

# Espera 30 segundos a que Keycloak esté listo
sleep 30

# Verifica que Keycloak respondabien
curl http://localhost:8080/realms/tpi-backend/.well-known/openid-configuration | jq '.issuer'
```

Debería responder con: `"http://localhost:8080/realms/tpi-backend"`

## Para Debugging

Usa esta herramienta REST en VS Code:

**FILE: .vscode/tpi-test.http**

```http
### 1. Obtener Token
POST http://localhost:8080/realms/tpi-backend/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=tpi-backend-client&client_secret=YOUR_CLIENT_SECRET&username=testuser&password=testpass&grant_type=password

### 2. Decodificar el token en https://jwt.io
# Copia el token de la respuesta anterior y decodifícalo

### 3. Verificar Realm Configuration
GET http://localhost:8080/realms/tpi-backend/.well-known/openid-configuration

### 4. Usar token en Gateway (ejemplo)
GET http://localhost:8087/usuarios
Authorization: Bearer YOUR_TOKEN_HERE
```

## ¿Cuál debería ser el issuer correcto?

Para Docker:
- **Desde afuera (tu máquina)**: `http://localhost:8080/realms/tpi-backend`
- **Desde adentro (dentro de Docker)**: `http://keycloak:8080/realms/tpi-backend`

El Gateway está en Docker, así que debería validar contra `http://keycloak:8080/realms/tpi-backend`, pero el token se emite con `http://localhost:8080/realms/tpi-backend` si lo obtuviste desde tu máquina.

### Fix Final

Actualiza `gateway/src/main/resources/application.yml`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/tpi-backend
```

O si el token tiene `http://localhost:8080/realms/tpi-backend`, mantén eso igual en el issuer-uri del gateway.

**La clave es que sean exactamente iguales.**
