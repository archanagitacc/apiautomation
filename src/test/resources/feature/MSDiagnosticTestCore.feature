@E2E
Feature: Core regressions

  Scenario Outline: Validate HTTP Post Response - <workflowName> - <testcase>
    Given I have a request for <diagnosticTestTypeHost> service with <requestJSON> on <endPoint>
    Then Verify Post Response <responseCode>

    Examples:
      |testcase|workflowName|diagnosticTestTypeHost|requestJSON|endPoint|responseCode|
      |Expected outcome code: loopback_valid |loopback valid request workflow | devURL|/src/test/resources/testdata/loopback/request/loopback_request.json |v1/loopbackTest |202 |
      |Expected outcome code: dpuStatus_valid |dpustatus valid request workflow | devURL|/src/test/resources/testdata/dpustatus/request/dpustatus_request.json |v1/dpuStatusTest |202|
      |Expected outcome code: dpuportstatus_valid |dpuPortstatus valid request workflow | devURL|/src/test/resources/testdata/dpuportstatus/request/dpuportstatus_request.json |v1/dpuPortStatusTest |202|

      |Expected outcome code: loopback_completed |loopback Invalid request workflow | devURL|/src/test/resources/testdata/loopback/request/loopback_invalid_request.json |v1/loopbackTest |400 |
      |Expected outcome code: dpuStatus_completed |dpustatus Invalid request workflow | devURL|/src/test/resources/testdata/dpustatus/request/dpustatus_invalid_request.json |v1/dpuStatusTest |400|
      |Expected outcome code: dpuportstatus_completed |dpuPortstatus Invalid request workflow | devURL|/src/test/resources/testdata/dpuportstatus/request/dpuportstatus_invalid_request.json |v1/dpuPortStatusTest |400|


  Scenario Outline: Validate Get Response - <workflowName> - <testcase>
    Given I have a request for <diagnosticTestTypeHost> service with <requestJSON> on <endPoint>
    Then Verify Post Response <responseCode>
    Then Wait for Status <statusCode> in GET Response for service <diagnosticTestTypeHost> on <endPoint> for <timeOut> seconds

    Examples:
      |testcase|workflowName|diagnosticTestTypeHost|requestJSON|endPoint|responseCode|statusCode|timeOut|
      |Get Request : loopback| completed | devURL | /src/test/resources/testdata/loopback/request/loopback_request.json | v1/loopbackTest|202|completed|300|
      |Get Request : dpustatus| completed | devURL | /src/test/resources/testdata/dpustatus/request/dpustatus_request.json | v1/dpuStatusTest|202|completed|300|
      |Get Request : dpuportstatus| completed | devURL | /src/test/resources/testdata/dpuportstatus/request/dpuportstatus_request.json | v1/dpuPortStatusTest|202|completed|300|
      |Get Request : loopback | failed | devURL | /src/test/resources/testdata/loopback/request/loopback_failed_request.json | v1/loopbackTest|202|failed|300|
      |Get Request : dpustatus | failed | devURL | /src/test/resources/testdata/dpustatus/request/dpustatus_failed_request.json | v1/dpuStatusTest|202|failed|300|
      |Get Request : dpuportstatus| failed | devURL | /src/test/resources/testdata/dpuportstatus/request/dpuportstatus_failed_request.json | v1/dpuPortStatusTest|202|failed|300|
      |Get Request : loopback | cancelled | devURL | /src/test/resources/testdata/loopback/request/loopback_cancelled_request.json | v1/loopbackTest|202|Cancelled|300|
      |Get Request : dpustatus | cancelled | devURL | /src/test/resources/testdata/dpustatus/request/dpustatus_cancelled_request.json | v1/dpuStatusTest|202|Cancelled|300|
      |Get Request : dpuportstatus | cancelled | devURL | /src/test/resources/testdata/dpuportstatus/request/dpuportstatus_cancelled_request.json | v1/dpuPortStatusTest|202|Cancelled|300|
      |Get Request : loopback | rejected | devURL | /src/test/resources/testdata/loopback/request/loopback_rejected_request.json | v1/loopbackTest|202|Rejected|300|
      |Get Request : dpustatus | rejected | devURL | /src/test/resources/testdata/dpustatus/request/dpustatus_rejected_request.json | v1/dpuStatusTest|202|Rejected|300|
      |Get Request : dpuportstatus | rejected | devURL | /src/test/resources/testdata/dpuportstatus/request/dpuportstatus_rejected_request.json | v1/dpuPortStatusTest|202|Rejected|300|

