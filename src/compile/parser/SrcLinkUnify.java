/**
 * �������ե�������Υ��ñ�첽��ɽ��
 */

package compile.parser;
import java.util.LinkedList;

class SrcLinkUnify extends SrcAtom {

	private static final String LINK_UNIFY = "builtin::unify";
	
	/**
	 * ���ꤵ�줿��󥯤�ñ�첽��ɽ�����ȥ��������ޤ�
	 * @param leftLink ñ�첽������
	 * @param rightLink ñ�첽������
	 */
	public SrcLinkUnify(SrcLink leftLink, SrcLink rightLink) {
		super(LINK_UNIFY);
		LinkedList link = new LinkedList();
		link.add(leftLink);
		link.add(rightLink);
		this.process = link;
	}
}