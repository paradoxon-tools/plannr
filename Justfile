set shell := ["bash", "-cu"]

server:
    cd apps/server && sh ./gradlew bootRun

test-server:
    cd apps/server && sh ./gradlew test --no-daemon

build target:
    case "{{target}}" in \
        server) cd apps/server && sh ./gradlew build --no-daemon ;; \
        *) echo "usage: just build server" >&2; exit 1 ;; \
    esac

up:
    docker compose up --build
