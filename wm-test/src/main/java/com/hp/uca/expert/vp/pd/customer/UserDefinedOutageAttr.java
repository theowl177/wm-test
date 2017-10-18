package com.hp.uca.expert.vp.pd.customer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.uca.common.trace.LogHelper;
import com.hp.uca.expert.alarm.Alarm;
import com.hp.uca.expert.group.GroupBase;
import com.hp.uca.expert.scenario.Scenario;
import com.hp.uca.expert.scenario.ScenarioThreadLocal;
import com.hp.uca.expert.vp.common.core.ScenarioActionsContainer;
import com.hp.uca.expert.vp.pd.configurations.UserDefinedAttrFlag;
import com.hp.uca.expert.vp.pd.util.TemipProxy;

public class UserDefinedOutageAttr {
	
	private static final Logger LOG = LoggerFactory.getLogger(UserDefinedOutageAttr.class);
	
	public boolean isActivate(Scenario scenario, String bean) {
		if(UserDefinedAttrFlag.isInitiated()){
			UserDefinedAttrFlag userDefinedAttrFlag = null;
			if(scenario!=null && scenario.getProblems()!=null && scenario.getProblems().getMyApplicationContext()!=null){
				userDefinedAttrFlag = (UserDefinedAttrFlag) scenario.getProblems().getMyApplicationContext().
						getBean(bean);
			}else if(scenario!=null && scenario.getPropagations()!=null && scenario.getPropagations().getMyApplicationContext()!=null){
				userDefinedAttrFlag = (UserDefinedAttrFlag) scenario.getPropagations().getMyApplicationContext().
						getBean(bean);
			}
			return (userDefinedAttrFlag!=null && userDefinedAttrFlag.isOutageAttrActive());
		}
		return false;
	}
	
	/*
	 * This is function is to update PA Outage attributes, specially for Optus 
	 * If any subalarm's outage_flag is true, then PA's outage_flag is true
	 * And outage_reason is the all subalarms' outage_reason
	 */
	public void UpdateAlarmOutageAttributes(GroupBase group, Alarm alarm, ScenarioActionsContainer problem, Collection<Alarm> newSubAlarmList) throws Exception {
		if(LOG.isTraceEnabled()) {
			LogHelper.enter(LOG, "UpdatePAOutageAttributes()", 
					"[" + group.getName() + "]");
		}
		
		//If activate is false(setting in context.xml), means no need to log Outage attributes
		if(!isActivate(ScenarioThreadLocal.getScenario(), "userDefinedAttrFlag")) {
			return;
		}
		
		if(alarm != null) {
			String paFlag = group.getVar().getString("Outage_Flag");
			String paReason = group.getVar().getString("Outage_Reason");
			
			boolean outageFlag = false;
			String outageReason = "";
			
			String splitRegex = "[\\s;,]+"; //[\\s;,]+
			String separator = ";"; //,
			
			if(paFlag!=null && paFlag.equalsIgnoreCase("true")){
				outageFlag = true;
			}
			Set<String> reasons = new HashSet<String>();
			if(paReason!=null && !paReason.isEmpty()){
				outageReason = paReason.trim();
				String [] rs = outageReason.split(splitRegex);
				for(String reason : rs){
					reason = reason.trim();
					if(!reason.isEmpty()){
						reasons.add(reason);
					}
				}
			}
			if(newSubAlarmList!=null && !newSubAlarmList.isEmpty()){
				for(Alarm ra : newSubAlarmList){
					String or = ra.getCustomFieldValue("outageFlag");
					String flag = ra.getCustomFieldValue("outageReason");
					if(flag != null && flag.equalsIgnoreCase("true")) {
						outageFlag = true;
					}
					if(or != null && !or.isEmpty()) {
						String [] ors = or.split(splitRegex);
						boolean changed = false;
						for(String r : ors){
							r = r.trim();
							if(!r.isEmpty() && !reasons.contains(r)){
								if(!outageReason.isEmpty() && !outageReason.endsWith(",")){
									outageReason += separator;
								}
								outageReason += r;
								reasons.add(r);
								changed = true;
							}
						}
						if(changed && LOG.isDebugEnabled()) {
							LOG.debug("UpdatePAOutageAttributes(), flag is " + 
									flag + " reason is " + or + " outageReason is " 
									+ outageReason);
						}
					}
				}
			}
			
			//If new value is the same as previous, no need to set again.
			if(paFlag == null || paReason == null || !paReason.equalsIgnoreCase(outageReason) ||
					!paFlag.equalsIgnoreCase(String.valueOf(outageFlag))) {
				group.getVar().put("Outage_Flag", String.valueOf(outageFlag));
				group.getVar().put("Outage_Reason", outageReason);
				
				String[] customFieldNames = new String[] { "Outage_Flag", "Outage_Reason"};
				String[] AOfieldNames = new String[] { "outageFlag", "outageReason"};
				String[] newValues = new String[] {String.valueOf(outageFlag), outageReason};
				
				TemipProxy.updateAlarmAttribute(problem, alarm, customFieldNames, AOfieldNames, newValues);
			}
								
		}
		
		if(LOG.isTraceEnabled()) {
			LogHelper.exit(LOG, "UpdatePAOutageAttributes()");
		}
	}
}
