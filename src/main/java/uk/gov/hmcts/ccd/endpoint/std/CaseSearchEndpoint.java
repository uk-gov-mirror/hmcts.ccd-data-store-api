package uk.gov.hmcts.ccd.endpoint.std;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.domain.model.search.CaseSearchResult;
import uk.gov.hmcts.ccd.domain.service.search.elasticsearch.*;
import uk.gov.hmcts.ccd.domain.service.search.elasticsearch.security.AuthorisedCaseSearchOperation;

@RestController
@RequestMapping(path = "/",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = {"Elastic Based Search API"})
@SwaggerDefinition(tags = {
    @Tag(name = "Elastic Based Search API", description = "New ElasticSearch based search API")
})
@Slf4j
public class CaseSearchEndpoint {

    private final CaseSearchOperation caseSearchOperation;
    private final ElasticsearchCaseSearchOperation elasticsearchCaseSearchOperation;

    @Autowired
    public CaseSearchEndpoint(@Qualifier(AuthorisedCaseSearchOperation.QUALIFIER) CaseSearchOperation caseSearchOperation,
                              ElasticsearchCaseSearchOperation elasticsearchCaseSearchOperation) {
        this.caseSearchOperation = caseSearchOperation;
        this.elasticsearchCaseSearchOperation = elasticsearchCaseSearchOperation;
    }

    @PostMapping(value = "/searchCases")
    @ApiOperation("Search cases according to the provided ElasticSearch query. Supports searching across multiple case types.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of case data for the given search request")
    })
    public CaseSearchResult searchCases(
        @ApiParam(value = "Case type ID(s)", required = true)
        @RequestParam("ctid") List<String> caseTypeIds,
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

        CaseSearchResult result = caseSearchOperation.execute(request);

        Duration between = Duration.between(start, Instant.now());
        log.debug("searchCases execution completed in {} millisecs...", between.toMillis());

        return result;
    }
}
