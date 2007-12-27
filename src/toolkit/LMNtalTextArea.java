package toolkit;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import runtime.Atom;
import runtime.Membrane;
import runtime.StringFunctor;
import util.Util;

/**
 * LMNtalWindow�����֤���ƥ����ȥ��ꥢ
 * ���֥��������������ˡ���򸡺������ƥ����ȥ��ꥢ������Ԥ���
 */
public class LMNtalTextArea extends LMNComponent implements KeyListener {

	private JTextArea textarea;
	private JScrollPane scroll;
	private String text;
//	private Atom textatom;

//	final static
//	private Functor TEXT_ATOM = new SymbolFunctor("text",1);
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	/**
	 * @param lmnWindow �ƥ����ȥ��ꥢ���ɲä��륦����ɥ�
	 * @param mem �����оݤ���
	 */
	public LMNtalTextArea(LMNtalWindow lmnWindow, Membrane mem){
		super(lmnWindow, mem);
		Util.println("textarea");
	}
	/////////////////////////////////////////////////////////////////

	/**
	 * �줫��ܥ���������ɬ�פʾ����������롥
	 * @param mem �����оݤ���
	 */
	
	public Component initComponent(){
		textarea = new JTextArea(text);
		textarea.addKeyListener(this); // textarea��KeyListener�ƤӽФ�
		textarea.setLineWrap(true);    // �ޤ��֤�������ON
		scroll = new JScrollPane(textarea);
		return scroll;
	}
	
	public void setMembrane(Membrane mem){
		text = getText(mem);
		if(textarea != null){
			textarea.setText(text);
		}
	}
		
	public void addAtom(){
		if (textatom == null) return;
		text = textarea.getText();
		Atom reatom = textatom.nthAtom(0); // ������륢�ȥ�
		// ����������Ƥ�text���ɲä��롣StringFunctor�ˤ�ɬ����󥯤ҤȤġ�
		Atom newatom = getMymem().newAtom(new StringFunctor(text));
        // newatom��0���ܤȡ�reatom��0���ܤ���ˤĤʤ��äƤ��ΤȤĤʤ��롣
		getMymem().relink(newatom, 0, reatom, 0);
		reatom.remove(); // ��󥯤��ڤ줿reatom�������롣
	}

	public void keyTyped(KeyEvent arg0) {
		LMNtalTFrame.addUpdateComponent(this);
	}
	
/*	** text("")�Υ��ȥब���ä��Ȥ���text�����Ƥ�������� *
	public String getText(Membrane mem){
		String text = "";
		Iterator textAtomIte = mem.atomIteratorOfFunctor(TEXT_ATOM);
		if(textAtomIte.hasNext()){
			textatom = (Atom)textAtomIte.next();
			text = textatom.nth(0);
		}
		return text;
	}
*/
	
	public void keyPressed(KeyEvent arg0) {}
	public void keyReleased(KeyEvent arg0) {}
	
}