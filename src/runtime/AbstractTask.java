package runtime;

/** ��ݥ����� */
abstract public class AbstractTask {
	/** ʪ���ޥ��� */
	protected AbstractLMNtalRuntime runtime;
	/** �롼���� */
	protected AbstractMembrane root;
	
	/** ��������ͥ���١����Τˤϡ����Υ������Υ롼�륹��åɤ�ͥ���١�
	 * <p>��å�������˻��Ѥ���ͽ�ꡣ�������Υ������塼��󥰤ˤ���Ѥ���Ƥ��롣
	 * <p>HIGHEST_PRIORITY�򲼲��ʤ��㤤�ͤǤʤ���Фʤ�ʤ���*/
	int priority;
	/** ��󥿥���Ǻǽ�˺����Υǥե����ͥ���� */
	public static final int ROOT_PRIORITY = 4096;
	/** �ҥ�������ͥ���٤Υǥե���Ⱥ�ʬ�ʻҤ������㤤�͡��⤤ͥ���٤ˤʤ�� */
	public static final int PRIORITY_DELTA = 4;
	/** ��κǤ�⤤ͥ���� */
	public static final int HIGHEST_PRIORITY = 10;
	/** ���ۥ��Ȼ�����������줿���롼����Ȥ��륿�����Υǥե����ͥ���١�Ŭ����*/
	public static final int PSEUDOTASK_PRIORITY = 8192 + PRIORITY_DELTA;
	/** ��������ͥ���٤���� */
	public int getPriority() {
		return priority;
	}

	/** ���󥹥ȥ饯��
	 * @param runtime ��°�����󥿥���
	 * @param priority �롼�륹��åɤ�ͥ���� */
	AbstractTask(AbstractLMNtalRuntime runtime, int priority) {
		this.runtime = runtime;
		if (priority < HIGHEST_PRIORITY) priority = HIGHEST_PRIORITY;
		this.priority = priority;
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
