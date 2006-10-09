package gui2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import runtime.Atom;
import runtime.Membrane;

public class GraphMembrane {
	
	
	/////////////////////////////////////////////////////////////////
	//	���
	
	final static
	private double EDGE_LENGTH = 200.0;
	
	final static
	private double EDGE_LENGTH_TO_MEM = 450.0;
	
	final static
	private double EDGE_LENGTH_TO_DUMMY = 250.0;
	
	final static
	private int ROUND = 40;
	
	final static
	private Color MEM_COLOR = new Color(102, 153, 255);
	
	final static
	private Color MEM_BORDER_COLOR = new Color(100, 100, 100);
	
	final static
	private Color MEM_PIN_COLOR = new Color(0, 0, 0);
	
	final static
	private BasicStroke MEM_STROKE = new BasicStroke(2.0f);
	
	final static
	private double MEM_REPULSIVE_FORCE = 0.2;
	
	/////////////////////////////////////////////////////////////////
	
	private boolean root = false; 
	private Membrane myMem;
	private GraphMembrane myParent;
	private int posX1;
	private int posY1;
	private int posX2;
	private int posY2;
	private int pinPosY;
	private double dx;
	private double dy;
	private boolean viewInside = false;
	private GraphAtom dummyGraphAtom = new GraphAtom(null, this);
	static private GraphPanel panel;
	
	
	private Map<Atom, GraphAtom> atomMapTemp =
		Collections.synchronizedMap(new HashMap<Atom, GraphAtom>());
	
	/** ����Υ��ȥ���ݻ������Atom, GraphAtom�� */
	private Map<Atom, GraphAtom> atomMap =
		Collections.synchronizedMap(new HashMap<Atom, GraphAtom>());
	
	private Map<Membrane, GraphMembrane> memMapTemp =
		Collections.synchronizedMap(new HashMap<Membrane, GraphMembrane>());
	
	/** ����λ�����ݻ������Membrane, GraphMembrane�� */
	private Map<Membrane, GraphMembrane> memMap =
		Collections.synchronizedMap(new HashMap<Membrane, GraphMembrane>());
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	public GraphMembrane(GraphMembrane parent, Membrane mem) {
		myParent = parent;
		resetMembrane(mem);
	}
	
	public GraphMembrane(Membrane mem, GraphPanel graphPanel) {
		root = true;
		viewInside = true;
		panel = graphPanel;
		myParent = null;
		resetMembrane(mem);
	}
	
	/////////////////////////////////////////////////////////////////
	
	/**
	 * �б�����Membrane���֥������Ȥ��֤���
	 */
	public Membrane getMembrane(){
		return myMem;
	}
	
	/**
	 * ��ư��Υ�����ꤹ��
	 * @param x
	 * @param y
	 */
	public void moveDelta(double x, double y){
		dx += x;
		dy += y;
	}
	
	/**
	 * �����Ȥ�����ꤹ��
	 * @param mem �������оݤ���
	 */
	public void resetMembrane(Membrane mem){
		synchronized (atomMap) {
			myMem = mem;
			/////////////////////////////////////////////////////////////
			// ���ȥ����������
			atomMapTemp.clear();
			Iterator atoms = mem.atomIterator();
			posX1 = posY1 = 1000;
			posX2 = posY2 = 0;
			
			if(!getViewInside()){
				atomMap.clear();
				atomMap.put(null, dummyGraphAtom);
				if(posX1 > dummyGraphAtom.getPosX()){ posX1 = dummyGraphAtom.getPosX(); }
				if(posY1 > dummyGraphAtom.getPosY()){ posY1 = dummyGraphAtom.getPosY(); }
				if(posX2 < dummyGraphAtom.getPosX()){ posX2 = dummyGraphAtom.getPosX(); }
				if(posY2 < dummyGraphAtom.getPosY()){ posY2 = dummyGraphAtom.getPosY(); }
			}
			while(getViewInside() && atoms.hasNext()){
				Atom atom = (Atom)atoms.next();
				
				// Proxy���ȥ��̵��
//				if(atom.getFunctor().isInsideProxy() || atom.getFunctor().isOutsideProxy()){
//				continue;
//				}
				
				GraphAtom targetAtom = null;
				if(atomMap.containsKey(atom)){
					targetAtom = atomMap.get(atom);
					atomMapTemp.put(atom, targetAtom);
				}
				else{
					targetAtom = new GraphAtom(atom, this);
					atomMapTemp.put(atom, targetAtom);
				}
				if(targetAtom != null){
					// ProxyAtom����ϥ�󥯤����褷�ʤ�
					if(targetAtom.me.getFunctor().isInsideProxy() ||
							targetAtom.me.getFunctor().isOutsideProxy())
					{
						continue;
					}
					if(posX1 > targetAtom.getPosX()){ posX1 = targetAtom.getPosX(); }
					if(posY1 > targetAtom.getPosY()){ posY1 = targetAtom.getPosY(); }
					if(posX2 < targetAtom.getPosX()){ posX2 = targetAtom.getPosX(); }
					if(posY2 < targetAtom.getPosY()){ posY2 = targetAtom.getPosY(); }
				}
			}
			atomMap.clear();
			atomMap.putAll(atomMapTemp);
			
			/////////////////////////////////////////////////////////////
			// �����������
			int margin1 = GraphAtom.getAtomSize() * 2;
			memMapTemp.clear();
			Iterator<Membrane> mems = mem.memIterator();
			while(mems.hasNext()){
				Membrane targetMem = mems.next();
				GraphMembrane targetGraphMem = null;
				if(memMap.containsKey(targetMem)){
					targetGraphMem = memMap.get(targetMem);
					memMapTemp.put(targetMem, targetGraphMem);
					targetGraphMem.resetMembrane(targetMem);
				}
				else{
					targetGraphMem = new GraphMembrane(this, targetMem);
					memMapTemp.put(targetMem, targetGraphMem);
				}
				if(getViewInside()){
					if(posX1 > targetGraphMem.getPosX1() - margin1){ posX1 = targetGraphMem.getPosX1() - margin1; }
					if(posY1 > targetGraphMem.getPosY1() - margin1){ posY1 = targetGraphMem.getPosY1() - margin1; }
					if(posX2 < targetGraphMem.getPosX2() + margin1){ posX2 = targetGraphMem.getPosX2() + margin1; }
					if(posY2 < targetGraphMem.getPosY2() + margin1){ posY2 = targetGraphMem.getPosY2() + margin1; }
				}
			}
			memMap.clear();
			memMap.putAll(memMapTemp);
			SubFrame.resetList(memMap);
			
			/////////////////////////////////////////////////////////////
			// ���ȥ����ΰ���Ĵ��
			relaxEdge();
			relaxAngle();
			membraneAndAtomRepulsive();
			membraneAttraction();
			atomAndAtomRepulsive();
			memAndMemRepulsive();
			moveCalc();
		}
		
	}
	
	/**
	 * ��κ����X��ɸ��������롣
	 * @return
	 */
	public int getPosX1(){ return posX1; }
	
	/**
	 * ��α�����X��ɸ��������롣
	 * @return
	 */
	public int getPosX2(){ return posX2; }
	
	/**
	 * ��κ����Y��ɸ��������롣
	 * @return
	 */
	public int getPosY1(){ return posY1; }
	
	/**
	 * ��α�����Y��ɸ��������롣
	 * @return
	 */
	public int getPosY2(){ return posY2; }
	
	/**
	 * ��˴ޤޤ�륢�ȥ�ΰ��֤�ºݤ��ѹ����롣
	 *
	 */
	private void moveCalc(){
		if(!viewInside){
			dummyGraphAtom.moveDelta(dx, dy);
			dx = dy =0;
			dummyGraphAtom.moveCalc();
			return;
		}
		
		Iterator<GraphAtom> graphAtoms = atomMap.values().iterator();
		// ���ľ�ܴޤޤ�륢�ȥब�о�
		while(graphAtoms.hasNext()){
			GraphAtom targetAtom = graphAtoms.next();
			targetAtom.moveDelta(dx, dy);
			targetAtom.moveCalc();
		}
		

		// ���ľ�ܴޤޤ���줬�о�
		Iterator<GraphMembrane> graphMems = memMap.values().iterator();
		while(graphMems.hasNext()){
			GraphMembrane targetMem = graphMems.next();
			targetMem.moveDelta(dx, dy);
		}
		dx = dy =0;
		
	}
	
	/**
	 * ���ꤵ�줿����б�����GraphMembrane��������롣
	 * @param targetMem
	 * @return
	 */
	public GraphMembrane findGraphMem(Membrane targetMem){
		if(memMap.containsKey(targetMem)){
			return memMap.get(targetMem);
		}
		Iterator<GraphMembrane> graphMems = memMap.values().iterator();
		while(graphMems.hasNext()){
			GraphMembrane graphMem = graphMems.next().findGraphMem(targetMem);
			if(graphMem != null){ return graphMem; }
			
		}
		return null;
	}
	
	/**
	 * ���٤�Ĵ�᤹�롣
	 * ���������ºݤΰ�ư��moveCalc���ƤФ��ޤǹԤ��ʤ���
	 *
	 */
	private void relaxAngle(){
		GraphAtom targetAtom;
		Iterator<GraphAtom> graphAtoms = atomMap.values().iterator();
		
		// ���ľ�ܴޤޤ�륢�ȥब�о�
		while(graphAtoms.hasNext()){
			targetAtom = graphAtoms.next();
			
			// ProxyAtom��̵��
			if(targetAtom.me.getFunctor().isInsideProxy() ||
					targetAtom.me.getFunctor().isOutsideProxy())
			{
				continue;
			}
			
			int edgeNum = targetAtom.me.getEdgeCount(); 
			
			if(edgeNum < 2){ continue; }
			
			Map<Double, GraphAtom> treeMap = new TreeMap<Double, GraphAtom>();
			
			// �Ĥʤ��äƤ��륢�ȥ������
			for(int i = 0; i < edgeNum; i++){
				GraphAtom nthAtom = (GraphAtom)atomMap.get(targetAtom.me.nthAtom(i));
				
				nthAtom = getRealNthAtom(nthAtom);
				
				if(nthAtom == null){ continue; }
				
				if(null != nthAtom){
					double dx = nthAtom.getPosX() - targetAtom.getPosX();
					double dy = nthAtom.getPosY() - targetAtom.getPosY();
					if(dx == 0.0){ dx=0.000000001; }
					double angle = Math.atan(dy / dx);
					if(dx < 0.0) angle += Math.PI;
					treeMap.put(angle, nthAtom);
				}
			}
			
			Object[] nthAngles = treeMap.keySet().toArray();
			for(int i = 0; i < nthAngles.length; i++ ){
				Double nthAngle = (Double)nthAngles[i];
				GraphAtom nthAtom = (GraphAtom)treeMap.get(nthAngle);
				
//				nthAtom = getRealNthAtom(nthAtom);
				if(nthAtom == null){ continue; }
				
				
				// ProxyAtom��̵��
				if((nthAtom.me != null) && (nthAtom.me.getFunctor().isInsideProxy() ||
						nthAtom.me.getFunctor().isOutsideProxy()))
				{
					continue;
				}
				
				if(null != nthAtom){
					double anglePre = (i != 0) ? ((Double)nthAngles[i]).doubleValue() - ((Double)nthAngles[i - 1]).doubleValue() 
							: (Math.PI * 2) - ((Double)nthAngles[nthAngles.length - 1]).doubleValue() + ((Double)nthAngles[0]).doubleValue();
					double angleCur = (i != nthAngles.length - 1) ? ((Double)nthAngles[i + 1]).doubleValue() - ((Double)nthAngles[i]).doubleValue() 
							: (Math.PI * 2) - ((Double)nthAngles[nthAngles.length - 1]).doubleValue() + ((Double)nthAngles[0]).doubleValue();
					double angleR = angleCur - anglePre;
					double dx = nthAtom.getPosX() - targetAtom.getPosX();
					double dy = nthAtom.getPosY() - targetAtom.getPosY();
					double edgeLength = Math.sqrt(dx * dx + dy * dy);
					if(edgeLength == 0.0){ edgeLength = 0.00001; }
					//��ʬ�˿�ľ��Ĺ�����Υ٥��ȥ�
					// ���줬 you ��Ư���Ϥ�ñ�̥٥��ȥ�ˤʤ�
					double tx = -dy / edgeLength;
					double ty =  dx / edgeLength;
					
					dx = 1.5 * tx * angleR;
					dy = 1.5 * ty * angleR;
					
					dx = dx * 2;
					dy = dy *2;
					
					targetAtom.moveDelta(-dx, -dy);
					nthAtom.moveDelta(dx, dy);
					
				}
			}
			
		}
	}
	
	/**
	 * ��ΰ��ϡ�
	 * ��¦�Υ��ȥ��Ư��
	 *
	 */
	private void membraneAttraction(){
		Iterator<GraphAtom> graphAtoms = atomMap.values().iterator();
		
		int memCenterX;
		int memCenterY;
		
		if(root){
			memCenterX =  panel.getWidth() / 2;
			memCenterY =  panel.getHeight() / 2;
		} else {
			memCenterX = getCenterX();
			memCenterY = getCenterY();
		}
		// ���ľ�ܴޤޤ�륢�ȥब�о�
		while(graphAtoms.hasNext()){
			GraphAtom targetAtom = graphAtoms.next();
			// ProxyAtom��̵��
			if(targetAtom.me.getFunctor().isInsideProxy() ||
					targetAtom.me.getFunctor().isOutsideProxy())
			{
				continue;
			}
			
			double dx = memCenterX - targetAtom.getPosX();
			double dy = memCenterY - targetAtom.getPosY();
			
			double ddx = 0.01 * dx;
			double ddy = 0.01 * dy;
			
			targetAtom.moveDelta(ddx, ddy);
		}
		// ���ľ�ܴޤޤ���줬�о�
		Iterator<GraphMembrane> graphMems = memMap.values().iterator();
		while(graphMems.hasNext()){
			GraphMembrane targetMem = graphMems.next();
			double dx = memCenterX - targetMem.getCenterX();
			double dy = memCenterY - targetMem.getCenterY();
			
			double ddx = 0.01 * dx;
			double ddy = 0.01 * dy;
			
			targetMem.moveDelta(ddx, ddy);
		}
		
	}
	

	/**
	 * �������
	 *
	 */
	private void memAndMemRepulsive(){
		Iterator<GraphMembrane> graphMems = memMap.values().iterator();

		while(graphMems.hasNext()){
			GraphMembrane targetMem = graphMems.next();
			int targetCenterX = targetMem.getCenterX();
			int targetCenterY = targetMem.getCenterY();
			int targetSizeX = targetMem.getSizeX();
			int targetSizeY = targetMem.getSizeY();
			// ���٤Ƥλ���ȤνŤʤ�򸡽�
			Iterator<GraphMembrane> graphNthMems = memMap.values().iterator();
			while(graphNthMems.hasNext()){
				GraphMembrane nthMem = graphNthMems.next();
				if(targetMem.equals(nthMem)){ continue; }
				int nthCenterX = nthMem.getCenterX();
				int nthCenterY = nthMem.getCenterY();
				int nthSizeX = nthMem.getSizeX();
				int nthSizeY = nthMem.getSizeY();
				
				if((Math.abs(targetCenterX - nthCenterX) < targetSizeX + nthSizeX) &&
						(Math.abs(targetCenterY - nthCenterY) < targetSizeY + nthSizeY))
			    {
					double dx = targetCenterX - nthCenterX;
					double dy = targetCenterY - nthCenterY;
					
					double ddx = 0.02 * dx;
					double ddy = 0.02 * dy;
					
					targetMem.moveDelta(ddx, ddy);
					nthMem.moveDelta(-ddx, -ddy);
				}
			}
		}
	}
	
	public int getSizeX(){
		return getPosX2() - getPosX1() + (GraphAtom.getAtomSize() * 2);
	}
	
	public int getSizeY(){
		return getPosY2() - getPosY1() + (GraphAtom.getAtomSize() * 2);
	}
	
	public int getCenterX(){
		return (getPosX1() + getPosX2()) / 2;
	}
	
	public int getCenterY(){
		return (getPosY1() + getPosY2()) / 2;
	}

	/**
	 * �쥢�ȥ������
	 *
	 */
	// FIXME: ��ʬ��ǡ��������ǤϤʤ����濴����Υ���褦�ˡ�
	private void membraneAndAtomRepulsive(){
		Iterator<GraphAtom> graphAtoms = atomMap.values().iterator();
		
		// ���ľ�ܴޤޤ�륢�ȥब�о�
		while(graphAtoms.hasNext()){
			GraphAtom targetAtom = graphAtoms.next();
			// ProxyAtom��̵��
			if(targetAtom.me.getFunctor().isInsideProxy() ||
					targetAtom.me.getFunctor().isOutsideProxy())
			{
				continue;
			}
			
			int atomBottomX = targetAtom.getPosX() + GraphAtom.getAtomSize(); 
			int atomBottomY = targetAtom.getPosY() + GraphAtom.getAtomSize(); 
			int margin = (int)(GraphAtom.getAtomSize() * 2.5);
			
			// ���٤Ƥλ���ȤνŤʤ�򸡽�
			Iterator<GraphMembrane> graphMems = memMap.values().iterator();
			while(graphMems.hasNext()){
				GraphMembrane targetMem = graphMems.next();
				int targetMemPosX1 = targetMem.getPosX1() - margin;
				int targetMemPosY1 = targetMem.getPosY1() - margin;
				int targetMemPosX2 = targetMem.getPosX2() + margin;
				int targetMemPosY2 = targetMem.getPosY2() + margin;
				int memCenterX = (targetMem.getPosX1() + targetMem.getPosX2()) / 2;
				int memCenterY = (targetMem.getPosY1() + targetMem.getPosY2()) / 2;
				// ��ξ����˽ŤʤäƤ������
				if(targetMemPosX1 < atomBottomX &&
						targetMemPosX2 > targetAtom.getPosX() &&
						targetMemPosY1 < atomBottomY &&
						targetMemPosY2 > targetAtom.getPosY()){
					
					double dx = memCenterX - targetAtom.getPosX();
					double dy = memCenterY - targetAtom.getPosY();
					double sizeX = memCenterX - targetMemPosX1;
					double sizeY = memCenterY - targetMemPosY1;
					
					double edgeLen = Math.sqrt((double)((dx * dx) + (dy * dy)));
					double length = Math.sqrt((double)((sizeX * sizeX) + (sizeY * sizeY)));
					double f = (edgeLen - (length * GraphPanel.getMagnification()));
					double ddx = 0.05 * f * dx / edgeLen;
					double ddy = 0.05 * f * dy / edgeLen;
					
					targetAtom.moveDelta(-ddx, -ddy);
				}
			}
		}
		
	}
	
	/**
	 * ���ȥ������
	 */
	private void atomAndAtomRepulsive(){
		GraphAtom targetAtom;
		Iterator<GraphAtom> graphAtoms = atomMap.values().iterator();
		
		// ���ľ�ܴޤޤ�륢�ȥब�о�
		while(graphAtoms.hasNext()){
			double length = EDGE_LENGTH;
			targetAtom = (GraphAtom)graphAtoms.next();
			// ProxyAtom��̵��
			if(targetAtom.me.getFunctor().isInsideProxy() ||
					targetAtom.me.getFunctor().isOutsideProxy())
			{
				continue;
			}
			
			int dx = 0, dy = 0;
			Iterator<GraphAtom> firendGraphAtoms = atomMap.values().iterator();
			
			// ���ľ�ܴޤޤ�륢�ȥब�о�
			while(firendGraphAtoms.hasNext()){
				GraphAtom nthAtom = firendGraphAtoms.next();
				if(targetAtom == nthAtom){
					continue;
				}
				dx = nthAtom.getPosX() - targetAtom.getPosX();
				dy = nthAtom.getPosY() - targetAtom.getPosY();
				
				double edgeLen = Math.sqrt((double)((dx * dx) + (dy * dy)));
				if(edgeLen > GraphAtom.getAtomSize()){
					continue;
				}
				double f = (edgeLen - (length * GraphPanel.getMagnification()));
				double ddx = 0.05 * f * dx / edgeLen;
				double ddy = 0.05 * f * dy / edgeLen;
				
				targetAtom.moveDelta(ddx, ddy);
				nthAtom.moveDelta(-ddx, -ddy);
			}
		}
		
	}
	
	/**
	 * ��󥯤�Ĺ����Ĵ�᤹�롣
	 *
	 */
	private void relaxEdge(){
		GraphAtom targetAtom;
		Iterator graphAtoms = atomMap.values().iterator();
		
		// ���ľ�ܴޤޤ�륢�ȥब�о�
		while(graphAtoms.hasNext()){
			double length = EDGE_LENGTH;
			targetAtom = (GraphAtom)graphAtoms.next();
			// ProxyAtom��̵��
			if(targetAtom.me.getFunctor().isInsideProxy() ||
					targetAtom.me.getFunctor().isOutsideProxy())
			{
				continue;
			}
			
			int dx = 0, dy = 0;
			int edgeNum = targetAtom.me.getEdgeCount(); 
			
			if(edgeNum == 0){ continue; }
			
			// �Ĥʤ��äƤ��륢�ȥ������
			for(int i = 0; i < edgeNum; i++){
				GraphAtom nthAtom = (GraphAtom)atomMap.get(targetAtom.me.nthAtom(i));
				nthAtom = getRealNthAtom(nthAtom);
				if(null == nthAtom){
					continue;
				}
				
				if(nthAtom == null){ continue; }
				else if(nthAtom.isDummy()){
					length = EDGE_LENGTH_TO_DUMMY;
				}
				
				dx = nthAtom.getPosX() - targetAtom.getPosX();
				dy = nthAtom.getPosY() - targetAtom.getPosY();
				
				double edgeLen = Math.sqrt((double)((dx * dx) + (dy * dy)));
				double f = (edgeLen - (length * GraphPanel.getMagnification()));
				double ddx = 0.05 * f * dx / edgeLen;
				double ddy = 0.05 * f * dy / edgeLen;
				
				targetAtom.moveDelta(ddx, ddy);
				nthAtom.moveDelta(-ddx, -ddy);
			}
		}
		
	}
	
	public GraphAtom getGraphAtom(Atom atom){
		return atomMap.get(atom);
	}
	
	public GraphAtom getDummyGraphAtom(){
		return dummyGraphAtom;
	}
	
	public GraphMembrane getParent(){
		return myParent;
	}
	
	public boolean isRoot(){
		return root;
	}
	
	/** 
	 * �������٤�GraphAtom��õ���������롣
	 * <p>
	 * nthAtom��proxyAtom�ʤɤ��ä����ϡ�proxyAtom����ˤĤʤ��äƤ��륢�ȥ��������롣
	 * nthAtom����ɽ�������¸�ߤ������ϡ�ɽ��������ꤵ��Ƥ���������ΤҤȤĤ�������ɽ����Υ��ߡ����ȥ��������롣
	 * @param nthAtom
	 * @return
	 */
	public GraphAtom getRealNthAtom(GraphAtom nthAtom){
		Atom toProxyAtom = nthAtom.me;
		if(toProxyAtom == null){ return null; }
		GraphMembrane toProxyMem = null;
		// ����褬ProxyAtom���ä��顢������ʼ���γ��ޤ��ϻ�����ˤΥ��ȥ�����
		while(toProxyAtom.getFunctor().isInsideProxy() ||
				toProxyAtom.getFunctor().isOutsideProxy()) 
		{
			toProxyAtom = toProxyAtom.nthAtom(0).nthAtom(1);
			toProxyMem = findGraphMem(toProxyAtom.getMem());
			if(toProxyMem == null){ break; }
			nthAtom = toProxyMem.getGraphAtom(toProxyAtom);
//			if(nthAtom == null){ break; }
		}
		
		if((nthAtom == null) && (toProxyMem != null)){
			while((!toProxyMem.isRoot()) && (!toProxyMem.getParent().getViewInside())){
				toProxyMem = toProxyMem.getParent();
			}
			nthAtom = toProxyMem.getDummyGraphAtom();
		}
		return nthAtom;
	}
	
	/**
	 * ��������������������褹�롣
	 * �����������⤬��ɽ������ˤʤäƤ�����ϡ�
	 * ���ȥप��ӻ�������褷�ʤ���
	 * @param g
	 */
	public void paint(Graphics g){
		int deltaPos = (int)((GraphAtom.ATOM_DEF_SIZE / 2) * GraphPanel.getMagnification());
		int margin1 = GraphAtom.getAtomSize() * 2;
		int margin2 = GraphAtom.getAtomSize() * 4;

		
		((Graphics2D)g).setStroke(MEM_STROKE);
		
		// �ɤ�Ĥ֤��ʤ�����������
		if(viewInside && !root){
			g.setColor(MEM_COLOR);
			g.drawRoundRect(posX1 - margin1,
					posY1 - margin1,
					posX2 - posX1 + margin2,
					posY2 - posY1 + margin2,
					ROUND,
					ROUND);
		}
		synchronized (atomMap) {
			
			if(getViewInside()){
				Iterator graphAtoms = atomMap.values().iterator();
				
				// ��󥯤�����
				while(graphAtoms.hasNext()){
					GraphAtom targetAtom = (GraphAtom)graphAtoms.next();
					// ProxyAtom��̵��
					if(targetAtom.me.getFunctor().isInsideProxy() ||
							targetAtom.me.getFunctor().isOutsideProxy())
					{
						continue;
					}
					
					int edgeNum = targetAtom.me.getEdgeCount(); 
					g.setColor(Color.GRAY);
					
					if(edgeNum == 0){
						continue; 
					}
					
					// �Ĥʤ��äƤ��륢�ȥ������
					for(int i = 0; i < edgeNum; i++){
						GraphAtom nthAtom = (GraphAtom)atomMap.get(targetAtom.me.nthAtom(i));
						
						if(null != nthAtom){
							if(nthAtom.me == null)
								System.out.println(nthAtom.me);
							nthAtom = getRealNthAtom(nthAtom);
							
							if(nthAtom == null){ continue; }
							
							if((!nthAtom.isDummy()) && (targetAtom.me.getid() < nthAtom.me.getid())){
								continue;
							}
							g.drawLine(targetAtom.getPosX() + deltaPos,
									targetAtom.getPosY() + deltaPos,
									nthAtom.getPosX() + deltaPos,
									nthAtom.getPosY() + deltaPos);
						}
					}
				}
				
				// ���ȥ������
				graphAtoms = atomMap.values().iterator();
				while(graphAtoms.hasNext()){
					GraphAtom targetAtom = (GraphAtom)graphAtoms.next();
					if(targetAtom.me.getFunctor().isInsideProxy() ||
							targetAtom.me.getFunctor().isOutsideProxy())
					{
						continue;
					}
					targetAtom.paint(g, panel);
				}
				
				// �������
				Iterator graphMems = memMap.values().iterator();
				while(graphMems.hasNext()){
					GraphMembrane targetMem = (GraphMembrane)graphMems.next();
					targetMem.paint(g);
				}
			}
			
			// �ɤ�Ĥ֤��������������
			if(!viewInside && !root){
				int posX = posX1 - margin1;
				int posY = posY1 - margin1;
				int sizeX = posX2 - posX1 + margin2;
				int sizeY = posY2 - posY1 + margin2;
				// �ɤ�Ĥ֤�
				g.setColor(MEM_COLOR);
				g.fillRoundRect(posX,
						posY,
						sizeX,
						sizeY,
						ROUND,
						ROUND);
				// ������
				g.setColor(MEM_BORDER_COLOR);
				g.drawRoundRect(posX1 - margin1,
						posY1 - margin1,
						sizeX,
						sizeY,
						ROUND,
						ROUND);
				
				// �ԥ������
				if(dummyGraphAtom.isClipped()){
					dummyGraphAtom.paintPin(g, panel, -GraphAtom.getAtomSize() / 2, -GraphAtom.getAtomSize() / 2);
				}
			}
			
		}
	}
	
	/**
	 * ���٤Ƥ����ɽ��������
	 *
	 */
	public void showAll(){
		viewInside = true;
		Iterator<GraphMembrane> graphMems = memMap.values().iterator();
		while(graphMems.hasNext()){
			graphMems.next().showAll();
		}
	}
	
	/**
	 * ��������褹�뤫�ɤ���
	 * ��ɽ���˻��ꤵ�줿���ϡ���¹��⤹�٤���ɽ���ˤ��롣
	 * ɽ���˻��ꤵ�줿���ϡ�������⤹�٤�ɽ���ˤ��롣
	 * @param view
	 */
	public void setViewInside(boolean view){
		viewInside = view;
		if(viewInside){ 
			if(myParent != null){
				myParent.setViewInside(true);
			}
			return;
		}
		
		Iterator<GraphMembrane> graphMems = memMap.values().iterator();
		while(graphMems.hasNext()){
			graphMems.next().setViewInside(false);
		}
	}
	
	
	/**
	 * ��������褹�뤫�ɤ���
	 * @return
	 */
	public boolean getViewInside(){
		return (viewInside || root);
	}
	
	/** 
	 * clickedPoint �˰��ֶᤤ���ȥ���������
	 * �����оݤˤϻ����ޤޤ�롣
	 * @param clickedPoint
	 */
	public GraphAtom getNearestAtom(Point clickedPoint){
		GraphAtom nearestAtom = null;
		double distance = 0.0;
		synchronized (atomMap) {
			Iterator atoms = atomMap.values().iterator();
			
			// �����ɽ�����ʤ����ϡ����ߡ�GraphAtom�ǰ��֤����
			if(!getViewInside()){
				GraphAtom targetAtom = dummyGraphAtom;
				double dx = targetAtom.getPosX() - clickedPoint.x;
				double dy = targetAtom.getPosY() - clickedPoint.y;
				double distanceTmp = Math.sqrt((dx * dx) + (dy * dy));
				
				if(null != nearestAtom){
					if(distance >= distanceTmp){
						distance = distanceTmp;
						nearestAtom = targetAtom;
					}
				} else {
					distance = distanceTmp;
					nearestAtom = targetAtom;
				}
			}
			
			// �����ɽ��������������ȥ���Ф��ư��֤����
			while(getViewInside() && atoms.hasNext()){
				GraphAtom targetAtom = (GraphAtom)atoms.next();
				
				// ProxyAtom��̵��
				if(targetAtom.me.getFunctor().isInsideProxy() ||
						targetAtom.me.getFunctor().isOutsideProxy())
				{
					continue;
				}
				
				double dx = targetAtom.getPosX() - clickedPoint.x;
				double dy = targetAtom.getPosY() - clickedPoint.y;
				double distanceTmp = Math.sqrt((dx * dx) + (dy * dy));
				
				if(null != nearestAtom){
					if(distance < distanceTmp){
						continue;
					}
					distance = distanceTmp;
					nearestAtom = targetAtom;
				} else {
					distance = distanceTmp;
					nearestAtom = targetAtom;
				}
			}
			
			// ���줫�������Ǥΰ��ֶᤤ���ȥ��������������ݻ����Ƥ��륢�ȥ����Ӥ���
			Iterator mems = memMap.values().iterator();
			while(mems.hasNext()){
				GraphMembrane targetMem = (GraphMembrane)mems.next();
				GraphAtom targetAtom = targetMem.getNearestAtom(clickedPoint);
				double dx = targetAtom.getPosX() - clickedPoint.x;
				double dy = targetAtom.getPosY() - clickedPoint.y;
				double distanceTmp = Math.sqrt((dx * dx) + (dy * dy));
				
				if(null != nearestAtom){
					if(distance < distanceTmp){
						continue;
					}
					distance = distanceTmp;
					nearestAtom = targetAtom;
				} else {
					distance = distanceTmp;
					nearestAtom = targetAtom;
				}
			}
		}
		
		return nearestAtom;
	}
	
	
	/**
	 * �����ʸ����Ȥ����֤���
	 */
	public String toString(){
		return myMem.toString();
	}
}
