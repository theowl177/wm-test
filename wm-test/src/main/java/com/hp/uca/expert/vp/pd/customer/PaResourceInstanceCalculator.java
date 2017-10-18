package com.hp.uca.expert.vp.pd.customer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.uca.expert.alarm.Alarm;
import com.hp.uca.expert.group.Group;
import com.hp.uca.expert.group.GroupBase;
import com.hp.uca.expert.group.PropagationGroup;

public class PaResourceInstanceCalculator {

	private static final Logger LOG = LoggerFactory.getLogger(PaResourceInstanceCalculator.class);
	
	public boolean isNeedAppendSubAlarmResourceInstance(Group group){
		boolean rt = false;
		Collection<Alarm> alarms = group.getAlarmList();
		if(alarms!=null && !alarms.isEmpty()){
			String problem = group.getProblemContext().getName();
			LOG.debug("isNeedAppendSubAlarmResourceInstance(), group:["+group.getName()+"] alarms:["+alarms.size()+"] problem:["+problem+"]");
			for(Alarm alarm : alarms){
				Set<String> tags = alarm.getPassingFiltersTags().get(problem);
				if(tags!=null && tags.contains("appendSubAlarmResourceInstance")){
					rt = true;
					break;
				}
			}
		}
		LOG.debug("isNeedAppendSubAlarmResourceInstance(), group:["+group.getName()+"] return:["+rt+"]");
		return rt;
	}
	
	public String calculatePaRsourceInstance(GroupBase gw, boolean appendSubAlarmResourceInstance, Collection<Alarm> subAlarms, String trailUniqueId, String nodeUniqueId, String paMO){
		LOG.debug("calculatePaRsourceInstance(), group:["+gw.getName()+"] appendSubAlarmResourceInstance:["+appendSubAlarmResourceInstance+"]");
		
		String rt = "";
		StringBuilder riBuilder = new StringBuilder();
		
		if(appendSubAlarmResourceInstance){
			Set<String> riSet = new HashSet<String>();
			Alarm alarm = null;
			if(gw instanceof Group){
				alarm = ((Group)gw).getProblemAlarm();
			}else if(gw instanceof PropagationGroup){
				alarm = ((PropagationGroup)gw).getServiceAlarm();
			}
			
			if(alarm!=null){
				String tmp = alarm.getCustomFieldValue("resourceinstance");
				if(tmp!=null && !tmp.isEmpty()){
					String[] ris = tmp.split("#");
					for(String ri : ris){
						riSet.add(ri);
					}
					riBuilder.append(tmp);
				}
			}
			
			if(subAlarms!=null && !subAlarms.isEmpty()){
				for(Alarm subAlarm : subAlarms){
					String subAlarmRI = subAlarm.getCustomFieldValue("resourceinstance");
					if(subAlarmRI!=null && !subAlarmRI.isEmpty() && !riSet.contains(subAlarmRI)){
						if(riBuilder.length()>0){
							riBuilder.append("#");
						}
						riBuilder.append(subAlarmRI);
						riSet.add(subAlarmRI);
						LOG.debug("calculatePaRsourceInstance(), append subAlarm:["+subAlarm.getIdentifier()+"] resourceInstance:["+subAlarmRI+"]");
					}
				}
			}
			
			rt = riBuilder.toString();
		}else{
			//Resourceinstance = "(CI,ME O4CH ADWDM01 O4MO ADWDM03 DIR1001),(TID,null):ALCA_NE proto_ns:.NE.O4CH-A69-05AC-F01-01 RACK 1 SHELF 1 SLOT 14 PORT 1"
			riBuilder.append("(CI,").append(trailUniqueId!=null?trailUniqueId:"null").append("),(TID,").append(nodeUniqueId!=null?nodeUniqueId:"null").append(")");
			riBuilder.append(":").append(paMO);
			
			rt = riBuilder.toString();
			LOG.debug("calculatePaRsourceInstance(), new PA resourceInstance:["+rt+"]");
		}
		
		LOG.debug("calculatePaRsourceInstance(), return:["+rt+"]");
		
		return rt;
	}
	
	
}
