package compile.parser;

/**
 * �������ե�������Υ�󥯡����«���ץ�������ƥ����ȡ��롼�륳��ƥ����Ȥ���ݿƥ��饹
 * <p>�ץ���ʸ̮̾����ӥ롼��ʸ̮̾�ˤ� '...' �� [[...]] ���Ȥ��ʤ��褦�ˤ�����
 */

abstract class SrcContext {
 	
 	protected String name = null;
 	
 	/**
 	 * ���ꤵ�줿̾���ǥ���ƥ����Ȥ��������ޤ�
 	 * @param name ����ƥ�����̾
 	 */
	protected SrcContext(String name) {
		this.name = name;
	}
	
	/**
	 * ����ƥ����Ȥ�̾�����֤���
	 * @return ����ƥ����Ȥ�̾��
	 */
	public String getName() {
		return name;
	}
	/** ����ƥ����Ȥθ���̾�ʼ����̾�����Ȥ��Ф����դʼ��̻ҤȤ��ƻ��ѤǤ���ʸ����ˤ��֤���*/
	abstract public String getQualifiedName();
	public String toString() {
		return getQualifiedName();
	}
}