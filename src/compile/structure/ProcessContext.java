package compile.structure;

/**
 * ��������������Υץ���ʸ̮�ι�¤��ɽ�����饹
 * <br>TODO ���դ��ץ���ʸ̮�ΰ����ϡ�
 */
final public class ProcessContext extends Context{
	/**
	 * �����Υ��«
	 * <br>TODO ���󥹥ȥ饯�������ꤹ��Τ����᥽�åɤ���Τ�</p>
	 */
	private LinkOccurrence[] arg;
	/**
	 * �����Υ��«
	 * <bf>
	 * TODO ���󥹥ȥ饯�������ꤹ��Τ����᥽�åɤ���Τ�<br>
	 * TODO ���ѤΥ��饹���롩
	 */
	private LinkOccurrence bundle;
	ProcessContext(String name) {
		super(name);
	}
}
