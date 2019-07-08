package uk.gov.hmcts.ccd.datastore.tests.functional;

import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.ccd.datastore.tests.fixture.AATCaseType.CASE_TYPE;
import static uk.gov.hmcts.ccd.datastore.tests.fixture.AATCaseType.JURISDICTION;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.datastore.tests.AATHelper;
import uk.gov.hmcts.ccd.datastore.tests.BaseTest;
import uk.gov.hmcts.ccd.datastore.tests.fixture.AATCaseBuilder;
import uk.gov.hmcts.ccd.datastore.tests.fixture.AATCaseBuilder.FullCase;
import uk.gov.hmcts.ccd.datastore.tests.fixture.AATCaseType;
import uk.gov.hmcts.ccd.datastore.tests.fixture.AATCaseType.Event;

@DisplayName("Get case by reference")
class CaseCRUDScenarioTest extends BaseTest {
    private static final String NOT_FOUND_CASE_REFERENCE = "1234123412341238";
    private static final String INVALID_CASE_REFERENCE = "1234123412341234";

    protected CaseCRUDScenarioTest(AATHelper aat) {
        super(aat);
    }

    @Test
    @DisplayName("should retrieve case when the case reference exists")
    void shouldRetrieveWhenExists() {
        // Prepare new case in known state
        final Long caseReference = Event.create()
            .as(asAutoTestCaseworker())
            .withData(FullCase.build())
            .submitAndGetReference();

        final Long caseReference2 = Event.create()
            .as(asAutoTestCaseworker())
            .withData(FullCase.build())
            .submitAndGetReference();

        final Long caseReference3 = Event.create()
            .as(asAutoTestCaseworker())
            .withData(FullCase.build())
            .submitAndGetReference();

        asAutoTestCaseworker()
            .get()

            .given()
            .pathParam("jurisdiction", JURISDICTION)
            .pathParam("caseType", CASE_TYPE)
            .pathParam("caseReference", caseReference)
            .contentType(ContentType.JSON)

            .when()
            .get("/caseworkers/{user}/jurisdictions/{jurisdiction}/case-types/{caseType}/cases/{caseReference}")

            .then()
            .log().ifError()
            .statusCode(200);

        asAutoTestCaseworker()
            .get()

            .given()
            .pathParam("jurisdiction", JURISDICTION)
            .pathParam("caseType", CASE_TYPE)
            .pathParam("caseReference", caseReference2)
            .contentType(ContentType.JSON)

            .when()
            .get("/caseworkers/{user}/jurisdictions/{jurisdiction}/case-types/{caseType}/cases/{caseReference}")

            .then()
            .log().ifError()
            .statusCode(200);


        asAutoTestCaseworker()
            .get()

            .given()
            .pathParam("jurisdiction", JURISDICTION)
            .pathParam("caseType", CASE_TYPE)
            .pathParam("caseReference", caseReference3)
            .contentType(ContentType.JSON)

            .when()
            .get("/caseworkers/{user}/jurisdictions/{jurisdiction}/case-types/{caseType}/cases/{caseReference}")

            .then()
            .log().ifError()
            .statusCode(200);

    }
/*
    @Test
    @DisplayName("should get 404 when case reference does NOT exist")
    void should404WhenNotExists() {
        asAutoTestCaseworker()
            .get()

            .given()
            .pathParam("jurisdiction", JURISDICTION)
            .pathParam("caseType", CASE_TYPE)
            .pathParam("caseReference", NOT_FOUND_CASE_REFERENCE)
            .contentType(ContentType.JSON)

            .when()
            .get("/caseworkers/{user}/jurisdictions/{jurisdiction}/case-types/{caseType}/cases/{caseReference}")

            .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("should get 400 when case reference invalid")
    void should400WhenReferenceInvalid() {
        asAutoTestCaseworker()
            .get()

            .given()
            .pathParam("jurisdiction", JURISDICTION)
            .pathParam("caseType", CASE_TYPE)
            .pathParam("caseReference", INVALID_CASE_REFERENCE)
            .contentType(ContentType.JSON)

            .when()
            .get("/caseworkers/{user}/jurisdictions/{jurisdiction}/case-types/{caseType}/cases/{caseReference}")

            .then()
            .statusCode(400);
    }*/
}
