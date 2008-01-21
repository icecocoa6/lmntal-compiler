package toolkit;

import javax.swing.Timer;
import java.awt.event.*;
import java.util.Iterator;

import runtime.Atom;
import runtime.Functor;
import runtime.Membrane;
import runtime.SymbolFunctor;


/**
 * LMNtalWindow�����֤���ܥ���
 * ���֥��������������ˡ���򸡺������ܥ���������Ԥ���
 */
//public class LMNtalTimer extends LMNComponent  implements ActionListener {
public class LMNtalTimer implements ActionListener {

	final static
	private Functor TIME_FUNCTOR = new SymbolFunctor("time",1);
	
	private Timer timer;
	private Membrane mymem; //���äƤ��������¸���롣
	
	int timeCount = 0;
	
	private int time;
	
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	/**
	 * @param lmnWindow �ܥ�������֤��륦����ɥ�
	 * @param mem �����оݤ���
	 */
	public LMNtalTimer(LMNtalWindow lmnWindow, Membrane mem){
//		super(lmnWindow, mem);
		mymem = mem;
		init();
//		System.out.println("timer");
	}
	/////////////////////////////////////////////////////////////////


	/**
	 * �줫��ܥ���������ɬ�פʾ����������롥
	 * @param mem �����оݤ���
	 */
//	public Component initComponent(){
		
	public void init(){
		time = getTime(mymem);
//		System.out.println("time is :" + time);
		timer = new Timer(time, this);
//		timer.setRepeats(false); //���٥�Ȥ�������Τϣ��٤���
		timer.start();
	}


//	public void setMembrane(Membrane mem){
//		time = getTime(mem);
//		if(timer != null && getTextUpdate()){
//			setTextUpdate(false);
//			button.setText(text);
//		}
//	}
	

	public int getTime(Membrane mem){
		int time = 0;
		Iterator timeAtomIte = mem.atomIteratorOfFunctor(TIME_FUNCTOR);
		if(timeAtomIte.hasNext()){
			Atom atom = (Atom)timeAtomIte.next();
			time = Integer.parseInt(atom.nth(0));	
		}
		return time;
	}	
	
	public void actionPerformed(ActionEvent e) {
//		LMNtalTFrame.addUpdateComponent(this);
		timeCount++;
		addAtom();
	}

	public void addAtom(){ //timerư����������timeover������
		for(;timeCount > 0; timeCount--){
			Membrane mem = mymem; //���������
			Functor func = new SymbolFunctor("timeover", 0); //timeover.����
			mem.addAtom(new Atom(mem, func)); //func�򸵤ˤ��ƺ�ä����ȥ��mem���ɲä���
			//����addAtom��Membrane��addAtom��toolkit���addAtom�Ȥ���ʪ��
		}
		timeCount = 0;
	}


}
