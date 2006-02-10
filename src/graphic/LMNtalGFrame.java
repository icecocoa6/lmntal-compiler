package graphic;

import java.awt.*;
import java.util.*;


import runtime.AbstractMembrane;
import runtime.Functor;
import test.GUI.Node;
/**
 * 
 * @author nakano
 *	������ɥ��򥯥饹�������
 *����������ɥ�������LMNWindow�˰�Ǥ��
 *	�ɤ�Ĥ֤��䡢���֤ʤɤ�LMNGraphPanel�˰�Ǥ��
 *
 */

public class LMNtalGFrame{
	
	boolean busy;
	private Thread th;
	runtime.Membrane rootMem;
	HashMap windowmap=new HashMap();
//	LinkedList tmplist = new LinkedList();
	int killednum=0;
	long start ,stop,diff;
	public boolean running = true;
	private final Functor DRAW_MEM = new Functor("draw",0); 
	private final Functor WINDOW_MEM = new Functor("window",0); 
	private final Functor RELATIVE_MEM = new Functor("relative",0); 
	private final Functor NAME_ATOM = new Functor("name",1); 
	
	
	/**
	 * �����̾�ΤΥ��ȥब¸�ߤ��뤫������
	 * ����Х��ȥ�̾�򡢤ʤ����null���֤���
	 */
	private synchronized String searchAtom(AbstractMembrane m){
;
		if(m.getAtomCountOfFunctor(WINDOW_MEM)>0){
			return "window";
		}else if(m.getAtomCountOfFunctor(DRAW_MEM)>0){
			return "draw";
		}else if(m.getAtomCountOfFunctor(RELATIVE_MEM)>0){
			return "relative";
		}
//			Iterator ite = m.atomIterator();
//			Node a;
////			m.getAtomCountOfFunctor()
//			while(ite.hasNext()){
//				a = (Node)ite.next()
////			else if(a.getName() == "remove"){
////				return "remove";
////			}
//		}
		return null;
		
	}
	
	public void setRootMem(runtime.Membrane rootMem) {
		this.rootMem = rootMem;
		
	}
	
	public synchronized void setMem(AbstractMembrane m){

//		String s = searchAtom(m);

		/*���������Ͽ*/
		if(m.getAtomCountOfFunctor(DRAW_MEM)>0){
			setGraphicMem(m);
		}
		/*������ɥ������Ͽ*/
		else if(m.getAtomCountOfFunctor(WINDOW_MEM)>0){
			setWindowMem(m);
//			doAddAtom();		
		}
		/*������κ��*/
		else if(m.getAtomCountOfFunctor(RELATIVE_MEM)>0){
			removeGraphicMem(m);
		}


	}
	
	/**�ޥ����ΰ��֤򸡽Ф��롣�饤�֥��mouse�ǻ���*/
	public Point getMousePoint(AbstractMembrane m){
		if(m.isRoot())return null;
		String memname = getName(m);
		if(!windowmap.containsKey(memname)) return getMousePoint(m.getParent());
		WindowSet winset = (WindowSet)windowmap.get(memname);
		return winset.window.getMousePosition();
	}
	/**�ޥ����ΰ��֤򸡽Ф��롣�饤�֥��mouse�ǻ���*/
	public void setNoRepaint(AbstractMembrane m, boolean f){
		if(m.isRoot())return;
		String memname = getName(m);
		if(!windowmap.containsKey(memname)) setNoRepaint(m.getParent(),f);
		WindowSet winset = (WindowSet)windowmap.get(memname);
		winset.window.setNoRepaint(f);
	}
	/**������ɥ����֥������Ȥ�����*/
	private void setWindowMem(AbstractMembrane m){
		WindowSet win = new WindowSet();
		win.window = new LMNtalWindow(m, this);
		win.window.setMem(m);
		if(!windowmap.containsKey(win.window.getName())){
			win.window.makeWindow();
			windowmap.put(win.window.getName(), win);
//			long start ,stop,diff;
//
//			start = System.currentTimeMillis();
			/*���Ǥ˹������ߤ���Ƥ���С�õ���Τ�*/
			if(!win.window.getNoRepaint()){
				win.window.setNoRepaint(true);
				searchAllMem(m);
				win.window.setNoRepaint(false);
			}else
				searchAllMem(m);
				

//			stop = System.currentTimeMillis();
//			diff = stop - start;
//			System.out.println("searchall�¹Ի��� : "+diff+"�ߥ���");
			
			win.window.repaint();
			
		}else{
			WindowSet tmpwin = (WindowSet)windowmap.get(win.window.getName());
			tmpwin.window.setMem(m);
			tmpwin.window.timer = win.window.timer;
			tmpwin.window.doAddAtom();
//			WindowSet tmpwin2 = (WindowSet)windowmap.get(win.window.name);
		}
	}
	
	private void searchAllMem(AbstractMembrane m){
		Iterator ite = m.memIterator();
		while(ite.hasNext()){
			AbstractMembrane mem=(AbstractMembrane)ite.next();
			setMem(mem);
			searchAllMem(mem);
		}
	}
	
	public LMNtalWindow getWindow(String name){
		if(!windowmap.containsKey(name))return null;
		WindowSet tmpwin = (WindowSet)windowmap.get(name);
		return tmpwin.window;
	}
	public synchronized void removeGraphicMem(AbstractMembrane tmp){
		if(tmp == null || tmp.isRoot() )return;

		String n = searchWinName(tmp);
		/*������ɥ��줬��Ͽ�Ѥ�*/
		if(windowmap.containsKey(n)){
			WindowSet win=(WindowSet)windowmap.get(n);
			win.window.removeGraphicMem(tmp);
			return;
		}
	}
	
	private synchronized void setGraphicMem(AbstractMembrane tmp){
		if(tmp == null || tmp.isRoot() )return;
		AbstractMembrane m = tmp.getParent();
		/*������ɥ���Ȥε�Υ*/
		int distance = 0;
		
		while(m!=null){
			if(m.isRoot())
				return;


			String n = getName(m);

			/*������ɥ��줬��Ͽ�Ѥ�*/
			if(windowmap.containsKey(n)){
				WindowSet win = (WindowSet)windowmap.get(n);
				
				/*����ֳ֤�¬����*/
				stop = System.currentTimeMillis();
				diff = stop - start;
				long diff2 = win.window.timer - diff;
				if(diff2 > 0)
					waitBusy(diff2);
//				System.out.println("�¹Ի��� : "+diff+"+"+diff2+"="+(diff+diff2)+"�ߥ���");
				start = System.currentTimeMillis();
				
				win.window.setGraphicMem(tmp,distance);
				
				
				return;
			}
			/*������ɥ��줬̤��Ͽ*/
			else{
				if(searchWinMem(m)){
					n = getName(m);
					if(windowmap.containsKey(n)){
						
						WindowSet win = (WindowSet)windowmap.get(n);
						/*����ֳ֤�¬����*/
						stop = System.currentTimeMillis();
						diff = stop - start;
						long diff2 = win.window.timer - diff;
						if(diff2 > 0)
							waitBusy(diff2);
//						System.out.println("�¹Ի��� : "+diff+"+"+diff2+"="+(diff+diff2)+"�ߥ���");
						start = System.currentTimeMillis();
						win.window.setGraphicMem(tmp,distance);
						return;
					}
				}
			}
			m = m.getParent();
			distance++;
		}
	}
	
	/**�Ƶ�Ū�˿����õ����������ɥ����õ����ȯ���Ǥ���п�������ʤ���е����֤���*/
	private boolean searchWinMem(AbstractMembrane m){
		
		String s = searchAtom(m);
		/*������ɥ������Ͽ*/
		if(s == "window"){
			setWindowMem(m);
			return true;
		}else{
			if(m.getParent()!=null & !m.getParent().isRoot()){
				return searchWinMem(m.getParent());
			}
		}
		return false;
	}
	
	/**�Ƶ�Ū�˿����õ����������ɥ����õ����ȯ���Ǥ���Х�����ɥ����̾��������ʤ����null���֤���*/
	private String searchWinName(AbstractMembrane m){
		
//		String s = searchAtom(m);
		/*������ɥ������Ͽ*/
		if(m.getAtomCountOfFunctor(WINDOW_MEM)>0){
			return getName(m);
		}else{
			if(m.getParent()!=null & !m.getParent().isRoot()){
				return searchWinName(m.getParent());
			}
		}
		return null;
	}
	
	/**������ɥ����Ĥ���줿�Ȥ���ư����٤ƤΥ�����ɥ����Ĥ����齪λ��*/
	public void closewindow(String killme){
		if(!windowmap.containsKey(killme)){
			return;
		}
		
		WindowSet win = (WindowSet)windowmap.get(killme);
		win.killed = true;
		
		killednum++;
		if(killednum == windowmap.size()){
			busy=true;
			runtime.LMNtalRuntimeManager.terminateAllThreaded();
			System.exit(0);
		}
		
	}
	
	/**name���ȥब����Ф���˷Ҥ��ä����ȥ�̾��������ʤ����null���֤���*/
	public String getName(AbstractMembrane m){
//		Iterator ite = m.atomIterator();
		Iterator ite = m.atomIteratorOfFunctor(NAME_ATOM);
		Node a;
//		System.out.println("memname"+m.getLocalID());
		while(ite.hasNext()){
			a = (Node)ite.next();
			/**���褹��ե�����μ���*/
			if(a.getName()=="name"){
				if(a.getEdgeCount() != 1)return null;
				return a.getNthNode(0).getName();
			}
		}
		return null;
	}
	
	/** @return �롼�륹��åɤμ¹Ԥ��³���Ƥ褤���ɤ��� */
	public boolean onTrace() {
		if(busy)return false;
		return true;
	}
	
	public void waitBusy(long s) {	
		try {
			th.sleep(s);
		} catch (InterruptedException e) {}
	}
}
/**������ɥ��쥯�饹����ȡ���¸�ե饰��������ɥ����ݻ�*/
class WindowSet{
	public LMNtalWindow window;
	public boolean killed = false;
}
