#!/bin/bash
# Перезапускает сервисы из docker-compose.yml и ждёт, пока backend начнёт отвечать.
set -e

echo ">>> Останавливаем старые контейнеры"
docker compose down -v || true

echo ">>> Скачиваем образы"
docker compose pull

echo ">>> Запускаем сервисы"
docker compose up -d

echo ">>> Ждём запуска backend на http://localhost:4111"
for i in $(seq 1 30); do
  if curl -sf http://localhost:4111/v3/api-docs > /dev/null; then
    echo ">>> Backend запущен"
    exit 0
  fi
  echo "Попытка $i/30: backend ещё не отвечает..."
  sleep 5
done

echo ">>> Backend не запустился за отведённое время. Логи контейнеров:"
docker compose logs
exit 1