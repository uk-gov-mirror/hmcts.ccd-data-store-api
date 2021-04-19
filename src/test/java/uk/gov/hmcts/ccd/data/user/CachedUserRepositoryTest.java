package uk.gov.hmcts.ccd.data.user;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.data.casedetails.SecurityClassification.PRIVATE;
import static uk.gov.hmcts.ccd.data.casedetails.SecurityClassification.PUBLIC;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.data.casedetails.SecurityClassification;
import uk.gov.hmcts.ccd.domain.model.aggregated.IdamProperties;
import uk.gov.hmcts.ccd.domain.model.aggregated.IdamUser;

class CachedUserRepositoryTest {

    private static final String JURISDICTION_ID = "DIVORCE";
    @Mock
    private uk.gov.hmcts.ccd.data.user.UserRepository userRepository;
    private uk.gov.hmcts.ccd.data.user.CachedUserRepository cachedUserRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        cachedUserRepository = new uk.gov.hmcts.ccd.data.user.CachedUserRepository(userRepository);
    }

    @Nested
    @DisplayName("getUserId()")
    class GetUserId {

        @Test
        @DisplayName("should initially retrieve user name from decorated repository")
        void shouldRetrieveUserNameFromDecorated() {
            String expectedUserName = "26";
            doReturn(expectedUserName).when(userRepository).getUserId();

            String userName = cachedUserRepository.getUserId();

            assertAll(
                () -> assertThat(userName, is(expectedUserName)),
                () -> verify(userRepository, times(1)).getUserId()
            );
        }

        @Test
        @DisplayName("should cache user name for subsequent calls")
        void shouldCacheUserNameForSubsequentCalls() {
            String expectedUserName = "26";
            doReturn(expectedUserName).when(userRepository).getUserId();

            cachedUserRepository.getUserId();

            verify(userRepository, times(1)).getUserId();

            String userName = cachedUserRepository.getUserId();

            assertAll(
                () -> assertThat(userName, is(expectedUserName)),
                () -> verifyNoMoreInteractions(userRepository)
            );
        }
    }

    @Nested
    @DisplayName("getUserDetails()")
    class GetUserDetails {

        @Test
        @DisplayName("should initially retrieve user details from decorated repository")
        void shouldRetrieveUserDetailsFromDecorated() {
            final IdamProperties expectedUserDetails = new IdamProperties();
            doReturn(expectedUserDetails).when(userRepository).getUserDetails();

            final IdamProperties userDetails = cachedUserRepository.getUserDetails();

            assertAll(
                () -> assertThat(userDetails, is(expectedUserDetails)),
                () -> verify(userRepository, times(1)).getUserDetails()
            );
        }
    }

    @Nested
    @DisplayName("getUserRoles()")
    class GetUserRoles {

        @Test
        @DisplayName("should initially retrieve user roles from decorated repository")
        void shouldRetrieveUserRolesFromDecorated() {
            final HashSet<String> expectedUserRoles = Sets.newHashSet("role1", "role2");
            doReturn(expectedUserRoles).when(userRepository).getUserRoles();

            final Set<String> userRoles = cachedUserRepository.getUserRoles();

            assertAll(
                () -> assertThat(userRoles, is(expectedUserRoles)),
                () -> verify(userRepository, times(1)).getUserRoles()
            );
        }
    }


    @Nested
    @DisplayName("getUserClassifications()")
    class GetUserClassifications {

        @Test
        @DisplayName("should initially retrieve classifications from decorated repository")
        void shouldRetrieveClassificationsFromDecorated() {
            final HashSet<SecurityClassification> expectedClassifications = Sets.newHashSet(PUBLIC, PRIVATE);
            doReturn(expectedClassifications).when(userRepository).getUserClassifications(JURISDICTION_ID);

            final Set<SecurityClassification> classifications = cachedUserRepository.getUserClassifications(
                JURISDICTION_ID);

            assertAll(
                () -> assertThat(classifications, is(expectedClassifications)),
                () -> verify(userRepository, times(1)).getUserClassifications(JURISDICTION_ID)
            );
        }
    }

    @Nested
    @DisplayName("getUser()")
    class GetUser {

        @Test
        @DisplayName("should retrieve user from decorated repository")
        void shouldRetrieveUserFromDecorated() {
            IdamUser idamUser = new IdamUser();
            when(userRepository.getUser()).thenReturn(idamUser);

            IdamUser returnedIdamUser = cachedUserRepository.getUser();

            assertAll(
                () -> assertThat(returnedIdamUser, is(idamUser)),
                () -> verify(userRepository).getUser()
            );
        }

    }
}
