package toolkit;

import java.awt.BorderLayout;

import javax.swing.JButton;

import runtime.Membrane;

/**
 * LMNtalWindow�����֤���ܥ���
 * ���֥��������������ˡ���򸡺������ܥ���������Ԥ���
 */
public class LMNtalButton {

	private JButton button;
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	/**
	 * @param lmnWindow �ܥ�������֤��륦����ɥ�
	 * @param mem �����оݤ���
	 */
	public LMNtalButton(LMNtalWindow lmnWindow, Membrane mem){
		System.out.println("button");
		resetMembrane(mem);
		lmnWindow.add(button, BorderLayout.NORTH);
		// ������ɥ� �ι���
		lmnWindow.validate();
	}
	/////////////////////////////////////////////////////////////////

	/**
	 * �줫��ܥ���������ɬ�פʾ����������롥
	 * @param mem �����оݤ���
	 */
	private void resetMembrane(Membrane mem){
		button = new JButton("button");	
	}
}
