package runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** ��ݥ�󥿥���ʵ졧���ʪ���ޥ��󡢵졹����ݷ׻��Ρ��ɡ˥��饹��*/

abstract class AbstractLMNtalRuntime {
	protected String runtimeid;
	/** ���Υ�󥿥���˿��������ʤ���å�����Ƥ��ʤ��롼���������������Ǥʤ��¹��쥹���å����Ѥࡣ*/
	abstract AbstractTask newTask(AbstractMembrane parent);
	/** ���Υ�󥿥���μ¹Ԥ�λ���� */
	abstract public void terminate();
	
//	/** ��󥿥���Υ롼�륹��åɤ��Ф��ƺƼ¹Ԥ��׵᤹�롣*/
//	abstract public void awake();
}

/** ����VM�Ǽ¹Ԥ����󥿥���ʵ졧ʪ���ޥ��󡢵졹���׻��Ρ��ɡ�
 * ���Υ��饹�Υ��֥��饹�Υ��󥹥��󥹤ϡ�1�Ĥ� Java VM �ˤĤ��⡹1�Ĥ���¸�ߤ��ʤ���
*/
class LocalLMNtalRuntime extends AbstractLMNtalRuntime /*implements Runnable */{
	List tasks = new ArrayList();
//	protected Thread thread = new Thread(this);
	

	AbstractTask newTask(AbstractMembrane parent) {
		Task t = new Task(this,parent);
		tasks.add(t);
		return t;
	}
	/** �ʥޥ����������ˤ�äơˤ��Υ�󥿥���ν�λ���׵ᤵ�줿���ɤ��� */
	protected boolean terminated = false;
	/** ���Υ�󥿥���ν�λ���׵᤹�롣
	 * ����Ū�ˤϡ�����ʪ���ޥ����terminated�ե饰��ON�ˤ���
	 * �ƥ������Υ롼�륹��åɤ˽����褦�˸��� */
	synchronized public void terminate() {
		terminated = true;
		Iterator it = tasks.iterator();
		while (it.hasNext()) {
			((Task)it.next()).signal();
		}
		// TODO join����
		// ���졼�֥�󥿥���ʤ�С�VM��λ���롣
		// if (!(this instanceof MasterLMNtalRuntime)) System.exit(0);
	}
	/** ���Υ�󥿥���ν�λ���׵ᤵ�줿���ɤ��� */
	public boolean isTerminated() {
		return terminated;
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
}

/** ��󥿥��॰�롼�פ���ӥ����Х�롼����������������������󥿥���Υ��饹 */

public final class MasterLMNtalRuntime extends LocalLMNtalRuntime {
	private Membrane globalRoot;	// masterTask�ؤλ��Ȥ��������ʬ����䤹�����⤷��ʤ�

	public MasterLMNtalRuntime(){
		Task masterTask = new Task(this);
		tasks.add(masterTask);
		globalRoot = (Membrane)masterTask.getRoot();
		// Inline
		Inline.initInline();
		Env.theRuntime = this;
	}

//	/**
//	 * �������Ŭ�Ѥ���롼���globalRoot���Ŭ�Ѥ��롣
//	 * ������롼�롢�����REPL���������������������롼���Ŭ�Ѥ��뤿��˻��Ѥ��롣
//	 * @deprecated
//	 */
//	public void applyRulesetOnce(Ruleset r){
//		r.react(globalRoot);
//	}
	
	public final Membrane getGlobalRoot(){
		return globalRoot;
	}
	public final Task getMasterTask(){
		return (Task)globalRoot.getTask();
	}
//	/** �ޥ�����󥿥���Ȥ��Ƽ¹Ԥ��� */
//	public void run() {
//		RemoteLMNtalRuntime.init();
//		while (true) {
//			if (Env.fTrace) {
//				Env.p( Dumper.dump(getGlobalRoot()) );
//			}
//			localExec();
//			if (globalRoot.isStable()) break;
//			synchronized(this) {
//				try {
//					if (terminated) break;
//					wait();
//				}
//				catch (InterruptedException e) {}
//			}
//		}
//		RemoteLMNtalRuntime.terminateAll();
//	}
}

