package compile.structure;

/**
 * ��������������Υץ���ʸ̮�и������դ��ץ���ʸ̮�и����ޤ��ϥ롼��ʸ̮�и���ɽ����ݥ��饹��
 * todo ��󥯤�ĥ���뤿��Atom�Υ��֥��饹�ˤʤäƤ��뤬���Լ����ʤΤǤ����콤�����٤��Ǥ��롣*/

public abstract class Context extends Atom {
	/** ����ƥ����Ȥθ���̾ */
	protected String qualifiedName;	
	/** ����ƥ����ȤΥ������и� */
	public Context src = null;
	/** ���礦��2��и�������ˡ��⤦�����νи����ݻ����� */
	public Context buddy = null;
	
	/** ���󥹥ȥ饯��
	 * @param mem ��°��
	 * @param qualifiedName ����ƥ����Ȥθ���̾
	 * @param arity ����ƥ����Ƚи�������Ū�ʼ�ͳ��󥯰����θĿ�
	 */
	protected Context(Membrane mem, String qualifiedName, int arity) {
		super(mem,"",arity);
		this.qualifiedName = qualifiedName;
	}
	
	/** ����ƥ����Ȥθ���̾���֤� */
	public String getQualifiedName() {
		return qualifiedName;
	}
	/** ����ƥ����Ȥ�̾�����֤��ʲ��� */
	public String getName() {
		return qualifiedName.substring(1);
	}
	abstract public String toString();	
}
