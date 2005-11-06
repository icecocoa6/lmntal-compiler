package runtime;

//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import test.GUI.DoublePoint;
import test.GUI.Node;
import test3d.Double3DPoint;
import test3d.LMNTransformGroup;
import test3d.Node3D;
import util.QueuedEntity;
import daemon.IDConverter;
//import util.Stack;

/**
 * ���ȥ९�饹�������롦��⡼�Ȥ˴ؤ�餺���Υ��饹�Υ��󥹥��󥹤���Ѥ��롣
 * @author Mizuno
 */
public final class Atom extends QueuedEntity implements test.GUI.Node, test3d.Node3D, Serializable {
	/** ��°�졣AbstractMembrane�Ȥ��Υ��֥��饹���ѹ����Ƥ褤��
	 * �������ͤ��ѹ�����Ȥ���index��Ʊ���˹������뤳�ȡ�(null,-1)�Ͻ�°��ʤ���ɽ����*/
	AbstractMembrane mem;
	/** ��°���AtomSet��ǤΥ���ǥå��� */
	int index = -1;
	public int getid(){
		return id;
	}
	/** �ե��󥯥���̾���ȥ�󥯿��� */
	private Functor functor;
	/** ��� */
	Link[] args;
	
	private static int lastId = 0;
	/** ���Υ��ȥ�Υ�����ID */
	int id;
	
	/** ��⡼�ȥۥ��ȤȤ��̿��ǻ��Ѥ���뤳�Υ��ȥ��ID����⡼����˽�°����Ȥ��Τ߻��Ѥ���롣
	 * <p>��°��Υ���å�������塢��°���Ϣ³�����å�������Τ�ͭ����
	 * ����å���������˽�������졢����³����⡼�ȥۥ��Ȥؤ��׵���ۤ��뤿��˻��Ѥ���롣
	 * ��⡼�ȥۥ��Ȥؤ��׵�ǿ��������ȥब���������ȡ��������NEW_����������롣
	 * $inside_proxy���ȥ�ξ�硢̿��֥�å�������������ƥ�⡼��¦�Υ�����ID�Ǿ�񤭤���롣
	 * $inside_proxy�ʳ��Υ��ȥ�ξ�硢��å�����ޤ�NEW_�Τޤ����֤���롣
	 * @see Membrane.atomTable */
	protected String remoteid;

	///////////////////////////////
	// ���󥹥ȥ饯��

	/**
	 * ���ꤵ�줿̾���ȥ�󥯿�����ĥ��ȥ��������롣
	 * AbstractMembrane��newAtom�᥽�å���ǸƤФ�롣
	 * @param mem ��°��
	 */
	public Atom(AbstractMembrane mem, Functor functor) {
		this.mem = mem;
		this.functor = functor;
		args = new Link[functor.getArity()];
		id = lastId++;

		if(Env.fGUI) {
			Rectangle r = Env.gui.lmnPanel.getGraphLayout().getAtomsBound();
			pos = new DoublePoint(Math.random()*r.width + r.x, Math.random()*r.height + r.y);
		}
		if(Env.f3D) {
			pos3d = new Double3DPoint(Math.random(), Math.random(), Math.random());
		}
	}

	///////////////////////////////
	// ���
	public void setFunctor(Functor newFunctor) {
		if (newFunctor.getArity() > args.length) {
			// todo SystemError�Ѥ��㳰���饹���ꤲ��
			throw new RuntimeException("SYSTEM ERROR: insufficient link vector length");
		}
		functor = newFunctor;
	}
	/** �ե��󥯥�̾�����ꤹ�롣 */
	public void setName(String name) {
		setFunctor(name, getFunctor().getArity());
	}
	/** �ե��󥯥������ꤹ�롣
	 * ��°�줬��⡼�Ȥξ��⤢�ꡢ������AtomSet��ɬ���������ʤ���Фʤ�ʤ��Τǡ�
	 * ���alterAtomFunctor�᥽�åɤ�Ƥ֡�*/
	public void setFunctor(String name, int arity) {
		mem.alterAtomFunctor(this, new Functor(name, arity));
	}
	/**@deprecated*/
	public void set(String name, int arity) {
		setFunctor(name,arity);
	}
	/** ���ȥ���°�줫��������ʥ����Υ��ȥ�Ͻ���ʤ���*/
	public void remove() {
		mem.removeAtom(this);
	}
	///////////////////////////////
	// ����μ���

	public String toString() {
		return functor.getName();
	}
	/**
	 * �ǥե���Ȥμ������Ƚ����Ϥ��������֤��Ѥ����Ѥ�äƤ��ޤ��Τǡ�
	 * ���󥹥��󥹤��Ȥ˥�ˡ�����id���Ѱդ��ƥϥå��女���ɤȤ������Ѥ��롣
	 */
	public int hashCode() {
		return id;
	}
	/** ���Υ��ȥ�Υ�����ID��������� */
	String getLocalID() {
		return Integer.toString(id);
	}

	/** ��°��μ��� */
	public AbstractMembrane getMem() {
		return mem;
	}
	/** �ե��󥯥������ */
	public Functor getFunctor(){
		return functor;
	}
	/** ̾������� */
	public String getName() {
		return functor.getName();
	}
	/** Ŭ�ڤ˾�ά���줿̾������� */
	public String getAbbrName() {
		return functor.getAbbrName();
	}
	/** ��󥯿������ */
	int getArity() {
		return functor.getArity();
	}
	/** �ǽ���������� */
	Link getLastArg() {
		return args[getArity() - 1];
	}
	/** ��pos�����˳�Ǽ���줿��󥯥��֥������Ȥ���� */
	public Link getArg(int pos) {
		return args[pos];
	}
	/** �� n �����ˤĤʤ��äƤ륢�ȥ��̾����������� */
	public String nth(int n) {
		return nthAtom(n).getFunctor().getName();
	}
	/** �� n �����ˤĤʤ��äƤ륢�ȥ��������� */
	public Atom nthAtom(int n) {
		return args[n].getAtom();
	}
//	/** ��°������ꤹ�롣AbstractMembrane�Ȥ��Υ��֥��饹�Τ߸ƤӽФ��Ƥ褤��*/
//	void setMem(AbstractMembrane mem) {
//		this.mem = mem;
//	}
//	/**@deprecated*/	
//	void remove() {
//		mem.removeAtom(this);
//		mem = null;
//	}
//	/** �����å������äƤ���н���� */
//	public void dequeue() {
//		super.dequeue();
//	}
	
	///////////////////////////////////////////////////////////////
	
	/* *** *** *** *** *** BEGIN GUI *** *** *** *** *** */

	DoublePoint pos;
	Double3DPoint pos3d;
	LMNTransformGroup objTrans;
	double vx, vy, vz;
	public void initNode() {
		pos = new DoublePoint();
	}
	public void initNode3d() {
		pos3d = new Double3DPoint();
	}
	public boolean isVisible() {
		return !(functor.equals(Functor.INSIDE_PROXY) || functor.equals(Functor.OUTSIDE_PROXY));
	}
	public DoublePoint getPosition() {
		return pos;
	}

	public void setObj(LMNTransformGroup obj){
		objTrans=obj;
	}
	public LMNTransformGroup getObj(){
		return objTrans;
	}
	public Double3DPoint getPosition3d() {
		return pos3d;
	}
	public void setPosition(DoublePoint p) {
		pos = p;
	}
	public void setPosition3d(Double3DPoint p) {
		pos3d = p;
	}
	public Node getNthNode(int index) {
		Atom a = nthAtom(index);
		while(a.getFunctor().equals(Functor.INSIDE_PROXY) || a.getFunctor().equals(Functor.OUTSIDE_PROXY)) {
//			System.out.println(a.nthAtom(0).nthAtom(0));
//			System.out.println(a.nthAtom(0).nthAtom(1));
			a = a.nthAtom(0).nthAtom(1);
		}
//		System.out.println(this+" 's "+index+"th atom is "+a);
		return a;
	}
	public Node3D getNthNode3d(int index) {
		Atom a = nthAtom(index);
		while(a.getFunctor().equals(Functor.INSIDE_PROXY) || a.getFunctor().equals(Functor.OUTSIDE_PROXY)) {
//			System.out.println(a.nthAtom(0).nthAtom(0));
//			System.out.println(a.nthAtom(0).nthAtom(1));
			a = a.nthAtom(0).nthAtom(1);
		}
//		System.out.println(this+" 's "+index+"th atom is "+a);
		return a;
	}
	public int getEdgeCount() {
		return functor.getArity();
	}
	public void setMoveDelta(double dx, double dy) {
		vx += dx;
		vy += dy;
	}
	public void setMoveDelta3d(double dx, double dy, double dz) {
		vx += dx;
		vy += dy;
		vz += dz;
	}
	public void initMoveDelta() {
		vx = vy = 0;
	}
	public void initMoveDelta3d() {
		vx = vy = vz = 0;
	}
	public DoublePoint getMoveDelta(){
		return new DoublePoint(vx,vy);
	}
	public void move(Rectangle area) {
		//if (n.isFixed()) return;
		final int M = 100;
		pos.x += Math.max(-M, Math.min(M, vx));
		pos.y += Math.max(-M, Math.min(M, vy));
		
		if (pos.x < area.getMinX())		pos.x = (int)area.getMinX();
		else if (pos.x > area.getMaxX())	pos.x = (int)area.getMaxX();
		
		if (pos.y < area.getMinY())		pos.y = (int)area.getMinY();
		else if (pos.y > area.getMaxY())	pos.y = (int)area.getMaxY();
		
//		vx=vy=0;
		vx /= 2;
		vy /= 2;
	}
	public void paintEdge(Graphics g) {
		g.setColor(Color.BLACK);
		for(int i=0;i<getEdgeCount();i++) {
			Node n2 = getNthNode(i);
			if(this.hashCode() < n2.hashCode()) continue;
			g.drawLine((int)getPosition().x, (int)getPosition().y, (int)n2.getPosition().x, (int)n2.getPosition().y);
		}
	}
	
	public void paintNode(Graphics g) {
		String label = getName();
		FontMetrics fm = g.getFontMetrics();
		if(Env.fDEMO) {
			g.setFont(new Font(null, Font.PLAIN, 30));
		}
		int w = fm.stringWidth(label);
		int h = fm.getHeight();
		
		int wh = Env.fDEMO ? 40 : 16;
		Dimension size = new Dimension(wh, wh);
//		g.setColor(new Color(64,128,255));
		// Ŭ���˿�ʬ�����롪
		
		// [0, 7] -> [128, 255] for eash R G B
		int ir = 0x7F - ((label.hashCode() & 0xF00) >> 8) * 0x08 + 0x7F;
		int ig = 0x7F - ((label.hashCode() & 0x0F0) >> 4) * 0x08 + 0x7F;
		int ib = 0x7F - ((label.hashCode() & 0x00F) >> 0) * 0x08 + 0x7F;
		
//		System.out.println(label + "  " + ir + "  " + ig + "  " + ib);
		g.setColor(new Color(ir, ig, ib));
		
		g.fillOval((int)(pos.x - size.width/2), (int)(pos.y - size.height/ 2), size.width, size.height);
		
		g.setColor(Color.BLACK);
		g.drawOval((int)(pos.x - size.width/2), (int)(pos.y - size.height/ 2), size.width, size.height);
		g.drawString(label, (int)(pos.x - (w-10)/2), (int)(pos.y - (h-4)/2) + fm.getAscent()+size.height);
	}
	
	/* *** *** *** *** *** END GUI *** *** *** *** *** */
	
	/**
	 * ���Υ��ȥ��ľ�󲽤��ƥ��ȥ꡼��˽񤭽Ф��ޤ���
	 * ����å��幹���䡢�ץ���ʸ̮�ΰ����κݤ����Ѥ��ޤ���
	 * @param out ���ϥ��ȥ꡼��
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeObject(functor);
		if (functor.equals(Functor.INSIDE_PROXY)) {
			//����ؤΥ�󥯤��������ʤ�
			out.writeObject(args[1]);
		} else if (functor.equals(Functor.OUTSIDE_PROXY)) {
			//����ؤΥ�󥯤ϡ���³��atomID/memID�Τ���������³���INSIDE_PROXY���裱�����ʤΤǡ����ȥ��ID�Τߤǽ�ʬ��
			Atom a = args[0].getAtom();
			AbstractMembrane mem = a.mem;
			//�롼����ʳ��Ǥϥ����Х�ID����������Ƥ��ʤ��Τǡ��Ȥꤢ������ʬ�Ǻ�äƤ��롣
			//todo ��äȤ褤��ˡ��ͤ���
			out.writeObject(mem.getTask().getMachine().hostname);
			out.writeObject(mem.getLocalID());
			out.writeInt(a.id);
			out.writeObject(args[1]);
		} else {
			out.writeObject(args);
		}
	}
	
	/**
	 * ���Υ��ȥ�����Ƥ򥹥ȥ꡼�फ���������ޤ���
	 * ����å��幹���䡢�ץ���ʸ̮�ΰ����κݤ����Ѥ��ޤ���
	 * @param out ���ϥ��ȥ꡼��
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		id = lastId++;
		remoteid = Integer.toString(in.readInt());
		functor = (Functor)in.readObject();
		args = new Link[functor.getArity()];
		if (functor.equals(Functor.INSIDE_PROXY)) {
			//�Ȥꤢ�����������Ƥ��������RemoteMembrane��ǤĤʤ�ľ���졢���Υ��ȥ�ϻȤ��ʤ��ʤ롣
			args[1] = (Link)in.readObject();
		} else if (functor.equals(Functor.OUTSIDE_PROXY)) {
			//�������INSIDE_PROXY����������Ƥ��ʤ��Τǡ��������������롣
			String hostname = (String)in.readObject();
			String localid = (String)in.readObject();
			String globalid = hostname + ":" + localid;
			AbstractMembrane mem = IDConverter.lookupGlobalMembrane(globalid);
			//IDConverter�ˤϡ�RemoteMembrane.updateCache()����Ͽ�ѤߤΤϤ���
			Atom inside = new Atom(mem, Functor.INSIDE_PROXY);
			mem.atoms.add(inside);
			
			inside.remoteid = Integer.toString(in.readInt());
			args[0] = new Link(inside, 0);
			inside.args[0] = new Link(this, 0);
			//inside����2��������³��ϡ��Ͷ��Υ��ȥ�ǽ�ü�����ۤ����褤���⡣
			args[1] = (Link)in.readObject();
		} else {
			args = (Link[])in.readObject();
		}
	}
}
