package runtime;

/** ��ݥ����� */
abstract public class AbstractTask {
	/** ʪ���ޥ��� */
	protected AbstractLMNtalRuntime runtime;
	/** �롼���� */
	protected AbstractMembrane root;
	/** ayncUnlock���줿�Ȥ���true�ˤʤ�*/
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
