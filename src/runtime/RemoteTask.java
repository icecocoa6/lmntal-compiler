package runtime;

import java.util.HashMap;

import daemon.IDConverter;

/**
 * ��⡼�ȥ��������饹
 * <p>̿��֥�å���������롣
 * @author n-kato
 */
public final class RemoteTask extends AbstractTask {
	// ������
	String cmdbuffer;	// ̿��֥�å������ѥХåե�
	int nextid;		// ����NEW_�ѿ��ֹ�
	
//	// ������
//	HashMap memTable;

	/** �̾�Υ��󥹥ȥ饯����
	 * <p>���ꤷ���������Ŀ�������å����줿��⡼�ȤΥ롼���줪����б������⡼�ȥ��������������
	 * @param runtime ����������������¹Ԥ����󥿥�����б������⡼�ȥ�󥿥���
	 * @param parent ���� */
	RemoteTask(RemoteLMNtalRuntime runtime, AbstractMembrane parent){
		super(runtime);
		root = new RemoteMembrane(this, parent);
		root.locked = true;
		root.remote = parent.remote;
		parent.addMem(root);	// ����������κ����������ꤷ��
	}
	/** ���������������ѤΥ��󥹥ȥ饯���ʵ�����������root=null��
	 * @see RemoteLMNtalRuntime#createPseudoMembrane() */
	RemoteTask(RemoteLMNtalRuntime runtime) {
		super(runtime);
	}

	//
	
	/** �����̵���������������Υ�����������������ˤ��롣 */
	public RemoteMembrane createFreeMembrane() {
		return new RemoteMembrane(this);
	}


	///////////////////////////////
	// ������

	String generateNewID() {
		return "NEW_" + nextid++;
	}

	/** ̿��֥�å������ѥХåե������NEW_�Ѵ�ɽ���������� */
	public void init() {
		cmdbuffer = "";
		nextid = 0;
		memTable.clear();
		atomTable.clear();
	}

	/**	̿��֥�å������ѥХåե��˥ܥǥ�̿����ɲä��� */
	public void send(String cmd) {
		cmdbuffer += cmd + "\n";
	}
	
	void send(String cmd, AbstractMembrane mem) {
		cmdbuffer += cmd + " " + mem.getGlobalMemID() + "\n"; 
	}
	void send(String cmd, AbstractMembrane mem, String args) {
		cmdbuffer += cmd + " " + mem.getGlobalMemID() + " " + args + "\n"; 
	}
	void send(String cmd, AbstractMembrane mem,
			String arg1, int arg2, String arg3, int arg4) {
		cmdbuffer += cmd + " " + mem.getGlobalMemID() + " "
			+ arg1 + " " + arg2 + " " + arg3 + " " + arg4 + "\n"; 
	}
	void send(String cmd, String outarg, AbstractMembrane mem) {
		cmdbuffer += cmd + " " + mem.getGlobalMemID() + " " + outarg + "\n"; 
	}
	void send(String cmd, String outarg, AbstractMembrane mem, String args) {
		cmdbuffer += cmd + " " + mem.getGlobalMemID() + " " + outarg + " " + args + "\n"; 
	}

	/**
	 * ̿��֥�å������ѥХåե������Ƥ��⡼�Ȥ����������Хåե�����������
	 * <p>
	 * @throws RuntimeException �̿����ԡ�fatal��
	 */
	public void flush() {
		if (cmdbuffer.equals("")) return;
		String cmd = "BEGIN\n" + cmdbuffer + "END";
		String result = LMNtalRuntimeManager.daemon.sendWaitText(runtime.hostname, cmd);

		if (result.length() >= 4 && result.substring(0, 4).equalsIgnoreCase("FAIL")) {
			throw new RuntimeException("RemoteTask.flush: failure");
		}
		// BEGIN��å��������Ф����������᤹��
		String[] binds = result.split(";");
		for (int i = 0; i < binds.length; i++) {
			String[] args = binds[i].split("=",2);
			String tmpid = args[0];
			String newid = args[1];
			// todo �⤦������ĥ���ι⤤������ˡ��ͤ��롣�����餯NEW_��ʬ��������Ф褤�Ϥ���
			if (newid.indexOf(':') >= 0) {
				RemoteMembrane mem = (RemoteMembrane)memTable.get(tmpid);
				if (mem != null) IDConverter.registerGlobalMembrane(newid, mem);
			}
			else {
				Atom atom = (Atom)atomTable.get(tmpid);
				if (atom != null) atom.remoteid = newid;
			}
		}
		//
		cmdbuffer = "";	// nextid�Ͻ�������ʤ�
	}

	///////////////////////////////
	// NEW_ �������Ϥ������
	
	/** NEW_memid (String) -> AbstractMembrane */
	HashMap memTable = new HashMap();
	/** NEW_atomid (String) -> Atom */
	HashMap atomTable = new HashMap();

	void registerMem(String memid, RemoteMembrane mem) {
		memTable.put(memid, mem);
	}
	void registerAtom(String atomid, Atom atom) {
		atomTable.put(atomid, atom);
	}
	
	///////////////////////////////
	// ������

//	synchronized void registerMem(String id, String mem) {
//		memTable.put(id, mem);
//	}
	
//	/*  
//	 * "NEW_1"�Τ褦��ID���Ϥ��ȡ������Х����ID���֤���
//	 * 
//	 * @param id "NEW_1"�Τ褦��String
//	 * @return ��ID���ʤ��ä���null��
//	 */
//	String getRealMemName(String id) {
//		return (String) memTable.get(id);
//	}
}