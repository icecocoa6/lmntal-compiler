/*
 * ������: 2003/10/21
 *
 */
package runtime;
import java.util.*;

/**
 * 1 �Ĥ�̿����ݻ����롣
 * 
 * ʣ���Ǥ⹽��ʤ��Ȥ��⤦��
 * 
 * �ǥХå���ɽ���᥽�åɤ������롣
 *
 * @author pa
 *
 */
public class Instruction {
	public static final int NULL      = 0;
	public static final int MEM       = 1;
	public static final int VAR       = 2;
	public static final int NAME      = 3;
	public static final int AT        = 4;
	public static final int METAVAR   = 5;

    //����̵�����Ƚ�����̤�10(by api���ͽ�)
	public List data = new ArrayList();
	
	public Instruction() {
		// ���Ȥ��� [react, [1, 2, 5]]
		ArrayList sl = new ArrayList();
		sl.add(new Integer(1));
		sl.add(new Integer(2));
		sl.add(new Integer(5));
		data.add("react");
		data.add(sl);
		System.out.println(data);
	}

    /**
     * �ǥХå���ɽ���᥽�åɡ�
     * ̿��ο���(int)��Ϳ����ȡ���������̿���String���֤��Ƥ����
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return String
     * 
     */

     public static String getInstrcutionString(int instrcutionNum){
	 /* see http://www.ueda.info.waseda.ac.jp/~mizuno/lmntal/method4.html
	  *
	  * �ޥå��󥰤μ¹�
	  * ����̿��
	  * 0 -> deref 
	  * 1 -> lock
	  * 2 -> anymem
	  * 3 -> findatom
	  * 4 -> findchildatom
	  * 5 -> func
	  * 6 -> norules 
	  * 7 -> ieq
	  * 8 -> eq
	  * 9 -> neq 
	  * 10 -> getmem
	  * 11 -> not
	  * 12 -> stop
	  * 13 -> react
	  *
	  * ��ĥ̿��
	  * 14 -> getlink
	  * 15 -> pos
	  *
	  * �ܥǥ��μ¹�
	  * 16 -> unlock
	  * 17 -> removeatom
	  * 18 -> removemem
	  * 19 -> dequeueatom
	  * 20 -> dequeuemem
	  * 21 -> newatom
	  * 22 -> newmem
	  * 23 -> newlink
	  * 24 -> relink
	  * 25 -> unify
	  * 26 -> movemem
	  * 
	  * ��ĥ̿��
	  * 27 -> freeze
	  * 28 -> copy
	  * 29 -> removemem
	  * 30 -> bundle
	  *
	  *
	  */ 

	 int maxNum = 30+1; //̿��μ���ο�
	 String[] hoge = new String[maxNum];
	 String answer = "";

	 hoge[0] = "deref";
	 hoge[1] = "lock";
	 hoge[2] = "anymem";
	 hoge[3] = "findatom";
	 hoge[4] = "findchildatom";
	 hoge[5] = "func";
	 hoge[6] = "norules";
	 hoge[7] = "ieq";
	 hoge[8] = "eq";
	 hoge[9] = "neq";
	 hoge[10] = "getmem";
	 hoge[11] = "not";
	 hoge[12] = "stop";
	 hoge[13] = "react";
	 hoge[14] = "getlink";
	 hoge[15] = "pos";
	 hoge[16] = "unlock";
	 hoge[17] = "removeatom";
	 hoge[18] = "removemem";
	 hoge[19] = "dequeueatom";
	 hoge[20] = "dequeuemem";
	 hoge[21] = "newatom";
	 hoge[22] = "newmem";
	 hoge[23] = "newlink";
	 hoge[24] = "relink";
	 hoge[25] = "unify";
	 hoge[26] = "movemem";
	 hoge[27] = "freeze";
	 hoge[28] = "copy";
	 hoge[29] = "removemem";
	 hoge[30] = "bundle";
	 
	 try {
	     answer = hoge[instrcutionNum];
	 } catch (ArrayIndexOutOfBoundsException e){
	     //	     answer = "�� �ʡ��� \n���ʡ����ϡ��ˡ㡡�̤�� \n\n";

	     answer = "1 ̾�������ͽ�̵������ 03/09/21 00:23\n�� �ʡ��� \n���ʡ����ϡ��ˡ㡡�̤�� \n\n2 ̾�������ͽ�̵������ ��03/09/21 00:24\n������������������ \n�� �ʡ����ϡ��ˡ�����|��|�����ގ�\n���ȡ��������ˡ� �� |��| \n���� �١�/�Ρ����� �� \n������ /���ˡ� �� < ��>__���� \n�� ��/��'������. �֡�������/ ��>>���ޤ�\n���ʡ����ġ��������� ����/ \n\n";
	 } catch (Exception e){
	     //�����˥���������
	     System.out.println(e);
	     System.exit(1);
	 }

	 return answer;
     }

    /**
     * �ǥХå���ɽ���᥽�åɡ�
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return String
     *
     * ��⡧Instruction����ϡ�List�����ArrayList������Ҥˤʤä����äƤ��롣
     * �Ĥޤꡢ[̿��, [����], ̿��, [����], ��]
     * ������̿���int����������int[]��
     *
     */
    public String toString(){
	StringBuffer buffer = new StringB2ufffer("");

	

	return buffer.toString();
    }

    /**
     *
     * Deprecated: 2003-10-28 �ǡ��������ѹ��ˤʤä�����
     *
     * �ǥХå���ɽ���᥽�åɡ�
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return String
     *
     * ��⡧Instruction����ϡ�List�����ArrayList������Ҥˤʤä����äƤ��롣
     * �Ĥޤꡢ[̿��, [����], ̿��, [����], ��]
     *
     */
//     public String toString(){
// 	//��ȴ������ˡ
// 	//	return "not implemented\n";

// 	//��ȴ������ˡ����2
// 	/*	Object hoge;
// 	 *	hoge = (Object)data;
// 	 *	return hoge.toString();     
// 	*/

// 	//�ޤȤ����ˡ
// 	//[̿��, [����], ̿��, [����], ��]������String���Ѵ�
// 	Object[] hoge; 
// 	Object[] fuga;
// 	StringBuffer buffer = new StringBuffer();

// 	try {
// 	    hoge = data.toArray();

// 	    buffer.append("[ ");

// 	    //̿��Τ���Υ롼��
// 	    for (int i = 0; i < hoge.length-1; i+=2) {
// 		buffer.append(hoge[i]);
// 		buffer.append(", ");
		
// 		buffer.append("[");

// 		//�����Τ���Υ롼��
// 		fuga = hoge[i+1].toArray();
// 		for (int j = 0; j < fuga.length; j++) {
// 		    buffer.append(fuga[j]);
// 		}
// 		buffer.append("]");
// 	    }

// 	    buffer.append(" ]");

// 	} catch (Exception e){
// 	    //���ꤵ����硧
// 	    //ArrayList data������̿�᤬���äƤʤ�
// 	    //ArrayList data[i]����������̵��
// 	    //̤�ΤΥХ����Ȥꤢ����exception��print

// 	    //����ʳ����ʤ󤫤��롩

// 	    //�㡧ArrayStoreException - a �μ¹Ի��η����ꥹ�����
// 	    //    �����Ǥμ¹Ի��η��Υ����ѡ����åȤǤʤ����
// 	    //    (by API���ͽ��ArrayList���饹toArray�᥽�åɤβ���)

// 	    System.out.println(e);

// 	    return "General Protection Fault\n\n";
// 	}

// 	return (buffer.toString());
//     }




    /**
     * �ǥХå���ɽ���᥽�åɡ�Ϳ����줿List��Object[]���Ѵ��������줾������Ǥ��Ф���toString()��Ƥ��stdout�˿��ή����
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return void
     * @param List
     *
     * NAKJAIMA:2003-10-26:����ʤ��ä��ʡ�
     *
     */
//     public static void Dump(List listToBeDumped){
// 	Object[] hoge = listToBeDumped.toArray();
// 	Object[] fuga;
	
// 	for (int i = 0; i < hoge.length-1 ; i+=2){
// 	    System.out.print("Command: ");
// 	    System.out.print(hoge[i].toString());

// 	    System.out.print("\t");
// 	    System.out.print("Arguments: ");

// 	    fuga = hoge[i+1].toArray();
// 	    for (int j = 0; j < fuga.length; j++){
// 		System.out.print(fuga[j].toString());
// 		System.out.print(" ");
// 	    }
// 	    System.out.println();
// 	}
// 	System.out.println();
//     }

    /**
     * �ǥХå���ɽ���᥽�åɡ�Instruction
     * ���֥����������List��Object[]���Ѵ�����
     * ���줾������Ǥ��Ф���toString()��Ƥ֡�
     * 
     * deprecated: by NAKAJIMA Motomu on 2003-10-25
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return void
     */
//     public void Dump(){
// 	Object[] tmp = data.toArray();

// 	for (int i = 0; i < tmp.length; i++){
// 	    System.out.print(tmp[i].toString());
// 	    System.out.print(" ");
// 	    System.out.println();
// 	}
//     }

    /**
     * �ǥХå���ɽ���᥽�åɡ�
     * [̿��, [����], ̿��, [����], ��]������print���롣
     * 
     * List����Ƭ��command�ȸ��ʤ���
     * command�μ��ϡ�������ArrayList�ȸ��ʤ���
     * ���μ��Ϥޤ�command�ȸ��ʤ���
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return void
     */
//     public void Dump(){
// 	//[̿��, [����], ̿��, [����], ��]������print����
// 	Object[] hoge; 
// 	Object[] fuga;

// 	try {
// 	    hoge = data.toArray();

// 	    System.out.print("[ ");

// 	    //̿��Τ���Υ롼��
// 	    for (int i = 0; i < hoge.length-1; i+=2) {
// 		System.out.print("Command: ");
// 		System.out.print(hoge[i]);

// 		System.out.print("\t");
// 		System.out.print("Arguments: ");

// 		//�����Τ���Υ롼��
// 		fuga = hoge[i+1].toArray();
// 		for (int j = 0; j < fuga.length; j++) {
// 		    System.out.print(fuga[j]);
// 		    System.out.print(" ");
// 		}
// 	    }

// 	    System.out.println(" ]");

// 	} catch (Exception e){
// 	    //���ꤵ����硧
// 	    //ArrayList data������̿�᤬���äƤʤ�
// 	    //ArrayList data[i]����������̵��
// 	    //̤�ΤΥХ����Ȥꤢ����exception��print

// 	    //����ʳ����ʤ󤫤��롩

// 	    //�㡧ArrayStoreException - a �μ¹Ի��η����ꥹ�����
// 	    //    �����Ǥμ¹Ի��η��Υ����ѡ����åȤǤʤ����
// 	    //    (by API���ͽ��ArrayList���饹toArray�᥽�åɤβ���)

// 	    System.out.println(e);
// 	}
//     }
}
