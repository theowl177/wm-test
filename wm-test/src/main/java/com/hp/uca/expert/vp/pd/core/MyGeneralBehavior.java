package com.hp.uca.expert.vp.pd.core;

import org.slf4j.LoggerFactory;

import com.hp.uca.expert.event.Event;

public class MyGeneralBehavior extends GeneralBehaviorDefault {

	/**
	 * Instantiates a new my general behavior.
	 */
	public MyGeneralBehavior() {
		super();
		setLog(LoggerFactory.getLogger(MyGeneralBehavior.class));
	}
	
	@Override
	public String computeSourceUniqueId(Event event) throws Exception {
		String rt = super.computeSourceUniqueId(event);
		if(rt!=null){
			rt = rt.toLowerCase();
		}
		return rt;
	}

}