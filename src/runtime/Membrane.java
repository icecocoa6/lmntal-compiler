package runtime;

import java.util.Iterator;
import java.util.LinkedList;
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
		daemon.IDConverter.registerLocalMembrane(getLocalID(), this); // TODO free���˾ä�
	}
	/**
	 * ���������ʤ����������롣Task.createFreeMembrane ����ƤФ�롣
	 */
	Membrane(Task task) {
		super(task, null);
	}

	public String getGlobalMemID() { return task.runtime.hostname + ":" + getLocalID(); }
	public String getAtomID(Atom atom) { return atom.getLocalID(); }

	///////////////////////////////
	// �ܥǥ����

	// �ܥǥ����1 - �롼������
	
	/** �롼������ƾõ�� */
	public void clearRules() {
		if (task.remote == null) super.clearRules();
		else task.remote.send("CLEARRULES",this);
	}
	/** srcMem�ˤ���롼��򤳤���˥��ԡ����롣 */
	public void copyRulesFrom(AbstractMembrane srcMem) {
		if (task.remote == null) super.copyRulesFrom(srcMem);
		else task.remote.send("COPYRULESFROM",this,srcMem.getGlobalMemID());
	}
	/** �롼�륻�åȤ��ɲ� */
	public void loadRuleset(Ruleset srcRuleset) {
		if (task.remote == null) super.loadRuleset(srcRuleset);
		else task.remote.send("LOADRULESET",this,srcRuleset.getGlobalRulesetID());
	}

	// �ܥǥ����2 - ���ȥ�����

	/** ���������ȥ�����������������ɲä��롣*/
	public Atom newAtom(Functor functor) {
		if (task.remote == null) return super.newAtom(functor);
		else task.remote.send("NEWATOM",this,functor.toString());
		return null;	// TODO �ʤ�Ȥ�����
	}
	/** �ʽ�°�������ʤ��˥��ȥ�򤳤�����ɲä��롣*/
	public void addAtom(Atom atom) {
		if (task.remote == null) super.addAtom(atom);
		else task.remote.send("ADDATOM", this);
	}
	/** ���ꤵ�줿���ȥ��̾�����Ѥ��� */
	public void alterAtomFunctor(Atom atom, Functor func) {
		if (task.remote == null) super.alterAtomFunctor(atom,func);
		else task.remote.send("ALTERATOMFUNCTOR", this, atom + " " + func);	// TODO ����
	}

	/** 
	 * ���ꤵ�줿���ȥ�򤳤���μ¹ԥ��ȥॹ���å����ɲä��롣
	 * @param atom �¹ԥ��ȥॹ���å����ɲä��륢�ȥ�
	 */
	public void enqueueAtom(Atom atom) {
		ready.push(atom);
	}
	/** �����줬��ư���줿�塢�����ƥ��֥��ȥ��¹ԥ��ȥॹ���å�������뤿��˸ƤӽФ���롣
	 * <p>Ruby�ǤǤ�movedTo(task,dstMem)��Ƶ��ƤӽФ����Ƥ�������
	 * ���塼��ľ���٤����ɤ�����Ƚ�Ǥμ�֤��ݤ��ꤹ���뤿���¹������Ф���������ѻߤ��줿�� 
	 * <p>
	 * <p>
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

	/** ���ꤵ�줿���ȥ�򤳤��줫�����롣
	 * <strike>�¹ԥ��ȥॹ���å������äƤ����硢�����å������������</strike>*/
	public void removeAtom(Atom atom) {
		if(Env.fGUI) {
			Env.gui.lmnPanel.getGraphLayout().removedAtomPos.add(atom.getPosition());
		}
		atoms.remove(atom);
		atom.mem = null;
	}
	


	/** �������dstMem�˰�ư�������������롣*/
	public void moveTo(AbstractMembrane dstMem) {
		if (dstMem.task.getMachine() != task.getMachine()) {
			parent = dstMem;
			//((RemoteMembrane)dstMem).send("ADDMEM",getGlobalMemID());
			throw new RuntimeException("cross-site process migration not implemented");
		}
		super.moveTo(dstMem);
	}

	// �ܥǥ����3 - ��������

	/** ���������������������������� */
	public AbstractMembrane newMem() {
		if (task.remote != null) {
			task.remote.send("NEWMEM",this);
			return null; // todo
		}
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
	/** newMem��Ʊ��������������ʥ᥽�åɤ��ƤФ줿������ˤϲ��Ǥʤ��¹��쥹���å����Ѥޤ�Ƥ��롣
	 * <p>��Ŭ���ѡ��������ºݤˤϺ�Ŭ���θ��̤�̵���������롣*/
	public AbstractMembrane newLocalMembrane() {
		Membrane m = new Membrane(task, this);
		mems.add(m);
		((Task)task).memStack.push(m);
		return m;		
	}
	
	/** ���ꤵ�줿����򤳤��줫�����롣
	 * <strike>�¹��쥹���å������ʤ���</strike>
	 * �¹��쥹���å����Ѥޤ�Ƥ���м������� */
	public void removeMem(AbstractMembrane mem) {
		if (task.remote == null) super.removeMem(mem);
		else task.remote.send("REMOVEMEM", this, mem.getGlobalMemID());
	}
	/** ���ꤵ�줿�Ρ��ɤǼ¹Ԥ�����å����줿�롼������������������λ���ˤ������������롣
	 * @param node �Ρ���̾��ɽ��ʸ����
	 * @return �������줿�롼����
	 */
	public AbstractMembrane newRoot(String node) {
		if (task.remote == null) return super.newRoot(node);
		else task.remote.send("NEWROOT", this, node);
		return null;	// TODO �ʤ�Ȥ�����
	}
	
	// �ܥǥ����5 - �켫�Ȥ��ư�˴ؤ������

	/** ��γ����� */
	public void activate() {
		if (isQueued()) {
			return;
		}
		Task t = (Task)task;
		if (!isRoot()) {
			((Membrane)parent).activate();
			synchronized(task) {
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
	
	// ��å��˴ؤ������ - ������̿��ϴ�������task��ľ��ž�������
	
	/**
	 * ������Υ�å��������ߤ롣
	 * <p>�롼�륹��åɤ�������Υ�å����������Ȥ��˻��Ѥ��롣
	 * @return ��å��μ����������������ɤ��� */
	synchronized public boolean lock() {
		if (locked) {
			return false;
		} else {
//			if (isRoot()) {
//				if (parent == null || parent.task.remote == null) {
//					task.remote = (RemoteTask)task;
//				}
//				else {
//					task.remote = parent.task.remote;
//				}
//			}
			locked = true;
			return true;
		}
	}
	/**
	 * ������Υ�å��������ߤ롣
	 * ���Ԥ�����硢�������������륿�����Υ롼�륹��åɤ�����׵�����롣���θ塢
	 * ���Υ������������ʥ��ȯ�Ԥ���Τ��ԤäƤ��顢�Ƥӥ�å��������ߤ뤳�Ȥ򷫤��֤���
	 * <p>�롼�륹��åɰʳ��Υ���åɤ�2���ܰʹߤΥ�å��Ȥ��Ƥ�����Υ�å����������Ȥ��˻��Ѥ��롣
	 * @return �Ĥͤ�true */
	public boolean blockingLock() {
		if (lock()) return true;
		synchronized(task) {
			((Task)task).requestLock();
			do {
				try {
					task.wait();
				}
				catch (InterruptedException e) {}
			}
			while (!lock());
			((Task)task).retractLock();
		}
		return true;
	}
	/**
	 * �����줫�餳�����������륿�����Υ롼����ޤǤ����Ƥ���Υ�å�����������¹��쥹���å��������롣
	 * <p>�롼�륹��åɰʳ��Υ���åɤ��ǽ�Υ�å��Ȥ��Ƥ�����Υ�å����������Ȥ��˻��Ѥ��롣
	 * @return �Ĥͤ�true */
	public boolean asyncLock() {
		if (!isRoot()) parent.asyncLock();
		blockingLock();
		dequeue();
		return true;
	}

	/**
	 * ��������������Υ�å���������롣
	 * �롼����ξ��ޤ���signal������true�ξ�硢
	 * ���μ¹��쥹���å������Ƥ�¹��쥹���å������ž������
	 * �������������륿�������Ф��ƥ����ʥ��notify�᥽�åɡˤ�ȯ�Ԥ��롣
	 * <p>lock�����blockingLock�θƤӽФ����б����롣asyncLock�ˤ�asyncUnlock���б����롣*/
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
	public void forceUnlock() {
		unlock();
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
	
	/** ���Υ�å�����������Ƥλ�¹����Υ�å���Ƶ�Ū�˥֥�å��󥰤Ǽ������롣
	 * @return ��å��μ����������������ɤ��� */
	public boolean recursiveLock() {
		Iterator it = memIterator();
		LinkedList lockedmems = new LinkedList();
		boolean ok = true;
		while (it.hasNext()) {
			AbstractMembrane mem = (AbstractMembrane)it.next();
			if (!mem.blockingLock()) {
				ok = false;
				break;
			}
			if (!mem.recursiveLock()) {
				mem.blockingUnlock();
				ok = false;
				break;
			}
			lockedmems.add(mem);
		}
		if (ok) return true;
		it = lockedmems.iterator();
		while (it.hasNext()) {
			AbstractMembrane mem = (AbstractMembrane)it.next();
			mem.recursiveUnlock();
			mem.unlock();
		}
		return false;
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

	///////////////////////////////	
	// LocalMembrane ����������᥽�å�
	
//	// ���줫�ɤ���
//	boolean isCurrent() { return getTask().memStack.peek() == this; }
	
	/** �ǥХå��� */
	String getReadyStackStatus() { return ready.toString(); }

	/** �¹ԥ��ȥॹ���å�����Ƭ�Υ��ȥ����������¹ԥ��ȥॹ���å�������� */
	Atom popReadyAtom() {
		return (Atom)ready.pop();
	}

//	/** ��γ�������������������ϥ롼����ǤϤʤ��������å����Ѥޤ�Ƥ��餺��
//	 * ���������ϲ��Ǥʤ��¹��쥹���å����Ѥޤ�Ƥ��롣
//	 * �� newMem / newLocalMembrane �˰�ư���ޤ��� */
//	public void activateThis() {
//		((Task)task).memStack.push(this);
//	}

	/** ������Υ���å����ɽ���Х������������� */
	public byte[] cache() {
		return new byte[0];	// TODO ����
	}
}
