FROM node:20-alpine AS build
WORKDIR /app
RUN npm install -g pnpm
COPY frontend/package.json frontend/pnpm-lock.yaml* ./
RUN pnpm install --frozen-lockfile
COPY frontend/ ./
RUN pnpm run build

# Stage 2: Runner
FROM node:20-alpine
WORKDIR /app
RUN npm install -g pnpm
COPY --from=build /app ./

EXPOSE 3000

# Добавляем флаги для запуска на 3000 порту и открытия хоста
CMD ["pnpm", "run", "dev", "--port", "3000", "-H", "0.0.0.0"]