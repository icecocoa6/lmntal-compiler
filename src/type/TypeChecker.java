package type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import runtime.Env;
import runtime.Functor;
import runtime.IntegerFunctor;
import runtime.SymbolFunctor;
import type.argument.ActiveAtomPath;
import type.argument.ArgumentInferer;
import type.argument.ConstraintSet;
import type.argument.ModeVar;
import type.argument.Path;
import type.argument.TracingPath;
import type.argument.TypeVar;
import type.argument.TypeVarConstraint;
import type.quantity.Count;
import type.quantity.FixedCount;
import type.quantity.FixedCounts;
import type.quantity.InfinityCount;
import type.quantity.IntervalCount;
import type.quantity.NumCount;
import type.quantity.QuantityInferer;

import compile.structure.Atom;
import compile.structure.Atomic;
import compile.structure.Membrane;

/** */
public class TypeChecker {
	
	//��̾���Ȥˡ����줪��ӥ����ƥ��֥��ȥ�θĿ����������
	private final Map<String, Map<String, FixedCount>> memCounts = new HashMap<String, Map<String, FixedCount>>();
	private final Map<String, Map<Functor,FixedCount>> functorCounts = new HashMap<String, Map<Functor,FixedCount>>();
	
	// �����ƥ��֥��ȥ�η�����
	private final Map<String, Map<Functor, List<ModedType>>> activeAtomTypes = new HashMap<String, Map<Functor, List<ModedType>>>();
	// �ǡ������ȥ�η����� (?)
	private final Map<Functor, List<ModedType>> dataAtomTypes = new HashMap<Functor, List<ModedType>>();
	
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
				if(signatom.functor.equals(new SymbolFunctor("+",2))){
					Atomic signedatomic = TypeEnv.getRealBuddy(signatom.args[0]).atom;
					if(!(signedatomic instanceof Atom))
						throw new TypeParseException("context appearing in type definition.");
					String dataname = signedatomic.getName();
					types.add(j, new ModedType(dataname, 1));
				}
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
			addDataAtomType(dataatom.functor, types);
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
								FixedCount fc = getCountFromList(atom);
								addChildCount(memname, childname, fc);
							}
							else{ // �����ƥ��֥��ȥ�
								constrainActiveAtomArgument(memname, lastatom);
								FixedCount fc = getCountFromList(atom);
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
								FixedCount fc = new NumCount(count);
								addChildCount(memname, childname, fc);
							}
							else{ //�����ƥ��֥��ȥ�
								constrainActiveAtomArgument(memname, lastatom);
								FixedCount fc = new NumCount(count);
								addFunctorCount(memname, new SymbolFunctor(lastf.getName(),lastf.getArity()-1),fc);
							}
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
			Map<String, FixedCount> mtof = memCounts.get(memname);
			for(String child : mtof.keySet()){
				Env.p("\t" + child + " = " + mtof.get(child));
			}
			Env.p("}");
		}
		for(String memname : functorCounts.keySet()){
			Env.p(memname + "{");
			Map<Functor, FixedCount> ftof = functorCounts.get(memname);
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
			if(signatom.functor.equals(new SymbolFunctor("+",2))){
				Atomic signedatomic = signatom.args[0].atom;
				if(!(signedatomic instanceof Atom))
					throw new TypeParseException("context appearing in type definition.");
				String dataname = signedatomic.getName();
				types.add(i, new ModedType(dataname, 1));
			}
			else if(signatom.functor.equals(new SymbolFunctor("-",2))){
				Atomic signedatomic = signatom.args[0].atom;
				if(!(signedatomic instanceof Atom))
					throw new TypeParseException("context appearing in type definition.");
				String dataname = signedatomic.getName();
				types.add(i, new ModedType(dataname, -1));
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
	private static FixedCount getCountFromList(Atom firstcons)throws TypeParseException{
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
	private void addChildCount(String parentname, String childname, FixedCount fc)throws TypeParseException{
		if(!memCounts.containsKey(parentname))
			memCounts.put(parentname, new HashMap<String, FixedCount>());
		Map<String, FixedCount> counts = memCounts.get(parentname);
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
	private void addFunctorCount(String parentname, Functor functor, FixedCount fc)throws TypeParseException{
		if(!functorCounts.containsKey(parentname))
			functorCounts.put(parentname, new HashMap<Functor, FixedCount>());
		Map<Functor, FixedCount> counts = functorCounts.get(parentname);
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
				String typeName = tv.getTypeName(); // �ǡ�����̾���������
				if(typeName == null){ // �ǡ�������̤��
					tv.setTypeName(mt.typename);
				}
				else if(typeName.equals(mt.typename))
					throw new TypeException("type error : " + mt.typename + "(user def) <=> " + typeName + "(infered)");
			}
			else if(p instanceof TracingPath){
				TracingPath tp = (TracingPath)p;
				Functor f = tp.getFunctor();
				int pos = tp.getPos();
				if(!dataAtomTypes.containsKey(f))continue;
				List<ModedType> types = dataAtomTypes.get(f);
				ModedType mt = types.get(pos);
				ModeVar mv = tvc.getModeVar();
				if(mv.value == 0)mv.bindSign(mt.sign); // �⤷�⡼���ѿ���̤����ʤ���ꤹ��
				else if(mv.value != mt.sign)
					throw new TypeException("mode error : " + mt.sign + "(user def) <=> " + mv.value + "(infered)");
				TypeVar tv = tvc.getTypeVar();
				String typeName = tv.getTypeName(); // �ǡ�����̾���������
				if(typeName == null){ // �ǡ�������̤��
					tv.setTypeName(mt.typename);
				}
				else if(typeName.equals(mt.typename))
					throw new TypeException("type error : " + mt.typename + "(user def) <=> " + typeName + "(infered)");
			}
			else Env.p("fatal error : RootPath");
		}
		//���˸Ŀ��θ����򤹤�
		Map<String, FixedCounts> memnameToCounts = qi.getMemNameToFixedCountsSet();
		for(String memname : memnameToCounts.keySet()){
			FixedCounts fcs = memnameToCounts.get(memname);
			for(Functor f : fcs.functorToCount.keySet()){
				if(!functorCounts.containsKey(memname))break;
				Map<Functor, FixedCount> fToC = functorCounts.get(memname);
				if(!fToC.containsKey(f))continue;
				FixedCount fc = fToC.get(f);
				checkCount(f.toString(), fc,fcs.functorToCount.get(f));
			}
			for(String childname : fcs.memnameToCount.keySet()){
				if(!memCounts.containsKey(memname))break;
				Map<String, FixedCount> mToC = memCounts.get(memname);
				if(!mToC.containsKey(childname))continue;
				FixedCount fc = mToC.get(childname);
				checkCount(childname, fc, fcs.memnameToCount.get(childname));
			}
		}
	}
	
	private void checkCount(String s, FixedCount constraint, FixedCount infered)throws TypeException{
		if(constraint instanceof InfinityCount){
			throw new TypeException("fatal error : infinity is given as definition.");
//			if(!(infered instanceof InfinityCount))
//				errorCount(constraint, infered);
//			InfinityCount ci = (InfinityCount)constraint;
//			InfinityCount ii = (InfinityCount)infered;
//			if(ci.minus != ii.minus)
//				errorCount(constraint, infered);
		}
		else if(constraint instanceof NumCount){
			if(!(infered instanceof NumCount))
				errorCount(s, constraint, infered);
			NumCount cn = (NumCount)constraint;
			NumCount in = (NumCount)infered;
			if(cn.value != in.value)
				errorCount(s, constraint, infered);
		}
		else{
			IntervalCount ci = (IntervalCount)constraint;
			if(infered instanceof InfinityCount){
				InfinityCount ii = (InfinityCount)infered;
				if(!ii.minus){
					if(ci.max instanceof InfinityCount){
						if(((InfinityCount)ci.max).minus)
							errorCount(s, constraint, infered);
					}
					else
						errorCount(s, constraint, infered);
				}
				else{
					throw new TypeException("fatal error : -inf infered.");
				}
			}
			else if(infered instanceof NumCount){
				NumCount in = (NumCount)infered;
				if(ci.min.compare(in) > 0 || ci.max.compare(in) < 0)
					errorCount(s, constraint, infered);
			}
			else{
				IntervalCount ii = (IntervalCount)infered;
				if(ci.min.compare(ii.min) > 0 || ci.max.compare(ii.max) < 0)
					errorCount(s, constraint, infered);
			}
		}
	}
	
	private void errorCount(String s, FixedCount constraint, FixedCount infered)throws TypeException{
		throw new TypeException("quantity error : " + s + " : " + constraint + "(user def) <=> " + infered + "(infered)");
	}
	
}

class ModedType{
	public String typename;
	public int sign;
	public ModedType(String typename, int sign) {
		this.typename = typename;
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