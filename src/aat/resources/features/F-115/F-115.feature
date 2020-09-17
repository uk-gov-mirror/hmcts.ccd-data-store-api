@F-115
Feature: F-115 RESERVED!! Configuration of hidden field value being persisted

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-115.1
  Scenario: Complex type leaf elements RetainHiddenValue will be true if the parent Complex has been configured to RetainHiddenValue true
    Given a user with [an active profile in CCD]
    And a request is prepared with appropriate values
    And the request [gets the FT_ComplexCollectionComplex CaseType containing a complex type with RetainHiddenValue=true]
    And it is submitted to call the [Start Case Trigger] operation of [CCD Data Store]
    Then a positive response is received
    And the response [shows complex leaf elements having the inherited 'true' value for RetainHiddenValue property]
    And the response has all the details as expected

#  @F-115.2
#  Scenario: Child items in a Collection's RetainHiddenValue will be true if the parent Collection has been configured to RetainHiddenValue true
#    Given a user with [an active profile in CCD]
#    And a request is prepared with appropriate values
#    And the request [gets the FT_ComplexCollectionComplex CaseType containing a Collection with RetainHiddenValue=true]
#    And it is submitted to call the [Start Case Trigger] operation of [CCD Data Store]
#    Then a positive response is received
#    And the response [shows Collection child items having the inherited 'true' value for RetainHiddenValue property]
#    And the response has all the details as expected
#
#  @F-115.3
#  Scenario: Complex type leaf element can override RetainHiddenValue true set by the parent complex
#    Given a user with [an active profile in CCD]
#    And a request is prepared with appropriate values
#    And the request [gets the FT_ComplexCollectionComplex CaseType containing a complex type with RetainHiddenValue=true]
#    And it is submitted to call the [Start Case Trigger] operation of [CCD Data Store]
#    Then a positive response is received
#    And the response [shows complex leaf elements having the inherited 'true' value for RetainHiddenValue property]
#    And the response [shows some complex leaf elements having a 'false' value for RetainHiddenValue property by overriding the inherited value through definition configuration]
#    And the response has all the details as expected


#    //def-store test
#  Scenario: Complex type leaf element can not override RetainHiddenValue false set by the parent complex
