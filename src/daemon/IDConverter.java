package daemon;

//import java.net.InetAddress;
import java.util.HashMap;

import runtime.*;

/**
 * �����Х�ID -> �������object �Ȥ����Ѵ��򤹤륯�饹���߷����
 * 
 * <p>NEW_->object���Ѵ��ϡ����ӹԤ���
 * 
 * @author nakajima, n-kato
 *
 */
public class IDConverter {
	/** �����Х�롼�륻�å�ID (String) -> Ruleset */
	static HashMap rulesetTable = new HashMap();
	/** �����Х���ID (String) -> AbstractMembrane */
	static HashMap memTable = new HashMap();

	////////////////////////////////////////////////////////////////	

//	static void init() {
//		rulesetTable.clear();
//		memTable.clear();
//	}

	/** ���ꤵ�줿�롼�륻�åȤ�ɽ����Ͽ���� */
	public static void registerRuleset(String globalid, Ruleset rs){
		rulesetTable.put(globalid, rs);
	}
	/** ���ꤵ�줿globalRulesetID����ĥ롼�륻�åȤ�õ��
	 * @return Ruleset�ʸ��Ĥ���ʤ��ä�����null��*/
	public static Ruleset lookupRuleset(String globalRulesetID){
		return (Ruleset)rulesetTable.get(globalRulesetID);
	}

	/** ���ꤵ�줿���ɽ����Ͽ���� */
	public static void registerGlobalMembrane(String globalMemID, AbstractMembrane mem) {
		memTable.put(globalMemID, mem);
	}
	/** ���ꤵ�줿globalMemID��������õ��
	 * @return AbstractMembrane�ʸ��Ĥ���ʤ��ä�����null��*/
	public static AbstractMembrane lookupGlobalMembrane(String globalMemID){
		return (AbstractMembrane)memTable.get(globalMemID);
	}

	////////////////////////////////////////////////////////////////	
	
	/** �����Х���ID (String) -> AbstractMembrane */
	HashMap newMemTable = new HashMap();
	/** �����륢�ȥ�ID�ޤ���NEW_ (String) -> Atom */
	HashMap newAtomTable = new HashMap();
	
	////////////////////////////////////////////////////////////////

//	void clear() {
//		newMemTable.clear();
//		newAtomTable.clear();
//	}
	
	////////////////////////////////////////////////////////////////

	/** ���ꤵ�줿���ɽ����Ͽ���� */
	public void registerNewMembrane(String globalMemID, AbstractMembrane mem) {
		newMemTable.put(globalMemID, mem);
	}
	/** �����Х���ID�ޤ���NEW_���б��������õ��
	 * @return Membrane�ʸ��Ĥ���ʤ��ä�����null��*/
	public AbstractMembrane lookupMembrane(String memid) {
		Object obj = newMemTable.get(memid);
		if (obj instanceof AbstractMembrane) return (AbstractMembrane)obj;
		return (AbstractMembrane)memTable.get(memid);
	}

	/** ���ꤵ�줿���ȥ��ɽ����Ͽ���� */
	public void registerNewAtom(String atomID, Atom atom) {
		newAtomTable.put(atomID, atom);
	}
	/** ���ȥ�ID���б����륢�ȥ��õ��
	 * @param mem ��°��
	 * @return Atom�ʸ��Ĥ���ʤ��ä�����null��*/
	public Atom lookupAtom(AbstractMembrane mem, String atomid) {
		Object obj = newAtomTable.get(atomid);
		if (obj instanceof Atom) return (Atom)obj;
		if (mem instanceof Membrane) {
			return (Atom)((Membrane)mem).lookupAtom(atomid);
		}
		return null;
	}
	
	////////////////////////////////////////////////////////////////
	
	/*
	 * �����Х���ID -> ��������ID ����Ͽ����
	 */
//	boolean registerMemID(String globalMemID, String localMemID){
//		if(memTable.get(globalMemID)== null){
//			//��Ͽ����Ƥ��ʤ������Ͽ����
//			memTable.put(globalMemID, localMemID);
//			return true;
//		} else {
//			//��Ͽ�Ѥߤʤ鸽����Ͽ����Ƥ���Τ�localMemID��Ʊ����Ĵ�٤�
//			if(((String)memTable.get(globalMemID)).equalsIgnoreCase(localMemID)){
//				return true;
//			} else {
//				//��äƤ�����false
//				return false;
//			}
//		}
//	}
//
//	/*
//	 * ��IDɽ������
//	 */
//	void clearMemIDTable(){
//		memTable.clear();
//	}
//
	/*
	 * �����Х����ID��������ơ�Ʊ����ɽ����Ͽ���롣
	 * 
	 * @return �����Х����ID����Ȥ�InetAddress.getLocalHost() + ":" + AbstractMembrane.getID()��getLocalHost()�˼��Ԥ�����null�����롣
	 * @param mem �����Х��ID�򿶤ꤿ����
	 */	
//		public static String getGlobalMembraneID(AbstractMembrane mem){
//			//�⤦��Ͽ�Ѥߤʤ���Ͽ����Ƥ���ID���֤�
//			if(memTable.get(mem) != null){
//				return (String)(memTable.get(mem)); 
//			}
//		
//			String newid;
//			try {
//				//ID����������
//				newid = InetAddress.getLocalHost().toString() + ":" + mem.getLocalID();
//				//ID��Ͽ
//				memTable.put(mem,newid);
//				return newid;
//			} catch (Exception e){
//				//ID��������
//				e.printStackTrace();
//			}
//		
//			return null;
//		}

}