/*
 * ������: 2003/10/21
 *
 */
package runtime;

import java.util.*;

/**
 * 1 �Ĥ�̿����ݻ����롣
 * 
 * 
 * 
 * �ǥХå���ɽ���᥽�åɤ������롣
 *
 * @author hara
 *
 */
public class Instruction {
	
    /**
     * �ɤ�̿��ʤΤ����ݻ�����
     */
    private int id;
	
    //�ޥå���̿��
    /** deref [-dstatom, +srcatom, +srcpos, +dstpos] 
     * <BR>������̿��<BR>
     * ���ȥ�srcatom����srcpos�����Υ���褬��dstpos��������³���Ƥ��뤳�Ȥ��ǧ�����顢�����Υ��ȥ��dstatom���������롣
     */
    public static final int DEREF = 0;

    /** getmem [?dstmem, srcatom]
     * <BR>���Ԥ��ʤ�������̿�ᡢ�ܥǥ�̿��<BR>
     * ��srcmem�ν�°��ؤλ��Ȥ�������롣
     */
    public static final int GETMEM = 1;

    /** getparent [?dstmem, srcmem]
     * <BR>���Ԥ��ʤ�������̿�ᡢ�ܥǥ�̿��<BR>
     * ��srcmem�ο���ؤλ��Ȥ�������롣
     * <P>TODO: ��ͳ��󥯴������ȥब����Τ����ס�
     */
    public static final int GETPARENT = 2;

    /** anymem [??dstmem, srcmem] 
     * <BR>������̿��<BR>
     * ��srcmem�λ���Τ����ޤ���å���������Ƥ��ʤ�����Ф��Ƽ����ˡ��Υ�֥�å��󥰤ǤΥ�å��������ߤ롣
     * ��å���������������С�������Ϥޤ����Ȥ�ʡ��å���˼������Ƥ��ʤ��ä���Ǥ���
     * �ʤ��θ�������ˡ���Ǥ�neq̿��ǹԤäƤ����ˡ�
     * �����ơ��ƻ����dstmem���������롣
     * ����������å��ϡ���³��̿���󤬤�������Ф��Ƽ��Ԥ����Ȥ��˲�������롣
     * <P>���: ��å������˼��Ԥ������ȡ������줬¸�ߤ��Ƥ��ʤ��ä����Ȥ϶��̤Ǥ��ʤ���
     */
    public static final int ANYMEM = 3;

    /** findatom [dstatom, srcmem, func]
     * ��srcmem�ˤ��ä�̾��func����ĥ��ȥ�򼡡���dstatom���������롣
     */
    public static final int FINDATOM = 4;

    /** func [srcatom, func]
     * <BR>���Ԥ��ʤ�������̿�ᡢ�ܥǥ�̿��<BR>
     * ���ȥ�srcatom���ե��󥯥�func����Ĥ��Ȥ��ǧ���롣
     */
    public static final int FUNC = 5;

    /** norules [srcmem] 
     * <BR>������̿��<BR>
     * ��srcmem�˥롼�뤬¸�ߤ��ʤ����Ȥ��ǧ���롣
     */
    public static final int NORULES = 6;

    /** natoms [srcmem, count]
     * <BR>������̿��<BR>
     * ��srcmem�μ�ͳ��󥯥��ȥ�ʳ��Υ��ȥ����count�Ǥ��뤳�Ȥ��ǧ����
     * <P>TODO:ɬ�פǤ�����
     */
    public static final int NATOMS = 8;

    /** nfreelinks [srcmem, count]
     * <BR>������̿��<BR>
     * ��srcmem�μ�ͳ��󥯿���count�Ǥ��뤳�Ȥ��ǧ���롣
     */
    public static final int NFREELINKS = 9;

    /** nmems [srcmem, count]
     * <BR>������̿��<BR>
     * ��srcmem�λ���ο���count�Ǥ��뤳�Ȥ��ǧ���롣
     */
    public static final int NMEMS = 10;

    /** eq [atom1, atom2]<BR>
     *  eq [mem1, mem2]
     * <BR>������̿��<BR>
     * ���ȥ�atom1��atom2��Ʊ��Υ��ȥ�򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     * ��mem1����mem2��Ʊ�����򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     */
    public static final int EQ = 11;

    /** neq [atom1, atom2]<BR>
     *  neq [mem1, mem2]
     * <BR>������̿��<BR>
     * ���ȥ�atom1��atom2���ۤʤ륢�ȥ�򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     * ��mem1����mem2���ۤʤ���򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     * <P>TODO:����Ф���neq̿������ס�
     */
    public static final int NEQ = 12;


    //�ܥǥ�̿��
    /** lock [srcmem]
     * <BR>������̿��<BR>
     * ��srcmem���Ф���Υ�֥�å��󥰤ǤΥ�å��������ߤ롣
     * ��å���������������С�������Ϥޤ����Ȥ�ʡ��å���˼������Ƥ��ʤ��ä���Ǥ���
     * �ʤ��θ�������ˡ���Ǥ�neq̿��ǹԤäƤ����ˡ�
     * ����������å��ϡ���³��̿���󤬤�������Ф��Ƽ��Ԥ����Ȥ��˲�������롣  
     * TODO: srcmem��memof��ˡ���ѻߤ��줿���ᡢ��å���getparent�ǹԤ����������ä�lock���ѻߡ�*/
    public static final int LOCK = 13;

    /** unlock [srcmem]
     * <BR>�ܥǥ�̿��<BR>
     * ��srcmem�Υ�å���������롣
     */
    public static final int UNLOCK = 14;

    // �ܥǥ��μ¹�
    /** removeatom [srcatom]
     * <BR>�ܥǥ�̿��<BR>
     * ���ȥ�srcatom�򸽺ߤ��줫����Ф���
     */
    public static final int REMOVEATOM = 15;

    /** removemem [srcmem]
     * <BR>�ܥǥ�̿��<BR>
     * ��srcmem�򸽺ߤ��줫����Ф���
     */
    public static final int REMOVEMEM = 16;

    /** insertproxies [parentmem M],[srcmem N]
     * <BR>�ܥǥ�̿��<BR>
     * ��srcmem��� star ���ȥ� n ���Ф��ơ��ʲ���Ԥ���
     * <OL>
     * <LI>n ��̾���� inside_proxy ���Ѥ��롣
     * <LI>n ����1�����Υ���褬 parentmem ��Υ��ȥ�
     * �ʤ��ξ��ɬ�� outside_proxy �ޤ��� star�ˤʤ�ˤǤʤ���С�
     * <OL>
     * <LI>M ��� outside_proxy o ����� star m ���������� 
     newlink o,2,m,2 ��Ԥ��� 
     relink  m,1,n,1 ��Ԥ��� 
     newlink n,1,o,1 ��Ԥ��� 

    */
    public static final int INSERTPROXIES = 17;

    /** removeproxy [?]
     */
    public static final int REMOVEPROXIES = 18;

    /** hoge_star [?]
     */
    //public static final int HOGE_STAR = 19;

    /** newatom [?dstatom, srcmem, func]
     * <BR>�ܥǥ�̿��<BR>
     * ��srcmem��̾��func����Ŀ��������ȥ�����������Ȥ�dstatom���������롣
     */
    public static final int NEWATOM = 20;

    /** newmem [?dstmem, srcmem]
     * <BR>�ܥǥ�̿��<BR>
     * ��srcmem�˿�����������������dstmem���������롣
     */
    public static final int NEWMEM = 21;

    /** newlink [atom1, pos1, atom2, pos2]
     * <BR>�ܥǥ�̿��<BR>
     * ���ȥ�atom1����pos1�������顢���ȥ�atom2����pos2�����˸�������������󥯤�ĥ�롣
     * TODO: �ո�����Ʊ����ĥ��褦�ˤ��롣
     */
    public static final int NEWLINK = 22;

    /** relink [atom1, pos1, atom2, pos2]
     * <BR>�ܥǥ�̿��<BR>
     * ���ȥ�atom1����pos1�����Υ����ΰ����ȡ����ȥ�atom2����pos2��������³���롣
     * <P>ŵ��Ū�ˤϡ�atom1�ϥ롼��ܥǥ��ˡ�atom2�ϥ롼��إåɤ�¸�ߤ��롣
     */
    public static final int RELINK = 23;

    /** unify [atom1, pos1, atom2, pos2]
     * <BR>�ܥǥ�̿��<BR>
     * ���ȥ�atom1����pos1�����Υ����ΰ����ȡ����ȥ�atom2����pos2�����Υ����ΰ�������³���롣
     * <P>ŵ��Ū�ˤϡ�atom1��atom2��
     */
    public static final int UNIFY = 24;

    /** dequeueatom [srcatom]
     * ���ȥ�srcatom��¹ԥ����å�������Ф���
     */
    public static final int DEQUEUEATOM = 25;

    /* dequeuemem [srcmem]
     * ��srcmem��Ƶ�Ū�˼¹��쥹���å�������Ф���
     */
    public static final int DEQUEUEMEM = 26;

    /** movemem [dstmem, srcmem]
     * <BR>�ܥǥ�̿��<BR>
     * ��srcmem����dstmem�˰�ư���롣
     */
    public static final int MOVEMEM = 27;
	      
    //��ĥ̿��
    /** recursivelock [srcmem]
     * <BR>���Ԥ��ʤ��ʡ��˥�����̿��<BR>
     * ��srcmem�����Ƥλ�����Ф��ƺƵ�Ū�˥�å���������롣�֥�å��󥰤ǹԤ���
     */
    public static final int RECURSIVELOCK = 28;

    /** recursiveunlock [srcmem]
     * <BR>�ܥǥ�̿��<BR>
     * ��srcmem�����Ƥλ�����Ф��ƺƵ�Ū�˥�å���������롣
     */
    public static final int RECURSIVEUNLOCK = 29;

    /** copy [dstmem, srcmem]
     * �Ƶ�Ū�˥�å�������srcmem�����ƤΥ��ԡ������������dstmem������롣��������ͳ��󥯴������ȥ����1�����ξ��֤��������ʤ���
     */
    public static final int COPY = 30;

    /** not [instructions...]
     */
    public static final int NOT = 31;

    /** stop 
     */
    public static final int STOP = 32;

    /** react [ruleid, args...]
     * ���ȥ�srcatom��̾����func�Ǥ��뤳�Ȥ��ǧ���롣 
     */
    public static final int REACT = 33;

    /* control instructions */
    ///** [inline, text]*/
    //    public static final int INLINE = 34;
	
    /**
     * �������ɲä��롣�ޥ������䡣
     * @param o ���֥������ȷ��ΰ���
     */
    private void add(Object o) { data.add(o); }
	
    /**
     * �������ɲä��롣�ޥ������䡣
     * @param n int ���ΰ���
     */
    private void add(int n) { data.add(new Integer(n)); }
	
    /**
     * ���ߡ�̿�����������.
     * ���������ä������᥽�åɤ��ޤ��Ǥ��Ƥʤ�̿��Ϥ����Ȥ�
     * @param s �����Ѥ�ʸ����
     */
    public static Instruction dummy(String s) {
	Instruction i = new Instruction(-1);
	i.add(s);
	return i;
    }
	
    /**
     * react ̿�����������
     * 
     * @param r ȿ���Ǥ���롼�륪�֥�������
     * @param actual ����
     * @return
     */
    public static Instruction react(Rule r, List actual) {
	Instruction i = new Instruction(REACT);
	i.add(r);
	i.add(actual);
	return i;
    }
	
    /**
     * findatom ̿�����������
     * 
     * @param dstatom
     * @param srcmem
     * @param func
     * @return Instruction
     */
    public static Instruction findatom(int dstatom, List srcmem, Functor func) {
	Instruction i = new Instruction(FINDATOM);
	i.add(dstatom);
	i.add(srcmem);
	i.add(func);
	return i;
    }
	
    /**
     * anymem ̿�����������
     * 
     * @param dstmem
     * @param srcmem
     * @return
     */
    public static Instruction anymem(int dstmem, int srcmem) {
	Instruction i = new Instruction(ANYMEM);
	i.add(dstmem);
	i.add(srcmem);
	return i;
    }
	
	
	
	
	
	
    //����̵�����Ƚ�����̤�10(by api���ͽ�)
    public List data = new ArrayList();
	
    /**
     * ̵̾̿����롣
     *
     */
    public Instruction() {
    }
	
    /**
     * ���ꤵ�줿̿���Ĥ���
     * @param id
     */
    public Instruction(int id) {
    	this.id = id;

	//deprecated by NAKAJIMA: �Ť��ǡ�������
	//by HARA
	// ���Ȥ��� [react, [1, 2, 5]]
	// 		ArrayList sl = new ArrayList();
	// 		sl.add(new Integer(1));
	// 		sl.add(new Integer(2));
	// 		sl.add(new Integer(5));
	// 		data.add("react");
	// 		data.add(sl);
	// 		System.out.println(data);

	//�������ǡ�������
	/*
	  ArrayList sl = new ArrayList();
	  sl.add(new Integer(1));
	  sl.add(new Integer(2));
	  sl.add(new Integer(5));
	  data.add(new Integer(0)); // 0->deref̿��
	  data.add(sl);
	  System.out.println(data);
	*/
    }


    /**
     * �ǥХå���ɽ���᥽�åɡ�
     * ̿���ʸ����(String)��Ϳ����ȡ���������̿���int���֤��Ƥ����
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return int
     * 
     */
    public static int getInstructionInteger(String instructionString){
	Hashtable table = new Hashtable();
	int answer = -1;
	Object tmp;

	table.put("DEREF", new Integer(DEREF));
	table.put("GETMEM", new Integer(GETMEM));
	table.put("GETPARENT", new Integer(GETPARENT));
	table.put("ANYMEM", new Integer(ANYMEM));
	table.put("FINDATOM", new Integer(FINDATOM));
	table.put("FUNC", new Integer(FUNC));
	table.put("NORULES", new Integer(NORULES));
	table.put("NATOMS", new Integer(NATOMS));
	table.put("NFREELINKS", new Integer(NFREELINKS));
	table.put("EQ", new Integer(EQ));
	table.put("NEQ", new Integer(NEQ));
	table.put("LOCK", new Integer(LOCK));
	table.put("UNLOCK", new Integer(UNLOCK));
	table.put("REMOVEATOM", new Integer(REMOVEATOM));
	table.put("REMOVEMEM", new Integer(REMOVEMEM));
	table.put("INSERTPROXY", new Integer(INSERTPROXIES));
	table.put("REMOVEPROXY", new Integer(REMOVEPROXIES));
	table.put("NEWATOM", new Integer(NEWATOM));
	table.put("NEWMEM", new Integer(NEWMEM));
	table.put("NEWLINK", new Integer(NEWLINK));
	table.put("RELINK", new Integer(RELINK));
	table.put("UNIFY", new Integer(UNIFY));
	table.put("DEQUEUEATOM", new Integer(DEQUEUEATOM));
	table.put("DEQUEUEMEM", new Integer(DEQUEUEMEM));
	table.put("MOVEMEM", new Integer(MOVEMEM));
	table.put("RECURSIVELOCK", new Integer(RECURSIVELOCK));
	table.put("COPY", new Integer(COPY));
	table.put("NOT", new Integer(NOT));
	table.put("STOP", new Integer(STOP));
	table.put("REACT", new Integer(REACT));

	try {
	    answer = ((Integer)table.get(instructionString.toUpperCase())).intValue();
	} catch (NullPointerException e){
	    System.out.println(e);
	    System.exit(1);
	}

	return answer;
    }


    /**
     * �ǥХå���ɽ���᥽�åɡ�
     * ̿��ο���(int)��Ϳ����ȡ���������̿���String���֤��Ƥ���롣
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return String
     * 
     */
    public static String getInstructionString(int instrcutionNum){
	String answer = "";

	int maxNum = REACT+1; //̿��μ���ο�
	String[] hoge = new String[maxNum];
	hoge[DEREF] = new String("DEREF");
	hoge[GETMEM] = new String("GETMEM");
	hoge[GETPARENT] = new String("GETPARENT");
	hoge[ANYMEM] = new String("ANYMEM");
	hoge[FINDATOM] = new String("FINDATOM");
	hoge[FUNC] = new String("FUNC");
	hoge[NORULES] = new String("NORULES");
	hoge[NATOMS] = new String("NATOMS");
	hoge[NFREELINKS] = new String("NFREELINKS");
	hoge[EQ] = new String("EQ");
	hoge[NEQ] = new String("NEQ");
	hoge[LOCK] = new String("LOCK");
	hoge[UNLOCK] = new String("UNLOCK");
	hoge[REMOVEATOM] = new String("REMOVEATOM");
	hoge[REMOVEMEM] = new String("REMOVEMEM");
	hoge[INSERTPROXIES] = new String("INSERTPROXIES");
	hoge[REMOVEPROXIES] = new String("REMOVEPROXIES");
	hoge[NEWATOM] = new String("NEWATOM");
	hoge[NEWMEM] = new String("NEWMEM");
	hoge[NEWLINK] = new String("NEWLINK");
	hoge[RELINK] = new String("RELINK");
	hoge[UNIFY] = new String("UNIFY");
	hoge[DEQUEUEATOM] = new String("DEQUEUEATOM");
	hoge[DEQUEUEMEM] = new String("DEQUEUEMEM");
	hoge[MOVEMEM] = new String("MOVEMEM");
	hoge[RECURSIVELOCK] = new String("RECURSIVELOCK");
	hoge[COPY] = new String("COPY");
	hoge[NOT] = new String("NOT");
	hoge[STOP] = new String("STOP");
	hoge[REACT] = new String("REACT");

	try {
	    answer = hoge[instrcutionNum];
	} catch (ArrayIndexOutOfBoundsException e){
	    //	     answer = "�� �ʡ��� \n���ʡ����ϡ��ˡ㡡�̤�� \n\n";

	    answer = "\n1 ̾�������ͽ�̵������ 03/09/21 00:23\n�� �ʡ��� \n���ʡ����ϡ��ˡ㡡�̤�� \n\n2 ̾�������ͽ�̵������ ��03/09/21 00:24\n������������������ \n�� �ʡ����ϡ��ˡ�����|��|�����ގ�\n���ȡ��������ˡ� �� |��| \n���� �١�/�Ρ����� �� \n������ /���ˡ� �� < ��>__���� \n�� ��/��'������. �֡�������/\n���ʡ����ġ��������� ����/ \n\n";
	    answer = "dummy";
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
     * �Ĥޤꡢ��int����name��̿�� [����1, ����2, �� , ����n] 
     * ����Ĥ�Instruction���󥹥��󥹤ˤϡ�1�Ĥ���̿�᤬�ʤ�
     * ������̿���Integer����������Object��
     *
     * ��⡧���Ϥλ����⡼�ɤ��ݤ��ѿ��򥤥�ǥ�Ȥ��롣
     *
     */
    public String toString(){
	return getInstructionString(id)+" "+data.toString();

	//	StringBuffer buffer = new StringBuffer("");
	//
	//	if(data.isEmpty()){
	//	    buffer.append(" No Instructions! ");
	//	} else {
	//	    for (int i = 0; i < data.size()-1; i+=2){
	//		buffer.append("[");
	//
	//		buffer.append("Command: ");
	//		buffer.append( getInstructionString(((Integer)data.get(i)).intValue()));
	//		buffer.append(" Arguments: ");
	//		buffer.append(data.get(i+1));
	//		
	//		buffer.append("]");
	//	    }
	//	}
	//
	//	return buffer.toString();
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
