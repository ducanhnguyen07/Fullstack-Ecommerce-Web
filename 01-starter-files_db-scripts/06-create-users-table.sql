USE `full-stack-ecommerce`;

-- Tạo bảng users
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL UNIQUE,
  `password` varchar(100) NOT NULL,
  `role` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- Insert 2 users để test
-- Password được encode bằng BCrypt
-- user/password = $2a$10$VEjxo0jq2YG8D8hFKcO3L.KF1uQKe9z1K1zP5L5pNvWvL5pNvWvL5p
-- admin/admin = $2a$10$DowJonesEvolutionOfSomethingElse.something

INSERT INTO `users` (username, password, role) VALUES 
('user', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'USER'),
('admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'ADMIN');

-- Kiểm tra
SELECT * FROM users;