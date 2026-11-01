package api.database;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class ResultSetMapper {
    private ResultSetMapper() {
    }

    public static <T> T mapOne(ResultSet rs, Class<T> clazz) throws SQLException {
        return rs.next() ? mapRow(rs, clazz) : null;
    }

    public static <T> List<T> mapAll(ResultSet rs, Class<T> clazz) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapRow(rs, clazz));
        }
        return result;
    }

    private static <T> T mapRow(ResultSet rs, Class<T> clazz) throws SQLException {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            ResultSetMetaData meta = rs.getMetaData();

            for (int i = 1; i <= meta.getColumnCount(); i++) {
                Field field = findField(clazz, toCamelCase(meta.getColumnLabel(i)));
                if (field == null) continue; // в DAO нет такого поля, пропускаем

                field.setAccessible(true);
                field.set(instance, convert(rs.getObject(i), field.getType()));
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot map row to " + clazz.getSimpleName(), e);
        }
    }

    private static Field findField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    // account_number -> accountNumber
    private static String toCamelCase(String column) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : column.toLowerCase().toCharArray()) {
            if (c == '_') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return sb.toString();
    }

    // приводим тип из БД к типу поля
    private static Object convert(Object value, Class<?> target) {
        if (value == null) return null;
        if (target.isInstance(value)) return value;

        if (value instanceof Number n) {
            if (target == Long.class || target == long.class) return n.longValue();
            if (target == Integer.class || target == int.class) return n.intValue();
            if (target == Double.class || target == double.class) return n.doubleValue();
            if (target == BigDecimal.class) return new BigDecimal(n.toString());
        }
        if (value instanceof Timestamp ts && target == java.time.LocalDateTime.class) {
            return ts.toLocalDateTime();
        }
        throw new IllegalArgumentException(
                "Cannot convert " + value.getClass() + " to " + target);
    }
}