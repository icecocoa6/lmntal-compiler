package toolkit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import runtime.Atom;
import runtime.Functor;
import runtime.Membrane;
import runtime.SymbolFunctor;

/**
 * LMNtalWindow�����֤���ܥ���
 * ���֥��������������ˡ���򸡺������ܥ���������Ԥ���
 */
public class LMNtalButton extends LMNComponent implements ActionListener {

	private JButton button;
	private String label;
	
	private int clickedCounter = 0;
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	/**
	 * @param lmnWindow �ܥ�������֤��륦����ɥ�
	 * @param mem �����оݤ���
	 */
	public LMNtalButton(LMNtalWindow lmnWindow, Membrane mem){
		super(lmnWindow, mem);
		System.out.println("button");		
	}
	/////////////////////////////////////////////////////////////////

	/**
	 * �줫��ܥ���������ɬ�פʾ����������롥
	 * @param mem �����оݤ���
	 */
	
	public Component initComponent(){
		button = new JButton(label);
		button.addActionListener(this); //button��ActionListener�ƤӽФ�
		return button;
	}
	
	public void setMembrane(Membrane mem){
		label = getLabel(mem);
		if(button != null && getLabelUpdate()){
			setLabelUpdate(false);
			button.setText(label);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
//		System.out.println("clicked.");
		LMNtalTFrame.addUpdateComponent(this);
		clickedCounter++;
	}
	
	public void addAtom(){
		for(;clickedCounter > 0; clickedCounter--){
			Membrane mem = getMymem(); //���������
			Functor func = new SymbolFunctor("clicked", 0); //clicked.����
			mem.addAtom(new Atom(mem, func)); //func�򸵤ˤ��ƺ�ä����ȥ��mem���ɲä���
			//����addAtom��Membrane��addAtom��toolkit���addAtom�Ȥ���ʪ��
		}
		clickedCounter = 0;
	}
}
