package compile.structure;

import java.util.List;
import java.util.ArrayList;

/** �ץ���ʸ̮̾�����դ��ץ���ʸ̮̾���롼��ʸ̮̾�ȡ�����˴ؤ��������ݻ����륯�饹��
 * �Խ��� */

public abstract class ContextDef {
	/** ����ƥ����Ȥ�̾�� */
	protected String name;
	
	/**
	 * ���󥹥ȥ饯��
	 * @param name ����ƥ�����̾
	 */
	protected ContextDef(String name) {
		this.name = name;
	}
	
	/**
	 * ����ƥ����Ȥ�̾�������ޤ�
	 * @return ����ƥ�����̾
	 */
	public String getName() {
		return name;
	}
	
	/** �������и� */
	public Context src;
	
	/** �������Ǥʤ��и�  */
	List rhsMems = new ArrayList();
	
}
