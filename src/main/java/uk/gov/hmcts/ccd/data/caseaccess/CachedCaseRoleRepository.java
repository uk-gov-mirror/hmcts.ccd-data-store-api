package uk.gov.hmcts.ccd.data.caseaccess;

import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;

@Service
@Singleton
@Qualifier(CachedCaseRoleRepository.QUALIFIER)
public class CachedCaseRoleRepository implements CaseRoleRepository {

    public static final String QUALIFIER = "cached";

    private final CaseRoleRepository caseRoleRepository;

    public CachedCaseRoleRepository(@Qualifier(DefaultCaseRoleRepository.QUALIFIER)
                                    final CaseRoleRepository caseRoleRepository) {
        this.caseRoleRepository = caseRoleRepository;
    }

    @Override
    @Cacheable("caseRolesForCaseTypeCache")
    public Set<String> getCaseRoles(String caseTypeId) {
        return caseRoleRepository.getCaseRoles(caseTypeId);
    }
}
