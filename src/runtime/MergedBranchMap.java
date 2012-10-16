package runtime;

import java.util.ArrayList;
import java.util.HashMap;

import runtime.functor.Functor;

/**
 * �Ԥ߾夲���̿����򰷤����饹
 * �롼�륻�å�1�Ĥ������1�ĺ��������
 * �����ƥ��֥��ȥ�Υե��󥯥��򥭡��ˡ�Ŭ�Ѳ�ǽ�ʤᤤ������t��������Ƥ��뤳�Ȥ���ǽ
 * @author sakurai
 *
 */

public class MergedBranchMap {
	/**
	 * �����ƥ��֥��ȥ�Υե��󥯥��ͤ����³��̿����Υޥå�
	 */
	public HashMap branchMap;
	
	public MergedBranchMap(HashMap bm){
		branchMap = bm;
	}
	
	/**
	 * �ե��󥯥����б�����̿������֤�
	 * @param func �����ƥ��֥��ȥ�Υե��󥯥�
	 * @return��̿����
	 */
	public ArrayList getInsts(Functor func){
		return (ArrayList)branchMap.get(func);
	}
	
	/**
	 * �ޥåפ˥ե��󥯥����ޤޤ�뤫���ǧ
	 * @param func�������ƥ��֥��ȥ�Υե��󥯥�
	 * @return �ե��󥯥����ޤޤ�뤫�ɤ���(boolean)
	 */
	public boolean containsKey(Functor func){
		return branchMap.containsKey(func);
	}
}
