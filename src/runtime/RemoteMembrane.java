package runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

// ���Υե�������Խ���Ǥ���

/**
 * ��⡼�ȥޥ��󥯥饹
 * @author n-kato
 */
final class RemoteMachine extends AbstractMachine {
	String cmdbuffer;
	int nextatomid;
	int nextmemid;
	RemoteMachine() {}
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
	protected String memid;
	protected HashMap atomids;
	public RemoteMembrane(AbstractMachine machine, RemoteMembrane parent, String memid) {
		super(machine,parent);
		this.memid = memid;
		atomids = new HashMap();
	}
	String getMemID() { return memid; }
	void send(String cmd) {
		((RemoteMachine)machine).send(cmd + " " + memid);
	}
	void send(String cmd, String args) {
		((RemoteMachine)machine).send(cmd + " " + memid + " " + args);
	}

	///////////////////////////////
	// ���

	/** ��å����������줿�Ȥ��˺Ƴ���������뤿�ᡢ���⤷�ʤ��Ƥ褤 */
	void activate() {}
	void clearRules() {
		send("CLEARRULES");
		super.clearRules();
	}
	/** TODO Ruleset#getGlobalRulesetID()�Τ褦�ʥ᥽�åɤ��� */
	void inheritRules(AbstractMembrane srcMem) {
		Iterator it = srcMem.rulesetIterator();
		while (it.hasNext()) {
			Ruleset rs = (Ruleset)it.next();
			send("LOADRULESET"/*,rs.getGlobalRulesetID()*/);
		}
		super.inheritRules(srcMem);
	}
	/** ���ȥ���ɲ� */
	Atom newAtom(Functor functor) {
		Atom a = super.newAtom(functor);
		String atomid = ((RemoteMachine)machine).getNextAtomID();
		atomids.put(a,atomid);
		send("NEWATOM",atomid);
		return a;
	}
	/** ���ꤵ�줿���ȥ��¹ԥ����å����Ѥ� */
	protected void enqueueAtom(Atom atom) {
		send("ENQUEUEATOM",(String)atomids.get(atom));
	}
	/** ����ɲ� */
	AbstractMembrane newMem() {
		String newmemid = ((RemoteMachine)machine).getNextMemID();
		RemoteMembrane m = new RemoteMembrane(machine, this, newmemid);
		m.memid = newmemid;
		mems.add(m);
		send("NEWMEM",newmemid);
		return m;
	}
	
	/** dstMem�˰�ư 
	 * TODO �롼����Ȥ���ʳ���ʬ���� */
	void moveTo(AbstractMembrane dstMem) {
		/*
		send("MOVETO");
		parent.removeMem(this);
		dstMem.addMem(this);
		parent = dstMem;
		enqueueAllAtoms();
		*/
	}
	void alterAtomFunctor(Atom atom, Functor func) {
		send("ALTERATONFUNCTOR",atomids.get(atom) + " " + func.toString());
		super.alterAtomFunctor(atom,func);
	}
	protected void enqueueAllAtoms() {
		send("ENQUEUEALLATOMS");
	}
	void removeAtom(Atom atom) {
		send("REMOVEATOM",(String)atomids.get(atom));		super.removeAtom(atom);
	}
	void removeAtoms(ArrayList atomlist) {
		Iterator it = atomlist.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			send("REMOVEATOM",(String)atomids.get(atom));
		}
		super.removeAtoms(atomlist);
	}

	void remove() {
		send("REMOVE");
		super.remove();
	}
	void pour(AbstractMembrane srcMem) {
		//send("POUR");
	}
	
	////////////////////////////////
	// ��å�
	synchronized boolean lock(AbstractMembrane mem) {
		if (locked) {
			//todo:���塼�˵�Ͽ
			// TODO locked==true�ΤȤ������η׻��Ρ��ɤ�ï����å�������ʬ����ʤ��Τ򲿤Ȥ�����
			return false;
		} else {
			//todo:�׻��Ρ��ɤε�Ͽ������å���ι���
			locked = true;
			return false;
		}
	}
	boolean recursiveLock(AbstractMembrane mem) {
		//send("RECURSIVELOCK");
		return false;
	}
	void unlock() {
		send("UNLOCK");
	}
	void recursiveUnlock() {
		send("RECURSIVEUNLOCK");
	}
	// ��󥯤����	
	void newLink(Atom atom1, int pos1, Atom atom2, int pos2) {
		send("NEWLINK",""+atomids.get(atom1)+pos1+atomids.get(atom2)+pos2);
		super.newLink(atom1,pos1,atom2,pos2);
	}
	void relinkAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		send("RELINKATOMARGS",""+atomids.get(atom1)+pos1+atomids.get(atom2)+pos2);
		super.relinkAtomArgs(atom1,pos1,atom2,pos2);
	}
	void unifyAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		send("UNIFYATOMARGS",""+atomids.get(atom1)+pos1+atomids.get(atom2)+pos2);
		super.unifyAtomArgs(atom1,pos1,atom2,pos2);
	}
}