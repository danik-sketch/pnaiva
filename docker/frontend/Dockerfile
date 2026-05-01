FROM node:20-alpine AS build
WORKDIR /app
RUN npm install -g pnpm
# Копируем файлы из папки frontend
COPY frontend/package.json frontend/pnpm-lock.yaml* ./
RUN pnpm install --frozen-lockfile
COPY frontend/ ./
RUN pnpm run build

# Stage 2: Runner
FROM node:20-alpine
WORKDIR /app
RUN npm install -g pnpm
COPY --from=build /app ./

# Указываем Node.js слушать порт 3000
ENV PORT=3000
EXPOSE 3000

# Флаг --host важен, чтобы сервер принимал внешние подключения внутри Docker
CMD ["pnpm", "start", "--port", "3000", "--host"]