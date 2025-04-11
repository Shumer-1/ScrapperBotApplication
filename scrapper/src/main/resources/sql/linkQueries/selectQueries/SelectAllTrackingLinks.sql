SELECT
    tl.id AS tracking_link_id,
    tl.link AS tracking_link_url,
    tl.last_updated AS tracking_link_last_updated,
    u.id AS user_id,
    u.tg_id AS user_telegram_id,
    u.username AS user_username
FROM tracking_link tl
JOIN users u ON tl.user_id = u.id
ORDER BY tl.id
LIMIT :limit OFFSET :offset;
