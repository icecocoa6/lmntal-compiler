package graphic;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

import javax.swing.*;

import runtime.AbstractMembrane;
import runtime.Env;
import test.GUI.Node;

public class LMNtalWindow extends JFrame{

	public LMNGraphPanel lmnPanel = null;
	public boolean busy = true;
	public boolean running = true;
	public boolean waitawhile = false;
	
	/*������ɥ�������ɬ��*/
	private boolean ready = false; 
	public String name = null;
	int sizex = 0;
	int sizey = 0;
	public int color_r = 255;
	public int color_g = 255;
	public int color_b = 255;
	
	public void setcolor(int a, int b , int c){
		if(a > 255) color_r = 255;
		else if(a < 0)color_r = 0;
		else color_r = a;
		
		if(b > 255) color_g =255;
		else if(b < 0)color_g = 0;
		else color_g = b;
		
		if(c > 255) color_b = 255;
		else if(c < 0)color_b = 0;
		else color_b = c;
	}
	
    public LMNtalWindow(AbstractMembrane m){
    	ready = setatoms(m);
    }
    
	public void setname(int n){
		name = Integer.toString(n);
	}
	public void setname(String n){
		name = n;
	}
    
	public boolean makewindow(){
		if(!ready)return false;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				running = busy = waitawhile = false;
				runtime.LMNtalRuntimeManager.terminateAllThreaded();
				//�Ĥ���ݤˡ�lmnPanel�򻦤���
				if(lmnPanel!=null)
					lmnPanel.stop();
				Env.LMNgraphic.closewindow();
				
			}
		});
		initComponents();
		setSize(sizex,sizey);
		if(Env.getExtendedOption("screen").equals("max")) {
			setExtendedState(Frame.MAXIMIZED_BOTH | getExtendedState());
		}
		setVisible(true);
//		System.out.println("make window");
		return true;
	}
	
	public boolean setgraphicmem(AbstractMembrane m){
		if(lmnPanel == null)return false;
		lmnPanel.setgraphicmem(m);
		return true;
	}
	
	protected void initComponents() {
		lmnPanel = new LMNGraphPanel(this);
//		JButton bt;
		
		setTitle("It's Graphical LMNtal");
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JScrollPane(lmnPanel), BorderLayout.CENTER);
//		getContentPane().add(bt=new JButton("Wait a While"), BorderLayout.SOUTH);
//		bt.addActionListener(new ActionAdapter(this));
	}
	
	/**
	 * �����ѤΥ��ȥ෴�μ���
	 */
	private boolean setatoms(AbstractMembrane m){
		Iterator ite = m.atomIterator();
		Node a;

		while(ite.hasNext()){
			a = (Node)ite.next();
			/**���褹��ե�����μ���*/
			if(a.getName()=="name"){
				if(a.getEdgeCount() != 1)return false;
				setname(a.getNthNode(0).getName());
			}
			/**�������μ���*/
			else if(a.getName()=="size"){
				if(a.getEdgeCount() != 2)return false;
				try{
					sizex = Integer.parseInt(a.getNthNode(0).getName());
				}catch(NumberFormatException error){
					return false;
				}

				try{
					sizey = Integer.parseInt(a.getNthNode(1).getName());
				}catch(NumberFormatException error){
					return false;
					
				}
			}
			/**�طʿ��μ���*/
			else if(a.getName()=="bgcolor"){
				if(a.getEdgeCount() != 3)return false;
				try{
					setcolor(Integer.parseInt(a.getNthNode(0).getName()), Integer.parseInt(a.getNthNode(1).getName()), Integer.parseInt(a.getNthNode(2).getName()));
				}catch(NumberFormatException error){
					return false;
				}
			}
		}
		if(name!=null & sizex > 0 & sizey > 0)
			return true;
		return false;
	}
}