package runtime;

//import java.util.HashMap;
import java.util.Iterator;

//import daemon.IDConverter;

/**
 * ��⡼���쥯�饹
 * @author n-kato
 */
public final class RemoteMembrane extends AbstractMembrane {
	/** ������Υ����Х�ID�ޤ���NEW_n
	 * �ʥ롼����䵼����Ǥʤ���硢�����Ϣ³�����å�����������Τ�ͭ����
	 * <p>Atom��remoteid ���б����Ƥ��� */
	protected String globalid;
	
//	/** ������Υ��ȥफ���⡼��ID�ޤ���NEW_n�ؤμ��� (Atom -> String) */
//	protected HashMap atomids = new HashMap();	// Atom.remoteid �˰ܴ�
//	/** ������λ��줫���⡼��ID�ؤμ��� (AbstractMembrane -> String) */
//	protected HashMap memids = new HashMap();	// Membrane.globalid �˰ܴ�

	/** ����å����֤��ɤ�������ñ�Τ��ᡢ�롼����Τߤ�����å����֤ˤʤ���ΤȤ��롣
	 * �ʥ�⡼�ȤǤϥ�å�����Ƥ���ȸ��ʤ���������Ǥϥ�å���������Ƥ���Ȥߤʤ����֤Τ��ȡ�*/
	private boolean fUnlockDeferred = false;
	
	////////////////////////////////////////////////////////////////
	
	/**
	 * ���󥹥ȥ饯����globalid�Ͻ�������ʤ��ʺ��ä��á�
	 */
	public RemoteMembrane(RemoteTask task, AbstractMembrane parent) {
		super(task, parent);
	}
	/**
	 * ���󥹥ȥ饯����remoteid������Ū���Ϥ���
	 */
	public RemoteMembrane(RemoteTask task, RemoteMembrane parent, String remoteid) {
		super(task, parent);
		this.globalid = remoteid;
	}
	/**
	 * ���������ʤ����������롣RemoteLMNtalRuntime.createPseudoMembrane ����ƤФ�롣
	 * @see RemoteLMNtalRuntime#createPseudoMembrane() */
	protected RemoteMembrane(RemoteTask task) {
		super(task, null);
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
		remote.send("CLEARRULES",this);
		super.clearRules();
	}

	public void copyRulesFrom(AbstractMembrane srcMem) {
		Iterator it = srcMem.rulesetIterator();
		while (it.hasNext()) {
			Ruleset rs = (Ruleset) it.next();
			remote.send("LOADRULESET", this, rs.getGlobalRulesetID());
		}
		super.copyRulesFrom(srcMem);
	}
	/** �롼�륻�åȤ��ɲ� */
	public void loadRuleset(Ruleset srcRuleset) {
		remote.send("LOADRULESET", this, srcRuleset.getGlobalRulesetID());
		super.loadRuleset(srcRuleset);
	}

	// �ܥǥ����2 - ���ȥ�����

	/** ���������ȥ�����������������ɲä���������μ¹ԥ��ȥॹ���å�������롣 */
	public Atom newAtom(Functor func) {
		Atom atom = super.newAtom(func);
		String atomid = remote.generateNewID();
		//atomids.put(atom,atomid);
		atom.remoteid = atomid;
		remote.send("NEWATOM", atomid, this, func.serialize());
		if (func.equals(Functor.INSIDE_PROXY)) remote.registerAtom(atomid, atom);
		return atom;
	}
	public void alterAtomFunctor(Atom atom, Functor func) {
		remote.send("ALTERATOMFUNCTOR", this, getAtomID(atom) + " " + func.serialize());
		super.alterAtomFunctor(atom, func);
	}
	public void removeAtom(Atom atom) {
		remote.send("REMOVEATOM", this, getAtomID(atom));
		super.removeAtom(atom);
	}
	/** ���ꤵ�줿���ȥ�򤳤���μ¹ԥ��ȥॹ���å����Ѥ� */
	public void enqueueAtom(Atom atom) {
		String atomid = getAtomID(atom);
		if (atomid != null) { // AbstractMembrane#addAtom����θƤӽФ���̵�뤹��
			remote.send("ENQUEUEATOM", this, atomid);
		}
	}
	/** ��⡼�Ȥ�moveCellsFrom�ǹԤ��뤿�Ჿ�⤷�ʤ��Ƥ褤 */
	public void enqueueAllAtoms() {
	}

	// �ܥǥ����3 - ��������

	/** ������������������ */
	public AbstractMembrane newMem() {
		String newglobalid = remote.generateNewID();
		RemoteMembrane submem = new RemoteMembrane((RemoteTask)task, this, newglobalid);
		//memids.put(submem.globalid, newglobalid);
		mems.add(submem);
		remote.send("NEWMEM", newglobalid, this);
		remote.registerMem(newglobalid, submem);
		return submem;
	}

	public void removeMem(AbstractMembrane mem) {
		remote.send("REMOVEMEM", this, mem.getGlobalMemID());
		super.removeMem(mem);
	}

	// �ܥǥ����4 - ��󥯤����

	public void newLink(Atom atom1, int pos1, Atom atom2, int pos2) {
		remote.send("NEWLINK", this,
			getAtomID(atom1), pos1,
			getAtomID(atom2), pos2);
		super.newLink(atom1, pos1, atom2, pos2);
	}
	public void relinkAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		remote.send("RELINKATOMARGS", this,
			getAtomID(atom1), pos1,
			getAtomID(atom2), pos2);
		super.relinkAtomArgs(atom1, pos1, atom2, pos2);
	}
	public void inheritLink(Atom atom1, int pos1, Link link2) {
		remote.send("RELINKATOMARGS", this,
			getAtomID(atom1), pos1,
			getAtomID(link2.getAtom()), link2.getPos());
		super.inheritLink(atom1, pos1, link2);
	}
	public void unifyAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		remote.send("UNIFYATOMARGS", this,
			getAtomID(atom1), pos1,
			getAtomID(atom2), pos2);
		super.unifyAtomArgs(atom1, pos1, atom2, pos2);
	}
	public void unifyLinkBuddies(Link link1, Link link2) {
		remote.send("UNIFYATOMARGS", this,
			getAtomID(link1.getAtom()), link1.getPos(),
			getAtomID(link2.getAtom()), link2.getPos());
		super.unifyLinkBuddies(link1, link2);
	}

	// �ܥǥ����5 - �켫�Ȥ��ư�˴ؤ������

	public void activate() {
		remote.send("ACTIVATE",this);
	}

	public void moveCellsFrom(AbstractMembrane srcMem) {
		//todo ����
		
		if (srcMem.task.getMachine() != task.getMachine()) {
			throw new RuntimeException("cross-site remote process fusion not implemented");
		}
		remote.send("MOVECELLSFROM", this, srcMem.getGlobalMemID());
		remote.flush();
	}

	/** dstMem�˰�ư */
	public void moveTo(AbstractMembrane dstMem) {
		//todo ����
		
		if (dstMem.task.getMachine() != task.getMachine()) {
			throw new RuntimeException("cross-site remote process migration not implemented");
		}
		// remote call of a local process migration
		remote.send("MOVETO", this, dstMem.getGlobalMemID());
		super.moveTo(dstMem);
	}

	// ��å��˴ؤ������ - ������̿��ϴ�������task��ľ��ž�������
	
	// - ������̿��
	
	synchronized public boolean lock() {
		if (locked) return false;
		if (fUnlockDeferred) {
			fUnlockDeferred = false;
		} else {
			if (!doLock("LOCK")) return false;
		}
		onLock(false);
		return true;
	}
	public boolean blockingLock() {
		//todo:locked==true�ΤȤ������η׻��Ρ��ɤ�ï����å�������ʬ����ʤ��Τ򲿤Ȥ�����
		// [��å������׵�ν�����ˡ]
		// * ����å����줿����Υ�å���������Ƥ�餦�Ȥ���ɬ�ס�
		// �롼�륹��åɤ���å����Ƥ�����硢��������ID���⡼�Ȥ��Ϥ�����todo ������ɬ�פ�����
		// ��롼�륹��åɤ���å����Ƥ�����硢��������¸�ߤ��ʤ�����
		// ͥ����̵����ȸ��ʤ��Τǥ�å��ϲ����Ǥ��ʤ����ȤˤʤäƤ���Τ�����ס�
		if (!doLock("BLOCKINGLOCK")) return false;
		onLock(false);
		return true;
	}
	/** ��Ʊ��Ū�˥�å����롣�롼����ο�������������Ȥ��ʤɤ˻��Ѥ���롣
	 * ������Υ���å���Ϲ������ʤ���*/
	public boolean asyncLock() {
		if (!doLock("ASYNCLOCK")) return false;
		onLock(true);
		return true;
	}
	public boolean recursiveLock() {
		return sendWait("RECURSIVELOCK");
	}
	
	// - �ܥǥ�̿��
	
	public void unlock() {
		if (false && isRoot() && remote.cmdbuffer.length() == 0) {
			fUnlockDeferred = true;
		}
		else {
			remote.send("UNLOCK",this);
			onUnlock(false);
		}
	}
	public void forceUnlock() {
		remote.send("UNLOCK",this);
		onUnlock(false);
	}
	public void asyncUnlock() {
		remote.send("ASYNCUNLOCK",this);
		onUnlock(true);
	}
	public void recursiveUnlock() {
		remote.send("RECURSIVEUNLOCK",this);
	}

	///////////////////////////////	
	// RemoteMembrane ����������᥽�å�
	
	private boolean doLock(String cmd) {
		Object obj = sendWaitObject(cmd);
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
		return true;
	}

	private void onLock(boolean signal) {
		locked = true;
		if (signal || isRoot()) {
			if (signal || parent == null || parent.remote == null) {
				remote = (RemoteTask)task;
				remote.init();							// ̿��֥�å����Ѥ߾夲�򳫻Ϥ���
			}
		} else {
			remote = parent.remote;
		}
	}
	private void onUnlock(boolean signal) {
		if (signal || isRoot()) {
			if (remote == task) remote.flush();	// ̿��֥�å����Ѥ߾夲��λ����
		}
		remote = null;
		locked = false;
	}
	/** ����å���򹹿�����
	 * @see Membrane#cache() */
	protected void updateCache(byte[] data) {
		// TODO �ڼ����ۡ�ͭ��A��2/2
		
		// ���ȥ�      ->
		// ����        -> daemon.IDConverter.registerGlobalMembrane()����ͳ��󥯤���³
		// �롼�륻�å� -> ľ���˸�ʪ������ʥ��ȥ꡼�����ͭ����Ȥ��줬�Ǥ��ʤ��ΤǺ��ä��͡�

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
	String sendWaitText(String cmd) {
		String host = task.runtime.hostname;
		String msg = cmd + " " + getGlobalMemID();
		return LMNtalRuntimeManager.daemon.sendWaitText(host,msg);
	}
	/** ��å�������ľ�����������������Ԥäƥ֥�å����롣�ʲ���*/
	Object sendWaitObject(String cmd) {
		String host = task.runtime.hostname;
		String msg = cmd + " " + getGlobalMemID();
		return LMNtalRuntimeManager.daemon.sendWaitObject(host,msg);
	}
	
}