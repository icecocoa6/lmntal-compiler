package runtime;

import java.util.HashMap;
import java.util.Iterator;

import daemon.LMNtalDaemon;
import daemon.LMNtalNode;

/**
 * ��⡼�ȷ׻��Ρ���
 * 
 * �긵�ˤ��äơ���⡼��¦�ʥͥåȥ���θ�����¦�ˤ������ͤȤ���¸�ߤ��롣
 * ��äƤ����̿����⡼�Ȥ�ž���������ܡ�
 * 
 * @author n-kato
 * 
 */
final class RemoteLMNtalRuntime extends AbstractLMNtalRuntime{
	boolean result;
	
	/*
	 * ��⡼��¦�Υۥ���̾��Fully Qualified Domain Name�Ǥ���ɬ�פ����롣
	 */
	protected String hostname;
	/*
	 * hostname���б�����LMNtalNode���ºݤ�LMNtalDaemon.getLMNtalNodeFromFQDN()�ǤȤäƤ��Ƥ������
	 */
	protected LMNtalNode lmnNode; 
	
/*
 * ���󥹥ȥ饯��
 * 
 * @param hostname �Ĥʤ������ۥ��ȤΥۥ���̾��Fully Qualified Domain Name�Ǥ���ɬ�פ����롣
 */	
	protected RemoteLMNtalRuntime(String hostname) {
		//runtimeid����ˤ�fqdn�����äƤ���ʤȤߤʤ���

		this.hostname = hostname;
	}

	public AbstractTask newTask() {
		// todo ����Ʊ��
		return (AbstractTask)null;
	}
	
	/*
	 * ����������̿���ȯ�Ԥ��롣
	 * �ºݤ˥������������Τϥ�⡼��¦��
	 * 
	 * @param AbstractMembrane ����
	 * @return AbstractTask
	 */
	public AbstractTask newTask(AbstractMembrane parent) {
		// TODO ���ͥ������δ�����RemoteTask���餳�Υ��饹�˰ܤ������send��ȯ�Ԥ��륳���ɤ��

		return (AbstractTask)null;
	}
	
	/*
	 * TERMINATE��ȯ�ԡ�
	 */
	public void terminate() {
		//TODO ����@LMNtalDaemon(or MessageProcessor
		//send("TERMINATE");
	}
	
	/*
	 * AWAKE��ȯ��
	 */
	public void awake() {
		//TODO ����
		//send("AWAKE");
	}

/*
 * ��⡼��¦����³���롣
 * �ºݤ�LMNtalDaemon.connet(hostname)��ƽФ��Ƥ��������
 * 
 * @return ��³����������true�����Ԥ�����false����³������Ƚ���LMNtalDaemon.connect()���֤�boolean�ȡ�
 */	
	public boolean connect(){
		//TODO ñ�Υƥ���
		result = LMNtalDaemon.connect(hostname);
		lmnNode = LMNtalDaemon.getLMNtalNodeFromFQDN(hostname);
		if(lmnNode != null && result == true){
			return true;
		} else {
			return false;
		}
	}
	

}

/**
 * ��⡼�ȥ��������饹
 * ��20040707 nakajima ���ͥ������δ�����RemoteMachine�ˤޤ����롣
 *       ������nextatom(mem)id��synchronized�ˤ��ʤ���Фʤ�ʤ��ʤ롣
 * @author n-kato
 */
final class RemoteTask extends AbstractTask {
	String cmdbuffer;
	int nextatomid;
	int nextmemid;
	LMNtalNode remoteNode;

	HashMap memIDTable;

	/*
	 * ���󥹥ȥ饯����
	 */
	RemoteTask(AbstractLMNtalRuntime runtime) {
		 super(runtime);
		 
		 //runtime��RemoteLMNtalRuntime�ΤϤ�
		 remoteNode = ((RemoteLMNtalRuntime)runtime).lmnNode;
	}

//	String getNextAtomID() {
//		return "NEW_" + nextatomid++;
//	}
	
	synchronized String getNextAtomID(){
		return "NEW_" + nextatomid++;
	}

//	String getNextMemID() {
//		return "NEW_" + nextmemid++;
//	}

	synchronized String getNextMemID() {
		//LMNtalDaemon.getGlobalMembraneID(mem);
		
		return "NEW_" + nextmemid++;
	}

/*	
 * ���ޥ�ɤ��⡼��¦���������롣���֤�String cmdbuffer��cmd�Ȳ���(\n)��ä��Ƥ��������
 * 
 * @param cmd ���ꤿ�����ޥ��
 */
	void send(String cmd) {
		cmdbuffer += cmd + "\n";
	}


	synchronized void registerMem(String id, String mem){
		memIDTable.put(id, mem);
	}
	/*  
	 * "NEW_1"�Τ褦��ID���Ϥ��ȡ������Х����ID���֤���
	 * 
	 * @param id "NEW_1"�Τ褦��String
	 * @return ��ID���ʤ��ä���null��
	 */
	String getRealMemName(String id){
		return (String)memIDTable.get(id);
	}

	/*
	 * cmdbuffer�ˤ��ޤä�̿����⡼��¦�����ꡢcmdbuffer����ˤ��롣
	 * �ºݤˤ�LMNtalDaemon.sendMessage()��Ƥ�Ǥ��������
	 * 
	 * @throw RuntimeException LMntalDaemon.sendMessage()���֤��ͤ�false�λ��ʤĤޤ��������Ի���
	 */
	synchronized void flush() {
		//TODO BEGIN��END��Ĥ���ʤ����Ǥ��٤���LMNtalDaemon�ʤ�¾�ξ��Ǥ��٤���
		
		boolean result = LMNtalDaemon.sendMessage(remoteNode,cmdbuffer);

		if(result == true){
			cmdbuffer = ""; //�Хåե�������
			nextatomid = 0;
			nextmemid = 0;
		} else {
			throw new RuntimeException("error in flush()");
		}
	}
	
	// ��å�
	public void lock() {
		//TODO ����
		throw new RuntimeException("not implemented");
	}
	public boolean unlock() {
		//TODO ����
		
		//��⡼�ȤΥ롼�����unlock̿�������
		//�⤦�����Ƥ���Τ��ɤ���Ĵ�٤�
		
		
		//cmdbuffer��flush()����
		flush();
		
		throw new RuntimeException("not implemented");
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
	/** ������λ���Υ�����ID�����⡼��ID�ؤμ��� */
	protected HashMap memids = new HashMap();
	
	/** ����å����֤��ɤ�����
	 * �ʥ�⡼�ȤǤϥ�å�����Ƥ���ȸ��ʤ���������Ǥϥ�å���������Ƥ���Ȥߤʤ����֤Τ��ȡ�*/
	protected boolean fUnlockDeferred = false;
	
	/*
	 * ���󥹥ȥ饯����remoteid��LMNtalDaemon.getGlobalMembraneID�ˤ�ä���������롣
	 */
	public RemoteMembrane(RemoteTask task, RemoteMembrane parent){
		super(task,parent);
		this.remoteid = LMNtalDaemon.getGlobalMembraneID(this);
	}
	
	/*
	 * ���󥹥ȥ饯����remoteid������Ū���Ϥ���
	 */
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
	
	public void copyRulesFrom(AbstractMembrane srcMem) {
		Iterator it = srcMem.rulesetIterator();
		while (it.hasNext()) {
			Ruleset rs = (Ruleset)it.next();
			send("LOADRULESET",rs.getGlobalRulesetID());
		}
		super.copyRulesFrom(srcMem);
	}
	/** �롼�륻�åȤ��ɲ� */
	public void loadRuleset(Ruleset srcRuleset) {
		send("LOADRULESET",srcRuleset.getGlobalRulesetID());
		super.loadRuleset(srcRuleset);
	}
	
	// ���2 - ���ȥ�����

	/** ���������ȥ�����������������ɲä���������μ¹ԥ��ȥॹ���å�������롣 */
	public Atom newAtom(Functor functor) {
		Atom a = super.newAtom(functor);
		String atomid = ((RemoteTask)task).getNextAtomID(); //NEW_1�Ȥ������äƤ��ޤ�
		atomids.put(a,atomid);
		send("NEWATOM",atomid);
		return a;
	}
	public void alterAtomFunctor(Atom atom, Functor func) {
		send("ALTERATOMFUNCTOR",getAtomID(atom),func.getName()); // getName�Ǥϡ����Τ�ž���ϴ��ԤǤ��ʤ�
		super.alterAtomFunctor(atom,func);
	}
	public void removeAtom(Atom atom) {
		send("REMOVEATOM",getAtomID(atom));
		super.removeAtom(atom);
	}
	/** ���ꤵ�줿���ȥ�򤳤���μ¹ԥ��ȥॹ���å����Ѥ� */
	public void enqueueAtom(Atom atom) {
		// TODO ��⡼�ȤΥ��ȥ���Ѥ��礬���뤬��������ǽ���ɤ���Ĵ�٤�
		//String atomid = getAtomID(atom);
		//if (atomid != null) { // AbstractMembrane#addAtom����θƤӽФ���̵�뤹��
		//	send("ENQUEUEATOM",atomid);
		//}
	}
	/** ��⡼�Ȥ�moveCellsFrom�ǹԤ��뤿�Ჿ�⤷�ʤ��Ƥ褤 */
	public void enqueueAllAtoms() {}

	// ���3 - ��������
	
	/** ������������������ */
	public AbstractMembrane newMem() {
		//String newremoteid = ((RemoteTask)task).getNextMemID();
		//RemoteMembrane m = new RemoteMembrane((RemoteTask)task, this, newremoteid);
		//m.remoteid = newremoteid;
		//mems.add(m);
		//send("NEWMEM",newremoteid);
		//return m;
		
		String newremoteid = ((RemoteTask)task).getNextMemID();
		RemoteMembrane m = new RemoteMembrane((RemoteTask)task, this);
		memids.put(m.remoteid, newremoteid);
		mems.add(m);
		send("NEWMEM", newremoteid);
		((RemoteTask)task).registerMem(newremoteid, m.remoteid);
				
		return m;
	}
	
	public AbstractMembrane newRoot(AbstractLMNtalRuntime runtime) {
		// TODO ��������
		
		//RemoteTask����
		RemoteTask newtask = new RemoteTask(runtime);
						
		//��⡼�Ȥ������̿���ȯ�Ԥ���
		send("NEWROOT",""+ remoteid ); //�����Ϥ���Ǥ����Τ���
		
		//RemoteTask.root���֤�		
		return newtask.getRoot();
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
	public void unifyAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2) {
		send("UNIFYATOMARGS",""+getAtomID(atom1)+" "+pos1+" "+getAtomID(atom2)+" "+pos2);
		super.unifyAtomArgs(atom1,pos1,atom2,pos2);
	}
	public void unifyLinkBuddies(Link link1, Link link2) {
		send("NEWLINK",""+getAtomID(link1.getAtom())+" "+link1.getPos()+" "
						+getAtomID(link2.getAtom())+" "+link2.getPos());
		super.unifyLinkBuddies(link1,link2);
	}

	// ���5 - �켫�Ȥ��ư�˴ؤ������
	
	public void activate() {
		send("ACTIVATE");
	}
//	public void remove() {
//		send("REMOVE");
//		super.remove();
//	}

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
	
	synchronized public boolean lock() {
		if (locked) {
			return false;
		} else {
			if (fUnlockDeferred) {
				locked = true;
				fUnlockDeferred = false;	// ?
			}
			else {
				send("LOCK");
				//wait();
				if (true) { // ��å���������
					//todo:����å���ι���
				}
				else { // ��å���������
					return false;
				}
			}
			locked = true;
			return true;
		}
	}
	public void blockingLock() {
		//todo:locked==true�ΤȤ������η׻��Ρ��ɤ�ï����å�������ʬ����ʤ��Τ򲿤Ȥ�����
		// [��å������׵�ν�����ˡ]
		// * ����å����줿����Υ�å���������Ƥ�餦�Ȥ���ɬ�ס�
		// �롼�륹��åɤ���å����Ƥ�����硢��������ID���⡼�Ȥ��Ϥ�����TODO ������ɬ�פ�����
		// ��롼�륹��åɤ���å����Ƥ�����硢��������¸�ߤ��ʤ�����
		// ͥ����̵����ȸ��ʤ��Τǥ�å��ϲ����Ǥ��ʤ����ȤˤʤäƤ���Τ�����ס�
		send("BLOCKINGLOCK");
		//wait;
		// todo:����å���ι���
	}
	public void asyncLock() {
		send("ASYNCLOCK");
	}
	public void unlock(boolean signal) {
		fUnlockDeferred = true;
		//send("");
	}
	public void forceUnlock() {
		send("UNLOCK",""+false); // ���ȤǤ褯�ͤ��뤳��
	}
	public void asyncUnlock() {
		send("ASYNCUNLOCK");
	}
	public void recursiveLock() {
		send("RECURSIVELOCK");
	}
	public void recursiveUnlock() {
		send("RECURSIVEUNLOCK");
	}
}