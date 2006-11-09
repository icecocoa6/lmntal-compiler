package type.argument;

import java.util.HashSet;
import java.util.Set;

import type.TypeException;

/**
 * �⡼���ѿ����������
 * @author kudo
 *
 */
public class ModeVarSet {
	
	private Set<ModeVar> modes;
	
	public ModeVarSet(){
		modes = new HashSet<ModeVar>();
	}
	
	/**
	 * Ϳ����줿�ѥ��Υ⡼���ѿ����֤�
	 * @param path
	 * @return
	 */
	private ModeVar getModeVarOfPath(Path path){
		for(ModeVar ms : modes)
			if(ms.contains(path))return ms;
		return null;
	}
	
	/**
	 * Ϳ����줿�⡼���ѿ����Ĥ�Ʊ���⡼�ɤ�ɽ�����Ȥ�ؤ���ms2��ms1�˼����ޤ�롣
	 * @param ms1
	 * @param ms2
	 */
	public void merge(ModeVar ms1, ModeVar ms2){
		ms1.addAll(ms2);
		ms1.buddy.addAll(ms2.buddy);
		modes.remove(ms2);
		modes.remove(ms2.buddy);
	}
	
	/**
	 * ���ĤΥѥ��ˤĤ��ƤΥ⡼�ɴط�����Ͽ����
	 * @param sign
	 * @param p1
	 * @param p2
	 * @throws TypeException
	 */
	public void add(int sign, Path p1, Path p2)throws TypeException{
		// Ʊ��ѥ����Ф��⡼�ɤ��դ����ꤵ��Ƥ���
		if((sign == -1) && p1.equals(p2))
			throw new TypeException("mode error (same path with in/out) : " + p1 + " <=> " + p2);
		ModeVar ms1 = getModeVarOfPath(p1);
		ModeVar ms2 = getModeVarOfPath(p2);
		// ξ���˴��˥⡼���ѿ��������Ƥ���
		if(ms1!=null && ms2!=null){
			// ��椬��������
			if(sign==1){
				// ����줿�⡼���ѿ���Ʊ���ʤ鲿�⤷�ʤ�
				if(ms1==ms2)return;
				// ����줿�⡼���ѿ��������ʤ����
				else if(ms1==ms2.buddy)
					throw new TypeException("mode error :" + p1 + " <=> " + p2);
				else merge(ms1, ms2);
			}
			// ��椬�դʤ�
			else if(sign==-1){
				// ����줿�⡼���ѿ��������ʤ鲿�⤷�ʤ�
				if(ms1==ms2.buddy)return;
				// ����줿�⡼���ѿ���Ʊ���ʤ����
				else if(ms1==ms2)
					throw new TypeException("mode error :" + p1 + " <=> " + p2);
				else merge(ms1, ms2.buddy);
			}
		}
		//�������������Ƥ��ʤ���硢�⤦�����������򿶤�
		else if(ms1!=null && ms2==null){
			if(sign==1)ms1.add(p2);
			else ms1.buddy.add(p2);
		}
		else if(ms2!=null && ms1==null){
			if(sign==1)ms2.add(p1);
			else ms2.buddy.add(p1);
		}
		//ξ�������Ƥ��ʤ���硢��������Τ���
		else{
			ModeVar nms = newModeVar();
			nms.add(p1);
			if(sign==1)nms.add(p2);
			else nms.buddy.add(p2);
		}
	}
	
	public ModeVar getModeVar(Path path)throws TypeException{
		ModeVar ms = getModeVarOfPath(path);
		if(ms==null){
			ms = newModeVar();
			ms.add(path);
		}
		return ms;
	}
	
	private static int modeid = 0;
	private String newModeVarName(){
		return /*"'" +*/ "m" + (modeid++);
	}
	
	/**
	 * �����˥⡼���ѿ����Ȥ�������������������֤�
	 * @return
	 */
	public ModeVar newModeVar(){
		String nm = newModeVarName();
		ModeVar ms1 = new ModeVar(nm,1);
		ModeVar ms2 = new ModeVar(nm,-1);
		ms1.buddy = ms2;
		ms2.buddy = ms1;
		modes.add(ms1);
		modes.add(ms2);
		return ms1;
	}
	
}
