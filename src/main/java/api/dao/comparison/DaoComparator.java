package api.dao.comparison;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;

public class DaoComparator {

    private final DaoComparisonConfigLoader configLoader;

    public DaoComparator() {
        this.configLoader = new DaoComparisonConfigLoader("dao-comparison.properties");
    }

    public void compare(Object apiResponse, Object dao) {
        DaoComparisonConfigLoader.DaoComparisonRule rule =
                configLoader.getRuleFor(apiResponse.getClass());

        if (rule == null) {
            throw new RuntimeException(
                    "No comparison rule found for " + apiResponse.getClass().getSimpleName());
        }

        Map<String, String> fieldMappings = rule.getFieldMappings();

        for (Map.Entry<String, String> mapping : fieldMappings.entrySet()) {
            String apiPath = mapping.getKey();
            String daoPath = mapping.getValue();

            Object apiValue = readPath(apiResponse, apiPath);
            Object daoValue = readPath(dao, daoPath);

            if (!valuesEqual(apiValue, daoValue)) {
                throw new AssertionError(String.format(
                        "Field mismatch for %s: API=%s, DAO=%s",
                        apiPath, apiValue, daoValue));
            }
        }
    }

    // Идёт по пути через точку: customer.name
    private Object readPath(Object root, String path) {
        Object current = root;
        for (String part : path.split("\\.")) {
            if (current == null) {
                return null;
            }
            current = readField(current, part);
        }
        return current;
    }

    private Object readField(Object obj, String fieldName) {
        Field field = findField(obj.getClass(), fieldName);
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value: " + fieldName, e);
        }
    }

    // Ищет поле в классе и во всех родителях (например, в BaseModel)
    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new RuntimeException(
                "Field '" + fieldName + "' not found in " + clazz.getSimpleName());
    }

    // Числа сравниваем по значению, а не по типу: Integer 97 == Long 97
    private boolean valuesEqual(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a instanceof Number && b instanceof Number) {
            return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString())) == 0;
        }
        return a.equals(b);
    }
}