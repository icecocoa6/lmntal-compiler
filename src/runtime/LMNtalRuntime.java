package runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** ���ʪ���ޥ������ݷ׻��Ρ��ɡ˥��饹 */
abstract class AbstractMachine {
	protected String runtimeid;
	/** ����ʪ���ޥ���˿��������ʤ���å�����Ƥ��ʤ��롼���������������Ǥʤ��¹��쥹���å����Ѥࡣ*/
	abstract AbstractTask newTask();
	/** ����ʪ���ޥ���˻���ο������ĥ�å����줿�롼���������������μ¹��쥹���å����Ѥࡣ*/
	abstract AbstractTask newTask(AbstractMembrane parent);
	/** ����ʪ���ޥ���μ¹Ԥ�λ���� */
	abstract public void terminate();
	
	/** ���η׻��Ρ��ɤΥ롼�륹��åɤ��Ф��ƺƼ¹Ԥ��׵᤹�롣*/
	abstract public void awake();
}

/** ʪ���ޥ��� */
class Machine extends AbstractMachine implements Runnable {
	List tasks = new ArrayList();
	protected Thread thread = new Thread(this);
	
	AbstractTask newTask() {
		Task t = new Task(this);
		tasks.add(t);
		return t;
	}
	AbstractTask newTask(AbstractMembrane parent) {
		Task t = new Task(this,parent);
		tasks.add(t);
		return t;
	}
	/** ����ʪ���ޥ���Υ롼�륹��åɤκƼ¹Ԥ��׵ᤵ�줿���ɤ��� */
	protected boolean awakened = false;
	/** �ʥޥ������׻��Ρ��ɤˤ�äơˤ���ʪ���ޥ���ν�λ���׵ᤵ�줿���ɤ��� */
	protected boolean terminated = false;
	/** ����ʪ���ޥ���Υ롼�륹��åɤκƼ¹Ԥ��׵᤹�� */
	synchronized public void awake() {
		awakened = true;
		notify();
	}
	/** ����ʪ���ޥ���ν�λ���׵᤹�� */
	synchronized public void terminate() {
		terminated = true;
		notify();
	}
	/** ʪ���ޥ��󤬻��ĥ��������Ƥ�idle�ˤʤ�ޤǼ¹ԡ�<br>
	 *  Tasks���Ѥޤ줿��˼¹Ԥ��롣�ƥ�����ͥ��ˤ��뤿��ˤ�
	 *  ���������ڹ�¤�ˤʤäƤ��ʤ��Ƚ���ʤ���ͥ���٤Ϥ��Ф餯̤������
	 */
	protected void localExec() {
		boolean allIdle;
		do {
			allIdle = true; // idle�Ǥʤ������������Ĥ��ä���false�ˤʤ롣
			Iterator it = tasks.iterator();
			while (it.hasNext()) {
				Task task = (Task)it.next();
				if (!task.isIdle()) { // idle�Ǥʤ������������ä���
					task.exec(); // �ҤȤ�����¹�
					allIdle = false; // idle�Ǥʤ�������������
				//	break;
				}
			}
		} while(!allIdle);
	}
	/** ���졼�ַ׻��Ρ��ɤȤ��Ƽ¹Ԥ��� */
	public void run() {
		while (true) {
			localExec();
			synchronized(this) {
				if (terminated) break;
				if (awakened) {
					awakened = false;
					continue;
				}
				try {
					wait();
				}
				catch (InterruptedException e) {}
			}
		}
	}
	/** ����ʪ���ޥ����¹Ԥ��� */
	public void exec() {
		thread.start();
		try {
			thread.join();
		}
		catch (InterruptedException e) {}
	}
}

/** �����Х�롼�Ȥ��������ʪ���ޥ��� */
public final class LMNtalRuntime extends Machine {
	protected Membrane globalRoot;
	
	public LMNtalRuntime(){
		AbstractTask t = newTask();
		globalRoot = (Membrane)t.getRoot();
		// Inline
		Inline.initInline();
	}

//	/**
//	 * �������Ŭ�Ѥ���롼���globalRoot���Ŭ�Ѥ��롣
//	 * ������롼�롢�����REPL���������������������롼���Ŭ�Ѥ��뤿��˻��Ѥ��롣
//	 * @deprecated
//	 */
//	public void applyRulesetOnce(Ruleset r){
//		r.react(globalRoot);
//	}
	
	public Membrane getGlobalRoot(){
		return globalRoot;
	}
//	/**@deprecated*/
//	public Membrane getRoot(){
//		return globalRoot;
//	}
	/** �ޥ������׻��Ρ��ɤȤ��Ƽ¹Ԥ��� */
	public void run() {
		RemoteMachine.init();
		while (true) {
			localExec();
			if (globalRoot.isStable()) break;
			synchronized(this) {
				try {
					if (terminated) break;
					wait();
				}
				catch (InterruptedException e) {}
			}
		}
		RemoteMachine.terminateAll();
	}
}

