package runtime;

/** ��ݥ����� */
abstract public class AbstractTask {
	/** ʪ���ޥ��� */
	protected AbstractLMNtalRuntime runtime;
	/** �롼���� */
	protected AbstractMembrane root;
	/** �᥽�åɸƤӽФ���ž����Υ�⡼�ȥ������ޤ���null */
	protected RemoteTask remote = null;
	/** ���󥹥ȥ饯��
	 * @param runtime ��°�����󥿥��� */
	AbstractTask(AbstractLMNtalRuntime runtime) {
		this.runtime = runtime;
	}
	/** ��󥿥���μ��� */
	public AbstractLMNtalRuntime getMachine() {
		return runtime;
	}
	/** �롼����μ��� */
	public AbstractMembrane getRoot() {
		return root;
	}
	
	/** ������ݥ������Υ롼�륹��åɤκƼ¹Ԥ��׵ᤵ�줿���ɤ��� */
	protected boolean awakened = false;

	/** ���Υ��������Ф��ƥ����ʥ��ȯ�Ԥ��롣
	 * ���ʤ�������Υ������Υ롼����Υ�å��μ����򤹤뤿��˥֥�å����Ƥ��륹��åɤ�¸�ߤ���ʤ��
	 * ���Υ���åɤ�Ƴ����ƥ�å��μ������ߤ뤳�Ȥ��׵ᤷ��
	 * ¸�ߤ��ʤ��ʤ�Ф��Υ������Υ롼�륹��åɤκƼ¹Ԥ��׵᤹�롣*/
	synchronized public final void signal() {
		awakened = true;
		notify();
	}
}
