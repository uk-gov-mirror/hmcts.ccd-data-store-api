package uk.gov.hmcts.ccd.domain.service.accessprofile.filter;

import java.util.List;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignmentFilteringResult;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignments;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.std.CaseDataContent;

public interface RoleAssignmentsFilteringService {

    List<RoleAssignmentFilteringResult> filter(RoleAssignments roleAssignments, CaseDetails caseDetails);

    List<RoleAssignmentFilteringResult> filter(RoleAssignments roleAssignments, CaseDataContent caseDataContent);

    List<RoleAssignmentFilteringResult> filter(RoleAssignments roleAssignments, CaseTypeDefinition caseTypeDefinition);
}
