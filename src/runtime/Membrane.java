package runtime;

import java.util.Iterator;
import util.Stack;

/**
 * �������쥯�饹���¹Ի��Ρ����׻��Ρ�����ˤ������ɽ����
 * TODO ��Ŭ���Ѥˡ���¹�˥롼�������Ĥ��Ȥ��Ǥ��ʤ��ʼ¹Ի����顼��Ф��ˡ֥ǡ�����ץ��饹���롣
 * @author Mizuno, n-kato
 */
public final class Membrane extends AbstractMembrane {
	/** �¹ԥ��ȥॹ���å� */
	private Stack ready = new Stack();
	
	/**
	 * ���ꤵ�줿�������˽�°�������������롣
	 * newMem/newRoot �᥽�å���ǸƤФ�롣
	 */
	private Membrane(AbstractTask task, AbstractMembrane parent) {
		super(task, parent);
	}
	/**
	 * ���������ʤ����������롣Task.createFreeMembrane ����ƤФ�롣
	 */
	Membrane(Task task) {
		super(task, null);
	}

	String getMemID() { return getLocalID(); }
	String getAtomID(Atom atom) { return atom.getLocalID(); }
	
//	boolean isCurrent() { return getTask().memStack.peek() == this; }
	
	/** �ǥХå��� */
	String getReadyStackStatus() { return ready.toString(); }

	///////////////////////////////
	// ���

	/** �¹ԥ��ȥॹ���å�����Ƭ�Υ��ȥ����������¹ԥ��ȥॹ���å�������� */
	Atom popReadyAtom() {
		return (Atom)ready.pop();
	}
	/** 
	 * ���ꤵ�줿���ȥ�򤳤���μ¹ԥ��ȥॹ���å����ɲä��롣
	 * @param atom �¹ԥ��ȥॹ���å����ɲä��륢�ȥ�
	 */
	public void enqueueAtom(Atom atom) {
		ready.push(atom);
	}

//	/** ��γ�������������������ϥ롼����ǤϤʤ��������å����Ѥޤ�Ƥ��餺��
//	 * ���������ϲ��Ǥʤ��¹��쥹���å����Ѥޤ�Ƥ��롣
//   * �� newMem / newLocalMembrane �˰�ư���ޤ��� */
//	public void activateThis() {
//		((Task)task).memStack.push(this);
//	}

	/** ��γ����� */
	public void activate() {
		if (isQueued()) {
			return;
		}
		Task t = (Task)task;
		if (!isRoot()) {
			((Membrane)parent).activate();
			synchronized(task.getMachine()) {
				if (t.bufferedStack.isEmpty()) {
					t.memStack.push(this);
				}
				else {
					t.bufferedStack.push(this);
				}
			}
		}
		else {
			// ASSERT(t.bufferedStack.isEmpty());
			t.bufferedStack.push(this);
		}
	}

	/** �������dstMem�˰�ư�������������롣*/
	public void moveTo(AbstractMembrane dstMem) {
		if (dstMem.task.getMachine() != task.getMachine()) {
			parent = dstMem;
			//((RemoteMembrane)dstMem).send("ADDMEM",getMemID());
			throw new RuntimeException("cross-site process migration not implemented");
		}
		super.moveTo(dstMem);
	}
	/** 
	 * ��ư���줿�塢������Υ����ƥ��֥��ȥ��¹ԥ��ȥॹ���å�������뤿��˸ƤӽФ���롣
	 * <p><b>���</b>��Ruby�Ǥ�movedto�Ȱۤʤꡢ��¹����ˤ��륢�ȥ���Ф��Ƥϲ��⤷�ʤ���*/
	public void enqueueAllAtoms() {
		Iterator i = atoms.functorIterator();
		while (i.hasNext()) {
			Functor f = (Functor)i.next();
			if (f.isActive()) {
				Iterator i2 = atoms.iteratorOfFunctor(f);
				while (i2.hasNext()) {
					Atom a = (Atom)i2.next();
					a.dequeue();
					ready.push(a);
				}
			}
		}
	}
	public AbstractMembrane newMem() {
		Membrane m = new Membrane(task, this);
		mems.add(m);
		// �����Ʊ���¹��쥹���å����Ѥ�
		Task t = (Task)task;
		if (t.bufferedStack.isEmpty()) {
			t.memStack.push(m);
		}
		else {
			t.bufferedStack.push(m);
		}		
		return m;
	}
	/** newMem��Ʊ��������������ʥ᥽�åɤ��ƤФ줿������ˤϲ��Ǥʤ��¹��쥹���å����Ѥޤ�Ƥ��롣 */
	public AbstractMembrane newLocalMembrane() {
		Membrane m = new Membrane(task, this);
		mems.add(m);
		((Task)task).memStack.push(m);
		return m;		
	}
	public AbstractMembrane newRoot(AbstractMachine runtime) {
		AbstractTask task = runtime.newTask(this);
		return task.getRoot();
	}

	// ��å�
	
	/**
	 * ������Υ�å��������ߤ롣
	 * <p>�롼�륹��åɤ�������Υ�å����������Ȥ��˻��Ѥ��롣
	 * @return ��å��μ����������������ɤ��� */
	synchronized public boolean lock() {
		if (locked) {
			return false;
		} else {
			locked = true;
			return true;
		}
	}
	/**
	 * ������Υ�å��������ߤ롣
	 * ���Ԥ�����硢�������������륿�����Υ롼�륹��åɤ�����׵�����롣���θ塢
	 * ���Υ������������ʥ��ȯ�Ԥ���Τ��ԤäƤ��顢�Ƥӥ�å��������ߤ뤳�Ȥ򷫤��֤���
	 * <p>�롼�륹��åɰʳ��Υ���åɤ�������Υ�å����������Ȥ��˻��Ѥ��롣*/
	public void blockingLock() {
		if (lock()) return;
		AbstractMachine mach = task.getMachine();
		synchronized(mach) {
			((Task)task).requestLock();
			do {
				try {
					mach.wait();
				}
				catch (InterruptedException e) {}
			}
			while (!lock());
			((Task)task).retractLock();
		}
	}
	/**
	 * �����줫�餳�����������륿�����Υ롼����ޤǤ����Ƥ���Υ�å�����������¹��쥹���å��������롣
	 * <p>�롼�륹��åɰʳ��Υ���åɤ�������Υ�å����������Ȥ��˻��Ѥ��롣*/
	public void asyncLock() {
		if (!isRoot()) parent.asyncLock();
		blockingLock();
		dequeue();
	}

	/**
	 * ��������������Υ�å���������롣
	 * �롼����ξ��ޤ���signal������true�ξ�硢
	 * ���μ¹��쥹���å������Ƥ�¹��쥹���å������ž������
	 * �������������륿�������Ф��ƥ����ʥ��notify�᥽�åɡˤ�ȯ�Ԥ��롣*/
	public void unlock(boolean signal) {
		if (isRoot()) signal = true;
		if (signal) {
			Task t = (Task)task;
			synchronized(task) {
				t.memStack.moveFrom(t.bufferedStack);
			}
			t.idle = false;
		}
		locked = false;
		if (signal) {
			// ���Υ������Υ롼�륹��åɤޤ��Ϥ�����ߤ��Ԥäƥ֥�å����Ƥ��륹��åɤ�Ƴ����롣
			getTask().signal();
		}
	}
	/** �����줫�餳�����������륿�����Υ롼����ޤǤ����Ƥ���μ���������å�������������������������롣
	 * ���μ¹��쥹���å������Ƥ�¹��쥹���å���ž�����롣
	 * <p>�롼�륹��åɰʳ��Υ���åɤ��ǽ�˼���������Υ�å����������Ȥ��˻��Ѥ��롣*/
	public void asyncUnlock() {
		activate();
		AbstractMembrane mem = this;
		while (!mem.isRoot()) {
			mem.locked = false;
			mem = mem.parent;
		}
		mem.unlock();
	}
	
	/** ����������Ƥλ�¹����Υ�å���Ƶ�Ū�˥֥�å��󥰤Ǽ������롣*/
	public void recursiveLock() {
		Iterator it = memIterator();
		while (it.hasNext()) {
			AbstractMembrane mem = (AbstractMembrane)it.next();
			mem.blockingLock();
			mem.recursiveLock();
		}
	}
	/** ������������������Ƥλ�¹����Υ�å���Ƶ�Ū�˲������롣*/
	public void recursiveUnlock() {
		Iterator it = memIterator();
		while (it.hasNext()) {
			AbstractMembrane mem = (AbstractMembrane)it.next();
			mem.recursiveUnlock();
			mem.unlock();
		}
	}
}
