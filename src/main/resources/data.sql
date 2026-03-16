-- 1. Чистим таблицы
TRUNCATE reminders, task_categories, tasks, projects, users RESTART IDENTITY CASCADE;

-- 2. Пользователь
INSERT INTO users (username, email, password)
VALUES ('daniil_developer', 'daniil@bsuir.by', 'password_123');

-- 3. Проекты
INSERT INTO projects (name, description, user_id)
VALUES
    ('Программа-аналог fdupes', 'Курсач по ОСиСП', 1),
    ('Arch Linux Configs', 'Мои дотфайлы для Hyprland и Waybar', 1);

-- 4. Задачи (поля соответствуют Java: title, description, completed, due_date, project_id)
INSERT INTO tasks (title, description, completed, due_date, project_id)
VALUES
    ('Дописать код', '', null, '2026-03-10 10:00:00', 1),
    ('Исправить отчет', 'Доделать обзор литературы и листиннг кода', null, '2026-03-15 12:00:00', 1),
    ('Fix Waybar', 'Поправить отображение заряда батареи', null, '2026-03-01 15:00:00', 2);

-- 5. Категории (поле title вместо name)
INSERT INTO categories (title)
VALUES ('Backend'), ('Linux'), ('University');

-- 6. Связи (task_categories)
INSERT INTO task_categories (task_id, category_id)
VALUES (1, 1), (1, 3), (3, 2);

-- 7. Напоминания (поля: reminder_time, message, task_id)
INSERT INTO reminders (time, message, task_id)
VALUES
    ('2026-03-14 10:00:00', 'Повторить теорию по Hibernate', 1),
    ('2026-03-09 09:00:00', 'Проверить конфиг sway', 3);