package gui2;

import java.awt.Color;
import java.awt.Graphics;
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
	private double EDGE_LENGTH = 150.0;
	
	final static
	private int EDGE_ERROR = 10;
	
	final static
	private double EDGE_MOVE_DELTA = 0.04;
	
	/////////////////////////////////////////////////////////////////


	private int posX = 0;
	private int posY = 0;
	private boolean viewInside = true;
	private Map<Atom, GraphAtom> atomMapTemp =
		Collections.synchronizedMap(new HashMap<Atom, GraphAtom>());
	
	private Map<Atom, GraphAtom> atomMap =
		Collections.synchronizedMap(new HashMap<Atom, GraphAtom>());
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	public GraphMembrane(Membrane mem) {
		resetMembrane(mem);
	}
	
	/////////////////////////////////////////////////////////////////
	
	public void resetMembrane(Membrane mem){
		synchronized (atomMap) {
			/////////////////////////////////////////////////////////////
			// ���ȥ����������
			atomMapTemp.clear();
			Iterator<Atom> atoms = mem.atomIterator();
			while(atoms.hasNext()){
				Atom atom = atoms.next();
				if(atomMap.containsKey(atom)){
					atomMapTemp.put(atom, atomMap.get(atom));
				}
				else{
					atomMapTemp.put(atom, new GraphAtom(atom));
				}
			}
			atomMap.clear();
			atomMap.putAll(atomMapTemp);
			
			/////////////////////////////////////////////////////////////
			// ���ȥ�ΰ���Ĵ��
			relaxEdge();
			relaxAngle();
			moveCalc();
		}
		
	}

	private void moveCalc(){
		Iterator<GraphAtom> graphAtoms = atomMap.values().iterator();
		
		// ���ľ�ܴޤޤ�륢�ȥब�о�
		while(graphAtoms.hasNext()){
			graphAtoms.next().moveCalc();
		}
		
	}
	
	private void relaxAngle(){
		GraphAtom targetAtom;
		Iterator<GraphAtom> graphAtoms = atomMap.values().iterator();
		
		// ���ľ�ܴޤޤ�륢�ȥब�о�
		while(graphAtoms.hasNext()){
			targetAtom = graphAtoms.next();
			int edgeNum = targetAtom.me.getEdgeCount(); 
			
			if(edgeNum < 2){ continue; }

			Map<Double, GraphAtom> treeMap = new TreeMap<Double, GraphAtom>();
			
			// �Ĥʤ��äƤ��륢�ȥ������
			for(int i = 0; i < edgeNum; i++){
				GraphAtom nthAtom = atomMap.get(targetAtom.me.nthAtom(i));
				if(null != nthAtom){
					double dx = nthAtom.getPosX() - targetAtom.getPosX();
					double dy = nthAtom.getPosY() - targetAtom.getPosY();
					if(dx == 0.0){ dx=0.000000001; }
					double angle = Math.atan(dy / dx);
					angle = regulate(angle);
					if(dx < 0.0) angle += Math.PI;
					treeMap.put(new Double(angle), nthAtom);
				}
			}
		
			Object[] nthAngles = treeMap.keySet().toArray();
			for(int i = 0; i < nthAngles.length; i++ ){
				Double nthAngle = (Double)nthAngles[i];
				GraphAtom nthAtom = treeMap.get(nthAngle);
				
				if(null != nthAtom){
					double anglePre = (i != 0) ? (Double)nthAngles[i] - (Double)nthAngles[i - 1] 
					                            : (Math.PI * 2) - (Double)nthAngles[nthAngles.length - 1] + (Double)nthAngles[0];
					double angleCur = (i != nthAngles.length - 1) ? (Double)nthAngles[i + 1] - (Double)nthAngles[i] 
					                      		: (Math.PI * 2) - (Double)nthAngles[nthAngles.length - 1] + (Double)nthAngles[0];
					double angleR = angleCur - anglePre;
					double dx = nthAtom.getPosX() - targetAtom.getPosX();
					double dy = nthAtom.getPosY() - targetAtom.getPosY();
					double edgeLength = Math.sqrt(dx * dx + dy * dy);
					if(edgeLength == 0.0){ edgeLength = 0.00001; }
					//��ʬ�˿�ľ��Ĺ�����Υ٥��ȥ�
					// ���줬 you ��Ư���Ϥ�ñ�̥٥��ȥ�ˤʤ�
					double tx = -dy / edgeLength;
					double ty =  dx / edgeLength;

					dx = 1.5 * tx * angleR * 2;
					dy = 1.5 * ty * angleR * 2;
					
					targetAtom.moveDelta(-dx, -dy);
					nthAtom.moveDelta(dx, dy);

				}
			}
			
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
	
	private void relaxEdge(){
		GraphAtom targetAtom;
		Iterator<GraphAtom> graphAtoms = atomMap.values().iterator();
		
		// ���ľ�ܴޤޤ�륢�ȥब�о�
		while(graphAtoms.hasNext()){
			targetAtom = graphAtoms.next();
			int dx = 0, dy = 0;
			int edgeNum = targetAtom.me.getEdgeCount(); 
			
			if(edgeNum == 0){ continue; }
			
			// �Ĥʤ��äƤ��륢�ȥ������
			for(int i = 0; i < edgeNum; i++){
				GraphAtom nthAtom = atomMap.get(targetAtom.me.nthAtom(i));
				if(null == nthAtom){
					continue;
				}
				dx = nthAtom.getPosX() - targetAtom.getPosX();
				dy = nthAtom.getPosY() - targetAtom.getPosY();
				
				double edgeLen = Math.sqrt((double)((dx * dx) + (dy * dy)));
				double f = (edgeLen - (EDGE_LENGTH * GraphPanel.getMagnification()));
				double ddx = 0.05 * f * dx / edgeLen;
				double ddy = 0.05 * f * dy / edgeLen;
				
				targetAtom.moveDelta(ddx, ddy);
				nthAtom.moveDelta(-ddx, -ddy);
			}
		}
		
	}
	
	public void paint(Graphics g){
		int deltaPos = (int)((GraphAtom.ATOM_DEF_SIZE / 2) * GraphPanel.getMagnification());
		synchronized (atomMap) {
			Iterator<GraphAtom> graphAtoms = atomMap.values().iterator();
			
			// ��󥯤�����
			while(graphAtoms.hasNext()){
				GraphAtom targetAtom = graphAtoms.next();
				int edgeNum = targetAtom.me.getEdgeCount(); 
				g.setColor(Color.GRAY);
				
				if(edgeNum == 0){
					continue; 
				}
				
				// �Ĥʤ��äƤ��륢�ȥ������
				for(int i = 0; i < edgeNum; i++){
					GraphAtom nthAtom = atomMap.get(targetAtom.me.nthAtom(i));
					if(null != nthAtom){
						if(targetAtom.me.getid() < nthAtom.me.getid()){
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
				GraphAtom targetAtom = graphAtoms.next();
				targetAtom.paint(g);
			}

		}
	}

}
