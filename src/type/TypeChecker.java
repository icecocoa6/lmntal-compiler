package type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import runtime.Env;
import runtime.functor.Functor;
import runtime.functor.IntegerFunctor;
import runtime.functor.SymbolFunctor;
import type.argument.ActiveAtomPath;
import type.argument.ArgumentInferer;
import type.argument.ConstraintSet;
import type.argument.ModeVar;
import type.argument.Path;
import type.argument.TracingPath;
import type.argument.TypeVar;
import type.argument.TypeVarConstraint;
import type.quantity.Count;
import type.quantity.FixedCounts;
import type.quantity.IntervalCount;
import type.quantity.NumCount;
import type.quantity.QuantityInferer;

import compile.structure.Atom;
import compile.structure.Atomic;
import compile.structure.Membrane;

/** */
public class TypeChecker {
	
	//��̾���Ȥˡ����줪��ӥ����ƥ��֥��ȥ�θĿ����������
	private final Map<String, Map<String, IntervalCount>> memCounts = new HashMap<String, Map<String, IntervalCount>>();
	private final Map<String, Map<Functor,IntervalCount>> functorCounts = new HashMap<String, Map<Functor, IntervalCount>>();
	
	// �����ƥ��֥��ȥ�η�����
	private final Map<String, Map<Functor, List<ModedType>>> activeAtomTypes = new HashMap<String, Map<Functor, List<ModedType>>>();
	// �ǡ������ȥ�η����� (?)
	private final Map<Functor, List<ModedType>> dataAtomTypes = new HashMap<Functor, List<ModedType>>();
	
	private final Set<String> nomores = new HashSet<String>();
	
	/**
	 * �ǡ����������ѡ�������
	 * @param atom datatype���ȥ�
	 * @throws TypeParseException
	 */
	private void parseDatatypeAtom(Atom atom)throws TypeParseException{
		Functor f = atom.functor;
		Atomic typeatomic = TypeEnv.getRealBuddy(atom.args[f.getArity()-1]).atom;
		if(!(typeatomic instanceof Atom))
			throw new TypeParseException("context appearing in type definition.");

		String typename = typeatomic.getName();
		for(int i=0;i<f.getArity()-1;i++){
			
			Atomic dataatomic = TypeEnv.getRealBuddy(atom.args[i]).atom;
			if(!(dataatomic instanceof Atom))
				throw new TypeParseException("context appearing in type definition.");
			Atom dataatom = (Atom)dataatomic;

			List<ModedType> types = new ArrayList<ModedType>(dataatom.getArity()-1);
			boolean flgRegistered = false;
			for(int j=0;j<dataatom.getArity()-1;j++){
				Atomic signatomic = TypeEnv.getRealBuddy(dataatom.args[j]).atom;
				if(!(signatomic instanceof Atom))
					throw new TypeParseException("context appearing in type definition.");
				Atom signatom = (Atom)signatomic;
				if(signatom.getName().equals("+")){
					Set<String> datanames = new HashSet<String>();
					for(int k=0;k<signatom.getArity()-1;k++){
						Atomic signedatomic = TypeEnv.getRealBuddy(signatom.args[k]).atom;
						String dataname = signedatomic.getName();
						if(!datanames.contains(dataname))datanames.add(dataname);
					}
					types.add(j, new ModedType(datanames, 1));
				}
//				else if(signatom.getName().equals("-")){
//					Set<String> datanames = new HashSet<String>();
//					for(int k=0;k<signatom.getArity()-1;k++){
//						Atomic signedatomic = signatom.args[0].atom;
//						String dataname = signedatomic.getName();
//						if(!datanames.contains(dataname))datanames.add(dataname);
//					}
//					types.add(i, new ModedType(datanames, -1));
//				}
				else if(signatom.getName().equals("*")){
					Set<String> datanames = new HashSet<String>();
					for(int k=0;k<signatom.getArity()-1;k++){
						Atomic signedatomic = TypeEnv.getRealBuddy(signatom.args[k]).atom;
						String dataname = signedatomic.getName();
						if(!datanames.contains(dataname))datanames.add(dataname);
					}
					types.add(j, new ModedType(datanames, 0));
				}
//				if(signatom.functor.equals(new SymbolFunctor("+",2))){
//					Atomic signedatomic = TypeEnv.getRealBuddy(signatom.args[0]).atom;
//					if(!(signedatomic instanceof Atom))
//						throw new TypeParseException("context appearing in type definition.");
//					String dataname = signedatomic.getName();
//					types.add(j, new ModedType(dataname, 1));
//				}
				else if(signatom.functor.equals(new SymbolFunctor("-",1))){
					if(flgRegistered)
						throw new TypeParseException("data atom must have only one output argument.");
					TypeEnv.registerDataFunctor(new SymbolFunctor(dataatom.functor.getName(),dataatom.functor.getArity()-1),typename,j);
					flgRegistered = true;
					types.add(j, null);
				}
				else throw new TypeParseException("data atom must have sign atom : " + dataatom.functor +" -> " + signatom.functor);
			}
			if(!flgRegistered)
				throw new TypeParseException("datatype atom must have output sign.");
			addDataAtomType(new SymbolFunctor(dataatom.getName(),dataatom.getArity()-1), types);
		}
	}
	
	/**
	 * ���������ɤ߹���Ƿ����������
	 * @param typedefmem
	 */
	public boolean parseTypeDefinition(List<Membrane> typedefmems){
		
		for(Membrane typedefmem : typedefmems){
			try{
				for(Atom topatom : typedefmem.atoms){
					if(topatom.getName().equals("datatype")){
						parseDatatypeAtom(topatom);
					}
				}
				for(Membrane mem : typedefmem.mems){
					String memname = TypeEnv.getMemName(mem);
					for(Atom atom : mem.atoms){
						Functor f = atom.functor;
						if(f.getName().equals("datatype")){
							parseDatatypeAtom(atom);
						}
						else if(f.equals(new SymbolFunctor(".",3))){
							Atomic lastatomic = TypeEnv.getRealBuddy(atom.args[2]).atom;
							if(!(lastatomic instanceof Atom))
								throw new TypeParseException("context appearing in type definition");
							Atom lastatom = (Atom)lastatomic;
							Functor lastf = lastatom.functor;
							if(lastf.equals(new SymbolFunctor(".", 3)))
								continue;
							else if(lastf.equals(new SymbolFunctor("+", 1)) && lastatom.mem.parent == mem){ // ����ʤ�
								String childname = TypeEnv.getMemName(lastatom.mem); // 0�����ܤ���($in)��1�����ܤ���Υ��ȥ�ν�°��
								IntervalCount fc = getCountFromList(atom);
								addChildCount(memname, childname, fc);
							}
							else{ // �����ƥ��֥��ȥ�
								constrainActiveAtomArgument(memname, lastatom);
								IntervalCount fc = getCountFromList(atom);
								addFunctorCount(memname, new SymbolFunctor(lastf.getName(),lastf.getArity()-1),fc);
							}
						}
						else if(f instanceof IntegerFunctor){
							Atomic actatomic = TypeEnv.getRealBuddy(atom.args[0]).atom;
							if(!(actatomic instanceof Atom))
								throw new TypeParseException("context appearing in type definition");
							Atom lastatom = (Atom)actatomic;
							Functor lastf = lastatom.functor;
							int count = ((IntegerFunctor)f).intValue();
							if(lastf.equals(new SymbolFunctor(".",3)))
								continue;
							else if(lastf.equals(new SymbolFunctor("+",1)) && lastatom.mem.parent == mem){ // ����ʤ�
								String childname = TypeEnv.getMemName(lastatom.mem); // 0�����ܤ���($in)��1�����ܤ���Υ��ȥ�ν�°��
								IntervalCount fc = new IntervalCount(count,count);//new NumCount(count);
								addChildCount(memname, childname, fc);
							}
							else{ //�����ƥ��֥��ȥ�
								constrainActiveAtomArgument(memname, lastatom);
								IntervalCount fc = new IntervalCount(count,count);//new NumCount(count);
								addFunctorCount(memname, new SymbolFunctor(lastf.getName(),lastf.getArity()-1),fc);
							}
						}
						// "nomore" ̤������ȥ�νи��ػߥե饰
						else if(f.equals(new SymbolFunctor("nomore",0))){
							nomores.add(TypeEnv.getMemName(mem));
						}
					}
				}
	//			printTypeDefinitions();
			}catch(TypeParseException e){
				e.printError();
				return false;
			}
		}
		return true;
	}
	
	private void printTypeDefinitions(){
		for(String memname : memCounts.keySet()){
			Env.p(memname + "{");
			Map<String, IntervalCount> mtof = memCounts.get(memname);
			for(String child : mtof.keySet()){
				Env.p("\t" + child + " = " + mtof.get(child));
			}
			Env.p("}");
		}
		for(String memname : functorCounts.keySet()){
			Env.p(memname + "{");
			Map<Functor, IntervalCount> ftof = functorCounts.get(memname);
			for(Functor f : ftof.keySet()){
				Env.p("\t" + f + " = " + ftof.get(f));
			}
			Env.p("}");
		}
	}
	
	private void constrainActiveAtomArgument(String memname, Atom atom)throws TypeParseException{
		List<ModedType> types = new ArrayList<ModedType>(atom.getArity()-1);
		for(int i=0;i<atom.getArity()-1;i++){
			Atomic signatomic = atom.args[i].buddy.atom;
			if(!(signatomic instanceof Atom))
				throw new TypeParseException("context appearing in type definition.");
			Atom signatom = (Atom)signatomic;
			if(signatom.getName().equals("+")){
				Set<String> datanames = new HashSet<String>();
				for(int j=0;j<signatom.getArity()-1;j++){
					Atomic signedatomic = TypeEnv.getRealBuddy(signatom.args[j]).atom;
					String dataname = signedatomic.getName();
					if(!datanames.contains(dataname))datanames.add(dataname);
				}
				types.add(i, new ModedType(datanames, 1));
			}
			else if(signatom.getName().equals("-")){
				Set<String> datanames = new HashSet<String>();
				for(int j=0;j<signatom.getArity()-1;j++){
					Atomic signedatomic = TypeEnv.getRealBuddy(signatom.args[j]).atom;
					String dataname = signedatomic.getName();
					if(!datanames.contains(dataname))datanames.add(dataname);
				}
				types.add(i, new ModedType(datanames, -1));
			}
			else if(signatom.getName().equals("*")){
				Set<String> datanames = new HashSet<String>();
				for(int j=0;j<signatom.getArity()-1;j++){
					Atomic signedatomic = TypeEnv.getRealBuddy(signatom.args[j]).atom;
					String dataname = signedatomic.getName();
					if(!datanames.contains(dataname))datanames.add(dataname);
				}
				types.add(i, new ModedType(datanames, 0));
			}
			else throw new TypeParseException("active atom must have signed data atom : " + atom.functor + " -> " + signatom.functor);
		}
		addActiveAtomType(memname, new SymbolFunctor(atom.functor.getName(), atom.functor.getArity()-1), types);
	}
	
	/**
	 * 2���ǤΥꥹ�Ȥ������ͤ�����
	 * @param firstcons
	 * @return
	 * @throws TypeParseException
	 */
	private static IntervalCount getCountFromList(Atom firstcons)throws TypeParseException{
		Atomic atomic1 = firstcons.args[0].buddy.atom;
		if(!(atomic1 instanceof Atom))
			throw new TypeParseException("context appearing in type definition.");
		Atom atom1 = (Atom)atomic1;
		if(!(atom1.functor instanceof IntegerFunctor))
			throw new TypeParseException("1st element of interval is not integer.");
		int min = ((IntegerFunctor)atom1.functor).intValue();
		Atomic consatomic = firstcons.args[1].buddy.atom;
		if(!(consatomic instanceof Atom))
			throw new TypeParseException("context appearing in type definition.");
		Atom secondcons = (Atom)consatomic;
		if(!secondcons.functor.equals(new SymbolFunctor(".",3)))
			throw new TypeParseException("length of interval list must be over 2.");
		Atomic atomic2 = secondcons.args[0].buddy.atom;
		if(!(atomic2 instanceof Atom))
			throw new TypeParseException("context appearing in type definition.");
		Atom atom2 = (Atom)atomic2;
		if(atom2.functor instanceof IntegerFunctor){
			int max = ((IntegerFunctor)atom2.functor).intValue();
			return new IntervalCount(min, max);
		}
		else if(atom2.functor.equals(new SymbolFunctor("inf",1))){
			return new IntervalCount(new NumCount(min),Count.INFINITY);
		}
		else
			throw new TypeParseException("2nd element of interval is not integer or inf.");
	}
	
	private void addActiveAtomType(String memname, Functor functor, List<ModedType> types)throws TypeParseException{
		if(!activeAtomTypes.containsKey(memname))
			activeAtomTypes.put(memname, new HashMap<Functor, List<ModedType>>());
		Map<Functor, List<ModedType>> functorToTypes = activeAtomTypes.get(memname);
		if(!functorToTypes.containsKey(functor))
			functorToTypes.put(functor, types);
		else{
			throw new TypeParseException("two atom type definition about same active atom.");
		}
	}
	
	private void addDataAtomType(Functor functor, List<ModedType> types)throws TypeParseException{
		if(!dataAtomTypes.containsKey(functor))
			dataAtomTypes.put(functor, types);
		else{
			throw new TypeParseException("two atom type definition about same data atom.");
		}
	}
	
	/**
	 * ����θĿ�������ɲä���
	 * @param parentname ��°��̾
	 * @param childname ����̾
	 * @param fc �Ŀ�����
	 * @throws TypeParseException Ʊ����̾�ˤĤ���2�ĸĿ�������ɲä��褦�Ȥ���ȥ��顼
	 */
	private void addChildCount(String parentname, String childname, IntervalCount fc)throws TypeParseException{
		if(!memCounts.containsKey(parentname))
			memCounts.put(parentname, new HashMap<String, IntervalCount>());
		Map<String, IntervalCount> counts = memCounts.get(parentname);
		if(!counts.containsKey(childname))
			counts.put(childname, fc);
		else{
			throw new TypeParseException("two descriptions about same membrane name.");
		}
	}
	
	/**
	 * �����ƥ��֥��ȥ�θĿ�������ɲä���
	 * @param parentname ��°��
	 * @param functor �����ƥ��֥��ȥ�Υե��󥯥�
	 * @param fc �Ŀ�����
	 * @throws TypeParseException Ʊ�������ƥ��֥��ȥ�ˤĤ���2�ĸĿ�������ɲä��褦�Ȥ���ȥ��顼
	 */
	private void addFunctorCount(String parentname, Functor functor, IntervalCount fc)throws TypeParseException{
		if(!functorCounts.containsKey(parentname))
			functorCounts.put(parentname, new HashMap<Functor, IntervalCount>());
		Map<Functor, IntervalCount> counts = functorCounts.get(parentname);
		if(!counts.containsKey(functor))
			counts.put(functor, fc);
		else{
			throw new TypeParseException("two descriptions about same active atom.");
		}
	}
	
	/**
	 * �桼����Ϳ����������ȿ�����̤ȤΡ�������������å�����
	 * @throws TypeException
	 */
	public void check(ArgumentInferer ai, QuantityInferer qi)throws TypeException{
		//�ޤ������η���������
		ConstraintSet cs = ai.getConstraints();
		Set<TypeVarConstraint> tvcs = cs.getTypeVarConstraints();
		for(TypeVarConstraint tvc : tvcs){
			Path p = tvc.getPath();
			if(p instanceof ActiveAtomPath){
				ActiveAtomPath aap = (ActiveAtomPath)p;
				String memname = aap.getMemName();
				Functor f = aap.getFunctor();
				if(!activeAtomTypes.containsKey(memname))continue;
				Map<Functor, List<ModedType>> fToTypes = activeAtomTypes.get(memname);
				if(!fToTypes.containsKey(f))continue;
				List<ModedType> types = fToTypes.get(f);
				int pos = aap.getPos();
				ModedType mt = types.get(pos);
				ModeVar mv = tvc.getModeVar();
				if(mv.value == 0)mv.bindSign(mt.sign); // �⤷�⡼���ѿ���̤����ʤ���ꤹ��
				else if(mv.value != mt.sign)
					throw new TypeException("mode error : " + mt.sign + "(user def) <=> " + mv.value + "(infered)");
				TypeVar tv = tvc.getTypeVar();
				Set<String> typeNames = tv.getTypeName(); // �ǡ�����̾���������
				if(typeNames == null){ // �ǡ�������̤��
					tv.setTypeName(mt.typenames);
				}
				else if(!checkDataTypes(mt.typenames,typeNames))//typeName.equals(mt.typename))
					throw new TypeException("type error : " + f + "/" + pos + ":" + mt.typenames + "(user def) <=> " + typeNames + "(infered)");
			}
			else if(p instanceof TracingPath){
				TracingPath tp = (TracingPath)p;
				Functor f = tp.getFunctor();
				int pos = tp.getPos();
				if(!dataAtomTypes.containsKey(f))continue;
				List<ModedType> types = dataAtomTypes.get(f);
				ModedType mt = types.get(pos);
				ModeVar mv = tvc.getModeVar();
				ActiveAtomPath ap = getTracedRoot(tp);
				Functor af = ap.getFunctor();
				Map<Functor,List<ModedType>> f2ts = activeAtomTypes.get(ap.getMemName());
				if(f2ts == null)continue;
				if(!f2ts.containsKey(af))continue;
				List<ModedType> amts = f2ts.get(af);
				ModedType amt = amts.get(ap.getPos());
				//����ã��Ȥ�ActiveAtomPath�Υ⡼�ɤˤ��դˤʤ�
				if(amt.sign == -1){
					if(mv.value == 0)mv.bindSign(-mt.sign); // �⤷�⡼���ѿ���̤����ʤ���ꤹ��
					else if(mv.value != -mt.sign)
						throw new TypeException("mode error : " + f + "/" + pos + " : " + mt.sign + "(user def) <=> " + mv.value + "(infered)");
				}
				else if(amt.sign == 1){
					if(mv.value == 0)mv.bindSign(mt.sign); // �⤷�⡼���ѿ���̤����ʤ���ꤹ��
					else if(mv.value != mt.sign)
						throw new TypeException("mode error : " + f + "/" + pos + " : " + mt.sign + "(user def) <=> " + mv.value + "(infered)");
				}else{ // �롼�Ȥ������ξ�硢�ä˥����å����ʤ�
					continue;
				}
				TypeVar tv = tvc.getTypeVar();
				Set<String> typeNames = tv.getTypeName(); // �ǡ�����̾���������
				if(typeNames == null){ // �ǡ�������̤��
					tv.setTypeName(mt.typenames);
				}
				else if(!checkDataTypes(mt.typenames,typeNames))
					throw new TypeException("type error : " + f + "/" + pos + " : " + mt.typenames + "(user def) <=> " + typeNames + "(infered)");
			}
			else{ // RootPath �ΤޤޤˤʤäƤ��뤳�Ȥ⤢�ꤦ�롣
				// TODO ArgumentInferer.getPolarizedPath��1�ļ����ޤ�trace���롣
				//
//				Env.p("fatal error : RootPath");
			}
		}
		//���˸Ŀ��θ����򤹤�
		Map<String, FixedCounts> memnameToCounts = qi.getMemNameToFixedCountsSet();
		for(String memname : memnameToCounts.keySet()){
			FixedCounts fcs = memnameToCounts.get(memname);
			for(Functor f : fcs.functorToCount.keySet()){
				if(TypeEnv.outOfPassiveFunctor(f)!=TypeEnv.ACTIVE)continue;
				if(!functorCounts.containsKey(memname))break;
				Map<Functor, IntervalCount> fToC = functorCounts.get(memname);
				if(!fToC.containsKey(f)){
					if(nomores.contains(memname)){
						throw new TypeException("undefined atom occurs. : " + f);
					}
					else continue;
				}
				IntervalCount fc = fToC.get(f);
				checkCount(f.toString(), fc,fcs.functorToCount.get(f));
			}
			for(String childname : fcs.memnameToCount.keySet()){
				if(!memCounts.containsKey(memname))break;
				Map<String, IntervalCount> mToC = memCounts.get(memname);
				if(!mToC.containsKey(childname))continue;
				IntervalCount fc = mToC.get(childname);
				checkCount(childname, fc, fcs.memnameToCount.get(childname));
			}
		}
	}
	
	private ActiveAtomPath getTracedRoot(TracingPath tp){
		Path p = tp.getPath();
		if(p instanceof ActiveAtomPath){
			return (ActiveAtomPath)p;
		}
		else return getTracedRoot((TracingPath)p);
	}
	
	/**
	 * $con���$inf�����ޤäƤ��뤳�Ȥ��ǧ����
	 * @param con
	 * @param inf
	 * @return
	 */
	private boolean checkDataTypes(Set<String> con, Set<String> inf){
		for(String dname : inf){
			if(!con.contains(dname)){
				return false;
			}
		}
		return true;
	}
	
	private void checkCount(String s, IntervalCount constraint, IntervalCount infered)throws TypeException{
//		if(constraint instanceof InfinityCount){
//			throw new TypeException("fatal error : infinity is given as definition.");
////			if(!(infered instanceof InfinityCount))
////				errorCount(constraint, infered);
////			InfinityCount ci = (InfinityCount)constraint;
////			InfinityCount ii = (InfinityCount)infered;
////			if(ci.minus != ii.minus)
////				errorCount(constraint, infered);
//		}
//		else if(constraint instanceof NumCount){
//			if(!(infered instanceof NumCount))
//				errorCount(s, constraint, infered);
//			NumCount cn = (NumCount)constraint;
//			NumCount in = (NumCount)infered;
//			if(cn.value != in.value)
//				errorCount(s, constraint, infered);
//		}
//		else{
			IntervalCount ci = (IntervalCount)constraint;
//			if(infered instanceof InfinityCount){
//				InfinityCount ii = (InfinityCount)infered;
//				if(!ii.minus){
//					if(ci.max instanceof InfinityCount){
//						if(((InfinityCount)ci.max).minus)
//							errorCount(s, constraint, infered);
//					}
//					else
//						errorCount(s, constraint, infered);
//				}
//				else{
//					throw new TypeException("fatal error : -inf infered.");
//				}
//			}
//			else if(infered instanceof NumCount){
//				NumCount in = (NumCount)infered;
//				if(ci.min.compare(in) > 0 || ci.max.compare(in) < 0)
//					errorCount(s, constraint, infered);
//			}
//			else{
				IntervalCount ii = (IntervalCount)infered;
				if(ci.min.compare(ii.min) > 0 || ci.max.compare(ii.max) < 0)
					errorCount(s, constraint, infered);
//			}
//		}
	}
	
	private void errorCount(String s, IntervalCount constraint, IntervalCount infered)throws TypeException{
		throw new TypeException("count error : " + s + " : " + constraint + "(user def) <=> " + infered + "(infered)");
	}
	
}

class ModedType{
	public Set<String> typenames;
	public int sign;
	public ModedType(Set<String> typenames, int sign) {
		this.typenames = typenames;
		this.sign = sign;
	}
}

class TypeParseException extends Throwable{

	/** */
	private static final long serialVersionUID = 1L;
	
	private final String message;
	
	public TypeParseException(String message){
		this.message = message;
	}
	
	public void printError(){
		Env.p("TYPE DEFINITION ERROR : " + message);
	}
	
}