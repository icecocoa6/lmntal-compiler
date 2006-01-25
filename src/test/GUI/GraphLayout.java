package test.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;

import runtime.AbstractMembrane;
import runtime.Env;

public class GraphLayout extends AbstractGraphLayout {
	private GraphDialog dialog;
	
	public GraphLayout(Component parent){
		super(parent);
	}
	
	
	/**
	 * �� m �ˤ��륢�ȥ�ˤĤ����Ϥ���Ѥ����롣
	 * @param m
	 */
	boolean testf = false;
	protected void relax(runtime.AbstractMembrane m) {
		if(m==null) return;
		Object[] mems = m.getMemArray();
		atoms = separateAtomGroup(m);
		AtomGroup atomgroup;
		
		//���ȥ॰�롼�ץƥ��Ƚ���
//		if(!testf){
//			testf = true;
//			int testi = 0;
//			for(Iterator it = atoms.iterator();it.hasNext();testi++){
//				atomgroup = (AtomGroup)it.next();
//				System.out.print("Group"+(testi+1)+":");
//				for(Iterator it2 = atomgroup.atoms.iterator();it2.hasNext();){
//					System.out.print(((Node)it2.next()).getName()+":");
//				}
//				System.out.println("\n");
//			}
//		}
//		
		//���ȥ॰�롼�פ��Ĥ��ļ��Ф����ޤޤ�륢�ȥ�˽�����Ԥ���
		for(Iterator it = atoms.iterator();it.hasNext();){
			atomgroup = (AtomGroup)it.next();
			
			if(!doesCutAcrossMembrane(atomgroup,m))
			if(getFlag(6))//10))
				if(!isFixed(atomgroup)){
					atomgroup.rotateAtomGroup();
				}
			for (Iterator i=atomgroup.atoms.iterator();i.hasNext();) {
				Node me = (Node)i.next();
				if(!me.isVisible()) continue;
				
				//�դ�Ĺ���ȳ��٤����ˤ��롣
				if(getFlag(0)){
			//	calculateEdge(me);
				calculateEdgeLength(me);
				
				calculateEdge2(me);
				}
				//����濴���Ϥ򤫤���
				//���ȥ॰�롼�פ��Ȥ��ѹ�
				/*double d;
				DoublePoint pos = me.getPosition();
				dx = pos.x;dy = pos.y;
				//�Ƥ�̵����ξ��ϲ��̤�������Ϥ򤫤��롣
				if(m == rootMem){
					dx -= 400;dy -= 300;
					//dx -= area.getCenterX();dy -= area.getCenterY();
					System.out.println("testnulll");
				} else {
					dx -= m.rect.getCenterX();dy -= m.rect.getCenterY();
				}
				d = Math.sqrt(dx*dx + dy*dy);
				dx = 1.2*dx/d;
				dy = 1.2*dy/d;
				me.setMoveDelta(-dx,-dy);*/
			}
			
			//����濴���Ϥ򤫤���
			if(getFlag(1))
			attractToCenter(atomgroup,m);
			//�����¾�Υ��ȥ��겼¦�����ڲ���
			if(getFlag(4))//2))
			upForceAtomGroup(atomgroup,m,mems);
//			//��˴ޤޤ�륢�ȥ�Ʊ�Τ�����
//			for(Iterator i1 =atomgroup.atoms.iterator();i1.hasNext();){
//				double teisuu = 2;
//				double dx = 0;
//				double dy = 0;
//				
//				Node me = (Node)i1.next();
//				if(!me.isVisible())continue;
//				
//				for(Iterator i2 = m.atomIterator();i2.hasNext();){
//					Node me2 = (Node)i2.next();
//					if(!me2.isVisible()&&me==me2)continue;
//					Edge edge = new Edge(me,me2);
//					double f = teisuu / Math.pow(edge.getLen(),2);
//					dx = f*((me.getPosition().x-me2.getPosition().x)/edge.getLen());
//					dy = f*((me.getPosition().y-me2.getPosition().y)/edge.getLen());
//					me.setMoveDelta(dx,dy);
//					me2.setMoveDelta(-dx,-dy);
//					
//				}				
//					
//			}
		}
		
		
		/*
		 * ��˴ޤޤ�륢�ȥ�ҤȤĤŤĤ��Ϥ�ä��롣
		 */
		for(Iterator i = m.atomIterator();i.hasNext();){
			Node me = (Node)i.next();
			if(!me.isVisible())continue;
			//��°���ʤ�����ϰϤ���Ф�褦���Ϥ򤫤��롣
			//����ϰϤ����褹��ޡ������꾮����Ƚ�ꤹ�뤿��˥Ρ��ɤκ�ɸ��¦��Ĵ�������ͤ�Ȥ���
			if(getFlag(2))//3))
			repulseAtomFromMembrane(me,mems);
		}
		
		
		/*
		 * ��󥯤ǷҤ��줿���ȥ�Υ��롼�ס�����ϰϤ��ŤʤäƤ������˽ŤʤäƤ���
		 * ���롼�פ�Υ���褦�˥��롼��������ƤΥ��ȥ���Ϥ�ä��롣
		 * 
		 * �� m �˴ޤޤ�륢�ȥ॰�롼�פ����Ƥΰ��а���ȹ�碌���Ф��ƽ����򤹤�롼�ס�
		 */
		AtomGroup atomgroup1,atomgroup2;
		for(int j = 0;j < atoms.size()-1;j++){
			for(int k = j+1;k < atoms.size();k++){
				atomgroup1 = (AtomGroup)atoms.get(j);
				atomgroup2 = (AtomGroup)atoms.get(k);
				
				//���롼�פ��ŤʤäƤ����������Ϥ򤫤���
				if(getFlag(2))//4))
				repulsiveForceBetweenAtomgroups(atomgroup1,atomgroup2);
								
				//�Ŀ���¿�����ȥ॰�롼�פ����ʤ���Τ�������褦���Ϥ򤫤��롣
				//5�İʾ� > 4,3�� > 2�ġ�> 1�ġ����Ȥꤢ����
				if(getFlag(3))//5))
				sortAtomGroupBySize(atomgroup1,atomgroup2,m);
					
				
				//Ʊ���Ŀ��Υ��롼��Ʊ�Τǰ����դ������Ϥ�ä��롣
				/*
				if(((AtomGroup)atoms.get(j)).atoms.size() == ((AtomGroup)atoms.get(k)).atoms.size()){
					double dx,dy,d;
					Node n;
					
					DoublePoint pos = ((AtomGroup)atoms.get(j)).getCenterPos();
					dx = pos.x;dy = pos.y;
					pos = ((AtomGroup)atoms.get(k)).getCenterPos();
					dx -= pos.x;dy -= pos.y;
					d = Math.sqrt(dx*dx + dy*dy);
					dx = 1*dx/d;
					dy = 1*dy/d;
					
					for(Iterator i1 = ((AtomGroup)atoms.get(j)).atoms.iterator();i1.hasNext();){
						n = (Node)i1.next();
						n.setMoveDelta(-dx,-dy);
					}
					
					for(Iterator i1 = ((AtomGroup)atoms.get(k)).atoms.iterator();i1.hasNext();){
						n = (Node)i1.next();
						n.setMoveDelta(dx,dy);
					}	
				}*/
				
			}

			
/*
			mems = m.getMemArray();
			AbstractMembrane mem;
			for(int k = 0;k < mems.length;k++){
				mem = (AbstractMembrane)mems[k];
				if(((AtomGroup)atoms.get(j)).doesOverLap(mem.rect)){
					double dx,dy,d;
					Node n;
					
					DoublePoint pos = ((AtomGroup)atoms.get(j)).getCenterPos();
					dx = pos.x;dy = pos.y;
					pos = new DoublePoint(mem.rect.getCenterX(),mem.rect.getCenterY());
					dx -= pos.x;dy -= pos.y;
					d = Math.sqrt(pos.x*pos.x + pos.y*pos.y);
					dx = 5*dx/d;
					dy = 5*dy/d;
					for(Iterator i1 = ((AtomGroup)atoms.get(j)).atoms.iterator();i1.hasNext();){
						n = (Node)i1.next();
						n.setMoveDelta(dx,dy);
					}
				}
			}*/
			
		}
		
		//����ν���
		{
			double length;
			double maxy;
			double margin = Env.atomSize;
			double mx,my;
			AbstractMembrane mem1,mem2;
			
			length = margin * 1.5;


			AtomGroup atomgorup;
			
			//�����¾�Υ��ȥ��겼¦������ѡڲ���		
			maxy = 0;
			for(Iterator it = atoms.iterator();it.hasNext();){
				atomgroup = (AtomGroup)it.next();
				//������Υ��ȥ����ĥ��롼�פϽ���
				if(doesCutAcrossMembrane(atomgroup,m)){
					continue;
				}
				if(maxy < atomgroup.getMaxPos().y)
					maxy = atomgroup.getMaxPos().y;
			}
			maxy += margin;

			
			//�����Ĥ��Ĥؤν���
			for(int k = 0;k < mems.length;k++){
				mem1 = (AbstractMembrane)mems[k];
				//�����¾�Υ��ȥ��겼¦�����ڲ���
				if(getFlag(4))//6))
				downForceMems(mem1,maxy,length);
				//������濴�˸������Ϥ򤫤���
				if(getFlag(1))//7))
				attractToCenter(mem1,m);
			}

			
			//����Ʊ�Τν���
			for(int k =0;k < mems.length - 1;k++){
				for(int l = k+1;l < mems.length;l++){
					mem1 = (AbstractMembrane)mems[k];mem2 = (AbstractMembrane)mems[l];
					// ��Ʊ�ΤǽŤʤäƤ����Τ��ư
					if(getFlag(2))//8))
					repulsiveForceBetweenMems(mem1,mem2);
				}
			}
			
			
		}
	
		//�໤�Ϥʤɤν������� m �Τʤ������ƤΥ��ȥ�˹Ԥ���
		for (Iterator i = m.atomIterator();i.hasNext();){
			Node me = (Node)i.next();
			if(!me.isVisible()) continue;
			
			//�໤�Ϥ򤫤��롣�����äƤ����Ϥ������ͤ�꾮�������˰�ư�̤򣰤ˤ��롣
			//�ޤ��Ϥ��������ٰʾ���礭�����˰�����ͤޤǾ��������롣
			if(getFlag(5))//9))
				frictionalForce(me);
		}
		
		// �ºݤ˰�ư����
		for (Iterator i=m.atomIterator();i.hasNext();) {
			Node me = (Node)i.next();
			if(!me.isVisible()) continue;
			if(me==((LMNGraphPanel)parent).movingNode) continue;
			if(isFixed(me))continue;
			me.move(parent.getBounds());
		}
		// ����
		mems = m.getMemArray();
		for(int i=0;i<mems.length;i++) {
			relax((AbstractMembrane)mems[i]);
		}
	}
	
	/**
	 * �ƥ�������˴ޤޤ�륢�ȥ��̾����moveDelta����Ϥ��롣
	 * @param a
	 * @return
	 */
	void testPrint(AbstractMembrane m,String comment){
		System.out.println(comment+"\n\n");
		
		if(m == rootMem)System.out.println("rootMem");
		else System.out.println("Not rootMem");
		
		int i = 0;
		for(Iterator it = m.atomIterator();it.hasNext();i++){
			Node n = (Node)it.next();
			if(!n.isVisible()){
				continue;
			}
			System.out.print("Atom "+i);
			System.out.print("; "+n.getName());
//			System.out.print("; x "+ n.getMoveDelta().x+",y "+ n.getMoveDelta().y);
			System.out.print("; Position "+ n.getPosition().x + ", "+n.getPosition().y);
			System.out.println("");
		}
		System.out.println("\n");
//		
//		Object mems[] = m.getMemArray();
//		if(mems.length != 0){
//			for(int j = 0;j < mems.length;j++){
//				System.out.println("����" + (j+1));
//			
//				i = 0;
//				for(Iterator it = ((AbstractMembrane)mems[j]).atomIterator();it.hasNext();i++){
//					Node n = (Node)it.next();
//					System.out.print("Atom "+i);
//					System.out.print("; "+n.getName());
//					System.out.print("; x "+ n.getMoveDelta().x+",y "+ n.getMoveDelta().y);
//					System.out.print("; Position "+ n.getPosition().x + ", "+n.getPosition().y);
//					if(n.getPosition().x == 0&&n.getPosition().y == 0)System.out.print("; ���̤Ϥ�");
//					System.out.println("");
//				}
//			}
//		}
//		System.out.println("\n\n\n\n");
	}
	

	/**
	 * ���ȥ॰�롼�פ��� m �λ���˴ޤޤ�륢�ȥब���뤫
	 * @param a
	 * @return
	 */
	boolean doesCutAcrossMembrane(AtomGroup atomgroup,AbstractMembrane m){
		boolean f = false;
		runtime.Atom n;
		
		Object[] mems = m.getMemArray();
		for(Iterator it = atomgroup.atoms.iterator();it.hasNext();){
			n = (runtime.Atom)it.next();
			if(n.getMem() != m){
				for(int i = 0;i<mems.length;i++){
					if(n.getMem() == (AbstractMembrane)mems[i]){
						f = true;
					}
				}
				if(n.getMem() == m.getParent())f = true;
			}
			
		}
		
		return f;
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
	
	/*
	 * 1/r,1/r^2,����ʤɤδؿ������̾�����ѹ����롣
	 */
	/**
	 * y1 �� y2 ��꾮�����ä��Ȥ��� y2 ����礭���ʤ�褦����
	 */
	double force(double y1,double y2){
		double f1 = 1.6,f2 = 6.0;
		double dy = 0;
		double d,f;
		f1 = 1.6;f2 = 6.0;
		d = y1 - y2;
		d = f1 + d/15;
		
		if(d <= 0){
			f = f2;
		} else {
			f = f1 / d;
			if(f > f2){
				f = f2;
			}
		}
		
		return f;
	}
	/**
	 * ���롼��Ʊ�Τ�ȿȯ�����Ϥ�׻����롣
	 */
	 public double repulsiveForce(AbstractMembrane m1,AbstractMembrane m2){
			DoublePoint max,min,maxa,mina;
			boolean a1,a2,a3,a4,b1,b2,b3,b4;
			double f,f1,f2,l;
			double d = 0;
			double margin = Env.atomSize;
			f = 0.0;
			f1 = 1.6;
			f2 = 6.0;
			l = margin * 1.5;
			
			max = new DoublePoint(m1.rect.getMaxX(),m1.rect.getMaxY());
			min = new DoublePoint(m1.rect.getMinX(),m1.rect.getMinY());
			maxa = new DoublePoint(m2.rect.getMaxX(),m2.rect.getMaxY());
			mina = new DoublePoint(m2.rect.getMinX(),m2.rect.getMinY());
			
//			max.x += margin;max.y += margin;
//			min.x -= margin;min.y -= margin;
//			maxa.x += margin;maxa.y += margin;
//			mina.x -= margin;mina.y -= margin;
			
			if(max.x >= mina.x - l && max.x <= maxa.x + l)a1 = true;else a1 =false;
			if(min.x >= mina.x - l && min.x <= maxa.x + l)a2 = true;else a2 =false;
			if(max.y >= mina.y - l && max.y <= maxa.y + l)a3 = true;else a3 =false;
			if(min.y >= mina.y - l && min.y <= maxa.y + l)a4 = true;else a4 =false;

			if(maxa.x >= min.x - l && maxa.x <= max.x + l)b1 = true;else b1 =false;
			if(mina.x >= min.x - l && mina.x <= max.x + l)b2 = true;else b2 =false;
			if(maxa.y >= min.y - l && maxa.y <= max.y + l)b3 = true;else b3 =false;
			if(mina.y >= min.y - l && mina.y <= max.y + l)b4 = true;else b4 =false;

			//�ϰϤ��ŤʤäƤ��ʤ����� 0 ���֤�
			if((!a1&&!a2&&!b1&&!b2)||(!a3&&!a4&&!b3&&!b4)){
				return 0;
			}
			//�Ťʤ�������櫓���Ʒ׻�
			if((a1&&a2&&a3&&a4)||(b1&&b2&&b3&&b4))return f2;
			if(((a1&&a2)&&(a3||a4))||((b1&&b2)&&(b3||b4))){
				if(a1&&a2){
					if(a3){
						d =  mina.y - max.y;
					} else if(a4){
						d =  min.y - maxa.y;
					}
				} else if(b1&&b2){
					if(b3){
						d = min.y - maxa .y;
					} else if(b4){
						d = mina .y - max.y;
					}
				}
			} else if(((a1||a2)&&(a3&&a4))||((b1||b2)&&(b3&&b4))){
				if(a3&&a4){
					if(a1){
						d = mina.x - max.x;
					} else if(a2){
						d = min.x - maxa.x;
					}
				} else if(b3&&b4){
					if(b1){
						d = min.x - maxa.x;
					} else if(b2){
						d = mina.x - max.x;
					}
				}
			} else if(((a1||a2)&&(a3||a4))||((b1||b2)&&(b3||b4))){
				double dtemp;
				if(a1){
					if(a3){
						d = mina.x - max.x;
						dtemp = mina.y - max.y;
						if(dtemp > d)d = dtemp;
					} else if(a4){
						d = mina.x - max.x;
						dtemp = min.y - maxa.y;
						if(dtemp > d)d = dtemp;
					}
				} else if(a2){
					if(a3){
						d = min.x - maxa.x;
						dtemp = mina.y - max.y;
						if(dtemp > d)d = dtemp;
					} else if(a4){
						d = min.x -maxa.x;
						dtemp = min.y - maxa.y;
						if(dtemp > d)d = dtemp;
					}
				}
			} else if(((a1||a2)&&(b3||b4))||((b1||b2)&&(a3||a4))){
				return f2;
			}
			
			return repulsiveForcef1(d,f1,f2);	

	 }
	 
	 public double repulsiveForce(Node n,AbstractMembrane m1){
		DoublePoint max,min;
		boolean b1,b2;
		double f,f1,f2,l,nx,ny;
		double d = 0,dtemp = 0;
		double margin = AtomGroup.margin * 2;
		f = 0.0;
		f1 = 1.6;
		f2 = 6.0;
		l = margin * 1.5;
		
		max = new DoublePoint(m1.rect.getMaxX(),m1.rect.getMaxY());
		min = new DoublePoint(m1.rect.getMinX(),m1.rect.getMinY());
		nx = n.getPosition().x;
		ny = n.getPosition().y;
		
//		max.x += margin;max.y += margin;
//		min.x -= margin;min.y -= margin;
//		maxa.x += margin;maxa.y += margin;
//		mina.x -= margin;mina.y -= margin;
		
		if(nx >= min.x - l - margin && nx <= max.x + l + margin)b1 = true;else b1 =false;
		if(ny >= min.y - l - margin && ny <= max.y + l + margin)b2 = true;else b2 =false;

		
		if(b1 && b2){
			if(nx - m1.rect.getCenterX() < 0){
				d = m1.rect.getMinX() - (nx + margin);
			} else {
				d = (nx - margin) - m1.rect.getMaxX();
			}
			if(ny - m1.rect.getCenterX() < 0){
				dtemp = m1.rect.getMinY() - (ny + margin);
			} else {
				dtemp = (ny - margin) - m1.rect.getMaxY();
			}
			if(d > dtemp){
				d = dtemp;
			}
			f = repulsiveForcef1(d,f1,f2);
			return f;
		} else {
			return 0;
		}
	 }
	 
	 double repulsiveForcef1(double x,double f1,double f2){
	 	double f;
	 	x = x / 15;
		if(x+f1 <= 0){
			return f2;
		}
		f = f1 / (x + f1);
		if(f > f2){
			return f2;
		}
	 	return f;
	 }
	/*
	double force1(DoublePoint d1,DoublePoint d2,double teisuu){
		return teisuu;
	}
	double force2(DoublePoint d1,DoublePoint d2,double teisuu){
		double f = teisuu / doublePointLength(d1,d2);
		return f;
	}
	double force3(DoublePoint d1,DoublePoint d2,double teisuu){
		double d = doublePointLength(d1,d2);
		double f = teisuu / d*d;
		return f;
	}
	double force4(DoublePoint d1,DoublePoint d2,double l,double teisuu){
		double d = doublePointLength(d1,d2);
		double f = teisuu * (l - d);
		return f;
	}
	double force5(DoublePoint d1,DoublePoint d2,double l,double teisuu){
		double d = doublePointLength(d1,d2);
		double f = 0;
		if(d > l)f = teisuu;
		return f;
	}
	
	double doublePointLength(DoublePoint d1,DoublePoint d2){
		double dx = d1.x - d2.x;
		double dy = d1.y - d2.y;
		
		return Math.sqrt(dx*dx + dy*dy);
	}
	*/
	/**
	 * ����󥯤ǷҤ��ä����ȥ�Υ��롼�פ˴ޤޤ��Ρ��ɤˤޤȤ�ư�ư�̤����ꤹ��
	 */
	
	public void setMoveDelta(AbstractMembrane m ,double dx ,double dy){
		Iterator i = getAllNodeIterator(m);
		while(i.hasNext()){
			((Node)i.next()).setMoveDelta(dx,dy);
		}
	}
	
	public void setMoveDelta(AtomGroup a ,double dx ,double dy){
		Iterator i = a.atoms.iterator();
		while(i.hasNext()){
			((Node)i.next()).setMoveDelta(dx,dy);
		}
	}
	
	/**
	 * ��˴ޤޤ�����ƤΥ��ȥ���֤� Iterator ���֤�
	 */
	
	public Iterator getAllNodeIterator(AbstractMembrane mem){
		Iterator it;
		Vector vector = new Vector();
		Object[] mems = mem.getMemArray();
		
		if(mems.length == 0)return mem.atomIterator();
		for(int i = 0;i<mems.length;i++){
			getAllNodeIterator((AbstractMembrane)mems[i],vector);
		}
		vector.addAll(Arrays.asList(mem.getAtomArray()));
		
		return vector.iterator();
	}
	
	private void getAllNodeIterator(AbstractMembrane mem,Vector vector){
		Object[] mems = mem.getMemArray();
		
		if(mems.length == 0){
			vector.addAll(Arrays.asList(mem.getAtomArray()));
			return;
		}
		
		for(int i = 0;i < mems.length;i++){
			getAllNodeIterator((AbstractMembrane)mems[i],vector);
		}
		return;
	}
	
	/**
	 * ���ȥ॰�롼�פ��سԤ����褹�롣 ogawa
	 * @param g
	 */
	public void paintAtomGroup(Graphics g){
		AtomGroup atomgroup;
		DoublePoint max,min;
		if(atoms != null){
			for(Iterator it = atoms.iterator();it.hasNext();){
				atomgroup = (AtomGroup)it.next();
				max = atomgroup.getMaxPos();
				min = atomgroup.getMinPos();
				max.x += Env.atomSize;max.y += Env.atomSize;
				min.x -= Env.atomSize;min.y -= Env.atomSize;
				
				g.drawRoundRect((int)min.x,(int)min.y,(int)(max.x - min.x),(int)(max.y - min.y),10,10);
			}
		}
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
		//paintAtomGroup(g);
		
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
			if(isFixed(nodes[i])){
				paintSelectedNode(g,nodes[i]);
			}
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
	
	/**
	 * ���֥륯��å��Ǹ��ꤵ��Ƥ��륢�ȥ�θ��˿���Ĥ��ޤ���
	 */
	public void paintSelectedNode(Graphics g,Node me){

		double paintMargin = 1.5;
		int wh = Env.fDEMO ? 40 : 16;
		Dimension size = new Dimension(wh, wh);
//			g.setColor(new Color(64,128,255));
		// Ŭ���˿�ʬ�����롪

		
//			System.out.println(label + "  " + ir + "  " + ig + "  " + ib);
		g.setColor(Color.GRAY);
		
		g.fillOval((int)(me.getPosition().x - size.width/2), (int)(me.getPosition().y - size.height/ 2), (int)(size.width * paintMargin), (int)(size.height * paintMargin));
		
		g.setColor(Color.BLACK);

	}
	
	//���������Ϥη׻��᥽�å�
	/**
	 * �դ�Ĺ���ȳ��٤�Ĵ�����롣
	 */
	public void calculateEdge(Node me){
		double dx = 0;
		double dy = 0;
		
//			System.out.println(i+" "+me.getName());
		
		// angle �ǥ�����
		Edge ie[] = new Edge[me.getEdgeCount()];
//			System.out.println("arround "+me+"  "+me.getEdgeCount());
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
				//���Ķ�����դξ���Ϥ�夯���롣
				if(((runtime.Atom)edge.from).getMem() != ((runtime.Atom)edge.to).getMem()){
					l = l * 1.5;
					f = f/10;
				}
				// TODO 0.5�ΤȤ����1/arity[�κ�����] �ˤ���ȿ�ư���ʤ���
				//                      1/(2 * arity[�κ�����])���Ȥʤ��褤��
				// [�κ�����]��ä��Ƥ�褤��
				// ���ξ�硢����ƥ������ȥ�μ��̤Τ褦�ʤ�ΤȤ��Ʒ׻������
				double ddx = 0.05 * f * edge.getVx()/l;
				double ddy = 0.05 * f * edge.getVy()/l;
				
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
	
	/**
	 * �դ�Ĺ�������ˤ��롣
	 */
	public void calculateEdgeLength(Node me){
		
//			System.out.println(i+" "+me.getName());
		
		// angle �ǥ�����
		Edge ie[] = new Edge[me.getEdgeCount()];
//			System.out.println("arround "+me+"  "+me.getEdgeCount());
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
				//���Ķ�����դξ���Ϥ�夯���롣
				if(((runtime.Atom)edge.from).getMem() != ((runtime.Atom)edge.to).getMem()){
					l = l * 1.5;
					f = f/10;
				}
				// TODO 0.5�ΤȤ����1/arity[�κ�����] �ˤ���ȿ�ư���ʤ���
				//                      1/(2 * arity[�κ�����])���Ȥʤ��褤��
				// [�κ�����]��ä��Ƥ�褤��
				// ���ξ�硢����ƥ������ȥ�μ��̤Τ褦�ʤ�ΤȤ��Ʒ׻������
				double ddx = 0.05 * f * edge.getVx()/l;
				double ddy = 0.05 * f * edge.getVy()/l;
				
				edge.from.setMoveDelta(ddx, ddy);
				edge.to.setMoveDelta(-ddx, -ddy);
			}
		}
	}
	
	public void calculateEdge2(Node me){
		double dx = 0;
		double dy = 0;
		
//			System.out.println(i+" "+me.getName());
		
		// angle �ǥ�����
		Edge ie[] = new Edge[me.getEdgeCount()];
//			System.out.println("arround "+me+"  "+me.getEdgeCount());
		for(int j=0;j<ie.length;j++) {
			ie[j]=new Edge(me, me.getNthNode(j));
//				System.out.println(ie[j]);
		}
		Arrays.sort(ie);
		
		for (int j=0;j<ie.length;j++) {
			Edge edge = ie[j];
			
			
			// cur.to �ˤ������Ϥ�׻�����
			{
				boolean f1 = false;
				Edge cur = ie[j];
//				System.out.println(cur);
				Node you = cur.to;
				
				Edge prev = null;
				Edge next = null;
				if(you.getEdgeCount() == 1){
					prev = ie[(j-1+ie.length)%ie.length];
					next = ie[(j+1)%ie.length];
				} else {
					int tmp = 1;
					f1 = true;
					
					while(prev == null){
						if(ie[(j-tmp+ie.length)%ie.length].to.getEdgeCount()!=1){
							prev = ie[(j-tmp+ie.length)%ie.length];
						}
						tmp++;
					}
					tmp = 1;
					while(next == null){
						if(ie[(j+tmp+ie.length)%ie.length].to.getEdgeCount()!=1){
							next = ie[(j+tmp+ie.length)%ie.length];
						}
						tmp++;
					}
					//if(prev == next)continue;
				}
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

				dx = dx * 2;
				dy = dy *2;
				me.setMoveDelta(-dx,-dy);
				you.setMoveDelta(dx,dy);
			}
		
		}
	}
	/**
	 * ���ȥ॰�롼�פ�����濴�˸������Ϥ򤫤��롣
	 */
	public void attractToCenter(AtomGroup atomgroup,AbstractMembrane m){
		Node n;
		double d;
		double dx = atomgroup.getCenterPos().x;
		double dy = atomgroup.getCenterPos().y;
		
		//���롼�פ�����˴ޤޤ�륢�ȥ��ޤ���������ʤ�
		if(doesCutAcrossMembrane(atomgroup,m))return;
		
		if(m == rootMem){
			dx -= 400;dy -= 300;
			//dx -= area.getCenterX();dy -= area.getCenterY();
		} else {
			dx -= m.rect.getCenterX();dy -= m.rect.getCenterY();
		}
		d = Math.sqrt(dx*dx + dy*dy);
		
		//�����濴�ˤ���Ȥ����Ϥ򤫤��ʤ�
		if(d >= 10){
			dx = 1.0*dx/d;
			dy = 1.0*dy/d;
		} else {
			dx = 0;
			dy = 0;
		}
		setMoveDelta(atomgroup,-dx,-dy);
	}
	
	public void attractToCenter(AbstractMembrane mem,AbstractMembrane m){
		double mx,my,d,dx,dy;
		dx = 0;dy = 0;
		mx = mem.rect.getCenterX();
		my = mem.rect.getCenterY();
		
		if(m == rootMem){
			mx -= 400;my -= 300;
			//mx -= area.getCenterX();my -= area.getCenterY();
		} else {
			mx -= m.rect.getCenterX();my -= m.rect.getCenterY();
		}
		d = Math.sqrt(mx*mx + my*my);
		
		//�����濴�ˤ���Ȥ����Ϥ򤫤��ʤ�
		if(d >= 10){
			dx -= 1.0*mx/d;
			dy -= 1.0*my/d;
		}

		setMoveDelta(mem,dx,dy);
	}
	/**
	 * ���ȥ॰�롼�פ˻����������褦���Ϥ򤫤��롣
	 */
	public void upForceAtomGroup(AtomGroup atomgroup,AbstractMembrane m,Object[] mems){
		double length =  AtomGroup.margin * 1.5;
		double miny = 0,dx = 0,dy = 0;
		if(mems.length == 0){
			return;
		} else {
			miny = ((AbstractMembrane)mems[0]).rect.getMinY();
		}
		for(int i = 1;i < mems.length;i++){
			if(miny >((AbstractMembrane)mems[i]).rect.getMinY()){
				miny = ((AbstractMembrane)mems[i]).rect.getMinY();
			}
		}
		
		if(doesCutAcrossMembrane(atomgroup,m))return;
		
		if(atomgroup.getMaxPos().y  > miny - AtomGroup.margin - length){
			dy += force(miny,atomgroup.getMaxPos().y);
		}

		setMoveDelta(atomgroup,dx,-dy);
	}
	
	/**
	 *�� m �˴ޤޤ�륢�ȥ�����γ��ˤϤ��� 
	 */
	public void repulseAtomFromMembrane(Node me,Object[] mems){
		for(int j = 0;j < mems.length;j++){
			if(!((AbstractMembrane)mems[j]).rect.isEmpty()&&
				((AbstractMembrane)mems[j]).rect.contains(me.getPosition().x,me.getPosition().y)){
				double d,dx,dy,f;
				
				DoublePoint pos = me.getPosition();
				dx = pos.x;dy = pos.y;
				pos = new DoublePoint(((AbstractMembrane)mems[j]).rect.getCenterX(),((AbstractMembrane)mems[j]).rect.getCenterY());
				dx -= pos.x;dy -= pos.y;
				d = Math.sqrt(dx*dx + dy*dy);
				f = repulsiveForce(me,(AbstractMembrane)mems[j]);
				if(d >= 3){
					dx = f*dx/d;
					dy = f*dy/d;
				} else {
					dx = f/2;
					dy = f/2;
				}
				
				me.setMoveDelta(dx,dy);
			}
		}
	}
	
	/**
	 * ���ȥ॰�롼��Ʊ�Τ��ŤʤäƤ���������Ϥ򤫤��롣
	 * @param atomgroup1
	 * @param atomgroup2
	 */
	public void repulsiveForceBetweenAtomgroups(AtomGroup atomgroup1,AtomGroup atomgroup2){
		if(atomgroup1.doesOverLap(atomgroup2)){
			double dx,dy,d,f;
			Node n;
			
			f = atomgroup1.repulsiveForce(atomgroup2);
			DoublePoint pos = atomgroup1.getCenterPos();
			dx = pos.x;dy = pos.y;
			pos = atomgroup2.getCenterPos();
			dx -= pos.x;dy -= pos.y;
			d = Math.sqrt(dx*dx + dy*dy);
			
			if(d >= 3){
				dx = f*dx/d;
				dy = f*dy/d;
			} else {
				dx = f/2;
				dy = f/2;
			}
			
			setMoveDelta(atomgroup1,dx,dy);
			setMoveDelta(atomgroup2,-dx,-dy);

		}
	}
	
	/**
	 * ����Ʊ�Τ��ŤʤäƤ���������Ϥ򤫤��롣
	 */
	public void repulsiveForceBetweenMems(AbstractMembrane mem1,AbstractMembrane mem2){
		double f,mx,my,dx,dy,d;
		dx = 0;dy = 0;
		
		f = repulsiveForce(mem1,mem2);
		mx = mem1.rect.getCenterX();my = mem1.rect.getCenterY();
		mx -= mem2.rect.getCenterX();my -= mem2.rect.getCenterY();
		d = Math.sqrt(mx*mx + my*my);
		if(d >= 1){
			dx += f*mx/d;
			dy += f*my/d;
		} else {
			dx += f/2;
			dy += f/2;
		}

		setMoveDelta(mem1,dx,dy);
		setMoveDelta(mem2,-dx,-dy);
	}
	/**
	 * �Ŀ���¿�����ȥ॰�롼�פ���ˤʤ�褦���Ϥ򤫤��롣
	 */
	public void sortAtomGroupBySize(AtomGroup atomgroup1,AtomGroup atomgroup2,AbstractMembrane m){

		boolean fl1 = false;boolean fl2 = false;
		double dx,dy,f1,f2,l,d,margin;
		int s1,s2;
		
		Node n;
		dx = 0;dy = 0;l = 30;margin = 0;
		f1 = 1.6;f2 = 6.0;
		s1 = atomgroup1.atoms.size();
		s2 = atomgroup2.atoms.size();
		
		//�ɤ��餫�Υ��롼�פ������Ķ�����󥯤�ޤ�Ȥ��Ͻ������ʤ���
		if(doesCutAcrossMembrane(atomgroup1,m) || doesCutAcrossMembrane(atomgroup2,m))
			return;
		
		if(s1 == 1){
			if(s2 != 1){
				fl2 = true;
			}
		} else if(s1 == 2){
			if(s2 == 1){
				fl1 = true;
			} else if(s2 > 2){
				fl2 = true;
			}
		} else if(s1 <= 4){
			if(s2 <= 2){
				fl1 = true;
			} else if(s2 > 4){
				fl2 = true;
			}
		} else if(s1 >= 5){
			if(s2 < 5){
				fl1 = true;
			}
		}
		if(fl1){
			if(atomgroup1.getMaxPos().y + l  > atomgroup2.getMinPos().y){
				d = atomgroup1.getMaxPos().y - atomgroup2.getMinPos().y;
				d = f1 - (d+ margin*15)/15;
				if(d <= 0)dy = -1 * f2;
				else dy =  -1 * f1 / d;
			}
		} else 	if(fl2){
			if(atomgroup2.getMaxPos().y  + l > atomgroup1.getMinPos().y){
				d = atomgroup2.getMaxPos().y - atomgroup1.getMinPos().y;
				d = f1 - (d + margin*15)/15;
				if(d <= 0)dy = f2;
				else dy =  f1 / d;
			}
		}
		
		if(dy != 0){
			setMoveDelta(atomgroup1,dx,dy);
			setMoveDelta(atomgroup2,-dx,-dy);
			}
		
	}
	
	/**
	 * ����˻��ꤷ��Y��ɸ��겼¦�����褦���Ϥ򤫤��롣
	 */
	public void downForceMems(AbstractMembrane mem,double maxy,double length){
		double dy = 0;
		if(mem.rect.getMinY()  < maxy + AtomGroup.margin + length){
			dy += force(mem.rect.getMinY(),maxy);
		}
		setMoveDelta(mem,0,dy);
	}
	
	/**
	 * �໤�Ϥ򤫤��롣
	 * ������夤�Ϥ�0�ˤ��������궯���Ϥ򾮤������롣
	 */
	 public void frictionalForce(Node me){
		double masaturyoku = 0.9;
		double maxpower = 10;
		double tempd;

		if(me==((LMNGraphPanel)parent).movingNode) return;
		DoublePoint p = me.getMoveDelta();
		tempd = Math.sqrt(p.x*p.x + p.y*p.y);
		if(tempd < masaturyoku){
			me.initMoveDelta();
//		}
		}else if(tempd > maxpower){
			me.initMoveDelta();
//			System.out.println("maxpowerrrrr");
//			System.out.println("p.x "+p.x+":p.y "+p.y);
//			System.out.println("x*m/t "+ (p.x*(maxpower/tempd)) + ":y*m/t "+ (p.y*(maxpower/tempd)));
//			System.out.println("tempd "+ tempd + ":d2 " + (Math.sqrt(p.x*(maxpower/tempd)*p.x*(maxpower/tempd) + p.y*(maxpower/tempd)*p.y*(maxpower/tempd))) );
			me.setMoveDelta(p.x*(maxpower/tempd),p.y*(maxpower/tempd));
		}else {
//			System.out.println("notmaxpowerrrrr");
//			System.out.println("tempd "+ tempd);
		//	me.setMoveDelta(p.x*(maxpower/tempd),p.y*(maxpower/tempd));
			
			
		}
	 }
	 
	 public void initGraphDialog(JFrame frame){
	 	dialog = new GraphDialog(frame);
	 	int length = 7;
	 	boolean flags[] = new boolean[length];
	 	flags[0] = true;flags[1] = false;flags[2] = true;flags[3] = false;
	 	flags[4] = false;flags[5] = true;flags[6] = false;
//	 	flags[0] = true;flags[1] = false;flags[2] = false;flags[3] = true;
//	 	flags[4] = true;flags[5] = false;flags[6] = false;flags[7] = false;
//	 	flags[8] = true;flags[9] = true;flags[10] = false;
	 	
	 	String names[] = new String[length];
	 	for(int i = 0;i<length;i++){
	 		names[i] = Integer.toString(i);
	 	}
	 	
	 	names[0] = "�դ�Ĺ���ȳ��٤����ˤ���";
	 	names[1] = "�濴���Ϥ򤫤���";
	 	names[2] = "���ȥ���줬�Ťʤ�ʤ��褦�ˤ���";
	 	names[3] = "���ȥ����¿������դ�������";
	 	names[4] = "���¾�Υ��ȥ��겼�����֤���";
	 	names[5] = "�໤�Ϥ򤫤���";
	 	names[6] = "��ʿ�ˤʤ�褦�˲�ž����";

//	 	names[0] = "�դ�Ĺ���ȳ��٤����ˤ���";
//	 	names[1] = "���ȥ������濴���Ϥ򤫤���";
//	 	names[2] = "���ȥ�������������";
//	 	names[3] = "���ȥब����˽Ťʤ�ʤ��褦�ˤ���";
//	 	names[4] = "���ȥ�Υ����Ʊ�Τ��Ťʤ�ʤ�";
//	 	names[5] = "���ȥ����¿������դ�������";
//	 	names[6] = "�����¾�Υ��ȥ��겼������";
//	 	names[7] = "��˿�����濴���Ϥ򤫤���";
//	 	names[8] = "��Ʊ�Τ��Ťʤ�ʤ��褦�ˤ���";
//	 	names[9] = "�໤�Ϥ򤫤���";
//	 	names[10] = "��ʿ�ˤʤ�褦�˲�ž����";
	 	dialog.setLength(length);
	 	dialog.setFlags(flags);
	 	dialog.setNames(names);
	 	dialog.initButtons();
	 }
	 public void showGraphDialog(){
	 	dialog.setVisible(true);
	 }
	 private boolean getFlag(int i){
	 	return dialog.flags[i];
	 }
}

