package daemon;

import java.net.InetAddress;
import java.util.HashMap;

import runtime.AbstractMembrane;

/*
 * �����Х�ID -> ������ID, object �Ȥ����Ѵ��򤹤륯�饹
 * 
 * @author nakajima
 *
 */
public class IDConverter{
	//�����Х���ID -> �쥪�֥�������
	HashMap memTable = new HashMap();
	//������ID -> 
	
	
	/*
	 * �����Х���ID -> �쥪�֥�������
	 * @return ��Ͽ����Ƥ����AbstractMembrane, ����Ƥʤ����null
	 */
	AbstractMembrane getMem(String globalMemID){
		return (AbstractMembrane)memTable.get(globalMemID);
	}
	
	/*
	 * �����Х���ID -> ��������ID ����Ͽ����
	 */
	boolean registerMemID(String globalMemID, String localMemID){
		if(memTable.get(globalMemID)!= null){
			//��Ͽ�Ѥߤʤ�true
			return true;
		} else {
			//��Ͽ����Ƥ��ʤ������Ͽ����
			memTable.put(globalMemID, localMemID);
			return true;			
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