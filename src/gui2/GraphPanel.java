package gui2;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import runtime.Membrane;

public class GraphPanel extends JPanel implements Runnable {

	private Thread th = null;
	private Image OSI = null;
	private Graphics OSG = null;
	private GraphMembrane rootGraphMembrane;
	private Membrane rootMembrane;
	private GraphAtom moveAtom;
	private GraphPanel panel;
	
	static
	private double magnification = 0.5;
	
	static
	private Image PIN;
	
	public GraphPanel() {
		super();
		panel = this;
		PIN = Toolkit.getDefaultToolkit().getImage(getClass().getResource("gabyou.gif"));
		// PIN�Υ����Ԥ�����������
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(PIN, 0);
		try {
			mt.waitForAll();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//��PIN�Υ����Ԥ��������ޤ�
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				OSI = createImage((int) getSize().getWidth(), (int) getSize().getHeight());
				OSG = OSI.getGraphics();	
			}

		});
		addMouseListener(new MouseAdapter() {
			
			/** 
			 * �ޥ����������줿�Ȥ��ν���
			 * <p>ctrl�������줨�Ƥ���С����ɽ������
			 * <p>ctrl��������Ƥ��ʤ���Х��ȥ��Ĥ���
			 * <p>ct��l��������Ƥ��ʤ������ĥ��֥륯��å��ʤ�Х��ȥ�θ���
			 */
			public void mousePressed(MouseEvent e) {
				//determine nearest node
				moveAtom = rootGraphMembrane.getNearestAtom(e.getPoint());
				if(e.getButton() == MouseEvent.BUTTON1){
					if(e.isControlDown()){
						moveAtom.flipViewMem(OSI.getGraphics(), panel);
						return;
					}
					if(e.getClickCount() == 2){
						if(!e.isControlDown()){
							moveAtom.flipClip(OSI.getGraphics(), panel);
						}
					}
				}
				moveAtom.setHold(true);
			}
			
			/**
			 * �ޥ�����Υ���줿�Ȥ��ν���
			 * <p>�ǽ�˲����줿�Ȥ�����ư���Ƥ������ư������Υ�����</p>
			 */
			public void mouseReleased(MouseEvent e) {
				if(moveAtom==null){ return; }
				moveAtom.setHold(false);
				moveAtom = null;
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			
			/**
			 * �ޥ������ɥ�å����줿�Ȥ��ν���
			 * <p>��ư������Υ�����</p>
			 */
			public void mouseDragged(MouseEvent arg0) {
				if(moveAtom==null){ return; }
				moveAtom.setPosition(arg0.getPoint().x, arg0.getPoint().y);
			}

		}
		);
		
		start();
	}
	
	public void setRootMem(Membrane mem){
		rootMembrane = mem;
		rootGraphMembrane = new GraphMembrane(mem, this);
	}
	
	public Image getPin(){
		return PIN;
	}
	/**
	 * ���٤Ƥ����ɽ��������
	 *
	 */
	public void showAll(){
		if(rootGraphMembrane == null){
			return;
		}
		rootGraphMembrane.showAll();
	}
	
	/**
	 * ���٤Ƥ������ɽ��������
	 *
	 */
	public void hideAll(){
		if(rootGraphMembrane == null){
			return;
		}
		rootGraphMembrane.setViewInside(false);
		
	}
	
	public void calc(){
		if(null == rootGraphMembrane){ return; }
		rootGraphMembrane.resetMembrane(rootMembrane);
	}

	static
	public void setMagnification(double magni){
		magnification = magni;
	}
	
	static
	public double getMagnification(){
		return magnification;
	}
	
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
				calc();
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			repaint();
		}
	}
	
	public void paint(Graphics g) {
		if (OSI == null) {
			OSI = createImage((int) getSize().getWidth(), (int) getSize().getHeight());
			OSG = OSI.getGraphics();			
		}
		if (OSI != null){
			OSG.setColor(Color.WHITE);
			OSG.fillRect(0,0,(int) getSize().getWidth(), (int) getSize().getHeight());
			g.drawImage(OSI,0,0,this);
			
			if(null != rootGraphMembrane){
				rootGraphMembrane.paint(g);
			}
		}
	}
}