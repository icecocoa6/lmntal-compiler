package daemon;

import java.net.InetAddress;
import java.util.HashMap;

import runtime.AbstractMembrane;

/**
 * �����Х�ID -> �������object �Ȥ����Ѵ��򤹤륯�饹
 * 
 * @author nakajima, n-kato
 *
 */
public class IDConverter{
	//�����Х���ID (String) -> �쥪�֥������� (AbstractMembrane)
	HashMap memTable = new HashMap();

	
	
	/*
	 * �����Х���ID -> �쥪�֥�������
	 * @return ��Ͽ����Ƥ����Membrane, ����Ƥʤ����null
	 */
	AbstractMembrane getMem(String globalMemID){
		return (AbstractMembrane)memTable.get(globalMemID);
	}


	
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

	/*
	 * �����Х���ID -> �쥪�֥������� ����Ͽ����
	 */
	boolean registerMem(String globalMemID, AbstractMembrane memobj){
		if(memTable.get(globalMemID)==null){
			memTable.put(globalMemID, memobj);
			return true;
		} else {
			return false;
		}
	}

	/*
	 * ��IDɽ������
	 */
	void clearMemIDTable(){
		memTable.clear();
	}

	/*
	 * �����Х����ID���������롣
	 *
	 * @return �����Х����ID����Ȥ�InetAddress.getLocalHost() + ":" + AbstractMembrane.getID()��getLocalHost()�˼��Ԥ�����null�����롣
	 * @param mem �����Х��ID�򿶤ꤿ����
	 */
	public static String getGlobalMembraneID(AbstractMembrane mem){
		 String newid;
		 try {
			 //ID����������
			 newid = InetAddress.getLocalHost().toString() + ":" + mem.getLocalID();
			 return newid;
		 } catch (Exception e){
			 //ID��������
			 e.printStackTrace();
		 }
		
		 return null;
	}

	/*
	 * �����Х����ID��������ơ�Ʊ����ɽ����Ͽ���롣
	 * 
	 * @return �����Х����ID����Ȥ�InetAddress.getLocalHost() + ":" + AbstractMembrane.getID()��getLocalHost()�˼��Ԥ�����null�����롣
	 * @param mem �����Х��ID�򿶤ꤿ����
	 */	
//		public static String getGlobalMembraneID(AbstractMembrane mem){
//			//�⤦��Ͽ�Ѥߤʤ���Ͽ����Ƥ���ID���֤�
//			if(localMemTable.get(mem) != null){
//				return (String)(localMemTable.get(mem)); 
//			}
//		
//			String newid;
//			try {
//				//ID����������
//				newid = InetAddress.getLocalHost().toString() + ":" + mem.getLocalID();
//				//ID��Ͽ
//				localMemTable.put(mem,newid);
//				return newid;
//			} catch (Exception e){
//				//ID��������
//				e.printStackTrace();
//			}
//		
//			return null;
//		}

}