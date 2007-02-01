package type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import runtime.FloatingFunctor;
import runtime.Functor;
import runtime.IntegerFunctor;
import runtime.ObjectFunctor;
import runtime.StringFunctor;
import runtime.SymbolFunctor;

import compile.structure.Atom;
import compile.structure.ContextDef;
import compile.structure.LinkOccurrence;
import compile.structure.Membrane;
import compile.structure.ProcessContext;
import compile.structure.RuleStructure;

public final class TypeEnv {

	public static final int ACTIVE = -1;

	public static final int CONNECTOR = -2;
	
	private static final Map<Functor, Integer> functorToOut = new HashMap<Functor, Integer>();
	private static final Map<Functor, String> functorToTypeName = new HashMap<Functor, String>();

	static{
		functorToOut.put(Functor.UNIFY, CONNECTOR);
		functorToOut.put(Functor.INSIDE_PROXY, CONNECTOR);
		functorToOut.put(Functor.OUTSIDE_PROXY, CONNECTOR);
		/* Passive atom */
		/* List atom */
		functorToOut.put(new SymbolFunctor(".",3), 2);
		functorToOut.put(new SymbolFunctor("[]",1), 0);
		/* Boolean atom */
		functorToOut.put(new SymbolFunctor("true",1), 0);
		functorToOut.put(new SymbolFunctor("false",1), 0);
	}
	
	public static void registerDataFunctor(Functor f, String dataname, int out){
		functorToOut.put(f, out);
		functorToTypeName.put(f, dataname);
	}
	
	public static int outOfPassiveFunctor(Functor f){
		/* Special atom */
		if (f instanceof IntegerFunctor)
			return 0;
		if (f instanceof FloatingFunctor)
			return 0;
		if (f instanceof StringFunctor)
			return 0;
		if (f instanceof ObjectFunctor)
			return 0;
		if (functorToOut.containsKey(f))
			return ((Integer)functorToOut.get(f)).intValue();
		return ACTIVE;
	}

	public static int outOfPassiveAtom(Atom atomic) {
//		if(atomic instanceof Atom){
			Atom atom = (Atom)atomic;
			Functor f = atom.functor;
			return outOfPassiveFunctor(f);
//		}
//		else(atomic instanceof ProcessContext){
//			ProcessContext pc = (ProcessContext)atomic;
//			String dt = def2type.get(pc.def);
//			if(dt == null)return PC;
//		}
	}
	
	static{
		functorToTypeName.put(new SymbolFunctor(".",3),"list");
		functorToTypeName.put(new SymbolFunctor("[]",1),"list");
		functorToTypeName.put(new SymbolFunctor("true",1),"bool");
		functorToTypeName.put(new SymbolFunctor("false",1),"bool");
	}
	public static String getTypeNameOfPassiveFunctor(Functor f){
		/* Special Atom */
		if(f instanceof IntegerFunctor)
			return "int";
		if(f instanceof FloatingFunctor)
			return "float";
		if(f instanceof StringFunctor)
			return "string";
		if(f instanceof ObjectFunctor)
			return "java-object";
		if (functorToTypeName.containsKey(f))
			return (String)functorToTypeName.get(f);
		else return null;
	}

	/** �����줪��Ӻ��սи���ν��� */
	private static final Set<Membrane> lhsmems = new HashSet<Membrane>();

	private static final Map<Membrane, String> memToName = new HashMap<Membrane, String>();
	
	public static void initialize(Membrane root)throws TypeException{
		// ���Ƥ���ˤĤ��ơ��롼��κ��պǳ����и����ɤ����ξ��������
		TypeEnv.collectLHSMemsAndNames(root.rules);
		
		// ���Ƥη��դ��ץ���ʸ̮�ˤĤ��ơ��ץ��������ΤäƤ���
		// TODO �����쥬���ɥ���ѥ������Ω�Ƥ�
		knowTPCMem(root);
		
		// ���Ƥα�����ˤĤ��ơ�
	}
	
	/** ���դ��ץ���ʸ̮��� -> �ǡ�����*/
	private static Map<ContextDef, Functor> def2type = new HashMap<ContextDef, Functor>();
	
	public static Functor dataTypeOfContextDef(ContextDef cd){
		return def2type.get(cd);
	}
	
	private static void knowTPCMem(Membrane mem)throws TypeException{
		for(RuleStructure rs : mem.rules){
			knowTPCGuard(rs.guardMem);
		}
	}
	
	private static void knowTPCGuard(Membrane guardmem)throws TypeException{
		Functor intf = new IntegerFunctor(0);
		Functor floatf = new FloatingFunctor(0.0);
		Functor stringf = new StringFunctor("hello");
		Functor classf = new ObjectFunctor(new Object());
		for(Atom guard : guardmem.atoms){
			if(guard.functor.equals(new SymbolFunctor("int",1))){
				ProcessContext tpc = (ProcessContext)guard.args[0].buddy.atom;
				constrainTPC(tpc.def, intf);
			}
			else if(guard.functor.equals(new SymbolFunctor("float",1))){
				ProcessContext tpc = (ProcessContext)guard.args[0].buddy.atom;
				constrainTPC(tpc.def, floatf);
			}
			else if(guard.functor.equals(new SymbolFunctor("string",1))){
				ProcessContext tpc = (ProcessContext)guard.args[0].buddy.atom;
				constrainTPC(tpc.def, stringf);
			}
			else if(guard.functor.equals(new SymbolFunctor("class",2))){
				ProcessContext tpc = (ProcessContext)guard.args[0].buddy.atom;
				constrainTPC(tpc.def, classf);
			}
			else if(
				guard.functor.equals(new SymbolFunctor("=:=",2)) ||
				guard.functor.equals(new SymbolFunctor("=\\=",2)) ||
				guard.functor.equals(new SymbolFunctor(">",2)) ||
				guard.functor.equals(new SymbolFunctor("<",2)) ||
				guard.functor.equals(new SymbolFunctor(">=",2)) ||
				guard.functor.equals(new SymbolFunctor("=<",2))){
				ProcessContext tpc = (ProcessContext)guard.args[0].buddy.atom;
				constrainTPC(tpc.def, intf);
				tpc = (ProcessContext)guard.args[1].buddy.atom;
				constrainTPC(tpc.def, intf);
			}
			else if(
			guard.functor.equals(new SymbolFunctor("+",3)) ||
			guard.functor.equals(new SymbolFunctor("-",3)) ||
			guard.functor.equals(new SymbolFunctor("*",3)) ||
			guard.functor.equals(new SymbolFunctor("/",3)) ||
			guard.functor.equals(new SymbolFunctor("mod",3))
			){
				ProcessContext tpc = (ProcessContext)guard.args[0].buddy.atom;
				constrainTPC(tpc.def, intf);
				tpc = (ProcessContext)guard.args[1].buddy.atom;
				constrainTPC(tpc.def, intf);
				tpc = (ProcessContext)guard.args[2].buddy.atom;
				constrainTPC(tpc.def, intf);
			}
			else if(
			guard.functor.equals(new SymbolFunctor("=:=.",2)) ||
			guard.functor.equals(new SymbolFunctor("=\\=.",2)) ||
			guard.functor.equals(new SymbolFunctor(">.",2)) ||
			guard.functor.equals(new SymbolFunctor("<.",2)) ||
			guard.functor.equals(new SymbolFunctor(">=.",2)) ||
			guard.functor.equals(new SymbolFunctor("=<.",2))){
				ProcessContext tpc = (ProcessContext)guard.args[0].buddy.atom;
				constrainTPC(tpc.def, floatf);
				tpc = (ProcessContext)guard.args[1].buddy.atom;
				constrainTPC(tpc.def, floatf);
			}
			else if(
			guard.functor.equals(new SymbolFunctor("+.",3)) ||
			guard.functor.equals(new SymbolFunctor("-.",3)) ||
			guard.functor.equals(new SymbolFunctor("*.",3)) ||
			guard.functor.equals(new SymbolFunctor("/.",3))
			){
				ProcessContext tpc = (ProcessContext)guard.args[0].buddy.atom;
				constrainTPC(tpc.def, floatf);
				tpc = (ProcessContext)guard.args[1].buddy.atom;
				constrainTPC(tpc.def, floatf);
				tpc = (ProcessContext)guard.args[2].buddy.atom;
				constrainTPC(tpc.def, floatf);
			}
		}
		for(Atom guard : guardmem.atoms){
			if(
				guard.functor.equals(new SymbolFunctor("=",2))||
				guard.functor.equals(new SymbolFunctor("==",2))){
				ProcessContext tpc1 = (ProcessContext)guard.args[0].buddy.atom;
				ProcessContext tpc2 = (ProcessContext)guard.args[0].buddy.atom;
				if(def2type.get(tpc1.def)!=null){
					constrainTPC(tpc2.def,def2type.get(tpc1.def));
				}
				else if(def2type.get(tpc2.def)!=null){
					constrainTPC(tpc1.def,def2type.get(tpc2.def));
				}
			}
		}
	}
	
	private static void constrainTPC(ContextDef cd, Functor datatype)throws TypeException{
		Functor df = def2type.get(cd);
		if(df == null)def2type.put(cd, datatype);
		else if(df.equals(datatype))return;
		else throw new TypeException("Typed Process Context is constrained two process type. : " + cd.getName());
	}
	
	
	/**
	 * ���սи����$lhsmems����Ͽ����
	 * �������̾���°�����̾�Ȥ���
	 * @param mem
	 */
	private static void collectLHSMemsAndNames(List<RuleStructure> rules){
		for(RuleStructure rule : rules)
			collectLHSMemsAndNames(rule);
	}
	/**
	 * ���սи����$lhsmems����Ͽ����
	 * @param rule
	 */
	private static void collectLHSMemsAndNames(RuleStructure rule){
		collectLHSMem(rule.leftMem);
		memToName.put(rule.leftMem, rule.parent.name);
		memToName.put(rule.rightMem, rule.parent.name);
//		 ���դ˥롼��Ͻи����ʤ�
		for(RuleStructure rhsrule : ((List<RuleStructure>)rule.rightMem.rules))
			collectLHSMemsAndNames(rhsrule);
	}
	/**
	 * ���սи����$lhsmems����Ͽ����
	 * @param mem ���սи���
	 */
	private static void collectLHSMem(Membrane mem){
		lhsmems.add(mem);
		for(Membrane cmem : ((List<Membrane>)mem.mems))
			collectLHSMem(cmem);
	}
	
	/** ���դΥ��ȥफ�ɤ������֤� */
	public static boolean isLHSAtom(Atom atom) {
		return isLHSMem(atom.mem);
	}

	/** ���դ��줫�ɤ������֤� */
	public static boolean isLHSMem(Membrane mem) {
		return lhsmems.contains(mem);
	}

	/**
	 * get real buddy through =/2, $out, $in
	 * 
	 * @param lo
	 * @return
	 */
	public static LinkOccurrence getRealBuddy(LinkOccurrence lo) {
		if (lo.buddy.atom instanceof Atom) {
			Atom a = (Atom) lo.buddy.atom;
			int o = TypeEnv.outOfPassiveAtom(a);
			if (o == TypeEnv.CONNECTOR)
				return getRealBuddy(a.args[1 - lo.buddy.pos]);
			else
				return lo.buddy;
		} else
			return lo.buddy;
	}
	
	
	public static final String ANNONYMOUS = "??";
	/**
	 * �롼�������ˤĤ��ƤϽ�°���̾�����֤�
	 */
	public static String getMemName(Membrane mem){
		String registered = memToName.get(mem);
		if(registered == null){
			if(mem.name == null)return ANNONYMOUS;
			else return mem.name;
		}
		else return registered;
	}

}
