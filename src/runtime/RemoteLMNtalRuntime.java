package runtime;

//import daemon.Cache;
//import daemon.LMNtalDaemon;
//import daemon.LMNtalNode;

/**
 * ��⡼�ȷ׻��Ρ���
 * <p>
 * ��⡼�Ȥ�¸�ߤ���LMNtalRuntime��ɽ�����饹��
 * 
 * @author n-kato, nakajima
 * 
 */
public final class RemoteLMNtalRuntime extends AbstractLMNtalRuntime {
//	/**
//	 * ��⡼��¦�Υۥ���̾��Fully Qualified Domain Name�Ǥ���ɬ�פ����롣
//	 */
//	protected String hostname; ��AbstractLMNtalRuntime�˰�ư���Ƥߤ�
	
	/**
	 * ���󥹥ȥ饯��
	 * 
	 * @param hostname �׻��Ρ��ɤ�¸�ߤ���ۥ���̾��fqdn��
	 */
	protected RemoteLMNtalRuntime(String hostname) {
//		this.runtimeid = runtimeid;  // todo ��³�褫��ID�������ä���������ʺ���̤���ѤʤΤ����꤬ȯ�����ʤ���
		this.hostname = hostname;
	}

	/**
	 * ��⡼�Ȥ˥�������������롣
	 * 
	 * @param AbstractMembrane ����
	 * @return AbstractTask
	 */
	public AbstractTask newTask(AbstractMembrane parent) {
		RemoteTask task = new RemoteTask(this, parent);
		if (parent.task.remote == null) {
			task.remote = task;
			task.init();
		}
		else {
			task.remote = parent.task.remote;
		}
		String newmemid = task.remote.generateNewID();
		((RemoteMembrane)task.root).globalid = newmemid;
		//String parentmemid = daemon.IDConverter.getGlobalMembraneID(parent);
		task.remote.send("NEWROOT",newmemid,parent,hostname);
		return task;
	}
	
	////////////////////////////////
	// RemoteLMNtalRuntime ����������᥽�å�

	/**
	 * ��⡼�ȥۥ��Ȥ��Ф�����³��ǧ��Ԥ����ǽ����³����Ȥ��ˤ���Ѥ���롣
	 * @return ��¸����ǧ�Ǥ������ɤ���
	 */
	public boolean connect() {
		return LMNtalRuntimeManager.daemon.sendWait(hostname,
			"CONNECT \"" + hostname + "\" \""
				+ daemon.LMNtalDaemonMessageProcessor.getLocalHostName() + "\"" );
	}
	
	/** ���Υ�󥿥���˵�������������ӵ��������������֤���
	 * ������ϡ�����롼����ο���Υץ����Ȥ��ƻ��Ѥ���롣
	 * <br>���������� �� �롼���������ʤ���⡼�ȥ�����
	 * <br>������ �� ���������������������⡼����ʿ��������ʤ���
	 * @return �������������� */
	public RemoteMembrane createPseudoMembrane() {
		RemoteTask task = new RemoteTask(this);	// ���������������
		return new RemoteMembrane(task);
	}

}