package runtime;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import daemon.LMNtalDaemon;

/** ����VM�Ǽ¹Ԥ����󥿥���ʵ졧ʪ���ޥ��󡢵졹���׻��Ρ��ɡ�
 * ���Υ��饹�ʤޤ��ϥ��֥��饹�ˤΥ��󥹥��󥹤ϡ�1�Ĥ� Java VM �ˤĤ��⡹1�Ĥ���¸�ߤ��ʤ���
 * @author n-kato, nakajima
 */
public class LocalLMNtalRuntime extends AbstractLMNtalRuntime implements Runnable {
	/** ���ƤΥ����� */
	List tasks = new ArrayList();
	
	////////////////////////////////////////////////////////////////	

	public LocalLMNtalRuntime(){
		Env.theRuntime = this;
		this.runtimeid = LMNtalDaemon.makeID();	// ��������������
			// NIC�������äƤʤ��Ȥ����ǻ�̡�ʬ���Ȥ������ʤ����ˢ� ����� 2004-11-12
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
	
	////////////////////////////////////////////////////////////////
	
	/** �ʥޥ�����󥿥���ʤɤˤ�äơˤ��Υ�󥿥���ν�λ���׵ᤵ�줿���ɤ��� */
	protected boolean terminated = false;
	/** ���Υ�󥿥���ν�λ���׵ᤵ�줿���ɤ��� */
	public boolean isTerminated() {
		return terminated;
	}
	/** ���Υ�󥿥���ν�λ���׵᤹�롣
	 * ����Ū�ˤϡ����Υ�󥿥����terminated�ե饰��ON�ˤ���
	 * �롼�륹��åɤ���ߤ���ޤ��Ԥġ�*/
	synchronized public void terminate() {
//		if(Env.debug > 0)System.out.println("LocalLMNtalRuntime.terminate()");
		terminated = true;
//		if(Env.debug > 0)System.out.println("LocalLMNtalRuntime.terminate(): sending signal");
		interrupted = true;
		try {
//			if(Env.debug > 0)System.out.println("LocalLMNtalRuntime.terminate(): wait for thread");
			thread.join();
//			if(Env.debug > 0)System.out.println("LocalLMNtalRuntime.terminate(): joined");
		} catch (InterruptedException e) {}
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
	
	////////////////////////////////////////////////////////////////
	// �롼�륹��åɡʥ�󥿥���ˤĤ�1�ġ�
	
	/** ���Υ�󥿥���Υ롼�륹��å� */
	Thread thread = new Thread(this,"RuleThread");
	/** ���Υ�󥿥���Υ롼�륹��åɤκƼ¹Ԥ��׵ᤵ�줿���ɤ�����
	 * �ɤ߼�ꤪ���false�ν񤭹��ߤ�synchronized(this)��˸¤롣*/
	protected boolean awakened = false;	
	/** ��¾�Υ���åɤˤ�äơˤ��Υ�󥿥���Υ롼�륹��åɤμ¹���ߤ��׵ᤵ�줿���ɤ�����
	 * false�ν񤭹��ߤϤ��Υ�󥿥���Υ롼�륹��åɤ˸¤롣*/
	protected boolean interrupted = false;
	/** asyncUnlock���줿�Ȥ���true�ˤʤ��true�ʤ�Х����ʥ���������˥ȥ졼��dump�����*/
	protected boolean asyncFlag = false;
	/** ���������塼��synchronized(this)����ɤ߽񤭤��뤳�ȡ�
	 * TODO ͥ�����դ�FIFO���塼�˰ܹԡ��׵����礹�륯�饹��̵���Τǿ����˺��Τ����� */
	LinkedList taskQueue = new LinkedList();
	
	/** ����Υ������򥿥������塼������롣
	 * ���Ǥ����äƤ���Ȥ��ϲ��⤷�ʤ��Τ����ۤ��������ߤμ����ǤϽ�ʣ�������ꡢ����������ư��롣*/
	synchronized public void activateTask(Task task) {
		taskQueue.addLast(task);
		awakened = true;
		notify();
	}
	/** ���������塼�������ɤ������֤����ƽФ���˶��Ǥʤ��ʤ뤳�Ȥ⤢��Τ���դ��뤳�ȡ�*/
	synchronized public boolean isIdle() {
		return taskQueue.isEmpty();
	}
	/** ���������塼����Ƭ�����Ǥ���������֤������ΤȤ���null�����褦�ˤ����������ߤ��㳰��ȯ����*/
	synchronized public Task getNextTask() {
		return (Task)taskQueue.removeFirst();
	}
	
	/** �롼�륹��åɤμ¹ԥ����� */
	public void run() {
		Membrane root = null; // �ޥ�����󥿥���ΤȤ��Τ�����Ū�롼���줬���롣����ʳ���null
		if (this instanceof MasterLMNtalRuntime) {
			root = ((MasterLMNtalRuntime)this).getGlobalRoot();
		}
		if (root != null) { 	
			if (Env.fTrace) {
				Env.p( Dumper.dump(root) );
			}
		}
		while (true) {
			while (!interrupted) {
				if (isTerminated()) return;
				if (isIdle()) break;
				Task task = getNextTask();
				task.exec();
				if (!task.isIdle()) taskQueue.addLast(task);
			}
			interrupted = false;
			if (root != null && root.isStable()) return;
			if (isTerminated()) return;
			synchronized(this) {
				if (awakened) {
					awakened = false;
					continue;
				}
				try {
					//System.out.println("RuleThread suspended");
					wait();
					//System.out.println("RuleThread resumed");
				}
				catch (InterruptedException e) {}
				awakened = false;
			}
			if (root != null) { 	
				if (Env.fTrace) {
					if (asyncFlag) {
						asyncFlag = false;
						Env.p( " ==>* \n" + Dumper.dump(root) );
					}
				}
			}	
		}
	}
}