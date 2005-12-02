package runtime;

import java.util.ArrayList;

import runtime.AbstractMembrane;
import runtime.Membrane;

public class Uniq {
	/**
	 * ���դ��ץ���ʸ̮��«�����줿��¤�ؤΥ�󥯤���¸��������
	 * ���Ǥ� Link[] ��
	 */
	public ArrayList history = new ArrayList();
	
	/**
	 * ���դ��ץ���ʸ̮��«�����줿��¤�μ��֤���¸�����졣������==null
	 */
	public AbstractMembrane mem = new Membrane();
	
	/**
	 * el �� history �˴ޤޤ�뤫�ɤ����������롣
	 * �ޤޤ��ʤ� false ���֤�
	 * �ޤޤ�ʤ���п������ɲä��� true ���֤�
	 * @param el
	 * @return
	 */
	public boolean check(Link[] el) {
		for(int i=0;i<history.size();i++) {
			int NG=0;
			for(int j=0;j<el.length;j++) {
				Link[] aH = (Link[])history.get(i);
				if(aH[j].eqGround(el[j])) NG++;
			}
			if(NG==el.length) return false;
		}
		// ���ԡ������������¸
		for(int i=0;i<el.length;i++) {
			el[i] = mem.copyGroundFrom(el[i]);
			// ���Σ��ԤϤʤ��Ƥ�ư�������������ä���� dump ���褦�Ȥ����ɬ�פˤʤ롣
			Atom dummy = mem.newAtom(new Functor("hist_"+history.size()+"_"+i, 1));
			dummy.args[0] = new Link(dummy, 0);
			mem.unifyLinkBuddies(dummy.getArg(0), el[i]);
		}
		history.add(el);
//		Env.p("MEM>> "+Dumper.dump(mem, false));
		return true;
	}
}
