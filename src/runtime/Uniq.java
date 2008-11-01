package runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Uniq {
	/**
	 * ���դ��ץ���ʸ̮��«�����줿��¤�ؤΥ�󥯤���¸��������
	 * ���Ǥ� Link[] ��
	 */
	public ArrayList<Link[]> history = new ArrayList<Link[]>();
	public HashSet<String> historyH = new HashSet<String>();
	
	/**
	 * ���դ��ץ���ʸ̮��«�����줿��¤�μ��Τ���¸�����졣������==null
	 */
	public Membrane mem = new Membrane();
	
	/**
	 * el �� history �˴ޤޤ�뤫�ɤ����������롣
	 * �ޤޤ��ʤ� false ���֤�
	 * �ޤޤ�ʤ���п������ɲä��� true ���֤�
	 * @param el
	 * @return
	 */
	public boolean checkOld(Link[] el) {
		// ����ܤ����� O(N)
		for(int i=0;i<history.size();i++) {
			int NG=0;
			for(int j=0;j<el.length;j++) {
				Link[] aH = history.get(i);
				if(aH[j].eqGround(el[j])) NG++;
			}
			if(NG==el.length) return false;
		}
		// ���ԡ������������¸
		for(int i=0;i<el.length;i++) {
			el[i] = mem.copyGroundFrom(el[i]);
			// ���Σ��ԤϤʤ��Ƥ�ư�������������ä���� dump ���褦�Ȥ����ɬ�פˤʤ롣
			Atom dummy = mem.newAtom(new SymbolFunctor("hist_"+history.size()+"_"+i, 1));
			dummy.args[0] = new Link(dummy, 0);
			mem.unifyLinkBuddies(dummy.getArg(0), el[i]);
		}
		history.add(el);
//		Env.p("MEM>> "+Dumper.dump(mem, false));
		return true;
	}
	/**
	 * el �� history �˴ޤޤ�뤫�ɤ����������롣
	 * �ޤޤ��ʤ� false ���֤�
	 * �ޤޤ�ʤ���п������ɲä��� true ���֤�
	 * 
	 * (2006/09/29 kudo)
	 * 2�����ʾ���б���2�����ʾ�ʤΤǡ�el�ϥ�󥯤�����ǤϤʤ�����󥯥ꥹ�Ȥ�����ˤ�����
	 * 
	 * @param el
	 * @return
	 */
//	public boolean check(Link[] el) {
//		// O(1) �ΤϤ�
//		StringBuffer cur=new StringBuffer();
//		for(int j=0;j<el.length;j++) {
//			cur.append(el[j].groundString());
//			cur.append(":");
//		}
//		String curI = cur.toString();
////		System.out.println(historyH);
//		if(historyH.contains(curI)) return false;
//		historyH.add(curI);
//		return true;
//	}
	public boolean check(List<Link>[] el) {
		// O(1) �ΤϤ�
		StringBuffer cur=new StringBuffer();
		for(int j=0;j<el.length;j++) {
			cur.append(Link.groundString(el[j]));
			cur.append(":");
		}
		String curI = cur.toString();
//		System.out.println(historyH);
		if(historyH.contains(curI)) return false;
		historyH.add(curI);
		return true;
	}

}
