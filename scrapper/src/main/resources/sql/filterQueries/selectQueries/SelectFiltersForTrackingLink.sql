SELECT
    f.id AS filter_id,
    f.filter AS filter_value
FROM filter f
JOIN link_and_filters lf ON f.id = lf.filter_id
WHERE lf.link_id = :linkId;
