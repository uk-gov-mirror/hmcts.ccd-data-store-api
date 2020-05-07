package uk.gov.hmcts.ccd.v2.internal.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.ccd.domain.model.search.*;
import uk.gov.hmcts.ccd.domain.service.aggregated.*;
import uk.gov.hmcts.ccd.domain.service.common.*;
import uk.gov.hmcts.ccd.domain.service.search.elasticsearch.*;
import uk.gov.hmcts.ccd.domain.service.search.elasticsearch.security.*;
import uk.gov.hmcts.ccd.endpoint.std.*;
import uk.gov.hmcts.ccd.v2.*;
import uk.gov.hmcts.ccd.v2.internal.resource.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping(path = "/internal/cases/search")
@Slf4j
public class UICaseSearchController {
    private static final String ERROR_CASE_ID_INVALID = "Case ID is not valid";

    private final CaseSearchOperation caseSearchOperation;
    private final ElasticsearchCaseSearchOperation elasticsearchCaseSearchOperation;
    private final SearchResultViewOperation searchResultViewOperation;

    @Autowired
    public UICaseSearchController(
        @Qualifier(AuthorisedCaseSearchOperation.QUALIFIER) CaseSearchOperation caseSearchOperation,
        SearchResultViewOperation searchResultViewOperation,
        ElasticsearchCaseSearchOperation elasticsearchCaseSearchOperation) {
        this.caseSearchOperation = caseSearchOperation;
        this.elasticsearchCaseSearchOperation = elasticsearchCaseSearchOperation;
        this.searchResultViewOperation = searchResultViewOperation;
    }

    @PostMapping(
        path = "/cases",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.CASE_SEARCH
        }
    )
    @ApiOperation(
        value = "Elastic search for cases returning paginated data",
        notes = V2.EXPERIMENTAL_WARNING
    )
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Success",
            response = CaseViewResource.class
        ),
        @ApiResponse(
            code = 400,
            message = ERROR_CASE_ID_INVALID
        ),
        @ApiResponse(
            code = 404,
            message = "Case not found"
        )
    })


    public SearchResultView getCases(@ApiParam(value = "Case type ID(s)", required = true)
                                     @RequestParam("ctid") List<String> caseTypeIds,
                                     @RequestParam("searchType") final String searchType,
                                     @ApiParam(value = "Native ElasticSearch Search API request. Please refer to the ElasticSearch official "
                                         + "documentation. For cross case type search, "
                                         + "the search results will contain only metadata by default (no case field data). To get case data in the "
                                         + "search results, please state the alias fields to be returned in the _source property for e.g."
                                         + " \"_source\":[\"alias.customer\",\"alias.postcode\"]",
                                         required = true)
                                     @RequestBody String jsonSearchRequest) {


        Instant start = Instant.now();
        elasticsearchCaseSearchOperation.rejectBlackListedQuery(jsonSearchRequest);

        CrossCaseTypeSearchRequest request = new CrossCaseTypeSearchRequest.Builder()
            .withCaseTypes(caseTypeIds)
            .withSearchRequest(elasticsearchCaseSearchOperation.stringToJsonNode(jsonSearchRequest))
            .build();

        CaseSearchResult caseSearchResult = caseSearchOperation.execute(request);

        SearchResultView result = searchResultViewOperation.executeAndConvert(caseSearchResult, caseTypeIds, searchType);

        Duration between = Duration.between(start, Instant.now());
        log.debug("searchCases execution completed in {} millisecs...", between.toMillis());

        return result;
    }
}
