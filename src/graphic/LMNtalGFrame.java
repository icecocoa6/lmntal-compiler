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
	    		WindowSet win = new WindowSet();
	    		win.mem = m;
	    		win.window = new LMNtalWindow(m, this);
	    		
	    		if(!windowmap.containsKey(win.window.name))
					win.window.makewindow();
	    			
	    		windowmap.put(win.window.name, win);
	    		
	    	}
	    	/*���������Ͽ*/
	    	else if(s=="draw" || s=="graphic"){
	    		if(m == rootMem)return;
	    		else if(m.getParent() == rootMem) return;
	  
	    		if(windowmap.size()==0){
	    			tmplist.add(m);
	    		}
	    		else{
	    			tmplist.addFirst(m);
	//    			searchtmp();
	    		}
	    	}
    	}
    }
    
   private synchronized void searchtmp(){
	   if(tmplist.size()==0)return;
	   
	   for(int j = 0; j < tmplist.size(); j++){
		   AbstractMembrane tmp = (AbstractMembrane)tmplist.get(j);
		   if(tmp == null || tmp.isRoot())return;
		   for(int i = 0; i < windowlist.size(); i++){
			   WindowSet win = (WindowSet)windowlist.get(i);
			   AbstractMembrane m = tmp.getParent();
			   int distance = 0;
			   if(m==null)return;
			   
			   while(!m.isRoot()){
				   String n = getname(m);
					if(win.window.name.equals(n)){
						if(win.window.setgraphicmem(tmp,distance)){
							tmplist.remove(j);
							j--;
							return;
						}
						else
							break;
					}
					m = m.getParent();
					distance++;
			   }
			   
			} 
	   }
   }
   
   public void closewindow(String killme){
	   int j=0;
	   for(int i = 0; i < windowlist.size(); i++){
		   WindowSet win = (WindowSet)windowlist.get(i);
		   if(win.window.name.equals(killme)){
			   win.killed=true;
//			   windowlist.remove(i);
		   }
		   if(win.killed){
			   j++;
			   if(j==windowlist.size()){
				   runtime.LMNtalRuntimeManager.terminateAllThreaded();
				   th=null;  
			   }
		   }
		}
   }
   
   private String getname(AbstractMembrane m){
		Iterator ite = m.atomIterator();
		Node a;

		while(ite.hasNext()){
			a = (Node)ite.next();
			/**���褹��ե�����μ���*/
			if(a.getName()=="name"){
				if(a.getEdgeCount() != 1)return null;
				return a.getNthNode(0).getName().toString();
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
				Thread.sleep(1);
	    		searchtmp();
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

class WindowSet{
	public AbstractMembrane mem;
	public LMNtalWindow window;
	public boolean killed = false;
	WindowSet(){
	}
}