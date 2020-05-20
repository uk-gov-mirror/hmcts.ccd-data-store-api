package uk.gov.hmcts.ccd.domain.model.search;

import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Data
public class UICaseSearchResult {

    public static final UICaseSearchResult EMPTY = new UICaseSearchResult(emptyList(), emptyList(), 0L);

    @NonNull
    private List<UICaseSearchHeader> headers;
    @NonNull
    private List<SearchResultViewItem> cases;
    @NonNull
    private Long total;

    public Optional<UICaseSearchHeader> findHeaderByCaseType(String caseTypeId) {
        return headers.stream().filter(header -> header.getMetadata().getCaseTypeId().equals(caseTypeId)).findFirst();
    }
}
