set shell := ["bash", "-cu"]

server:
    cd apps/server && sh ./gradlew bootRun

web:
    cd apps/web && npm install && npm run dev

web-install:
    cd apps/web && npm install

web-build:
    cd apps/web && npm run build

test-server:
    cd apps/server && sh ./gradlew test --no-daemon

build target:
    case "{{target}}" in \
        server) cd apps/server && sh ./gradlew build --no-daemon ;; \
        web) cd apps/web && npm run build ;; \
        *) echo "usage: just build [server|web]" >&2; exit 1 ;; \
    esac

check-web:
    cd apps/web && npm run check

up:
    docker compose up --build
