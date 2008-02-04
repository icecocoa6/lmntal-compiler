package toolkit;

import java.awt.Component;

import runtime.Membrane;
import util.Util;

/**
 * LMNtalWindow�����֤����٥�
 * ���֥��������������ˡ���򸡺�������٥�������Ԥ���
 */
public class LMNtalGraphic extends LMNComponent {

	private LMNtalPanel panel;
	
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	/**
	 * @param lmnWindow �ܥ�������֤��륦����ɥ�
	 * @param mem �����оݤ���
	 */
	public LMNtalGraphic(LMNtalWindow lmnWindow, Membrane mem){
		super(lmnWindow, mem);
	}
	/////////////////////////////////////////////////////////////////

	/**
	 * �줫��ܥ���������ɬ�פʾ����������롥
	 * @param mem �����оݤ���
	 */
	
	public Component initComponent(){
		panel = new LMNtalPanel(getMymem());
		return panel;
	}
	
	public void setChildMem(Membrane mem){
		panel.setChildMem(mem);
	}
}
