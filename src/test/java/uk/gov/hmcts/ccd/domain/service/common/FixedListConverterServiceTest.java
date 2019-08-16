package uk.gov.hmcts.ccd.domain.service.common;

import static org.hamcrest.CoreMatchers.is;
import static uk.gov.hmcts.ccd.domain.model.definition.FieldType.COMPLEX;
import static uk.gov.hmcts.ccd.domain.service.common.FixedListConverterService.generateJsonNodeWithData;
import static uk.gov.hmcts.ccd.domain.service.common.TestBuildersUtil.CaseDataBuilder.newCaseData;
import static uk.gov.hmcts.ccd.domain.service.common.TestBuildersUtil.CaseDetailsBuilder.newCaseDetails;
import static uk.gov.hmcts.ccd.domain.service.common.TestBuildersUtil.CaseFieldBuilder.newCaseField;
import static uk.gov.hmcts.ccd.domain.service.common.TestBuildersUtil.CaseTypeBuilder.newCaseType;
import static uk.gov.hmcts.ccd.domain.service.common.TestBuildersUtil.FieldTypeBuilder.aFieldType;
import static uk.gov.hmcts.ccd.domain.service.common.TestBuildersUtil.FixedListItemBuilder.newFixedListItem;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseField;
import uk.gov.hmcts.ccd.domain.model.definition.CaseType;
import uk.gov.hmcts.ccd.domain.model.definition.FieldType;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FixedListConverterServiceTest {
    private static final JsonNodeFactory JSON_NODE_FACTORY = new JsonNodeFactory(false);
    private static final String DYNAMIC_LIST_TYPE = "DynamicList";
    private static final String TEXT_TYPE = "Text";
    private static final String JURISDICTION_ID = "JR";
    private static final String CASE_TYPE_ID = "TypeR";
    private static final String NAME = "Name";
    private static final String SURNAME = "Surname";
    private static final String PERSON = "Person";
    private static final String GENDER = "Gender";
    private static final String GENDER_W_NO_FIXEDLISTITEMS = "genderWOutFixedList";
    private static final String FIXED_LIST_ITEM_1 = "Male";
    private static final String FIXED_LIST_ITEM_2 = "Female";
    private static final String FIXED_LIST_ITEM_3 = "Other";

    private static final String male = "\"code\": \"Male\",\n \"label\": \"Male\"\n";

    private FieldType genderFieldType = aFieldType().withId(DYNAMIC_LIST_TYPE).withType(DYNAMIC_LIST_TYPE)
        .withFixedListItem(newFixedListItem().withCode(FIXED_LIST_ITEM_1).withLabel(FIXED_LIST_ITEM_1).withOrder("1").build())
        .withFixedListItem(newFixedListItem().withCode(FIXED_LIST_ITEM_2).withLabel(FIXED_LIST_ITEM_2).withOrder("2").build())
        .withFixedListItem(newFixedListItem().withCode(FIXED_LIST_ITEM_3).withLabel(FIXED_LIST_ITEM_3).withOrder("3").build())
        .build();
    private FieldType genderFieldTypeWNoFixedListItems = aFieldType().withId(DYNAMIC_LIST_TYPE).withType(DYNAMIC_LIST_TYPE)
        .build();
    private CaseField name = newCaseField().withId(NAME).withFieldType(aFieldType().withId(TEXT_TYPE).withType(TEXT_TYPE).build()).build();
    private CaseField surname = newCaseField().withId(SURNAME).withFieldType(aFieldType().withId(TEXT_TYPE).withType(TEXT_TYPE).build()).build();
    private CaseField personGender = newCaseField().withId(GENDER).withFieldType(genderFieldType).build();
    private FieldType personFieldType = aFieldType().withId(PERSON).withType(COMPLEX).withComplexField(name).withComplexField(surname).withComplexField(personGender).build();
    private CaseField person = newCaseField().withId(PERSON).withFieldType(personFieldType).build();
    private CaseField gender = newCaseField().withId(GENDER).withFieldType(genderFieldType).build();
    private CaseField genderWNoFixedList = newCaseField().withId(GENDER_W_NO_FIXEDLISTITEMS).withFieldType(genderFieldTypeWNoFixedListItems).build();
    private final CaseType caseType = newCaseType()
        .withId(CASE_TYPE_ID)
        .withField(person)
        .withField(gender)
        .withField(genderWNoFixedList)
        .build();

    private final Map<String, JsonNode> data = newCaseData()
        .withPair(NAME, JSON_NODE_FACTORY.textNode("Thomas A."))
        .withPair(SURNAME, JSON_NODE_FACTORY.textNode("Anderson"))
        .withPair(GENDER, JSON_NODE_FACTORY.textNode("Male"))
        .build();
    private final CaseDetails caseDetails = newCaseDetails()
        .withCaseTypeId(CASE_TYPE_ID)
        .withJurisdiction(JURISDICTION_ID)
        .withId("666")
        .withData(data)
        .build();

    FixedListConverterService classUnderTest = new FixedListConverterService();
    private JsonNode genderDynamic;

    @BeforeEach
    void setup() {

    }

    @DisplayName("should convert to dynamic List format when value is found in fixed list items")
    @Test
    void shouldConvertWhenValueFound() throws IOException {
        genderDynamic = generateJsonNodeWithData("{\n"
            + "          \"value\": {\n"
            + male
            + "          },\n"
            + "          \"list_items\": [\n"
            + "            {\n"
            + male
            + "            },\n"
            + "            {\n"
            + "              \"code\": \"Female\",\n"
            + "              \"label\": \"Female\"\n"
            + "            },\n"
            + "            {\n"
            + "              \"code\": \"Other\",\n"
            + "              \"label\": \"Other\"\n"
            + "            }\n"
            + "          ]\n"
            + "        }");

        classUnderTest.processListTypeData(caseType.getCaseFields(), caseDetails);
        assertThat(caseDetails.getData().get(GENDER), is(genderDynamic));
    }

    @DisplayName("should convert to dynamic List format when value is not found in fixed list items")
    @Test
    void shouldConvertWhenValueNotFound() throws IOException {
        final Map<String, JsonNode> dataWUnknownListItem = newCaseData()
            .withPair(NAME, JSON_NODE_FACTORY.textNode("Thomas A."))
            .withPair(SURNAME, JSON_NODE_FACTORY.textNode("Anderson"))
            .withPair(GENDER, JSON_NODE_FACTORY.textNode("Not Stated"))
            .build();
        CaseDetails caseDetailsWUnknownListValue = newCaseDetails()
            .withCaseTypeId(CASE_TYPE_ID)
            .withJurisdiction(JURISDICTION_ID)
            .withId("666")
            .withData(dataWUnknownListItem)
            .build();
        JsonNode genderJsonWUnknownValue = generateJsonNodeWithData("{\n"
            + "          \"value\": {\n"
            + "             \"code\": \"Not Stated\","
            + "             \"label\": \"Not Stated\""
            + "          },\n"
            + "          \"list_items\": [\n"
            + "            {\n"
            + male
            + "            },\n"
            + "            {\n"
            + "              \"code\": \"Female\",\n"
            + "              \"label\": \"Female\"\n"
            + "            },\n"
            + "            {\n"
            + "              \"code\": \"Other\",\n"
            + "              \"label\": \"Other\"\n"
            + "            },\n"
            + "            {\n"
            + "             \"code\": \"Not Stated\","
            + "             \"label\": \"Not Stated\""
            + "            }\n"
            + "          ]\n"
            + "        }");

        classUnderTest.processListTypeData(caseType.getCaseFields(), caseDetailsWUnknownListValue);
        assertThat(caseDetailsWUnknownListValue.getData().get(GENDER), is(genderJsonWUnknownValue));
    }


    @DisplayName("should convert to dynamic List format when fixed list items not found")
    @Test
    void shouldConvertWhenFixedListItemsNotFound() throws IOException {
        final Map<String, JsonNode> dataWUnknownListItem = newCaseData()
            .withPair(NAME, JSON_NODE_FACTORY.textNode("Thomas A."))
            .withPair(SURNAME, JSON_NODE_FACTORY.textNode("Anderson"))
            .withPair(GENDER_W_NO_FIXEDLISTITEMS, JSON_NODE_FACTORY.textNode("Male"))
            .build();
        CaseDetails caseDetailsWUnknownListValue = newCaseDetails()
            .withCaseTypeId(CASE_TYPE_ID)
            .withJurisdiction(JURISDICTION_ID)
            .withId("666")
            .withData(dataWUnknownListItem)
            .build();
        JsonNode genderJsonWNOFixedListItemValue = generateJsonNodeWithData("{\n"
            + "          \"value\": {\n"
            + male
            + "          },\n"
            + "          \"list_items\": [\n"
            + "            {\n"
            + male
            + "            }\n"
            + "          ]\n"
            + "        }");

        classUnderTest.processListTypeData(caseType.getCaseFields(), caseDetailsWUnknownListValue);
        assertThat(caseDetailsWUnknownListValue.getData().get(GENDER_W_NO_FIXEDLISTITEMS), is(genderJsonWNOFixedListItemValue));
    }
}
