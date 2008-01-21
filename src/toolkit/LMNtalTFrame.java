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

	/** �ä���������Ȥ���� */
	final private HashSet<String> removedIDSet = new HashSet<String>();
		
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
	 * (�����å������ľ���˸ƤФ��᥽�å�) runtime.Membrane��unlock(boolean)
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
			if(windowMap.containsKey(mem.getMemID())){
				return (LMNtalWindow)windowMap.get(mem.getMemID());
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
		if(mem.getAtomCountOfFunctor(NAME_FUNCTOR)==0){ // �������ʤ��ä��饨�顼�Ф��ƽ�λ
			System.out.println("Name Error. Plaese make Name_Atom with one argument");
			System.exit(0);
		}
		Iterator nameAtomIte = mem.atomIteratorOfFunctor(NAME_FUNCTOR);
		if(nameAtomIte.hasNext()){
			String windowName = ((Atom)nameAtomIte.next()).nth(0);
			// ���Ǥ˥�����ɥ�������
			if(windowMap.containsKey(windowName)){
				LMNtalWindow window = (LMNtalWindow)windowMap.get(windowName);
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
	public void addRemovedMem(Membrane mem){ // �ä���ľ���ν���(runtime.Membrane���)
		String id = mem.getMemID(); // ���դ����ID�����
		String componentID = LMNtalWindow.getmemIDMapKey(id); //�ä�������б����륳��ݡ��ͥ�Ȥ�ID�����
		if(componentID != null)
			removedIDSet.add(componentID); // remove���줿���ID��removedIDSet����˻Ĥ�
		LMNtalWindow.removememIDMap(id);
	}
	
	public void removeMem(){ // �롼��Ŭ�Ѹ�ν���(runtime.InterpretedRuleset���)
		Iterator<String> ids = removedIDSet.iterator();
		while(ids.hasNext()){
			String id = ids.next();
			if(id == null){
				return;
			}
			Iterator<LMNtalWindow> windows = windowMap.values().iterator();
			while(windows.hasNext()){
				windows.next().removeChildMem(id); // ���դˤ��륳��ݡ��ͥ�Ⱦä�
			}
		}
		removedIDSet.clear();
		Iterator<LMNtalWindow> windows = windowMap.values().iterator();
		while(windows.hasNext()){
			windows.next().setVisible(true); // �����Ǻ�����
		}		
		System.out.println("remove");
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

