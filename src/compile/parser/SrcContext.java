/**
 * �ץ�������ƥ����Ȥȥ롼�륳��ƥ����Ȥ���ݥ��饹
 */

package compile.parser;
 
abstract class SrcContext {
 	
 	private String name = null;
 	
 	/**
 	 * ���ꤵ�줿̾���ǥ���ƥ����Ȥ��������ޤ�
 	 * @param name ����ƥ�����̾
 	 */
	protected SrcContext(String name) {
		this.name = name;
	}
	
	/**
	 * ����ƥ����Ȥ�̾�������ޤ�
	 * @return ����ƥ����Ȥ�̾��
	 */
	public String getName() {
		return name;
	}
}