SELECT
    l.id AS tracking_link_id,
    l.link AS tracking_link_url,
    l.last_updated AS tracking_link_last_updated,
    u.id AS user_id,
    u.tg_id AS user_telegram_id,
    u.username AS user_username
FROM tracking_link l
JOIN users u ON l.user_id = u.id
WHERE u.tg_id = :telegramId
  AND l.link = :linkUrl;
