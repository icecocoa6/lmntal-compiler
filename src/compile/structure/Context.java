package compile.structure;

/**
 * ��������������Υץ���ʸ̮�и������դ��ץ���ʸ̮�и����ޤ��ϥ롼��ʸ̮�и���ɽ����ݥ��饹��
 * @author n-kato
 */
public abstract class Context extends Atomic
{
	/**
	 * ����ƥ����Ȥθ���̾
	 */
	protected String qualifiedName;

	/**
	 * ����ƥ�����̾�ξ���
	 */
	public ContextDef def;

	/** ���󥹥ȥ饯��
	 * @param mem ��°��
	 * @param qualifiedName ����ƥ����Ȥθ���̾
	 * @param arity ����ƥ����Ƚи�������Ū�ʼ�ͳ��󥯰����θĿ�
	 */
	protected Context(Membrane mem, String qualifiedName, int arity)
	{
		super(mem, arity);
		this.qualifiedName = qualifiedName;
	}

	/**
	 * ����ƥ����Ȥθ���̾���֤�
	 */
	public String getQualifiedName()
	{
		return qualifiedName;
	}

	/**
	 * ����ƥ����Ȥ�̾�����֤��ʲ���
	 */
	public String getName()
	{
		return qualifiedName.substring(1);
	}
}
