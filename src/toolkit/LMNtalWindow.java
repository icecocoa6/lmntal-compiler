package toolkit;

import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

import javax.swing.*;

import runtime.Membrane;
import runtime.Atom;
import runtime.Dumper;
import runtime.Functor;
import runtime.SymbolFunctor;

public class LMNtalWindow extends JFrame {
	
	/////////////////////////////////////////////////////////////////
	// ������Functor
	
	final static
	private Functor NAME_ATOM = new SymbolFunctor("name",1); 

	final static
	private Functor SIZE_ATOM = new SymbolFunctor("size", 2);
	
	final static
	private Functor KILLER_ATOM = new SymbolFunctor("killer", 0);
	
	final static
	private Functor BUTTON_FUNCTOR = new SymbolFunctor("button",0);
	
	final static
	private Functor TEXTAREA_FUNCTOR = new SymbolFunctor("textarea",0);
	
	/////////////////////////////////////////////////////////////////

	// ������ɥ����Ĥ�����ȡ��ץ�������Ū�˽�λ�����뤫�ɤ����Υե饰
	private boolean killer = false;

	private String memID;
	private Membrane mymem;
	private String windowName;
	private int sizeX = 0;
	private int sizeY = 0;
	private GridBagLayout layout;

	private boolean sizeUpdate = false;

	
	///////////////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	public LMNtalWindow(Membrane mem, String name){
		resetMembrane(mem);
		windowName = name;
		makeWindow();
	}
	///////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * ��ξ���򤹤٤Ƽ������������ꤹ�롥
	 * ���������ϥ�����ɥ���Ǥ��뤳���ݾڤ�����Ƥ��뤳�ȡ�
	 * @param mem
	 */
	public void resetMembrane(Membrane mem){
		Iterator atomIte;
		Atom targetAtom;
		
		mymem = mem;
		// membrane ID
		memID = mem.getGlobalMemID();
				
		// name atom
		atomIte= mem.atomIteratorOfFunctor(NAME_ATOM);
		if(atomIte.hasNext()){
			targetAtom = (Atom)atomIte.next();
			windowName = ((null != targetAtom) ? targetAtom.nth(0) : "");
		}
			
		// size���ȥࡡ�����
		setSizeAtom(mem);
		
		// killer���ȥ�����
		setKiller(mem);
//		doAddAtom(mem);
	}
	
	/**
	 * component��̵ͭ��Ƚ��
	 */
	public void setChildMem(Membrane mem){
	
		if(mem.getAtomCountOfFunctor(BUTTON_FUNCTOR)>0){
			LMNtalButton button = new LMNtalButton(this, mem);
		}

		if(mem.getAtomCountOfFunctor(TEXTAREA_FUNCTOR)>0){
			LMNtalTextArea textarea = new LMNtalTextArea(this, mem);
		}

	}
	
	/**
	 * ����Υ��ȥ�򸡺�����"size"���ȥ��������롥
	 * ���ȥ�������������������󥯤�⤵�Ȥ��롥
	 * @param mem �����оݤ���
	 */
	private void setSizeAtom(Membrane mem){
		Atom targetAtom;
		Iterator atomIte= mem.atomIteratorOfFunctor(SIZE_ATOM);
		if(atomIte.hasNext()){
			targetAtom = (Atom)atomIte.next();
			try{
				if( (null != targetAtom) &&
					( (sizeX != Integer.parseInt(targetAtom.nth(0))) ||
					  (sizeY != Integer.parseInt(targetAtom.nth(1))) )){
					sizeUpdate = true;
				}
				sizeX = ((null != targetAtom) ? Integer.parseInt(targetAtom.nth(0)) : 0);
				sizeY = ((null != targetAtom) ? Integer.parseInt(targetAtom.nth(1)) : 0);
			}
			catch(NumberFormatException e){}
		}
		
	}

	/**
	 * ����Υ��ȥ�򸡺�����"killer"���ȥ��������롥
	 * "killer"���ȥब¸�ߤ������ϡ�killer�ե饰��Ω�Ƥ�
	 * @param mem �����оݤ���
	 */
	private void setKiller(Membrane mem) {
		// killer
		Iterator atomIte= mem.atomIteratorOfFunctor(KILLER_ATOM);
		if(atomIte.hasNext()){
			killer = true;
		}
	}
	
	
	/** ������ɥ����������� */
	public void makeWindow(){
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		System.out.println("make window");
		setTitle(windowName);
		layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		
		setSize(sizeX, sizeY);
		
		// killer��true�ΤȤ���������ɥ����Ĥ���ȡ��ץ���������λ������
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// �Ĥ���ݤν���
				if(killer){
					String dump = Dumper.dump(mymem.getTask().getRoot()).toString();
					dump = dump.replaceAll("\\}", "\\}\n");
					dump = dump.replaceAll("\\{", "\n\\{");
					dump = dump.substring(1);
					System.out.println(dump);
					System.exit(0);
				}
			}
		});

		setVisible(true);
	}
	
	public GridBagLayout getGridBagLayout(){
		return layout;
	}
	
	
}