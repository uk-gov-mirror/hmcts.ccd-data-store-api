package uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol;

import java.util.List;

public class AccessProfile {

    private Boolean readOnly;

    private String classification;

    private List<String> accessProfiles;

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public List<String> getAccessProfiles() {
        return accessProfiles;
    }

    public void setAccessProfiles(List<String> accessProfiles) {
        this.accessProfiles = accessProfiles;
    }
}