package uk.gov.hmcts.ccd.domain.service.search.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.ApplicationParams;
import uk.gov.hmcts.ccd.data.casedetails.search.SortOrderField;
import uk.gov.hmcts.ccd.data.user.UserService;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.search.UseCase;
import uk.gov.hmcts.ccd.domain.service.aggregated.AuthorisedGetCaseTypeOperation;
import uk.gov.hmcts.ccd.domain.service.aggregated.GetCaseTypeOperation;
import uk.gov.hmcts.ccd.domain.service.aggregated.SearchQueryOperation;
import uk.gov.hmcts.ccd.domain.service.common.ObjectMapperService;
import uk.gov.hmcts.ccd.endpoint.exceptions.BadSearchRequest;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ccd.domain.service.common.AccessControlService.CAN_READ;

@Service
@Slf4j
public class ElasticsearchQueryHelper {

    private static final String SORT = "sort";

    private final ObjectMapper objectMapper;
    private final ApplicationParams applicationParams;
    private final ObjectMapperService objectMapperService;
    private final SearchQueryOperation searchQueryOperation;
    private final GetCaseTypeOperation getCaseTypeOperation;
    private final UserService userService;

    @Autowired
    public ElasticsearchQueryHelper(@Qualifier("DefaultObjectMapper") ObjectMapper objectMapper,
                                    ApplicationParams applicationParams,
                                    ObjectMapperService objectMapperService,
                                    SearchQueryOperation searchQueryOperation,
                                    @Qualifier(AuthorisedGetCaseTypeOperation.QUALIFIER) GetCaseTypeOperation getCaseTypeOperation,
                                    UserService userService) {
        this.objectMapper = objectMapper;
        this.applicationParams = applicationParams;
        this.objectMapperService = objectMapperService;
        this.searchQueryOperation = searchQueryOperation;
        this.getCaseTypeOperation = getCaseTypeOperation;
        this.userService = userService;
    }

    public CrossCaseTypeSearchRequest prepareRequest(List<String> caseTypeIds, String useCaseString, String jsonSearchRequest) {
        UseCase useCase;
        try {
            useCase = UseCase.valueOfReference(useCaseString);
        } catch (IllegalArgumentException ex) {
            throw new BadSearchRequest(String.format("The provided use case '%s' is unsupported.", useCaseString));
        }

        rejectBlackListedQuery(jsonSearchRequest);

        JsonNode searchRequest = stringToJsonNode(jsonSearchRequest);
        if (useCase != UseCase.DEFAULT) {
            applyConfiguredSort(searchRequest, caseTypeIds, useCase);
        }

        return new CrossCaseTypeSearchRequest.Builder()
            .withCaseTypes(buildCaseTypeIds(caseTypeIds))
            .withSearchRequest(searchRequest)
            .build();
    }

    private void applyConfiguredSort(JsonNode searchRequest, List<String> caseTypeIds, UseCase useCase) {
        JsonNode sortNode = searchRequest.get(SORT);
        if (sortNode == null) {
            ArrayNode appliedSortsNode = buildSortNode(caseTypeIds, useCase);
            ((ObjectNode)searchRequest).set(SORT, appliedSortsNode);
        }
    }

    private ArrayNode buildSortNode(List<String> caseTypeIds, UseCase useCase) {
        ArrayNode sortNode = objectMapper.createArrayNode();
        caseTypeIds.forEach(caseTypeId -> addCaseTypeSorts(caseTypeId, useCase, sortNode));
        return sortNode;
    }

    private void addCaseTypeSorts(String caseTypeId, UseCase useCase, ArrayNode sortNode) {
        Optional<CaseTypeDefinition> caseTypeOpt = getCaseTypeOperation.execute(caseTypeId, CAN_READ);
        caseTypeOpt.ifPresent(caseType -> {
            searchQueryOperation.getSortOrders(caseType, useCase).forEach(field -> {
                ObjectNode sortOrderFieldNode = buildSortOrderFieldNode(field);
                sortNode.add(sortOrderFieldNode);
            });
        });
    }

    private ObjectNode buildSortOrderFieldNode(SortOrderField field) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        // TODO: Handle metadata sorts and fields without keyword
        // TODO: If alias, then use _keyword instead of .keyword
        // TODO: If text, add keyword, otherwise ignore
        objectNode.set("data." + field.getCaseFieldId() + ".keyword", new TextNode(field.getDirection()));
        return objectNode;
    }

    private JsonNode stringToJsonNode(String jsonSearchRequest) {
        return objectMapperService.convertStringToObject(jsonSearchRequest, JsonNode.class);
    }

    private void rejectBlackListedQuery(String jsonSearchRequest) {
        List<String> blackListedQueries = applicationParams.getSearchBlackList();
        Optional<String> blackListedQueryOpt = blackListedQueries
            .stream()
            .filter(blacklisted -> {
                Pattern p = Pattern.compile("\\b" + blacklisted + "\\b");
                Matcher m = p.matcher(jsonSearchRequest);
                return m.find();
            })
            .findFirst();
        blackListedQueryOpt.ifPresent(blacklisted -> {
            throw new BadSearchRequest(String.format("Query of type '%s' is not allowed", blacklisted));
        });
    }

    private List<String> buildCaseTypeIds(List<String> caseTypeIds) {
        return CollectionUtils.isEmpty(caseTypeIds)
            ? userService.getUserCaseTypes().stream().map(CaseTypeDefinition::getId).collect(Collectors.toList())
            : caseTypeIds;
    }
}
