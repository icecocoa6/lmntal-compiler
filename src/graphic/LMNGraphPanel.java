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
	public Thread th = null;
	/**��å�����Ƥ��������줬���뤫�ɤ���*/
	public boolean locked = false;
	private Image OSI = null;
	private Graphics OSG = null;
	runtime.Membrane rootMem;
	/**���褹�륪�֥������ȥꥹ��*/
	LinkedList drawlist = new LinkedList();
	
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
		start();
	}
	public void paint(Graphics g) {

		//���̤����Ϥǽ�������ɤ�Ĥ֤���
		OSG.setColor(Color.WHITE);
		OSG.fillRect(0,0,(int) getSize().getWidth(), (int) getSize().getHeight());
		getlayout();
		paintlayout();
		g.drawImage(OSI,0,0,this);
	}
	/**
	 * ���ꤵ�줿̾�ΤΥ��ȥब¸�ߤ��뤫������
	 * ����п��򡢤ʤ���е����֤���
	 */
	private boolean searchatom(AbstractMembrane m , String sstring){
		Iterator ite = m.atomIterator();
		Node a;
//		LinkedList atoms = new LinkedList();
//		
//		while(ite.hasNext()){
//			atoms.add(ite.next());
//		}
//		
//		while(atoms.size() > 0){
//			a = (Node)atoms.removeFirst();
//			if(a.getName() == sstring){
//				return true;
//			}
//		}
		while(ite.hasNext()){
			a = (Node)ite.next();
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
		
//		while(ite.hasNext()){
//			atoms.add(ite.next());
//		}
//		
//		while(atoms.size() > 0){
//			a = (Node)atoms.removeFirst();
		if(m.getLockThread() != null) locked = true;
		while(ite.hasNext()){
			a = (Node)ite.next();
			/**���褹��ե�����μ���*/
			if(a.getName()=="getpic"){
				if(a.getEdgeCount() != 1)continue;
				ga.SetPic( a.getNthNode(0).getName() );
//				System.out.print(ga.filename + "\n");
			}
			/**����ν��֡�����ط��ˤμ���*/
			else if(a.getName()=="name"){
				ga.name = a.getNthNode(0).getName();
			}else if(a.getName()=="sequence"){
				try{
					ga.sequence = Integer.parseInt(a.getNthNode(0).getName());
				}catch(NumberFormatException error){
					try{
						ga.sequence = Integer.parseInt(a.getNthNode(0).getNthNode(0).getName());
					}catch(NumberFormatException e){
						
					}						
				}
				
			}
			/**���褹����֤μ���*/
			else if(a.getName()=="position"){
				if(a.getEdgeCount() != 2)continue;
				try{
					ga.posx = Integer.parseInt(a.getNthNode(0).getName());
				}catch(NumberFormatException error){
//					try{
//						ga.posx = Integer.parseInt(a.getNthNode(0).getNthNode(0).getName());
//					}catch(NumberFormatException e){
//						
//					}	
					return null;
				}

				try{
					ga.posy = Integer.parseInt(a.getNthNode(1).getName());
				}catch(NumberFormatException error){
//					try{
//						ga.posy = Integer.parseInt(a.getNthNode(1).getNthNode(0).getName());
//					}catch(NumberFormatException e){
//						
//					}	
					return null;
					
				}
				
			}
			/**���褹�뤫�ɤ����μ���*/
			else if(a.getName()=="enable"){
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
	private void getlayout(){
		if(rootMem == null) return;
		
		
		Iterator ite = rootMem.memIterator();
//		LinkedList mems = new LinkedList();
		AbstractMembrane m;
		
		GraphicAtoms ga;
		
		searchatom(rootMem , "draw");
		
		locked = false;
		/*���٤Ƥ�����Ф��ƹԤ�*/
		while(ite.hasNext()){
			m = (AbstractMembrane)ite.next();
			if(searchatom(m , "draw")){
				ga = getgraphicatoms(m);
				if(ga == null) continue;
				/**Ʊ��atom���ʤ���С��ꥹ�Ȥ��ɲá�ɽ��ͥ���̹�θ��*/
				for(int i = 0; i < drawlist.size(); i++){
					GraphicAtoms ga2 = (GraphicAtoms)drawlist.get(i);

					if(ga.name.equals(ga2.name)){
						drawlist.set(i, ga);
						break;
					}
					if(ga.sequence < ga2.sequence){
						drawlist.add(i, ga);
						break;
					}
				}
				if(drawlist.size() == 0)
					drawlist.add(ga);
			}
		}
	}

	private void paintlayout(){
		Iterator ite = drawlist.iterator();
		while(ite.hasNext()){
			GraphicAtoms ga = (GraphicAtoms)ite.next();
			ga.drawatom(OSG);
		}
		
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