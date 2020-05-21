package uk.gov.hmcts.ccd.domain.service.search.elasticsearch;

import uk.gov.hmcts.ccd.domain.model.search.*;
import uk.gov.hmcts.ccd.domain.model.search.elasticsearch.UICaseSearchResult;

import java.util.List;


public interface CaseSearchOperation {

    CaseSearchResult execute(CrossCaseTypeSearchRequest request);

    UICaseSearchResult execute(CaseSearchResult caseSearchResult,
                               List<String> caseTypeIds,
                               UseCase useCase);
}
