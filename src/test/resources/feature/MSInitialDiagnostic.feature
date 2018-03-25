Feature: Initial Diagnostic E2E Testing

Scenario Outline: Validate Initial diagnostic response - <testcase>
 Given  I have a request for <diagnosticTestTypeHost> service with <requestJSON> on <endPoint>
 Then verify Initial diagnostic response
 Then Get the response from <endPoint> with correlationId with status <expectedStatus> for service <diagnosticTestTypeHost>
 Then Validate Response from microservice based on <expectedNotification> json file
 
 Examples:
	 |testcase|diagnosticTestTypeHost|endPoint      |requestJSON                                                                               |expectedStatus |expectedNotification   |
	 |Overall completed|initialDiagnosticHost|v1/initialDiagnostic |\\src\\test\\resources\\testdata\\initialdiagnostic\\request\\CompletedPassed.json |COMPLETED      |\\src\\test\\resources\\testdata\\initialdiagnostic\\expected\\CompletedPassed.json|
     |Completed Failed (NTD Status Failed, PON Status Passed)|initialDiagnosticHost|v1/initialDiagnostic |\\src\\test\\resources\\testdata\\initialdiagnostic\\request\\CompletedFailed_NTDFailed_PON_Passed.json  |COMPLETED      |\\src\\test\\resources\\testdata\\initialdiagnostic\\expected\\CompletedFailed_NTDFailed_PON_Passed.json|
     |Completed Failed (NTD Status & PON Status Failed)|initialDiagnosticHost|v1/initialDiagnostic |\\src\\test\\resources\\testdata\\initialdiagnostic\\request\\CompletedFailed_NTDFailed_PON_Failed.json |COMPLETED       |\\src\\test\\resources\\testdata\\initialdiagnostic\\expected\\CompletedFailed_NTDFailed_PON_Failed.json|
     |Cancelled (NTD Status Cancelled)|initialDiagnosticHost|v1/initialDiagnostic|\\src\\test\\resources\\testdata\\initialdiagnostic\\request\\Cancelled_NTDCancelled.json |REJECTED        |\\src\\test\\resources\\testdata\\initialdiagnostic\\expected\\Cancelled_NTDCancelled.json|
     |Cancelled (NTD Status Failed & PON Status Cancelled)|initialDiagnosticHost|v1/initialDiagnostic|\\src\\test\\resources\\testdata\\initialdiagnostic\\request\\Cancelled_NTDFailed_PON_Cancelled.json |REJECTED         |\\src\\test\\resources\\testdata\\initialdiagnostic\\expected\\Cancelled_NTDFailed_PON_Cancelled.json|


Scenario Outline: Validate Initial diagnostic response - <testcase>
 Given  I have a request for <diagnosticTestTypeHost> service with <requestJSON> on <endPoint>
 Then verify Initial diagnostic response <statusCode>
 
 Examples:
   |testcase|diagnosticTestTypeHost|endPoint      |requestJSON                                                   |statusCode|
   |NTD lights information for Missing AVC|initialDiagnosticHost|v1/initialDiagnostic |\\src\\test\\resources\\json\\NTDLights_MissingAVC.json|400         |