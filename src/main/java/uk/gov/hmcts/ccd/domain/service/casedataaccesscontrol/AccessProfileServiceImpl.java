package uk.gov.hmcts.ccd.domain.service.casedataaccesscontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.AccessProfile;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignment;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignmentFilteringResult;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.definition.RoleToAccessProfileDefinition;
import uk.gov.hmcts.ccd.domain.service.AccessControl;

@Component
public class AccessProfileServiceImpl implements AccessProfileService, AccessControl {

    @Override
    public List<AccessProfile> generateAccessProfiles(RoleAssignmentFilteringResult filteringResults,
                                                      CaseTypeDefinition caseTypeDefinition) {

        List<AccessProfile> accessProfiles = new ArrayList<>();

        for (RoleAssignment roleAssignment : filteringResults.getRoleAssignments()) {

            RoleToAccessProfileDefinition roleToAccessProfileDefinition = caseTypeDefinition
                .getRoleToAccessProfileMapping(roleAssignment.getRoleName());

            if (roleToAccessProfileDefinition != null && !roleToAccessProfileDefinition.isDisabled()) {
                List<String> authorisations = roleToAccessProfileDefinition.getAuthorisationList();
                List<String> roleAssignmentAuthorisations = roleAssignment.getAuthorisations();

                if (authorisationsAllowMappingToAccessProfiles(authorisations, roleAssignmentAuthorisations)) {
                    accessProfiles.addAll(createAccessProfiles(roleAssignment, roleToAccessProfileDefinition));
                }
            }
        }
        return accessProfiles;
    }

    private boolean authorisationsAllowMappingToAccessProfiles(List<String> authorisations,
                                                               List<String> roleAssignmentAuthorisations) {
        if (roleAssignmentAuthorisations != null
            && authorisations.size() > 0) {
            Collection<String> filterAuthorisations = CollectionUtils
                .intersection(roleAssignmentAuthorisations, authorisations);

            return filterAuthorisations.size() > 0;
        }
        return authorisations.size() == 0;
    }

    private List<AccessProfile> createAccessProfiles(RoleAssignment roleAssignment,
                                                     RoleToAccessProfileDefinition roleToAccessProfileDefinition) {
        List<String> accessProfileList = roleToAccessProfileDefinition.getAccessProfileList();
        return accessProfileList
            .stream()
            .map(accessProfileValue -> {
                AccessProfile accessProfile = new AccessProfile();

                accessProfile.setReadOnly(roleToAccessProfileDefinition.isReadOnly()
                    || roleAssignment.getReadOnly());
                accessProfile.setClassification(roleAssignment.getClassification());
                accessProfile.setAccessProfile(accessProfileValue);
                return accessProfile;
            }).collect(Collectors.toList());
    }
}
