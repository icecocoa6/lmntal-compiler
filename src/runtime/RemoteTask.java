package runtime;

//import java.util.HashMap;

/**
 * ��⡼�ȥ��������饹
 * <p>̿��֥�å���������롣
 * <p>
 * ��20040707 nakajima ���ͥ������δ�����RemoteMachine�ˤޤ����롣
 * @author n-kato
 */
final class RemoteTask extends AbstractTask {
	// ������
	String cmdbuffer;	// ̿��֥�å������ѥХåե�
	int nextid;		// ����NEW_�ѿ��ֹ�
	
//	// ������
//	HashMap memTable;

	/** �̾�Υ��󥹥ȥ饯�� */
	RemoteTask(RemoteLMNtalRuntime runtime, AbstractMembrane parent){
		super(runtime);
		root = new RemoteMembrane(this, parent);
		parent.addMem(root);	// ����������κ����������ꤷ��
	}

	///////////////////////////////
	// ������

	String getNextAtomID() {
		return "NEW_" + nextid++;
	}
	String getNextMemID() {
		return "NEW_" + nextid++;
	}

	/** ̿��֥�å������ѥХåե����������� */
	void init() {
		cmdbuffer = "";
		nextid = 0;
	}

	/**	̿��֥�å������ѥХåե��˥ܥǥ�̿����ɲä��� */
	void send(String cmd) {
		cmdbuffer += cmd + "\n";
	}
	
	void send(String cmd, AbstractMembrane mem) {
		cmdbuffer += cmd + " " + mem.getGlobalMemID();
	}
	void send(String cmd, AbstractMembrane mem, String args) {
		cmdbuffer += cmd + " " + mem.getGlobalMemID() + " " + args;
	}
	void send(String cmd, AbstractMembrane mem,
			String arg1, int arg2, String arg3, int arg4) {
		cmdbuffer += cmd + " " + mem.getGlobalMemID() + " "
			+ arg1 + " " + arg2 + " " + arg3 + " " + arg4;
	}
	void send(String cmd, String outarg, AbstractMembrane mem) {
		cmdbuffer += cmd + " " + outarg + " " + mem.getGlobalMemID();
	}
	void send(String cmd, String outarg, AbstractMembrane mem, String args) {
		cmdbuffer += cmd + " " + outarg + " " + mem.getGlobalMemID() + " " + args;
	}

	/**
	 * ̿��֥�å������ѥХåե������Ƥ��⡼�Ȥ���������
	 * <p>
	 * (n-kato) synchronized�Ϥʤ��դ��Ƥ���Τ���
	 * @throws RuntimeException �̿����ԡ�fatal��
	 */
	synchronized void flush() {
		String cmd = "BEGIN\n" + cmdbuffer + "END";
		boolean result = LMNtalRuntimeManager.daemon.sendWait(runtime.hostname, cmd);
		if (!result) {
			throw new RuntimeException("error in flush()");
		}
	}

	// ��å�
	// nakajima20040719: RemoteMembrane�ˤ���ΤǤ���ʤ��褦�ʵ�������
	// n-kato  20040815: �롼����Υ�å������/��������Ȥ��Υեå���ͤ��Ƥ����Τ��Ȼפ�

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