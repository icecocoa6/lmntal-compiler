package runtime;

import java.util.HashMap;

import daemon.LMNtalDaemon;
import daemon.LMNtalNode;

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

	//
	HashMap memTable;

	/*
	 * ���󥹥ȥ饯����
	 */
	RemoteTask(AbstractLMNtalRuntime runtime) {
		super(runtime);

		//runtime��RemoteLMNtalRuntime�ΤϤ�
		remoteNode = ((RemoteLMNtalRuntime) runtime).lmnNode;
	}

	//	String getNextAtomID() {
	//		return "NEW_" + nextatomid++;
	//	}

	synchronized String getNextAtomID() {
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

	synchronized void registerMem(String id, String mem) {
		memTable.put(id, mem);
	}
	
	/*  
	 * "NEW_1"�Τ褦��ID���Ϥ��ȡ������Х����ID���֤���
	 * 
	 * @param id "NEW_1"�Τ褦��String
	 * @return ��ID���ʤ��ä���null��
	 */
	String getRealMemName(String id) {
		return (String) memTable.get(id);
	}

	/*
	 * cmdbuffer�ˤ��ޤä�̿����⡼��¦�����ꡢcmdbuffer����ˤ��롣
	 * �ºݤˤ�LMNtalDaemon.sendMessage()��Ƥ�Ǥ��������
	 * 
	 * @throw RuntimeException LMntalDaemon.sendMessage()���֤��ͤ�false�λ��ʤĤޤ��������Ի���
	 */
	synchronized void flush() {
		//TODO BEGIN��END��Ĥ���ʤ����Ǥ��٤���LMNtalDaemon�ʤ�¾�ξ��Ǥ��٤���

		boolean result = LMNtalDaemon.sendMessage(remoteNode, cmdbuffer);

		if (result == true) {
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