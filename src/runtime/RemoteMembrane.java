package runtime;

//import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.net.Socket;

/**
 * ��⡼�ȷ׻��Ρ���
 * @author n-kato
 */
final class RemoteMachine extends AbstractMachine {
	protected String runtimeid;
	protected Socket socket;
	protected RemoteMachine(String runtimeid) {
		this.runtimeid = runtimeid;
	}
	//
	static HashMap runtimeids = new HashMap();
	public static AbstractMachine connectRuntime(String node) {
		node = node.intern();
		AbstractMachine ret = (AbstractMachine)runtimeids.get(node);
		if (ret == null) {
			ret = new RemoteMachine(node);
			runtimeids.put(node,ret);
		}
		return ret;
	}
	public AbstractTask newTask() {
		// TODO ���ͥ������δ�����RemoteTask���餳�Υ��饹�˰ܤ������send��ȯ�Ԥ��륳���ɤ��
		return (AbstractTask)null;
	}
}


/**
 * ��⡼�ȥ��������饹
 * TODO ���ͥ������δ�����RemoteMachine�ˤޤ����롣
 *       ������nextatom(mem)id��synchronized�ˤ��ʤ���Фʤ�ʤ��ʤ롣
 * @author n-kato
 */
final class RemoteTask extends AbstractTask {
	String cmdbuffer;
	int nextatomid;
	int nextmemid;
	RemoteTask() {}
	String getNextAtomID() {
		return "NEW_" + nextatomid++;
	}
	String getNextMemID() {
		return "NEW_" + nextmemid++;
	}
	void send(String cmd) {
		cmdbuffer += cmd + "\n";
	}
	void flush() {
		System.out.println("SYSTEM ERROR: remote call not implemented");
	}
}

/**
 * ��⡼���쥯�饹
 * @author n-kato
 */
final class RemoteMembrane extends AbstractMembrane {
	/** ��������������׻��Ρ��ɤˤ����뤳�����ID */
	protected String remoteid;
	/** ������Υ��ȥ�Υ�����ID�����⡼��ID�ؤμ��� */
	protected HashMap atomids = new HashMap();
	public RemoteMembrane(RemoteTask task, RemoteMembrane parent, String remoteid) {
		super(task,parent);
		this.remoteid = remoteid;
	}
	
	void send(String cmd) {
		((RemoteTask)task).send(cmd + " " + remoteid);
	}
	void send(String cmd, String args) {
		((RemoteTask)task).send(cmd + " " + remoteid + " " + args);
	}
	void send(String cmd, String arg1, String arg2) {
		((RemoteTask)task).send(cmd + " " + remoteid + " " + arg1 + " " + arg2);
	}
	void send(String cmd, String arg1, String arg2, String arg3, String arg4) {
		((RemoteTask)task).send(cmd + " " + remoteid + " " + arg1 + " " + arg2
														   + " " + arg3 + " " + arg4);
	}
	///////////////////////////////
	// ����μ���
	
	String getMemID() { return remoteid; }
	String getAtomID(Atom atom) { return (String)atomids.get(atom); }

	///////////////////////////////
	// ���

	// ���1 - �롼������

	public void clearRules() {
		send("CLEARRULES");
		super.clearRules();
	}
	/** TODO Ruleset#getGlobalRulesetID()�Τ褦�ʥ᥽�åɤ��롣 */
	public void copyRulesFrom(AbstractMembrane srcMem) {
		Iterator it = srcMem.rulesetIterator();
		while (it.hasNext()) {
			Ruleset rs = (Ruleset)it.next();
			send("LOADRULESET"/*,rs.getGlobalRulesetID()*/);
		}
		super.copyRulesFrom(srcMem);
	}
	/** �롼�륻�åȤ��ɲ� */
	public void loadRuleset(Ruleset srcRuleset) {
		send("LOADRULESET"/*,srcRuleset.getGlobalRulesetID()*/);
		super.loadRuleset(srcRuleset);
	}
	
	// ���2 - ���ȥ�����

	/** ���������ȥ�����������������ɲä���������μ¹ԥ����å�������롣 */
	public Atom newAtom(Functor functor) {
		Atom a = super.newAtom(functor);
		String atomid = ((RemoteTask)task).getNextAtomID();
		atomids.put(a,atomid);
		send("NEWATOM",atomid);
		return a;
	}
	public void alterAtomFunctor(Atom atom, Functor func) {
		send("ALTERATOMFUNCTOR",getAtomID(atom),func.toString());
		super.alterAtomFunctor(atom,func);
	}
	public void removeAtom(Atom atom) {
		send("REMOVEATOM",getAtomID(atom));
		super.removeAtom(atom);
	}
	/** ���ꤵ�줿���ȥ�򤳤���μ¹ԥ����å����Ѥ� */
	protected void enqueueAtom(Atom atom) {
		String atomid = getAtomID(atom);
		if (atomid != null) { // AbstractMembrane#addAtom����θƤӽФ���̵�뤹��
			send("ENQUEUEATOM",atomid);
		}
	}
	/** ��⡼�Ȥ�moveCellsFrom�ǹԤ��뤿�Ჿ�⤷�ʤ��Ƥ褤 */
	protected void enqueueAllAtoms() {}

	// ���3 - ��������
	
	/** ������������������ */
	public AbstractMembrane newMem() {
		String newremoteid = ((RemoteTask)task).getNextMemID();
		RemoteMembrane m = new RemoteMembrane((RemoteTask)task, this, newremoteid);
		m.remoteid = newremoteid;
		mems.add(m);
		send("NEWMEM",newremoteid);
		return m;
	}
	public AbstractMembrane newRoot(AbstractMachine runtime) {
		// TODO ��������
		return null;
	}

	public void removeMem(AbstractMembrane mem) {
		send("REMOVEMEM",mem.getMemID());
		super.removeMem(mem);
	}

	// ���4 - ��󥯤����

	public void newLink(Atom atom1, int pos1, Atom atom2, int pos2) {
		send("NEWLINK",""+getAtomID(atom1)+" "+pos1+" "+getAtomID(atom2)+" "+pos2);
		super.newLink(atom1,pos1,atom2,pos2);
	}
	public void relinkAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		send("RELINKATOMARGS",""+getAtomID(atom1)+" "+pos1+" "+getAtomID(atom2)+" "+pos2);
		super.relinkAtomArgs(atom1,pos1,atom2,pos2);
	}
	public void inheritLink(Atom atom1, int pos1, Link link2) {
		send("RELINKATOMARGS",""+getAtomID(atom1)+" "+pos1+" "
			+getAtomID(link2.getAtom())+" "+link2.getPos());
		super.inheritLink(atom1,pos1,link2);
	}
//	public void unifyAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
//		send("UNIFYATOMARGS",""+getAtomID(atom1)+pos1+getAtomID(atom2)+pos2);
//		super.unifyAtomArgs(atom1,pos1,atom2,pos2);
//	}

	// ���5 - �켫�Ȥ��ư�˴ؤ������
	
	public void activate() {
		send("ACTIVATE");
	}
	public void remove() {
		send("REMOVE");
		super.remove();
	}

	public void moveCellsFrom(AbstractMembrane srcMem) {
		if (srcMem.task.getMachine() != task.getMachine()) {
			throw new RuntimeException("cross-site remote process fusion not implemented");
		}
		send("POUR",srcMem.getMemID());
	}
	
	/** dstMem�˰�ư */
	public void moveTo(AbstractMembrane dstMem) {
		if (dstMem.task.getMachine() != task.getMachine()) {
			throw new RuntimeException("cross-site remote process migration not implemented");
		}
		// remote call of a local process migration
		send("MOVETO",dstMem.getMemID());
		super.moveTo(dstMem);
	}
	
	// ���6 - ��å��˴ؤ������
	
	synchronized public boolean lock(AbstractMembrane mem) {
		if (locked) {
			//todo:���塼�˵�Ͽ
			//todo:locked==true�ΤȤ������η׻��Ρ��ɤ�ï����å�������ʬ����ʤ��Τ򲿤Ȥ�����
			return false;
		} else {
			//todo:�׻��Ρ��ɤε�Ͽ������å���ι���
			locked = true;
			return true;
		}
	}
	public boolean recursiveLock(AbstractMembrane mem) {
		//send("RECURSIVELOCK");
		return false;
	}
	public void unlock() {
		send("UNLOCK");
	}
	public void recursiveUnlock() {
		//send("RECURSIVEUNLOCK");
	}

}