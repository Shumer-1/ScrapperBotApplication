INSERT INTO tracking_link (link, user_id)
VALUES (:link, :userId)
RETURNING id;
