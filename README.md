1.
初始化数据库
CREATE DATABASE todo_db DEFAULT CHARSET utf8mb4;

USE todo_db;

CREATE TABLE user (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
username VARCHAR(50) UNIQUE NOT NULL,
password VARCHAR(100) NOT NULL,
nickname VARCHAR(50),
create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE todo (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
user_id BIGINT NOT NULL,
content VARCHAR(255) NOT NULL,
finished TINYINT DEFAULT 0,
create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);


2.
src/main/resources/application.yaml
填上你自己的数据库账户名和密码


3.
src/main/java/com/wang/back/common/JwtUtil.java
填上你自己的密钥


4.
开启redis


