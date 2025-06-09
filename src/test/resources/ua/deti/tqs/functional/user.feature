Feature: User Workflow Operations
  As a regular user
  I want to perform standard operations
  So that I can accomplish my tasks efficiently

  @user @workflow
  Scenario: User performs standard workflow operations
    Given I am on the user login page
    When I enter user credentials "user@user.com" and "password123"
    And I click the login button
    Then I should access the user dashboard
    When I navigate through the available options
    And I interact with workflow elements
    And I scroll to view additional content
    And I complete the workflow steps
    Then the workflow should be completed successfully