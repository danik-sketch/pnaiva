[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=danik-sketch_pnaiva&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=danik-sketch_pnaiva)

# Notebook API (Spring Boot + PostgreSQL)

Проект покрывает требования лабораторной работы по JPA/Hibernate и транзакциям.

## 1) Реляционная БД

Используется PostgreSQL.

Конфигурация: [application.properties](/home/bazelin/projects/sem4/pnaiva/notebook/src/main/resources/application.properties)

- `spring.datasource.url=jdbc:postgresql://localhost:5432/notebook_db`
- `spring.datasource.driver-class-name=org.postgresql.Driver`
- `spring.jpa.hibernate.ddl-auto=update`

## 2) Модель данных (5 сущностей + связи)

Сущности:
- `User`
- `Project`
- `Task`
- `Category`
- `Reminder`

Связи:
- `OneToMany`: `User -> Project`, `Project -> Task`, `Task -> Reminder`
- `ManyToMany`: `Task <-> Category` (через `task_categories`)

## 3) CRUD операции

CRUD реализован через REST-контроллеры:
- `/api/users`
- `/api/projects`
- `/api/tasks`
- `/api/categories`
- `/api/reminders`

Операции `GET/POST/PUT/DELETE` есть для всех сущностей.

## 4) CascadeType и FetchType (обоснование)

- `@OneToMany(..., cascade = CascadeType.ALL, orphanRemoval = true)`
  Используется для дочерних коллекций (`projects`, `tasks`, `reminders`), чтобы дочерние записи создавались/удалялись вместе с родителем и не оставались «сиротами».
- `@ManyToOne(fetch = FetchType.LAZY)`
  Используется в `Project.user`, `Task.project`, `Reminder.task`, чтобы не тащить родительские сущности при каждом чтении дочерней.
- `@ManyToMany` для `Task.categories`
  Категории живут независимо от задач, поэтому без каскадного удаления категории при удалении задачи.

## 5) N+1: демонстрация и решение

Демонстрация N+1:
- `GET /api/tasks`
- `GET /api/projects`

Оптимизированные варианты:
- `GET /api/tasks/optimized` (использует `@EntityGraph` в `TaskRepository.findAllWithRelations`)
- `GET /api/projects/optimized` (использует `@EntityGraph` в `ProjectRepository.findAllWithUserAndTasks`)

Файлы:
- [TaskRepository.java](/home/bazelin/projects/sem4/pnaiva/notebook/src/main/java/dev/notebook/notebook/repository/TaskRepository.java)
- [ProjectRepository.java](/home/bazelin/projects/sem4/pnaiva/notebook/src/main/java/dev/notebook/notebook/repository/ProjectRepository.java)

## 6) Частичное сохранение без `@Transactional` и rollback с `@Transactional`

Сценарии реализованы в [ProjectService.java](/home/bazelin/projects/sem4/pnaiva/notebook/src/main/java/dev/notebook/notebook/service/ProjectService.java):
- `createProjectWithTasksNonTransactional(Long userId)`
- `createProjectWithTasksTransactional(Long userId)`

Endpoints для демонстрации:
- `POST /api/projects/demo/non-transactional/{userId}`
  Сохраняет проект и 1 задачу, затем выбрасывает ошибку. Часть данных останется в БД.
- `POST /api/projects/demo/transactional/{userId}`
  Работает внутри транзакции и при ошибке откатывает все изменения.

## 7) ER-диаграмма

ER-диаграмма с PK/FK и связями: [docs/ER_DIAGRAM.md](/home/bazelin/projects/sem4/pnaiva/notebook/docs/ER_DIAGRAM.md)

## Быстрый запуск

```bash
./mvnw spring-boot:run
```

Проверка тестов:

```bash
./mvnw test
```
