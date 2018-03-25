@E2EJeddis
Feature: Jeddis regressions

 @WorkflowJeddis
 Scenario Outline: Jeddis notification validation - <workflowName> - <testcase>
 Given I have a request for <diagnosticTestTypeHost> service with <requestJSON> on <endPoint>
 Then Verify Post Response <responseCode>
 Then Consume messages from jeddis cache <notificationCount>
 Then Validate jeddis messages <expectedNotification>
 
 Examples:
   |testcase|workflowName|diagnosticTestTypeHost|requestJSON                                                                                 |notificationCount |endPoint          |responseCode|expectedNotification|
	|Overall Completed|Red optical workflow|diagnosticTestRedOpticalHost |\\src\test\\resources\\testdata\\dignostictest\\redoptical\\request\\RedOpticalNTDPass.json | 5                |v1/diagnosticTest |202         |OverallCompleted    |

Scenario Outline: Jeddis notification validation using polling mechanism - <testcase>
 Given I have a request for <diagnosticTestTypeHost> service with <requestJSON> on <endPoint>
 Then Verify Post Response <responseCode>
 Then Poll and get response from <redOpticalEndPoint> with correlationId with status <expectedNotification> for service <diagnosticTestTypeHost>
 
 Examples:
   |testcase|diagnosticTestTypeHost|requestJSON                                                                                 |notificationCount |endPoint          |redOpticalEndPoint|responseCode|expectedNotification|
   |with Get|diagnosticTestRedOpticalHost |\\src\test\\resources\\testdata\\dignostictest\\redoptical\\request\\RedOpticalNTDPass.json | 5                |v1/diagnosticTest |v1/redOptical     |202         |Completed           |

Scenario Outline: Jeddis validate response code - <testcase>
 Given I have a request for <diagnosticTestTypeHost> service with <requestJSON> on <endPoint>
 Then Verify Post Response <responseCode>
 
 Examples:
   |testcase|diagnosticTestTypeHost|requestJSON                                                                                 | endPoint          |responseCode|
   |Diagnostic wrapper with 202 response code|diagnosticTestRedOpticalHost |\\src\test\\resources\\testdata\\dignostictest\\redoptical\\request\\RedOpticalNTDPass.json | v1/diagnosticTest |202         |
   |Diagnostic wrapper with 400 response code|diagnosticTestRedOpticalHost|\\src\test\\resources\\testdata\\dignostictest\\redoptical\\request\\InvalidRequest.json | v1/diagnosticTest |400         |
