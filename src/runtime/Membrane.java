package runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import util.Stack;

/**
 * �������쥯�饹���¹Ի��Ρ����׻��Ρ�����ˤ������ɽ����
 * todo �ʸ�Ψ������ ��Ŭ���Ѥˡ���¹�˥롼�������Ĥ��Ȥ��Ǥ��ʤ��ʼ¹Ի����顼��Ф��ˡ֥ǡ�����ץ��饹���롣
 * @author Mizuno, n-kato
 */
public final class Membrane extends AbstractMembrane {
	/** �¹ԥ��ȥॹ���å� */
	private Stack ready = new Stack();
	
	/** ��⡼�ȥۥ��ȤȤ��̿��Ǥ�����Υ��ȥ��Ʊ�ꤹ��Ȥ��˻��Ѥ����atomid��ɽ��
	 * <p>atomid (String) -> Atom
	 * <p>������Υ���å��������塢�������Ϣ³�����å�������Τ�ͭ����
	 * ����å����������˽�������졢����³����⡼�ȥۥ��Ȥ�����׵���᤹�뤿��˻��Ѥ���롣
	 * ��⡼�ȥۥ��Ȥ�����׵�ǿ��������ȥब���������ȡ���������NEW_�򥭡��Ȥ��륨��ȥ꤬�ɲä���롣
	 * $inside_proxy���ȥ�ξ�硢̿��֥�å��������Υ����ߥ󥰤ǥ�����ID�Ǿ�񤭤���롣
	 * $inside_proxy�ʳ��Υ��ȥ�ξ�硢��å�����ޤ�NEW_�Τޤ����֤���롣
	 * @see Atom.remoteid */
	protected HashMap atomTable = new HashMap();

	//
	
	/** ���ꤵ�줿�������˽�°�������������롣newMem/newRoot ����ƤФ�롣*/
	private Membrane(AbstractTask task, AbstractMembrane parent) {
		super(task, parent);
		daemon.IDConverter.registerGlobalMembrane(getGlobalMemID(), this); // TODO �ʸ�Ψ������free���˾ä�
	}
	/** ���������ʤ����������롣Task.createFreeMembrane ����ƤФ�롣*/
	protected Membrane(Task task) {
		super(task, null);
	}

	public String getGlobalMemID() { return task.runtime.hostname + ":" + getLocalID(); }
	public String getAtomID(Atom atom) { return atom.getLocalID(); }

	///////////////////////////////
	// �ܥǥ����

	// �ܥǥ����1 - �롼������
	
//	/** �롼������ƾõ�� */
//	public void clearRules() {
//		if (remote == null) super.clearRules();
//		else remote.send("CLEARRULES",this);
//	}
//	/** srcMem�ˤ���롼��򤳤���˥��ԡ����롣 */
//	public void copyRulesFrom(AbstractMembrane srcMem) {
//		if (remote == null) super.copyRulesFrom(srcMem);
//		else remote.send("COPYRULESFROM",this,srcMem.getGlobalMemID());
//		// todo RemoteMembrane�Τ�����ʺǽ���ʳ���LOADRULESET��Ÿ�����������ˤ˹�碌��
//	}
//	/** �롼�륻�åȤ��ɲ� */
//	public void loadRuleset(Ruleset srcRuleset) {
//		if (remote == null) super.loadRuleset(srcRuleset);
//		else remote.send("LOADRULESET",this,srcRuleset.getGlobalRulesetID());
//	}

	// �ܥǥ����2 - ���ȥ�����

//	/** ���������ȥ�����������������ɲä��롣*/
//	public Atom newAtom(Functor functor) {
//		if (remote == null) return super.newAtom(functor);
//		else remote.send("NEWATOM",this,functor.toString());
//		return null;	// todo �ʤ�Ȥ������local-remote-local �����
//	}
//	/** �ʽ�°�������ʤ��˥��ȥ�򤳤�����ɲä��롣*/
//	public void addAtom(Atom atom) {
//		if (remote == null) super.addAtom(atom);
//		else remote.send("ADDATOM", this);
//	}
//	/** ���ꤵ�줿���ȥ��̾�����Ѥ��� */
//	public void alterAtomFunctor(Atom atom, Functor func) {
//		if (remote == null) super.alterAtomFunctor(atom,func);
//		else remote.send("ALTERATOMFUNCTOR", this, atom + " " + func.serialize());
//	}

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
//		if (remote != null) {
//			remote.send("NEWMEM",this);
//			return null; // todo
//		}
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
	
//	/** ���ꤵ�줿����򤳤��줫�����롣
//	 * <strike>�¹��쥹���å������ʤ���</strike>
//	 * �¹��쥹���å����Ѥޤ�Ƥ���м������� */
//	public void removeMem(AbstractMembrane mem) {
//		if (remote == null) super.removeMem(mem);
//		else remote.send("REMOVEMEM", this, mem.getGlobalMemID());
//	}
//	/** ���ꤵ�줿�Ρ��ɤǼ¹Ԥ�����å����줿�롼������������������λ���ˤ������������롣
//	 * @param node �Ρ���̾��ɽ��ʸ����
//	 * @return �������줿�롼����
//	 */
//	public AbstractMembrane newRoot(String node) {
//		if (remote == null) return super.newRoot(node);
//		else remote.send("NEWROOT", this, node);
//		return null;	// todo �ʤ�Ȥ������local-remote-local �����
//	}
	
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
	
	// - ������̿��
	
	/**
	 * ������Υ�å��������ߤ롣
	 * <p>�롼�륹��åɤ�������Υ�å����������Ȥ��˻��Ѥ��롣
	 * <p>�������������Υ�⡼�Ȥ�Ѿ����롣
	 * �������äƥ롼�륹��åɤϡ�������å�������礿�����˥�⡼�Ȥ�null�����ꤹ�뤳�ȡ�
	 * @return ��å��μ����������������ɤ��� */
	synchronized public boolean lock() {
		if (locked) {
			return false;
		} else {
			locked = true;
			if (parent != null) remote = parent.remote;
			return true;
		}
	}
	/**
	 * ������Υ�å��������ߤ롣
	 * ���Ԥ�����硢�������������륿�����Υ롼�륹��åɤ�����׵�����롣���θ塢
	 * ���Υ������������ʥ��ȯ�Ԥ���Τ��ԤäƤ��顢�Ƥӥ�å��������ߤ뤳�Ȥ򷫤��֤���
	 * <p>�롼�륹��åɰʳ��Υ���åɤ�2���ܰʹߤΥ�å��Ȥ��Ƥ�����Υ�å����������Ȥ��˻��Ѥ��롣
	 * <p>�������������Υ�⡼�Ȥ�Ѿ����롣
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
	 * <p>�����������⡼�Ȥ�null�����ꤹ�롣
	 * @return �Ĥͤ�true */
	public boolean asyncLock() {
		if (!isRoot()) parent.asyncLock();
		blockingLock();
		remote = null;
		if (isRoot()) {
			// task.async = new Async();
		}
		dequeue();
		return true;
	}

	/** ���Υ�å�����������Ƥλ�¹����Υ�å���Ƶ�Ū�˥֥�å��󥰤Ǽ������롣
	 * @return ��å��μ����������������ɤ��� */
	public boolean recursiveLock() {
		Iterator it = memIterator();
		LinkedList lockedmems = new LinkedList();
		boolean result = true;
		while (it.hasNext()) {
			AbstractMembrane mem = (AbstractMembrane)it.next();
			if (!mem.blockingLock()) {
				result = false;
				break;
			}
			if (!mem.recursiveLock()) {
				mem.unlock();
				result = false;
				break;
			}
			lockedmems.add(mem);
		}
		if (result) return true;
		it = lockedmems.iterator();
		while (it.hasNext()) {
			AbstractMembrane mem = (AbstractMembrane)it.next();
			mem.recursiveUnlock();
			mem.unlock();
		}
		return false;
	}

	// - �ܥǥ�̿��
	
	/**
	 * ��������������Υ�å���������롣�롼����ξ�硢
	 * ���μ¹��쥹���å������Ƥ�¹��쥹���å������ž������
	 * �������������륿�������Ф��ƥ����ʥ��notify�᥽�åɡˤ�ȯ�Ԥ��롣
	 * <p>lock�����blockingLock�θƤӽФ����б����롣asyncLock�ˤ�asyncUnlock���б����롣*/
	public void unlock() {
		Task task = (Task)getTask();
		if (isRoot()) {
			synchronized(task) {
				task.memStack.moveFrom(task.bufferedStack);
			}
			task.idle = false;
		}
		locked = false;
		if (isRoot()) {
			// ���Υ������Υ롼�륹��åɤޤ��Ϥ�����ߤ��Ԥäƥ֥�å����Ƥ��륹��åɤ�Ƴ����롣
			task.signal();
		}
	}
	public void forceUnlock() {
		unlock();
	}
	/** �����줫�餳�����������륿�����Υ롼����ޤǤ����Ƥ���μ���������å�������������������������롣
	 * ���μ¹��쥹���å������Ƥ�¹��쥹���å���ž�����롣�롼����ξ���unlock()��Ʊ���ˤʤ롣
	 * <p>�롼�륹��åɰʳ��Υ���åɤ��ǽ�˼���������Υ�å����������Ȥ��˻��Ѥ��롣*/
	public void asyncUnlock() {
		activate();
		AbstractMembrane mem = this;
		while (!mem.isRoot()) {
			mem.locked = false;
			mem = mem.parent;
		}
		// task.async = null;
		mem.unlock();
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
	// Membrane ����������᥽�å�
	
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

	/** ������Υ���å����ɽ���Х������������롣
	 * @see RemoteMembrane#updateCache(byte[]) */
	public byte[] cache() {
		
		// atomTable�򹹿����� // ����μ�ͳ��󥯤ˤĤ��Ƥ��׸�Ƥ
		atomTable.clear();
		Iterator it = atomIterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			atomTable.put(atom.getLocalID(), atom);
		}

		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);

			//����
			out.writeInt(mems.size());
			it = memIterator();
			while (it.hasNext()) {
				AbstractMembrane m = (AbstractMembrane)it.next();
				out.writeObject(m.getTask().getMachine().hostname);
				out.writeObject(m.getLocalID());
			}
			//���ȥ�
			out.writeInt(atoms.size());
			it = atomIterator();
			while (it.hasNext()) {
				Atom a = (Atom)it.next();
				out.writeObject(a);
			}
			//�롼�륻�å�
			out.writeInt(rulesets.size());
			it = rulesetIterator();
			while (it.hasNext()) {
				Ruleset r = (Ruleset)it.next();
				out.writeObject(r.getGlobalRulesetID());
			}
			//todo name�����ס�

			out.close();
			return bout.toByteArray();
		} catch (IOException e) {
			//ByteArrayOutputStream�ʤΤǡ�ȯ������Ϥ����ʤ�
			throw new RuntimeException("Unwxpected Exception", e);
		}
	}

	/** ���ȥ�ID���б����륢�ȥ��������� */
	public Atom lookupAtom(String atomid) {
		return (Atom)atomTable.get(atomid);
	}
	/** ���ȥ�ID���б����륢�ȥ����Ͽ���� */
	public void registerAtom(String atomid, Atom atom) {
		atomTable.put(atomid, atom);
	}
}
// todo �ڸ��ڡ�local-remote-local ���꤬��褷�����ɤ���Ĵ�٤�
