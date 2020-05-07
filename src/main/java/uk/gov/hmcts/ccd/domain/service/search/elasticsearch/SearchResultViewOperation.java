package uk.gov.hmcts.ccd.domain.service.search.elasticsearch;

import uk.gov.hmcts.ccd.domain.model.search.*;

import java.util.*;


public interface SearchResultViewOperation {

    SearchResultView executeAndConvert(CaseSearchResult request, List<String> caseTypeIds, String searchType);

}
