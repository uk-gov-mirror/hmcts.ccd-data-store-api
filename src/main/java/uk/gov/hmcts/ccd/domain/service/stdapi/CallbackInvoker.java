package uk.gov.hmcts.ccd.domain.service.stdapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.domain.model.callbacks.AfterSubmitCallbackResponse;
import uk.gov.hmcts.ccd.domain.model.callbacks.CallbackResponse;
import uk.gov.hmcts.ccd.domain.model.callbacks.SignificantItemType;
import uk.gov.hmcts.ccd.domain.model.callbacks.SignificantItem;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseEvent;
import uk.gov.hmcts.ccd.domain.model.definition.CaseType;
import uk.gov.hmcts.ccd.domain.service.callbacks.CallbackService;
import uk.gov.hmcts.ccd.domain.service.common.CaseDataService;
import uk.gov.hmcts.ccd.domain.service.common.CaseTypeService;
import uk.gov.hmcts.ccd.domain.service.common.SecurityValidationService;
import uk.gov.hmcts.ccd.domain.types.sanitiser.CaseSanitiser;
import java.util.*;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.ofNullable;

@Service
public class CallbackInvoker {

    private static final String CALLBACK_RESPONSE_KEY_STATE = "state";
    private static final HashMap<String, JsonNode> EMPTY_DATA_CLASSIFICATION = Maps.newHashMap();
    private final CallbackService callbackService;
    private final CaseTypeService caseTypeService;
    private final CaseDataService caseDataService;
    private final CaseSanitiser caseSanitiser;
    private final SecurityValidationService securityValidationService;
    private static final UrlValidator URL_VALIDATOR = UrlValidator.getInstance();
    private static final int MIN_LENGTH_OF_DESCRIPTION = 0;
    private static final int MAX_LENGTH_OF_DESCRIPTION = 65;


    @Autowired
    public CallbackInvoker(final CallbackService callbackService,
                           final CaseTypeService caseTypeService,
                           final CaseDataService caseDataService,
                           final CaseSanitiser caseSanitiser,
                           final SecurityValidationService securityValidationService) {
        this.callbackService = callbackService;
        this.caseTypeService = caseTypeService;
        this.caseDataService = caseDataService;
        this.caseSanitiser = caseSanitiser;
        this.securityValidationService = securityValidationService;
    }

    public void invokeAboutToStartCallback(final CaseEvent caseEvent,
                                           final CaseType caseType,
                                           final CaseDetails caseDetails,
                                           final Boolean ignoreWarning) {
        final Optional<CallbackResponse> callbackResponse = callbackService.send(
            caseEvent.getCallBackURLAboutToStartEvent(),
            caseEvent.getRetriesTimeoutAboutToStartEvent(),
            caseEvent, caseDetails);

        callbackResponse.ifPresent(response -> validateAndSetFromAboutToStartCallback(caseType,
                                                                                      caseDetails,
                                                                                      ignoreWarning,
                                                                                      response));
    }

    public AboutToSubmitCallbackResponse invokeAboutToSubmitCallback(final CaseEvent eventTrigger,
                                                                     final CaseDetails caseDetailsBefore,
                                                                     final CaseDetails caseDetails,
                                                                     final CaseType caseType,
                                                                     final Boolean ignoreWarning) {

        final Optional<CallbackResponse> callbackResponse = callbackService.send(
            eventTrigger.getCallBackURLAboutToSubmitEvent(),
            eventTrigger.getRetriesTimeoutURLAboutToSubmitEvent(),
            eventTrigger, caseDetailsBefore, caseDetails);

        if (callbackResponse.isPresent()) {
            return validateAndSetFromAboutToSubmitCallback(caseType,
                                                           caseDetails,
                                                           ignoreWarning,
                                                           callbackResponse.get());
        }

        return new AboutToSubmitCallbackResponse();
    }

    public ResponseEntity<AfterSubmitCallbackResponse> invokeSubmittedCallback(final CaseEvent eventTrigger,
                                                                               final CaseDetails caseDetailsBefore,
                                                                               final CaseDetails caseDetails) {
        return callbackService.send(eventTrigger.getCallBackURLSubmittedEvent(),
                                    eventTrigger.getRetriesTimeoutURLSubmittedEvent(),
                                    eventTrigger,
                                    caseDetailsBefore,
                                    caseDetails,
                                    AfterSubmitCallbackResponse.class);
    }

    private void validateAndSetFromAboutToStartCallback(CaseType caseType,
                                                        CaseDetails caseDetails,
                                                        Boolean ignoreWarning,
                                                        CallbackResponse callbackResponse) {
        callbackService.validateCallbackErrorsAndWarnings(callbackResponse, ignoreWarning);

        if (callbackResponse.getData() != null) {
            validateAndSetData(caseType, caseDetails, callbackResponse.getData());
        }
    }

    private AboutToSubmitCallbackResponse validateAndSetFromAboutToSubmitCallback(final CaseType caseType,
                                                                                  final CaseDetails caseDetails,
                                                                                  final Boolean ignoreWarning,
                                                                                  final CallbackResponse callbackResponse) {

        final AboutToSubmitCallbackResponse aboutToSubmitCallbackResponse = new AboutToSubmitCallbackResponse();

        validateSignificantItem(aboutToSubmitCallbackResponse, callbackResponse);
        callbackService.validateCallbackErrorsAndWarnings(callbackResponse, ignoreWarning);
        if (callbackResponse.getData() != null) {
            validateAndSetData(caseType, caseDetails, callbackResponse.getData());
            if (callbackResponseHasCaseAndDataClassification(callbackResponse)) {
                securityValidationService.setClassificationFromCallbackIfValid(callbackResponse,
                                                                               caseDetails,
                                                                               deduceDefaultClassificationForExistingFields(
                                                                                   caseType,
                                                                                   caseDetails));
            }
            final Optional<String> newCaseState = filterCaseState(callbackResponse.getData());
            newCaseState.ifPresent(caseDetails::setState);
            aboutToSubmitCallbackResponse.setState(newCaseState);
            return aboutToSubmitCallbackResponse;
        }

        aboutToSubmitCallbackResponse.setState(Optional.empty());
        return aboutToSubmitCallbackResponse;
    }


    private boolean callbackResponseHasCaseAndDataClassification(CallbackResponse callbackResponse) {
        return (callbackResponse.getSecurityClassification() != null && callbackResponse.getDataClassification() != null) ? true : false;
    }

    private Map<String, JsonNode> deduceDefaultClassificationForExistingFields(CaseType caseType,
                                                                               CaseDetails caseDetails) {
        Map<String, JsonNode> defaultSecurityClassifications = caseDataService.getDefaultSecurityClassifications(
            caseType,
            caseDetails.getData(),
            EMPTY_DATA_CLASSIFICATION);
        return defaultSecurityClassifications;
    }

    private void validateAndSetData(final CaseType caseType,
                                    final CaseDetails caseDetails,
                                    final Map<String, JsonNode> responseData) {
        caseTypeService.validateData(responseData, caseType);
        caseDetails.setData(caseSanitiser.sanitise(caseType, responseData));
        deduceDataClassificationForNewFields(caseType, caseDetails);
    }

    private void deduceDataClassificationForNewFields(CaseType caseType, CaseDetails caseDetails) {
        Map<String, JsonNode> defaultSecurityClassifications = caseDataService.getDefaultSecurityClassifications(
            caseType,
            caseDetails.getData(),
            ofNullable(caseDetails.getDataClassification()).orElse(
                newHashMap()));
        caseDetails.setDataClassification(defaultSecurityClassifications);
    }

    Optional<String> filterCaseState(final Map<String, JsonNode> data) {
        final Optional<JsonNode> jsonNode = ofNullable(data.get(CALLBACK_RESPONSE_KEY_STATE));
        jsonNode.ifPresent(value -> data.remove(CALLBACK_RESPONSE_KEY_STATE));
        return jsonNode.flatMap(value -> value.isTextual() ? Optional.of(value.textValue()) : Optional.empty());
    }

    private void validateSignificantItem(AboutToSubmitCallbackResponse response, CallbackResponse callbackResponse) {
        final SignificantItem significantItem = callbackResponse.getSignificantItem();
        final List<String> errors = new ArrayList<>();

        if (significantItem != null) {
            if (significantItem.getType() != SignificantItemType.DOCUMENT) {

                errors.add("Significant Item type incorrect");
            }
            if (!URL_VALIDATOR.isValid(significantItem.getUrl())) {
                errors.add("Url Invalid");
            }
            if (isDescriptionEmptyOrNotWithInSpecifiedRange(significantItem)) {
                errors.add("Description should not be empty but also not more than 64 characters");
            }
            if (errors.isEmpty()) {
                response.setSignificantItem(significantItem);
            } else {
                callbackResponse.setErrors(errors);
            }
        }
    }

    private boolean isDescriptionEmptyOrNotWithInSpecifiedRange(SignificantItem significantItem) {

        return StringUtils.isEmpty(significantItem.getDescription())
            || (StringUtils.isNotEmpty(significantItem.getDescription())
            && !(significantItem.getDescription().length() > MIN_LENGTH_OF_DESCRIPTION
            && significantItem.getDescription().length() < MAX_LENGTH_OF_DESCRIPTION));
    }

}
