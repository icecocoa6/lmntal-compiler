package runtime;

import java.util.Iterator;
import util.Stack;

/**
 * �������쥯�饹���¹Ի��Ρ����׻��Ρ�����ˤ������ɽ����
 * TODO ��Ŭ���Ѥˡ���¹�˥롼�������Ĥ��Ȥ��Ǥ��ʤ��ʼ¹Ի����顼��Ф��ˡ֥ǡ�����ץ��饹���롣
 * @author Mizuno
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
	 * ���ꤵ�줿���ȥ��¹ԥ��ȥॹ���å����ɲä��롣
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
			if (t.bufferedStack.isEmpty()) {
				t.memStack.push(this);
			}
			else {
				t.bufferedStack.push(this);
			}			
		}
		else {
			// ASSERT(t.bufferedStack.isEmpty());
			t.bufferedStack.push(this);
		}
	}

	/** dstMem�˰�ư */
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
	 * <p><b>���</b>��Ruby�Ǥ�movedto�Ȱۤʤꡢ��¹����ˤ��륢�ȥ���Ф��Ƥϲ��⤷�ʤ���
	 */
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
	 * �������������륿�����Υ�å�����������塢������Υ�å���������롣
	 * <p>�롼�륹��åɰʳ��Υ���åɤ���Υ�å����������Ȥ��˻��Ѥ��롣*/
	public void blockingLock() {
		((Task)task).lock();
		while (!lock()) {
			AbstractMachine mach = task.getMachine();
			synchronized(mach) {
				try {
					mach.wait();
				}
				catch (InterruptedException e) {}
			}
		}
	}

	/**
	 * ��������å����롣
	 * <p>�롼�륹��åɤ���Υ�å��򤹤�Ȥ��˻��Ѥ��롣
	 * @return ��å���������������true */
	synchronized public boolean lock() {
		if (locked) {
			return false;
		} else {
			locked = true;
			return true;
		}
	}
	/**
	 * ������Ȥ��λ�¹��Ƶ�Ū�˥�å����롣
	 * todo �ץ���ʸ̮�Υ��ԡ��Ȥ���������Ū����ͤ��ơ��֥�å��󥰤ǹԤ��٤��Ǥ���Ȼפ��롣
	 * @return ��å���������������true */
	public boolean recursiveLock() {
		// ��������
		return false;
	}
	/** 
	 * ����������Υ�å�����������������������륿�����Υ�å�������Ȥ����ʤ��1���餹��
	 * <p>�������Υ�å����������줿���ޤ��ϥ롼����ξ�硢����˰ʲ��ν��֤ǽ�����Ԥ�:
	 * <ul>
	 * <li>���μ¹��쥹���å������Ƥ�¹��쥹���å������ž������
	 * <li>�������򥢥��ɥ���֤Ǥʤ�����
	 * <li>��������¹Ԥ���ʪ���ޥ���˥����ʥ��ȯ�Ԥ��롣
	 * </ul>*/
	public void unlock() {
		boolean signal = ( ((Task)task).unlock() || isRoot() );
		if (signal) {
			Task t = (Task)task;
			t.memStack.moveFrom(t.bufferedStack);
			((Task)task).idle = false;
		}
		locked = false;
		if (signal) {
			AbstractMachine machine = getTask().getMachine();
			synchronized(machine) {
				machine.notify();
			}
		}
	}
	public void recursiveUnlock() {
		// ��������
	}
}

