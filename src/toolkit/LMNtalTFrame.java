package toolkit;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import runtime.Atom;
import runtime.Functor;
import runtime.Membrane;
import runtime.SymbolFunctor;

/** --wt�⡼�ɤǼ¹Ԥ����Ȥ��ν��� */
public class LMNtalTFrame {

	final static
	private Functor WINDOW_FUNCTOR = new SymbolFunctor("window",0);

	final static
	private Functor NAME_FUNCTOR = new SymbolFunctor("name",1); 
	
	final static
	private Set UPDATE_COMMAND = new HashSet();
	
	// ������ɥ������ޥåס�������ɥ�̾(String)�򥭡��ˤ��ƴ���
	final private HashMap windowMap = new HashMap();

	/** LMNtalTFrame�Υ��󥹥ȥ饯�� */
	public LMNtalTFrame(){}

	/** �������줿����ݡ��ͥ�Ȥ��ɲä��� */
	public static void addUpdateComponent(LMNComponent component){
		UPDATE_COMMAND.add(component);
	}
	
	/** �ɲä��줿����ݡ��ͥ�Ȥ��ɤ�ǥ��ȥ���ɲä��� */
	private void addAtom(){
		// UPDATE_COMMAND����Ȥ�Iterator�Ȥ���addAtomIte�������
		Iterator addAtomIte = UPDATE_COMMAND.iterator();
		while(addAtomIte.hasNext()){
			// Set�Ǽ�����ä����֥������Ȥ򥭥㥹�Ȥ��ơ�component�������
			LMNComponent component = (LMNComponent)addAtomIte.next();
			component.addAtom();
		}
		UPDATE_COMMAND.clear();
	}
	
	/**
	 * ���Τ��饢���å����줿�줹�٤Ƽ������(=mem)
	 * ������ä���ϥ���ե��å���Ǥ���Ȥϸ¤�ʤ���
	 * (�����å������ľ���˸ƤФ��᥽�å�)
	 */
	public void setMem(Membrane mem){
		addAtom();
		//���ꤵ�줿�ե��󥯥�(window.)���ĥ��ȥ�ο��������,
		//0�ʾ�ʤ餽�Τޤޤ������setWindowMem���Ϥ���
		if(mem.getAtomCountOfFunctor(WINDOW_FUNCTOR)>0){
			setWindowMem(mem);
		}
		// ������ɥ���Ǥʤ������ĿƤ��롼����Ǥʤ����������ޥåפ��ɲ�
		else if(null != mem.getParent() && !mem.getParent().isRoot()){
			LMNtalWindow window = searchWindowMem(mem.getParent());
			if(null != window){ window.setChildMem(mem); }
		}
	}
	
	/**
	 * �����줫�饦����ɥ����õ�����롥
	 * @param mem
	 * @return LMNtalWindow
	 */
	private LMNtalWindow searchWindowMem(Membrane mem){
		while(!mem.isRoot()){
			if(windowMap.containsKey(mem.getGlobalMemID())){
				return (LMNtalWindow)windowMap.get(mem.getGlobalMemID());
			}
			else if(mem.getAtomCountOfFunctor(WINDOW_FUNCTOR)>0){
				//window.�Ȥ������ȥ�򤽤���˻��äƤ�����
				setWindowMem(mem);
				Iterator nameAtomIte = mem.atomIteratorOfFunctor(NAME_FUNCTOR);
				if(!nameAtomIte.hasNext()){ continue; }
				String windowName = ((Atom)nameAtomIte.next()).nth(0);
				return (LMNtalWindow)windowMap.get(windowName);
			}
			mem = mem.getParent();
		}
		return null;
	}

	/**
	 * ������ɥ������Ͽ��Ԥ���
	 * ���������ϥ�����ɥ���Ǥ��뤳�Ȥ��ݾڤ���Ƥ��뤳�ȡ�
	 * ̤��Ͽ����ξ��ϥ�����ɥ����������롤
	 * ��Ͽ�Ѥߤ���ξ������ID�򹹿����롥
	 * @param mem
	 */
	public void setWindowMem(Membrane mem){
		Iterator nameAtomIte = mem.atomIteratorOfFunctor(NAME_FUNCTOR);
		if(nameAtomIte.hasNext()){
			String windowName = ((Atom)nameAtomIte.next()).nth(0);
			// ���Ǥ˥�����ɥ�������
			if(windowMap.containsKey(windowName)){
				LMNtalWindow window = (LMNtalWindow)windowMap.get(windowName);
//				window.resetWindow(mem);
			}
			//��������ɥ���̵��
			else{
				LMNtalWindow window = new LMNtalWindow(mem, windowName);
				windowMap.put(windowName, window);			
			}
		}
	}
	

	/**
	 * ���Τ������줿��򤹤٤Ƽ�����롥
	 * ������ä���ϥ���ե��å���Ǥ���Ȥϸ¤�ʤ���
	 * @param mem
	 */
	public void removeGraphicMem(Membrane mem){
		if(null != mem.getParent()){
			Iterator windowIte = windowMap.values().iterator();
//			while(windowIte.hasNext()){ ((LMNtalWindow)windowIte.next()).removeChildMem(mem); }
		}
	}
	
	public void setRepaint(Membrane mem, boolean flag){
		setMem(mem);
		Iterator nameAtomIte = mem.atomIteratorOfFunctor(NAME_FUNCTOR);
		if(nameAtomIte.hasNext()){
			String windowName = ((Atom)nameAtomIte.next()).nth(0);
			LMNtalWindow window = (LMNtalWindow)windowMap.get(windowName);
//			window.setRepaint(flag);
		}
	}
		
//	/**
//	 * �ޥ����ΰ��֤򸡽Ф��롣�饤�֥��mouse�ǻ���
//	 * @param mem
//	 */
//	  
//	public Point getMousePoint(Membrane mem){
//		if(mem.isRoot())return null;
//		setMem(mem);
//		Iterator nameAtomIte = mem.atomIteratorOfFunctor(NAME_FUNCTOR);
//		if(nameAtomIte.hasNext()){
//			String windowName = ((Atom)nameAtomIte.next()).nth(0);
//			LMNtalWindow window = (LMNtalWindow)windowMap.get(windowName);
//			if(null != window){
//				window.getMousePosition();
//			}
//		}
//		return getMousePoint(mem.getParent());
//	}

	
	public Dimension getWindowSize(Membrane mem){
		if(mem.isRoot())return null;
		setMem(mem);
		Iterator nameAtomIte = mem.atomIteratorOfFunctor(NAME_FUNCTOR);
		if(nameAtomIte.hasNext()){
			String windowName = ((Atom)nameAtomIte.next()).nth(0);
			LMNtalWindow window = (LMNtalWindow)windowMap.get(windowName);
			if(null != window){
				return window.getSize();
			}
		}
		return getWindowSize(mem.getParent());
	}
	

	
	
	
}

