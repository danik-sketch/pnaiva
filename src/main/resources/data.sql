-- 1. Чистим таблицы
TRUNCATE reminders, task_categories, tasks, projects, users RESTART IDENTITY CASCADE;

-- 2. Пользователь
INSERT INTO users (username, email, password)
VALUES
    ('daniil_developer', 'daniil@bsuir.by', 'password_123'),
    ('PEPE', 'fa@bsuir.by', 'cocohne');

-- 3. Проекты
INSERT INTO projects (name, description, user_id)
VALUES
    ('Программа-аналог fdupes', 'Курсач по ОСиСП', 1),
    ('Arch Linux Configs', 'Мои дотфайлы для Hyprland и Waybar', 1),
    ('Note API', 'Учебный REST API для заметок', 2),
    ('CI Scripts', 'Скрипты для автоматизации сборки', 2);

-- 4. Задачи (поля соответствуют Java: title, description, completed, due_date, project_id)
INSERT INTO tasks (title, description, completed, due_date, project_id)
VALUES
    ('Дописать код', '', null, '2026-03-10 10:00:00', 1),
    ('Исправить отчет', 'Доделать обзор литературы и листиннг кода', null, '2026-03-15 12:00:00', 1),
    ('Fix Waybar', 'Поправить отображение заряда батареи', null, '2026-03-01 15:00:00', 2),
    ('Собрать билд', 'Проверить сборку проекта', '2026-03-05 18:00:00', '2026-03-04 10:30:00', 2),
    ('Сделать эндпоинты', 'CRUD для заметок', null, '2026-03-20 09:00:00', 3),
    ('Добавить пагинацию', 'Pageable для списка заметок', '2026-03-21 12:00:00', '2026-03-19 17:15:00', 3),
    ('Настроить CI', 'GitHub Actions workflow', null, '2026-03-25 14:00:00', 4),
    ('Починить тесты', 'Падает интеграционный тест', null, '2026-03-22 11:00:00', 4);

-- 5. Категории (поле title вместо name)
INSERT INTO categories (title)
VALUES ('Backend'), ('Linux'), ('University'), ('Frontend'), ('DevOps');

-- 6. Связи (task_categories)
INSERT INTO task_categories (task_id, category_id)
VALUES
    (1, 1), (1, 3), (3, 2),
    (4, 2), (5, 1), (6, 1),
    (6, 4), (7, 5), (8, 5);

-- 7. Напоминания (поля: reminder_time, message, task_id)
INSERT INTO reminders (time, message, task_id)
VALUES
    ('2026-03-14 10:00:00', 'Повторить теорию по Hibernate', 1),
    ('2026-03-09 09:00:00', 'Проверить конфиг sway', 3),
    ('2026-03-18 08:30:00', 'Подготовить демо для API', 5),
    ('2026-03-23 16:00:00', 'Проверить логи CI', 7);
