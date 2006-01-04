package graphic;

import java.awt.*;
import java.util.*;


import runtime.AbstractMembrane;
import runtime.*;
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
	public LinkedList atomlist = new LinkedList();
	
	
	public LMNtalGFrame(){
	}
	
	/**
	 * ���ꤵ�줿̾�ΤΥ��ȥब¸�ߤ��뤫������
	 * ����п��򡢤ʤ���е����֤���
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
			doAddAtom();		
		}
		/*���������Ͽ*/
		else if(s=="draw" || s=="graphic"){
			setgraphicmem(m);
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
//			WindowSet tmpwin2 = (WindowSet)windowmap.get(win.window.name);
		}
	}
	
	public LMNtalWindow getWindow(String name){
		if(!windowmap.containsKey(name))return null;
		WindowSet tmpwin = (WindowSet)windowmap.get(name);
		return tmpwin.window;
	}
	public void setAddAtom(Functor a, String win){
		WaitingAtomSet wa = new WaitingAtomSet(a,win);
		atomlist.add(wa);
	}
	/**�������Ϥǥꥹ�Ȥ��Ѥޤ줿���ȥ��Functor�ˤ�����ɲä���*/
	public void doAddAtom(){
		while(!atomlist.isEmpty()){
			WaitingAtomSet wa = (WaitingAtomSet)atomlist.removeFirst();
			
			/*��ȯ��*/
			if(windowmap.containsKey(wa.window)){
				WindowSet win = (WindowSet)windowmap.get(wa.window);
				Iterator ite = win.window.getmem().atomIterator();
				/*�Ѥޤ줿���ȥ���ɲä���ꥹ�Ȥ򸡺�*/
				while(ite.hasNext()){
					Atom a = (Atom)ite.next();
					if(a.getName()=="keyByChar" || a.getName()=="keyByCode"){
						Atom nth1 = a;
						Atom nth2 = null;
						while(true){
							int nth1_arg=1;
							if(nth1.getFunctor().getArity()==1)
								nth1_arg=0;
							try{
								nth2 = nth1.getArg(nth1_arg).getAtom();
							}catch(ArrayIndexOutOfBoundsException e){
								break;
							}
							if(nth2.getName().equals("[]")){
								Atom data = win.window.getmem().newAtom(wa.functor);
								Atom dot = win.window.getmem().newAtom(new Functor(".", 3));
								win.window.getmem().newLink(dot, 0, data, 0);
								win.window.getmem().newLink(nth1, nth1_arg, dot, 2);
								win.window.getmem().newLink(nth2, 0, dot, 1);
								break;
							}
							nth1 = nth2;
						}
						break;
					}
				}
			}
			
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
				searchwinmem(m.getParent());
			}
		}
		return false;
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

class WaitingAtomSet{
	public Functor functor;
	public String window;
	public WaitingAtomSet(Functor f, String w){
		functor=f;
		window=w;
	}
}