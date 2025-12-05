#!/bin/bash
cd "$(dirname "$0")"
echo "=== Iniciando TPI Backend con Docker Compose ==="
echo "Construyendo e iniciando todos los servicios..."
docker-compose up --build
