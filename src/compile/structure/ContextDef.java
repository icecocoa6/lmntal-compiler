package compile.structure;

import java.util.List;
import java.util.ArrayList;

/** �ץ���ʸ̮̾�����դ��ץ���ʸ̮̾���롼��ʸ̮̾�ȡ�����˴ؤ��������ݻ����륯�饹��*/

public class ContextDef {
	/** ����ƥ����Ȥ�̾�� */
	protected String name;
	/** ���դ��ץ�������ƥ����Ȥ��ɤ������Ǽ���� */
	public boolean typed = false;
	
	/**
	 * ���󥹥ȥ饯��
	 * @param name ����ƥ����Ȥθ���̾
	 */
	public ContextDef(String name) {
		this.name = name;
	}
	
	/**
	 * ����ƥ����Ȥθ���̾���������
	 * @return ����ƥ����Ȥθ���̾
	 */
	public String getName() {
		return name;
	}
	public boolean isTyped() {
		return typed;
	}
	/** �������и��ʱ��դǤ��������˻Ȥ����ꥸ�ʥ�ؤλ��ȡˤޤ���null
	 * null�ΤȤ����롼�륳��ѥ���ϥ����ɽи����������Ƥ褤��*/
	public Context src = null;
	
	/** ���դǤΥ���ƥ����Ƚи� (Context) �Υꥹ�� */
	public List rhsOccs = new ArrayList();
	
}
