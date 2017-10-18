package com.hp.uca.expert.vp.pd.util;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.uca.common.trace.LogHelper;
import com.hp.uca.expert.alarm.Alarm;
import com.hp.uca.expert.vp.common.core.ScenarioActionsContainer;
import com.hp.uca.mediation.action.client.Action;
import com.hp.uca.mediation.action.client.ActionStatus;
import com.hp.uca.temip.mvp.aodirective.mapper.AODirective;
import com.hp.uca.temip.mvp.aodirective.mapper.AODirectiveKey;

public class TemipProxy {
	
	private static Logger LOG = LoggerFactory.getLogger(TemipProxy.class);

	public static final String ACTION_REFERENCE = "TeMIP_AO_Directives_localhost";
	
	public TemipProxy() {
	}
	
	/**
	 * Update alarm additional text in TeMIP.
	 * @param problem
	 * @param alarm
	 * @param newValue
	 */
	public static void updateAlarmAdditionalText(ScenarioActionsContainer problem, Alarm alarm, String newValue) {
		if (LOG.isTraceEnabled()) {
			LogHelper.enter(LOG, "updateAlarmAdditionalText", alarm.getIdentifier() + " "
					+ newValue);
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateAlarmAdditionalText: Updating Additional Text for the Alarm {}", 
					alarm.getIdentifier());
		}
		try {
			//SupportedActions supportedActions = PD_Service_Action.retrieveSupportedActions(alarm, problem);
			Action action = new Action(ACTION_REFERENCE);
			
			action.addCommand(AODirectiveKey.DIRECTIVE_NAME, AODirective.SET);
			action.addCommand(AODirectiveKey.ENTITY_NAME, alarm.getIdentifier());
			action.addCommand(AODirectiveKey.ADDITIONAL_TEXT, newValue);
			if (problem.getScenario()!=null && !problem.getScenario().isTestOnly()) {
				action.executeSync();
			} else {
				alarm.setAdditionalText(newValue);
			}

			if (action.getActionStatus().equals(ActionStatus.Completed)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("updateAlarmAdditionalText: Action update is success for : {}", newValue);
				}
				alarm.setAdditionalText(newValue);
			} else {
				LOG.info(
						"updateAlarmAdditionalText: Exception occured: Unable to update the alarm additional text in TeMIP for the alarm : {}",
						alarm.getIdentifier());
			}
		} catch (Exception e) {
			LOG.error("updateAlarmAdditionalText: Exception while initializing Action: updateAlarmAdditionalText for {}",
					alarm.getIdentifier(), e);
		}
		if (LOG.isTraceEnabled()) {
			LogHelper.exit(LOG, "updateAlarmAdditionalText");
		}
	}
	
	/**
	 * Update alarm attribute in TeMIP.
	 * 
	 * @param problem
	 *            the problem
	 * @param alarm
	 *            the alarm
	 * @param customFieldName
	 *            the custom field name
	 * @param AOfieldName
	 *            the a ofield name
	 * @param newValue
	 *            the new value
	 */
	public static void updateAlarmAttribute(ScenarioActionsContainer problem, Alarm alarm, String customFieldName,
			String AOfieldName, String newValue) {
		if (LOG.isTraceEnabled()) {
			LogHelper.enter(LOG, "updateAlarmAttribute", alarm.getIdentifier() + " "
					+ customFieldName + " " + AOfieldName + " " + newValue);
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateAlarmAttribute: Updating Custom Attribute {} for the Alarm {}", customFieldName,
					alarm.getIdentifier());
		}
		try {
			//SupportedActions supportedActions = PD_Service_Action.retrieveSupportedActions(alarm, problem);
			Action action = new Action(ACTION_REFERENCE);
			
			action.addCommand(AODirectiveKey.DIRECTIVE_NAME, AODirective.SET);
			action.addCommand(AODirectiveKey.ENTITY_NAME, alarm.getIdentifier());
			action.addCommand(customFieldName, newValue);
			if (problem.getScenario()!=null && !problem.getScenario().isTestOnly()) {
				action.executeSync();
			} else {
				alarm.setCustomFieldValue(customFieldName, newValue);
			}
			//action.executeSync();

			if (action.getActionStatus().equals(ActionStatus.Completed)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("updateAlarmAttribute: Action update is success for {} : {}", customFieldName, newValue);
				}
				alarm.setCustomFieldValue(AOfieldName, newValue);

			} else {
				LOG.info(
						"updateAlarmAttribute: Exception occured: Unable to update the alarm custom attribute in TeMIP: {} for the alarm : {}",
						customFieldName, alarm.getIdentifier());
			}
		} catch (Exception e) {
			LOG.error("updateAlarmAttribute: Exception while initializing Action: updateAlarmCustomAttr {} for {}",
					customFieldName, alarm.getIdentifier(), e);
		}
		if (LOG.isTraceEnabled()) {
			LogHelper.exit(LOG, "updateAlarmAttribute");
		}
	}
	
	/**
	 * Update alarm attribute in TeMIP.
	 * @param problem
	 * @param alarm
	 * @param customFieldNames
	 * @param AOfieldNames
	 * @param newValues
	 */
	public static void updateAlarmAttribute(ScenarioActionsContainer problem, Alarm alarm, String[] customFieldNames, String[] AOfieldNames, String[] newValues) {
		if (LOG.isTraceEnabled()) {
			LogHelper.enter(LOG, "updateAlarmAttribute()", "[" + alarm.getIdentifier() + "], [" + Arrays.asList(customFieldNames)
					+ "], [" + Arrays.asList(newValues) + "]");
		}
		try {
			//SupportedActions supportedActions = PD_Service_Action.retrieveSupportedActions(alarm, problem);
			Action action = new Action(ACTION_REFERENCE);
			
			action.addCommand(AODirectiveKey.DIRECTIVE_NAME, AODirective.SET);
			action.addCommand(AODirectiveKey.ENTITY_NAME, alarm.getIdentifier());//
			for (int i = 0; i < customFieldNames.length; i++) {
				if (problem.getScenario()!=null && !problem.getScenario().isTestOnly()) {
					action.addCommand(customFieldNames[i], newValues[i]);
				}else{
					alarm.setCustomFieldValue(customFieldNames[i], newValues[i]);
				}
			}
			if (problem.getScenario()!=null && !problem.getScenario().isTestOnly()) {
				action.executeSync();
			}
			
			if (action.getActionStatus().equals(ActionStatus.Completed)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("updateAlarmAttribute: Action update is success for {} : {}", Arrays.asList(customFieldNames), Arrays.asList(newValues));
				}
				
				for (int i = 0; i < AOfieldNames.length; i++) {
					alarm.setCustomFieldValue(AOfieldNames[i], newValues[i]);
				}
			} else {
				LOG.info(
						"updateAlarmAttribute: Exception occured: Unable to update the alarm custom attribute in TeMIP: {} for the alarm : {}",
						Arrays.asList(customFieldNames), alarm.getIdentifier());
			}

		} catch (Exception e) {
			LOG.error("updateAlarmAttribute: Exception while initializing Action: updateAlarmCustomAttr {} for {}",
					Arrays.asList(customFieldNames), alarm.getIdentifier(), e);
		}
		
		if (LOG.isTraceEnabled()) {
			LogHelper.exit(LOG, "updateAlarmAttribute()");
		}

	}
}
