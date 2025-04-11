SELECT
    tl.id AS tracking_link_id,
    tl.link AS tracking_link_url,
    tl.last_updated AS tracking_link_last_updated,
    u.id AS user_id,
    u.tg_id AS user_telegram_id,
    u.username AS user_username
FROM tracking_link tl
JOIN users u ON tl.user_id = u.id
JOIN link_and_tags lt ON tl.id = lt.link_id
JOIN tags t ON lt.tag_id = t.id
WHERE u.tg_id = :telegramId
  AND t.id = :tagId;
