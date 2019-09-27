package uk.gov.hmcts.ccd.v2.external.controller;

import uk.gov.hmcts.ccd.data.casedetails.search.FieldMapSanitizeOperation;
import uk.gov.hmcts.ccd.domain.service.search.PaginatedSearchMetaDataOperation;

public class PaginationController {

    private final FieldMapSanitizeOperation fieldMapSanitizeOperation;
    private final PaginatedSearchMetaDataOperation paginatedSearchMetaDataOperation;

    public PaginationController(final FieldMapSanitizeOperation fieldMapSanitizeOperation, final PaginatedSearchMetaDataOperation paginatedSearchMetaDataOperation) {
        this.fieldMapSanitizeOperation = fieldMapSanitizeOperation;
        this.paginatedSearchMetaDataOperation = paginatedSearchMetaDataOperation;
    }


}
