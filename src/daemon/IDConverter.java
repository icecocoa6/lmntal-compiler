package daemon;

//import java.net.InetAddress;
import java.util.HashMap;

import runtime.*;

/**
 * �����Х�ID -> �������object �Ȥ����Ѵ��򤹤륯�饹���߷����
 * 
 * @author nakajima, n-kato
 *
 */
public class IDConverter {
	/** �����Х�롼�륻�å�ID (String) -> Ruleset */
	static HashMap rulesetTable = null;
	/** �����Х���ID (String) -> AbstractMembrane */
	static HashMap memTable = null;

	////////////////////////////////////////////////////////////////	

//	static void init() {
//		rulesetTable.clear();
//		memTable.clear();
//	}

	/** ���ꤵ�줿�롼�륻�åȤ�ɽ����Ͽ���� */
	public static void registerRuleset(String globalid, Ruleset rs){
		if(null == rulesetTable){ rulesetTable = new HashMap(); }
		rulesetTable.put(globalid, rs);
	}
	/** ���ꤵ�줿globalRulesetID����ĥ롼�륻�åȤ�õ��
	 * @return Ruleset�ʸ��Ĥ���ʤ��ä�����null��*/
	public static Ruleset lookupRuleset(String globalRulesetID){
		if(null == rulesetTable){ return null; }
		return (Ruleset)rulesetTable.get(globalRulesetID);
	}

	/** ���ꤵ�줿���ɽ����Ͽ���� */
	public static void registerGlobalMembrane(String globalMemID, AbstractMembrane mem) {
		if(Env.debugDaemon > 0) System.out.println("IDConverter.registerGlobalMembrane(" + globalMemID + ", " + mem.toString() + ")"); //todo use Env
		if(null == memTable){ memTable = new HashMap(); }
		memTable.put(globalMemID, mem);
	}
	/** ���ꤵ�줿���ɽ���������� */
	public static void unregisterGlobalMembrane(String globalMemID) {
		if(null == memTable){ return; }
		memTable.remove(globalMemID);
		if(memTable.isEmpty()){ memTable = null; }
	}
	
	
	/** ���ꤵ�줿globalMemID��������õ��
	 * @return AbstractMembrane�ʸ��Ĥ���ʤ��ä�����null��*/
	public static AbstractMembrane lookupGlobalMembrane(String globalMemID){
		if(null == memTable){ return null; }
		return (AbstractMembrane)memTable.get(globalMemID);
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