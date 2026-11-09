package api.database;

import api.configs.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Единый клиент для работы с БД.
 * Поддерживает только те типы запросов, которые реально реализованы: SELECT и UPDATE.
 */
public class DBRequest {

    public enum RequestType {
        SELECT, UPDATE
    }

    private final RequestType requestType;
    private final String table;
    private final List<Condition> conditions;
    private final Map<String, Object> values;

    private DBRequest(DBRequestBuilder builder) {
        this.requestType = builder.requestType;
        this.table = builder.table;
        this.conditions = builder.conditions;
        this.values = builder.values;
    }

    public static DBRequestBuilder builder() {
        return new DBRequestBuilder();
    }

    /** Выполняет SELECT и маппит первую строку результата в объект. */
    public <T> T extractAs(Class<T> clazz) {
        requireType(RequestType.SELECT);

        String sql = "SELECT * FROM " + table + buildWhere();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            bind(statement, conditionValues());

            try (ResultSet resultSet = statement.executeQuery()) {
                return ResultSetMapper.mapOne(resultSet, clazz);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database select failed: " + sql, e);
        }
    }

    /** Выполняет UPDATE и возвращает количество затронутых строк. */
    public int execute() {
        requireType(RequestType.UPDATE);

        if (values.isEmpty()) {
            throw new IllegalStateException("UPDATE requires at least one value, use set(column, value)");
        }
        if (conditions.isEmpty()) {
            throw new IllegalStateException("UPDATE without conditions is not allowed");
        }

        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ");
        List<Object> params = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (i++ > 0) sql.append(", ");
            sql.append(entry.getKey()).append(" = ?");
            params.add(entry.getValue());
        }
        sql.append(buildWhere());
        params.addAll(conditionValues());

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            bind(statement, params);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database update failed: " + sql, e);
        }
    }

    private void requireType(RequestType expected) {
        if (requestType != expected) {
            throw new IllegalStateException(
                    "Request type is " + requestType + ", but this method requires " + expected);
        }
    }

    private String buildWhere() {
        if (conditions.isEmpty()) {
            return "";
        }
        StringBuilder where = new StringBuilder(" WHERE ");
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) where.append(" AND ");
            Condition c = conditions.get(i);
            where.append(c.getColumn()).append(" ").append(c.getOperator()).append(" ?");
        }
        return where.toString();
    }

    private List<Object> conditionValues() {
        List<Object> params = new ArrayList<>();
        for (Condition c : conditions) {
            params.add(c.getValue());
        }
        return params;
    }

    private void bind(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                Config.getProperty("db.url"),
                Config.getProperty("db.username"),
                Config.getProperty("db.password")
        );
    }

    public static class DBRequestBuilder {
        private RequestType requestType;
        private String table;
        private final List<Condition> conditions = new ArrayList<>();
        private final Map<String, Object> values = new LinkedHashMap<>();

        public DBRequestBuilder requestType(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public DBRequestBuilder table(String table) {
            this.table = table;
            return this;
        }

        public DBRequestBuilder where(Condition condition) {
            this.conditions.add(condition);
            return this;
        }

        /** Колонка и новое значение для UPDATE. */
        public DBRequestBuilder set(String column, Object value) {
            this.values.put(column, value);
            return this;
        }

        public DBRequest build() {
            if (requestType == null) {
                throw new IllegalStateException("requestType is required");
            }
            if (table == null || table.isBlank()) {
                throw new IllegalStateException("table is required");
            }
            return new DBRequest(this);
        }

        public <T> T extractAs(Class<T> clazz) {
            return build().extractAs(clazz);
        }

        public int execute() {
            return build().execute();
        }
    }
}