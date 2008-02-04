package toolkit;

import java.awt.Component;

import javax.swing.JLabel;

import runtime.Membrane;
import util.Util;

/**
 * LMNtalWindow�����֤����٥�
 * ���֥��������������ˡ���򸡺�������٥�������Ԥ���
 */
public class LMNtalLabel extends LMNComponent {

	private JLabel label;
	private String text;
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	/**
	 * @param lmnWindow �ܥ�������֤��륦����ɥ�
	 * @param mem �����оݤ���
	 */
	public LMNtalLabel(LMNtalWindow lmnWindow, Membrane mem){
		super(lmnWindow, mem);
	}
	/////////////////////////////////////////////////////////////////

	/**
	 * �줫��ܥ���������ɬ�פʾ����������롥
	 * @param mem �����оݤ���
	 */
	
	public Component initComponent(){
		label = new JLabel(text);
		return label;
	}
	
	public void setMembrane(Membrane mem){
		text = getText(mem);
		if(label != null && getTextUpdate()){
			setTextUpdate(false);
			label.setText(text);
		}
	}
		
}
