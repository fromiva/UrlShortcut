-- PostgreSQL-specific function to retrieve URL entity and log each access to separate table
CREATE OR REPLACE FUNCTION get_url(id uuid) RETURNS urls AS $$
    INSERT INTO url_access_log(url_uuid, timestamp) VALUES ($1, now());
    SELECT * FROM urls u WHERE u.url_uuid = $1;
$$  LANGUAGE sql;
