-- 插入初始角色数据
-- 使用 INSERT IGNORE 或者类似的语法可以防止在表已存在数据时重复插入导致出错（语法取决于你的数据库，如 MySQL）
-- 对于 H2, PostgreSQL, Oracle 等，可以使用更复杂的 ON CONFLICT 语句，但对于初始化，直接 INSERT 即可，
-- 因为它只在数据库首次创建时运行。

INSERT IGNORE INTO roles (name) VALUES ('ROLE_STUDENT');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_RECRUITER');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');

-- 注意：上面的 'ON CONFLICT (name) DO NOTHING' 是 PostgreSQL 的语法。
-- 如果你使用 MySQL，可以是：
-- INSERT IGNORE INTO roles (name) VALUES ('ROLE_STUDENT'), ('ROLE_RECRUITER'), ('ROLE_ADMIN');
-- 如果你不确定，或者只是用于开发环境的 H2 数据库，可以只写简单的 INSERT，
-- 因为 Spring Boot 的 ddl-auto: create-drop 策略会确保每次启动都是一个全新的空表。
-- INSERT INTO roles (name) VALUES ('ROLE_STUDENT');
-- INSERT INTO roles (name) VALUES ('ROLE_RECRUITER');
-- INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
