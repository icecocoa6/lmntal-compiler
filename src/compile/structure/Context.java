package compile.structure;

import java.util.List;
import java.util.ArrayList;

/** 
 * ProcessContext��RuleContext�οƤȤʤ���ݥ��饹
 */
public abstract class Context {
	/**
	 * ����ƥ����Ȥ�̾��
	 */
	protected String name;
	
	/**
	 * ���󥹥ȥ饯��
	 * @param name ����ƥ�����̾
	 */
	protected Context(String name) {
		this.name = name;
		status = ST_FRESH;
	}
	
	/**
	 * ����ƥ����Ȥ�̾�������ޤ�
	 * @return ����ƥ�����̾
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * ���դǤν�°��
	 */
	public Membrane lhsMem;
	
	/**
	 * ���դǤν�°�������
	 */
	List rhsMems = new ArrayList();
	
	/**
	 * ���ߤξ��֡�ST_�ǻϤޤ�����Τ����줫���ͤ�Ȥ�
	 */
	public int status = ST_FRESH;

	/**
	 * �������
	 */
	public static final int ST_FRESH = 0;

	/**
	 * ���դ˰��ٽи���������
	 */
	public static final int ST_LHSOK = 1;

	/**
	 * ���ա�����ξ���˽и���������
	 */
	public static final int ST_READY = 2;
	
	/**
	 * ���顼
	 */
	static final int ST_ERROR = 3;
}
