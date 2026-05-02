FROM node:20-alpine AS build
WORKDIR /app
RUN npm install -g pnpm

# Убираем "frontend/", так как контекст сборки уже внутри этой папки
COPY package.json pnpm-lock.yaml* ./
RUN pnpm install --frozen-lockfile

# Копируем всё содержимое текущего контекста (папки frontend) в /app
COPY ./ ./
RUN pnpm run build

# Stage 2: Runner
FROM node:20-alpine
WORKDIR /app
RUN npm install -g pnpm
COPY --from=build /app ./

EXPOSE 3000
# Вставь это вместо текущего CMD
ENV PORT=3000
ENV HOSTNAME=0.0.0.0
CMD ["pnpm", "run", "dev"]