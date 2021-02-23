package uk.gov.hmcts.ccd.domain.service.accessprofile.filter.matcher;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignment;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignmentFilteringResult;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleMatchingResult;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;

@Slf4j
@Component
public class BeginDateEndDateMatcher implements AttributeMatcher {

    @Override
    public boolean matchAttribute(RoleAssignmentFilteringResult result, CaseDetails caseDetails) {
        RoleAssignment roleAssignment = result.getRoleAssignment();
        log.debug("Apply filter on start {} and end time {} for role assignment {}",
            roleAssignment.getBeginTime(),
            roleAssignment.getEndTime(),
            roleAssignment.getId());
        RoleMatchingResult matchingResult = result.getRoleMatchingResult();
        if (roleAssignment.getBeginTime() != null && roleAssignment.getEndTime() != null) {
            Instant now = Instant.now();
            matchingResult.setValidDate(roleAssignment.getBeginTime().compareTo(now) < 0
                && roleAssignment.getEndTime().compareTo(now) > 0);
        }
        return matchingResult.isValidDate();
    }
}
