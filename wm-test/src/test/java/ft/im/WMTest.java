package ft.im;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.JUnit4TestAdapter;

import org.drools.ObjectFilter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.loader.csv.Loader;
import org.neo4j.loader.csv.Report;
import org.neo4j.loader.csv.utils.TmpDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.uca.common.misc.Constants;
import com.hp.uca.expert.alarm.Alarm;
import com.hp.uca.expert.group.Group;
import com.hp.uca.expert.group.PropagationGroup;
import com.hp.uca.expert.scenario.Scenario;
import com.hp.uca.expert.scenario.exception.NoSuchScenarioException;
import com.hp.uca.expert.testmaterial.AbstractJunitIntegrationTest;
import com.hp.uca.expert.topology.TopoAccess;
import com.hp.uca.expert.vp.flow.db.AlarmFlow;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WMTest extends AbstractJunitIntegrationTest {
	
	private static Logger LOG = LoggerFactory.getLogger(WMTest.class);
	
	private static final String ALARM_WMT_01 = "src/test/resources/alarms/ALARM_WMT_01.xml";
	
	/**
	 * The Constant SCENARIO_PD_NAME.
	 */
	private static final String SCENARIO_PD_NAME = "com.hp.uca.expert.vp.pd.ProblemDetection";
	
	/**
	 * The Constant TOPOLOGY_DATALOAD_DIR.
	 */
	private static final String TOPOLOGY_DATALOAD_DIR = "valuepack/topologyDataload";

	/**
	 * The tmp dir.
	 */
	private TmpDir tmpDir;

	/**
	 * The pd scenario.
	 */
	Scenario pdScenario;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LOG.info(Constants.TEST_START.val());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		LOG.info(Constants.TEST_END.val());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		LOG.info(Constants.TEST_START.val() + this.getClass().getName());

//		tmpDir = new TmpDir(TOPOLOGY_DATALOAD_DIR);
//		Loader loader = new Loader(TopoAccess.getGraphDB(), tmpDir.tmpCsvPath());
//		Report report = loader.loadAll();

//		LOG.info(report.toString());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@After
	public void tearDown() throws Exception {
		LOG.info(Constants.TEST_END.val() + this.getClass().getName()
				+ Constants.GROUP_ALT1_SEPARATOR.val());
		//tmpDir.cleanup();
	}

	/**
	 * Initialize variables and Enable engine internal logs
	 */
	protected void initTest() throws NoSuchScenarioException,
			InterruptedException {
		initTest(SCENARIO_PD_NAME, BMK_PATH);
		getScenario().setTestOnly(true);

		for (AlarmFlow flow : getScenario().getValuePack().getDbFlowsMap()
				.values()) {
			LOG.info("This test holds a dbFlow: " + flow.getName()
					+ " start it!");
			Assert.assertEquals("DB Flow " + flow.getName() + " started",
					flow.start());
		}

		pdScenario = getScenario();
		//tspScenario = retrieveScenario(SCENARIO_TSP_NAME);
	}

	/**
	 * Way to run tests via ANT Junit
	 * 
	 * @return the JUnit4TestAdapter
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(WMTest.class);
	}
	
	@Test
	@DirtiesContext
	public final void testInferenceMachine() throws Exception {

		initTest();

		getProducer().sendAlarms(ALARM_WMT_01);
	
		Thread.sleep(5 * SECOND);

		/*
		 * Disable Rule Logger to close the file used to compare engine
		 * historical events
		 */
		closeRuleLogFiles(getScenario());

		if (LOG.isDebugEnabled()) {
			LOG.debug("DUMPING PD");
			pdScenario.getSession().dump();
			
			//Thread.sleep(10 * SECOND);
			
			//LOG.debug("DUMPING TSP");
			//tspScenario.getSession().dump();
		}
		pdScenario.getSession().dump();
		//tspScenario.getSession().dump();
		/*
		 * Checking Group Number in PD
		 */
		Collection<Group> groups = pdScenario.getGroups().getAllGroups();
		//Assert.assertEquals(1, groups.size());

		/*
		 * Checking Propagation Group Number in TSP
		 */
		//Collection<PropagationGroup> pGroups = tspScenario
		//		.getPropagationGroups().getAllGroups();
		//Assert.assertEquals(10, pGroups.size());

		/*
		 * Checking Alarm Number in PD
		 */
		Collection<Alarm> alarms = getAlarmsFromWorkingMemory();
		//Assert.assertEquals(2, alarms.size());

		

	}

}
