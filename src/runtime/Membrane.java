package runtime;

import java.util.Iterator;
import util.Stack;

/**
 * �������쥯�饹���¹Ի��Ρ����׻��Ρ�����ˤ������ɽ����
 * TODO ��Ŭ���Ѥˡ���¹�˥롼�������Ĥ��Ȥ��Ǥ��ʤ��ʼ¹Ի����顼��Ф��ˡ֥ǡ�����ץ��饹���롣
 * @author Mizuno
 */
public final class Membrane extends AbstractMembrane {
	/** �¹ԥ����å� */
	private Stack ready = new Stack();
	/**
	 * ���ꤵ�줿�������˽�°�������������롣
	 * newMem/newRoot �᥽�å���ǸƤФ�롣
	 */
	private Membrane(AbstractTask task, AbstractMembrane parent) {
		super(task, parent);
	}
	/**
	 * ���������ʤ��������������ꤵ�줿�������Υ롼����ˤ��롣
	 */
	Membrane(Task task) {
		super(task, null);
	}

	String getMemID() { return getLocalID(); }
	String getAtomID(Atom atom) { return atom.getLocalID(); }
	
	///////////////////////////////
	// ���

	/** �¹ԥ����å�����Ƭ�Υ��ȥ����������¹ԥ����å�������� */
	Atom popReadyAtom() {
		return (Atom)ready.pop();
	}
	/** 
	 * ���ꤵ�줿���ȥ��¹ԥ����å����ɲä��롣
	 * @param atom �¹ԥ����å����ɲä��륢�ȥࡣ�����ƥ��֥��ȥ�Ǥʤ���Фʤ�ʤ���
	 */
	protected void enqueueAtom(Atom atom) {
		ready.push(atom);
	}
	/** ��γ����� */
	public void activate() {
		if (!isQueued()) {
			return;
		}
		if (!isRoot()) {
			((Membrane)parent).activate();
		}
		((Task)task).memStack.push(this);
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
	 * ��ư���줿�塢������Υ����ƥ��֥��ȥ��¹ԥ����å�������뤿��˸ƤӽФ���롣
	 * <p><b>���</b>��Ruby�Ǥ�movedto�Ȱۤʤꡢ��¹����ˤ��륢�ȥ���Ф��Ƥϲ��⤷�ʤ���
	 */
	protected void enqueueAllAtoms() {
		Iterator i = atoms.functorIterator();
		while (i.hasNext()) {
			Functor f = (Functor)i.next();
			if (f.isActive()) {
				Iterator i2 = atoms.iteratorOfFunctor(f);
				while (i2.hasNext()) {
					Atom a = (Atom)i2.next();
					dequeueAtom(a);
					ready.push(a);
				}
			}
		}
	}
	public AbstractMembrane newMem() {
		Membrane m = new Membrane(task, this);
		mems.add(m);
		return m;
	}
	public AbstractMembrane newRoot(AbstractMachine runtime) {
		AbstractTask task = runtime.newTask();
		task.getRoot().setParent(this);
		return task.getRoot();
	}
}

