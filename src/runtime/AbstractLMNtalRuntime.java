package runtime;

/** ��ݥ�󥿥���ʵ졧���ʪ���ޥ��󡢵졹����ݷ׻��Ρ��ɡ˥��饹��*/

abstract class AbstractLMNtalRuntime {
	protected String runtimeid;
	/** ���Υ�󥿥���˿��������ʤ���å�����Ƥ��ʤ��롼���������������Ǥʤ��¹��쥹���å����Ѥࡣ*/
	abstract AbstractTask newTask(AbstractMembrane parent);
	/** ���Υ�󥿥���μ¹Ԥ�λ���� */
	abstract public void terminate();
	
//	/** ��󥿥���Υ롼�륹��åɤ��Ф��ƺƼ¹Ԥ��׵᤹�롣*/
//	abstract public void awake();
}
