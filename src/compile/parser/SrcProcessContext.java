package compile.parser;
import java.util.LinkedList;

/** �������ե�������Υץ�������ƥ����Ȥ�ɽ�� */

class SrcProcessContext extends SrcContext {
	/** ����Ū�ʰ�����
	 * <p>����¦��LinkedList�����������������뤳�� */
	public LinkedList args = null;
	/** ���« */
	public SrcLinkBundle bundle = null;
	
	/**
	 * ���ꤵ�줿̾�����İ���̵���Υץ�������ƥ����Ȥ��������
	 * @param name ����ƥ�����̾
	 */
	public SrcProcessContext(String name) {
		super(name);
//		// �ץ���ʸ̮̾�򥨥������פ��롣
//		// �㤤����ʸ̮̾��quote��ˡ�λ��Ѥ�ػߤ�������פˤʤ롣�䢪�ػߤ����Τ��ѻߤ���
//		// $_X $_7 �� *p �ʤɤ�����ͽ��Ȥ��Ƥ��뤿���quote��ɬ�פȤʤäƤ��롣
//		if (name.matches("^[A-Z_].*")) { this.name = "_" + name; }
	}
	/** ����ͽ��̾����ĥץ�������ƥ����Ȥ�������롣
	 * @param name ����ƥ�����̾��_�ǻϤޤ�����ͽ��̾���Ϥ����Ȥ��Ǥ����
	 * @param dummy true���Ϥ����� */
	public SrcProcessContext(String name, boolean dummy) {
		super(name);
	}
	public String getQualifiedName() {
		return "$" + name;
	}
}