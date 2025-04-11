SELECT
    t.id AS tag_id,
    t.tag AS tag_value
FROM tags t
JOIN link_and_tags lt ON t.id = lt.tag_id
WHERE lt.link_id = :linkId;
