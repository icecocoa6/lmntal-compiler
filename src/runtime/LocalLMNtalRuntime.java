package runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import daemon.LMNtalDaemon;

/** ����VM�Ǽ¹Ԥ����󥿥���ʵ졧ʪ���ޥ��󡢵졹���׻��Ρ��ɡ�
 * ���Υ��饹�Υ��֥��饹�Υ��󥹥��󥹤ϡ�1�Ĥ� Java VM �ˤĤ��⡹1�Ĥ���¸�ߤ��ʤ���
 * @author n-kato, nakajima
 */
public class LocalLMNtalRuntime extends AbstractLMNtalRuntime {
	List tasks = new ArrayList();
//	protected Thread thread = new Thread(this);

	/*
	 * global ruleset id --> ruleset object��ɽ
	 */
	HashMap rulesetIDMap = new HashMap();


	public LocalLMNtalRuntime(){
		Env.theRuntime = this;
		this.runtimeid = LMNtalDaemon.makeID();	// ��������������
		this.hostname = LMNtalDaemon.getLocalHostName();
	}

	public static LocalLMNtalRuntime getInstance() {
		return Env.theRuntime;
	}
	
	/**
	/* ���ꤷ��������Ȥ���롼�������ĥ������򤳤Υ�󥿥���˺������롣
	 * @param parent �롼����ο���
	 */
	AbstractTask newTask(AbstractMembrane parent) {
		Task t = new Task(this, parent);
		tasks.add(t);
		return t;
	}

	/** �ʥޥ����������ˤ�äơˤ��Υ�󥿥���ν�λ���׵ᤵ�줿���ɤ��� */
	protected boolean terminated = false;
	/** ���Υ�󥿥���ν�λ���׵ᤵ�줿���ɤ��� */
	public boolean isTerminated() {
		return terminated;
	}
	/** ���Υ�󥿥���ν�λ���׵᤹�롣
	 * ����Ū�ˤϡ�����ʪ���ޥ����terminated�ե饰��ON�ˤ���
	 * �ƥ������Υ롼�륹��åɤ˽����褦�˸�����*/
	synchronized public void terminate() {
		terminated = true;
		Iterator it = tasks.iterator();
		while (it.hasNext()) {
			((Task)it.next()).signal();
		}
		// TODO �롼�륹��åɤ��Ф���join����ʰʲ��Υ����ɤϲ���
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
	}

	/** terminate�ե饰��ON�ˤʤ�ޤ��Ԥġ�
	 * <p>���졼�֥�󥿥���Ȥ��Ƽ¹Ԥ���Ȥ��˻��Ѥ��롣*/
	public void waitForTermination() {
		while (!terminated) {
			try {
				synchronized(this) {
					wait();
				}
			}
			catch (InterruptedException e) {}
		}
	}
	
//	/** ʪ���ޥ��󤬻��ĥ��������Ƥ�idle�ˤʤ�ޤǼ¹ԡ�<br>
//	 *  Tasks���Ѥޤ줿��˼¹Ԥ��롣�ƥ�����ͥ��ˤ��뤿��ˤ�
//	 *  ���������ڹ�¤�ˤʤäƤ��ʤ��Ƚ���ʤ���ͥ���٤Ϥ��Ф餯̤������
//	 */
//	protected void localExec() {
//		boolean allIdle;
//		do {
//			allIdle = true; // idle�Ǥʤ������������Ĥ��ä���false�ˤʤ롣
//			Iterator it = tasks.iterator();
//			while (it.hasNext()) {
//				Task task = (Task)it.next();
//				if (!task.isIdle()) { // idle�Ǥʤ������������ä���
//					task.exec(); // �ҤȤ�����¹�
//					allIdle = false; // idle�Ǥʤ�������������
//				//	break;
//				}
//			}
//		} while(!allIdle);
//	}

	/**
	 * global ruleset id --> �롼�륻�åȥ��֥������Ȥ�ɽ����Ͽ
	 * @deprecated
	 */
	boolean registerRuleset(Ruleset rs){
		String globalid = rs.getGlobalRulesetID();
		
		if(globalid != null) {
			rulesetIDMap.put(rs.getGlobalRulesetID(), rs);
			return true;
		}
		
		return false;
	}

	/**
	 * global ruleset id --> rulset object
	 */
	Ruleset getRulset(String globalRulesetID){
		Ruleset rs = (Ruleset)rulesetIDMap.get(globalRulesetID);
		
		if(rs != null) return rs;
		
		return null;
	}
}