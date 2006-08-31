package runtime;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/** ����VM�Ǽ¹Ԥ����󥿥���ʵ졧ʪ���ޥ��󡢵졹���׻��Ρ��ɡ�
 * ���Υ��饹�ʤޤ��ϥ��֥��饹�ˤΥ��󥹥��󥹤ϡ�1�Ĥ� Java VM �ˤĤ��⡹1�Ĥ���¸�ߤ��ʤ���
 * @author n-kato, nakajima
 */
public class LocalLMNtalRuntime{

	/** ��󥿥���ID������VM�Υ����Х�ʼ��̻ҡ��롼�륻�åȤ�ID�ΰ����Ȥ��ƻ��Ѥ���� */
	protected String runtimeid;
	/** ���Υ�󥿥��बư���ۥ���̾��Fully Qualified Domain Name�Ǥ���ɬ�פ����롣 */
	protected String hostname;

	/** ��󥿥���ID��������� */
	public String getRuntimeID() {
		return runtimeid;
	}
	
	/** ���ƤΥ����� */
	List tasks = new ArrayList();
	
	////////////////////////////////////////////////////////////////	

	public LocalLMNtalRuntime(){
		Env.theRuntime = this;
//		this.runtimeid = LMNtalDaemon.makeID();	// ��������������
			// NIC�������äƤʤ��Ȥ����ǻ�̡�ʬ���Ȥ������ʤ����ˢ� ����� 2004-11-12
//		this.hostname = LMNtalDaemon.getLocalHostName();
	}

	public static LocalLMNtalRuntime getInstance() {
		return Env.theRuntime;
	}
	
	/**
	/* ���ꤷ��������Ȥ���롼�������ĥ������򤳤Υ�󥿥���˺������롣
	 * @param parent �롼����ο���
	 * @return ��������������
	 */
	Task newTask(Membrane parent) {
		Task t = new Task(this, parent);
		tasks.add(t);
		return t;
	}
	
	////////////////////////////////////////////////////////////////
	
	/** �ʥޥ�����󥿥���ʤɤˤ�äơˤ��Υ�󥿥���ν�λ���׵ᤵ�줿���ɤ��� */
	protected boolean terminated = false;
	/** ���Υ�󥿥���ν�λ���׵ᤵ�줿���ɤ��� */
	public boolean isTerminated() {
		return terminated;
	}
	/** ���Υ�󥿥���ν�λ���׵᤹�롣
	 * ����Ū�ˤϡ�����ʪ���ޥ����terminated�ե饰��ON�ˤ���
	 * �ƥ������Υ롼�륹��åɤ������ޤ��Ԥġ�*/
	synchronized public void terminate() {
//		System.out.println("LocalLMNtalRuntime#terminate()");
		terminated = true;
		Iterator it = tasks.iterator();
		while (it.hasNext()) {
			Task task = (Task)it.next();
			synchronized(task) {
				task.notifyAll();
			}
			try {
				if(Env.profile == Env.PROFILE_BYDRIVEN)
					task.outTime();
				task.thread.join();
			} catch (InterruptedException e) {}
		}
		tasks.clear();	// �ɲ� n-kato 2004-10-30
	}
	
//	/** terminate�ե饰��ON�ˤʤ�ޤ��Ԥġ�
//	 * <p>���졼�֥�󥿥���Ȥ��Ƽ¹Ԥ���Ȥ��˻��Ѥ��롣*/
//	public void waitForTermination() {
//		while (!terminated) {
//			try {
//				synchronized(this) {
//					wait();
//				}
//			}
//			catch (InterruptedException e) {}
//		}
//	}
	
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
}