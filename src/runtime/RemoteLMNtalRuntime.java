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
		if(Env.debug > 0 )System.out.println("RemoteLMNtalRuntime.newTask()");
		RemoteTask task = new RemoteTask(this, parent);
		RemoteMembrane newroot = (RemoteMembrane)task.getRoot();
		if (newroot.remote == null) {
			// �������������򤷤Ф餯ž����Ȥ��ƻ��Ѥ��뤿��˽��������
			newroot.remote = task;
			task.init();
		}
		String newmemid = newroot.remote.generateNewID();
		newroot.globalid = newmemid;
		newroot.remote.send("NEWROOT",newmemid,parent,hostname); // ����ؤ�̿���ҥۥ��Ȥ�����
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
				+ daemon.LMNtalDaemon.getLocalHostName() + "\"" );
	}
	
	/** ���Υ�⡼�ȥ�󥿥���ʥ�������Τߡˤ˵�������������ӵ��������������֤���
	 * ������ϡ�����롼����ο���Υץ����Ȥ��ƻ��Ѥ���롣
	 * <br>���������� �� �롼���������ʤ���⡼�ȥ�����
	 * <br>������ �� ���������������������⡼����ʿ��������ʤ���
	 * @return �������������� */
	public RemoteMembrane createPseudoMembrane(String globalid) {
		RemoteTask task = new RemoteTask(this);	// ���������������
		RemoteMembrane mem = new RemoteMembrane(task);
		mem.globalid= globalid;
		return mem;
	}

}