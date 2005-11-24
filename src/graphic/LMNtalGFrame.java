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
	LinkedList windowlist=new LinkedList();
	LinkedList tmplist = new LinkedList();
	
	
    public LMNtalGFrame(){
    	this.start();
    }

	/**
	 * ���ꤵ�줿̾�ΤΥ��ȥब¸�ߤ��뤫������
	 * ����п��򡢤ʤ���е����֤���
	 */
	private String searchatom(AbstractMembrane m){
		Iterator ite = m.atomIterator();
		Node a;

		while(ite.hasNext()){
			a = (Node)ite.next();
			if(a.getName() == "window"){
				return "window";
			}else if(a.getName() == "remove"){
				return "remove";
			}else if(a.getName() == "draw"){
				return "draw";
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
    		WindowSet win = new WindowSet();
    		win.mem = m;
    		win.window = new LMNtalWindow(m);
    		for(int i = 0; i < windowlist.size(); i++){
    			WindowSet win2 = (WindowSet)windowlist.get(i);
    			if(win.window.name.equals(win2.window.name)){
    				break;
    			}
    			if(i == windowlist.size() - 1){
    				windowlist.add(win);
    				win.window.makewindow();
    				break;
    			}
    		}
    		if(windowlist.size()==0){
				windowlist.add(win);
				win.window.makewindow();
    		}
    	}
    	/*���������Ͽ*/
    	else if(s=="draw"){
    		if(m == rootMem)return;
    		else if(m.getParent() == rootMem) return;
  
    		if(windowlist.size()==0){
    			tmplist.add(m);
    		}
    		else{
    			tmplist.addFirst(m);
    			searchtmp();
    		}
    	}
    }
    
   private void searchtmp(){
	   if(tmplist.size()==0)return;
	   AbstractMembrane tmp = (AbstractMembrane)tmplist.removeFirst();
	   if(tmp == null || tmp.isRoot())return;
	   for(int i = 0; i < windowlist.size(); i++){
		   WindowSet win = (WindowSet)windowlist.get(i);
		   AbstractMembrane m = tmp.getParent();
		   if(m==null)return;
		   while(!m.isRoot()){
			   String n = getname(m);
				if(win.window.name.equals(n)){
					if(win.window.setgraphicmem(tmp))
						return;
					else
						break;
				}
				m = m.getParent();
		   }
		} 

	   tmplist.add(tmp);
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
				Thread.sleep(100);
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
	WindowSet(){
	}
}