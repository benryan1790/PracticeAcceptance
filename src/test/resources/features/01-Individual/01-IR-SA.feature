@all
Feature: IR-SA

#  Scenario: User is able to request access to IR-SA
#    Given I log in to request access to IR-SA
#    When the UTR is input as 1231231231
#    And the NINO is input as 123123123
#    And I click the request access button
#    Then the I am presented with the IR-SA access requested screen

  Scenario: inject user with activated IR-SA enrolment
    When I call the enrolment store stub and inject details
    Then the response code is 204

  Scenario: User is able to de-enrol from IR-SA
    Given I have an activated enrolment for IR-SA and request a removal
    Then I redirect to the remove Self-Assessment access page
    When I click the continue button
    Then I redirect to the Are you sure you want to remove Self-Assessment access? page
    And I click the remove access button
    Then I am is presented with the IR-SA access removed screen
#      And I am no longer enrolled for IR-SA