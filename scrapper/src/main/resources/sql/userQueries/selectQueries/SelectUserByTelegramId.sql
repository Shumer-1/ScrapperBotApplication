SELECT
    id,
    tg_id,
    username
FROM users
WHERE tg_id = :telegramId;
