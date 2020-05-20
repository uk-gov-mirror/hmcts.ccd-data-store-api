package uk.gov.hmcts.ccd.domain.model.search;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class UICaseSearchHeader {

    @NonNull
    private UICaseSearchHeaderMetadata metadata;
    @NonNull
    private List<SearchResultViewColumn> fields;
    @NonNull
    private List<Long> cases;
}
