package com.hp.uca.expert.vp.pd.ProblemDetection;

import java.util.List;

import com.hp.uca.expert.alarm.Alarm;
import com.hp.uca.expert.alarm.TimeStampedAttributeChange;
import com.hp.uca.expert.scenario.Scenario;
import com.hp.uca.expert.scenario.ScenarioThreadLocal;
import com.hp.uca.expert.x733alarm.AttributeChange;

/**
 * Utility class
 * 
 */
public class ProblemDetection {

	/**
	 * Method corresponding to alarm insertion in working memory
	 * 
	 * @param alarm
	 *            the alarm that is just inserted in the Working Memory
	 */
	public static void newAlarmInsertion(Alarm alarm) {

		/*
		 * Get the Scenario associated to the current thread
		 */
		Scenario theScenario = ScenarioThreadLocal.getScenario();

		/*
		 * Get the logger used for the current scenario and check if enabled ie
		 * defined in log4j.xml
		 */
		if (theScenario.getLogger().isInfoEnabled()) {
			/* Display full textual description of the received alarm */
			theScenario.getLogger().info(
					"Alarm received: \n" + alarm.toFormattedString());
			theScenario
					.getLogger()
					.info("Rule has fired correctly, and new alarm has been inserted in working memory");
		}
		/*
		 * Update the "justInserted" flag to avoid the rule
		 * "New Alarm Insertion" to be fired at each scenario rules execution
		 */
		alarm.setJustInserted(false);
	}

	/**
	 * Method corresponding to the rule [alarm attribute value change]
	 * 
	 * @param alarm
	 *            the alarm that has received an AttributeValueChange message
	 */
	public static void alarmAttributeChange(Alarm alarm) {

		/*
		 * Get the Scenario associated to the current thread
		 */
		Scenario theScenario = ScenarioThreadLocal.getScenario();

		/*
		 * Get the logger used for the current scenario and check if enabled ie
		 * defined in log4j.xml
		 */
		if (theScenario.getLogger().isInfoEnabled()) {
			/* Display simplest text description of the alarm */
			theScenario.getLogger().info(
					"Alarm received: \n" + alarm.toString());
			theScenario.getLogger().info(
					"Rule has fired correctly, and AVC has been proceeded");

			/*
			 * Retrieve the list of attribute value updates, clears the list and
			 * reset the HasAVCChanged flag to false
			 */
			List<TimeStampedAttributeChange> attributeChanges = alarm
					.getAttributeValueChanges().getChangesAndClear();

			/*
			 * Check if retrieved list exists and contains any modifications of
			 * alarm attribute
			 */
			if (attributeChanges != null && !attributeChanges.isEmpty()) {
				for (TimeStampedAttributeChange timeStampedAttributeChange : attributeChanges) {

					/* Retrieve the list of attribute change stored */
					List<AttributeChange> attChanges = timeStampedAttributeChange
							.getAttributeChange();
					if (attChanges != null && !attChanges.isEmpty()) {

						for (AttributeChange attributeChange : attChanges) {
							/*
							 * Display the name of attribute with its old and
							 * new value
							 */
							theScenario
									.getLogger()
									.info(String
											.format("Field update [name:%s][newValue:%s][oldValue:%s]",
													attributeChange.getName(),
													attributeChange
															.getNewValue(),
													attributeChange
															.getOldValue()));

						}

					}

				}

			}
		}
	}

	/**
	 * Method corresponding to the rule [alarm state change]
	 * 
	 * @param alarm the alarm that has received an StateChange message
	 */
	public static void alarmStateChange(Alarm alarm) {

		/*
		 * Get the Scenario associated to the current thread
		 */
		Scenario theScenario = ScenarioThreadLocal.getScenario();

		/*
		 * Get the logger used for the current scenario and check if enabled ie
		 * defined in log4j.xml
		 */
		if (theScenario.getLogger().isInfoEnabled()) {
			/* Display simplest text description of the alarm */
			theScenario.getLogger().info(
					"Alarm received: \n" + alarm.toString());
			theScenario
					.getLogger()
					.info("Rule has fired correctly, and State Change has been proceeded");

			/*
			 * Retrieve the list of State updates, clears the list and reset the
			 * HasStateChanged flag to false
			 */
			List<TimeStampedAttributeChange> StateChanges = alarm
					.getStateChanges().getChangesAndClear();

			/* Check if retrieved list exists and contains any state change */
			if (StateChanges != null && !StateChanges.isEmpty()) {
				for (TimeStampedAttributeChange timeStampedAttributeChange : StateChanges) {

					/* Retrieve the list of state change stored */
					List<AttributeChange> stChanges = timeStampedAttributeChange
							.getAttributeChange();

					/*
					 * If exists and not empty, display the list of state
					 * changed with their old and new value
					 */
					if (stChanges != null && !stChanges.isEmpty()) {
						for (AttributeChange attributeChange : stChanges) {
							theScenario
									.getLogger()
									.info(String
											.format("State update [name:%s][newValue:%s][oldValue:%s]",
													attributeChange.getName(),
													attributeChange
															.getNewValue(),
													attributeChange
															.getOldValue()));

						}
					}
				}
			}
		}
	}
}
