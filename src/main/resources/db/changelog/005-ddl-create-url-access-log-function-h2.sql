-- H2-specific function to retrieve URL entity without logging (for test database only)
CREATE ALIAS get_url AS $$
ResultSet getUrl(Connection conn, UUID uuid) throws SQLException {
    PreparedStatement statement = conn.prepareStatement("SELECT * FROM urls u WHERE u.url_uuid = ?1");
    statement.setObject(1, uuid);
    return statement.executeQuery();
}
$$;
