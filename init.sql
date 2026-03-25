CREATE DATABASE IF NOT EXISTS gym_saas;
CREATE DATABASE IF NOT EXISTS evolution_db;

-- Optional: grant permissions
GRANT ALL PRIVILEGES ON gym_saas.* TO 'gymuser'@'%';
GRANT ALL PRIVILEGES ON evolution_db.* TO 'gymuser'@'%';

FLUSH PRIVILEGES;