/*
 * ������: 2003/10/22
 */
package runtime;

/**
 * LMNtal �Υᥤ��
 * 
 * <pre>
 * TODO for �ư��ʤ����礵�� 
 * ���ޥ�ɥ饤������ν���
 * �ʥե�����̾�����ꤵ��Ƥ����餽�����¹�
 * �����ꤵ��Ƥʤ��ä��� runREPL() �¹ԡ�
 * 
 * TODO ̾���� FrontEnd �Ǥ������������
 *       �ơ�FrontEnd
 *           ��ľ�� Main
 * </pre>
 * 
 * ������: 2003/10/22
 */
public class FrontEnd {
	/**
	 * ���ƤλϤޤ�
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FrontEnd fe = new FrontEnd();
		/**
		 * TODO ���ޥ�ɥ饤����������ä���ե��������Ȥ���¹�
		 */
		
		//���꤬�ʤ���Ф����Ƥ�
		REPL repl = new REPL();
		repl.run();
	}
}
