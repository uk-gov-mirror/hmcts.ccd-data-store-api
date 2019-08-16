package uk.gov.hmcts.ccd.domain.service.common;

import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseField;
import uk.gov.hmcts.ccd.domain.model.definition.FixedListItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class FixedListConverterService {
    private static final TypeReference STRING_JSON_MAP = new TypeReference<HashMap<String, JsonNode>>() {
    };
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DYNAMIC_DATA_START = "{\n \"value\": {\n \"code\": \"";
    private static final String DYNAMIC_DATA_LABEL_START = "\",\n \"label\": \"";
    private static final String DYNAMIC_LIST_ITEMS = "\"\n },\n \"list_items\": [";
    private static final String DYNAMIC_DATA_END = "]\n }";

    public void processListTypeData(final List<CaseField> caseFields, final CaseDetails caseDetails) {
        caseFields.stream()
            .filter(caseField -> caseField.getFieldType().getType().equals("DynamicList"))
            .forEach(caseField -> {
                final JsonNode jsonNode = caseDetails.getData().get(caseField.getId());
                if (jsonNode != null && !jsonNode.isNull() && jsonNode.isTextual()) {
                    caseDetails.getData().put(caseField.getId(), convertToDynamic(caseField.getFieldType().getFixedListItems(), jsonNode));
                }
                if (caseField.isCompound()) {
                    processChildren(caseField, caseDetails);
                }
                //TODO still need to handle fixed/dynamic conversions in the complex/collection fields
            });
    }

    public void processChildren(final CaseField caseField, final CaseDetails caseDetails) {
        caseField.getFieldType().getChildren().stream()
            .filter(childField -> childField.getFieldType().getType().equals("DynamicList"))
            .forEach(childField -> {
                final JsonNode jsonNode = caseDetails.getData().get(caseField.getId()).get(childField.getId());
                if (jsonNode != null && !jsonNode.isNull() && jsonNode.isTextual()) {
                    caseDetails.getData().put(childField.getId(), convertToDynamic(childField.getFieldType().getFixedListItems(), jsonNode));
                }
                if (childField.isCompound()) {
                    processChildren(childField, caseDetails);
                }
                //TODO still need to handle fixed/dynamic conversions in the complex/collection fields
            });
    }

    private JsonNode convertToDynamic(final List<FixedListItem> fixedListItems, final JsonNode jsonNode) {
        JsonNode resultJson = jsonNode;
        final Optional<FixedListItem> optionalItem = fixedListItems.stream()
            .filter(item -> item.getCode().equalsIgnoreCase(jsonNode.textValue()))
            .findFirst();
        StringBuilder builder = new StringBuilder(DYNAMIC_DATA_START);
        if (optionalItem.isPresent()) {
            FixedListItem item = optionalItem.get();
            builder.append(item.getCode())
                .append(DYNAMIC_DATA_LABEL_START)
                .append(item.getLabel())
                .append(DYNAMIC_LIST_ITEMS);
        } else {
            FixedListItem fixedListItem = new FixedListItem();
            fixedListItem.setCode(jsonNode.textValue());
            fixedListItem.setLabel(jsonNode.textValue());
            builder.append(jsonNode.textValue())
                .append(DYNAMIC_DATA_LABEL_START)
                .append(jsonNode.textValue())
                .append(DYNAMIC_LIST_ITEMS);
            fixedListItems.add(fixedListItem);
        }
        builder.append(generateOptionsAsString(fixedListItems));
        builder.append(DYNAMIC_DATA_END);

        try {
            resultJson = generateJsonNodeWithData(builder.toString());
        } catch (IOException e) {
            //Ignore Exception
            e.printStackTrace();
        }
        return resultJson;
    }

    private CharSequence generateOptionsAsString(final List<FixedListItem> fixedListItems) {
        String options = "";
        for (FixedListItem item : fixedListItems) {
            options = options + "{\"code\": \"" + item.getCode() + "\", \"label\": \"" + item.getLabel() + "\"},";
        }
        return options.length() > 0 ? options.substring(0, options.length() - 1) : options;
    }

    static JsonNode generateJsonNodeWithData(String stringData) throws IOException {
        final Map<String, JsonNode> data = MAPPER.convertValue(MAPPER.readTree(stringData), STRING_JSON_MAP);

        return MAPPER.convertValue(data, JsonNode.class);
    }
}
