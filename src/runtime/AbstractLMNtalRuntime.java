package runtime;

/** ��ݥ�󥿥���ʵ졧���ʪ���ޥ��󡢵졹����ݷ׻��Ρ��ɡ˥��饹��
 * @author n-kato
 */
abstract class AbstractLMNtalRuntime {
	protected String runtimeid;
	protected String runtimeGroupID;
	public String getRuntimeID() {
		return runtimeid;
	}
	public String getRuntimeGroupID() {
		return runtimeGroupID;
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
