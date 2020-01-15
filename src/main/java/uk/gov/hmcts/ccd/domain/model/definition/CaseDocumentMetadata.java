package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import uk.gov.hmcts.ccd.data.casedetails.SecurityClassification;

import javax.print.Doc;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ToString
public class CaseDocumentMetadata implements Serializable {

    private String caseId;
    private String caseTypeId;
    private String jurisdictionId;
    private String eventId;
    private List<Document> documentList;

}
