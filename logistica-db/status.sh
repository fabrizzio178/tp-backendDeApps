#!/bin/bash
cd "$(dirname "$0")"
echo "=== Estado de los Contenedores TPI Backend ==="
docker-compose ps
echo ""
echo "=== Logs Recientes ==="
docker-compose logs --tail=50
