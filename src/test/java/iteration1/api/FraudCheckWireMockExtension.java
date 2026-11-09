package iteration1.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common.annotations.FraudCheckMock;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Optional;

public class FraudCheckWireMockExtension implements BeforeEachCallback, AfterEachCallback {

    private WireMockServer server;

    @Override
    public void beforeEach(ExtensionContext context) {
        findConfig(context).ifPresent(config -> {
            server = new WireMockServer(WireMockConfiguration.wireMockConfig().port(config.port()));
            server.start();
            server.stubFor(FraudStubFactory.create(config));
        });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    private Optional<FraudCheckMock> findConfig(ExtensionContext context) {
        return AnnotationSupport.findAnnotation(context.getTestMethod(), FraudCheckMock.class)
                .or(() -> AnnotationSupport.findAnnotation(context.getTestClass(), FraudCheckMock.class));
    }
}