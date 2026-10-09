#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
container=inventario-postgres
if [[ $(docker inspect --format '{{.State.Running}}' "$container") != true ]]; then
  docker start "$container" >/dev/null
fi
port=$(docker inspect --format '{{(index (index .NetworkSettings.Ports "5432/tcp") 0).HostPort}}' "$container")
export DB_USERNAME=$(docker exec "$container" sh -c 'printf %s "${POSTGRES_USER:-postgres}"')
export DB_PASSWORD=$(docker exec "$container" sh -c 'printf %s "$POSTGRES_PASSWORD"')
export DB_URL="jdbc:postgresql://localhost:${port}/reportes_ciudadanos"
: "${JWT_SECRET:?Define JWT_SECRET con al menos 32 bytes antes de ejecutar este script}"
export JWT_SECRET
exec mvn "${@:-spring-boot:run}"
