/*
 * ������: 2003/12/16
 *
 */
package runtime;

/**
 * ����饤�󥳡��ɤΥ��󥿡��ե�������
 * �����������륯�饹�ϥ���ѥ��餬��ư�������롣
 * @author hara
 *
 */
public interface InlineCode {
	public boolean runGuard(String guardID, Membrane mem, Object obj) throws Exception;
	public void run(Atom a, int codeID);
}

