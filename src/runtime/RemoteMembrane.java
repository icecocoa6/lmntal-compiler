package runtime;

//import java.util.HashMap;
import java.util.Iterator;

import daemon.IDConverter;

/**
 * ��⡼���쥯�饹
 * @author n-kato
 */
final class RemoteMembrane extends AbstractMembrane {
	/** ������Υ����Х�ID�ޤ���NEW_n�ʥ롼����Ǥʤ���硢����Υ�å��������Τ�ͭ����*/
	protected String globalid;
	
//	/** ������Υ��ȥफ���⡼��ID�ޤ���NEW_n�ؤμ��� (Atom -> String) */
//	protected HashMap atomids = new HashMap();
//	/** ������λ��줫���⡼��ID�ؤμ��� (AbstractMembrane -> String) */
//	protected HashMap memids = new HashMap();

	/** ����å����֤��ɤ�����
	 * �ʥ�⡼�ȤǤϥ�å�����Ƥ���ȸ��ʤ���������Ǥϥ�å���������Ƥ���Ȥߤʤ����֤Τ��ȡ�*/
	protected boolean fUnlockDeferred = false;
	
	/*
	 * ���󥹥ȥ饯����globalid��LMNtalDaemon.getGlobalMembraneID�ˤ�ä���������롣
	 */
	public RemoteMembrane(RemoteTask task, AbstractMembrane parent) {
		super(task, parent);
		this.globalid = IDConverter.getGlobalMembraneID(this);
	}

	/*
	 * ���󥹥ȥ饯����remoteid������Ū���Ϥ���
	 */
	public RemoteMembrane(RemoteTask task, RemoteMembrane parent, String remoteid) {
		super(task, parent);
		this.globalid = remoteid;
	}

	///////////////////////////////
	// ����μ���

	public String getGlobalMemID() {
		return globalid;
	}
	public String getAtomID(Atom atom) {
		//return (String) atomids.get(atom);
		return atom.remoteid;
	}

	///////////////////////////////
	// �ܥǥ����

	// �ܥǥ����1 - �롼������

	public void clearRules() {
		task.remote.send("CLEARRULES",this);
		super.clearRules();
	}

	public void copyRulesFrom(AbstractMembrane srcMem) {
		Iterator it = srcMem.rulesetIterator();
		while (it.hasNext()) {
			Ruleset rs = (Ruleset) it.next();
			task.remote.send("LOADRULESET", this, rs.getGlobalRulesetID());
		}
		super.copyRulesFrom(srcMem);
	}
	/** �롼�륻�åȤ��ɲ� */
	public void loadRuleset(Ruleset srcRuleset) {
		task.remote.send("LOADRULESET", this, srcRuleset.getGlobalRulesetID());
		super.loadRuleset(srcRuleset);
	}

	// �ܥǥ����2 - ���ȥ�����

	/** ���������ȥ�����������������ɲä���������μ¹ԥ��ȥॹ���å�������롣 */
	public Atom newAtom(Functor func) {
		Atom atom = super.newAtom(func);
		String atomid = task.remote.getNextAtomID();
		//atomids.put(atom,atomid);
		atom.remoteid = atomid;
		task.remote.send("NEWATOM", atomid, this, func.getName());
		return atom;
	}
	public void alterAtomFunctor(Atom atom, Functor func) {
		// getName�Ǥϡ����Τ�ž���ϴ��ԤǤ��ʤ�
		task.remote.send("ALTERATOMFUNCTOR", this, getAtomID(atom) + " " + func.getName());
		super.alterAtomFunctor(atom, func);
	}
	public void removeAtom(Atom atom) {
		task.remote.send("REMOVEATOM", this, getAtomID(atom));
		super.removeAtom(atom);
	}
	/** ���ꤵ�줿���ȥ�򤳤���μ¹ԥ��ȥॹ���å����Ѥ� */
	public void enqueueAtom(Atom atom) {
		String atomid = getAtomID(atom);
		if (atomid != null) { // AbstractMembrane#addAtom����θƤӽФ���̵�뤹��
			task.remote.send("ENQUEUEATOM", this, atomid);
		}
	}
	/** ��⡼�Ȥ�moveCellsFrom�ǹԤ��뤿�Ჿ�⤷�ʤ��Ƥ褤 */
	public void enqueueAllAtoms() {
	}

	// �ܥǥ����3 - ��������

	/** ������������������ */
	public AbstractMembrane newMem() {
		RemoteMembrane submem = new RemoteMembrane((RemoteTask)task, this);
		String newglobalid = task.remote.getNextMemID();
		submem.globalid = newglobalid;
		//memids.put(submem.globalid, newglobalid);
		mems.add(submem);
		task.remote.send("NEWMEM", newglobalid, this);
		//task.registerMem(newglobalid, submem.globalid);//��ž���褬�Ԥ��ΤǤ��뤫������
		return submem;
	}

	public void removeMem(AbstractMembrane mem) {
		task.remote.send("REMOVEMEM", this, mem.getGlobalMemID());
		super.removeMem(mem);
	}

	// �ܥǥ����4 - ��󥯤����

	public void newLink(Atom atom1, int pos1, Atom atom2, int pos2) {
		task.remote.send("NEWLINK", this,
			getAtomID(atom1), pos1,
			getAtomID(atom2), pos2);
		super.newLink(atom1, pos1, atom2, pos2);
	}
	public void relinkAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		task.remote.send("RELINKATOMARGS", this,
			getAtomID(atom1), pos1,
			getAtomID(atom2), pos2);
		super.relinkAtomArgs(atom1, pos1, atom2, pos2);
	}
	public void inheritLink(Atom atom1, int pos1, Link link2) {
		task.remote.send("INHERITLINK", this,
			getAtomID(atom1), pos1,
			getAtomID(link2.getAtom()), link2.getPos());
		super.inheritLink(atom1, pos1, link2);
	}
	public void unifyAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		task.remote.send("UNIFYATOMARGS", this,
			getAtomID(atom1), pos1,
			getAtomID(atom2), pos2);
		super.unifyAtomArgs(atom1, pos1, atom2, pos2);
	}
	public void unifyLinkBuddies(Link link1, Link link2) {
		task.remote.send("UNIFYLINKBUDDIES", this,
			getAtomID(link1.getAtom()), link1.getPos(),
			getAtomID(link2.getAtom()), link2.getPos());
		super.unifyLinkBuddies(link1, link2);
	}

	// �ܥǥ����5 - �켫�Ȥ��ư�˴ؤ������

	public void activate() {
		task.remote.send("ACTIVATE",this);
	}

	public void moveCellsFrom(AbstractMembrane srcMem) {
		//todo ����
		
		if (srcMem.task.getMachine() != task.getMachine()) {
			throw new RuntimeException("cross-site remote process fusion not implemented");
		}
		task.remote.send("MOVECELLS", this, srcMem.getGlobalMemID());
	}

	/** dstMem�˰�ư */
	public void moveTo(AbstractMembrane dstMem) {
		//todo ����
		
		if (dstMem.task.getMachine() != task.getMachine()) {
			throw new RuntimeException("cross-site remote process migration not implemented");
		}
		// remote call of a local process migration
		task.remote.send("MOVETO", this, dstMem.getGlobalMemID());
		super.moveTo(dstMem);
	}

	// ��å��˴ؤ������ - ������̿��ϴ�������task��ľ��ž�������
	
	synchronized public boolean lock() {
		if (locked) {
			return false;
		} else {
			if (fUnlockDeferred) {
				fUnlockDeferred = false;
			} else {
				Object obj = sendWaitObject("LOCK");
				if (obj instanceof byte[]) { // ��å���������
					updateCache((byte[])obj);
				}
				else if (obj instanceof String) {
					String response = (String)obj;
					if (response.equalsIgnoreCase("UNCHANGED")) {
						// ��å���������
					} else { // ��å���������
						return false;
					}
				}
			}
			locked = true;
			onLock();
			return true;
		}
	}
	public boolean blockingLock() {
		//todo:locked==true�ΤȤ������η׻��Ρ��ɤ�ï����å�������ʬ����ʤ��Τ򲿤Ȥ�����
		// [��å������׵�ν�����ˡ]
		// * ����å����줿����Υ�å���������Ƥ�餦�Ȥ���ɬ�ס�
		// �롼�륹��åɤ���å����Ƥ�����硢��������ID���⡼�Ȥ��Ϥ�����TODO ������ɬ�פ�����
		// ��롼�륹��åɤ���å����Ƥ�����硢��������¸�ߤ��ʤ�����
		// ͥ����̵����ȸ��ʤ��Τǥ�å��ϲ����Ǥ��ʤ����ȤˤʤäƤ���Τ�����ס�
		return lock();
	}
	public boolean asyncLock() {
		// TODO this��this.remoteid�ϸ��ߤ�ͭ���ʤΤ���
		//sendWait("ASYNCLOCK");
		return blockingLock();
	}
	public void unlock(boolean signal) {
		forceUnlock();
	}
	public void forceUnlock() {
		// send("UNLOCK", "" + false); // ���ȤǤ褯�ͤ��뤳��
		if (false && task.remote.cmdbuffer.length() == 0) {
			fUnlockDeferred = true;
		}
		else {
			task.remote.send("UNLOCK", this);
			onUnlock();
		}
	}
	public void asyncUnlock() {
		//send("ASYNCUNLOCK");
		forceUnlock();
	}
	public boolean recursiveLock() {
		return sendWait("RECURSIVELOCK");
	}
	public void recursiveUnlock() {
		sendWait("RECURSIVEUNLOCK");
	}

	///////////////////////////////	
	// RemoteMembrane ����������᥽�å�
	
	private void onLock() {
		if (isRoot()) {
			if (parent.task.remote == null) {
				task.remote = (RemoteTask)task;		// ̿��֥�å����Ѥ߾夲�򳫻Ϥ���
				task.remote.init();
			}
		}
	}
	private void onUnlock() {
		if (isRoot()) {
			if (task.remote == task) task.remote.flush();
			task.remote = null;
		}
	}
	/** ����å���򹹿����� */
	protected void updateCache(byte[] data) {
		// todo ����
		// ���ȥ�Ȼ����id�򹹿�����
	}

	///////////////////////////////
	// ������
	
	/** ��å�������ľ�����������������Ԥäƥ֥�å����롣�ʲ���*/
	boolean sendWait(String cmd) {
		String host = task.runtime.hostname;
		String msg = cmd + " " + getGlobalMemID();
		return LMNtalRuntimeManager.daemon.sendWait(host,msg);
	}
	/** ��å�������ľ�����������������Ԥäƥ֥�å����롣�ʲ���*/
	Object sendWaitObject(String cmd) {
		String host = task.runtime.hostname;
		String msg = cmd + " " + getGlobalMemID();
		return LMNtalRuntimeManager.daemon.sendWaitObject(host,msg);
	}
	
}