/**
 * ��������Υ��ɽ��
 */

package compile.parser;

class SrcLink {
	
	protected String name = null;
	
	/**
	 * ���ꤵ�줿̾���Υ�󥯤�������ޤ�
	 * @param name ���̾
	 */
	public SrcLink(String name) {
		this.name = name;	
	}
	
	/**
	 * ��󥯤�̾����������ޤ�
	 * @return ��󥯤�̾��
	 */
	public String getName() {
		return this.name;
	}
}