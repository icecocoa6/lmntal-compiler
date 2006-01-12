package graphic;

import java.awt.*;
import java.util.*;


import runtime.AbstractMembrane;
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
	
	public LMNGraphPanel lmnPanel = null;
	boolean busy;
	private Thread th;
	runtime.Membrane rootMem;
	HashMap windowmap=new HashMap();
	LinkedList tmplist = new LinkedList();
	int killednum=0;
	public static Object lock2 = new Object();
	long start ,stop,diff;
	public boolean running = true;
//	public LinkedList atomlist = new LinkedList();
	
	
	public LMNtalGFrame(){
	}
	
	/**
	 * �����̾�ΤΥ��ȥब¸�ߤ��뤫������
	 * ����Х��ȥ�̾�򡢤ʤ����null���֤���
	 */
	private synchronized String searchatom(AbstractMembrane m){
		Iterator ite = m.atomIterator();
		Node a;
		
		while(ite.hasNext()){
			a = (Node)ite.next();
			if(a.getName() == "window"){
				return "window";
			}else if(a.getName() == "draw"){
				return "draw";
			}else if(a.getName() == "graphic"){
				return "graphic";
			}else if(a.getName() == "remove"){
				return "remove";
			}
		}
		return null;
		
	}
	
	public void setRootMem(runtime.Membrane rootMem) {
		this.rootMem = rootMem;
		
	}
	
	public synchronized void setmem(AbstractMembrane m){
		
		String s = searchatom(m);
		/*������ɥ������Ͽ*/
		if(s == "window"){
			setwindowmem(m);
//			doAddAtom();		
		}
		/*���������Ͽ*/
		else if(s=="draw" || s=="graphic"){
			setgraphicmem(m);
		}
		/*������κ��*/
		else if(s=="remove"){
			removegraphicmem(m);
		}
	}
	
	/**�ޥ����ΰ��֤򸡽Ф��롣�饤�֥��mouse�ǻ���*/
	public Point getMousePoint(AbstractMembrane m){
		if(m.isRoot())return null;
		String memname = getname(m);
		if(!windowmap.containsKey(memname)) return getMousePoint(m.getParent());
		WindowSet winset = (WindowSet)windowmap.get(memname);
		return winset.window.getMousePosition();
	}
	
	/**������ɥ����֥������Ȥ�����*/
	private void setwindowmem(AbstractMembrane m){
		WindowSet win = new WindowSet();
		win.window = new LMNtalWindow(m, this);
		win.window.setmem(m);
		if(!windowmap.containsKey(win.window.name)){
			win.window.makewindow();
			windowmap.put(win.window.name, win);
		}else{
			WindowSet tmpwin = (WindowSet)windowmap.get(win.window.name);
			tmpwin.window.setmem(m);
			tmpwin.window.timer = win.window.timer;
			tmpwin.window.doAddAtom();
//			WindowSet tmpwin2 = (WindowSet)windowmap.get(win.window.name);
		}
	}
	
	public LMNtalWindow getWindow(String name){
		if(!windowmap.containsKey(name))return null;
		WindowSet tmpwin = (WindowSet)windowmap.get(name);
		return tmpwin.window;
	}
	public synchronized void removegraphicmem(AbstractMembrane tmp){
		if(tmp == null || tmp.isRoot() )return;

		String n = searchwinname(tmp);
		/*������ɥ��줬��Ͽ�Ѥ�*/
		if(windowmap.containsKey(n)){
			WindowSet win=(WindowSet)windowmap.get(n);
			win.window.removegraphicmem(tmp);
			return;
		}
	}
	
	private synchronized void setgraphicmem(AbstractMembrane tmp){
		if(tmp == null || tmp.isRoot() )return;
		AbstractMembrane m = tmp.getParent();
		/*������ɥ���Ȥε�Υ*/
		int distance = 0;
		
		while(m!=null){
			if(m.isRoot())
				return;
			String n = getname(m);
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
				
				win.window.setgraphicmem(tmp,distance);
				
				
				return;
			}
			/*������ɥ��줬̤��Ͽ*/
			else{
				if(searchwinmem(m)){
					n = getname(m);
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
						win.window.setgraphicmem(tmp,distance);
						return;
					}
				}
			}
			m = m.getParent();
			distance++;
		}
	}
	
	/**�Ƶ�Ū�˿����õ����������ɥ����õ����ȯ���Ǥ���п�������ʤ���е����֤���*/
	private boolean searchwinmem(AbstractMembrane m){
		
		String s = searchatom(m);
		/*������ɥ������Ͽ*/
		if(s == "window"){
			setwindowmem(m);
			return true;
		}else{
			if(m.getParent()!=null & !m.getParent().isRoot()){
				return searchwinmem(m.getParent());
			}
		}
		return false;
	}
	
	/**�Ƶ�Ū�˿����õ����������ɥ����õ����ȯ���Ǥ���Х�����ɥ����̾��������ʤ����null���֤���*/
	private String searchwinname(AbstractMembrane m){
		
		String s = searchatom(m);
		/*������ɥ������Ͽ*/
		if(s == "window"){
			return getname(m);
		}else{
			if(m.getParent()!=null & !m.getParent().isRoot()){
				return searchwinname(m.getParent());
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
	public String getname(AbstractMembrane m){
		Iterator ite = m.atomIterator();
		Node a;
		
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
