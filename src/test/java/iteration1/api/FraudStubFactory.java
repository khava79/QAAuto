package iteration1.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import common.annotations.FraudCheckMock;

final class FraudStubFactory {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FraudStubFactory() {
    }

    static MappingBuilder create(FraudCheckMock config) {
        return WireMock.post(WireMock.urlPathMatching(config.endpoint()))
                .willReturn(buildResponse(config));
    }

    private static ResponseDefinitionBuilder buildResponse(FraudCheckMock config) {
        ResponseDefinitionBuilder response = WireMock.aResponse();

        switch (config.fault()) {
            case CONNECTION_RESET -> response.withFault(Fault.CONNECTION_RESET_BY_PEER);
            case EMPTY_RESPONSE -> response.withFault(Fault.EMPTY_RESPONSE);
            case NONE -> response
                    .withStatus(config.httpStatus())
                    .withHeader("Content-Type", "application/json")
                    .withBody(toJson(FraudMockBody.from(config)));
        }

        if (config.delayMs() > 0) {
            response.withFixedDelay(config.delayMs());
        }
        return response;
    }

    private static String toJson(FraudMockBody body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize fraud mock body", e);
        }
    }
}