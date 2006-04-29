package graphic;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;

import runtime.AbstractMembrane;
import runtime.Atom;
import runtime.Functor;
import runtime.Membrane;

/**
 * Graphic LMNtal�Υᥤ�󥯥饹��
 * ���ΤȤΤ��٤Ƥ��̿��Ϥ��Υ��饹��𤹡�
 * @author nakano
 *
 */
public class LMNtalGFrame{	
	final static
	private Functor WINDOW_MEM = new Functor("window",0);
	
	final static
	private Functor NAME_ATOM = new Functor("name",1); 
	
	// ������ɥ������ޥåס�������ɥ�̾(String)�򥭡��ˤ��ƴ���
	final private HashMap windowMap = new HashMap();

	
	///////////////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	public LMNtalGFrame(){}
	///////////////////////////////////////////////////////////////////////////

	/**
	 * ���Τ��饢���å����줿�줹�٤Ƽ�����롥
	 * ������ä���ϥ���ե��å���Ǥ���Ȥϸ¤�ʤ���
	 * @param mem
	 */
	public void setMem(Membrane mem){
		// window���ȥब����Х�����ɥ������Ͽ
		if(mem.getAtomCountOfFunctor(WINDOW_MEM)>0){
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
	private LMNtalWindow searchWindowMem(AbstractMembrane mem){
		while(!mem.isRoot()){
			if(windowMap.containsKey(mem.getGlobalMemID())){
				return (LMNtalWindow)windowMap.get(mem.getGlobalMemID());
			}
			else if(mem.getAtomCountOfFunctor(WINDOW_MEM)>0){
				setWindowMem(mem);
				Iterator nameAtomIte = mem.atomIteratorOfFunctor(NAME_ATOM);
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
	public void setWindowMem(AbstractMembrane mem){
		Iterator nameAtomIte = mem.atomIteratorOfFunctor(NAME_ATOM);
		if(nameAtomIte.hasNext()){
			String windowName = ((Atom)nameAtomIte.next()).nth(0);
			// ���Ǥ˥�����ɥ�������
			if(windowMap.containsKey(windowName)){
				LMNtalWindow window = (LMNtalWindow)windowMap.get(windowName);
				window.resetWindow(mem);
			}
			//��������ɥ���̵��
			else{
				LMNtalWindow window = new LMNtalWindow(mem);
				windowMap.put(windowName, window);
			}
		}
	}
	
	/**
	 * ���Τ������줿��򤹤٤Ƽ�����롥
	 * ������ä���ϥ���ե��å���Ǥ���Ȥϸ¤�ʤ���
	 * @param mem
	 */
	public void removeGraphicMem(AbstractMembrane mem){
		if(null != mem.getParent()){
			Iterator windowIte = windowMap.values().iterator();
			while(windowIte.hasNext()){ ((LMNtalWindow)windowIte.next()).removeChildMem(mem); }
		}
	}
	
	public void setRepaint(Membrane mem, boolean flag){
		if(windowMap.containsKey(mem.getGlobalMemID())){
			LMNtalWindow window = (LMNtalWindow)windowMap.get(mem.getGlobalMemID());
			window.setRepaint(flag);
		}
	}
	/**
	 * �ޥ����ΰ��֤򸡽Ф��롣�饤�֥��mouse�ǻ���
	 * @param mem
	 */
	  
	public Point getMousePoint(AbstractMembrane mem){
		if(mem.isRoot())return null;
		Iterator nameAtomIte = mem.atomIteratorOfFunctor(NAME_ATOM);
		if(nameAtomIte.hasNext()){
			String windowName = ((Atom)nameAtomIte.next()).nth(0);
			LMNtalWindow window = (LMNtalWindow)windowMap.get(windowName);
			if(null != window){
				window.getMousePosition();
			}
		}
		return getMousePoint(mem.getParent());
	}
	
}
