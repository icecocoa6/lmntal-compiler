package test.GUI;

import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;
import java.util.Arrays;
import java.util.LinkedList;
import java.awt.*;
import java.awt.geom.Rectangle2D;

import runtime.AbstractMembrane;

public class GraphLayout implements Runnable {
	
	private Vector nodes = new Vector();
	private Thread th = null;
	private static final int DELAY = 50;
	private Component parent = null;
	private Rectangle area;// = new Rectangle(25,25,400,400);
	runtime.Membrane rootMem;
	
	public GraphLayout(Component parent) {
		this.parent = parent;
		area = parent.getBounds();
//		System.out.println("area = "+area);
	}
	
	public void setRootMem(runtime.Membrane rootMem) {
		this.rootMem = rootMem;
//		System.out.println("setRootMem"+rootMem);
	}
	
	public Rectangle getPreferredArea() {
		return this.area;
	}
	
//	public void addNode(GraphNode node) {
//		nodes.add(node);
//	}
	
	public void removeAllNodes() {
		nodes.removeAllElements();
	}
	
	class D_N_tuple {
		double d = Double.MAX_VALUE;
		Node n   = null;
	}
	
	/**
	 * PROXY ����������ȥ�Τ��� p �˺Ǥ�ᤤ Node ���֤���
	 * @param p
	 * @return Node
	 */
	public Node getNearestNode(Point p) {
		D_N_tuple t = new D_N_tuple();
		getNearestNode(p, rootMem, t);
		return t.n;
	}
	
	/**
	 * �� m ��ˤ��� Node �� t.n �Τ��� p �˺Ǥ�ᤤ Node �� t.n ���������롣t.d �ˤϤ��ε�Υ���������롣
	 * @param p
	 * @param m
	 * @param t
	 */
	public void getNearestNode(Point p, runtime.AbstractMembrane m, D_N_tuple t) {
		for(Iterator i=m.atomIterator();i.hasNext();) {
			Node n = (Node)i.next();
			if(!n.isVisible()) continue;
			double d = p.distance(n.getPosition().toPoint());
			if(t.d>d) {
				t.d = d;
				t.n = n;
			}
		}
		Object[] mems = m.getMemArray();
		for(int i=0;i<mems.length;i++) {
			getNearestNode(p, (AbstractMembrane)mems[i], t);
		}
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
	
	public void calc() {
		setAllowRelax(true);
		for(int i=0;i<100;i++) {
			relax();
		}
		setAllowRelax(false);
	}
	
	public void run() {
		Thread me = Thread.currentThread();
		while (th == me) {
			relax();
			
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	public List removedAtomPos = Collections.synchronizedList(new LinkedList());
	public Rectangle getAtomsBound() {
		final int m=1;
		Rectangle r=null;
		if(!removedAtomPos.isEmpty()) {
			return new Rectangle(((DoublePoint)removedAtomPos.remove(0)).toPoint());
		}
		r = new Rectangle((parent.getWidth() - parent.getWidth()/m)/2, (parent.getHeight() - parent.getHeight()/m)/2, parent.getWidth()/m, parent.getHeight()/m);
//		if(rootMem.getAtomCount()==0) {
//		} else {
//		}
		for (Iterator i=rootMem.atomIterator();i.hasNext();) {
			Node n = (Node)i.next();
			Point p = n.getPosition().toPoint();
//			System.out.println(r+" "+p);
//			if(r==null) r = new Rectangle(p);
			if(!r.contains(p)) {
				r.add(p);
			}
		}
//		int w = r.width, h = r.height;
//		r.x += w/4;
//		r.y += h/4;
//		r.width -= w/2;
//		r.height -= h/2;
		return r;
	}
	
	private volatile boolean allowRelax;
	public synchronized void setAllowRelax(boolean v) {
		allowRelax = v;
	}
	public synchronized boolean getAllowRelax() {
		return allowRelax;
	}
	
	/**
	 * ���٤ƤΥ��ȥ�ˤĤ����Ϥ���Ѥ����롣
	 *
	 */
	protected synchronized void relax() {
		if(!getAllowRelax()) return;
		relax(rootMem);
		Object[] mems = rootMem.getMemArray();
		for(int i=0;i<mems.length;i++) {
			relax((AbstractMembrane)mems[i]);
		}
	}
	
	/**
	 * �� m �ˤ��륢�ȥ�ˤĤ����Ϥ���Ѥ����롣
	 * @param m
	 */
	protected void relax(runtime.AbstractMembrane m) {
		if(m==null) return;
		
		for (Iterator i=m.atomIterator();i.hasNext();) {
			Node me = (Node)i.next();
			double dx = 0;
			double dy = 0;
			
//			System.out.println(i+" "+me.getName());
			
			// angle �ǥ�����
			Edge ie[] = new Edge[me.getEdgeCount()];
			for(int j=0;j<ie.length;j++) {
				ie[j]=new Edge(me, me.getNthNode(j));
//				System.out.println(ie[j]);
			}
			Arrays.sort(ie);
			
			for (int j=0;j<ie.length;j++) {
				Edge edge = ie[j];
				
				// �ǥե���Ȥ�Ĺ���˿��̤���
				if(me.hashCode() < edge.to.hashCode()){
					double l = edge.getLen();
					double f = (l - edge.getStdLen());// / (edge.getStdLen() * 1);
					double ddx = 0.5 * f * edge.getVx()/l;
					double ddy = 0.5 * f * edge.getVy()/l;
					
					edge.from.setMoveDelta(ddx, ddy);
					edge.to.setMoveDelta(-ddx, -ddy);
				}
				
				if(me.getEdgeCount()<=1) continue;
				
				// cur.to �ˤ������Ϥ�׻�����
				{
					Edge cur = ie[j];
	//				System.out.println(cur);
					Node you = cur.to;
					
					Edge prev = ie[(j-1+ie.length)%ie.length];
					Edge next = ie[(j+1)%ie.length];
	//				System.out.println("  p : "+ prev);
	//				System.out.println("  n : "+ next);
					
					// �����դȤγ���
					double a_p = regulate(cur.getAngle() - prev.getAngle());
					// �����դȤγ���
					double a_n = regulate(next.getAngle() - cur.getAngle());
					double a_r = a_n-a_p;
	//				System.out.println("  a_p : "+ a_p*180/Math.PI);
	//				System.out.println("  a_n : "+ a_n*180/Math.PI);
	//				System.out.println("   a_r : "+ a_r*180/Math.PI);
					
					double l = cur.getLen();
					
					// ���׼�������ˤ�����ư�����оݤȼ�ʬ������ʬ�˿�ľ��Ĺ�����Υ٥��ȥ�
					// ���줬 you ��Ư���Ϥ�ñ�̥٥��ȥ�ˤʤ�
					double tx = -cur.getVy() / l;
					double ty =  cur.getVx() / l;
	//				System.out.println("   tx : "+ tx);
	//				System.out.println("   ty : "+ ty);
					
					// move = t times diff
					dx = 1.5 * tx * a_r;
					dy = 1.5 * ty * a_r;
					
					me.setMoveDelta(-dx,-dy);
					you.setMoveDelta(dx,dy);
				}
			}
		}
		// �ºݤ˰�ư����
		for (Iterator i=m.atomIterator();i.hasNext();) {
			Node me = (Node)i.next();
			if(me==((LMNGraphPanel)parent).movingNode) continue;
			me.move(parent.getBounds());
		}
	}
	
	/**
	 * [0, 2PI) �����������Ƥ�������mod 2PI �ߤ����ʤ��󤸡�
	 * @param a
	 * @return
	 */
	static double regulate(double a) {
		while(a<0.0) a+= Math.PI*2;
		while(a>=2*Math.PI) a-= Math.PI*2;
		return a;
	}
	
	/**
	 * ���٤ƤΥ��ȥ��������褹�롣
	 * @param g
	 */
	public void paint(Graphics g) {
		if(!getAllowRelax()) return;
		if(rootMem==null) return;
		g.setColor(Color.BLACK);
		
		paintMem(g, rootMem);
	}
	
	/**
	 * �� m ��°���뤹�٤ƤΥ��ȥ��������褹�롣
	 * @param g
	 * @param m
	 */
	public void paintMem(Graphics g, AbstractMembrane m) {
		// Ʊ������������ java.util.ConcurrentModificationException ���Ǥ�Τ� Iterator ��᤿
		// �����Ǥ����Ӥ� readonly
		final double MARGIN = 15.0;
		
		Node[] nodes = (Node[])m.getAtomArray();
		m.rect.setRect(m.rect.x, m.rect.y, 0.0, 0.0);
		for(int i=0;i<nodes.length;i++) {
			if(!nodes[i].isVisible()) continue;
			double mg = MARGIN+runtime.Env.atomSize;
			if(m.rect.isEmpty()) m.rect.setRect(nodes[i].getPosition().x-mg, nodes[i].getPosition().y-mg, mg*2, mg*2);
			else                 m.rect.add(new Rectangle2D.Double(nodes[i].getPosition().x-mg, nodes[i].getPosition().y-mg, mg*2, mg*2));
			nodes[i].paintEdge(g);
		}
		for(int i=0;i<nodes.length;i++) {
			if(!nodes[i].isVisible()) continue;
			nodes[i].paintNode(g);
		}
		// ����
		Object[] mems = m.getMemArray();
		for(int i=0;i<mems.length;i++) {
			AbstractMembrane mm = (AbstractMembrane)mems[i];
			paintMem(g, mm);
			if(m.rect.isEmpty()) m.rect.setRect(mm.rect.x-MARGIN, mm.rect.y-MARGIN, mm.rect.width+MARGIN*2, mm.rect.height+MARGIN*2);
			else                 m.rect.add(new Rectangle2D.Double(mm.rect.x-MARGIN, mm.rect.y-MARGIN, mm.rect.width+MARGIN*2, mm.rect.height+MARGIN*2));
//			System.out.println(m.rect);
		}
		if(!m.equals(rootMem)) {
			final int ROUND=40;
			// ������ΤФ����Ϥ����� empty �ˤʤäƤ���
			if(m.rect.isEmpty()) m.rect.width=m.rect.height=MARGIN*2;
			g.drawRoundRect((int)m.rect.x, (int)m.rect.y, (int)m.rect.width, (int)m.rect.height, ROUND, ROUND);
		}
	}
}
