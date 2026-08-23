package models.comparison;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModelComparator {

    public static <A, B> ComparisonResult compareFields(
            A request,
            B response,
            Map<String, String> fieldMappings
    ) {
        List<Mismatch> mismatches = new ArrayList<>();

        for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
            String requestField = entry.getKey();
            String responseField = entry.getValue();

            Object value1 = getFieldValue(request, requestField);
            Object value2 = getFieldValue(response, responseField);

            if (!Objects.equals(String.valueOf(value1), String.valueOf(value2))) {
                mismatches.add(
                        new Mismatch(
                                requestField + " -> " + responseField,
                                value1,
                                value2
                        )
                );
            }
        }

        return new ComparisonResult(mismatches);
    }

    private static Object getFieldValue(Object obj, String fieldPath) {
        if (obj == null) {
            return null;
        }

        String[] fields = fieldPath.split("\\.");

        Object currentObject = obj;

        for (String fieldName : fields) {

            if (currentObject == null) {
                return null;
            }

            Field field = findField(currentObject.getClass(), fieldName);

            try {
                field.setAccessible(true);
                currentObject = field.get(currentObject);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        "Cannot access field: " + fieldName,
                        e
                );
            }
        }

        return currentObject;
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;

        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }

        throw new RuntimeException(
                "Field not found: " + fieldName +
                        " in class " + clazz.getName()
        );
    }

    public static class ComparisonResult {
        private final List<Mismatch> mismatches;

        public ComparisonResult(List<Mismatch> mismatches) {
            this.mismatches = mismatches;
        }

        public boolean isSuccess() {
            return mismatches.isEmpty();
        }

        public List<Mismatch> getMismatches() {
            return mismatches;
        }

        @Override
        public String toString() {
            if (isSuccess()) {
                return "All fields match.";
            }

            StringBuilder sb = new StringBuilder("Mismatched fields:\n");

            for (Mismatch m : mismatches) {
                sb.append("- ")
                        .append(m.fieldName)
                        .append(": expected=")
                        .append(m.expected)
                        .append(", actual=")
                        .append(m.actual)
                        .append("\n");
            }

            return sb.toString();
        }
    }

    public static class Mismatch {
        public final String fieldName;
        public final Object expected;
        public final Object actual;

        public Mismatch(
                String fieldName,
                Object expected,
                Object actual
        ) {
            this.fieldName = fieldName;
            this.expected = expected;
            this.actual = actual;
        }
    }
}