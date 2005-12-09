package graphic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import runtime.AbstractMembrane;
import runtime.Env;
import test.GUI.Node;
/**
 * 
 * @author nakano
 *	������ɥ��򥯥饹�������
 *����������ɥ�������LMNWindow�˰�Ǥ��
 *	�ɤ�Ĥ֤��䡢���֤ʤɤ�LMNGraphPanel�˰�Ǥ��
 *
 */

public class LMNtalGFrame implements Runnable{

	public LMNGraphPanel lmnPanel = null;
	boolean busy;
	Thread th;
	runtime.Membrane rootMem;
	HashMap windowmap=new HashMap();
	LinkedList tmplist = new LinkedList();
	int killednum=0;
	public static Object lock = new Object();
	public static Object lock2 = new Object();
	
    public LMNtalGFrame(){
    	this.start();
    }

	/**
	 * ���ꤵ�줿̾�ΤΥ��ȥब¸�ߤ��뤫������
	 * ����п��򡢤ʤ���е����֤���
	 */
	private String searchatom(AbstractMembrane m){
		synchronized(lock){
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
		}
		return null;
				
	}
	
	public void setRootMem(runtime.Membrane rootMem) {
		this.rootMem = rootMem;
		
	}
	
    public void setmem(AbstractMembrane m){
    	String s = searchatom(m);
    	synchronized (lock) {
    		/*������ɥ������Ͽ*/
	    	if(s == "window"){
	    		setwindowmem(m);    		
	    	}
	    	/*���������Ͽ*/
	    	else if(s=="draw" || s=="graphic"){
	    		setgraphicmem(m);
	    	}
    	}
    }
    
    /***/
   public Point getMousePoint(AbstractMembrane m){
	   String memname = getname(m);
	   if(!windowmap.containsKey(memname)) return null;
	   WindowSet winset = (WindowSet)windowmap.get(memname);
	   return winset.window.getMousePosition();
   }
    /**������ɥ����֥������Ȥ�����*/
    private void setwindowmem(AbstractMembrane m){
		WindowSet win = new WindowSet();
		win.mem = m;
		win.window = new LMNtalWindow(m, this);
		if(!windowmap.containsKey(win.window.name)){
			win.window.makewindow();
			
		windowmap.put(win.window.name, win);
		}
    }
    private void setgraphicmem(AbstractMembrane tmp){
	   synchronized(lock){

			   if(tmp == null || tmp.isRoot())return;
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
						win.window.setgraphicmem(tmp,distance);
						return;
					}
				   /*������ɥ��줬̤��Ͽ*/
				   else{
					   if(searchwinmem(m)){
						   n = getname(m);
						   if(windowmap.containsKey(n)){
							   	WindowSet win = (WindowSet)windowmap.get(n);
								win.window.setgraphicmem(tmp,distance);
								return;
							}
					   }
				   }
					m = m.getParent();
					distance++;
			   }
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
   public synchronized void closewindow(String killme){
	   if(!windowmap.containsKey(killme))
		   return;
	   
	   WindowSet win = (WindowSet)windowmap.get(killme);
	   win.killed = true;
	   //windowmap.put(killme, win);
	   
	   killednum++;
	   if(killednum == windowmap.size()){
		   runtime.LMNtalRuntimeManager.terminateAllThreaded();
		   th=null;  
	   }
   }
   
   /**name���ȥब����Ф���˷Ҥ��ä����ȥ�̾��������ʤ����null���֤���*/
   private String getname(AbstractMembrane m){
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
    
	/**
	 * ����åɴط�
	 */
	public void start() {
		if (th == null) {
			th = new Thread(this);
			th.start();
		}
	}


	public void run() {
		Thread me = Thread.currentThread();
		while (me == th) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
	/** @return �롼�륹��åɤμ¹Ԥ��³���Ƥ褤���ɤ��� */
	public boolean onTrace() {
		if(Env.fGraphic) {
////			lmnPanel.start();
			waitBusy();
////			lmnPanel.stop();
		}
//		return running;
		return true;
	}
	
	public void waitBusy() {
		busy = true;
////		System.out.print("*");
//		while(busy) {
			try {
				th.sleep(1);
				//busy = waitawhile;
			} catch (Exception e) {
			}
//		}
	}
}
/**������ɥ��쥯�饹����ȡ���¸�ե饰��������ɥ����ݻ�*/
class WindowSet{
	public AbstractMembrane mem;
	public LMNtalWindow window;
	public boolean killed = false;
	WindowSet(){
	}
}