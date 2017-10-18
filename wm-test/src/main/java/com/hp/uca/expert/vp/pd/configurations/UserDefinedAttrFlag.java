package com.hp.uca.expert.vp.pd.configurations;

public class UserDefinedAttrFlag {
	
	private static boolean initiated = false;
	
	private String automationAttrFlag = "false"; 
	
	private String outageAttrFlag = "false";
	
	public UserDefinedAttrFlag() {
		super();
		initiated = true;
	}

	public String getAutomationAttrFlag() {
		return automationAttrFlag;
	}

	public void setAutomationAttrFlag(String automationAttrFlag) {
		this.automationAttrFlag = automationAttrFlag;
	}        

	public String getOutageAttrFlag() {
		return outageAttrFlag;
	}

	public void setOutageAttrFlag(String outageAttrFlag) {
		this.outageAttrFlag = outageAttrFlag;
	}

	public boolean isAutomationAttrActive() {
		String flag = getAutomationAttrFlag();
		if(flag.equalsIgnoreCase("true")) {
			return true;
		}
		
		return false;
		
	}
	
	public boolean isOutageAttrActive() {
		String flag = getOutageAttrFlag();
		if(flag.equalsIgnoreCase("true")) {
			return true;
		}
		
		return false;
	}

	public static boolean isInitiated() {
		return initiated;
	}

}
