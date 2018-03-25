@E2EAMQ
Feature: AMQ Regressions

@WorkflowAMQ
Scenario Outline: AMQ notification validation - <workflowName> - <testcase>
 Given  I have a request for  <diagnosticTestTypeHost> service with <requestJSON> on <endPoint>
 Then Verify Post Response <responseCode>
 Then Connect to AMQ for <brokerName>
 Then Consume the message from AMQ for <topic>
 Then Validate AMQ Messages for response <expectedNotification>

 Examples:
	|workflowName|testcase|diagnosticTestTypeHost|requestJSON                                                                              |endPoint       |responseCode|expectedNotification| topic| brokerName|
	|PostInstall|overall Completed|postInstallHost|\\src\\test\\resources\\testdata\\hfspostinstall\\request\\PostInstallCompletedPass.json |v1/postInstall |202         |OverallCompleted    | hfs-post-install| Broker1|
    |PostInstall|Loopback Failed|postInstallHost|\\src\\test\\resources\\testdata\\hfspostinstall\\request\\PostInstallLBFail.json      |v1/postInstall |202         |OverallCompletedLBFail|hfs-post-install   |Broker1 |
    |PostInstall|NTD Failed|postInstallHost|\\src\\test\\resources\\testdata\\hfspostinstall\\request\\PostInstallNTDFail.json         |v1/postInstall |202         |OverallCancelledNTDFail|hfs-post-install   |Broker1 |


Scenario Outline: AMQ validate response code - <workflowName> - <testcase>
 Given I have a request for  <diagnosticTestTypeHost> service with <requestJSON> on <endPoint>
 Then Verify Post Response <responseCode>
 
 Examples:
	 |testcase|workflowName|diagnosticTestTypeHost|requestJSON                                                                                     |endPoint       |responseCode|
	 |Rejected scenerio|PostInstall|postInstallHost|\\src\\test\\resources\\testdata\\hfspostinstall\\request\\PostInstallRejectedWithInvalidAVC.json  |v1/postInstall |400         |
	 |Rejected scenerio|PostInstall|postInstallHost|\\src\\test\\resources\\testdata\\hfspostinstall\\request\\PostInstallRejectedWithInvalidASID.json |v1/postInstall |400         |
	 |Rejected scenerio|PostInstall|postInstallHost|\\src\\test\\resources\\testdata\\hfspostinstall\\request\\PostInstallRejectedWithMissingRole.json |v1/postInstall |400         |
	 |Rejected scenerio|PostInstall|postInstallHost|\\src\\test\\resources\\testdata\\hfspostinstall\\request\\PostInstallRejectedWithMissingChannel.json |v1/postInstall |400      |
	 
