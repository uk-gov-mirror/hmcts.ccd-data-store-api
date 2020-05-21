package uk.gov.hmcts.ccd.domain.service.aggregated;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.ccd.data.user.CachedUserRepository;
import uk.gov.hmcts.ccd.data.user.UserRepository;
import uk.gov.hmcts.ccd.domain.model.aggregated.CommonField;
import uk.gov.hmcts.ccd.domain.model.definition.*;
import uk.gov.hmcts.ccd.domain.model.search.CaseSearchResult;
import uk.gov.hmcts.ccd.domain.model.search.UseCase;
import uk.gov.hmcts.ccd.domain.model.search.elasticsearch.*;
import uk.gov.hmcts.ccd.domain.service.search.elasticsearch.CrossCaseTypeSearchRequest;
import uk.gov.hmcts.ccd.endpoint.exceptions.BadRequestException;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static java.lang.String.format;
import static uk.gov.hmcts.ccd.domain.model.definition.FieldTypeDefinition.LABEL;
import static uk.gov.hmcts.ccd.domain.service.common.AccessControlService.CAN_READ;

@Named
@Singleton
public class MergeDataToCaseSearchOperation {

    private static final String NESTED_ELEMENT_NOT_FOUND_FOR_PATH = "Nested element not found for path %s";

    private final UserRepository userRepository;
    private final GetCaseTypeOperation getCaseTypeOperation;
    private final SearchQueryOperation searchQueryOperation;

    public MergeDataToCaseSearchOperation(@Qualifier(CachedUserRepository.QUALIFIER) final UserRepository userRepository,
                                          @Qualifier(AuthorisedGetCaseTypeOperation.QUALIFIER) final GetCaseTypeOperation getCaseTypeOperation,
                                          final SearchQueryOperation searchQueryOperation) {
        this.userRepository = userRepository;
        this.getCaseTypeOperation = getCaseTypeOperation;
        this.searchQueryOperation = searchQueryOperation;
    }

    public UICaseSearchResult execute(final CrossCaseTypeSearchRequest searchRequest,
                                      final CaseSearchResult caseSearchResult,
                                      final UseCase useCase) {
        UICaseSearchResult uiCaseSearchResult = new UICaseSearchResult(
            buildHeaders(searchRequest, useCase, caseSearchResult),
            buildItems(useCase, caseSearchResult),
            caseSearchResult.getTotal()
        );

        if (useCase != UseCase.DEFAULT) {
            // Return appropriate use case fields
        } else {
            // Default use case - all case fields with permissions
        }

        return uiCaseSearchResult;
    }

    private List<SearchResultViewItem> buildItems(UseCase useCase, CaseSearchResult caseSearchResult) {
        List<SearchResultViewItem> items = new ArrayList<>();
        caseSearchResult.getCases().forEach(caseDetails -> {
            Optional<CaseTypeDefinition> caseType = getCaseTypeOperation.execute(caseDetails.getCaseTypeId(), CAN_READ);
            if (!caseType.isPresent()) {
                // TODO: Handle
            }

            // TODO: Handle no searchType
            final SearchResult searchResult = searchQueryOperation.getSearchResultDefinition(caseType.get(), useCase);

            items.add(buildSearchResultViewItem(caseDetails, caseType.get(), searchResult));
        });

        return items;
    }

    private List<UICaseSearchHeader> buildHeaders(CrossCaseTypeSearchRequest request, UseCase useCase, CaseSearchResult caseSearchResult) {
        List<UICaseSearchHeader> headers = new ArrayList<>();
        request.getCaseTypeIds().forEach(caseTypeId -> {
            Optional<CaseTypeDefinition> caseTypeOpt = getCaseTypeOperation.execute(caseTypeId, CAN_READ);
            caseTypeOpt.ifPresent(caseType -> {
                // TODO: Handle no searchType
                final SearchResult searchResult = searchQueryOperation.getSearchResultDefinition(caseType, useCase);

                UICaseSearchHeader caseSearchHeader = new UICaseSearchHeader(
                    new UICaseSearchHeaderMetadata(caseType.getJurisdictionId(), caseTypeId),
                    buildSearchResultViewColumn(caseType, searchResult),
                    caseSearchResult.buildCaseReferenceList(caseTypeId)
                );
                headers.add(caseSearchHeader);
            });
        });

        return headers;
    }

    public List<SearchResultViewColumn> buildSearchResultViewColumn(CaseTypeDefinition caseTypeDefinition,
                                                                    SearchResult searchResult) {
        final HashSet<String> addedFields = new HashSet<>();

        return Arrays.stream(searchResult.getFields())
            .flatMap(searchResultField -> caseTypeDefinition.getCaseFieldDefinitions().stream()
                .filter(caseField -> caseField.getId().equals(searchResultField.getCaseFieldId()))
                .filter(caseField -> filterDistinctFieldsByRole(addedFields, searchResultField))
                .map(caseField -> createSearchResultViewColumn(searchResultField, caseField))
            )
            .collect(Collectors.toList());
    }

    private SearchResultViewColumn createSearchResultViewColumn(final SearchResultField searchResultField, final CaseFieldDefinition caseFieldDefinition) {
        CommonField commonField = commonField(searchResultField, caseFieldDefinition);
        return new SearchResultViewColumn(
            searchResultField.buildCaseFieldId(),
            commonField.getFieldTypeDefinition(),
            searchResultField.getLabel(),
            searchResultField.getDisplayOrder(),
            searchResultField.isMetadata(),
            displayContextParameter(searchResultField, commonField));
    }

    private boolean filterDistinctFieldsByRole(final HashSet<String> addedFields, final SearchResultField resultField) {
        String id = resultField.buildCaseFieldId();
        if (addedFields.contains(id)) {
            return false;
        } else {
            if (StringUtils.isEmpty(resultField.getRole()) || userRepository.getUserRoles().contains(resultField.getRole())) {
                addedFields.add(id);
                return true;
            } else {
                return false;
            }
        }
    }



    private CommonField commonField(SearchResultField searchResultField, CaseFieldDefinition caseFieldDefinition) {
        return caseFieldDefinition.getComplexFieldNestedField(searchResultField.getCaseFieldPath())
            .orElseThrow(() ->
                new BadRequestException(format("CaseField %s has no nested elements with code %s.",
                    caseFieldDefinition.getId(), searchResultField.getCaseFieldPath())));
    }

    private String displayContextParameter(SearchResultField searchResultField, CommonField commonField) {
        return searchResultField.getDisplayContextParameter() == null
            ? commonField.getDisplayContextParameter()
            : searchResultField.getDisplayContextParameter();
    }

    public SearchResultViewItem buildSearchResultViewItem(final CaseDetails caseDetails,
                                                          final CaseTypeDefinition caseTypeDefinition,
                                                          final SearchResult searchResult) {

        Map<String, JsonNode> caseData = new HashMap<>(caseDetails.getData());
        Map<String, Object> caseMetadata = new HashMap<>(caseDetails.getMetadata());
        Map<String, TextNode> labels = getLabelsFromCaseFields(caseTypeDefinition);
        Map<String, Object> caseFields = prepareData(searchResult, caseData, caseMetadata, labels);

        String caseId = caseDetails.hasCaseReference() ? caseDetails.getReferenceAsString() : caseDetails.getId();
        return new SearchResultViewItem(caseId, caseFields, new HashMap<>(caseFields));
    }

    private Map<String, Object> prepareData(SearchResult searchResult,
                                            Map<String, JsonNode> caseData,
                                            Map<String, Object> metadata,
                                            Map<String, TextNode> labels) {

        Map<String, Object> newResults = new HashMap<>();

        searchResult.getFieldsWithPaths().forEach(searchResultField -> {
            JsonNode jsonNode = caseData.get(searchResultField.getCaseFieldId());
            if (jsonNode != null) {
                newResults.put(searchResultField.getCaseFieldId() + "." + searchResultField.getCaseFieldPath(),
                    getObjectByPath(searchResultField, jsonNode));
            }
        });

        newResults.putAll(caseData);
        newResults.putAll(labels);
        newResults.putAll(metadata);

        return newResults;
    }

    private Object getObjectByPath(SearchResultField searchResultField, JsonNode value) {

        List<String> pathElements = searchResultField.getCaseFieldPathElements();

        return reduce(value, pathElements, searchResultField.getCaseFieldPath());
    }

    private Object reduce(JsonNode caseFields, List<String> pathElements, String path) {
        String firstPathElement = pathElements.get(0);

        JsonNode caseField = Optional.ofNullable(caseFields.get(firstPathElement))
            .orElseThrow(() -> new BadRequestException(format(NESTED_ELEMENT_NOT_FOUND_FOR_PATH, path)));

        if (pathElements.size() == 1) {
            return caseField;
        } else {
            List<String> tail = pathElements.subList(1, pathElements.size());
            return reduce(caseField, tail, path);
        }
    }

    private Map<String, TextNode> getLabelsFromCaseFields(CaseTypeDefinition caseTypeDefinition) {
        return caseTypeDefinition.getCaseFieldDefinitions()
            .stream()
            .filter(caseField -> LABEL.equals(caseField.getFieldTypeDefinition().getType()))
            .collect(Collectors.toMap(CaseFieldDefinition::getId, caseField -> instance.textNode(caseField.getLabel())));
    }
}
