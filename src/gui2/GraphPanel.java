package gui2;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import runtime.Membrane;

public class GraphPanel extends JPanel {

	///////////////////////////////////////////////////////////////////////////
	// static
	
	static
	private double magnification_ = 0.5;
	
	static
	private Image pin_;
	
	///////////////////////////////////////////////////////////////////////////
	
	private Thread calcTh_ = null;
	private Thread repaintTh_ = null;
	private Node moveTargetNode_ = null;
	private Node rootNode_;
	private Membrane rootMembrane_;
	private AffineTransform af_ = new AffineTransform();
	private Point lastPoint;
	private List<Membrane> rootMemList_ = new ArrayList<Membrane>();
	
	///////////////////////////////////////////////////////////////////////////
	
	public GraphPanel() {
		super();
		Node.setPanel(this);
		pin_ = Toolkit.getDefaultToolkit().getImage(getClass().getResource("gabyou.gif"));
		// PIN�Υ����Ԥ�����������
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(pin_, 0);
		try {
			mt.waitForAll();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		//��PIN�Υ����Ԥ��������ޤ�
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				System.out.println("key");
				if(e.getKeyCode() == KeyEvent.VK_A){
					rootMembrane_ = rootMemList_.remove(rootMemList_.size() - 1);
					setRootMem(rootMembrane_);
					System.out.println(rootMembrane_);
				}
				else if(e.getKeyCode() == KeyEvent.VK_S){
//					rootMemList_.add(((O)rootMembrane));
				}
				
			}

			public void keyReleased(KeyEvent e) {
				System.out.println("keyR");
				
			}

			public void keyTyped(KeyEvent e) {
				System.out.println("keyT");
				
			}
			
		});
		addMouseListener(new MouseAdapter() {
			
			/** 
			 * �ޥ����������줿�Ȥ��ν���
			 */
			public void mousePressed(MouseEvent e) {
				if(null == rootNode_){ return; }
				int pointX = (int)((e.getX() - (getWidth() / 2)) / getMagnification());
				int pointY = (int)((e.getY() - (getHeight() / 2)) / getMagnification());
				
				// �Ļ��ԲĻ��ȿž
				if(e.isControlDown()){
					moveTargetNode_ = rootNode_.getPointNode(pointX, pointY, true);
					if(null == moveTargetNode_){ return; }
					moveTargetNode_.swapVisible();
					moveTargetNode_.setUncalc(false);
					moveTargetNode_ = null;
					lastPoint = null;
					return;
				}
				
				if(e.getClickCount() == 2){
					moveTargetNode_ = rootNode_.getPointNode(pointX, pointY, true);
					if(null != moveTargetNode_){
						moveTargetNode_.swapClipped();
					}
					moveTargetNode_ = null;
					return;
				}
				
				setCursor(new Cursor(Cursor.MOVE_CURSOR));
				moveTargetNode_ = rootNode_.getPointNode(pointX, pointY, true);
				if(null != moveTargetNode_){
					moveTargetNode_.setUncalc(true);
				}
				lastPoint = e.getPoint();
			}
			
			/**
			 * �ޥ�����Υ���줿�Ȥ��ν���
			 * <p>�ǽ�˲����줿�Ȥ�����ư���Ƥ������ư������Υ�����</p>
			 */
			public void mouseReleased(MouseEvent e) {
				if(null != moveTargetNode_){
					moveTargetNode_.setUncalc(false);
					moveTargetNode_ = null;
				}
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			
			/**
			 * �ޥ������ɥ�å����줿�Ȥ��ν���
			 * <p>��ư������Υ�����</p>
			 */
			public void mouseDragged(MouseEvent e) {
				if(moveTargetNode_ != null){
					int pointX = (int)((e.getX() - (getWidth() / 2)) / getMagnification());
					int pointY = (int)((e.getY() - (getHeight() / 2)) / getMagnification());
					moveTargetNode_.setPos(pointX, pointY);
				} else {
					if(null == lastPoint){ return; }
					rootNode_.setPosDelta(e.getPoint().getX() - lastPoint.getX(),
							e.getPoint().getY() - lastPoint.getY());
					lastPoint = e.getPoint();
				}
			}

		}
		);
		
		addMouseWheelListener(new SliderMouseListener(null));

		calcTh_ = new CalcThread();
		calcTh_.start();
		repaintTh_ = new RepaintThread();
		repaintTh_.start();
	}
	
	/**
	 * �ϳإ�ǥ�η׻�����ư�η׻���Ԥ�
	 *
	 */
	public void calc(){
		if(null == rootNode_){ return; }
		rootNode_.reset(rootMembrane_);
		rootNode_.calcAll();
		rootNode_.moveAll();
	}

	/**
	 * ����̾�����Ψ���������
	 * @return
	 */
	static
	public double getMagnification(){
		return magnification_;
	}

	/**
	 * Pin�Υ��᡼�����������
	 * @return
	 */
	public Image getPin(){
		return pin_;
	}
	
	/**
	 * ���٤Ƥ������ɽ��������
	 *
	 */
	public void hideAll(){
		rootNode_.setVisible(false, true);
		rootNode_.setInvisibleRootNode(null);
	}

	/**
	 * ����դ����褹��
	 */
	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0,(int)getWidth(), (int)getHeight());
	
		g.setColor(Color.WHITE);
		af_.setTransform(getMagnification(), 0, 0, getMagnification(), getWidth() / 2, getHeight() / 2);
		((Graphics2D)g).setTransform(af_);
	
		g.setColor(Color.BLACK);
		if(null != rootNode_){
			rootNode_.paint(g);
		}
	
		// ������֤��᤹
		int divergenceTimer = NodeFunction.getDivergence();
		if(0 < divergenceTimer){
			((Graphics2D)g).setTransform(new AffineTransform());
			g.setColor(Color.RED);
			g.drawString("Divergence Timer:" + divergenceTimer, 10, 30);
		}
	}

	public void setMagnification(double magni){
		magnification_ = magni * 2;
	}

	/**
	 * �롼����򥻥åȤ���
	 * @param mem
	 */
	public void setRootMem(Membrane mem){
		rootMembrane_ = mem;
		rootNode_ = new Node(null, mem);
	}

	/**
	 * ���٤Ƥ����ɽ��������
	 *
	 */
	public void showAll(){
		rootNode_.setVisible(true, true);
		rootNode_.setInvisibleRootNode(null);
	}
	
	public Node getRootNode(){
		return (Node)rootNode_.clone();
	}
	
	public void setRootNode(Node node){
		rootNode_ = node;
	}

	///////////////////////////////////////////////////////////////////////////
	/**
	 * �黻�ѥ���å�
	 * @author nakano
	 *
	 */
	class CalcThread extends Thread {
		private boolean runnable_ = true;
		
		public CalcThread() {}
		
		public void run() {
			while(runnable_) {
				try {
					sleep(40);
					calc();
				} catch (Exception e) {
				}
			}
		}
		
		public void setRunnable(boolean runnable){
			runnable_ = runnable;
		}
	}

	///////////////////////////////////////////////////////////////////////////
	/**
	 * �����ѥ���å�
	 * @author nakano
	 *
	 */
	class RepaintThread extends Thread {
		private boolean runnable_ = true;
		
		public RepaintThread() {}
		
		public void run() {
			while(runnable_) {
				try {
					sleep(50);
					repaint();
				} catch (Exception e) {
				}
			}
		}
		
		public void setRunnable(boolean runnable){
			runnable_ = runnable;
		}
	}
}