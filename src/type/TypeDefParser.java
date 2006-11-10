package type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import runtime.Env;
import runtime.Functor;
import runtime.IntegerFunctor;
import runtime.SymbolFunctor;
import type.quantity.Count;
import type.quantity.FixedCount;
import type.quantity.IntervalCount;
import type.quantity.NumCount;

import compile.structure.Atom;
import compile.structure.Atomic;
import compile.structure.Membrane;

/** */
public class TypeDefParser {
	
	/**
	 * ���������ɤ߹���Ƿ����������
	 * @param typedefmem
	 */
	public static void parseFromMembrane(Membrane typedefmem){
		
		//��̾���Ȥˡ����줪��ӥ����ƥ��֥��ȥ�θĿ����������
		Map<String, Map<String, FixedCount>> memCounts = new HashMap<String, Map<String, FixedCount>>();
		Map<String, Map<Functor,FixedCount>> functorCounts = new HashMap<String, Map<Functor,FixedCount>>();
		
		// �����ƥ��֥��ȥ�η�����
		Map<String, Map<Functor, List<ModedType>>> activeAtomTypes = new HashMap<String, Map<Functor, List<ModedType>>>();
		// �ǡ������ȥ�η����� (?)
		Map<Functor, List<ModedType>> dataAtomTypes = new HashMap<Functor, List<ModedType>>();
		
		try{
			for(Membrane mem : typedefmem.mems){
				String memname = mem.name;
				for(Atom atom : mem.atoms){
					Functor f = atom.functor;
					if(f.getName().equals("datatype")){
						Atomic typeatomic = atom.args[f.getArity()-1].buddy.atom;
						if(!(typeatomic instanceof Atom))
							throw new TypeParseException("context appearing in type definition.");
	
						String typename = typeatomic.getName();
						for(int i=0;i<f.getArity()-1;i++){
							
							Atomic dataatomic = atom.args[i].buddy.atom;
							if(!(dataatomic instanceof Atom))
								throw new TypeParseException("context appearing in type definition.");
							Atom dataatom = (Atom)dataatomic;
	
							boolean flgRegistered = false;
							for(int j=0;j<dataatom.getArity()-1;j++){
								Atomic signatomic = atom.args[i].buddy.atom;
								if(!(signatomic instanceof Atom))
									throw new TypeParseException("context appearing in type definition.");
								Atom signatom = (Atom)signatomic;
								if(signatom.functor.equals(new SymbolFunctor("+",2))){
									
								}
								else if(signatom.functor.equals(new SymbolFunctor("-",1))){
									if(flgRegistered)
										throw new TypeParseException("data atom must have only one output argument.");
									TypeEnv.registerDataFunctor(dataatom.functor,typename,j);
									flgRegistered = true;
								}
								else throw new TypeParseException("datatype atom must have sign atom");
							}
						}
					}
					else if(f.equals(new SymbolFunctor(".",3))){
						Atomic lastatomic = atom.args[2].buddy.atom;
						if(!(lastatomic instanceof Atom))
							throw new TypeParseException("context appearing in type definition");
						Atom lastatom = (Atom)lastatomic;
						Functor lastf = lastatom.functor;
						if(lastf.equals(new SymbolFunctor(".", 3)))
							continue;
						else if(lastf.equals(Functor.OUTSIDE_PROXY)){ // ����ʤ�
							String childname = lastatom.args[0].buddy.atom.args[1].buddy.atom.mem.name; // 0�����ܤ���($in)��1�����ܤ���Υ��ȥ�ν�°��
							FixedCount fc = getCountFromList(atom);
							addChildCount(memCounts, memname, childname, fc);
						}
						else{ // �����ƥ��֥��ȥ�
							constrainActiveAtomArgument(activeAtomTypes, memname, lastatom);
							FixedCount fc = getCountFromList(atom);
							addFunctorCount(functorCounts, memname, new SymbolFunctor(f.getName(),f.getArity()-1),fc);
						}
					}
					else if(f instanceof IntegerFunctor){
						Atomic actatomic = atom.args[0].buddy.atom;
						if(!(actatomic instanceof Atom))
							throw new TypeParseException("context appearing in type definition");
						Atom lastatom = (Atom)actatomic;
						int count = ((IntegerFunctor)f).intValue();
						if(lastatom.equals(new SymbolFunctor(".",3)))
							continue;
						else if(lastatom.equals(Functor.OUTSIDE_PROXY)){ // ����ʤ�
							String childname = lastatom.args[0].buddy.atom.args[1].buddy.atom.mem.name; // 0�����ܤ���($in)��1�����ܤ���Υ��ȥ�ν�°��
							FixedCount fc = new NumCount(count);
							addChildCount(memCounts, memname, childname, fc);
						}
						else{ //�����ƥ��֥��ȥ�
							constrainActiveAtomArgument(activeAtomTypes, memname, lastatom);
							FixedCount fc = new NumCount(count);
							addFunctorCount(functorCounts, memname, new SymbolFunctor(f.getName(),f.getArity()-1),fc);
						}
					}
				}
			}
		}catch(TypeParseException e){
			e.printError();
		}
	}
	
	private static void constrainActiveAtomArgument(Map<String, Map<Functor, List<ModedType>>> map, String memname, Atom act){
		for(int i=0;i<act.getArity()-1;i++){
			
		}
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
	
	/**
	 * ����θĿ�������ɲä���
	 * @param map �ɲä�����Υޥå�
	 * @param parentname ��°��̾
	 * @param childname ����̾
	 * @param fc �Ŀ�����
	 * @throws TypeParseException Ʊ����̾�ˤĤ���2�ĸĿ�������ɲä��褦�Ȥ���ȥ��顼
	 */
	private static void addChildCount(Map<String, Map<String, FixedCount>> map, String parentname, String childname, FixedCount fc)throws TypeParseException{
		if(!map.containsKey(parentname))
			map.put(parentname, new HashMap<String, FixedCount>());
		Map<String, FixedCount> counts = map.get(parentname);
		if(!counts.containsKey(childname))
			counts.put(childname, fc);
		else{
			throw new TypeParseException("two descriptions about same membrane name.");
		}
	}
	
	/**
	 * �����ƥ��֥��ȥ�θĿ�������ɲä���
	 * @param map �ɲä�����Υޥå�
	 * @param parentname ��°��
	 * @param functor �����ƥ��֥��ȥ�Υե��󥯥�
	 * @param fc �Ŀ�����
	 * @throws TypeParseException Ʊ�������ƥ��֥��ȥ�ˤĤ���2�ĸĿ�������ɲä��褦�Ȥ���ȥ��顼
	 */
	private static void addFunctorCount(Map<String, Map<Functor, FixedCount>> map, String parentname, Functor functor, FixedCount fc)throws TypeParseException{
		if(!map.containsKey(parentname))
			map.put(parentname, new HashMap<Functor, FixedCount>());
		Map<Functor, FixedCount> counts = map.get(parentname);
		if(!counts.containsKey(functor))
			counts.put(functor, fc);
		else{
			throw new TypeParseException("two descriptions about same active atom.");
		}
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
	
	public TypeParseException(String msg){
		message = msg;
	}
	
	public void printError(){
		Env.p("TYPE DEFINITION SYNTAX ERROR : " + message);
	}
	
}