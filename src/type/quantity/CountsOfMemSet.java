package type.quantity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import runtime.Env;

import compile.structure.Membrane;

/**
 * ��̾���Ȥ���Ū���Ϸ�̤��ݻ�����
 * @author kudo
 *
 */
public class CountsOfMemSet {
	/** ����������� -> �̲��Ϸ�� */
	Map<Membrane,CountsOfMem> memToCounts;
	/** ����������� -> �̲��Ϸ��(ɾ���Ѥ� */
	Map<Membrane,FixedCounts> memToFixedCounts;
//	/** ��̾ -> �Ѳ��ץ��� */
//	Map<Membrane,CountsOfMem> memToInhCounts;
	public CountsOfMemSet(){
//		memToGenCounts = new HashMap<Membrane,CountsOfMem>();
//		memToInhCounts = new HashMap<Membrane,CountsOfMem>();
		memToCounts = new HashMap<Membrane, CountsOfMem>();
	}
	/**
	 * ��ˤĤ��Ƥβ��Ϸ�̤�ޡ������Ƥ���
	 * TODO addCountsOfMem�Ϥ����������Ǥ���(���֤�)
	 * @param counts
	 */
	public void add(CountsOfMem counts){
		switch(counts.multiple){
		case 0:// ����
		case 1:// ����
			addCountsOfMem(counts);
			break;
		default://�ޡ���
			addMerCounts(counts);
		}
	}
	/**
	 * ��β��Ϸ�̤�ä��Ƥ�����
	 * �����ʳ��Ǥϡ����Ʊ̾�Ǥ⥽��������̤���ʤ���̤���롣
	 * ���������롼�������ϡ��롼��ν�°�������Ʊ���Ȥ���롣
	 * �������������Ϸ�̤�Ʊ����ˤĤ��Ƥϲû�����롣
	 * @param counts
	 */
	public void addCountsOfMem(CountsOfMem counts){
//		String memname = TypeEnv.getMemName(counts.mem);
		if(!memToCounts.containsKey(counts.mem))
			memToCounts.put(counts.mem,counts);
		else{
			CountsOfMem oldcounts = memToCounts.get(counts.mem);
			oldcounts.addAllCounts(counts);
		}
	}
	/**
	 * 
	 * @param counts
	 */
	public void addMerCounts(CountsOfMem counts){
		
	}
	
	/**
	 * ��̾���Ȥ˥ޡ�������
	 */
	public void mergeForName(){
		Map<String, FixedCounts> memnameToCounts = new HashMap<String, FixedCounts>();
		Iterator<Membrane> itm = memToFixedCounts.keySet().iterator();
		while(itm.hasNext()){
			Membrane m = itm.next();
			String memname = m.name;
			FixedCounts fc = memToFixedCounts.get(m);
			if(!memnameToCounts.containsKey(memname))
				memnameToCounts.put(memname,fc);
			else{
				FixedCounts oldfc = memnameToCounts.get(memname);
				oldfc.merge(fc);
			}
		}
	}
	
	/**
	 * �Ʋ��Ϸ�̤���������
	 * (-RV1+RV1 -> 0 �ʤ�)
	 */
	public void reflesh(){
		Iterator<CountsOfMem> itms = memToCounts.values().iterator();
		while(itms.hasNext()){
			itms.next().reflesh();
		}
	}
	
	public void solve(){
		memToFixedCounts = new HashMap<Membrane, FixedCounts>();
		Iterator<Membrane> itm = memToCounts.keySet().iterator();
		while(itm.hasNext()){
			Membrane m = itm.next();
			memToFixedCounts.put(m,memToCounts.get(m).solve());
		}
	}
	
	public void printAll(){
		Env.p("--QUANTITY ANALYSIS");
		Env.p("---mem on source counts:");
		Iterator<FixedCounts> itfc = memToFixedCounts.values().iterator();
		while(itfc.hasNext()){
			itfc.next().print();
		}
//		Env.p("---inh counts:");
//		Iterator<CountsOfMem> itmis = memToInhCounts.values().iterator();
//		while(itmis.hasNext()){
//			itmis.next().print();
//		}
		Env.p("");
	}
	
}
