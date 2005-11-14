package graphic;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;

import runtime.AbstractMembrane;
import test.GUI.Node;
/**
 * 
 * @author nakano
 * LMNGraphPanel ���طʤ��ɤ�Ĥ֤�
 */
public class LMNGraphPanel extends JPanel implements Runnable {
	LMNtalGFrame frame;
	private Thread th = null;
	private Image OSI = null;
	private Graphics OSG = null;
	runtime.Membrane rootMem;
	
	public LMNGraphPanel(LMNtalGFrame f) {
		super();
		frame = f;
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				OSI = createImage((int) getSize().getWidth(), (int) getSize().getHeight());
				OSG = OSI.getGraphics();			
			}

		});
		//start();
	}
	public void paint(Graphics g) {

		//���̤����Ϥǽ�������ɤ�Ĥ֤���
		OSG.setColor(Color.WHITE);
		OSG.fillRect(0,0,(int) getSize().getWidth(), (int) getSize().getHeight());
		g.drawImage(OSI,0,0,this);
		paintlayout();
	}
	/**
	 * ���ꤵ�줿̾�ΤΥ��ȥब¸�ߤ��뤫������
	 * ����п��򡢤ʤ���е����֤���
	 */
	private boolean searchatom(AbstractMembrane m , String sstring){
		Iterator ite = m.atomIterator();
		Node a;
		LinkedList atoms = new LinkedList();
		
		while(ite.hasNext()){
			atoms.add(ite.next());
		}
		
		while(atoms.size() > 0){
			a = (Node)atoms.removeFirst();
			if(a.getName() == sstring){
				return true;
			}
		}
		return false;
				
	}
	
	/**
	 * �����ѤΥ��ȥ෴�μ���
	 */
	private GraphicAtoms getgraphicatoms(AbstractMembrane m){
		Iterator ite = m.atomIterator();
		Node a;
		LinkedList atoms = new LinkedList();
		GraphicAtoms ga = new GraphicAtoms();
		
		while(ite.hasNext()){
			atoms.add(ite.next());
		}
		
		while(atoms.size() > 0){
			a = (Node)atoms.removeFirst();
			if(a.getName()=="getpic"){
				if(a.getEdgeCount() != 1)continue;
				ga.SetPic( a.getNthNode(0).getName() );
//				System.out.print(ga.filename + "\n");
			}else if(a.getName()=="position"){
				if(a.getEdgeCount() != 2)continue;
				ga.posx = Integer.parseInt(a.getNthNode(0).getName());
				ga.posy = Integer.parseInt(a.getNthNode(1).getName());
//				System.out.print(ga.posx + "," + ga.posy + "\n");
				
			}else if(a.getName()=="enable"){
				ga.enable = true;
//				System.out.print("find_enable\n");
				
			}
		}
		return ga;
	}
	
	
	/**
	 * �ºݤ��������
	 *
	 */
	private void paintlayout(){
		if(rootMem == null) return;
		
		LinkedList drawlist = new LinkedList();
		
		Iterator ite = rootMem.memIterator();
		LinkedList mems = new LinkedList();
		AbstractMembrane m;
		
		GraphicAtoms ga;
		
		searchatom(rootMem , "draw");
		/*���٤Ƥ�����Ф��ƹԤ�*/
		while(ite.hasNext()){
			mems.add(ite.next());
		}

		while(mems.size() > 0){
			m = (AbstractMembrane)mems.removeFirst();
			if(searchatom(m , "draw")){
				ga = getgraphicatoms(m);
				drawlist.add(ga);
				ga.drawatom(OSG);
			}
		}
		
//		while(drawlist.size() > 0){
//			
//		}
	}
	
	public void setRootMem(runtime.Membrane rootMem) {
		this.rootMem = rootMem;
		
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
	public void stop() {
		th = null;
	}
	public void run() {
		Thread me = Thread.currentThread();
		while (me == th) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			repaint();
		}
	}

}