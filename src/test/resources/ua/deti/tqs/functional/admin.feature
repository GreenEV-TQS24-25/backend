Feature: Admin Charger Management
  As an administrator
  I want to manage charging stations
  So that I can configure and maintain the charging network

  Background:
    Given the admin user is logged into the system

  @admin @charger-management
  Scenario: Admin logs in and manages chargers
    Given I am on the login page
    When I enter admin credentials "admin@admin.com" and "password123"
    And I click the login button
    Then I should be logged into the admin dashboard
    When I navigate to the charger management section
    And I interact with the map interface
    And I select a charger location
    And I enter charger name "Downtown Charger!"
    And I save the charger configuration
    Then the charger should be successfully created