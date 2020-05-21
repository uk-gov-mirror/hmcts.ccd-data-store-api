package uk.gov.hmcts.ccd.domain.service.search.elasticsearch;

import uk.gov.hmcts.ccd.domain.model.search.*;
import uk.gov.hmcts.ccd.domain.model.search.elasticsearch.UICaseSearchResult;


public interface CaseSearchOperation {

    CaseSearchResult execute(CrossCaseTypeSearchRequest request);

    UICaseSearchResult execute(CrossCaseTypeSearchRequest searchRequest,
                               CaseSearchResult caseSearchResult,
                               UseCase useCase);
}
