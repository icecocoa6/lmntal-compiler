package runtime;

/** ��ݥ����� */
abstract public class AbstractTask {
	/** ʪ���ޥ��� */
	protected AbstractLMNtalRuntime runtime;
	/** �롼���� */
	protected AbstractMembrane root;
	/** asyncUnlock���줿�Ȥ���true�ˤʤ�
	 * ��true�ʤ�Х����ʥ���������˥ȥ졼��dump�����*/
	protected boolean asyncFlag = false;
	
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
}
