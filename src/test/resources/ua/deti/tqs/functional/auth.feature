Feature: User Authentication Testing

  @authentication @data-driven
  Scenario Outline: Login with different user types
    Given I am on the login page
    When I enter credentials "<email>" and "<password>"
    And I click the login button
    Then I should see the "<expected_page>" page

    Examples:
      | email           | password    | expected_page   |
      | admin@admin.com | password123 | admin_dashboard |
      | user@user.com   | password123 | user_dashboard  |
