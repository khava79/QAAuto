package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

public abstract class Request<T extends BaseModel> {

    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public Request(RequestSpecification requestSpecification,
                   ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }

    public ValidatableResponse post(T model) {
        throw new UnsupportedOperationException("POST method is not supported for this requester");
    }

    public ValidatableResponse put(T model) {
        throw new UnsupportedOperationException("PUT method is not supported for this requester");
    }
}