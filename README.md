**TicketService**

**Features**

Validates ticket purchase requests, ensuring account IDs and ticket types are correct.
Enforces business rules such as: Maximum of 25 tickets per purchase, Child and infant tickets must accompany adult tickets.  
Calculates total payment dynamically based on ticket prices loaded from a properties file
Reserves seats based on the number of adult and child tickets.
Provides meaningful exception handling for invalid purchase scenarios.

**Technologies Used**

JAVA 21
Junit & Mockito
SLF4J
Maven

**Installation**

Build : mvn clean install
Ensure the **ticket-prices.properties** file is placed in the  directory. The file contains price configurations for various ticket types (e.g., ADULT, CHILD, INFANT).

**Testing**

Unit tests are provided to validate ticket purchase scenarios. Run the tests with: mvn test

**Test Coverage Includes**

Invalid scenarios (e.g., negative account IDs, exceeding ticket limits).
Valid ticket purchases and seat reservations.
Edge cases (e.g., maximum limit of 25 tickets).

**Logging**
Logs are provided for debugging and tracing operations using SLF4J. 
