package runtime;

import java.util.Iterator;
import util.Stack;

/**
 * �������β��ˡ
 * [1] �Ʒ׻��Ρ��ɤ�����ͳ��󥯽��ϴ������ȥ�Υ����Х�ID���դ�����ˡ
 *  - newFreeLink̿�������ˡ
 *      ̿��μ��ब������
 *  - newFreeLink̿�����ʤ����
 *      newAtom�������functor�򸡺����٤���
 * [2] �Ʒ׻��Ρ��ɤ�����ͳ��󥯽��ϴ������ȥ�Υ����Х�ID���դ��ʤ���ˡ
 *  - ���ߥåȻ��˿������ȥ�β�ID���˴�������
 *    ����������ͳ��󥯽��ϴ������ȥ�μ��̤��Ǥ��ʤ��ʤ뢪NG
 *  - ���ߥåȻ��˿������ȥ�β�ID���˴����ʤ����
 *    ����Υ���å��幹�����˲�ID�������������Х�ID���ѹ������å��������������뢪�٤�����������
 * 
 * ������newFreeLink̿����ѻߡ�
 */

/**
 * �������쥯�饹���¹Ի��Ρ����׻��Ρ�����ˤ������ɽ����
 * @author Mizuno
 */
final class Membrane extends AbstractMembrane {
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
	void activate() {
		if (!isQueued()) {
			return;
		}
		if (!isRoot()) {
			((Membrane)parent).activate();
		}
		((Task)task).memStack.push(this);
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
	AbstractMembrane newMem() {
		Membrane m = new Membrane(task, this);
		mems.add(m);
		return m;
	}
	AbstractMembrane newRoot(AbstractMachine runtime) {
		AbstractTask task = runtime.newTask();
		task.getRoot().setParent(this);
		return task.getRoot();
	}
}

