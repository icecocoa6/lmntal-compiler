package type.argument;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import type.TypeConstraintException;

public class ModeVar {

	/** ����(�����Υ⡼���ѿ�) */
	public ModeVar buddy;

	/** ���Υ⡼���ѿ�����ĥѥ� */
	private Set<Path> pathes;
	
	public Set<Path> getPathes(){
		return pathes;
	}

	/** �⡼���ѿ�̾ */
	public String name;

	/** ��� */
	public int sign;

	/** �ºݤΥ⡼����
	 * 1: ����
	 * -1 : ����
	 * 0 : ����
	 */
	public int value = 0;

	public ModeVar() {
		pathes = new HashSet<Path>();
	}	
	
	/**
	 * �⡼���ѿ����ͤ�«�����롣����«������Ƥ�����硢�ۤʤ�����«�����褦�Ȥ�����㳰���ꤲ�롣
	 * @param s
	 * @throws TypeConstraintException
	 */
	public void bindSign(int s) throws TypeConstraintException {
		// �����ʤ�«������
		if (value == 0) {
			value = s;
			buddy.value = -s;
		} else if (value == s)
			return;
		else
			throw new TypeConstraintException("mode error " + value
					+ " <=> " + s);
	}

	public void add(Path path) {
		pathes.add(path);
	}

	/**
	 * �оݤΥ⡼���ѿ�����ĥѥ��ˤĤ��ơ����Υ⡼���ѿ����������
	 * @param ms
	 */
	public void addAll(ModeVar ms) {
		for(Path path : ms.getPathes()){
			add(path);
		}
	}

	public boolean contains(Path path) {
		return pathes.contains(path);
	}

	public String toString() {
		return ("["
				+ (value == 0 ? ("?" + (sign == 1 ? "+" : "-") + "(" + name + ")")
						: value == 1 ? "+" : "-") + "]");
	}

	public boolean equals(ModeVar mv) {
//		if (o instanceof ModeVar) {
//			ModeVar mv = (ModeVar) o;
			return name.equals(mv.name) && sign == mv.sign;
//		} else
//			return false;
	}

	public int hashCode() {
		return name.hashCode() + sign;
	}

}
