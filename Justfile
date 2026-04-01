set shell := ["bash", "-cu"]

server:
    cd apps/server && ./gradlew bootRun

web:
    cd apps/web && npm install && npm run dev

web-install:
    cd apps/web && npm install

web-build:
    cd apps/web && npm run build

test-server:
    cd apps/server && ./gradlew test --no-daemon

check-web:
    cd apps/web && npm run check

up:
    docker compose up --build
