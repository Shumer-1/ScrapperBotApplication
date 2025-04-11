UPDATE tracking_link
SET last_updated = :time
FROM users u
WHERE tracking_link.user_id = u.id
  AND u.tg_id = :userTelegramId
  AND tracking_link.link = :linkName;
