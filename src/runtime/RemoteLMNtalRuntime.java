package runtime;

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
final class RemoteLMNtalRuntime extends AbstractLMNtalRuntime {
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
		return (AbstractTask) null;
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

		return (AbstractTask) null;
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
	public boolean connect() {
		//TODO ñ�Υƥ���
		result = LMNtalDaemon.connect(hostname);
		lmnNode = LMNtalDaemon.getLMNtalNodeFromFQDN(hostname);
		if (lmnNode != null && result == true) {
			return true;
		} else {
			return false;
		}
	}

}