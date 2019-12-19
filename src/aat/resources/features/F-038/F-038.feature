@F-038
Feature: F-038: Get case data for a given case type for Case Worker

  Background: Load test data for the scenario
    Given an appropriate test context as detailed in the test data source

  @S-068 # should succeed with 200
  Scenario: must get case data for a given case type for Case Worker successfully and return positive response HTTP-200 for correct inputs
    Given a user with [an active profile in CCD]
    And   a successful call [to get an event token for the case just created] as in [F-038-Prerequisite]
    And   a case that has just been created as in [F-038-Prerequisite-CreateCase]
    When  a request is prepared with appropriate values
    And   the request [contains case data for a given case type for Case Worker]
    And   it is submitted to call the [get case data for a given case type for case worker] operation of [CCD Data Store]
    Then  a positive response is received
    And   the response [code is HTTP-200]
    And   the response has all other details as expected



  @S-060
  Scenario:	should retrieve empty result when a case exists if caseworker has 'C' access on CaseType
    Given a user with [an active profile in CCD]
    When  a request is prepared with appropriate values
    And   the request [includes caseworker has 'C' access on CaseType]
    And   it is submitted to call the [get case data for a given case type for case worker] operation of [CCD Data Store]
    Then  a negative response is received
    And   the response [code is HTTP-403]
    And   the response has all other details as expected

  @S-061
  Scenario:	should retrieve empty result when a case exists if caseworker has 'CU' access on CaseType
    Given a user with [an active profile in CCD]
    When  a request is prepared with appropriate values
    And   the request [includes caseworker has 'CU' access on CaseType]
    And   it is submitted to call the [get case data for a given case type for case worker] operation of [CCD Data Store]
    Then  a negative response is received
    And   the response [code is HTTP-403]
    And   the response has all other details as expected

  @S-062
  Scenario:	should retrieve empty result when a case exists if caseworker has 'D' access on CaseType
    Given a user with [an active profile in CCD]
    When  a request is prepared with appropriate values
    And   the request [includes caseworker has 'D' access on CaseType]
    And   it is submitted to call the [get case data for a given case type for case worker] operation of [CCD Data Store]
    Then  a negative response is received
    And   the response [code is HTTP-403]
    And   the response has all other details as expected

  @S-063
  Scenario:	should retrieve empty result when a case exists if caseworker has 'U' access on CaseType
    Given a user with [an active profile in CCD]
    When  a request is prepared with appropriate values
    And   the request [includes caseworker has 'U' access on CaseType]
    And   it is submitted to call the [get case data for a given case type for case worker] operation of [CCD Data Store]
    Then  a negative response is received
    And   the response [code is HTTP-403]
    And   the response has all other details as expected

  @S-064
  Scenario:	should retrieve when a case exists if caseworker has 'CR' access on CaseType
    Given a user with [an active profile in CCD]
    When  a request is prepared with appropriate values
    And   the request [includes caseworker has 'CR' access on CaseType]
    And   it is submitted to call the [get case data for a given case type for case worker] operation of [CCD Data Store]
    Then  a negative response is received
    And   the response [code is HTTP-403]
    And   the response has all other details as expected

  @S-065
  Scenario:	should retrieve when a case exists if caseworker has 'CRUD' access on CaseType
    Given a user with [an active profile in CCD]
    When  a request is prepared with appropriate values
    And   the request [includes caseworker has 'CRUD' access on CaseType]
    And   it is submitted to call the [get case data for a given case type for case worker] operation of [CCD Data Store]
    Then  a negative response is received
    And   the response [code is HTTP-403]
    And   the response has all other details as expected

  @S-066
  Scenario:	should retrieve when a case exists if caseworker has 'R' access on CaseType
    Given a user with [an active profile in CCD]
    When  a request is prepared with appropriate values
    And   the request [includes caseworker has 'R' access on CaseType]
    And   it is submitted to call the [get case data for a given case type for case worker] operation of [CCD Data Store]
    Then  a negative response is received
    And   the response [code is HTTP-403]
    And   the response has all other details as expected

  @S-067
  Scenario:	should retrieve when a case exists if caseworker has 'RU' access on CaseType
    Given a user with [an active profile in CCD]
    When  a request is prepared with appropriate values
    And   the request [includes caseworker has 'RU' access on CaseType]
    And   it is submitted to call the [get case data for a given case type for case worker] operation of [CCD Data Store]
    Then  a negative response is received
    And   the response [code is HTTP-403]
    And   the response has all other details as expected





#
#  @S-270 @Ignore # wrong scenario in Excel
#  Scenario: must return 201 if event creation is successful for a citizen
#
#  @S-271 @Ignore # Response code mismatch, expected: 401, actual: 403
#  Scenario: must return 401 when request does not provide valid authentication credentials
#    Given a user with [an active profile in CCD]
#    When  a request is prepared with appropriate values
#    And   the request [does not provide valid authentication credentials]
#    And   it is submitted to call the [submit case creation as citizen] operation of [CCD Data Store]
#    Then  a negative response is received
#    And   the response [code is HTTP-401]
#    And   the response has all other details as expected
#
#  @S-272
#  Scenario: must return 403 when request provides authentic credentials without authorised access to the operation
#    Given a user with [an active profile in CCD]
#    When  a request is prepared with appropriate values
#    And   the request [provides authentic credentials without authorised access to the operation]
#    And   it is submitted to call the [submit case creation as citizen] operation of [CCD Data Store]
#    Then  a negative response is received
#    And   the response [code is HTTP-403]
#    And   the response has all other details as expected
#
#  @S-273 @Ignore # Postponed.
#  Scenario: must return 409 if case is altered outside of transaction
#
#
#  @S-274 @Ignore # Postponed
#  Scenario: must return 409 when case reference is not unique
#
#
#  @S-275 @Ignore # Postponed
#  Scenario: must return 422 if event trigger has failed
#
#
#  @S-276 @Ignore # Postponed
#  Scenario: must return 422 when process could not be started
#
#
#  @S-267 @Ignore # Response code mismatch, expected: 400, actual: 500
#  Scenario: must return negative response HTTP-400 when request contains a malformed case type ID
#    Given a user with [an active profile in CCD]
#    When  a request is prepared with appropriate values
#    And   the request [contains a malformed case type ID]
#    And   it is submitted to call the [submit case creation as citizen] operation of [CCD Data Store]
#    Then  a negative response is received
#    And   the response [code is HTTP-400]
#    And   the response has all other details as expected
#
#  @S-268 @Ignore # Response code mismatch, expected: 400, actual: 500
#  Scenario: must return negative response HTTP-400 when request contains a malformed jurisdiction ID
#    Given a user with [an active profile in CCD]
#    When  a request is prepared with appropriate values
#    And   the request [contains a malformed jurisdiction ID]
#    And   it is submitted to call the [submit case creation as citizen] operation of [CCD Data Store]
#    Then  a negative response is received
#    And   the response [code is HTTP-400]
#    And   the response has all other details as expected
#
#  @S-552
#  Scenario: must return negative response HTTP-400 when request contains a non-existing jurisdiction ID
#    Given a user with [an active profile in CCD]
#    When  a request is prepared with appropriate values
#    And   the request [contains a non-existing jurisdiction ID]
#    And   it is submitted to call the [submit case creation as citizen] operation of [CCD Data Store]
#    Then  a negative response is received
#    And   the response [code is HTTP-400]
#    And   the response has all other details as expected
#
#  @S-553
#  Scenario: must return negative response HTTP-404 when request contains a non-existing case type ID
#    Given a user with [an active profile in CCD]
#    When  a request is prepared with appropriate values
#    And   the request [contains a non-existing case type ID]
#    And   it is submitted to call the [submit case creation as citizen] operation of [CCD Data Store]
#    Then  a negative response is received
#    And   the response [code is HTTP-404]
#    And   the response has all other details as expected
#
#  @S-554
#  Scenario: must return negative response HTTP-403 when request contains a non-existing user ID
#    Given a user with [an inactive profile in CCD]
#    When  a request is prepared with appropriate values
#    And   the request [contains a non-existing user ID]
#    And   it is submitted to call the [submit case creation as citizen] operation of [CCD Data Store]
#    Then  a negative response is received
#    And   the response [code is HTTP-403]
#    And   the response has all other details as expected
#
#  @S-555 @Ignore # Response code mismatch, expected: 400, actual: 500
#  Scenario: must return negative response HTTP-400 when request contains a malformed user ID
#    Given a user with [an inactive profile in CCD]
#    When  a request is prepared with appropriate values
#    And   the request [contains a malformed user ID]
#    And   it is submitted to call the [submit case creation as citizen] operation of [CCD Data Store]
#    Then  a negative response is received
#    And   the response [code is HTTP-400]
#    And   the response has all other details as expected
