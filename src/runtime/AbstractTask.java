package runtime;

import java.util.Iterator;
import util.Stack;

/** ��ݥ����� */
abstract public class AbstractTask {
	/** ʪ���ޥ��� */
	protected AbstractMachine runtime;
	/** �롼���� */
	protected AbstractMembrane root;
	/** ���󥹥ȥ饯��
	 * @param runtime �¹Ԥ����ʪ���ޥ��� */
	AbstractTask(AbstractMachine runtime) {
		this.runtime = runtime;
	}
	/** ʪ���ޥ���μ��� */
	public AbstractMachine getMachine() {
		return runtime;
	}
	/** �롼����μ��� */
	public AbstractMembrane getRoot() {
		return root;
	}
}

/** ������
 * TODO �������֤ξ岼�ط��μ����򤹤롩
 * <p>
 * <b>��ˡ6</b><br>
 * ��Υ�å���������褦�Ȥ��륹��åɤϡ��ɤ���Υ�å���������Ƥ��ʤ�����
 * �ޤ��ϥ�å���������褦�Ȥ�����ο���Υ�å���������Ƥ��ʤ���Фʤ�ʤ���
 * <p>
 * �롼��Ŭ�Ѥ�Ԥ�����Υ���åɤ�롼�륹��åɤȸƤ֡�
 * ���ߥ롼�륹��åɤ�ʪ���ޥ��󤴤Ȥ�1�ĤȤʤäƤ��뤬������Ū�ˤϥ��������Ȥ�1�ĤȤ��٤��Ǥ��롣
 * <p>
 * �롼�륹��åɤϡ��롼��Ŭ�ѻ��˥������Υ�å���������褦�Ȥ��롣
 * �롼�륹��åɤϡ���Υ�å�������Υ�֥�å��󥰤ǹԤ������Ǥ˥�å�����Ƥ����������֤��롣
 * <p>
 * �롼�륹��åɤǤʤ�����åɤϡ���Υ�å�������֥�å��󥰤ǹԤ���
 * ��Υ�å�������֥�å��󥰤ǹԤ���硢���Υ������Υ�å���������롣
 * �������Υ�å������˼��Ԥ�����硢���Υ������˥�å��׵��Ԥ���
 * ��������Ʊ��ʪ���ޥ���Ǽ¹Ԥ���Ƥ����硢�����ʥ�����ǽ�������뤿��ID�����ס�
 * ��������¾��ʪ���ޥ���Ǽ¹Ԥ���Ƥ����硢�׵ḵ����åɤ�ID��Ŭ�����ꤷ���������롣
 * ���ξ�硢��å���������ȯ�����륷���ʥ������ƥ�å��κƼ�������³��������������ID�ȶ����ֿ����롣
 * <p>
 * �롼�륹��åɤǤʤ�����åɤϡ�û���֤ǥ�å���������٤��Ǥ��롣
 * �롼�륹��åɤϡ����Υ��������Ф��ƥ�å��׵᤬���ä����ˤ�ľ���˼¹Ԥ���ߤ��٤��Ǥ��롣
 * �롼�륹��åɤϡ������������륿�����ʳ��Υ������Υ�å���û���֤ǲ������٤��Ǥ��롣
 */
final class Task extends AbstractTask {
	/** �¹��쥹���å� */
	Stack memStack = new Stack();
	Stack bufferedStack = new Stack();
	boolean idle = false;
	static final int maxLoop = 100;
	/** ���������ʤ��������롼���줪����б����륿�������������
	 * @param runtime ����������������¹Ԥ���ʪ���ޥ��� */
	Task(AbstractMachine runtime) {
		super(runtime);
		root = new Membrane(this);
		memStack.push(root);
	}
	/** ���ꤷ���������Ŀ������롼���줪����б����륿�������������
	 * @param runtime ����������������¹Ԥ���ʪ���ޥ���
	 * @param parent ���� */
	Task(AbstractMachine runtime, AbstractMembrane parent) {
		super(runtime);
		root = new Membrane(this);
		root.lock();
		root.activate(); 		// ���μ¹��쥹���å����Ѥ�
		parent.addMem(root);	// ����������κ����������ꤷ��
	}
	/** �����̵���������������Υ�����������������ˤ��롣 */
	Membrane createFreeMembrane() {
		return new Membrane(this);
	}
	
	boolean isIdle(){
		return idle;
	}
	/** ���Υ�������¹Ԥ��� */
	void exec() {
		// �������Υ�å����������
		if (lockRequested || !nonblockingLock()) {
			// ����Υ�å������ˡ��������Υ�å�������Ǥ��ʤ��Ȥ�
			idle = true;
			return;
		}
		// ����Υ�å����������
		Membrane mem = (Membrane)memStack.peek();
		if(mem == null || !mem.lock()) {
			// ���줬̵�����ޤ�������Υ�å�������Ǥ��ʤ��Ȥ�
			idle = true;
			unlock();
			return;
		}
		// �¹�
		for(int i=0; i < maxLoop && mem == memStack.peek() && !lockRequested; i++){
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
						Env.p( " ==> " );
						Env.p( Dumper.dump(getRoot()) );
					}
				}// �����ƥॳ���륢�ȥ�ʤ����ˤĤߡ�����������
			}else{ // �¹��쥹���å������λ�
				flag = false;
				// ���ȥ��Ƴ��̵�����ߡ�­��������˹Ԥ�����ˡ����֤��Ѥ��Ƥߤ���
				// ����ϥ��ȥ��Ƴ�ˤ�ꡢ+ ��dequeue���줿ľ��ˡ��Ƶ��ƤӽФ���dequeue����롣
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
						mem.getParent().activate();
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
						Env.p( " ==> " );
						Env.p( Dumper.dump(getRoot()) );
					}
				}					
			}
		}
		// ���줬�Ѥ�ä�or�����������֤����顢��å���������ƽ�λ
		mem.unlock();
		unlock();
	}
	
	// ��å�
	
	/** ���Υ��������å���������åɤޤ���null */
	private Thread lockingThread = null;
	/** ��å�������� */
	private int lockCount = 0;
	/** �롼�륹��åɰʳ��Υ���åɤ����Υ������Υ�å��������׵ᤷ�Ƥ��뤫�ɤ��� */
	private boolean lockRequested = false;
	/** ���Υ������Υ�å���Υ�֥�å��󥰤Ǽ������� */
	synchronized public boolean nonblockingLock() {
		if (lockingThread == null) {
			lockingThread = Thread.currentThread();
			lockCount = 1;
			lockRequested = false;
			return true;
		}
		if (lockingThread == Thread.currentThread()) {
			lockCount++;
			return true;
		}
		return false;
	}
	/** ���Υ������Υ�å���֥�å��󥰤Ǽ���������å�������Ȥ�1���䤹��*/
	synchronized public void lock() {
		while (true) {
			if (nonblockingLock()) return;
			lockRequested = true;
			try {
				wait();
			}
			catch (InterruptedException e) {}
		}
	}
	/** ���Υ������Υ�å�������Ȥ����ʤ��1���餹��
	 * ��å�������Ȥ�0�ˤʤä���硢��å����������줿���Ȥ��̣����Τǡ�
	 * �ԤäƤ���ۤ��Υ���åɤ���å��κƼ������ߤ뤳�Ȥ��Ǥ���褦�˼�ʬ���Ȥ˥����ʥ��ȯ�Ԥ��롣
	 * @return ��å����������줿���ɤ��� */
	public boolean unlock() {
		if (lockCount == 0) return false;
		synchronized(this) {
			if (--lockCount == 0) {
				lockingThread = null;
				notify();
				return true;
			}
			return false;
		}
	}
}
