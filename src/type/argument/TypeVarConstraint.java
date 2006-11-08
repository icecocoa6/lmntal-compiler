/**
 * 
 */
package type.argument;


/**
 * �ƥѥ��ˤĤ��Ƥη��ѿ����⡼���ѿ������
 * @author kudo
 *
 */
public class TypeVarConstraint {

	/**
	 * ���Ť�����ѥ������β����������RootPath�������β����줿���ActiveAtomPath or TracingPath������
	 */
	private Path p;	
	public Path getPath(){
		return p;
	}
	
	/**
	 * ���ѿ�
	 */
	private TypeVar tv;
	public TypeVar getTypeVar(){
		return tv;
	}
	/**
	 * �⡼���ѿ�
	 */
	private ModeVar mv;
	public ModeVar getModeVar(){
		return mv;
	}

	public TypeVarConstraint(Path p, TypeVar tv, ModeVar mv){
		this.p = p;
		this.tv = tv;
		this.mv = mv;
	}
	
	public boolean equals(Object o){
		if(o instanceof TypeVarConstraint){
			TypeVarConstraint tvc = (TypeVarConstraint)o;
			return p.equals(tvc.p) && tv.equals(tvc.tv) && mv.equals(tvc.mv);
		}else return false;
	}
	
	public int hashCode(){
		return p.hashCode() + tv.self().hashCode() + tv.self().hashCode();
	}
	
	public String toString(){
		return fixLength(p.toStringWithOutAnonMem(),15) + " : mode=" + 
		(fixLength(mv.toString(),12)) + ", type=" + tv;
	}
	
	public String shortString(){
		return mv.shortString() + tv.shortString();
	}
	
	private String fixLength(String orig, int length){
		for(int s = orig.length();s<length;s++)
			orig += " ";
		return orig;
	}
	
}
