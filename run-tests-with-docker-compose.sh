#!/usr/bin/env bash
set -Eeuo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

compose=(docker compose -p nbank-homework -f infra/docker_compose/docker-compose.lesson.yml)
image="${TEST_IMAGE:-nbank-tests:latest}"

cleanup() {
    local status=$?
    trap - EXIT

    if (( status != 0 )); then
        echo ">>> Прогон завершился с ошибкой. Последние логи стенда:"
        "${compose[@]}" logs --tail 50 || true
    fi

    echo ">>> Останавливаем тестовое окружение"
    if ! "${compose[@]}" down; then
        echo ">>> Не удалось полностью остановить окружение" >&2
        if (( status == 0 )); then
            status=1
        fi
    fi

    exit "$status"
}

wait_for_http() {
    local label=$1
    local url=$2
    local attempt

    for (( attempt = 1; attempt <= 30; attempt++ )); do
        if curl --fail --silent --max-time 3 "$url" >/dev/null; then
            echo ">>> $label готов"
            return 0
        fi
        sleep 2
    done

    echo ">>> $label не стал доступен: $url" >&2
    return 1
}

echo ">>> Проверяем конфигурацию Compose"
"${compose[@]}" config --quiet

echo ">>> Собираем образ тестов"
docker build -t "$image" .

echo ">>> Скачиваем браузеры из конфигурации урока"
for browser_image in \
    selenoid/vnc:chrome_91.0 \
    selenoid/vnc:firefox_89.0 \
    selenoid/vnc:opera_76.0
do
    if docker image inspect "$browser_image" >/dev/null 2>&1; then
        echo ">>> Используем локальный образ $browser_image"
    else
        echo ">>> Скачиваем $browser_image"
        docker pull "$browser_image"
    fi
done

trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

echo ">>> Поднимаем тестовое окружение"
"${compose[@]}" up -d

wait_for_http "Backend" "http://localhost:4111/v3/api-docs"
wait_for_http "Frontend через nginx" "http://localhost/"
wait_for_http "Selenoid" "http://localhost:4444/status"

echo ">>> Запускаем API- и UI-тесты"
MSYS_NO_PATHCONV=1 docker run --rm \
    --network nbank-network \
    -e APIBASEURL=http://backend:4111 \
    -e UIBASEURL=http://nginx \
    -e SELENOID_URL=http://selenoid:4444/wd/hub \
    -e SELENOID_UI_URL=http://selenoid-ui:8080 \
    "$image" \
    bash -c '
        api_status=0
        ui_status=0

        echo ">>> API-тесты"
        mvn -B -Papi test || api_status=$?

        echo ">>> UI-тесты"
        mvn -B -Pui test || ui_status=$?

        echo ">>> Коды завершения: API=$api_status, UI=$ui_status"
        if (( api_status != 0 )); then
            exit "$api_status"
        fi
        exit "$ui_status"
    '