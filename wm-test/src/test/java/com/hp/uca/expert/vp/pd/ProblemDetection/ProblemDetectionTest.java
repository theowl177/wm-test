package com.hp.uca.expert.vp.pd.ProblemDetection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import junit.framework.JUnit4TestAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.uca.common.misc.Constants;
import com.hp.uca.common.trace.LogHelper;
import com.hp.uca.expert.alarm.Alarm;
import com.hp.uca.expert.testmaterial.AbstractJunitIntegrationTest;
import com.hp.uca.expert.x733alarm.OperatorState;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProblemDetectionTest extends AbstractJunitIntegrationTest {

	private static Logger log = LoggerFactory.getLogger(ProblemDetectionTest.class);
	private static final String SCENARIO_BEAN_NAME = "WMT.ProblemDetection";
	private static final String ALARM_FILE = "src/test/resources//com/hp/uca/expert/vp/pd/ProblemDetection/Alarms.xml";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log.info(Constants.TEST_START.val() + ProblemDetectionTest.class.getName());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		log.info(Constants.TEST_END.val() + ProblemDetectionTest.class.getName()
				+ Constants.GROUP_ALT1_SEPARATOR.val());
	}

	// Way to run tests via ANT Junit
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ProblemDetectionTest.class);
	}

	@Test
	@DirtiesContext()
	public void test() throws Exception {
		LogHelper.enter(log, "test()");

		/*
		 * Initialize variables and Enable engine internal logs
		 */
		initTest(SCENARIO_BEAN_NAME, BMK_PATH);

		/*
		 * Create,Assign and store an Alarm Listener to the current scenario
		 */
		setAlarmListener(createAndAssignAlarmListener("1", getScenario()));
		
		/*
		 * Send alarms defined in Alarms.xml asynchronously with a tempo of 2 seconds between each alarm
		 */
		getProducer().sendAlarmsAsync(ALARM_FILE, 2 * SECOND);

		/*
		 * Wait for an alarm insertion in scenario working memory  
		 */
		waitingForAlarmInsertion(getAlarmListener(), 100 * MS, 10 * SECOND);
		/*
		 * Retrieve from Working memory the Alarm with identifier '1'
		 */
		Alarm alarm= getAlarm("1");

		/* 
		 * Check that alarm with identifier '1' exists
		 */
		assertNotNull("The alarm 1 should be present in WM",alarm);
		
		/*
		 * Wait for an alarm update in scenario working memory
		 * 
		 */
		waitingForAlarmUpdate(getAlarmListener(), 100 * MS, 10 * SECOND);
		/* 
		 * Check the new values of attributes 'problemInformation' & 'notificationIdentifier' of alarm '1'  
		 */
		assertEquals("The problemInformation should be New Problem information","New Problem information", alarm.getProblemInformation());
		assertEquals("The notificationIdentifier should be equals to 100","100", alarm.getNotificationIdentifier());
		
		/*
		 * Wait for an alarm acknowledgement
		 */
		waitingForAlarmAcknowledgement(getAlarmListener(), 100 * MS, 10 * SECOND);
		/* 
		 * Check if the OperatorState of alarm has been correctly changed to ACKNOWLEDGED  
		 */
		assertEquals("Alarm 1 has been acknowledged",OperatorState.ACKNOWLEDGED, alarm.getOperatorState());

		/*
		 * Wait for an alarm retraction from scenario working memory
		 */
		waitingForAlarmRetract(getAlarmListener(), 100 * MS, 10 * SECOND);
		
		/*
		 * Disable Rule Logger to close the file used to compare engine
		 * historical events
		 */
		closeRuleLogFiles(getScenario());

		/*
		 * Check test result by comparing the historical engine events with a
		 * benchmark
		 */
		 checkTestResult(getLogRuleFileName(),getLogRuleFileNameBmk());

		LogHelper.exit(log, "test()");
	}
}
