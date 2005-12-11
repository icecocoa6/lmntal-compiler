package runtime;

import java.util.Iterator;

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
 * 
 * <p>
 * <b>��¾����</b>
 * <p>
 * ����������ߡ��Ƴ����Ԥ���碌�����Τ���ˡ����Υ��饹�Υ��󥹥��󥹤˴ؤ��� synchronized ������Ѥ��롣
 */

class Task extends AbstractTask implements Runnable {
	/** ���Υ������Υ롼�륹��å� */
	protected Thread thread = new Thread(this, "Task");
	/** �¹��쥹���å�*/
	Stack memStack = new Stack();
	/** ���μ¹��쥹���å� */
	Stack bufferedStack = new Stack();
	static final int maxLoop = 100;
	/** ��������ͥ���١����Τˤϡ����Υ������Υ롼�륹��åɤ�ͥ���١�
	 * <p>��å�������˻��Ѥ���ͽ�ꡣ����Ū�ˤϥ������Υ������塼��󥰤ˤ���Ѥ����ͽ�ꡣ
	 * <p>10�ʾ���ͤǤʤ���Фʤ�ʤ���*/
	int priority = 32;
	
	/** ���������ʤ��������롼���줪����б����륿�����ʥޥ����������ˤ��������
	 * @param runtime ����������������¹Ԥ����󥿥���ʤĤͤ�Env.getRuntime()���Ϥ���*/
	Task(AbstractLMNtalRuntime runtime) {
		super(runtime);
		root = new Membrane(this);
		memStack.push(root);
	}

	/** ���ꤷ���������Ŀ�������å����줿�롼���줪����б����륿�����ʥ��졼�֥������ˤ��������
	 * @param runtime ����������������¹Ԥ����󥿥���ʤĤͤ�Env.getRuntime()���Ϥ���
	 * @param parent ���� */
	Task(AbstractLMNtalRuntime runtime, AbstractMembrane parent) {
		super(runtime);
		root = new Membrane(this);
		root.lockThread = Thread.currentThread();
		root.remote = parent.remote;
		root.activate(); 		// ���μ¹��쥹���å����Ѥ�
		parent.addMem(root);	// ����������κ����������ꤷ��
		thread.start();
	}
	/** �����̵���������������Υ�����������������ˤ��롣 */
	public Membrane createFreeMembrane() {
		return new Membrane(this);
	}
	
	////////////////////////////////////////////////////////////////
	/** ���Υ������Υ롼�륹��åɤ�¹Ԥ��롣
	 * �¹Ԥ���λ����ޤ����ʤ���
	 * <p>�ޥ����������Υ롼�륹��åɤ�¹Ԥ��뤿��˻��Ѥ���롣*/
	public void execAsMasterTask() {
		thread.start();
		try {
			thread.join();
		}
		catch (InterruptedException e) {}
	}
	
	private int count = 1; // ���ֹ�ɽ��@�ȥ졼���⡼�� okabe
	private boolean trace(String arrow) {
		if (Env.fTrace) {
			if (getMachine() instanceof MasterLMNtalRuntime) {
				Membrane memToDump = ((MasterLMNtalRuntime)getMachine()).getGlobalRoot();
				// �롼��Ŭ�Ѥ�Ϣ��
				if(Env.dumpEnable) {
					if(Env.getExtendedOption("dump").equals("1")) {
						Env.p( Dumper.dump( memToDump ) );
					} else {
						System.out.print(" #" + (count++));
						Env.p( arrow + " \n" + Dumper.dump( memToDump ) );
					}
				}
			}
		}
		if (!Env.guiTrace()) return false;
		/**nakano graphic��*/
		if (!Env.graphicTrace()) return false;
		
		return true;
	}
	/** ���Υ�����������Υ롼���¹Ԥ��� */
	void exec(Membrane mem) {
		// �¹�
		for(int i=0; i < maxLoop && mem == memStack.peek() && lockRequestCount == 0; i++){
			// ���줬�Ѥ��ʤ��� & �롼�ײ����ۤ��ʤ���
			Atom a = mem.popReadyAtom();
			Iterator it = mem.rulesetIterator();
			boolean flag = false;
			if(Env.shuffle < Env.SHUFFLE_DONTUSEATOMSTACKS && a != null){ // �¹ԥ��ȥॹ���å������Ǥʤ��Ȥ�
				while(it.hasNext()){ // ����Τ�ĥ롼���a��Ŭ��
					if (((Ruleset)it.next()).react(mem, a)) {
						flag = true;
						//if (memStack.peek() != mem) break;
						break; // �롼�륻�åȤ��Ѥ�äƤ��뤫�⤷��ʤ�����
						//�롼�륻�åȤ��ɲä���Ƥ����ǽ���Ϥ��뤬������������Ϥʤ��Τ�
						//���Τޤ޽�����³���Ƥ⤤���Ȼפ��� 2005/12/08 mizuno
					}
				}
				
				if(flag){
					if (!trace("-->")) break;
				} else {
					if(!mem.isRoot()) {mem.getParent().enqueueAtom(a);} 
					// TODO �����ƥॳ���륢�ȥ�ʤ顢���줬�롼����Ǥ����ˤĤߡ�����������
				}
			}else{ // �¹ԥ��ȥॹ���å������λ�
				// ���ΤȤ��������ƥ�롼�륻�åȤ����Ƴ�ƥ��ȤǤ����¹Ԥ���ʤ���
				// ���ۤǤϡ��Ȥ߹��ߤ� + �ϥ���饤��Ÿ�������٤��Ǥ��롣
				flag = SystemRulesets.react(mem);

				if (!flag) {				
					while(it.hasNext()){ // ���Ƴ�ƥ��Ȥ�Ԥ�
						if(((Ruleset)it.next()).react(mem)) {
							flag = true;
							//if (memStack.peek() != mem) break;
							break; // �롼�륻�åȤ��Ѥ�äƤ��뤫�⤷��ʤ�����
						}
					}
				}
				
				if(flag){
					if (!trace("==>")) break;
				} else {
					memStack.pop(); // �����pop
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
				}
			}
		}

	}
	
	/** ���Υ�������ͭ�Υ롼�륹��åɤ��¹Ԥ������ */
	public void run() {
		Membrane root = null; // �롼�ȥ������ΤȤ��Τߥ롼���줬���롣����ʳ���null
		if (runtime instanceof MasterLMNtalRuntime) {
			root = ((MasterLMNtalRuntime)runtime).getGlobalRoot();
			if (root.getTask() != this) root = null;
		}
		
		if (root != null && Env.fTrace) {
			Env.p( Dumper.dump(root) );
		}
		
		LocalLMNtalRuntime r = (LocalLMNtalRuntime)runtime;
		while (true) {
			Membrane mem;
			synchronized(this) {
				while (lockRequestCount > 0 || (mem = (Membrane)memStack.peek()) == null || !mem.lock()) {
					if (r.isTerminated()) return;
					try {
						wait();
					} catch (InterruptedException e) {}
				}
				running = true;
			}
			mem.remote = null;
			if (root != null && Env.fTrace && asyncFlag) {
				//��롼�륹��åɤ��ѹ��������Ƥ���Ϥ��롣
				asyncFlag = false;
				Env.p( " ==>* \n" + Dumper.dump(root) );
			}
			exec(mem);
			mem.unlock(true);

			synchronized(this) {
				running = false;
				//���Υ���������ߤ��ԤäƤ��륹��åɤ����Ƶ�������
				notifyAll();
			}
			if (root != null && root.isStable()) break;

			// ����Υ롼��Ŭ�Ѥ�λ���Ƥ��ꡢ���줬root�줫�Ŀ������Ĥʤ顢����������
			if(memStack.isEmpty() && mem.isRoot()) {
				final AbstractMembrane memToActivate = mem.getParent();
				// ������Υ�å��������˿������������Ƥ⡢����Υ롼�뤬�������Ŭ�ѤǤ�����
				// ���줬�ƤӰ�����֤����뤳�Ȥ����뤿�ᡢ������Υ�å�������˿������������롣
				// ���ΤȤ������줬���Ǥ�̵���ˤʤäƤ�����硢�������׵��ñ���̵�뤹��Ф褤��
				// todo stable �ե饰�ν���������פ��� �� asyncLock ��ǥ�������ߤ�Ƥ���Τǡ�����ס�
				if (memToActivate != null) {
					// ����Υ�å����������ޤǥ֥�å�����Τ�
					// ���ä˥ޥ���ץ��å��Ķ��ǡˤ�ä����ʤ��Τ��̥���åɤǼ¹Ԥ��롣
					new Thread() {
						public void run() {
							if (memToActivate.asyncLock()){
								memToActivate.asyncUnlock();
							}
						}
					}.start();
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////

	// �������Υ롼�륹��åɤ��Ф�������׵�

	/** ���Υ������Υ롼�륹��åɤ��Ф�������׵��ȯ�Ԥ��Ƥ��륹��åɤθĿ�
	 * todo LinkedList��Ȥäƥ���åɤ򥭥塼�Ǵ������뤳�Ȥˤ�굲���̵������
	 * ����ˤ� Membrane#blockingLock() ���ľ�����Ȥ��ޤޤ�롣*/
	private int lockRequestCount = 0;
	private boolean running = false;
	
	public void suspend() {
		synchronized(this) {
			lockRequestCount++;
			while (running) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}
		}
	}
	public void resume() {
		synchronized(this) {
			lockRequestCount--;
			if (lockRequestCount == 0) {
				//�������򵯤�����
				//notifyAll ��ȤäƤϤ��뤬��wait ���Ƥ��륹��åɤϥ롼�륹��åɤΤߤΤϤ��Ǥ��롣
				notifyAll();
			}
		}
	}
}
