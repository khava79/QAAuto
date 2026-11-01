package common.extensions;

import common.annotations.APIVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class APIVersionExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String currentVersion = System.getProperty("apiVersion", "with_database_with_fix");

        APIVersion annotation = context.getElement()
                .map(e -> e.getAnnotation(APIVersion.class))
                .orElse(null);

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("Нет аннотации @APIVersion");
        }
        if (annotation.value().equals(currentVersion)) {
            return ConditionEvaluationResult.enabled("Версия совпадает");
        }
        return ConditionEvaluationResult.disabled(
                "Тест для версии " + annotation.value() + ", запущена " + currentVersion);
    }
}