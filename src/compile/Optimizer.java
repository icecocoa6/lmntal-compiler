/*
 * ������: 2003/11/30
 */
package compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;

import runtime.Env;
import runtime.Instruction;
import runtime.InstructionList;
import runtime.Rule;
import runtime.functor.Functor;
import runtime.functor.SpecialFunctor;

/**
 * ��Ŭ����Ԥ����饹�᥽�åɤ���ĥ��饹��
 * @author Mizuno
 */
public class Optimizer {
	/** ̿����Υ���饤�˥󥰤�Ԥ�*/
	public static boolean fInlining;
	/** ��κ����Ѥ�Ԥ� */
	public static boolean fReuseMem;
	/** ���ȥ�κ����Ѥ�Ԥ� */
	public static boolean fReuseAtom;
	/** ̿����Υ롼�ײ���Ԥ� */
	public static boolean fLoop;
	/** ̿������¤��ؤ���Ԥ� */
	public static boolean fGuardMove;
	/** ̿����Υ��롼�ײ���Ԥ� */
	public static boolean fGrouping;
	/** ̿������Ԥ߾夲��Ԥ� */
	public static boolean fMerging;
	/** �����ƥ�롼�륻�åȤΥ���饤��Ÿ�� */
	public static boolean fSystemRulesetsInlining;

	/**
	 * ���Ƥκ�Ŭ���ե饰�򥪥դˤ���
	 */
	public static void clearFlag() {
		fInlining = fReuseMem = fReuseAtom = fLoop = false;
		fGuardMove = fGrouping = fMerging = fSystemRulesetsInlining = false;
	}
	/**
	 * ��Ŭ����٥�����ꤹ�롣
	 * ��٥�˱����ƺ�Ŭ���ե饰�򥪥�ˤ��롣
	 * �㤤��٥����ꤷ�Ƥ⡢���Ǥ˥���ˤ��Ƥ���ե饰�򥪥դˤ��뤳�ȤϤʤ���
	 * ��������Ŭ�����������ݤˤϡ��ե饰��ե�����ɤ�������ơ������ǥ��󡦥��դ�Ԥ���
	 * @param level ��Ŭ����٥�
	 */
	public static void setLevel(int level) {
		if (level >= 1) {
			fReuseAtom = fReuseMem = true;
			fGuardMove = true;
		}
		if (level >= 2) {
//			�롼�ײ��Ϥޤ��Х�������Τǡ����̤˻��ꤷ�ʤ��¤�¹Ԥ��ʤ�
//			fLoop = true;
//			fGrouping = true;
		}
		if (level >= 3) {
			//����Ū�˻��ꤹ��ȡ��ܥǥ��⤯�äĤ���
			//���٤�����ʤ�����
			fInlining = true;
		}
	}

	/** �롼�륪�֥������Ȥ��Ŭ������
	 * ̿������Ф����Ŭ����ǽ���ɲä�����ϡ��������Ŭ���᥽�åɤθƤӽФ����ˤ���Ȥ�����
	 * 
	 *  @param rule �롼�륪�֥������� 
	 */
	public static void optimizeRule(Rule rule) {
		// TODO ��Ŭ��������礹��
		Compactor.compactRule(rule);
		// TODO �ܼ�Ū�˥���饤��Ÿ����ɬ�פʤ���Τϡ�Ÿ�����ʤ��Ƥ�Ǥ���褦�ˤ���
		if (fInlining || fGuardMove || fGrouping || fReuseMem || fReuseAtom || fLoop) {
			//head �� guard �򤯤äĤ���
			inlineExpandTailJump(rule.memMatch);
			//�����Ǥϥ��ȥ��Ƴ�ƥ��ȤΥ���饤��Ÿ���ˤ��б����Ƥ��ʤ� -> ����б�(sakurai)
			inlineExpandTailJump(rule.atomMatch);
			rule.guardLabel = null;
			rule.guard = null;
		}
		if(Env.findatom2)
			optimize(rule.tempMatch, rule.body);
		else
			optimize(rule.memMatch, rule.body);
		if(fGuardMove && !fMerging) {
			guardMove(rule.atomMatch);
			guardMove(rule.memMatch);
			allocMove(rule.atomMatch);
			allocMove(rule.memMatch);
		}
		if(Env.findatom2)return ;
		if(fGrouping && !fMerging) {
			Grouping g = new Grouping();
			g.grouping(rule.atomMatch, rule.memMatch);
		} 
		if (Env.hyperLinkOpt) findproccxtMove(rule.memMatch);//seiji
		if(fSystemRulesetsInlining) inlineExpandSystemRuleSets(rule.body);
		if (fInlining) {
			// head(+guard) �� body �򤯤äĤ���
			inlineExpandTailJump(rule.memMatch);
			inlineExpandTailJump(rule.atomMatch);

			rule.bodyLabel = null; 
			rule.body = null;
		}
	}
	/**	
	 * �Ϥ��줿̿����򡢸��ߤκ�Ŭ����٥�˱����ƺ�Ŭ�����롣<br>
	 * ̿������ˤϡ�1������removeatom/removemem̿�᤬����Ƥ��ƤϤ����ʤ���
	 * ���ߤΰ������ͤϻ���Ū�ʤ�Τǡ������ѹ������ͽ�ꡣ
	 * @param head ���Ƴ�ޥå���̿����
	 * @param body �ܥǥ�̿����
	 */
	public static void optimize(List<Instruction> head, List<Instruction> body) {
		if (fReuseMem) {
			reuseMem(head, body);
		}
		if (fReuseAtom) {
			if (changeOrder(body)) {
				reuseAtom2(head, body);
				removeUnnecessaryRelink(body);
			}
		}
		if (fLoop) {
			makeLoop(head, body);
		}
	}
	///////////////////////////////////////////////////////
	// @author n-kato
	// TODO spec̿��ο�ʬ��ͤ���

	/** ̿�����������jump̿��򥤥�饤��Ÿ�����롣
	 * @param insts ̿����
	 * <pre>
	 *     [ spec[X,Y];  C;jump[L,A1..Am] ] where L:[spec[m,m+n];D]
	 * ==> [ spec[X,Y+n];C; D{ 1..m->A1..Am, m+1..m+n->Y+1..Y+n } ]
	 * </pre> */
	public static void inlineExpandTailJump(List<Instruction> insts) {
		if (insts.isEmpty()) return;
		Instruction spec = insts.get(0);
		if (spec.getKind() != Instruction.SPEC) return;
		//���ȥ��Ƴ�ƥ�����
		for(int i = 1; i<insts.size(); i++){
			Instruction branch = insts.get(i);
			if (branch.getKind() != Instruction.BRANCH) break;
			InstructionList label = (InstructionList)branch.getArg1();
			inlineExpandTailJump(label.insts);
		}			

		int formals = spec.getIntArg1();
		int locals  = spec.getIntArg2();
		locals = inlineExpandTailJump(insts, locals);
		spec.updateSpec(formals, locals);
	}
	/** ̿�����������jump̿��򥤥�饤��Ÿ�����롣spec�Ϥޤ���������ʤ���
	 *  @param insts ̿����
	 *  @param varcount Ÿ�����μ°���
	 *  @return Ÿ����μ°���
	 *  */
	public static int inlineExpandTailJump(List<Instruction> insts, int varcount) {
		if (insts.isEmpty()) return varcount;
		int size = insts.size();
		Instruction jump = insts.get(size - 1);
		if (jump.getKind() != Instruction.JUMP) return varcount;
		//
		InstructionList label = (InstructionList)jump.getArg1();
		List<Instruction> subinsts = InstructionList.cloneInstructions(label.insts);
		Instruction subspec = subinsts.get(0);

		HashMap map = new HashMap();
		// �������ϡ��°����ֹ���ִ����롣
		List memargs   = (List)jump.getArg2();
		List atomargs  = (List)jump.getArg3();
		List otherargs = (List)jump.getArg4();
		for (int i = 0; i < memargs.size(); i++)
			map.put( new Integer(i), memargs.get(i) );
		for (int i = 0; i < atomargs.size(); i++)
			map.put( new Integer(memargs.size() + i), atomargs.get(i) );
		for (int i = 0; i < otherargs.size(); i++)
			map.put( new Integer(memargs.size() + atomargs.size() + i), otherargs.get(i) );
		// �ɽ��ѿ��ϡ��������ѿ��ֹ���ִ����롣
		int subformals = subspec.getIntArg1();
		int sublocals  = subspec.getIntArg2();
		for (int i = subformals; i < sublocals; i++) {
			map.put( new Integer(i), new Integer(varcount++) );
		}
		//
		Instruction.applyVarRewriteMap(subinsts,map);
		subinsts.remove(0);		// spec�����
		insts.remove(size - 1);	// jump̿������
		insts.addAll(subinsts);
		return varcount;
	}
	// n-kato ��

	//sakurai
	/** 
	 * ������̿����ǽ�ʸ¤����˰�ư������.
	 * �ܥǥ�̿����¤��ؤ��ʤ�
	 * @param insts ̿����(head��guard�򤯤äĤ������)
	 */
	public static void guardMove(List<Instruction> insts){
		for(int i=1; i<insts.size(); i++){
			Instruction inst = insts.get(i);

			switch(inst.getKind()){
			//�ܥǥ�̿������¤��ؤ��ʤ� -> �ܥǥ�����Ƭ��commit
			case Instruction.COMMIT:
				return;
				//�����������(���֤��Ѥ��ʤ�)
				//todo �ɤ����뤫�ͤ���
			case Instruction.NOT:
				continue;
				//���֤��Ѥ������ʤ�̿�ᡣ¾�ˤ���Ф������ɲä��롣
			case Instruction.FINDATOM:
			case Instruction.ANYMEM:
			case Instruction.NEWLIST:
			case Instruction.PROCEED:
			case Instruction.JUMP:
			case Instruction.RESETVARS:
			case Instruction.UNIQ:
			case Instruction.NOT_UNIQ:
			case Instruction.GUARD_INLINE:
			case Instruction.ADDTOLIST:
				continue;
				//������̿��������̿��
			case Instruction.GROUP:
			case Instruction.BRANCH:
				InstructionList subinsts = (InstructionList)inst.getArg1();
				guardMove(subinsts.insts);
				break;
				//��˳������ʤ�̿��ϡ����ΰ������ѿ��ֹ椬������줿̿�������ˤʤ�ʤ��¤ꡢ
				//����ư�����롣
			default:
				int judge = guardMove(insts, inst, i-1);
			if(judge == 2){
//				System.out.println("remove2\t"+insts.get(i));
				insts.remove(i);
				i--;
			} else if (judge == 1){
//				System.out.println("remove1\t"+insts.get(i+1));
				insts.remove(i+1);
			} 
			}
		}
	}
	/* ���̿��findproccxt��findatom�������˰�ư������ */
	private static void findproccxtMove(List<Instruction> insts) {//seiji
		int insert = 0, max = insts.size();
		for (int i = 0; i < max; i++) {
			if (insts.get(i).getKind() == Instruction.FINDATOM) {
				insert = i;
				break;
			}
		}
		for (int i = 0; i < max; i++) {
			if (insts.get(i).getKind() == Instruction.FINDPROCCXT) {
				Instruction ins = insts.get(i);
				insts.remove(i);
				insts.add(insert, ins);
			}
		}
	}
	
	private static int max(int m, int n){
		if(m>n)
			return m;
		else
			return n;
	}
	private static int guardMove(List<Instruction> insts, Instruction inst, int locate){
		int moveok = 0; //��ư��ǽȽ��ե饰
		ArrayList list;
		HashMap listn = new HashMap();
		int i=locate;
		int insert_index = 0;
		ff:
			for(; i>=0; i--){
				list = inst.getVarArgs(listn);
				Instruction inst2 = insts.get(i);
				if(inst2.getKind() == Instruction.GROUP) {
//					System.out.println("GROUP");
					InstructionList subinsts = (InstructionList)inst2.getArg1();
					moveok = max(moveok, guardMove(subinsts.insts, inst, subinsts.insts.size()-1));
					if(moveok > 0){
						moveok = 2;
						i=0;
						continue;
					}
//					System.out.println(moveok);
					continue;
				} else if(inst2.getKind() == Instruction.BRANCH) {
//					System.out.println("BRANCH");
					InstructionList subinsts = (InstructionList)inst2.getArg1();
					Instruction instrep  = (Instruction)inst.clone();
					moveok = max(moveok, guardMove(subinsts.insts, inst, subinsts.insts.size()-1));
					if(moveok > 0){
						moveok = 2;
					}
					inst = instrep;
//					System.out.println(moveok);
					continue;
				} else if(inst2.getKind() == Instruction.RESETVARS){
//					System.out.println("RESETVARS");
					int memnum = ((List)inst2.getArg1()).size();
					ArrayList mems = (ArrayList)inst2.getArg1();
					ArrayList atoms = (ArrayList)inst2.getArg2();

//					System.out.print(inst + "\t to ");
					for(int j=0; j<list.size(); j++){
						int num =((Integer)list.get(j)).intValue();
//						System.out.println("j=" + j +", atoms = "+atoms + ", num = "+num+ " ,memnum = " + memnum);
						//getVarArgs�Τ�����j���ܤ����ꤹ�٤�
						if(num<memnum){
							inst.data.set(((Integer)listn.get(j)).intValue(),  new Integer(((Integer)mems.get(num)).intValue()));
						} else if((num-memnum)>=atoms.size() || num-memnum<0) {
//							System.out.println("atom = "+atoms + ", num = "+num+ " ,memnum = " + memnum);
							continue;
						} else {
							inst.data.set(((Integer)listn.get(j)).intValue(),  new Integer(((Integer)atoms.get(num - memnum)).intValue()));
						}
//						System.out.println("set " + new Integer(((Integer)atoms.get(num - memnum)).intValue()) );
//						System.out.println(inst.getArg(j+1));
//						inst.setArg(j+1, new Integer(((Integer)atoms.get(num - memnum)).intValue()));
					}
//					System.out.println(inst);
//					System.out.println(inst2 + "\t" + memnum);
					continue;
				}

				//��inst��inst2�β������֤���٤���Ƚ��
				ArrayList list2 = inst2.getVarArgs(listn);
				if(inst.getKind() == Instruction.ALLOCATOM || inst.getKind() == Instruction.NEWLIST){
//					System.out.println("check " + inst + "\t to" + inst2);
					if(list2.contains(inst.getArg1())){
//						System.out.println("match2 " + inst);
						moveok = max(moveok, 1);
						insert_index = i;
//						i--;
//						break ff;
					}
				}
				else {
					for(int j=0; j<list.size(); j++){
						if(inst2.getKind() == Instruction.ALLOCATOM || inst2.getKind() == Instruction.NEWLIST)
							continue;
						if(inst2.getOutputType() != -1){
							if(list.get(j).equals(inst2.getArg1())) {
//								System.out.println("match1 " + inst);
								moveok = max(moveok, 1);
								insert_index = i+1;
								break ff;
							}
//							System.out.println("unmatch1 " + list.get(j) + "neq" + inst2.getArg1() + inst);
						}
						else if(list2.contains(list.get(j))){
//							System.out.println("match2 " + inst);
							moveok = max(moveok, 1);
							insert_index = i+1;
							break ff;
						}
//						System.out.println("unmatch2 " + inst);
					}
//					if(!moveok) break; 
				}
			}
//		System.out.println(moveok);
		if(moveok > 0){
			if(insert_index != 0){
//				System.out.println("add\t" + inst + "to "+insert_index);
				insts.add(insert_index, inst);
			}
			return moveok;
		}
//		System.out.println("no\t" + inst);
		return 0;
	}

	/** 
	 * ������̿����ǽ�ʸ¤����˰�ư������.
	 * �ܥǥ�̿����¤��ؤ��ʤ�
	 * @param insts ̿����(head��guard�򤯤äĤ������)
	 */
	public static void allocMove(List<Instruction> insts){
		for(int i=1; i<insts.size(); i++){
			Instruction inst = insts.get(i);

			switch(inst.getKind()){
			case Instruction.ALLOCATOM:
			case Instruction.NEWLIST:
				int judge = guardMove(insts, inst, i-1);
				if(judge == 2){
//					System.out.println("remove2\t"+insts.get(i));
					insts.remove(i);
					i--;
				} else if (judge == 1){
//					System.out.println("remove1\t"+insts.get(i+1));
					insts.remove(i+1);
				}
				break;
			case Instruction.GROUP:
			case Instruction.BRANCH:
				InstructionList subinsts = (InstructionList)inst.getArg1();
				allocMove(subinsts.insts);
				break;
			default:
				continue;
			}
		}
	}

	/**
	 * �����ƥ�롼�륻�åȤ�ܥǥ�̿�������Ÿ������
	 * @param body �ܥǥ�̿����
	 */
	private static void inlineExpandSystemRuleSets(List<Instruction> body){
		Instruction spec = body.get(0);
		if(spec.getKind() != Instruction.SPEC) return;
		int locals = spec.getIntArg2();
		HashMap getlinkmap = new HashMap();
		HashSet<Instruction> removelinks = new HashSet<Instruction>();
		InstructionList inline1;
		InstructionList inline2;
		HashSet<Instruction> newlink = new HashSet<Instruction>();
		HashSet<Instruction> newlinks2;
		HashSet<Instruction> enqueueatoms;
		HashMap<Integer, Integer> old2new = new HashMap<Integer, Integer>();
		for(int i=1; i<body.size(); i++){
			inline1 = new InstructionList();
			inline2 = new InstructionList();
			newlinks2 = new HashSet<Instruction>();
			enqueueatoms = new HashSet<Instruction>();
			Instruction inst = body.get(i);
			if(inst.getKind() == Instruction.GETLINK){
				if(!getlinkmap.containsKey(inst.getArg1())) getlinkmap.put(inst.getArg1(), inst);
			}
			if(inst.getKind() == Instruction.NEWATOM ){
				int newlinkmem = 0;
				Functor func = (Functor)inst.getArg3();
				String funcname = func.getName();
				int funcarity = func.getArity();
				int newatomvar = inst.getIntArg1();
				int arg1, arg2, result, resultlink;
				int op, typecheck;
				op = 0; typecheck = 0;
				arg1 = arg2 = result = resultlink = -1;
				if(funcarity == 3 &&
						funcname.equals("+") || funcname.equals("-") || funcname.equals("*") || funcname.equals("/")
						|| funcname.equals("mod") || funcname.equals("+.") || funcname.equals("-.")
						|| funcname.equals("*.") || funcname.equals("/.")){
					if(funcname.equals("+")) {
						op = Instruction.IADD;
						typecheck = Instruction.ISINT;
					}
					else if(funcname.equals("-")) {
						op = Instruction.ISUB;
						typecheck = Instruction.ISINT;
					}
					else if(funcname.equals("*")) {
						op = Instruction.IMUL;
						typecheck = Instruction.ISINT;
					}
					else if(funcname.equals("/")) {
						op = Instruction.IDIV;
						typecheck = Instruction.ISINT;
					}
					else if(funcname.equals("mod")) {
						op = Instruction.IMOD;
						typecheck = Instruction.ISINT;
					}
					if(funcname.equals("+.")) {
						op = Instruction.FADD;
						typecheck = Instruction.ISFLOAT;
					}
					else if(funcname.equals("-.")) {
						op = Instruction.FSUB;
						typecheck = Instruction.ISFLOAT;
					}
					else if(funcname.equals("*.")) {
						op = Instruction.FMUL;
						typecheck = Instruction.ISFLOAT;
					}
					else if(funcname.equals("/.")) {
						op = Instruction.FDIV;
						typecheck = Instruction.ISFLOAT;
					}
					for(int j=i+1; j<body.size(); j++){
						Instruction inst2 = (Instruction)body.get(j);
						if(inst2.getKind() == Instruction.NEWLINK ){
							if(inst2.getIntArg1() == newatomvar){
								if(!newlink.contains(inst2)) {
									newlink.add(inst2);
									newlinks2.add(inst2);
								}
								if(!removelinks.contains(inst2)) removelinks.add(inst2);
								if(inst2.getIntArg2() == 2) {
									result = inst2.getIntArg3(); 
									resultlink = inst2.getIntArg4();
									newlinkmem = inst2.getIntArg5();
								}
								else {
									if(inst2.getIntArg2() == 0) arg1 = inst2.getIntArg3();
									else arg2 = inst2.getIntArg3(); 
								}
							}
							else if(inst2.getIntArg3() == newatomvar){
								if(!newlink.contains(inst2)) {
									newlink.add(inst2);
									newlinks2.add(inst2);
								}
								if(!removelinks.contains(inst2)) removelinks.add(inst2);
								if(inst2.getIntArg4() == 2) {
									result = inst2.getIntArg1(); 
									resultlink = inst2.getIntArg2();
									newlinkmem = inst2.getIntArg5();
								}
								else {
									if(inst2.getIntArg4() == 0) arg1 = inst2.getIntArg1();
									else arg2 = inst2.getIntArg1();
								}
							}
						}
						else if(inst2.getKind() == Instruction.RELINK){
							if(inst2.getIntArg1() == newatomvar){
								if(!newlink.contains(inst2)) {
									newlink.add(inst2);
									newlinks2.add(inst2);
								}
								if(!removelinks.contains(inst2)) removelinks.add(inst2);
								if(inst2.getIntArg2() == 2) {
									result = inst2.getIntArg3(); 
									resultlink = inst2.getIntArg4();
									newlinkmem = inst2.getIntArg5();
								}
								else {
									inline1.add(new Instruction(Instruction.DEREFATOM, locals, inst2.getIntArg3(), inst2.getIntArg4()));
									if(inst2.getIntArg2() == 0) arg1 = locals;
									else arg2 = locals; 
								}
								locals++;
							}
							else if(inst2.getIntArg3() == newatomvar){
								if(!newlink.contains(inst2)) {
									newlink.add(inst2);
									newlinks2.add(inst2);
								}
								if(!removelinks.contains(inst2)) removelinks.add(inst2);
								if(inst2.getIntArg4() == 2) {
									result = inst2.getIntArg1(); 
									resultlink = inst2.getIntArg2();
									newlinkmem = inst2.getIntArg5();
								}
								else {
									inline1.add(new Instruction(Instruction.DEREFATOM, locals, inst2.getIntArg3(), inst2.getIntArg4()));
									if(inst2.getIntArg4() ==0) arg1 = locals;
									else arg2 = locals; 
								}
								locals++;
							}
						}
						else if(inst2.getKind() == Instruction.INHERITLINK){
							if(inst2.getIntArg1() == newatomvar){
								if(!newlink.contains(inst2)) {
									newlink.add(inst2);
									newlinks2.add(inst2);
								}
								if(!removelinks.contains(inst2)) removelinks.add(inst2);
								newlinkmem = inst2.getIntArg4();
								if(getlinkmap.containsKey(inst2.getArg3())){
									Instruction getlink = (Instruction)getlinkmap.get(inst2.getArg3());
									inline1.add(new Instruction(Instruction.DEREFATOM, locals, getlink.getIntArg2(), getlink.getIntArg3()));
									inline2.add(getlink);
									if(!removelinks.contains(getlink))removelinks.add(getlink);
								}
								if (arg1 == -1) arg1 = locals;
								else arg2 = locals;
								locals++;
							}
							else if(inst2.getIntArg3() == newatomvar){
								if(!newlink.contains(inst2)) {
									newlink.add(inst2);
									newlinks2.add(inst2);
								}
								if(!removelinks.contains(inst2)) removelinks.add(inst2);
								newlinkmem = inst2.getIntArg4();
								if(getlinkmap.containsKey(inst2.getArg1())){
									Instruction getlink = (Instruction)getlinkmap.get(inst2.getArg1());
									inline1.add(new Instruction(Instruction.DEREFATOM, locals, getlink.getIntArg2(), getlink.getIntArg3()));
									inline2.add(getlink);
									if(!removelinks.contains(getlink))removelinks.add(getlink);
								}
								if (inst2.getIntArg2() == 1) arg1 = locals;
								else arg2 = locals;
								locals++;
							}
						}

						if(arg1 !=-1 && arg2 != -1 && result != -1 && resultlink != -1) {
							//�����ƥ�롼�륻�å���������̿���� inline1
							if(old2new.containsKey(arg1)) arg1 = old2new.get(arg1);
							if(old2new.containsKey(arg1)) arg2 = old2new.get(arg2);

							inline1.add(new Instruction(typecheck, arg1));
							inline1.add(new Instruction(typecheck, arg2));
							inline1.add(new Instruction(op, locals, arg1, arg2));
							inline1.add(new Instruction(Instruction.ADDATOM, newlinkmem, locals));
							inline1.add(new Instruction(Instruction.NEWLINK, locals, 0, result, resultlink, newlinkmem));

							inline1.add(new Instruction(Instruction.REMOVEATOM, arg1, newlinkmem));
							inline1.add(new Instruction(Instruction.REMOVEATOM, arg2, newlinkmem));
							inline1.add(new Instruction(Instruction.REMOVEATOM, newatomvar, newlinkmem));
							inline1.add(new Instruction(Instruction.FREEATOM, arg1));
							inline1.add(new Instruction(Instruction.FREEATOM, arg2));
							inline1.add(new Instruction(Instruction.PROCEED));
							if(!old2new.containsKey(newatomvar)) old2new.put(newatomvar, locals);
							locals++;

							Instruction newenqueueatom = new Instruction(Instruction.ENQUEUEATOM, newatomvar);
							if(enqueueatoms.contains(newenqueueatom)) enqueueatoms.remove(newenqueueatom);
							else enqueueatoms.add(newenqueueatom);
							for(int k=i+1; k<body.size(); k++){
								Instruction inst3 = (Instruction)body.get(k);
								if(inst3.getKind() == Instruction.ENQUEUEATOM
										&& (inst3.getIntArg1() == newatomvar))
									body.remove(k--);
							}
							//�����ƥ�롼�륻�åȼ��Ի���̿���� inline2
							Iterator<Instruction> it = newlinks2.iterator();
							while(it.hasNext())
								inline2.add(it.next());
							it = enqueueatoms.iterator();
							while(it.hasNext())
								inline2.add(it.next());							
							inline2.add(new Instruction(Instruction.PROCEED));
							//System.out.println(inline2.insts);
							//�����ƥ�롼�륻�å�̿����ɲ�
							for(int i2=body.size()-1; i2>0; i2--){
								inst2 = body.get(i2);
								if(!(inst2.getKind() == Instruction.PROCEED)
										&& !(inst2.getKind() == Instruction.FREEATOM)
										&& !(inst2.getKind() == Instruction.FREEMEM)
										&& !(inst2.getKind() == Instruction.FREEGROUND)
										&& !(inst2.getKind() == Instruction.ENQUEUEALLATOMS)
										&& !(inst2.getKind() == Instruction.ENQUEUEATOM)
										&& !(inst2.getKind() == Instruction.ENQUEUEMEM)
										&& !(inst2.getKind() == Instruction.UNLOCKMEM)
										&& !(inst2.getKind() == Instruction.SYSTEMRULESETS)){
									body.add(i2, new Instruction(Instruction.SYSTEMRULESETS, inline1, inline2));
									break;
								}
							}
							break;
						} else continue;
					} 
				}
			}
		}

		body.remove(0);
		body.add(0, new Instruction(Instruction.SPEC, spec.getIntArg1(), locals));
//		���פ�newlink�ν���
		for(int i2=0; i2<body.size(); i2++){
			Instruction inst2 = (Instruction)body.get(i2);
			if(removelinks.contains(inst2)) body.remove(i2--);
		}
	}

	///////////////////////////////////////////////////////
	// ���Ŭ����Ϣ

	/**
	 * ��κ����Ѥ�Ԥ������ɤ��������롣<br>
	 * ̿������ˤϡ�1������removemem̿�᤬����Ƥ��ƤϤ����ʤ���
	 * ̿����κǸ��proceed̿��Ǥʤ���Фʤ�ʤ���
	 * @param head �إå�̿����
	 * @param body �ܥǥ�̿����
	 */
	private static void reuseMem(List<Instruction> head, List<Instruction> body) {
		Instruction spec = body.get(0);
		if (spec.getKind() != Instruction.SPEC) {
			return;
		}
		Instruction last = body.get(body.size() - 1);
		if (last.getKind() != Instruction.PROCEED) {
			return;
		}
		//�Х�����
		for(Iterator<Instruction> itb = body.iterator(); itb.hasNext();) {
			int itbKind = itb.next().getKind();
			if (itbKind == Instruction.COPYCELLS || itbKind == Instruction.DROPMEM)
				return;
		}

		HashMap<Integer, Integer> reuseMap = new HashMap<Integer, Integer>();
		HashSet<Integer> reuseMems = new HashSet<Integer>(); // �����Ѥ�������ID�ν���
		HashMap parent = new HashMap();
		HashMap<Integer, List<Integer>> removedChildren = new HashMap<Integer, List<Integer>>(); // map -> list of children
		HashMap<Integer, List<Integer>> createdChildren = new HashMap<Integer, List<Integer>>(); // map -> list of children
		HashMap<Integer, List<Integer>> pourMap = new HashMap<Integer, List<Integer>>();
		HashSet pourMems = new HashSet(); // pour̿����裲�����˴ޤޤ����
		HashMap<Integer, List<Integer>> copyRulesMap = new HashMap<Integer, List<Integer>>();
		
		HashMap<Integer, String> headMemName = new HashMap<Integer, String>(); // head �˴ؤ����줫����̾�ؤ� map
		HashMap<Integer, String> bodyMemName = new HashMap<Integer, String>(); // body �˴ؤ����줫����̾�ؤ� map

		//�����Ѥ�������Ȥ߹�碌����ꤹ��
		for(Instruction inst : body){
			switch (inst.getKind()) {
			case Instruction.REMOVEMEM:
				parent.put(inst.getArg1(), inst.getArg2());
				addToMap(removedChildren, inst.getArg2(), inst.getArg1());
				break;
			case Instruction.NEWMEM:
				parent.put(inst.getArg1(), inst.getArg2());
				addToMap(createdChildren, inst.getArg2(), inst.getArg1());
				break;
			case Instruction.MOVECELLS:
				addToMap(pourMap, inst.getArg1(), inst.getArg2());
				pourMems.add(inst.getArg2());
				break;
			case Instruction.COPYRULES:
				addToMap(copyRulesMap, inst.getArg2(), inst.getArg1());
				break;
			}
		}
		
		// ��̾�򵭲�����
		for(Instruction inst: head){
			switch(inst.getKind()){
			case Instruction.LOCKMEM:
				headMemName.put((Integer)inst.getArg1(), (String)inst.getArg3());
				break;
			case Instruction.ANYMEM:
				headMemName.put((Integer)inst.getArg1(), (String)inst.getArg4());
				break;
			case Instruction.ANYMEM2:
				headMemName.put((Integer)inst.getArg1(), (String)inst.getArg5());
				break;
			case Instruction.GETMEM:
				headMemName.put((Integer)inst.getArg1(), (String)inst.getArg4());
				break;
			}
		}
		for(Instruction inst: body){
			switch(inst.getKind()){
			case Instruction.SETMEMNAME:
				bodyMemName.put((Integer)inst.getArg1(), (String)inst.getArg2());

			}
		}
		

		createReuseMap(reuseMap, reuseMems, parent, removedChildren, createdChildren,
				pourMap, pourMems, new Integer(0));


		//̿�����񤭴�����
		//���κݡ���Ĺ��removemem/addmem̿�������
		HashSet<Integer> set = new HashSet<Integer>(); //removemem/addmem̿������פ�������Ѥ˴ؤ����
		for(Iterator<Integer> it = reuseMap.keySet().iterator(); it.hasNext();){
			Integer i1 = it.next();
			Integer i2 = reuseMap.get(i1);
			Integer p1 = (Integer)parent.get(i1);
			Integer p2 = (Integer)parent.get(i2);
			if (reuseMap.containsKey(p1)) {
				p1 = reuseMap.get(p1);
			}
			if (p1.equals(p2)) { //�Ƥ�Ʊ�����ä���
				set.add(i1);
				set.add(i2);
			}
		}

		//�롼�������
		HashMap<Integer, Integer> ruleMem = new HashMap<Integer, Integer>(); //�롼������򤷤���
		HashMap<Integer, Integer> varInBody = new HashMap<Integer, Integer>(); // �إåɤǤ��ѿ�̾���ܥǥ��Ǥ��ѿ�̾

		Instruction react = head.get(head.size() - 1);
		if (react.getKind() != Instruction.REACT && react.getKind() != Instruction.JUMP) {
			return;
		}
		int i = 0;
		ArrayList args = (ArrayList)react.getArg2();
		for(Iterator it = args.iterator(); it.hasNext();){
			varInBody.put((Integer)it.next(), new Integer(i++));
		}

		for(Iterator<Instruction> it = head.iterator(); it.hasNext();){
			Instruction inst = it.next();
			if (inst.getKind() == Instruction.NORULES) {
				//�롼��������оݤ��鳰��
				varInBody.remove(inst.getArg1());
			}
		}
		//���򤹤�̿�������
		ArrayList<Instruction> tmpInsts = new ArrayList<Instruction>();
		int nextArg = spec.getIntArg2();
		for(Iterator<Integer> it = varInBody.keySet().iterator(); it.hasNext();){
			Integer memInHead = it.next();
			Integer mem = varInBody.get(memInHead);
			if (reuseMems.contains(mem)) {//�����Ѹ�����ξ��
				List<Integer> copyRulesTo = copyRulesMap.get(mem);
				boolean flg = false;
				if (copyRulesTo == null) {
					//���դ˥롼��ʸ̮���и����ʤ��Τ��������ס�����Τ�
					tmpInsts.add(new Instruction(Instruction.CLEARRULES, mem)); 
				} else {
					for(Integer dstmem : copyRulesTo){
						if (mem.equals(reuseMap.get(dstmem))) {
							flg = true; //��ʬ�˥��ԡ����Ƥ���Τ���������
							break;
						}
					}
					if (!flg) {
						//�������
						ruleMem.put(mem, new Integer(nextArg));
						tmpInsts.add(new Instruction(Instruction.ALLOCMEM, nextArg));
						tmpInsts.add(new Instruction(Instruction.COPYRULES, nextArg, mem));
						tmpInsts.add(new Instruction(Instruction.CLEARRULES, mem)); 
						nextArg++;
					}
				}
			}
		}
		body.addAll(1, tmpInsts);

		//TODO removeproxies/insertproxies̿���Ŭ�ڤ��ѹ�����
//		tmpInsts = new ArrayList();
		ListIterator<Instruction> lit = body.listIterator();
		while (lit.hasNext()) {
			Instruction inst = lit.next();
			switch (inst.getKind()) {
			case Instruction.REMOVEMEM:
				if (set.contains(inst.getArg1())) {
					lit.remove();
				}
				break;
			case Instruction.NEWMEM:
				Integer arg1 = (Integer)inst.getArg1();
				if (reuseMap.containsKey(arg1)) {
					lit.remove();
					//addmem��enqueuemem̿����ѹ�
					int m = ((Integer)reuseMap.get(arg1)).intValue();
					if (!set.contains(arg1)) {
						lit.add(new Instruction(Instruction.ADDMEM, inst.getIntArg2(), m)); 
					}
					lit.add(new Instruction(Instruction.ENQUEUEMEM, m));
					
					// null �Ǥʤ���̾���� null ����̾�غ����Ѥ���Ȥ��ˤ� setmemname ��ɬ�� (�Х�/090207_-O2���ʣ���Х�)
					if(bodyMemName.get(arg1) == null && headMemName.get(args.get(m)) != null){
						lit.add(new Instruction(Instruction.SETMEMNAME, m, null));
					}
				}
				break;
			case Instruction.MOVECELLS:
				if (reuseMems.contains(inst.getArg2())) {
					//addmem̿��ǰ�ư����λ���Ƥ��뤿�����
					//�������줬��2�İʾ����κ����Ѥκ���Ȥʤ뤳�ȤϤʤ�
					lit.remove();
				}
				break;
//				case Instruction.LOADRULESET:
//				//�롼������򤷤ʤ����������ƺǸ�˰�ư
//				tmpInsts.add(inst);
//				lit.remove();
//				break;
			case Instruction.COPYRULES:
				Integer srcmem = (Integer)inst.getArg2();
				Integer dstmem = (Integer)inst.getArg1();
				if (ruleMem.containsKey(srcmem)) {
					if (!ruleMem.get(srcmem).equals(dstmem)) { //����Τ����̿��Ǥʤ����
						//���򤷤��줫��Υ��ԡ����ѹ�
						lit.remove();
						lit.add(new Instruction(Instruction.COPYRULES, dstmem.intValue(), ((Integer)ruleMem.get(srcmem)).intValue())); 
					}
				} else if (srcmem.equals(reuseMap.get(dstmem))) {
					//��ʬ�ؤΥ��ԡ��ʤΤǺ��
					lit.remove();
				}
				break;
			case Instruction.FREEMEM:
				if (reuseMems.contains(inst.getArg1())) {
					lit.remove();
				}
				break;
			}
		}
		lit.previous(); //�Ǹ��proceed̿��μ������ɲ�
//		//loadruleset̿��ΰ�ư
//		it = tmpInsts.iterator();
//		while (it.hasNext()) {
//		lit.add(it.next());
//		}
		//�����Ѥ������unlockmem̿����ɲ�
		addUnlockInst(lit, reuseMap, new Integer(0), createdChildren);
		//����˻��Ѥ�����β���
		for(Iterator<Integer> it = ruleMem.values().iterator(); it.hasNext();){
			lit.add(new Instruction(Instruction.FREEMEM, it.next()));
		}

		//spec���ѹ�
//		body.set(0, Instruction.spec(spec.getIntArg1(), nextArg));
		spec.updateSpec(spec.getIntArg1(), nextArg);

		Instruction.changeMemVar(body, reuseMap);
	}
	private static void addUnlockInst(ListIterator<Instruction> lit, HashMap<Integer, Integer> reuseMap,
			Integer mem, HashMap<Integer, List<Integer>> children) {
		//�������˽���
		List<Integer> c = children.get(mem);
		if (c != null) {
			Iterator<Integer> it = c.iterator();
			while (it.hasNext()) {
				addUnlockInst(lit, reuseMap, it.next(), children);
			}
		}

		if (reuseMap.containsKey(mem)) {
			lit.add(new Instruction(Instruction.UNLOCKMEM, mem));
		}
	}

	/**
	 * List���ͤȤ���褦�ʥޥåפ��ͤ��ɲä��롣
	 * ��Ŧ���줿���������Ǥ�¸�ߤ�������ͤΥꥹ�Ȥ�value���ɲä��롣
	 * ¸�ߤ��ʤ����Ͽ������ꥹ�Ȥ���Ͽ�����ޥåפ��ɲä��롣
	 * @param map �ޥå�
	 * @param key ����
	 * @param value ��
	 */
	private static void addToMap(HashMap map, Object key, Object value) {
		ArrayList list = (ArrayList)map.get(key);
		if (list == null) {
			list = new ArrayList();
			map.put(key, list);
		}
		list.add(value);
	}

	/**
	 * �����Ѥ��������ꤷ�ޤ���
	 * start�ǻ��ꤵ�줿����ˤ�����ˤĤ��ơ��Ƶ��ƤӽФ���Ԥ��ޤ���
	 * @param reuseMap ��������ˡ������뤿��Υޥå�
	 * @param reuseMems �����Ѥ������ν��������뤿��Υ��å�
	 * @param parent ����ؤΥޥå�
	 * @param children ���콸��ؤΥޥå�
	 * @param pourMap pour̿����б������줿�ޥå�
	 * @param pourMems pour̿����裲�����˴ޤޤ����Υ��å�
	 * @param start �����������κ����Ѥ���ꤹ�롣
	 */
	private static void createReuseMap(HashMap<Integer, Integer> reuseMap, 
			HashSet<Integer> reuseMems, 
			HashMap parent,
			HashMap<Integer, List<Integer>> removedChildren,
			HashMap<Integer, List<Integer>> createdChildren,
			HashMap<Integer, List<Integer>> pourMap, 
			HashSet pourMems, Integer start) {

		List<Integer> list = createdChildren.get(start);
		if (list == null || list.size() == 0) {
			return;
		}

		Integer start2; //start�Ρ������Ѹ���ѿ��ֹ�
		if (reuseMap.containsKey(start)) {
			start2 = reuseMap.get(start);
		} else {
			start2 = start;
		}
		for(int mem : list){
			//mem�κ����Ѹ������
			Integer candidate = null; //pour̿��ˤ������Ѹ���򣱤��ݻ����Ƥ���
			Integer result = null; //���ꤷ����������������
			List<Integer> list2 = pourMap.get(mem);
			if (list2 != null) {
				for(int mem2 : list2){
					//���Ǥ˺����Ѥ��뤳�Ȥ���ޤäƤ������̵��
					if (reuseMems.contains(mem2)) {
						continue;
					}

					//�������Ͽ
					candidate = mem2;

					if (parent.get(mem2).equals(start2)) {
						//���줬Ʊ���줫���pour̿�᤬������ϡ������ͥ��
						result = mem2;
						break;
					}
				}
			}
			if (result == null) {
				//�����ˡ�Ƿ�ޤ�ʤ��ä����
				if (candidate == null) {
					//���̤ο������ġ��ץ���ʸ̮�Τʤ�����椫��Ŭ���˷��ꡣ
					//���������줬�ʤ���к����Ѥ��ʤ�
					List<Integer> list3 = removedChildren.get(start2);
					if (list3 != null) {
						for(int m : list3){
							if (!pourMems.contains(m) && !reuseMems.contains(m)) {
								result = m;
								break;
							}
						}
					}
				} else {
					//pour̿�᤬�����椫��Ŭ���˷���
					result = candidate;
				}
			}
			if (result != null) {
				reuseMap.put(mem, result);
				reuseMems.add(result);
			}
			//�Ƶ��ƤӽФ�
			createReuseMap(reuseMap, reuseMems, parent, removedChildren, createdChildren,
					pourMap, pourMems, mem);
		}
	}

	//////////////////////////////////////////////////////////////////////
	// ���ȥ�����Ѵ�Ϣ

	/**
	 * relink̿���getlink/inheritlink̿����Ѵ�����
	 * getlink/unify̿���ܥǥ�̿�������Ƭ�˰�ư���롣
	 * ��Ƭ��̿���spec�Ǥʤ���Фʤ�ʤ���
	 * @param list �Ѵ�����̿����
	 * @return �Ѵ���������������true
	 */
	public static boolean changeOrder(List<Instruction> list) {
		Instruction spec = list.get(0);
		if (spec.getKind() != Instruction.SPEC) {
			return false;
		}
		int nextId = spec.getIntArg2();

		List<Instruction> moveInsts = new ArrayList<Instruction>();
		for(ListIterator<Instruction> it = list.listIterator(1); it.hasNext();){
			Instruction inst = it.next();
			switch (inst.getKind()) {
			case Instruction.UNIFY:
				moveInsts.add(inst);
				it.remove();
				break;
			case Instruction.RELINK:
				moveInsts.add(new Instruction(Instruction.GETLINK,  nextId, inst.getIntArg3(), inst.getIntArg4()));
				it.set(new Instruction(Instruction.INHERITLINK,  inst.getIntArg1(), inst.getIntArg2(), nextId, inst.getIntArg5()));
				nextId++;
				break;
			}
		}
//		list.set(0, Instruction.spec(spec.getIntArg1(), nextId));
		spec.updateSpec(spec.getIntArg1(), nextId);
		if (list.size() >= 2 && ((Instruction)list.get(1)).getKind() == Instruction.COMMIT)
			list.addAll(2, moveInsts); // (061128okabe) commit��������ϣ����ܤǤʤ���Фʤ�ʤ���
		else
			list.addAll(1, moveInsts);
//		spec.data.set(1, new Integer(nextId)); //�������ѿ��ο����ѹ�
		return true;
	}

	/**
	 * �졦�ե��󥯥���˥��ȥ�ν����������뤿��Υ��饹��
	 * ���ȥ�����ѥ����ɤ���������ݤ˥��ȥ��������뤿��˻��Ѥ��롣
	 * @author Ken
	 */
	private static class AtomSet {
		HashMap<Integer, HashMap<Functor, HashSet<Integer>>> map 
		= new HashMap<Integer, HashMap<Functor, HashSet<Integer>>>(); // mem -> (functor -> atoms)
		/**
		 * ���ȥ���ɲä���
		 * @param mem ���ȥब��°������
		 * @param functor ���ȥ�Υե��󥯥�
		 * @param atom �ɲä��륢�ȥ�
		 */
		void add(Integer mem, Functor functor, Integer atom) {
			HashMap<Functor, HashSet<Integer>> map2 = map.get(mem);
			if (map2 == null) {
				map2 = new HashMap<Functor, HashSet<Integer>>();
				map.put(mem, map2);
			}
			HashSet<Integer> atoms = map2.get(functor);
			if (atoms == null) {
				atoms = new HashSet<Integer>();
				map2.put(functor, atoms);
			}
			atoms.add(atom);
		}
		/**
		 * ���ꤵ�줿��˽�°���롢���ꤵ�줿�ե��󥯥�����ĥ��ȥ��ȿ���Ҥ��֤�
		 * @param mem ���ȥब��°���륢�ȥ�
		 * @param functor ���ȥ�Υե��󥯥���
		 * @return ȿ����
		 */
		Iterator<Integer> iterator(Integer mem, Functor functor) {
			HashMap<Functor, HashSet<Integer>> map2 = map.get(mem);
			if (map2 == null) {
				return util.Util.NULL_ITERATOR;
			}
			HashSet<Integer> atoms = map2.get(functor);
			if (atoms == null) {
				return util.Util.NULL_ITERATOR;
			}
			return atoms.iterator();
		}
		/**
		 * ���ȿ���Ҥ��֤�
		 * @return ȿ����
		 */
		Iterator<Integer> memIterator() {
			return map.keySet().iterator();
		}
		/**
		 * ���ꤵ�줿����ˤ��롢���Υ��󥹥��󥹤��������륢�ȥ�Υե��󥯥���ȿ���Ҥ��֤�
		 * @param mem ��
		 * @return ȿ����
		 */
		Iterator<Functor> functorIterator(Integer mem) {
			HashMap<Functor, HashSet<Integer>> map2 = map.get(mem);
			if (map2 == null) {
				return util.Util.NULL_ITERATOR;
			}
			return map2.keySet().iterator();
		}
	}
	/**	
	 * ���ȥ�����Ѥ�Ԥ������ɤ��������롣<br>
	 * �������Ϥ����̿����ϡ����ξ����������Ƥ���ɬ�פ����롣
	 * <ul>
	 *  <li>1������removeatom̿�����Ѥ��Ƥ��ʤ�
	 *  <li>getlink̿�����Ѥ��Ƥ��ʤ�
	 * </ul>
	 * @param list ��Ŭ��������̿���󡣺��ΤȤ���ܥǥ�̿�����Ϥ���뤳�Ȥ��ꤷ�Ƥ��롣
	 */
	private static void reuseAtom(List<Instruction> head, List<Instruction> body) {
		/////////////////////////////////////////////////
		//
		// �����Ѥ��륢�ȥ���Ȥ߹�碌����ꤹ��
		//

		// removeatom/newatom/getlink̿��ξ����Ĵ�٤�
		AtomSet removedAtoms = new AtomSet();
		AtomSet createdAtoms = new AtomSet();
//		HashMap getlinkInsts = new HashMap(); // linkId -> getlink instruction
		for(Instruction inst : body){
			switch (inst.getKind()) {
			case Instruction.REMOVEATOM:
				try {
					Integer atom = (Integer)inst.getArg1();
					Functor functor = (Functor)inst.getArg3();
					Integer mem = (Integer)inst.getArg2();
					removedAtoms.add(mem, functor, atom);
				} catch (IndexOutOfBoundsException e) {}
				break;
			case Instruction.NEWATOM:
				Integer atom = (Integer)inst.getArg1();
				Functor functor = (Functor)inst.getArg3();
				Integer mem = (Integer)inst.getArg2();
				createdAtoms.add(mem, functor, atom);
				break;
			}
		}

		//�����Ѥ��륢�ȥ���Ȥ߹�碌�����

		//���������Υ��ȥ�ID -> �����Ѹ�Υ��ȥ�ID
		HashMap<Integer, Integer> reuseMap = new HashMap<Integer, Integer>();
		//�����Ѥ���륢�ȥ��ID��reuseMap���ͤ����ꤵ��Ƥ���ID�ν����
		HashSet<Integer> reuseAtoms = new HashSet<Integer>(); 

		//Ʊ����ˤ��롢Ʊ��̾���Υ��ȥ������Ѥ���
		for(Iterator<Integer> memIterator = removedAtoms.memIterator(); memIterator.hasNext();){
			Integer mem = memIterator.next();
			for(Iterator<Functor> functorIterator = removedAtoms.functorIterator(mem); functorIterator.hasNext();){
				Functor functor = functorIterator.next();
				//removeproxies��insertproxies������Τǡ������ѤǤ��ʤ�
				if (functor instanceof SpecialFunctor) {
					continue;
				}
				Iterator<Integer> removedAtomIterator = removedAtoms.iterator(mem, functor);
				Iterator<Integer> createdAtomIterator = createdAtoms.iterator(mem, functor);
				while (removedAtomIterator.hasNext() && createdAtomIterator.hasNext()) {
					Integer removeAtom = removedAtomIterator.next();
					reuseMap.put(createdAtomIterator.next(), removeAtom);
					reuseAtoms.add(removeAtom);
					//�����Ѥ��Ȥ߹�碌����ޤä���ΤϺ������
					//�ʤ��θ��³�������ȥ�̾���ۤʤ���ʤɤκ����Ѥ��Ȥ߹�碌����ꤹ������Τ��ᡣ��
					removedAtomIterator.remove();
					createdAtomIterator.remove();
				}
			}
		}

		//TODO �졦���ȥ�̾���ۤʤ��Τκ����Ѥ��Ȥ߹�碌����ꤹ�륳���ɤ򤳤��˽�


		//////////////////////////////////////////////////
		//
		// ���ȥ������Ѥ���褦��̿�������������
		//

		//�������
		HashMap<Integer, Integer> varInBody = new HashMap<Integer, Integer>(); // �إåɤǤ��ѿ�̾���ܥǥ��Ǥ��ѿ�̾

		Instruction react = head.get(head.size() - 1);
		if (react.getKind() != Instruction.REACT && react.getKind() != Instruction.JUMP) {
			return;
		}
		int i = ((List)react.getArg2()).size();
		List<Integer> args = (List)react.getArg3();
		for(Iterator<Integer> it = args.iterator(); it.hasNext(); ){
			varInBody.put(it.next(), new Integer(i++));
		}

		HashMap<Link, Link> links = new HashMap<Link, Link>();
		for(Instruction inst : head){
			if (inst.getKind() == Instruction.DEREF) {
				if (!varInBody.containsKey(inst.getArg2()) || !varInBody.containsKey(inst.getArg1())) {
					//�ܥǥ�̿������Ϥ���ʤ��ѿ��˴ؤ������
					//��󥯹�¤���۴Ĥ��Ƥ�����˸���롣
					//̵�̤�ȯ�������礬���뤬���Х��ˤϤʤ�ʤ��ΤǤȤꤢ�������֡�
					//TODO Ŭ�ڤ˽�������
					continue;
				}
				int atom1, atom2;
				atom1 = varInBody.get(inst.getArg2());
				atom2 = varInBody.get(inst.getArg1());
				Link l1 = new Link(atom1, inst.getIntArg3());
				Link l2 = new Link(atom2, inst.getIntArg4());
				links.put(l1, l2);
				links.put(l2, l1);
			}
		}

		//���פˤʤä�removeatom/freeatom/newatom̿������
		for(ListIterator<Instruction> lit = body.listIterator(/*getlinkInsts.size() + */1); lit.hasNext();){
			Instruction inst = lit.next();
			switch (inst.getKind()) {
			case Instruction.REMOVEATOM:
			case Instruction.FREEATOM:
				Integer atomId = (Integer)inst.getArg1();
				if (reuseAtoms.contains(atomId)) {
					lit.remove();
				}
				break;
			case Instruction.NEWATOM:
				atomId = (Integer)inst.getArg1();
				if (reuseMap.containsKey(atomId)) {
					lit.remove();
				}
				break;
			case Instruction.NEWLINK:
				Integer a1 = reuseMap.get(inst.getArg1());
				Integer a2 = reuseMap.get(inst.getArg3());
				if (a1 != null && a2 != null) {
					Link l1 = new Link(a1.intValue(), inst.getIntArg2());
					Link l2 = new Link(a2.intValue(), inst.getIntArg4());
					if (l2.equals(links.get(l1))) {
						lit.remove();
					}
				}
				break;
			}
		}
		//TO DO enqueueatom̿������� �� ����̿����ˤ����Τ��Ȥ���Τ�����

		Instruction.changeAtomVar(body, reuseMap);

	}

	// ========================================================================
	// �ƥ��ȼ���
	// ========================================================================
	private static void reuseAtom2(List<Instruction> head, List<Instruction> body)
	{
		List<Instruction> getlinks = new ArrayList<Instruction>();
		List<Instruction> inhlinks = new ArrayList<Instruction>();
		for (int i = 0; i < body.size(); i++)
		{
			Instruction inst = body.get(i);
			if (inst.getKind() == Instruction.GETLINK)
			{
				getlinks.add(inst);
				body.remove(i);
				i--;
			}
			else if (inst.getKind() == Instruction.INHERITLINK)
			{
				inhlinks.add(inst);
				body.remove(i);
				i--;
			}
		}
		//for (Instruction inst : getlinks) { System.out.println(inst); }
		
		//* ======== reuse getlink ========
		class GLPair
		{
			public Instruction inst;
			public int atom, link;
			public GLPair(Instruction ins, int a, int l) { inst = ins; atom = a; link = l; };
			public boolean equals(Object o)
			{
				return o instanceof GLPair &&
					((GLPair)o).atom == atom && ((GLPair)o).link == link;
			}
			public int hashCode() { return 17 * atom * link; }
		}
		HashMap<GLPair, GLPair> gl = new HashMap<GLPair, GLPair>();
		TreeMap<Integer, Integer> rewriteMap = new TreeMap<Integer, Integer>();
		for (int i = 0; i < getlinks.size(); i++)
		{
			Instruction ins = getlinks.get(i);
			GLPair p = new GLPair(ins, ins.getIntArg2(), ins.getIntArg3());
			if (gl.containsKey(p))
			{
				rewriteMap.put(ins.getIntArg1(), gl.get(p).inst.getIntArg1());
			}
			else
			{
				gl.put(p, p);
			}
		}
		//System.out.println(rewriteMap);
		Instruction.applyVarRewriteMap(body, rewriteMap);
		//for (GLPair g : gl.keySet()) { System.out.println(g); }
		getlinks.clear();
		for (GLPair g : gl.keySet()) { getlinks.add(g.inst); }
		//*/
		
		int pos = 0;
		for (pos = 0; pos < body.size(); pos++)
		{
			if (body.get(pos).getKind() == Instruction.COMMIT) break;
		}
		pos++;
		body.addAll(pos, getlinks);
		for (pos = 0; pos < body.size(); pos++)
		{
			if (body.get(pos).getKind() == Instruction.NEWATOM) break;
		}
		while (pos < body.size() && body.get(pos).getKind() == Instruction.NEWATOM) pos++;
		body.addAll(pos, inhlinks);
		
		//for (Instruction inst : body) { System.out.println(inst); }
		//*/
		
		/////////////////////////////////////////////////
		//
		// �����Ѥ��륢�ȥ���Ȥ߹�碌����ꤹ��
		//
		

		// removeatom/newatom̿��ξ����Ĵ�٤�
		AtomSet removedAtoms = new AtomSet();
		AtomSet createdAtoms = new AtomSet();
		for(Instruction inst : body)
		{
			switch (inst.getKind())
			{
			case Instruction.REMOVEATOM:
				try
				{
					int atom = inst.getIntArg1();
					Functor functor = (Functor)inst.getArg3();
					int mem = inst.getIntArg2();
					removedAtoms.add(mem, functor, atom);
					//System.out.println("remov: " + inst);
				}
				catch (IndexOutOfBoundsException e)
				{
					//System.err.println("inst: " + inst);
					//e.printStackTrace();
				}
				break;
			case Instruction.NEWATOM:
				int atom = inst.getIntArg1();
				Functor functor = (Functor)inst.getArg3();
				int mem = inst.getIntArg2();
				createdAtoms.add(mem, functor, atom);
				break;
			}
		}

		//�����Ѥ��륢�ȥ���Ȥ߹�碌�����

		//���������Υ��ȥ�ID -> �����Ѹ�Υ��ȥ�ID
		HashMap<Integer, Integer> reuseMap = new HashMap<Integer, Integer>();
		//�����Ѥ���륢�ȥ��ID��reuseMap���ͤ����ꤵ��Ƥ���ID�ν����
		HashSet<Integer> reuseAtoms = new HashSet<Integer>(); 

		//Ʊ����ˤ��롢Ʊ��̾���Υ��ȥ������Ѥ���
		for (Iterator<Integer> memIterator = removedAtoms.memIterator(); memIterator.hasNext(); )
		{
			int mem = memIterator.next();
			
			for(Iterator<Functor> functorIterator = removedAtoms.functorIterator(mem); functorIterator.hasNext(); )
			{
				Functor functor = functorIterator.next();
				//removeproxies��insertproxies������Τǡ������ѤǤ��ʤ�
				if (functor instanceof SpecialFunctor)
				{
					continue;
				}
				Iterator<Integer> removedAtomIterator = removedAtoms.iterator(mem, functor);
				Iterator<Integer> createdAtomIterator = createdAtoms.iterator(mem, functor);
				while (removedAtomIterator.hasNext() && createdAtomIterator.hasNext())
				{
					int createAtom = createdAtomIterator.next();
					int removeAtom = removedAtomIterator.next();
					reuseMap.put(createAtom, removeAtom);
					reuseAtoms.add(removeAtom);
					//�����Ѥ��Ȥ߹�碌����ޤä���ΤϺ������
					//�ʤ��θ��³�������ȥ�̾���ۤʤ���ʤɤκ����Ѥ��Ȥ߹�碌����ꤹ������Τ��ᡣ��
					removedAtomIterator.remove();
					createdAtomIterator.remove();
				}
			}
		}
		
		//System.out.println("remain: " + removedAtomSet);
		//System.out.println("created: " + createdAtomSet);
		
		//TODO �졦���ȥ�̾���ۤʤ��Τκ����Ѥ��Ȥ߹�碌����ꤹ�륳���ɤ򤳤��˽�


		//////////////////////////////////////////////////
		//
		// ���ȥ������Ѥ���褦��̿�������������
		//

		//�������
		HashMap<Integer, Integer> varInBody = new HashMap<Integer, Integer>(); // �إåɤǤ��ѿ�̾���ܥǥ��Ǥ��ѿ�̾

		Instruction react = head.get(head.size() - 1);
		if (react.getKind() != Instruction.REACT && react.getKind() != Instruction.JUMP)
		{
			return;
		}
		int i = ((List)react.getArg2()).size();
		List<Integer> args = (List)react.getArg3();
		for(Iterator<Integer> it = args.iterator(); it.hasNext(); )
		{
			varInBody.put(it.next(), i++);
		}

		HashMap<Link, Link> links = new HashMap<Link, Link>();
		for (Instruction inst : head)
		{
			if (inst.getKind() == Instruction.DEREF)
			{
				if (!varInBody.containsKey(inst.getArg2()) || !varInBody.containsKey(inst.getArg1()))
				{
					//�ܥǥ�̿������Ϥ���ʤ��ѿ��˴ؤ������
					//��󥯹�¤���۴Ĥ��Ƥ�����˸���롣
					//̵�̤�ȯ�������礬���뤬���Х��ˤϤʤ�ʤ��ΤǤȤꤢ�������֡�
					//TODO Ŭ�ڤ˽�������
					continue;
				}
				int atom1, atom2;
				atom1 = varInBody.get(inst.getArg2());
				atom2 = varInBody.get(inst.getArg1());
				Link l1 = new Link(atom1, inst.getIntArg3());
				Link l2 = new Link(atom2, inst.getIntArg4());
				links.put(l1, l2);
				links.put(l2, l1);
			}
		}
		

		//System.out.println("=====before");
		//for (Instruction inst : body) { System.out.println(inst); }
		//System.out.println("=====");
		
		//���פˤʤä�removeatom/freeatom/newatom̿������
		for (ListIterator<Instruction> lit = body.listIterator(1); lit.hasNext(); )
		{
			Instruction inst = lit.next();
			int atomId;
			
			switch (inst.getKind())
			{
			case Instruction.REMOVEATOM:
			case Instruction.FREEATOM:
				atomId = inst.getIntArg1();
				if (reuseAtoms.contains(atomId)) lit.remove();
				break;
			case Instruction.NEWATOM:
				atomId = inst.getIntArg1();
				if (reuseMap.containsKey(atomId)) lit.remove();
				break;
			case Instruction.NEWLINK:
				int arg1 = inst.getIntArg1(), arg3 = inst.getIntArg3();
				if (reuseMap.containsKey(arg1) && reuseMap.containsKey(arg3))
				{
					// �����Ѥ��줿���ȥबnewlink�ǷҤ���뤬�����ˤĤʤ��äƤ�����
					int a1 = reuseMap.get(arg1);
					int a2 = reuseMap.get(arg3);
					Link l1 = new Link(a1, inst.getIntArg2());
					Link l2 = new Link(a2, inst.getIntArg4());
					if (l2.equals(links.get(l1)))
					{
						//System.out.println("test");
						lit.remove();
					}
				}
				break;
			}
		}
		//TO DO enqueueatom̿������� �� ����̿����ˤ����Τ��Ȥ���Τ�����

		Instruction.changeAtomVar(body, reuseMap);

	}

	/**
	 * ��Ĺ��relink/inheritlink̿������ޤ���
	 * @param list
	 */
	private static void removeUnnecessaryRelink(List<Instruction> list) {
		HashMap getlinkInsts = new HashMap(); // linkId -> getlink instruction
		List<Instruction> remove = new ArrayList<Instruction>();
		for(Instruction inst : list){
			switch (inst.getKind()) {
			case Instruction.GETLINK:
				getlinkInsts.put(inst.getArg1(), inst);
				break;
			case Instruction.INHERITLINK:
				Instruction getlink = (Instruction)getlinkInsts.get(inst.getArg3());
				if (getlink.getArg2().equals(inst.getArg1()) &&  // <- atomID
						getlink.getArg3().equals(inst.getArg2())) { // <- pos
					//��Ĺ�ʤΤǽ���
					remove.add(getlink);
					remove.add(inst);
				}
				break;
			}
		}
		list.removeAll(remove);
	}

	//////////////////////////////////////////////////////////////
	// �롼�ײ���Ϣ

	/**
	 * ���ȥ�Ȱ����ֹ���Ȥ��ݻ����륯�饹��
	 * @author Ken
	 */	
	private static class Link {
		int atom;
		int pos;
		Link(int atom, int pos) {
			this.atom = atom;
			this.pos = pos;
		}
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			Link l = (Link)o;
			return this.atom == l.atom && this.pos == l.pos;
		}
		public int hashCode() {
			return atom + pos;
		}
		public String toString() {
			return "(" + atom + ", " + pos + ")";
		}
	}
	/**
	 * Ʊ��롼���ʣ����Ʊ��Ŭ��<br>
	 * �Ȥꤢ���������ξ����������Ƥ�����ˤΤ߽�����Ԥ���
	 * <ul>
	 *  <li>�ܥǥ��¹Բ������ȼ°�����Ʊ���Ǥ��롣
	 *  <li>spec̿��ʳ��κǽ��̿�᤬findatom�ǡ��������������0�Ǥ��롣
	 *  <li>�Ϥ����findatom̿��ˤ�äƼ������줿���ȥब�����Ѥ���Ƥ��롣
	 *  <li>derefatom,dereffunc�����Ѥ��Ƥ��ʤ�
	 * </ul>
	 * ��郎��������ʤ����ϲ��⤷�ʤ���
	 * @param head ���Ƴ�ޥå���̿����
	 * @param body �ܥǥ�̿����
	 */	
	private static void makeLoop(List<Instruction> head, List<Instruction> body) {
		Instruction inst = head.get(0);
		if (inst.getKind() != Instruction.SPEC) {
			return;
		}
//		if (!inst.getArg2().equals(new Integer(0))) {
//		//�ޥå���̿����˥������ѿ���������
//		return;
//		}
		inst = head.get(1);
		if (inst.getKind() != Instruction.FINDATOM || inst.getIntArg2() != 0) {
			return;
		}
		Integer firstAtom = (Integer)inst.getArg1();

		//���˹��פ��뤫�����ܾ������
		HashMap<Link, Link> links = new HashMap<Link, Link>(); //newlink̿�������������󥯤ξ���
		HashMap linkGetFrom = new HashMap(); //��� -> getlink����(atom,pos)
		HashMap functor = new HashMap(); // atom -> functor
		HashMap inherit = new HashMap(); // (atom,pos) -> inherit������
		//���ऴ�Ȥ��ѿ����������
		ArrayList memvars = new ArrayList();
		ArrayList atomvars = new ArrayList();
		ArrayList othervars = new ArrayList();
		Instruction react = head.get(head.size() - 1);
//		Iterator it = ((List)react.getArg2()).iterator();
//		while (it.hasNext()) {
//		memvars.add(it.next());
//		}
//		it = ((List)react.getArg3()).iterator();
//		while (it.hasNext()) {
//		atomvars.add(it.next());
//		}
		HashMap varInBody = new HashMap();

		List memlist = (List)react.getArg2();
		for (int i = 0; i < memlist.size(); i++) {
			memvars.add(new Integer(i));
			varInBody.put(memlist.get(i), new Integer(i));
		}
		List atomlist = (List)react.getArg3();
		Integer newFirstAtom = firstAtom;
		for (int i = 0; i < atomlist.size(); i++) {
			atomvars.add(new Integer(memlist.size() + i));
			if (firstAtom.equals(atomlist.get(i))) {
				newFirstAtom = new Integer(memlist.size() + i);
			}
			varInBody.put(atomlist.get(i), new Integer(memlist.size() + i));
		}
		firstAtom = newFirstAtom;
//		added by <<n-kato
		List otherlist = (List)react.getArg4();
		for (int i = 0; i < otherlist.size(); i++) {
			othervars.add(new Integer(i));
			varInBody.put(otherlist.get(i), new Integer(memlist.size() + atomlist.size() + i));
		}
//		n-kato
		for(ListIterator<Instruction> lit = head.listIterator(); lit.hasNext();){
			inst = lit.next();
			switch (inst.getKind()) {
			case Instruction.FINDATOM:
				functor.put(varInBody.get(inst.getArg1()), inst.getArg3());
				break;
			case Instruction.FUNC:
				functor.put(varInBody.get(inst.getArg1()), inst.getArg2());
				break;
			}
		}
		for(ListIterator<Instruction> lit = body.listIterator(); lit.hasNext();){
			inst = lit.next();
			switch (inst.getOutputType()) {
			case Instruction.ARG_ATOM:
				atomvars.add(inst.getArg1());
				break;
			case Instruction.ARG_MEM:
				memvars.add(inst.getArg1());
				break;
			case Instruction.ARG_VAR:
				othervars.add(inst.getArg1());
				break;
			case -1:
				break;
			default:
				throw new RuntimeException("invalid output type : " + inst);
			}
			switch (inst.getKind()) {
			case Instruction.DEREFATOM:
			case Instruction.DEREFFUNC:
//				System.out.println(inst);
				return;
			case Instruction.REMOVEATOM:
//				case Instruction.FREEATOM:
				if (inst.getArg1().equals(firstAtom)) {
//					System.out.println(inst);
					return;
				}
				break;
			case Instruction.NEWLINK:
				Link l1 = new Link(inst.getIntArg1(), inst.getIntArg2());
				Link l2 = new Link(inst.getIntArg3(), inst.getIntArg4());
				links.put(l1, l2);
				links.put(l2, l1);
				break;
			case Instruction.INHERITLINK:
				Link l = new Link(inst.getIntArg1(), inst.getIntArg2());
				inherit.put(l, inst.getArg3());
				break;
			case Instruction.NEWATOM:
				functor.put(inst.getArg1(), inst.getArg3());
//				atomvars.add(inst.getArg1());
				break;
			case Instruction.GETLINK:
				linkGetFrom.put(inst.getArg1(), new Link(inst.getIntArg2(), inst.getIntArg3()));
//				othervars.add(inst.getArg1());
				break;
			}
		}


		//�롼����̿���������

		//�ޤ��ϥ��ԡ������ѿ��ֹ��դ��ؤ�
		Instruction spec = body.get(0);

		List<Instruction> loop = new ArrayList<Instruction>(); //�롼�����̿����
		//�ޥå���̿����
		for(ListIterator<Instruction> lit = head.subList(2, head.size()-1).listIterator(); lit.hasNext();){ //spec,findatom,react/jump�����
			loop.add((Instruction)lit.next().clone());
		}
		if (react.getKind() != Instruction.REACT && react.getKind() != Instruction.JUMP) {
//			System.out.println(react);
			return;
		}
		//�ܥǥ�̿������ѿ����碌��
		Instruction.applyVarRewriteMap(loop, varInBody);
		//�ܥǥ�̿����
		for(ListIterator<Instruction> lit = body.listIterator(1); lit.hasNext();){ //spec�����
			loop.add((Instruction)lit.next().clone());
		}

		//�롼�����ѿ����դ��ؤ�

		//��Ȥ�Ȥ��ѿ����롼����ǡ��������������ѿ�
		HashMap<Integer, Integer> memVarMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> atomVarMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> otherVarMap = new HashMap<Integer, Integer>();
		//�롼����Ǻ�������Ƥ����ѿ�����Ȥ�Ȥ��ѿ�
		HashMap<Integer, Integer> reverseAtomVarMap = new HashMap<Integer, Integer>();
		int base = atomvars.size() + memvars.size() + othervars.size(); //�롼����̿����ǻ��Ѥ����ѿ��γ����� 
		int nextArg = base;
		//��
		Iterator<Integer> it = memvars.subList(1, memvars.size()).iterator(); //�Ϥ��������
		while (it.hasNext()) {
			memVarMap.put(it.next(), new Integer(nextArg++));
		}
		memVarMap.put(new Integer(0), new Integer(0));
		//���ȥ�
		it = atomvars.iterator();
		while (it.hasNext()) {
			Integer a = (Integer)it.next();
			if (!a.equals(firstAtom)) {
				Integer t = new Integer(nextArg++);
				atomVarMap.put(a, t);
				reverseAtomVarMap.put(t, a); 
			}
		}
		Integer firstAtomInLoop = new Integer(memvars.size() + atomvars.indexOf(firstAtom)); 
		atomVarMap.put(firstAtom, firstAtomInLoop);
		reverseAtomVarMap.put(firstAtomInLoop, firstAtom);

		//����¾
		it = othervars.iterator();
		while (it.hasNext()) {
			otherVarMap.put(it.next(), new Integer(nextArg++));
		}
		Instruction.changeMemVar(loop, memVarMap);
		Instruction.changeAtomVar(loop, atomVarMap);
		Instruction.changeOtherVar(loop, otherVarMap);

		Instruction resetVars = Instruction.resetvars(memvars, atomvars, othervars);

		//�롼����Ρ��ѿ�->������ǡ��������ä��ѿ�
		HashMap<Integer, Integer> beforeVar = new HashMap<Integer, Integer>();
		//�롼�׳����ѿ�->�롼����Ǥ�������ǡ��������ä��ѿ�
		HashMap outToBeforeVar = new HashMap();
		for (int i = 0; i < memvars.size(); i++) {
			beforeVar.put(memVarMap.get(memvars.get(i)), new Integer(i));
			outToBeforeVar.put(memvars.get(i), new Integer(i));
		}
		for (int i = 0; i < atomvars.size(); i++) {
			beforeVar.put(atomVarMap.get(atomvars.get(i)), new Integer(memvars.size() + i));
			outToBeforeVar.put(atomvars.get(i), new Integer(memvars.size() + i));
		}
		for (int i = 0; i < othervars.size(); i++) {
			beforeVar.put(otherVarMap.get(othervars.get(i)), new Integer(memvars.size() + atomvars.size() + i));
			outToBeforeVar.put(othervars.get(i), new Integer(memvars.size() + atomvars.size() + i));
		}

		//̿������ѹ���Ԥ�

		//�롼�������ѿ��֤������ޥå�
		//�������ά���ơ�����롼�פ��ѿ�����Ѥ�����Ρ�
		// �ֺ�������������ѿ�->����롼�׻����ͤ����äƤ����ѿ���
		HashMap atomVarMap2 = new HashMap();
		//��ε�
//		HashMap reverseAtomVarMap2 = new HashMap();
//		HashMap memVarMap2 = new HashMap();
		HashMap otherVarMap2 = new HashMap();
//		memVarMap2.put(new Integer(0), new Integer(0)); //����
//		atomVarMap.put(new Integer(firstAtom.intValue() + base), firstAtom);
		atomVarMap2.put(firstAtomInLoop, firstAtomInLoop);
//		reverseAtomVarMap2.put(firstAtom, firstAtom);

		//dereflink̿�ᤫ�顢���Ǥ˥�󥯤��Ƥ�������狼�äƤ���(atom,pos) -> (atom,pos) (������)
		HashMap<Link, Link> alreadyLinked = new HashMap<Link, Link>();

		ArrayList<Instruction> moveInsts = new ArrayList<Instruction>();
		ListIterator<Instruction> baseIterator = head.subList(2, head.size() - 1).listIterator(); //��������̿����
		ListIterator<Instruction> loopIterator = loop.listIterator(); //�롼����̿����
		while (baseIterator.hasNext()) {
			baseIterator.next();
			inst = loopIterator.next();
			switch (inst.getKind()) {
			case Instruction.DEREF:
//				Link l = (Link)links.get(new Link(inst.getIntArg2(), inst.getIntArg3()));
//				Link l = (Link)links.get(new Link(((Integer)reverseAtomVarMap.get(inst.getArg2())).intValue(), inst.getIntArg3()));
				if (atomVarMap2.containsKey(inst.getArg2())) {
					Integer a = (Integer)atomVarMap2.get(inst.getArg2());
					if (a.intValue() < memvars.size() + atomvars.size()) {
						Link l = links.get(new Link(((Integer)atomvars.get(a.intValue() - memvars.size())).intValue(), inst.getIntArg3()));
						if (l != null) {
							if (l.pos == inst.getIntArg4()) {
								//						atomVarMap2.put(inst.getArg1(), new Integer(l.atom));
								atomVarMap2.put(inst.getArg1(), outToBeforeVar.get(new Integer(l.atom)));
								//						reverseAtomVarMap2.put(new Integer(l.atom), inst.getArg1());
								loopIterator.remove();
								break; //��������ΤǸ�ν����Ϥ��ʤ�
							} else {
								//���м��Ԥ���Τǥ롼�ײ����ʤ�
//								System.out.println(inst);
								return;
							}
						}
					}
				}

//				begin
//				Integer beforeAtom = (Integer)atomVarMap.get(atomvars.get(((Integer)beforeVar.get(inst.getArg2())).intValue() - memvars.size()));
				Integer atom = (Integer)atomVarMap2.get(inst.getArg2());
				if (atom != null && atom.intValue() < memvars.size() + atomvars.size()) {
					Integer out = (Integer)atomvars.get(atom.intValue() - memvars.size());
					Link t = new Link(out.intValue(), inst.getIntArg3());
					if (inherit.containsKey(t)) { 
						Integer beforeAtom = (Integer)atomVarMap.get(out);
//						end
						if (beforeVar.get(beforeAtom).equals(atomVarMap2.get(beforeAtom))) {
							Integer t1 = (Integer)inherit.get(t);
							Integer t2 = (Integer)outToBeforeVar.get(t1);
							loopIterator.set(new Instruction(Instruction.DEREFLINK, inst.getIntArg1(), t2.intValue(), inst.getIntArg4()));

							//��Ĺ��newlink����Τ���Υǡ���
							Link l1 = new Link(inst.getIntArg1(), inst.getIntArg4());
							Link l2out = (Link)linkGetFrom.get(t1);
							Link l2 = new Link(((Integer)outToBeforeVar.get(new Integer(l2out.atom))).intValue(), l2out.pos);
							alreadyLinked.put(l1, l2);
						}
					}
				}
				break;
			case Instruction.FUNC:
				if (atomVarMap2.containsKey(inst.getArg1())) {
//					begin
//					Integer atom = (Integer)atomVarMap2.get(inst.getArg1());
					int afterchange = ((Integer)atomVarMap2.get(inst.getArg1())).intValue();
					if (afterchange < memvars.size() + atomvars.size()) {
						atom = (Integer)atomvars.get(afterchange - memvars.size());
//						end
						if (functor.containsKey(atom)) {
							if (!functor.get(atom).equals(inst.getArg2())) {
								//���м��Ԥ���Τ�ʣ����Ʊ��Ŭ�ѤϹԤ�ʤ�
//								System.out.println(inst);
								return;
							}
							//������������Τǽ���	
							loopIterator.remove();
						}
					}
				}
				break;					
			}
		}

		HashMap changeToNewlink = new HashMap(); //newlink���ѹ������� -> �����
//		HashMap changeLink = new HashMap(); //inheritlink�ΰ�ư��ȼ��������줿getlink̿�����2����������롼�׻��ѿ�����1����
		HashSet movableEnqueue = new HashSet(); //enqueue̿���롼�׸�˰�ư�Ǥ��륢�ȥ�
		baseIterator = body.listIterator(1); //��������̿����
		//loopIterator�Ϥ��ä���³��
		while (baseIterator.hasNext()) {
			Instruction baseInst = baseIterator.next();
			inst = loopIterator.next();
			switch (inst.getKind()) {
			case Instruction.GETLINK:
				//����褬�狼�äƤ���getlink�ν���
				Integer atom = (Integer)inst.getArg2();
//				Integer baseAtom;
				if (atomVarMap2.containsKey(atom)) {
					atom = (Integer)atomVarMap2.get(inst.getArg2()); //�ѿ��֤�������
//					baseAtom = (Integer)reverseAtomVarMap2.get(atom); //����롼��
					if (atom.intValue() < memvars.size() + atomvars.size()) {
						Integer baseAtom = (Integer)atomvars.get(atom.intValue() - memvars.size()); //�롼�׳��ѿ�
						//					} else {
						//						baseAtom = null;//(Integer)atomVarMap.get(atom);
						//					}
						//					if (baseAtom != null) {
						Link l = links.get(new Link(baseAtom.intValue(), inst.getIntArg3()));
						if (l != null) { //����Υ롼�פ�newlink�ˤ�äƥ���褬����Ǥ�����
							//							Integer baseAtom2 = new Integer(l.atom);
							Integer atom2 = atomVarMap.get(new Integer(l.atom));
							//							if (baseAtom.equals(atomVarMap2.get(atom)) ||
							//								baseAtom2.equals(atomVarMap2.get(atomVarMap.get(baseAtom2)))) { //����Υ롼�פ�newlink̿�᤬���������Τξ��
							Integer baseAtomInLoop = atomVarMap.get(baseAtom);
							if (beforeVar.get(baseAtomInLoop).equals(atomVarMap2.get(baseAtomInLoop)) ||
									beforeVar.get(atom2).equals(atomVarMap2.get(atom2))) { //����Υ롼�פ�newlink̿�᤬���������Τξ��
								//getlink��������inheritlink��newlink���ѹ�
								loopIterator.remove();
								changeToNewlink.put(inst.getArg1(), l);
								//��������Τ�¾�ν����ϹԤ�ʤ�
								break;
							}
						}

						//inheritlink��Ǹ�˰�ư�������ȼ��getlink�ν���
						//						if (baseAtom.equals(atomVarMap2.get(reverseAtomVarMap.get(baseAtom)))) {
						if (atom.intValue() < memvars.size() + atomvars.size()) {
							Integer beforeOutVar = (Integer)atomvars.get(atom.intValue() - memvars.size());
							Integer beforeAtom = atomVarMap.get(beforeOutVar);
							if (beforeVar.get(beforeAtom).equals(atomVarMap2.get(beforeAtom))) {
								loopIterator.remove();
								//							changeLink.put(beforeAtom, inst.getArg1());
								otherVarMap2.put(inst.getArg1(), outToBeforeVar.get(inherit.get(new Link(beforeOutVar.intValue(), inst.getIntArg3()))));
							}
						}
					}
				}
				break;
			case Instruction.INHERITLINK:
				Integer atomVar = (Integer)inst.getArg1();
//				Integer baseAtom = (Integer)reverseAtomVarMap.get(atomVar);
				Integer beforeAtom = beforeVar.get(atomVar);
				if (beforeAtom.equals(atomVarMap2.get(atomVar))) { //��Ȥ�Ȥ��ѿ��ֹ��Ʊ���ˤʤäƤ�����
					//�Ǹ�˰�ư
					moveInsts.add(baseInst);
					baseIterator.remove();
					loopIterator.remove();
//					if (changeLink.containsKey(atomVar)) {
//					otherVarMap2.put(changeLink.get(atomVar), beforeVar.get(inst.getArg1()));
//					}
				} else {
					Integer linkVar = (Integer)inst.getArg3();
					if (changeToNewlink.containsKey(linkVar)) {
						Link l = (Link)changeToNewlink.get(linkVar);
						//newlink��롼�׸�˰�ư�����Τ�ȼ��inheritlink��newlink���ѹ�
						Link l1 = new Link(inst.getIntArg1(), inst.getIntArg2());
						Link l2 = new Link(((Integer)outToBeforeVar.get(new Integer(l.atom))).intValue(), l.pos);
						//��Ĺ�ʾ��Ͻ���
						if (l1.equals(alreadyLinked.get(l2)) || l2.equals(alreadyLinked.get(l1))) {
							loopIterator.remove();
						} else {
							loopIterator.set(Instruction.newlink(l1.atom, l1.pos, l2.atom, l2.pos, inst.getIntArg5()));
						}
					}
				}
				break;
			case Instruction.NEWLINK:
				atomVar = (Integer)inst.getArg1();
				Integer atomVar2 = (Integer)inst.getArg3();
				beforeAtom = beforeVar.get(inst.getArg1());
				Integer beforeAtom2 = beforeVar.get(inst.getArg3());
				if (beforeAtom.equals(atomVarMap2.get(atomVar)) ||
						beforeAtom2.equals(atomVarMap2.get(atomVar2))) { //��Ȥ�Ȥ��ѿ��ֹ��Ʊ���ˤʤäƤ�����
					//�Ǹ�˰�ư
					moveInsts.add(baseInst);
					baseIterator.remove();
					loopIterator.remove();
					break;
				}

				//��Ĺ�ʥ�������ν���					
				Link l1 = new Link(inst.getIntArg1(), inst.getIntArg2());
				Link l2 = new Link(inst.getIntArg3(), inst.getIntArg4());
				if (l1.equals(alreadyLinked.get(l2)) || l2.equals(alreadyLinked.get(l1))) {
					loopIterator.remove();
				}
				break;
			case Instruction.DEQUEUEATOM:
				if (atomVarMap2.containsKey(inst.getArg1())) {
					atom = (Integer)atomVarMap2.get(inst.getArg1());
					if (atom.intValue() < memvars.size() + atomvars.size()) {
						loopIterator.remove();
						movableEnqueue.add(atomVarMap.get(atomvars.get(atom.intValue() - memvars.size())));
					}
				}
				break;
			case Instruction.ENQUEUEATOM:
				if (movableEnqueue.contains(inst.getArg1())) {
					loopIterator.remove();
					baseIterator.remove();
					moveInsts.add(baseInst);
				}
				break;
			}
		}
		//�롼�פκǸ��resetvars̿�������
//		int memmax = ((List)react.getArg2()).size();
//		int atommax = memmax + ((List)react.getArg3()).size();

		ArrayList memvars2 = new ArrayList();
		ArrayList atomvars2 = new ArrayList();
		ArrayList othervars2 = new ArrayList();
		for(ListIterator lit = memvars.listIterator(); lit.hasNext();){
			memvars2.add((Integer)memVarMap.get(lit.next()));
		}
		for(ListIterator lit = atomvars.listIterator(); lit.hasNext();){
			Integer var = atomVarMap.get(lit.next());
			if (atomVarMap2.containsKey(var)) {
				var = (Integer)atomVarMap2.get(var);
			}
			atomvars2.add(var);
		}
		for(ListIterator lit = othervars.listIterator(); lit.hasNext();){
			othervars2.add(otherVarMap.get(lit.next()));
		}

		loop.add(loop.size() - 1, Instruction.resetvars(memvars2, atomvars2, othervars2));

		Instruction.changeAtomVar(loop, atomVarMap2);

		//proceed̿�����������
		body.add(body.size() - 1, resetVars);
		ArrayList looparg = new ArrayList();
		looparg.add(loop);
		body.add(body.size() - 1, new Instruction(Instruction.LOOP, looparg));
		//�Ǹ��1��¹Ԥ���̿�������
		Instruction.applyVarRewriteMap(moveInsts, outToBeforeVar);
		body.addAll(body.size() - 1, moveInsts);

		//spec̿����ѹ�
		if (nextArg > spec.getIntArg1()) {
//			body.set(0, Instruction.spec(spec.getIntArg1(), nextArg));
			spec.updateSpec(spec.getIntArg1(), nextArg);
		}
	}


}