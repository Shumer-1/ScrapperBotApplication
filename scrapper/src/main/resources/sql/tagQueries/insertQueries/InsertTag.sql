INSERT INTO tags (tag)
VALUES (:tag)
RETURNING id;
