package runtime;

import java.util.Iterator;
import java.util.LinkedList;

import util.Stack;

/** ������
 * todo ��������ͥ���٤����ꤹ�롣����ϥ롼�륹��åɤˤ���å����֥�å��󥰤ˤʤ뤫�ɤ����η���˱ƶ������롣
 * ��Ʊ���¹Ԥϥ롼�륹��åɤ���⤤ͥ���٤�Ϳ���롣
 * todo �롼���줬���ĥ������ޤ��ϼ��������Υ롼�륹��åɤˤ�äƥ�å����줿���ɤ����򵭲����롣
 * ����ˤ�ꡢ���ĥ������Υ롼�륹��åɤ���ߤ���ư�����Ʊ���¹Ԥ���ǽ�Ȥʤ롣
 * <p>
 * <font size=+1><b>��ˡ6</b></font><br>
 * 
 * <p>
 * <b>���Ѿ��</b>
 * <p>
 * ��Υ�å���������褦�Ȥ��륹��åɤϡ��ɤ���Υ�å���������Ƥ��ʤ�����
 * �ޤ��ϥ�å���������褦�Ȥ�����ο���Υ�å���������Ƥ��ʤ���Фʤ�ʤ���
 * <p>
 * �롼�륹��åɰʳ��Υ���åɤϡ��ǽ�˥롼����Υ�å���������ʤ���Фʤ�ʤ���
 * 
 * <p>
 * <b>�롼�륹��å�</b>
 * <p>
 * �롼��Ŭ�Ѥ�Ԥ�����Υ���åɤ�롼�륹��åɤȸƤ֡�
 * �������Ϥ��礦��1�ĤΥ롼�륹��åɤˤ�äƼ¹Ԥ���롣
 * <strike>�����Ǥϥ롼�륹��åɤ�ʪ���ޥ��󤴤Ȥ�1��¸�ߤ����������ˤ�äƶ�ͭ����Ƥ��롣</strike>
 * ���ߡ��ƥ���������ͭ�Υ롼�륹��åɤ���Ĥ褦�ˤʤäƤ��롣
 * <p>
 * �ƥ���åɤϡ��롼�륹��åɤ��Ф�������׵��ȯ�Ԥ�������뤳�Ȥ��Ǥ��롣
 * �롼�륹��åɤϡ�����׵���Υ���åɤ�¸�ߤ���Ȥ��ϥ롼��Ŭ�Ѥ�Ԥ�ʤ���
 * �롼�륹��åɤϡ�����׵���Τ����ľ���˥롼��Ŭ�Ѥ���ߤ�����ʬ���Ȥ˥����ʥ��ȯ�Ԥ��롣
 * <p>
 * �����Ǥϡ��롼�륹��åɤϡ���Υ�å�������Υ�֥�å��󥰤ǹԤ���
 * ���Τ��ᡢ�줬���Ǥ˥�å�����Ƥ�����硢��������Ф���롼��Ŭ�Ѥ�Ԥ�ʤ���
 * ����ϡ���������ͥ���٤��ߤ��뤳�Ȥˤ��֥�å��󥰤Ǥμ������ǽ�ˤ��뤳�Ȥˤ���褹�٤��Ǥ��롣
 * 
 * <p>
 * <b>�롼�륹��åɤǤʤ�����å�</b>
 * <p>
 * �롼�륹��åɤǤʤ�����åɤϡ���Υ�å�������֥�å��󥰤ǹԤ���
 * �줬��å�����Ƥ����硢�������Υ롼�륹��åɤ��Ф�������׵��ȯ�Ԥ����塢
 * ���Υ롼�륹��åɤ������ʥ��ȯ�Ԥ���Τ��Ԥäƺ�����Υ�å��������ߤĤŤ��롣
 * <p>
 * �롼�륹��åɤǤʤ�����åɤϡ�û���֤ǥ�å���������٤��Ǥ��롣
 * �롼�륹��åɤϡ����Υ��������Ф��ƥ�å��׵᤬���ä����ˤ�ľ���˼¹Ԥ���ߤ��٤��Ǥ��롣
 * �롼�륹��åɤϡ������������륿�����ʳ��Υ������Υ�å����������ץ���ʸ̮�ΤȤ��Τ�ɬ�ס�
 * ��û���֤ǲ������٤��Ǥ��롣
 * 
 * <p>
 * <b>�ƥ���������γ�����</b>
 * <p>
 * ��Ʊ������Υ�å���������뤳�Ȥˤ��¹��쥹���å��򹹿����뤳�Ȥˤ��¸����롣�����Ѥߡ��ƥ��Ⱥѡ�
 * 
 * <p>
 * <b>�����ƥॳ����</b>
 * <p>
 * ��Ʊ������Υ�å���������뤳�Ȥˤ��¸���ǽ��
 * ����������μ������֥�å��󥰤ˤʤ�褦�˥�������ͥ���٤��ߤ���ɬ�פ�����Ϥ��Ǥ��롣
 * �����ƥॳ����ϸ��ߤϤޤ���������Ƥ��ʤ���
 */

class Task extends AbstractTask {
	/** �¹��쥹���å����ɤ߽񤭤�synchronized(this)��˸¤롣*/
	Stack memStack = new Stack();
	/** ���μ¹��쥹���å� */
	Stack bufferedStack = new Stack();
	static final int maxLoop = 100;
	/** ���줬¸�ߤ��ʤ����Ȥ�Τ��᤿�����ޤ���������¹��Υ�å��������Ǥ��ʤ����ᡢ
	 * �롼�륹��åɤ�Ŭ�ѤǤ���롼�뤬̵���ä��Ȥ���true��
	 * <p>false�ν񤭹��ߤ�synchronized(this)��˸¤롣*/
	boolean idle = false;
	
	boolean isIdle(){
		return idle;
	}
	
	/** ���������ʤ��������롼���줪����б����륿�����ʥޥ����������ˤ��������
	 * @param runtime ����������������¹Ԥ����󥿥���ʤĤͤ�Env.getRuntime()���Ϥ���*/
	Task(AbstractLMNtalRuntime runtime) {
		super(runtime, Task.ROOT_PRIORITY);
		root = new Membrane(this);
		memStack.push(root);
	}

	/** ���ꤷ���������Ŀ�������å����줿�롼���줪����б����륿�����ʥ��졼�֥������ˤ��������
	 * @param runtime ����������������¹Ԥ����󥿥���ʤĤͤ�Env.getRuntime()���Ϥ���
	 * @param parent ���� */
	Task(AbstractLMNtalRuntime runtime, AbstractMembrane parent) {
		super(runtime, parent.getTask().getPriority() - Task.PRIORITY_DELTA);
		root = new Membrane(this);
		root.lockThread = Thread.currentThread();
		root.remote = parent.remote;
		root.activate(); 		// ���μ¹��쥹���å����Ѥ�
		parent.addMem(root);	// ����������κ����������ꤷ��
//		thread.start();
	}
	/** �����̵���������������Υ�����������������ˤ��롣 */
	public Membrane createFreeMembrane() {
		return new Membrane(this);
	}
	
	////////////////////////////////////////////////////////////////

	/** ���Υ�����������Υ롼���¹Ԥ��� */
	void exec() {
		Membrane mem; // ����
		//System.out.println(getRoot().getAtomCount() + ": exec1 ");
		synchronized(this) {
			// �롼�륹��åɡʸ��ߤΥ���åɡˤ��Ф��Ƥ��Υ������Υ�å��׵᤬����Ȥ��ϲ��⤷�ʤ�
			if (!lockRequestQueue.isEmpty()) {
				idle = true;
				return;
			}
			// ����Υ�å����������
			mem = (Membrane)memStack.peek();
			if(mem == null || !mem.lock()) {
				// ���줬̵�����ޤ�������Υ�å�������Ǥ��ʤ��Ȥ�
//				if (Env.debug > 7) if (mem != null) Env.p("cannot acquire lock");
				idle = true;
				return;
			}
			mem.remote = null;
		}
		//System.out.println(mem.getAtomCount() + ": exec2");
		// �¹�
		AbstractMembrane memToActivate = null;
		for(int i=0; i < maxLoop && mem == memStack.peek(); i++){
			synchronized(this) {
				if (!lockRequestQueue.isEmpty()) break;
			}
			// ���줬�Ѥ��ʤ��� & �롼�ײ����ۤ��ʤ���
//			System.out.println("mems  = " + memStack);
//			System.out.println("atoms = " + mem.getReadyStackStatus());
			Atom a = mem.popReadyAtom();
			Iterator it = mem.rulesetIterator();
			boolean flag;
//			mem.atoms.print();
			if(a != null){ // �¹ԥ��ȥॹ���å������Ǥʤ��Ȥ�
				flag = false;
				while(it.hasNext()){ // ����Τ�ĥ롼���a��Ŭ��
					if (((Ruleset)it.next()).react(mem, a)) {
						flag = true;
						//if (memStack.peek() != mem) break;
						break; // �롼�륻�åȤ��Ѥ�äƤ��뤫�⤷��ʤ�����
					}
				}
				if(flag == false){ // �롼�뤬Ŭ�ѤǤ��ʤ��ä���
					if(!mem.isRoot()) {mem.getParent().enqueueAtom(a);} 
				}
				else {
					if (Env.fTrace) {
						if (getMachine() instanceof MasterLMNtalRuntime) {
							Membrane memToDump = ((MasterLMNtalRuntime)getMachine()).getGlobalRoot();
							// memToDump = getRoot();
//							if (memToDump == getRoot()) // Dumper������å�����褦�ˤʤ�ޤǤβ�����
								Env.p( " --> \n" + Dumper.dump( memToDump ) );
						}
					}
					if (!Env.guiTrace()) break;
				}// �����ƥॳ���륢�ȥ�ʤ����ˤĤߡ�����������
			}else{ // �¹ԥ��ȥॹ���å������λ�
				flag = false;
				// ���ȥ��Ƴ�ƥ��Ȥ��ʤ��Ȥ���­��������˹Ԥ�����ˡ����֤��Ѥ��Ƥߤ���
				// ���ȥ��Ƴ�ƥ��Ȥ���Ȥ��ϡ�+ ����˼¹Ԥ����塢�Ƶ��ƤӽФ����¹Ԥ���롣
				// ���ۤǤϡ��Ȥ߹��ߤ� + �ϥ���饤��Ÿ�������٤��Ǥ��롣
				{
					int debugvalue = Env.debug; // todo spy��ǽ���������
					if (Env.debug < Env.DEBUG_SYSTEMRULESET) Env.debug = 0;
					flag = SystemRuleset.getInstance().react(mem);
					Env.debug = debugvalue;
				}
				if (flag == false) {				
					while(it.hasNext()){ // ���Ƴ�ƥ��Ȥ�Ԥ�
						if(((Ruleset)it.next()).react(mem)) {
							flag = true;
							//if (memStack.peek() != mem) break;
							break; // �롼�륻�åȤ��Ѥ�äƤ��뤫�⤷��ʤ�����
						}
					}
				}
				if(flag == false){ // �롼�뤬Ŭ�ѤǤ��ʤ��ä���
					memStack.pop(); // �����pop
					// ���줬root�줫�Ŀ������Ĥʤ顢����������
					if(mem.isRoot() && mem.getParent() != null) {
						memToActivate = mem.getParent();
					}
					if (!mem.perpetual) {
						// ���줬����stable�ʤ顢�������stable�ˤ��롣
						it = mem.memIterator();
						flag = false;
						while(it.hasNext()){
							if(((AbstractMembrane)it.next()).isStable() == false)
								flag = true;
						}
						if(flag == false) mem.toStable();
					}
				} else {
					if (Env.fTrace) {
						if (getMachine() instanceof MasterLMNtalRuntime) {
							Membrane memToDump = ((MasterLMNtalRuntime)getMachine()).getGlobalRoot();
							// memToDump = getRoot();
//							if (memToDump == getRoot()) // Dumper������å�����褦�ˤʤ�ޤǤβ�����
								Env.p( " ==> \n" + Dumper.dump( memToDump ) );
						}
					}
					if (!Env.guiTrace()) break;
				}
			}
		}
		// ���줬�Ѥ�ä�or�����������֤����顢��å���������ƽ�λ
		synchronized(this) {
			mem.unlock();
			if (!lockRequestQueue.isEmpty()) {
				signal();
			}
			if (memStack.isEmpty()) {
				idle = true;
			}
		}
		//System.out.println(mem.getAtomCount() + ": exec3");
		
		// ������Υ�å��������˿������������Ƥ⡢����Υ롼�뤬�������Ŭ�ѤǤ�����
		// ���줬�ƤӰ�����֤����뤳�Ȥ����뤿�ᡢ������Υ�å�������˿������������롣
		// ���ΤȤ������줬���Ǥ�̵���ˤʤäƤ�����硢�������׵��ñ���̵�뤹��Ф褤��
		if (memToActivate != null) {
			new Thread() {
				AbstractMembrane mem;
				public void run() {
					if (mem.asyncLock())
						mem.asyncUnlock();
				}
				public void activate(AbstractMembrane mem) {
					this.mem = mem;
					start();
				}
			}.activate(memToActivate);
		}
	}
//	/** ���Υ�������ͭ�Υ롼�륹��åɤ��¹Ԥ������ */
//	public void run() {
//		Membrane root = null; // �롼�ȥ������ΤȤ��Τߥ롼���줬���롣����ʳ���null
//		if (runtime instanceof MasterLMNtalRuntime) {
//			root = ((MasterLMNtalRuntime)runtime).getGlobalRoot();
//			if (root.getTask() != this) root = null;			
//		}
//		if (root != null) { 	
//			if (Env.fTrace) {
//				Env.p( Dumper.dump(root) );
//			}
//		}
//		while (true) {
//			while (true) {
//				exec();
//				if (((LocalLMNtalRuntime)runtime).isTerminated()) return;
//				if (root != null && root.isStable()) return;
//				if (!isIdle()) continue;
//				synchronized(this) {
//					if (awakened) {
//						awakened = false;
//						continue;
//					}
//					try {
//						//System.out.println(getRoot().getAtomCount() + " Thread suspended");
//						wait();
//						//System.out.println(getRoot().getAtomCount() + " Thread resumed");
//					}
//					catch (InterruptedException e) {}
//					awakened = false;
//				}
//				break;
//			}
//			if (root != null) { 	
//				if (Env.fTrace) {
//					if (asyncFlag) {
//						asyncFlag = false;
//						Env.p( " ==>* \n" + Dumper.dump(root) );
//					}
//				}
//			}
//		}	
//	}

	////////////////////////////////////////////////////////////////

	// �롼�륹��åɤ��Ф��뤳�Υ������μ¹Ԥ�����׵�

	/** ���Υ������Υ롼������å����褦�Ȥ��ƥ֥�å����Ƥ���Thread�Υꥹ�ȡ�
	 * synchronized(this)����ɤ߽񤭤��뤳�ȡ�*/
	LinkedList lockRequestQueue = new LinkedList();

	/** ���Υ������Υ롼����Υ�å���������롣�����Ǥ���ޤǥ֥�å����롣
	 * <p>Membrane.blockingLock ����ƤФ�롣*/
	synchronized public void lockRootMembrane() {
		Thread me = Thread.currentThread();
		synchronized(root) {
			if (root.lock()) return;
			lockRequestQueue.addLast(me);
		}
		do {
			do {
				try {
					wait();
				}
				catch (InterruptedException e) {}
			} while (lockRequestQueue.getFirst() != me);
		} while (!root.lock());
		lockRequestQueue.removeFirst();
	}
//	/** ���Υ������Υ롼����Υ�å��������롣*/
//	synchronized public void unlockRootMembrane() {
//		lockRequestQueue.removeFirst();
//		Object next = lockRequestQueue.getFirst();
//		if (next != null) next.notify();
//	}

	////////////////////////////////////////////////////////////////
	
	/** ���Υ��������Ф��ƥ����ʥ��ȯ�Ԥ��롣
	 * ���ʤ�������Υ������Υ롼����Υ�å��μ����򤹤뤿��˥֥�å����Ƥ��륹��åɤ�¸�ߤ���ʤ��
	 * ���Υ���åɤ�Ƴ����ƥ�å��μ������ߤ뤳�Ȥ��׵ᤷ��
	 * ¸�ߤ��ʤ��ʤ�Ф��Υ������Υ롼�륹��åɤκƼ¹Ԥ��׵᤹�롣*/
	synchronized public final void signal() {
		if (!lockRequestQueue.isEmpty()) {
			notifyAll();
		} else {
			((LocalLMNtalRuntime)runtime).activateTask(this);
		}
	}
}
