package uk.gov.hmcts.ccd.domain.service.aggregated;

import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.search.*;
import uk.gov.hmcts.ccd.domain.service.search.elasticsearch.CrossCaseTypeSearchRequest;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.ccd.domain.service.common.AccessControlService.CAN_READ;

@Named
@Singleton
public class MergeDataToCaseSearchOperation {

    private final MergeDataToSearchResultOperation mergeDataToSearchResultOperation;
    private final GetCaseTypeOperation getCaseTypeOperation;
    private final SearchQueryOperation searchQueryOperation;

    public MergeDataToCaseSearchOperation(final MergeDataToSearchResultOperation mergeDataToSearchResultOperation,
                                          @Qualifier(AuthorisedGetCaseTypeOperation.QUALIFIER) final GetCaseTypeOperation getCaseTypeOperation,
                                          final SearchQueryOperation searchQueryOperation) {
        this.mergeDataToSearchResultOperation = mergeDataToSearchResultOperation;
        this.getCaseTypeOperation = getCaseTypeOperation;
        this.searchQueryOperation = searchQueryOperation;
    }

    public UICaseSearchResult execute(final CrossCaseTypeSearchRequest searchRequest,
                                      final CaseSearchResult caseSearchResult,
                                      final String searchType) {
        UICaseSearchResult uiCaseSearchResult = new UICaseSearchResult(
            buildHeaders(searchRequest, searchType, caseSearchResult),
            buildItems(searchType, caseSearchResult),
            caseSearchResult.getTotal()
        );

        if (!Strings.isNullOrEmpty(searchType)) {
            // Return appropriate use case fields
        } else {
            // Default use case
        }

        return uiCaseSearchResult;
    }

    private List<SearchResultViewItem> buildItems(String searchType, CaseSearchResult caseSearchResult) {
        List<SearchResultViewItem> items = new ArrayList<>();
        caseSearchResult.getCases().forEach(caseDetails -> {
            Optional<CaseTypeDefinition> caseType = getCaseTypeOperation.execute(caseDetails.getCaseTypeId(), CAN_READ);
            if (!caseType.isPresent()) {
                // TODO: Handle
            }

            // TODO: Handle no searchType
            final uk.gov.hmcts.ccd.domain.model.definition.SearchResult searchResult =
                searchQueryOperation.getSearchResultDefinition(caseType.get(), searchType);

            items.add(mergeDataToSearchResultOperation.buildSearchResultViewItem(caseDetails, caseType.get(), searchResult));
        });

        return items;
    }

    private List<UICaseSearchHeader> buildHeaders(CrossCaseTypeSearchRequest request, String searchType, CaseSearchResult caseSearchResult) {
        List<UICaseSearchHeader> headers = new ArrayList<>();
        request.getCaseTypeIds().forEach(caseTypeId -> {
            Optional<CaseTypeDefinition> caseType = getCaseTypeOperation.execute(caseTypeId, CAN_READ);
            if (!caseType.isPresent()) {
                // TODO: Handle
            }

            // TODO: Handle no searchType
            final uk.gov.hmcts.ccd.domain.model.definition.SearchResult searchResult =
                searchQueryOperation.getSearchResultDefinition(caseType.get(), searchType);

            UICaseSearchHeader caseSearchHeader = new UICaseSearchHeader(
                new UICaseSearchHeaderMetadata(caseType.get().getJurisdictionId(), caseTypeId),
                mergeDataToSearchResultOperation.buildSearchResultViewColumn(caseType.get(), searchResult),
                caseSearchResult.buildCaseReferenceList(caseTypeId)
            );
            headers.add(caseSearchHeader);
        });

        return headers;
    }
}
