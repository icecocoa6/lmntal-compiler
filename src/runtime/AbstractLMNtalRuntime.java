package runtime;

/** ��ݥ�󥿥���ʵ졧���ʪ���ޥ��󡢵졹����ݷ׻��Ρ��ɡ˥��饹��
 * @author n-kato
 */
abstract class AbstractLMNtalRuntime {
	/** ��󥿥���ID������VM�Υ����Х�ʼ��̻ҡ��롼�륻�åȤ�ID�ΰ����Ȥ��ƻ��Ѥ���� */
	protected String runtimeid;
	/** ��󥿥���ID��������� */
	public String getRuntimeID() {
		return runtimeid;
	}
	/** ���ꤷ��������Ȥ���롼�������ĥ������򤳤Υ�󥿥���˺������롣
	 * @param parent �롼����ο���
	 * @return �������������� */
	abstract AbstractTask newTask(AbstractMembrane parent);
	/** ���Υ�󥿥���ν�λ���׵᤹�� */
	abstract public void terminate();
	
//	/** ��󥿥���Υ롼�륹��åɤ��Ф��ƺƼ¹Ԥ��׵᤹�롣*/
//	abstract public void awake();
}
