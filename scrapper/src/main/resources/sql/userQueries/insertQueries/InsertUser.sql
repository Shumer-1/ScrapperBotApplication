INSERT INTO users (tg_id, username)
VALUES (:telegramId, :username)
RETURNING id;
