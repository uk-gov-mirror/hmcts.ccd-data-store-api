package uk.gov.hmcts.ccd.data.draft;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.ccd.WireMockBaseTest;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.DraftResponseToCaseDetailsBuilder;
import uk.gov.hmcts.ccd.domain.model.draft.DraftResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.domain.service.common.TestBuildersUtil.CaseDetailsBuilder.newCaseDetails;
import static uk.gov.hmcts.ccd.domain.service.common.TestBuildersUtil.DraftResponseBuilder.newDraftResponse;

public class DraftGatewayIT extends WireMockBaseTest {

    @MockBean
    private DefaultDraftGateway draftGateway;

    @MockBean
    private DraftResponseToCaseDetailsBuilder draftResponseToCaseDetailsBuilder;

    @Autowired
    private CachedDraftGateway cachedDraftGateway;

    private final String draftId = "1";
    private final DraftResponse draftResponse = newDraftResponse().build();
    private final CaseDetails caseDetails = newCaseDetails().build();

    @Before
    public void setUp() {
        doReturn(draftResponse).when(draftGateway).get(draftId);
        doReturn(caseDetails).when(draftResponseToCaseDetailsBuilder).build(draftResponse);
    }

    @Test
    public void shouldCacheDraftsForSubsequentCalls() {
        DraftResponse result = cachedDraftGateway.get(draftId);

        assertAll(
            () -> assertThat(result.toString(), is(draftResponse.toString())),
            () -> verify(draftGateway, times(1)).get(draftId)
        );

        DraftResponse result1 = cachedDraftGateway.get(draftId);

        assertAll(
            () -> assertThat(result1.toString(), is(draftResponse.toString())),
            () -> verifyNoMoreInteractions(draftGateway)
        );
    }

    @Test
    public void shouldCacheDraftsCaseDetailsForSubsequentCalls() {
        CaseDetails result = cachedDraftGateway.getCaseDetails(draftId);

        assertAll(
            () -> assertThat(result.toString(), is(caseDetails.toString())),
            () -> verify(draftResponseToCaseDetailsBuilder, times(1)).build(draftResponse),
            () -> verify(draftGateway, times(1)).get(draftId)
        );

        CaseDetails result1 = cachedDraftGateway.getCaseDetails(draftId);

        assertAll(
            () -> assertThat(result1.toString(), is(caseDetails.toString())),
            () -> verify(draftResponseToCaseDetailsBuilder, times(1)).build(draftResponse),
            () -> verifyNoMoreInteractions(draftGateway)
        );
    }

    @Test
    public void shouldRetrieveDraftFromGetCache_InternalCallFromCaseDetails() {
        cachedDraftGateway.get(draftId);
        verify(draftGateway, times(1)).get(draftId);

        cachedDraftGateway.getCaseDetails(draftId);
        verifyNoMoreInteractions(draftGateway);
    }

    @Test
    public void shouldRetrieveDraftFromDecorated() {
        CaseDetails result = cachedDraftGateway.getCaseDetails(draftId);

        InOrder inOrder = inOrder(draftGateway, draftResponseToCaseDetailsBuilder);

        assertAll(
            () -> assertThat(result.toString(), is(caseDetails.toString())),
            () -> inOrder.verify(draftGateway).get(draftId),
            () -> inOrder.verify(draftResponseToCaseDetailsBuilder).build(draftResponse),
            inOrder::verifyNoMoreInteractions
        );
    }

}
