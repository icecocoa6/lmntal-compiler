package runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import util.Stack;
import daemon.IDConverter;

/**
 * �������쥯�饹���¹Ի��Ρ����׻��Ρ�����ˤ������ɽ����
 * todo �ʸ�Ψ������ ��Ŭ���Ѥˡ���¹�˥롼�������Ĥ��Ȥ��Ǥ��ʤ��ʼ¹Ի����顼��Ф��ˡ֥ǡ�����ץ��饹���롣
 * <p><b>��¾����</b></p>
 * <p>lockThread �ե�����ɤؤ������ʤ��ʤ����å��μ����������ˤȡ���å��������Ԥ���碌�Τ����
 * ���Υ��饹�Υ��󥹥��󥹤˴ؤ��� synchronized ������Ѥ��롣
 * @author Mizuno, n-kato
 */
public final class Membrane extends AbstractMembrane {
	/** �¹ԥ��ȥॹ���å���
	 * ����ݤˤ�����Υ�å���������Ƥ���ɬ�פϤʤ���
	 * ��¾����ˤϡ�Stack ���󥹥��󥹤˴ؤ��� synchronized ������Ѥ��Ƥ��롣 */
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
		daemon.IDConverter.registerGlobalMembrane(getGlobalMemID(), this);
	}
	/** ���������ʤ����������롣Task.createFreeMembrane ����ƤФ�롣*/
	protected Membrane(Task task) {
		super(task, null);
	}
	public Membrane() {
		super(null, null);
	}

	public String getGlobalMemID() { return (task==null ? "":task.runtime.hostname) + ":" + getLocalID(); }
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
	 * ���ߤ��Υ᥽�åɤ�ȤäƤ�����Ϥʤ���(2005/11/30 mizuno)
	 * <p>
	 * ��ư���줿�塢������Υ����ƥ��֥��ȥ��¹ԥ��ȥॹ���å�������뤿��˸ƤӽФ���롣
	 * <p><b>���</b>��Ruby�Ǥ�movedto�Ȱۤʤꡢ��¹����ˤ��륢�ȥ���Ф��Ƥϲ��⤷�ʤ���*/
	public void enqueueAllAtoms() {
		Iterator i = atoms.activeFunctorIterator();
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
//
//	/** ���ꤵ�줿���ȥ�򤳤��줫�����롣
//	 * <strike>�¹ԥ��ȥॹ���å������äƤ����硢�����å������������</strike>*/
//	public void removeAtom(Atom atom) {
//		if(Env.fGUI) {
//			Env.gui.lmnPanel.getGraphLayout().removedAtomPos.add(atom.getPosition());
//		}
//		atoms.remove(atom);
//		atom.mem = null;
//	}

	// �ܥǥ����3 - ��������

	/** �����������פ�k�λ��������������������� */
	public AbstractMembrane newMem(int k){
	//		if (remote != null) {
	//		remote.send("NEWMEM",this);
	//		return null; // todo
	//	}
		Membrane m = new Membrane(task, this);
		m.changeKind(k);
		mems.add(m);
		// �����Ʊ���¹��쥹���å����Ѥ�
		stack.push(m);
		return m;
	}
	/** �������ǥե���ȥ����פλ��������������������� */
	public AbstractMembrane newMem() {
		return newMem(0);
	}
	
	/** newMem��Ʊ��������������ʥ᥽�åɤ��ƤФ줿������ˤϲ��Ǥʤ��¹��쥹���å����Ѥޤ�Ƥ��롣
	 * <p>��Ŭ���ѡ��������ºݤˤϺ�Ŭ���θ��̤�̵���������롣*/
	public AbstractMembrane newLocalMembrane(int k) {
		Membrane m = new Membrane(task, this);
		m.changeKind(k);
		mems.add(m);
		((Task)task).memStack.push(m);
		return m;		
	}
	public AbstractMembrane newLocalMembrane() {
		return newLocalMembrane(0);
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

	/**
	 * ��γ�������
	 * @inheritDoc AbstractMembrane.activate()
	 */
	public void activate() {
		if (isNondeterministic()) return;
		stable = false;
		Task t = (Task)task;
		if (isRoot()) {
			dequeue();
			t.bufferedStack.push(this);
		} else {
			Stack s = ((Membrane)parent).activate2();
			if (s == t.bufferedStack) {
				dequeue();
				s.push(this);
			} else {
				//��ˡ7�Ǥϡ���������å����Ƥ������Ʊ����push����Ƥ�����Ϥʤ���
				//��ˡ8�Ǥϡ���2�Ԥ�synchronized(memStack)�������롣
				if (!isQueued())
					s.push(this);
			}
		}
	}
	/** 
	 * activate ��ǿ�������������Ȥ������Ѥ��롣
	 * ���Ǥ˥����å����Ѥޤ�Ƥ���Ȥ��ϲ����ʤ���
	 * @return �������Ǥ��Ѥޤ�Ƥ��륹���å���task.memStack �� task.bufferedStack �ΰ�����
	 */
	private Stack activate2() {
		Task t = (Task)task;
		//��ˡ7�Ǥϡ���������å����Ƥ������Ʊ����push����Ƥ�����Ϥʤ���
		//��ˡ8�Ǥϡ����β�������synchronized(memStack)�������롣
		if (isQueued())
			return stack;

		if (isRoot()) {
			// ASSERT(t.bufferedStack.isEmpty());
			t.bufferedStack.push(this);
			return t.bufferedStack;
		} else {
			Stack s = ((Membrane)parent).activate2();
			s.push(this);
			return s;
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
		if (lockThread != null) {
			return false;
		} else {
			lockThread = Thread.currentThread();
			//����줿��ϥ�å�����Ƥ���Τǡ�parent==null �ˤʤ�Τϥ����Х�롼�ȤΤ�
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
	 * ����Υ�å��Ϥ��Ǥ˼������Ƥ���ɬ�פ����롣
	 * @return ���true */
	public boolean blockingLock() {
		//����Υ�å���������Ƥ���Τǡ����������Ѳ������ꤳ���줬����줿�ꤹ����Ϥʤ���
		Task t = (Task)task;
		boolean stopped = false;
		while (true) {
			if (lockThread == t.thread) {
				//�����������Υ롼�륹��åɤ���å����Ƥ��뤫�⤷��ʤ���������׵������
				//suspend ��Ƥ���Ȥ��Ϥ��Ǥ˲����Ѥߤ��⤷��ʤ���������Ϥʤ���
				//�������˴ؤ��� synchronized ������Ѥ���Τǡ����� synchronized ����������ϤǤ��ʤ���
				t.suspend();
				stopped = true;
			}
			synchronized(this) {
				if (lock()) {
					break;
				} else {
					//��롼�륹��åɤ�ƥ������ʤ��������Τ��Ԥ�
					try {
						wait();
					} catch (InterruptedException e) {}
				}
			}
		}
		if (stopped)
			t.resume();
		return true;
	}
	/**
	 * �����줫�餳�����������륿�����Υ롼����ޤǤ����Ƥ���Υ�å�����������¹��쥹���å��������롣
	 * <p>�롼�륹��åɰʳ��Υ���åɤ��ǽ�Υ�å��Ȥ��Ƥ�����Υ�å����������Ȥ��˻��Ѥ��롣
	 * <p>�����������⡼�Ȥ�null�����ꤹ�롣
	 * @return ���������� true�����Ԥ���Τϡ������줬���Ǥ�remove����Ƥ�����Τߡ� */
	public boolean asyncLock() {
		Task t = (Task)task;
		AbstractMembrane root = t.getRoot();;
		//blockingLock �Ȱ㤤����ߤ���ޤ��Ԥ�ɬ�פ����롣
		t.suspend();
		synchronized(this) {
			while (true) {
				boolean ret = root.lock();
				if (parent == null || t != task) {
					//�������Ѳ����Ƥ������Υ���󥻥����
					if (ret) {
						root.activate();
						root.unlock();
					}
					t.resume();
	
					if (parent == null) {
						//�����줬����줿
						return false;
					} else {
						//��°���������Ѳ����Ƥ���
						t = (Task)task;
						root = t.getRoot();
						t.suspend();
					}
				} else if (ret) {
					//�롼����Υ�å��������������줫��롼����ޤǤδ֤����ƥ�å����롣
					for (AbstractMembrane mem = this; mem != root; mem = mem.parent) {
						ret = mem.lock();
						if (!ret)
							throw new RuntimeException("SYSTEM ERROR : failed to asyncLock" + mem.lockThread + mem.task);
					}
					t.resume();
					return true;
				} else {
					//�롼����Υ�å������������Τ��Ԥġ�
					//��°���������Ѳ����Ƥ��ʤ���������������뤿��ˡ������ॢ���Ȥ����ꤷ�Ƥ��롣
					try {
						wait(1);
					} catch (InterruptedException e) {}
				}
			}
		}
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
	 * ��������������Υ�å���������롣
	 * �¹��쥹���å������ʤ���
	 * �롼����ξ�硢�������������륿�������Ф��ƥ����ʥ��notify�᥽�åɡˤ�ȯ�Ԥ��롣
	 */
	public void quietUnlock() {
		Task task = (Task)getTask();
		synchronized(this) {
			lockThread = null;
			//blockingLock �ǥ֥�å����Ƥ��륹��åɤ򵯤�����
			//��ĵ������н�ʬ��
			notify();
		}
		if (isRoot()) {
			// ���Υ������Υ롼�륹��åɤ�Ƴ����롣
			// ���Υ������ʳ��Υ���åɤ�ɬ���롼���줫���å����Ƥ���Τǡ��롼����Υ�å��������˵������н�ʬ��
			// ��å���������Ƥ��餳�������ޤǤδ֤˽�°���������Ѥ�äƾ�礬���뤬���ä�����Ϥʤ���
			synchronized(task) {
				// �������ʳ��ˤ⡢suspend �᥽�å���� wait ���Ƥ��륹��åɤ����뤫�⤷��ʤ��Τǡ����Ƶ�������
				task.notifyAll();
			}
		}
	}
	
	/**
	 * ��������������Υ�å���������롣�롼����ξ�硢
	 * ���μ¹��쥹���å������Ƥ�¹��쥹���å������ž������
	 * �������������륿�������Ф��ƥ����ʥ��ȯ�Ԥ��롣
	 * <p>lock�����blockingLock�θƤӽФ����б����롣asyncLock�ˤ�asyncUnlock���б����롣*/
	public void unlock() {
		unlock(false);
	}
	
	public void unlock(boolean changed) {
		Task task = (Task)getTask();
		if (isRoot()) {
			task.memStack.moveFrom(task.bufferedStack);
		}
		quietUnlock();
		if(changed & Env.LMNgraphic != null)
			Env.LMNgraphic.setmem(this);
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
			mem.lockThread = null;
			mem = mem.parent;
		}
		task.asyncFlag = true;
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
				out.writeObject(new Boolean(m.isRoot()));
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
			//out.writeObject(name);
			out.writeObject(new Boolean(stable));

			out.close();
			return bout.toByteArray();
		} catch (IOException e) {
			//ByteArrayOutputStream�ʤΤǡ�ȯ������Ϥ����ʤ�
			throw new RuntimeException("Unexpected Exception", e);
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
	
	/** ������������� */
	public void free() {
		IDConverter.unregisterGlobalMembrane(getGlobalMemID());
	}
	
	
	/** ����饤���ѥޥ��� Any=_old/1 :- Any=_new/1 */
	public void replace1by1(Atom _old, Atom _new) {
		relink(_old, 0, _new, 0);
		removeAtom(_old);
	}
}
// todo �ڸ��ڡ�local-remote-local ���꤬��褷�����ɤ���Ĵ�٤�
