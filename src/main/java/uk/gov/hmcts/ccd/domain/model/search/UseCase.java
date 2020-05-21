package uk.gov.hmcts.ccd.domain.model.search;

import com.google.common.base.Strings;

public enum UseCase {

    WORKBASKET("WORKBASKET"),
    SEARCH("SEARCH"),
    ORG_CASES("ORGCASES"),
    DEFAULT("");

    private String reference;

    UseCase(String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    public static UseCase valueOfReference(String reference) {
        if (Strings.isNullOrEmpty(reference)) {
            return DEFAULT;
        }
        return valueOf(reference);
    }
}
