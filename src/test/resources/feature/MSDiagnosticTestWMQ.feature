@E2EAMQ
Feature: WMQ Regressions

  @WorkflowWMQ
    Scenario Outline: Test WMQ Post and Get Validation
    Given Connect to WMQ host <hostName> at port <port> with queue <queueManager>
    Then Post WMQ Request for queue <inputQueue> and replyTo <replyTo> with request <xmlFile>
    Then Get WMQ Response for queue <outputQueue>
    Then Validate WMQ Response for <validationKey> key and //<queueManager>/<replyTo> value

    Examples:
    |hostName|port|queueManager|inputQueue|replyTo|xmlFile|outputQueue|validationKey|
    |SVICF0000042NP.nbndc.local|1415|ICF42NP15|IB2B.SVC.MANAGE_APPOINTMENT.V1.INPUT| IB2B.POS.MANAGE_APPOINTMENT.V1.OUTPUT|\\src\\test\\resources\\testdata\\WMQ\\Request.xml| IB2B.POS.MANAGE_APPOINTMENT.V1.OUTPUT|JMSDestination|