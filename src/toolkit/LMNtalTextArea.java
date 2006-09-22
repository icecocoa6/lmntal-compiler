package toolkit;

import java.awt.Component;

import javax.swing.JTextArea;

import runtime.Membrane;

/**
 * LMNtalWindow�����֤���ܥ���
 * ���֥��������������ˡ���򸡺������ܥ���������Ԥ���
 */
public class LMNtalTextArea extends LMNComponent{

	private JTextArea textarea;
	private String text;
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	/**
	 * @param lmnWindow �ƥ����ȥ��ꥢ���ɲä��륦����ɥ�
	 * @param mem �����оݤ���
	 */
	public LMNtalTextArea(LMNtalWindow lmnWindow, Membrane mem){
		super(lmnWindow, mem);
		System.out.println("textarea");		
	}
	/////////////////////////////////////////////////////////////////

	/**
	 * �줫��ܥ���������ɬ�פʾ����������롥
	 * @param mem �����оݤ���
	 */
	
	public Component initComponent(){
		textarea = new JTextArea(text);
		return textarea;
	}
	
	public void setMembrane(Membrane mem){
		text = getText(mem);
	}
}