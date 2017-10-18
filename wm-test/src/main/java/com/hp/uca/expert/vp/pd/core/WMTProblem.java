package com.hp.uca.expert.vp.pd.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.ResourceIterator;
import org.slf4j.LoggerFactory;

import com.hp.uca.common.trace.LogHelper;
import com.hp.uca.expert.alarm.Alarm;
import com.hp.uca.expert.event.Event;
import com.hp.uca.expert.group.Group;
import com.hp.uca.expert.group.GroupBase;
import com.hp.uca.expert.instancemapper.MapperUtils;
import com.hp.uca.expert.scenario.Scenario;
import com.hp.uca.expert.topology.CypherQuery;
import com.hp.uca.expert.topology.query.GenericQuery;
import com.hp.uca.expert.topology.query.result.DbRecord;
import com.hp.uca.expert.vp.common.services.IM_Service_Cache;
import com.hp.uca.expert.vp.pd.configurations.UserDefinedAttrFlag;
import com.hp.uca.expert.vp.pd.customer.PaResourceInstanceCalculator;
import com.hp.uca.expert.vp.pd.customer.UserDefinedOutageAttr;
import com.hp.uca.expert.vp.pd.services.PD_Service_ProblemAlarm;
import com.hp.uca.expert.x733alarm.PerceivedSeverity;
import com.hp.uca.mediation.action.client.Action;

public class WMTProblem extends XmlProblem {
	
	private UserDefinedOutageAttr userDefinedOutageAttr = null;
	
	public WMTProblem() {
		super();
		setLog(LoggerFactory.getLogger(WMTProblem.class));
		
		userDefinedOutageAttr = new UserDefinedOutageAttr();
	}
	
	protected String getPassingFiltersParamValue(Event event, String problem, String paramKey){
		if (log.isTraceEnabled()) {
			LogHelper.enter(log, "getPassingFiltersParamValue(), {}",
					"alarm : " + event.getIdentifier() + ", problem : " + problem + ", paramKey : " + paramKey);
		}
		
		String result = null;
		if(event!=null && problem!=null && paramKey!=null){
			if(event.getPassingFiltersParams().get(problem)!=null
					&& event.getPassingFiltersParams().get(problem).get(paramKey)!=null){
				result = event.getPassingFiltersParams().get(problem).get(paramKey);
			}
		}
		
		if (log.isTraceEnabled()) {
			LogHelper.exit(log, "getPassingFiltersParamValue()",
					"result : " + result);
		}
		
		return result;
	}
	
	protected List<Object> getNodeFieldValueFromNeo4j(String query, Map<String, Object> params, String fieldKey){
		List<Object> ret = null;
		if(query!=null && !query.isEmpty() && fieldKey!=null && !fieldKey.isEmpty()){
			ExecutionResult cypherQuery = CypherQuery.executeAndreturnResult(query, params);
			if(cypherQuery!=null){
				ret = new ArrayList<Object>();
				ResourceIterator<Map<String, Object>> iterator = cypherQuery.iterator();
				while(iterator.hasNext()){
					Map<String, Object> map = cypherQuery.iterator().next();
					if(map!=null && map.containsKey(fieldKey)){
						Object fieldValue = map.get(fieldKey);
						if(fieldValue!=null){
							ret.add(fieldValue);
						}
					}
				}
			}
		}
		return ret;
	}
	
	@Override
	public String calculateAlarmProbableCause(GroupBase group, Event event) throws Exception {
		if (log.isTraceEnabled()) {
			LogHelper.enter(log, "calculateProblemAlarmProbableCause(), {}",
					"Event : " + event.getIdentifier() + ", Group : " + group.getName());
		}
		String probableCause = super.calculateAlarmProbableCause(group, event);
		if (((Group) group).getTriggerEvent().getPassingFiltersParams()
				.get(this.getProblemContext().getName()).get("ProbableCause") != null) {
			if (log.isDebugEnabled()) {
				log.debug(
						"Probable cause tag is present in the passing filter params for the event : {}",
						event.getIdentifier());
			}
			probableCause = ((Group) group).getTriggerEvent().getPassingFiltersParams()
					.get(this.getProblemContext().getName()).get("ProbableCause");
		}
		
		if (log.isTraceEnabled()) {
			LogHelper.exit(log, "calculateProblemAlarmProbableCause()",
					"probableCause : " + probableCause);
		}
		
		return probableCause;
	}
	
	@Override
	public PerceivedSeverity calculateProblemAlarmSeverity(Group group)
			throws Exception {
		if (log.isTraceEnabled()) {
			LogHelper.enter(log, "calculateProblemAlarmSeverity()" + " Group : "
					+ group.getName());
		}

		PerceivedSeverity perceivedSeverity = PerceivedSeverity.CRITICAL;
		if (((Group) group).getTriggerEvent()!=null && ((Group) group).getTriggerEvent().getPassingFiltersParams()
				.get(this.getProblemContext().getName()).get("PerceivedSeverity") != null) {
			Event trigger = ((Group) group).getTriggerEvent();
			if (log.isDebugEnabled()) {
				log.debug(
						"perceived severity tag is present in the passing filter params for the event : {}",
						trigger.getIdentifier());
			}
			String ps = ((Group) group).getTriggerEvent().getPassingFiltersParams()
					.get(this.getProblemContext().getName()).get("PerceivedSeverity");
			perceivedSeverity = PerceivedSeverity.fromValue(ps);
		}
		
		if (log.isTraceEnabled()) {
			LogHelper.exit(log, "calculateProblemAlarmSeverity()",
					"perceivedSeverity : " + perceivedSeverity.toString());
		}
		
		return perceivedSeverity;
	}
	
	@Override
	public String calculateProblemAlarmManagedEntity(Group group)
			throws Exception {
		if (log.isTraceEnabled()) {
			LogHelper.enter(log, "calculateProblemAlarmManagedEntity()" + " Group : "
					+ group.getName());
		}
		
		String problem = this.getProblemContext().getName();
		Alarm trigger = (Alarm)group.getTriggerEvent();
		String result = trigger.getOriginatingManagedEntity();
		
		String queryName = getPassingFiltersParamValue(trigger, problem, "ComputeNodeCypherQuery");
		if(queryName!=null && !queryName.isEmpty()){
			log.debug("calculateProblemAlarmManagedEntity(), ComputeNodeCypherQuery : "+queryName);
			
			String query = this.getScenario().getMappers().getCypherQuery(queryName);
			
			if(query!=null && !query.isEmpty()){
				String mapper = getPassingFiltersParamValue(trigger, "ReservedForGeneralBehavior", "ComputeSourceUniqueIdMapper");
				log.debug("calculateProblemAlarmManagedEntity(), UniqueIdMapper : "+mapper);
				if(mapper!=null && !mapper.isEmpty()){
					String uniqueId = MapperUtils.doMapping(trigger, mapper);
					if(uniqueId!=null && !uniqueId.isEmpty()){
						uniqueId = uniqueId.toLowerCase();
						log.debug("calculateProblemAlarmManagedEntity(), UniqueId : "+uniqueId);
						
						GenericQuery genericQuery = new GenericQuery();
						genericQuery.init(query, uniqueId);
						genericQuery.executeQueryAndExtractRecords();
						List<DbRecord> dbRecords = genericQuery.getDbRecords();
						if(dbRecords!=null && !dbRecords.isEmpty()){
							DbRecord dbRecord = dbRecords.get(0);
							String mo = (String) dbRecord.getAdditionalData().get("MO");
							if(mo!=null && !mo.isEmpty()){
								result = mo;
								log.debug("calculateProblemAlarmManagedEntity(), Get MO from DB : "+mo);
							}
						}
					}
				}
			}
		}
		
		if (log.isTraceEnabled()) {
			LogHelper.exit(log, "calculateProblemAlarmManagedEntity()",
					"result : " + result);
		}
		return result;
	}
	
	@Override
	public String calculateProblemAlarmAdditionalText(Group group)
			throws Exception {
		if (log.isTraceEnabled()) {
			LogHelper.enter(log, "calculateProblemAlarmAdditionalText()" + " Group : "
					+ group.getName());
		}
		
		String problem = this.getProblemContext().getName();
		Alarm trigger = (Alarm)group.getTriggerEvent();
		String result = "";
		
		String prefix = getPassingFiltersParamValue(trigger, problem, "ADText");
		String subType = getPassingFiltersParamValue(trigger, problem, "SubType");
		String nodeType = getPassingFiltersParamValue(trigger, problem, "NodeType");
		String faultCause = getPassingFiltersParamValue(trigger, problem, "FaultCause");
		String uniqueId = "";
		String mapper = getPassingFiltersParamValue(trigger, "ReservedForGeneralBehavior", "ComputeSourceUniqueIdMapper");
		if(mapper!=null && !mapper.isEmpty()){
			uniqueId = MapperUtils.doMapping(trigger, mapper);
			if(uniqueId!=null && !uniqueId.isEmpty()){
				uniqueId = uniqueId.toLowerCase();
			}
		}
		
		if(faultCause!=null && !faultCause.isEmpty()){
			faultCause = faultCause + "(" + ((uniqueId!=null)?uniqueId:"") + ")";
		}
		
		log.debug("calculateProblemAlarmAdditionalText(), Get ADText : "+prefix);
		log.debug("calculateProblemAlarmAdditionalText(), Get NodeType : "+nodeType);
		log.debug("calculateProblemAlarmAdditionalText(), Get SubType : "+subType);
		log.debug("calculateProblemAlarmAdditionalText(), Get RootCause : "+faultCause);
		log.debug("calculateProblemAlarmAdditionalText(), Get NodeUniqueId : "+uniqueId);
		
		StringBuilder builder = new StringBuilder();
		String lineSeparator = System.lineSeparator();
		if(prefix!=null){
			builder.append(prefix);
		}
		builder.append(lineSeparator);
		builder.append("<START>").append(lineSeparator);
		builder.append("NodeType=").append((nodeType!=null)?nodeType:"").append(";").append(lineSeparator);
		builder.append("SubType=").append((subType!=null)?subType:"").append(";").append(lineSeparator);
		builder.append("RootCause=").append((faultCause!=null)?faultCause:"").append(";").append(lineSeparator);
		builder.append("NodeUniqueId=").append((uniqueId!=null)?uniqueId:"").append(";").append(lineSeparator);
		
		if(uniqueId!=null && !uniqueId.isEmpty() && trigger!=null && trigger.getPassingFiltersParams()!=null
				&& trigger.getPassingFiltersParams().get(problem)!=null){
			Map<String, String> map = trigger.getPassingFiltersParams().get(problem);
			if(map!=null && !map.isEmpty()){
				Set<String> set = map.keySet();
				for(String key : set){
					if(key.matches("RelNodeField\\d+ComputeQuery$")){
						String fieldName = key.substring(0, key.indexOf("ComputeQuery"));
						String queryName = map.get(key);
						String resultKeyParam = fieldName + "ComputeKey";
						String resultKey = "endNode.uniqueId";
						if(map.containsKey(resultKeyParam)){
							resultKey = map.get(resultKeyParam);
						}
						String fieldValue = "";
						String query = null;
						
						if(queryName!=null && !queryName.isEmpty()){
							query = this.getScenario().getMappers().getCypherQuery(queryName);
						}
						
						if(query!=null && !query.isEmpty() && resultKey!=null && !resultKey.isEmpty()){
							Map<String, Object> params = new HashMap<String, Object>();
							params.put("nodeUniqueId", uniqueId);
							List<Object> values = getNodeFieldValueFromNeo4j(query, params, resultKey);
							if(values!=null && !values.isEmpty()){
								for(Object value : values){
									if(value!=null){
										fieldValue = value.toString();
										builder.append(fieldName).append("=").append((fieldValue!=null)?fieldValue:"").append(";").append(lineSeparator);
									}
								}
							}
						}
					}
				}
			}
		}
		
		builder.append("<END>");
		
		result = builder.toString();
		
		if (log.isTraceEnabled()) {
			LogHelper.exit(log, "calculateProblemAlarmAdditionalText()",
					"result : " + result);
		}
		return result;
	}
	
	@Override
	public boolean isAllCriteriaForProblemAlarmCreation(Group group)
			throws Exception {
		if (this.log.isTraceEnabled()) {
			LogHelper.enter(this.log, "isAllCriteriaForProblemAlarmCreation()",
					group.getName());
		}

		boolean ret = PD_Service_ProblemAlarm
				.isItTimeForProblemAlarmCreation(group);

		if (this.log.isTraceEnabled()) {
			LogHelper.exit(this.log, "isAllCriteriaForProblemAlarmCreation()",
					String.valueOf(ret));
		}

		return ret;
	}
	
	@Override
	public void calculateProblemAlarmOtherAttribute(Group group, Action action)
			throws Exception {
		if (this.log.isTraceEnabled()) {
			LogHelper.enter(this.log, "calculateProblemAlarmOtherAttribute", group.getName());
		}
		
		if(isAutomationActivate(getScenario())){
			String nodeUniqueId = null;
			String paMO = this.calculateProblemAlarmManagedEntity(group);
			String problem = "IPMPLS:"+this.getProblemContext().getName();
			String evp = "UCA_Automation_Optus_Evaluate";
			String evpscenario = "evaluate";
			String dbIdvalue = "dbNodeId=[";
			String dbId = null;
			
			Alarm trigger = (Alarm)group.getTriggerEvent();
			List<DbRecord> dbRecords = IM_Service_Cache.getDbRecords(trigger, this.getProblemContext().getName());
			if(dbRecords!=null && !dbRecords.isEmpty()){
				nodeUniqueId = dbRecords.get(0).getDbUniqueIdReference();
				dbId = dbRecords.get(0).getDbId().toString();
			}else{
				String queryName = getPassingFiltersParamValue(trigger, this.getProblemContext().getName(), "ComputeNodeCypherQuery");
				if(queryName!=null && !queryName.isEmpty()){
					log.debug("calculateProblemAlarmOtherAttribute(), ComputeNodeCypherQuery : "+queryName);
					
					String query = this.getScenario().getMappers().getCypherQuery(queryName);
					
					if(query!=null && !query.isEmpty()){
						String mapper = getPassingFiltersParamValue(trigger, "ReservedForGeneralBehavior", "ComputeSourceUniqueIdMapper");
						log.debug("calculateProblemAlarmOtherAttribute(), UniqueIdMapper : "+mapper);
						if(mapper!=null && !mapper.isEmpty()){
							String uniqueId = MapperUtils.doMapping(trigger, mapper);
							if(uniqueId!=null && !uniqueId.isEmpty()){
								uniqueId = uniqueId.toLowerCase();
								log.debug("calculateProblemAlarmOtherAttribute(), UniqueId : "+uniqueId);
								
								GenericQuery genericQuery = new GenericQuery();
								genericQuery.init(query, uniqueId);
								genericQuery.executeQueryAndExtractRecords();
								dbRecords = genericQuery.getDbRecords();
								if(dbRecords!=null && !dbRecords.isEmpty()){
									DbRecord dbRecord = dbRecords.get(0);
									nodeUniqueId = dbRecord.getDbUniqueIdReference();
									dbId = dbRecord.getDbId().toString();
								}
							}
						}
					}
				}
			}
			
			if(dbId!=null && !dbId.isEmpty()){
				dbIdvalue += dbId+ "] domain=[IPMPLS]";
			}else{
				dbIdvalue += " ] domain=[IPMPLS]";
			}
			
			PaResourceInstanceCalculator calculator = new PaResourceInstanceCalculator();
			boolean appendChildRI = calculator.isNeedAppendSubAlarmResourceInstance(group);
			String resourceInstance = calculator.calculatePaRsourceInstance(group, appendChildRI, group.getAlarmList(), nodeUniqueId, null, paMO);
			
			action.addCommand("Problem", problem);
			action.addCommand("Resourceinstance", resourceInstance);
			action.addCommand("Evpscenario", evpscenario);
			action.addCommand("Evp", evp);
			action.addCommand("Uca_Custom_Field5", dbIdvalue);
		}
		
		if (this.log.isTraceEnabled()) {
			LogHelper.exit(this.log, "calculateProblemAlarmOtherAttribute", group.getName());
		}
	}
	
	@Override
	public String calculateProblemAlarmUserText(Group group, Action action)
			throws Exception {
		String ut =  super.calculateProblemAlarmUserText(group, action);
		StringBuilder utBuilder = new StringBuilder();
		if(ut!=null){
			utBuilder.append(ut);
		}
		if(isAutomationActivate(getScenario())){
			utBuilder.append("<uca-auto>to_be_processed_by_UCAAutomation</uca-auto>");
		}
		return utBuilder.toString();
	}
	
	public boolean isAutomationActivate(Scenario scenario) {
		if(UserDefinedAttrFlag.isInitiated()){
			UserDefinedAttrFlag userDefinedAttrFlag = (UserDefinedAttrFlag) scenario.getProblems().getMyApplicationContext().
					getBean("userDefinedAttrFlag");
			return (userDefinedAttrFlag!=null && userDefinedAttrFlag.isAutomationAttrActive());
		}
		return false;
	}
	
	@Override
	public void whatToDoWhenProblemAlarmIsAttachedToGroup(Group group)
			throws Exception {
		if (this.log.isTraceEnabled()) {
			LogHelper.enter(this.log,
					"whatToDoWhenProblemAlarmIsAttachedToGroup, group:" + group.getName() + " alarm:" + group
							.getProblemAlarm().getIdentifier());
		}
		super.whatToDoWhenProblemAlarmIsAttachedToGroup(group);
		
		List<Alarm> alarms = new ArrayList<Alarm>();
		if(group.getAlarmList()!=null && !group.getAlarmList().isEmpty()){
			alarms.addAll(group.getAlarmList());
		}
		userDefinedOutageAttr.UpdateAlarmOutageAttributes(group, group.getProblemAlarm(), this, alarms);
		
		if (this.log.isTraceEnabled()) {
			LogHelper.exit(this.log, 
					"whatToDoWhenProblemAlarmIsAttachedToGroup, group:" + group.getName() + " alarm:" + group
							.getProblemAlarm().getIdentifier());
		}
	}
	
	@Override
	public void whatToDoWhenSubAlarmIsAttachedToGroup(Alarm alarm, Group group)
			throws Exception {
		if (this.log.isTraceEnabled()) {
			LogHelper.enter(this.log,
					"whatToDoWhenSubAlarmIsAttachedToGroup, group:" + group.getName() + " alarm:" + alarm.getIdentifier());
		}
		super.whatToDoWhenSubAlarmIsAttachedToGroup(alarm, group);
		
		if(group.getProblemAlarm()!=null){
			List<Alarm> alarms = new ArrayList<Alarm>();
			alarms.add(alarm);
			userDefinedOutageAttr.UpdateAlarmOutageAttributes(group, group.getProblemAlarm(), this, alarms);
		}
		
		if (this.log.isTraceEnabled()) {
			LogHelper.exit(this.log, 
					"whatToDoWhenSubAlarmIsAttachedToGroup, group:" + group.getName() + " alarm:" + alarm.getIdentifier());
		}
	}
	
	

}
