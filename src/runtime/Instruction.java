/*
 * ������: 2003/10/21
 *
 */
package runtime;

import java.util.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.io.*;

/*
 * <p><b>���</b>����ˡ4��ʸ����Ф��ơ��֥�å������ޤ޼¹��쥹���å����Ѥ�פȤ������
 * ����Ӥ��η�̤Ρ֥�å����줿�����ļ¹��쥹���å��ˤ��Ѥޤ줿�׾��֤��ɲä��줿��
 * 
 * <p>�ܥǥ��¹Ԥ�ή��ϼ����̤ꡣ�����ȳ����������¦����Ԥä��塢��å������¦����������롣
 * <ul>
 * <li>�����������줬�����Ʊ������������ξ�硢����¦����ºݤ˼¹��쥹���å����Ѥࡣ
 * ���줬�¹���ʤΤǡ��������줬�¹Ԥ���뤳�ȤϤʤ���
 * ���Υ롼���Ŭ�ѽ�λ���˥�å��ϲ������뤬�����ⵯ����ʤ���
 * ���Υ롼���Ŭ�Ѥ���λ����ȡ�����μ¹Ԥ����Ϥ���롣
 * <li>�����������줬��⡼�����¾�Υ���������ξ�硢���Ū�ʡֲ��Ρ׼¹��쥹���å����ꡢ
 * �����˿���¦�����Ѥ�Ǥ�����
 * ��⡼�ȤΥ롼����Υ�å������������ȡ��¹��쥹���å�����Ƭ�˴ݤ��Ȱ�ư����롣
 * ����ˤ�äƼ¹��쥹���å������Ƥ��줬���ȥߥå����Ѥޤ�뤳�Ȥˤʤ롣
 * </ul>
 * ����ѥ���ϼ��Υ����ɤ���Ϥ��롧addmem��newroot������ϡ��롼��¹Խ�λ���ˡʻ���¦������֤ˡ�unlockmem��¹Ԥ��롣
 */
 
/**
 * 1 �Ĥ�̿����ݻ����롣�̾�ϡ�Instruction��ArrayList�Ȥ����ݻ����롣
 * 
 * �ǥХå���ɽ���᥽�åɤ������롣
 *
 * @author hara, nakajima, n-kato
 */
public class Instruction implements Cloneable, Serializable {
	/** ̿����ΰ�������������ơ��֥� */
	private static HashMap argTypeTable = new HashMap();
	/**���ȥ�*/
	public static final int ARG_ATOM = 0;
	/**��*/
	public static final int ARG_MEM = 1;
	/**���ȥࡦ��ʳ����ѿ�*/
	public static final int ARG_VAR = 2;
	/**����*/
	public static final int ARG_INT = 3;
	/**̿����*/
	public static final int ARG_INSTS = 4;
	/**��٥��դ�̿����*/
	public static final int ARG_LABEL = 5;
	/**�ѿ��ֹ��List*/
	public static final int ARG_VARS = 6;
	/**����¾���֥�������(�롼��ʤ�)�ؤλ���*/
	public static final int ARG_OBJ = 7;
	
	
    /** ̿��μ�����ݻ����롣*/	
    private int kind;

	/**
	 * ̿��ΰ������ݻ����롣
	 * ̿��μ���ˤ�äư����η�����ޤäƤ��롣
	 */
	public List data = new ArrayList();
	
    //////////
    // ���

    /** �оݤ��줬������η׻��Ρ��ɤ�¸�ߤ��뤳�Ȥ��ݾڤ��뽤���ҡʰ���¾�����Ӥǻ��ѡ� */
    public static final int LOCAL = 100;
    /** ���դ����ȥ���Ф���̿�᤬���ȥ�ǤϤʤ����ե��󥯥����оݤˤ��Ƥ��뤳�Ȥ�ɽ�������� */
    public static final int OPT = 100;
    /** ���ߡ���̿�� */	
    public static final int DUMMY = -1;
    /** ̤�����̿�� */	
    public static final int UNDEF = 0;
//	/** ̿��κ���������̿��μ����ɽ���ͤϤ����꾮���ʿ��ˤ��뤳�ȡ�*/
//	private static final int END_OF_INSTRUCTION = 1024;

    // ���ȥ�˴ط�������Ϥ�����ܥ�����̿�� (1--5)
	//  -----  deref     [-dstatom, srcatom, srcpos, dstpos]
	//  -----  derefatom [-dstatom, srcatom, srcpos]
	//  -----  dereflink [-dstatom, srclink, dstpos]
	//  -----  findatom  [-dstatom, srcmem, funcref]

    /** deref [-dstatom, srcatom, srcpos, dstpos]
     * <br><strong><font color="#ff0000">���Ϥ��륬����̿��</font></strong><br>
     * ���ȥ�$srcatom����srcpos�����Υ���褬��dstpos��������³���Ƥ��뤳�Ȥ��ǧ�����顢
     * �����Υ��ȥ�ؤλ��Ȥ�$dstatom���������롣*/
	public static final int DEREF = 1;
	// LOCALDEREF������
	static {setArgType(DEREF, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_INT, ARG_INT));}

	/** derefatom [-dstatom, srcatom, srcpos]
	 * <br>���Ϥ��뼺�Ԥ��ʤ���Ŭ���ѡܷ��դ���ĥ�ѥ�����̿��<br>
	 * ���ȥ�$srcatom����srcpos�����Υ����Υ��ȥ�ؤλ��Ȥ�$dstatom���������롣
	 * <p>����³��$dstatom����ñ�ॢ�ȥ�������ʤɤ�ޤ�ˤ伫ͳ��󥯴������ȥ��
	 * �ޥå����뤫�ɤ�������������˻��Ѥ��뤳�Ȥ��Ǥ��롣*/
	public static final int DEREFATOM = 2;
	// LOCALDEREFATOM������
	static {setArgType(DEREFATOM, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_INT));}

    /** dereflink [-dstatom, srclink, dstpos]
     * <br>���Ϥ����Ŭ���ѥ�����̿��<br>
     * ���$srclink����dstpos��������³���Ƥ��뤳�Ȥ��ǧ�����顢
     * �����Υ��ȥ�ؤλ��Ȥ�$dstatom���������롣*/
	public static final int DEREFLINK = 3; // by mizuno
	// LOCALDEREFLINK������
	static {setArgType(DEREFLINK, new ArgType(true, ARG_ATOM, ARG_VAR, ARG_INT));}

	/** findatom [-dstatom, srcmem, funcref]
	 * <br>ȿ�����륬����̿��<br>
	 * ��$srcmem�ˤ��äƥե��󥯥�funcref����ĥ��ȥ�ؤλ��Ȥ򼡡���$dstatom���������롣*/
	public static final int FINDATOM = 4;
	// LOCALFINDATOM������
	static {setArgType(FINDATOM, new ArgType(true, ARG_ATOM, ARG_MEM, ARG_OBJ));}

	// ��˴ط�������Ϥ�����ܥ�����̿�� (5--9)
	// [local]lockmem    [-dstmem, freelinkatom]
	// [local]anymem     [-dstmem, srcmem] 
	// [local]lock       [srcmem]
	//  ----- getmem     [-dstmem, srcatom]
	//  ----- getparent  [-dstmem, srcmem]

    /** lockmem [-dstmem, freelinkatom]
     * <br>��å��������륬����̿��<br>
     * ��ͳ��󥯽��ϴ������ȥ�$freelinkatom����°��������Ф��ơ�
     * �Υ�֥�å��󥰤ǤΥ�å���������ߤ롣
     * �����ƥ�å���������������������ؤλ��Ȥ�$dstmem���������롣
     * ����������å��ϡ���³��̿���󤬤�������Ф��Ƽ��Ԥ����Ȥ��˲�������롣
     * <p>
     * ��å���������������С�������Ϥޤ����Ȥ�ʡ��å���˼������Ƥ��ʤ��ä���Ǥ���
     * �ʤ��θ�����Ruby�ǤǤ�neqmem̿��ǹԤäƤ����ˡ�
     * <p>��γ�����Υ�󥯤ǽ������ꤵ�줿��ؤλ��Ȥ�������뤿��˻��Ѥ���롣
     * @see testmem
     * @see getmem */
    public static final int LOCKMEM = 5;
	static {setArgType(LOCKMEM, new ArgType(true, ARG_MEM, ARG_ATOM));}
    
    /** locallockmem [-dstmem, freelinkatom]
     * <br>��å����������Ŭ���ѥ�����̿��<br>
     * lockmem��Ʊ����������$freelinkatom�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALLOCKMEM = LOCAL + LOCKMEM;
	static {setArgType(LOCALLOCKMEM, new ArgType(true, ARG_MEM, ARG_ATOM));}

    /** anymem [-dstmem, srcmem] 
     * <br>ȿ�������å��������륬����̿��<br>
     * ��$srcmem�λ���Τ����ޤ���å���������Ƥ��ʤ�����Ф��Ƽ����ˡ�
     * �Υ�֥�å��󥰤ǤΥ�å��������ߤ롣
     * �����ơ���å����������������ƻ���ؤλ��Ȥ�$dstmem���������롣
     * ����������å��ϡ���³��̿���󤬤�������Ф��Ƽ��Ԥ����Ȥ��˲�������롣
     * <p><b>���</b>����å������˼��Ԥ������ȡ������줬¸�ߤ��Ƥ��ʤ��ä����Ȥ϶��̤Ǥ��ʤ���*/
	public static final int ANYMEM = 6;
	static {setArgType(ANYMEM, new ArgType(true, ARG_MEM, ARG_MEM));}
	
	/** localanymem [-dstmem, srcmem]
     * <br>ȿ�������å����������Ŭ���ѥ�����̿��<br>
	 * anymem��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣$dstmem�ˤĤ��Ƥϲ��Ⲿ�ꤵ��ʤ���*/
	public static final int LOCALANYMEM = LOCAL + ANYMEM;
	static {setArgType(LOCALANYMEM, new ArgType(true, ARG_MEM, ARG_ATOM));}

	/** lock [srcmem]
	 * <br>��å��������륬����̿��<br>
	 * ��$srcmem���Ф��ơ��Υ�֥�å��󥰤ǤΥ�å���������ߤ롣
	 * ����������å��ϡ���³��̿���󤬼��Ԥ����Ȥ��˲�������롣
	 * <p>���ȥ��Ƴ�ƥ��Ȥǡ���Ƴ���륢�ȥ�ˤ�ä����ꤵ�줿��Υ�å���������뤿��˻��Ѥ���롣
	 * @see lockmem
	 * @see getmem */
	public static final int LOCK = 7;
	static {setArgType(LOCK, new ArgType(false, ARG_MEM));}
	
	/** locallock [srcmem]
	 * <br>��å����������Ŭ���ѥ�����̿��<br>
	 * lock��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALLOCK = LOCAL + LOCK;
	static {setArgType(LOCALLOCK, new ArgType(false, ARG_MEM));}

	/** getmem [-dstmem, srcatom]
	 * <br>���Ԥ��ʤ�������̿��<br>
	 * ���ȥ�$srcatom�ν�°��ؤλ��Ȥ��å�������$dstmem���������롣
	 * <p>���ȥ��Ƴ�ƥ��Ȥǻ��Ѥ���롣
	 * @see lock */
	public static final int GETMEM = 8;
	// LOCALGETMEM������
	static {setArgType(GETMEM, new ArgType(true, ARG_MEM, ARG_ATOM));}
	
	/** getparent [-dstmem, srcmem]
	 * <br>������̿��<br>
	 * �ʥ�å����Ƥ��ʤ�����$srcmem���Ф��ơ����ο���ؤλ��Ȥ��å�������$dstmem���������롣
	 * ���줬̵�����ϼ��Ԥ��롣
	 * <p>���ȥ��Ƴ�ƥ��Ȥǻ��Ѥ���롣*/
	public static final int GETPARENT = 9;
	// LOCALGETPARENT������
	static {setArgType(GETPARENT, new ArgType(true, ARG_MEM, ARG_MEM));}

    // ��˴ط�������Ϥ��ʤ����ܥ�����̿�� (10--19)
	//  ----- testmem    [dstmem, srcatom]
	//  ----- norules    [srcmem] 
	//  ----- nfreelinks [srcmem, count]
	//  ----- natoms     [srcmem, count]
	//  ----- nmems      [srcmem, count]
	//  ----- eqmem      [mem1, mem2]
	//  ----- neqmem     [mem1, mem2]
	//  ----- stable     [srcmem]

    /** testmem [dstmem, srcatom]
     * <br>������̿��<br>
     * ���ȥ�$srcatom���ʥ�å����줿����$dstmem�˽�°���뤳�Ȥ��ǧ���롣
     * <p><b>���</b>��Ruby�ǤǤ�getmem�ǻ��Ȥ�����������eqmem��ԤäƤ�����
     * @see lockmem */
	public static final int TESTMEM = 10;
	// LOCALTESTMEM������
	static {setArgType(TESTMEM, new ArgType(false, ARG_MEM, ARG_ATOM));}

    /** norules [srcmem] 
     * <br>������̿��<br>
     * ��$srcmem�˥롼�뤬¸�ߤ��ʤ����Ȥ��ǧ���롣*/
    public static final int NORULES = 11;
    // LOCALNORULES������
	static {setArgType(NORULES, new ArgType(false, ARG_MEM));}

    /** nfreelinks [srcmem, count]
     * <br>������̿��<br>
     * ��$srcmem�μ�ͳ��󥯿���count�Ǥ��뤳�Ȥ��ǧ���롣*/
    public static final int NFREELINKS = 12;
	// LOCALNFREELINKS������
	static {setArgType(NFREELINKS, new ArgType(false, ARG_MEM, ARG_INT));}

	/** natoms [srcmem, count]
	 * <br>������̿��<br>
	 * ��$srcmem�μ�ͳ��󥯴������ȥ�ʳ��Υ��ȥ����count�Ǥ��뤳�Ȥ��ǧ���롣*/
	public static final int NATOMS = 13;
	// LOCALNATOMS������
	static {setArgType(NATOMS, new ArgType(false, ARG_MEM, ARG_INT));}
	
	/** natomsindirect [srcmem, countfunc]
	 * <br>������̿��<br>
	 * ��$srcmem�μ�ͳ��󥯴������ȥ�ʳ��Υ��ȥ����$countfunc���ͤǤ��뤳�Ȥ��ǧ���롣
	 */
	public static final int NATOMSINDIRECT = 14;
	static {setArgType(NATOMSINDIRECT, new ArgType(false, ARG_MEM, ARG_VAR));}

    /** nmems [srcmem, count]
     * <br>������̿��<br>
     * ��$srcmem�λ���ο���count�Ǥ��뤳�Ȥ��ǧ���롣*/
    public static final int NMEMS = 15;
	// LOCALNMEMS������
	static {setArgType(NMEMS, new ArgType(false, ARG_MEM, ARG_INT));}

	// 16��ͽ�� see isground

    /** eqmem [mem1, mem2]
     * <br>������̿��<br>
     * $mem1��$mem2��Ʊ�����򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     * <p><b>���</b> Ruby�Ǥ�eq����ʬΥ */
    public static final int EQMEM = 17;
	// LOCALEQMEM������
	static {setArgType(EQMEM, new ArgType(false, ARG_MEM, ARG_MEM));}
	
    /** neqmem [mem1, mem2]
     * <br>������̿��<br>
     * $mem1��$mem2���ۤʤ���򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     * <p><b>���</b> Ruby�Ǥ�neq����ʬΥ
     * <p><font color=red><b>����̿������פ����Τ�ʤ�</b></font> */
    public static final int NEQMEM = 18;
	// LOCALNEQMEM������
	static {setArgType(NEQMEM, new ArgType(false, ARG_MEM, ARG_MEM));}

	/** stable [srcmem]
	 * <br>������̿��<br>
	 * ��$srcmem�Ȥ��λ�¹�����Ƥ���μ¹Ԥ���ߤ��Ƥ��뤳�Ȥ��ǧ���롣*/
	public static final int STABLE = 19;
	// LOCALSTABLE������
	static {setArgType(STABLE, new ArgType(false, ARG_MEM));}
    
	// ���ȥ�˴ط�������Ϥ��ʤ����ܥ�����̿�� (20-24)
	//  -----  func     [srcatom, funcref]
	//  -----  notfunc  [srcatom, funcref]
	//  -----  eqatom   [atom1, atom2]
	//  -----  neqatom  [atom1, atom2]
	//  -----  samefunc [atom1, atom2]

	/** func [srcatom, funcref]
	 * <br>������̿��<br>
	 * ���ȥ�$srcatom���ե��󥯥�funcref����Ĥ��Ȥ��ǧ���롣
	 * <p>getfunc[tmp,srcatom];loadfunc[func,funcref];eqfunc[tmp,func] ��Ʊ����*/
	public static final int FUNC = 20;
	// LOCALFUNC������
	static {setArgType(FUNC, new ArgType(false, ARG_ATOM, ARG_OBJ));}

	/** notfunc [srcatom, funcref]
	 * <br>������̿��<br>
	 * ���ȥ�$srcatom���ե��󥯥�funcref������ʤ����Ȥ��ǧ���롣
	 * <p>ŵ��Ū�ˤϡ��ץ���ʸ̮������Ū�ʼ�ͳ��󥯤νи����ȥब$inside_proxy�Ǥʤ����Ȥ��ǧ���뤿��˻Ȥ��롣
	 * <p>getfunc[tmp,srcatom];loadfunc[func,funcref];neqfunc[tmp,func] ��Ʊ����*/
	public static final int NOTFUNC = 21;
	// LOCALNOTFUNC������
	static {setArgType(NOTFUNC, new ArgType(false, ARG_ATOM, ARG_OBJ));}

	/** eqatom [atom1, atom2]
	 * <br>������̿��<br>
	 * $atom1��$atom2��Ʊ��Υ��ȥ�򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
	 * <p><b>���</b> Ruby�Ǥ�eq����ʬΥ */
	public static final int EQATOM = 22;
	// LOCALEQATOM������
	static {setArgType(EQATOM, new ArgType(false, ARG_ATOM, ARG_ATOM));}

	/** neqatom [atom1, atom2]
	 * <br>������̿��<br>
	 * $atom1��$atom2���ۤʤ륢�ȥ�򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
	 * <p><b>���</b> Ruby�Ǥ�neq����ʬΥ */
	public static final int NEQATOM = 23;
	// LOCALNEQATOM������
	static {setArgType(NEQATOM, new ArgType(false, ARG_ATOM, ARG_ATOM));}

	/** samefunc [atom1, atom2]
	 * <br>������̿��<br>
	 * $atom1��$atom2��Ʊ���ե��󥯥�����Ĥ��Ȥ��ǧ���롣
	 * <p>getfunc[func1,atom1];getfunc[func2,atom2];eqfunc[func1,func2]��Ʊ����*/
	public static final int SAMEFUNC = 24;
	// LOCALSAMEFUNC������
	static {setArgType(SAMEFUNC, new ArgType(false, ARG_ATOM, ARG_ATOM));}

	// �ե��󥯥��˴ط�����̿�� (25--29)	
	//  -----  dereffunc [-dstfunc, srcatom, srcpos]
	//  -----  getfunc   [-func,    atom]
	//  -----  loadfunc  [-func,    funcref]
	//  -----  eqfunc              [func1, func2]
	//  -----  neqfunc             [func1, func2]

	/** dereffunc [-dstfunc, srcatom, srcpos]
	 * <br>���Ϥ��뼺�Ԥ��ʤ���ĥ������̿��
	 * ���ȥ�$srcatom����srcpos�����Υ����Υ��ȥ�Υե��󥯥����������$dstfunc���������롣
	 * <p>����³�������դ�ñ�ॢ�ȥ�Υޥå��󥰤�Ԥ�����˻��Ѥ���롣
	 * <p>ñ�ॢ�ȥ�Ǥʤ����դ��ץ���ʸ̮�ϡ���󥯥��֥������Ȥ�Ȥä����롣
	 * <p>derefatom[dstatom,srcatom,srcpos];getfunc[dstfunc,dstatom]��Ʊ���ʤΤ��ѻߡ�*/
	public static final int DEREFFUNC = 25;
	// LOCALDEREFFUNC������
	static {setArgType(DEREFFUNC, new ArgType(true, ARG_VAR, ARG_ATOM, ARG_INT));}

	/** getfunc [-func, atom]
	 * <br>���Ϥ��뼺�Ԥ��ʤ���ĥ������̿��<br>
	 * ���ȥ�$atom�Υե��󥯥��ؤλ��Ȥ�$func���������롣*/
	public static final int GETFUNC = 26;
	// LOCALGETFUNC������
	static {setArgType(GETFUNC, new ArgType(true, ARG_VAR, ARG_ATOM));}

	/** loadfunc [-func, funcref]
	 * <br>���Ϥ��뼺�Ԥ��ʤ���ĥ������̿��<br>
	 * �ե��󥯥�funcref�ؤλ��Ȥ�$func���������롣*/
	public static final int LOADFUNC = 27;
	// LOCALLOADFUNC������
	static {setArgType(LOADFUNC, new ArgType(true, ARG_VAR, ARG_OBJ));}
	// func/funcref��VAR? OBJ? -> (n-kato) func=VAR, funcref=OBJ
	
	/** eqfunc [func1, func2]
	 * <br>���դ���ĥ�ѥ�����̿��<br>
	 * �ե��󥯥�$func1��$func2�����������Ȥ��ǧ���롣*/
	public static final int EQFUNC = 28;
	// LOCALEQFUNC������
	static {setArgType(EQFUNC, new ArgType(false, ARG_VAR, ARG_VAR));}

	/** neqfunc [func1, func2]
	 * <br>���դ���ĥ�ѥ�����̿��<br>
	 * �ե��󥯥�$func1��$func2���ۤʤ뤳�Ȥ��ǧ���롣*/
	public static final int NEQFUNC = 29;
	// LOCALEQFUNC������
	static {setArgType(NEQFUNC, new ArgType(false, ARG_VAR, ARG_VAR));}

    // ���ȥ��������ܥܥǥ�̿�� (30--39)    
	// [local]removeatom                  [srcatom, srcmem, funcref]
	// [local]newatom           [-dstatom, srcmem, funcref]
	// [local]newatomindirect   [-dstatom, srcmem, func]
    // [local]enqueueatom                 [srcatom]
	//  ----- dequeueatom                 [srcatom]
	//  ----- freeatom                    [srcatom]
	// [local]alterfunc                   [atom, funcref]
	// [local]alterfuncindirect           [atom, func]

    /** removeatom [srcatom, srcmem, funcref]
     * <br>�ܥǥ�̿��<br>
     * ����$srcmem�ˤ��äƥե��󥯥�$func����ġ˥��ȥ�$srcatom�򸽺ߤ��줫����Ф���
     * �¹ԥ��ȥॹ���å������ʤ���
     * @see dequeueatom */
	public static final int REMOVEATOM = 30;
	static {setArgType(REMOVEATOM, new ArgType(false, ARG_ATOM, ARG_MEM, ARG_OBJ));}
	
	/** localremoveatom [srcatom, srcmem, funcref]
     * <br>��Ŭ���ѥܥǥ�̿��<br>
     * removeatom��Ʊ����������$srcatom�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALREMOVEATOM = LOCAL + REMOVEATOM;
	static {setArgType(LOCALREMOVEATOM, new ArgType(false, ARG_ATOM, ARG_MEM, ARG_OBJ));}

    /** newatom [-dstatom, srcmem, funcref]
     * <br>�ܥǥ�̿��<br>
     * ��$srcmem�˥ե��󥯥�funcref����Ŀ��������ȥ�����������Ȥ�$dstatom���������롣
     * ���ȥ�Ϥޤ��¹ԥ��ȥॹ���å��ˤ��Ѥޤ�ʤ���
     * @see enqueueatom */
    public static final int NEWATOM = 31;
	static {setArgType(NEWATOM, new ArgType(true, ARG_ATOM, ARG_MEM, ARG_OBJ));}
    
	/** localnewatom [-dstatom, srcmem, funcref]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * newatom��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALNEWATOM = LOCAL + NEWATOM;
	static {setArgType(LOCALNEWATOM, new ArgType(true, ARG_ATOM, ARG_MEM, ARG_OBJ));}

	/** newatomindirect [-dstatom, srcmem, func]
	 * <br>���դ���ĥ�ѥܥǥ�̿��<br>
	 * ��$srcmem�˥ե��󥯥�$func����Ŀ��������ȥ�����������Ȥ�$dstatom���������롣
	 * ���ȥ�Ϥޤ��¹ԥ��ȥॹ���å��ˤ��Ѥޤ�ʤ���
	 * @see newatom */
	public static final int NEWATOMINDIRECT = 32;
	static {setArgType(NEWATOMINDIRECT, new ArgType(true, ARG_ATOM, ARG_MEM, ARG_VAR));}
	
	/** localnewatomindirect [-dstatom, srcmem, func]
	 * <br>���դ���ĥ�Ѻ�Ŭ���ѥܥǥ�̿��<br>
	 * newatomindirect��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALNEWATOMINDIRECT = LOCAL + NEWATOMINDIRECT;
	static {setArgType(LOCALNEWATOMINDIRECT, new ArgType(true, ARG_ATOM, ARG_MEM, ARG_VAR));}

	/** enqueueatom [srcatom]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�$srcatom���°��μ¹ԥ��ȥॹ���å����Ѥࡣ
     * <p>���Ǥ˼¹ԥ��ȥॹ���å����Ѥޤ�Ƥ�������ư���̤����Ȥ��롣
     * <p>���ȥ�$srcatom������ܥ�ե��󥯥�������ʤ�����ư���̤����Ȥ��롣
     * <p>�����ƥ��֤��ɤ����ˤ�ä�̿���ư����Ѥ��ʤ���
     * �ष����̿����Ѥޤ�륢�ȥब�����ƥ��֤Ǥ��롣*/
    public static final int ENQUEUEATOM = 33;
	static {setArgType(ENQUEUEATOM, new ArgType(false, ARG_ATOM));}
    
	/** localenqueueatom [srcatom]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * enqueueatom��Ʊ����������$srcatom��<B>�����Ʊ���������������������¸�ߤ���</B>��*/
	public static final int LOCALENQUEUEATOM = LOCAL + ENQUEUEATOM;
	static {setArgType(LOCALENQUEUEATOM, new ArgType(false, ARG_ATOM));}

    /** dequeueatom [srcatom]
     * <br>��Ŭ���ѥܥǥ�̿��<br>
     * ���ȥ�$srcatom�����η׻��Ρ��ɤˤ���¹ԥ��ȥॹ���å������äƤ���С������å�������Ф���
     * <p><b>���</b>������̿��ϡ���������̤Υ�������︺���뤿���Ǥ�դ˻��Ѥ��뤳�Ȥ��Ǥ��롣
     * ���ȥ������Ѥ���Ȥ��ϡ����̴ط�����դ��뤳�ȡ�
     * <p>�ʤ���¾�η׻��Ρ��ɤˤ���¹ԥ��ȥॹ���å������Ƥ����/�ѹ�����̿���¸�ߤ��ʤ���
     * <p>����̿��ϡ�Runtime.Atom.dequeue��ƤӽФ���*/
    public static final int DEQUEUEATOM = 34;
	// LOCALDEQUEUEATOM�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	static {setArgType(DEQUEUEATOM, new ArgType(false, ARG_ATOM));}

	/** freeatom [srcatom]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * ���⤷�ʤ���
	 * <p>$srcatom���ɤ���ˤ�°���������Ĥ��η׻��Ρ�����μ¹ԥ��ȥॹ���å����Ѥޤ�Ƥ��ʤ����Ȥ�ɽ����
	 * ���ȥ��¾�η׻��Ρ��ɤ��Ѥ�Ǥ����硢͢��ɽ��������������פ�Ĵ�٤롣
	 * �� ͢��ɽ�Ϻ��ʤ����Ȥˤ����Τ�����ס�*/
	public static final int FREEATOM = 35;
	// LOCALFREEATOM������
	static {setArgType(FREEATOM, new ArgType(false, ARG_ATOM));}

	/** alterfunc [atom, funcref]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * �ʽ�°�����ġ˥��ȥ�$atom�Υե��󥯥���funcref�ˤ��롣
	 * �����θĿ����ۤʤ����ư���̤����Ȥ��롣*/
	public static final int ALTERFUNC = 36;
	static {setArgType(ALTERFUNC, new ArgType(false, ARG_ATOM, ARG_OBJ));}

	/** localalterfunc [atom, funcref]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * alterfunc��Ʊ����������$atom�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALALTERFUNC = LOCAL + ALTERFUNC;
	static {setArgType(LOCALALTERFUNC, new ArgType(false, ARG_ATOM, ARG_OBJ));}

	/** alterfuncindirect [atom, func]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * alterfunc��Ʊ�����������ե��󥯥���$func�ˤ��롣*/
	public static final int ALTERFUNCINDIRECT = 37;
	static {setArgType(ALTERFUNCINDIRECT, new ArgType(false, ARG_ATOM, ARG_VAR));}
	
	/** localalterfuncindirect [atom, func]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * alterfuncindirect��Ʊ����������$atom�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALALTERFUNCINDIRECT = LOCAL + ALTERFUNCINDIRECT;
	static {setArgType(LOCALALTERFUNCINDIRECT, new ArgType(false, ARG_ATOM, ARG_VAR));}

	// ���ȥ�����뷿�դ���ĥ��̿�� (40--49)
	//  ----- allocatom         [-dstatom, funcref]
	//  ----- allocatomindirect [-dstatom, func]
	// [local]copyatom          [-dstatom, mem, srcatom]
	//  local addatom                     [dstmem, atom]

	/** allocatom [-dstatom, funcref]
	 * <br>���դ���ĥ��̿��<br>
	 * �ե��󥯥�funcref����Ľ�°�������ʤ����������ȥ�����������Ȥ�$dstatom���������롣
	 * <p>�����ɸ����ǻȤ���������ȥ���������뤿��˻��Ѥ���롣*/
	public static final int ALLOCATOM = 40;
	// LOCALALLOCATOM������
	static {setArgType(ALLOCATOM, new ArgType(true, ARG_ATOM, ARG_OBJ));}

	/** allocatomindirect [-dstatom, func]
	 * <br>���դ���ĥ�Ѻ�Ŭ����̿��<br>
	 * �ե��󥯥�$func����Ľ�°�������ʤ����������ȥ������������Ȥ�$dstatom���������롣
	 * <p>�����ɸ����ǻȤ���������ȥ���������뤿��˻��Ѥ���롣*/
	public static final int ALLOCATOMINDIRECT = 41;
	// LOCALALLOCATOMINDIRECT������
	static {setArgType(ALLOCATOMINDIRECT, new ArgType(true, ARG_ATOM, ARG_VAR));}

	/** copyatom [-dstatom, mem, srcatom]
	 * <br>���դ���ĥ�ѥܥǥ�̿��
	 * ���ȥ�$srcatom��Ʊ��̾���Υ��ȥ����$mem����������$dstatom�����������֤���
	 * �¹ԥ��ȥॹ���å������ʤ���
	 * <p>�ޥå��󥰤��������դ����ȥ�򥳥ԡ����뤿��˻��Ѥ��롣
	 * <p>getfunc[func,srcatom];newatomindirect[dstatom,mem,func]��Ʊ������ä��ѻߡ�
	 * copygroundterm�˰ܹԤ��٤����⤷��ʤ���*/
	public static final int COPYATOM = 42;
	static {setArgType(COPYATOM, new ArgType(true, ARG_ATOM, ARG_MEM, ARG_ATOM));}

	/** localcopyatom [-dstatom, mem, srcatom]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * copyatom��Ʊ����������$mem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALCOPYATOM = LOCAL + COPYATOM;
	static {setArgType(LOCALCOPYATOM, new ArgType(true, ARG_ATOM, ARG_MEM, ARG_ATOM));}

	/** localaddatom [dstmem, atom]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * �ʽ�°�������ʤ��˥��ȥ�$atom����$dstmem�˽�°�����롣
	 * ������$dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALADDATOM = LOCAL + 43;
	// ���̤� ADDATOM ��¸�ߤ��ʤ���
	static {setArgType(LOCALADDATOM, new ArgType(false, ARG_MEM, ARG_ATOM));}
	
	// ���������ܥܥǥ�̿�� (50--59)    
	// [local]removemem                [srcmem, parentmem]
	// [local]newmem          [-dstmem, srcmem]
	//  ----- allocmem        [-dstmem]
	//  ----- newroot         [-dstmem, srcmem, node]
	//  ----- movecells                [dstmem, srcmem]
	//  ----- enqueueallatoms          [srcmem]
	//  ----- freemem                  [srcmem]
	// [local]addmem                   [dstmem, srcmem]
	// [local]enququmem                [srcmem]
	// [local]unlockmem                [srcmem]
	// [local]setmemname               [dstmem, name]

	/** removemem [srcmem, parentmem]
	 * <br>�ܥǥ�̿��<br>
	 * ��$srcmem������$parentmem�ˤ�����Ф���
	 * <strike>��$srcmem�ϥ�å����˼¹��쥹���å���������Ƥ��뤿�ᡢ�¹��쥹���å������ʤ���</strike>
	 * �¹��쥹���å����Ѥޤ�Ƥ�����Ͻ���롣
	 * @see removeproxies */
	public static final int REMOVEMEM = 50;
	static {setArgType(REMOVEMEM, new ArgType(false, ARG_MEM, ARG_MEM));}

	/** localremovemem [srcmem, parentmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * removemem��Ʊ����������$srcmem�ο����$parentmem�ˤϤ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALREMOVEMEM = LOCAL + REMOVEMEM;
	static {setArgType(LOCALREMOVEMEM, new ArgType(false, ARG_MEM, ARG_MEM));}

	/** newmem [-dstmem, srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * �ʳ��������줿����$srcmem�˿������ʥ롼����Ǥʤ��˻�����������$dstmem�������������������롣
	 * ���ξ��γ������ϡ�$srcmem��Ʊ���¹��쥹���å����Ѥळ�Ȥ��̣���롣
	 * @see newroot
	 * @see addmem */
	public static final int NEWMEM = 51;
	static {setArgType(NEWMEM, new ArgType(true, ARG_MEM, ARG_MEM));}

	/** localnewmem [-dstmem, srcmem]
	* <br>��Ŭ���ѥܥǥ�̿��<br>
	* newmem��Ʊ����������$srcmem��<B>�����Ʊ���������ˤ�äƴ��������</B>��*/
	public static final int LOCALNEWMEM = LOCAL + NEWMEM;
	static {setArgType(LOCALNEWMEM, new ArgType(true, ARG_MEM, ARG_MEM));}

	/** allocmem [-dstmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * ���������ʤ��������������������Ȥ�$dstmem���������롣*/
	public static final int ALLOCMEM = 52;
	// LOCALALLOCMEM������
	static {setArgType(ALLOCMEM, new ArgType(true, ARG_MEM));}

	/** newroot [-dstmem, srcmem, nodeatom]
	 * <br>�ܥǥ�̿��<br>
	 * ��$srcmem�λ���˥��ȥ�$nodeatom��̾���ǻ��ꤵ�줿�׻��Ρ��ɤǼ¹Ԥ���뿷������å����줿
	 * �롼���������������Ȥ�$dstmem�����������ʥ�å������ޤޡ˳��������롣
	 * ���ξ��γ������ϡ����μ¹��쥹���å����Ѥळ�Ȥ��̣���롣
	 * <p>�������嵭�λ��ͤϷ׻��Ρ��ɻ��꤬��ʸ����Ǥʤ��Ȥ��Τߡ�
	 * ��ʸ����ξ��ϡ�newmem��Ʊ��������å����줿���֤Ǻ���롣
	 * <p>newmem�Ȱ㤤�����Υ롼����Υ�å�������Ū�˲������ʤ���Фʤ�ʤ���
	 * @see unlockmem */
	public static final int NEWROOT = 53;
	// LOCALNEWROOT�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	static {setArgType(NEWROOT, new ArgType(true, ARG_MEM, ARG_MEM, ARG_ATOM));}
	
	/** movecells [dstmem, srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * �ʿ��������ʤ�����$srcmem�ˤ������ƤΥ��ȥ�Ȼ���ʥ�å���������Ƥ��ʤ��ˤ���$dstmem�˰�ư���롣
	 * ����ϥ롼�����ľ������ޤǺƵ�Ū�˰�ư����롣�ۥ��ȴְ�ư������ϳ���������롣
	 * <p>�¹Ը塢��$srcmem�Ϥ��Τޤ��Ѵ�����ʤ���Фʤ�ʤ�
	 * <strike>������ʪ�Υ롼�륻�åȤ˸¤껲�Ȥ��Ƥ褤����</strike>
	 * <p>�¹Ը塢��$dstmem�����ƤΥ����ƥ��֥��ȥ�򥨥󥭥塼��ľ���٤��Ǥ��롣
	 * <p><b>���</b>��Ruby�Ǥ�pour����̾���ѹ�
	 * <p>moveCellsFrom�᥽�åɤ�ƤӽФ���
	 * @see enqueueallatoms */
	public static final int MOVECELLS = 54;
	// LOCALMOVECELLS�Ϻ�Ŭ���θ��̤�̵������Ѳ������뤤�Ϥ�����ò��������ͤˤ��롣
	static {setArgType(MOVECELLS, new ArgType(false, ARG_MEM, ARG_MEM));}

	/** enqueueallatoms [srcmem]
	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
	 * ���⤷�ʤ����ޤ��ϡ���$srcmem�ˤ������ƤΥ����ƥ��֥��ȥ�򤳤���μ¹ԥ��ȥॹ���å����Ѥࡣ
	 * <p>���ȥब�����ƥ��֤��ɤ�����Ƚ�Ǥ���ˤϡ�
	 * �ե��󥯥���ưŪ����������ˡ�ȡ�2�ĤΥ��롼�פΥ��ȥब����Ȥ��ƽ�°�줬����������ˡ�����롣
	 * @see enqueueatom */
	public static final int ENQUEUEALLATOMS = 55;
	// LOCALENQUEUEALLATOMS�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	static {setArgType(ENQUEUEALLATOMS, new ArgType(false, ARG_MEM));}

	/** freemem [srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * ��$srcmem���Ѵ����롣
	 * <p>$srcmem���ɤ���ˤ�°���������ĥ����å����Ѥޤ�Ƥ��ʤ����Ȥ�ɽ����
	 * @see freeatom */
	public static final int FREEMEM = 56;
	// LOCALFREEMEM������
	static {setArgType(FREEMEM, new ArgType(false, ARG_MEM));}

	/** addmem [dstmem, srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * ��å����줿�ʿ����̵������$srcmem��ʳ��������줿����$dstmem�˰�ư���롣
	 * ����Υ�å��ϼ������Ƥ��ʤ���ΤȤ��롣
	 * ����ϥ롼�����ľ������ޤǺƵ�Ū�˰�ư����롣�ۥ��ȴְ�ư������ϳ���������롣
	 * <p>��$srcmem������Ѥ��뤿��˻��Ѥ���롣
	 * <p>newmem�Ȱ㤤��$srcmem�Υ�å�������Ū�˲������ʤ���Фʤ�ʤ���
	 * <p>moveTo�᥽�åɤ�ƤӽФ���
	 * @see unlockmem, enqueuemem
	 */
	public static final int ADDMEM = 57;
	static {setArgType(ADDMEM, new ArgType(false, ARG_MEM, ARG_MEM));}

	/** enqueuemem [srcmem]
	 * ��å����줿��$srcmem���å������ޤ޳��������롣
	 * ���ξ��γ������ϡ�$srcmem���롼����ξ�硢���μ¹��쥹���å����Ѥळ�Ȥ��̣����
	 * �롼����Ǥʤ���硢�����Ʊ���¹��쥹���å����Ѥळ�Ȥ��̣���롣
	 * ���Ǥ˼¹��쥹���å��ޤ��ϲ��μ¹��쥹���å����Ѥޤ�Ƥ�����ϡ����⤷�ʤ���
	 */	
	public static final int ENQUEUEMEM = 58;
	static {setArgType(ENQUEUEMEM, new ArgType(false, ARG_MEM));}

	/** localaddmem [dstmem, srcmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * addmem��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣$dstmem�ˤĤ��Ƥϲ��Ⲿ�ꤷ�ʤ���*/
	public static final int LOCALADDMEM = LOCAL + ADDMEM;
	static {setArgType(LOCALADDMEM, new ArgType(false, ARG_MEM, ARG_MEM));}

	/** unlockmem [srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * �ʳ�������������$srcmem�Υ�å���������롣
	 * $srcmem���롼����ξ�硢���μ¹��쥹���å������Ƥ�¹��쥹���å������ž�����롣
	 * <p>addmem�ˤ�äƺ����Ѥ��줿�졢�����newroot�ˤ�äƥ롼��ǿ������������줿
	 * �롼������Ф��ơ��ʻ�¹������֤ˡ�ɬ���ƤФ�롣
	 * <p>�¹Ը塢$srcmem�ؤλ��Ȥ��Ѵ����ʤ���Фʤ�ʤ���*/
	public static final int UNLOCKMEM = 59;
	static {setArgType(UNLOCKMEM, new ArgType(false, ARG_MEM));}

	/** localunlockmem [srcmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * unlockmem��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALUNLOCKMEM = LOCAL + UNLOCKMEM;
	static {setArgType(LOCALUNLOCKMEM, new ArgType(false, ARG_MEM));}

	/** setmemname [dstmem, name]
	 * <br>�ܥǥ�̿��<br>
	 * ��$dstmem��̾����ʸ����ʤޤ���null��name�����ꤹ�롣
	 * <p>���ߡ����̾���λ�����Ū��ɽ���ѤΤߡ������졢��̾���Ф���ޥå��󥰤��Ǥ���褦�ˤʤ�Ϥ���*/
	public static final int SETMEMNAME = 60;
	static {setArgType(SETMEMNAME, new ArgType(false, ARG_MEM, ARG_OBJ));}

	/** localsetmemname [dstmem, name]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * setmemname��Ʊ����������$dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALSETMEMNAME = LOCAL + SETMEMNAME;
	static {setArgType(LOCALSETMEMNAME, new ArgType(false, ARG_MEM, ARG_OBJ));}

	// ͽ�� (61--62)

	// ��󥯤˴ط�������Ϥ��륬����̿�� (63--64)
	
	//	-----  getlink   [-link,atom,pos]
	//	-----  alloclink [-link,atom,pos]

	/** getlink [-link, atom, pos]
	 * <br>���Ϥ��뼺�Ԥ��ʤ���ĥ������̿�ᡢ��Ŭ���ѥܥǥ�̿��<br>
	 * ���ȥ�$atom����pos�����˳�Ǽ���줿��󥯥��֥������Ȥؤλ��Ȥ�$link���������롣
	 * <p>ŵ��Ū�ˤϡ�$atom�ϥ롼��إåɤ�¸�ߤ��롣*/
	public static final int GETLINK = 63;
	// LOCALGETLINK������
	static {setArgType(GETLINK, new ArgType(true, ARG_VAR, ARG_ATOM, ARG_INT));}

	/** alloclink [-link, atom, pos]
	 * <br>���Ϥ��뼺�Ԥ��ʤ���ĥ������̿�ᡢ��Ŭ���ѥܥǥ�̿��<br>
	 * ���ȥ�$atom����pos������ؤ���󥯥��֥������Ȥ������������Ȥ�$link���������롣
	 * <p>ŵ��Ū�ˤϡ�$atom�ϥ롼��ܥǥ���¸�ߤ��롣*/
	public static final int ALLOCLINK = 64;
	// LOCALGETLINK������
	static {setArgType(ALLOCLINK, new ArgType(true, ARG_VAR, ARG_ATOM, ARG_INT));}

	// ��󥯤�����ܥǥ�̿�� (65--69)
	// [local]newlink     [atom1, pos1, atom2, pos2, mem1]
	// [local]relink      [atom1, pos1, atom2, pos2, mem]
	// [local]unify       [atom1, pos1, atom2, pos2, mem]
	// [local]inheritlink [atom1, pos1, link2, mem]
	// [local]unifylinks  [link1, link2, mem]

	/** newlink [atom1, pos1, atom2, pos2, mem1]
	 * <br>�ܥǥ�̿��<br>
	 * ���ȥ�$atom1����$mem1�ˤ���ˤ���pos1�����ȡ�
	 * ���ȥ�$atom2����pos2�����δ֤�ξ������󥯤�ĥ�롣
	 * <p>ŵ��Ū�ˤϡ�$atom1��$atom2�Ϥ������롼��ܥǥ���¸�ߤ��롣
	 * <p><b>���</b>��Ruby�Ǥ���������������ѹ����줿��
	 * <p>alloclink[link1,atom1,pos1];alloclink[link2,atom2,pos2];unifylinks[link1,link2,mem1]��Ʊ����*/
	public static final int NEWLINK = 65;
	static {setArgType(NEWLINK, new ArgType(false, ARG_ATOM, ARG_INT, ARG_ATOM, ARG_INT, ARG_MEM));}

	/** localnewlink [atom1, pos1, atom2, pos2 (,mem1)]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * newlink��Ʊ������������$mem1�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALNEWLINK = LOCAL + NEWLINK;
	static {setArgType(LOCALNEWLINK, new ArgType(false, ARG_ATOM, ARG_INT, ARG_ATOM, ARG_INT, ARG_MEM));}

	/** relink [atom1, pos1, atom2, pos2, mem]
	 * <br>�ܥǥ�̿��<br>
	 * ���ȥ�$atom1����$mem�ˤ���ˤ���pos1�����ȡ�
	 * ���ȥ�$atom2����pos2�����Υ�������$mem�ˤ���ˤΰ�������³���롣
	 * <p>ŵ��Ū�ˤϡ�$atom1�ϥ롼��ܥǥ��ˡ�$atom2�ϥ롼��إåɤ�¸�ߤ��롣
	 * <p>���դ��ץ���ʸ̮��̵���롼��Ǥϡ��Ĥͤ�$mem������ʤΤ�localrelink�����ѤǤ��롣
	 * <p>�¹Ը塢$atom2[pos2]�����Ƥ�̵���ˤʤ롣
	 * <p>getlink[link2,atom2,pos2];inheritlink[atom1,pos1,link2,mem]��Ʊ����
	 * <p>alloclink[link1,atom1,pos1];getlink[link2,atom2,pos2];unifylinks[link1,link2,mem]��Ʊ����*/
	public static final int RELINK = 66;
	static {setArgType(RELINK, new ArgType(false, ARG_ATOM, ARG_INT, ARG_ATOM, ARG_INT, ARG_MEM));}

	/** localrelink [atom1, pos1, atom2, pos2 (,mem)]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * relink��Ʊ������������$mem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALRELINK = LOCAL + RELINK;
	static {setArgType(LOCALRELINK, new ArgType(false, ARG_ATOM, ARG_INT, ARG_ATOM, ARG_INT, ARG_MEM));}

	/** unify [atom1, pos1, atom2, pos2, mem]
	 * <br>�ܥǥ�̿��<br>
	 * ���ȥ�$atom1����pos1�����Υ����<strike>����$mem�ˤ����</strike>�ΰ����ȡ�
	 * ���ȥ�$atom2����pos2�����Υ����<strike>����$mem�ˤ����</strike>�ΰ�������³���롣
	 * <strike>������$atom1�����$atom2�Υ���褬�ɤ�����°�������ʤ����������Ƥ��ꡢ
	 * ���ξ�硢���⤷�ʤ��ǽ���äƤ�褤���ȤˤʤäƤ��롣����� f(A,A),(f(X,Y):-X=Y) �ν񤭴����ʤɤǵ����롣</strike>
	 * $atom1 �� $atom2 ��ξ���⤷���ϰ�������°�������ʤ����⤢�롣
	 * ����� a(A),f(A,B),(a(X),f(Y,Z):-Y=Z,b(X)) �ν񤭴����ʤɤǵ����롣
	 * <p>ŵ��Ū�ˤϡ�$atom1��$atom2�Ϥ������롼��إåɤ�¸�ߤ��롣
	 * <p>���դ��ץ���ʸ̮��̵���롼��Ǥϡ��Ĥͤ�$mem������ʤΤ�localunify�����ѤǤ��롣
	 * <p>getlink[link1,atom1,pos1];getlink[link2,atom2,pos2];unifylinks[link1,link2,mem]��Ʊ����*/
	public static final int UNIFY = 67;
	static {setArgType(UNIFY, new ArgType(false, ARG_ATOM, ARG_INT, ARG_ATOM, ARG_INT, ARG_MEM));}

	/** localunify [atom1, pos1, atom2, pos2 (,mem)]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * unify��Ʊ������������$mem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALUNIFY = LOCAL + UNIFY;
	static {setArgType(LOCALUNIFY, new ArgType(false, ARG_ATOM, ARG_INT, ARG_ATOM, ARG_INT, ARG_MEM));}

	/** inheritlink [atom1, pos1, link2, mem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * ���ȥ�$atom1����$mem�ˤ���ˤ���pos1�����ȡ�
	 * ���$link2�Υ�������$mem�ˤ���ˤ���³���롣
	 * <p>ŵ��Ū�ˤϡ�$atom1�ϥ롼��ܥǥ���¸�ߤ���$link2�ϥ롼��إåɤ�¸�ߤ��롣relink�����ѡ�
	 * <p>���դ��ץ���ʸ̮��̵���롼��Ǥϡ��Ĥͤ�$mem������ʤΤ�localinheritrelink�����ѤǤ��롣
	 * <p>$link2�Ϻ����Ѥ���뤿�ᡢ�¹Ը��$link2���Ѵ����ʤ���Фʤ�ʤ���
	 * <p>alloclink[link1,atom1,pos1];unifylinks[link1,link2,mem]��Ʊ����
	 * @see getlink */
	public static final int INHERITLINK = 68;
	static {setArgType(INHERITLINK, new ArgType(false, ARG_ATOM, ARG_INT, ARG_VAR, ARG_MEM));}

	/** localinheritlink [atom1, pos1, link2 (,mem)]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * inheritlink��Ʊ������������$mem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALINHERITLINK = LOCAL + INHERITLINK;
	static {setArgType(LOCALINHERITLINK, new ArgType(false, ARG_ATOM, ARG_INT, ARG_VAR, ARG_MEM));}

	/** unifylinks [link1, link2, mem]
	 * <br>�ܥǥ�̿��<br>
	 * ���$link1�λؤ����ȥ�����ȥ��$link2�λؤ����ȥ�����Ȥδ֤��������Υ�󥯤�ĥ�롣
	 * ������$link1����$mem�Υ��ȥ��ؤ��Ƥ��뤫���ޤ��Ͻ�°���̵�����ȥ��ؤ��Ƥ��롣
	 * ��Ԥξ�硢���⤷�ʤ��ǽ���äƤ�褤���ȤˤʤäƤ��롣
	 * <p>todo ̿��β�����mem�������Ȥ��뤳�ȤϤʤ��Τǡ������˴ޤ�ʤ��褦�ˤ��������褤��
	 * <p>�¹Ը�$link1�����$link2��̵���ʥ�󥯥��֥������ȤȤʤ뤿�ᡢ���Ȥ���Ѥ��ƤϤʤ�ʤ���
	 * <p>�����ǡ������Υ���ѥ���ǻ��Ѥ���롣*/
	public static final int UNIFYLINKS = 69;
	static {setArgType(UNIFYLINKS, new ArgType(false, ARG_VAR, ARG_VAR, ARG_MEM));}

	/** localunifylinks [link1, link2 (,mem)]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * unifylinks��Ʊ������������$mem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALUNIFYLINKS = LOCAL + UNIFYLINKS;
	static {setArgType(LOCALUNIFYLINKS, new ArgType(false, ARG_VAR, ARG_VAR, ARG_MEM));}

    // ��ͳ��󥯴������ȥ༫ư�����Τ���Υܥǥ�̿�� (70--74)
	//  -----  removeproxies          [srcmem]
	//  -----  removetoplevelproxies  [srcmem]
	//  -----  insertproxies          [parentmem,childmem]
	//  -----  removetemporaryproxies [srcmem]

    /** removeproxies [srcmem]
     * <br>�ܥǥ�̿��<br>
     * $srcmem���̤�̵�ط��ʼ�ͳ��󥯴������ȥ��ư������롣
     * <p>removemem��ľ���Ʊ������Ф��ƸƤФ�롣*/
    public static final int REMOVEPROXIES = 70;
    // LOCALREMOVEPROXIES�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	static {setArgType(REMOVEPROXIES, new ArgType(false, ARG_MEM));}

    /** removetoplevelproxies [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��$srcmem������ˤ��̲ᤷ�Ƥ���̵�ط��ʼ�ͳ��󥯴������ȥ�����롣
	 * <p>removeproxies�����ƽ���ä���ǸƤФ�롣*/
    public static final int REMOVETOPLEVELPROXIES = 71;
	// LOCALREMOVETOPLEVELPROXIES�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	static {setArgType(REMOVETOPLEVELPROXIES, new ArgType(false, ARG_MEM));}

    /** insertproxies [parentmem,childmem]
     * <br>�ܥǥ�̿��<br>
     * ���ꤵ�줿��֤˼�ͳ��󥯴������ȥ��ư�������롣
     * <p>addmem�����ƽ���ä���ǸƤФ�롣*/
    public static final int INSERTPROXIES = 72;
	// LOCALINSERTPROXIES�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	static {setArgType(INSERTPROXIES, new ArgType(false, ARG_MEM, ARG_MEM));}
	
    /** removetemporaryproxies [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��$srcmem������ˤ˻Ĥ��줿"star"���ȥ�����롣
     * <p>insertproxies�����ƽ���ä���ǸƤФ�롣*/
    public static final int REMOVETEMPORARYPROXIES = 73;
	// LOCALREMOVETEMPORARYPROXIES�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	static {setArgType(REMOVETEMPORARYPROXIES, new ArgType(false, ARG_MEM));}

	// �롼�������ܥǥ�̿�� (75--79)
	// [local]loadruleset [dstmem, ruleset]
	// [local]copyrules   [dstmem, srcmem]
	// [local]clearrules  [dstmem]

	/** loadruleset [dstmem, ruleset]
	 * <br>�ܥǥ�̿��<br>
	 * �롼�륻�å�ruleset����$dstmem�˥��ԡ����롣
	 * <p>������Υ����ƥ��֥��ȥ�Ϻƥ��󥭥塼���٤��Ǥ��롣
	 * @see enqueueallatoms */
	public static final int LOADRULESET = 75;
	static {setArgType(LOADRULESET, new ArgType(false, ARG_MEM, ARG_OBJ));}
	
	/** localloadruleset [dstmem, ruleset]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * loadruleset��Ʊ����������$dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALLOADRULESET = LOCAL + LOADRULESET;
	static {setArgType(LOCALLOADRULESET, new ArgType(false, ARG_MEM, ARG_OBJ));}

	/** copyrules [dstmem, srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * ��$srcmem�ˤ������ƤΥ롼�����$dstmem�˥��ԡ����롣
	 * <p><b>���</b>��Ruby�Ǥ�inheritrules����̾���ѹ� */
	public static final int COPYRULES = 76;
	static {setArgType(COPYRULES, new ArgType(false, ARG_MEM, ARG_MEM));}

	/** localcopyrules [dstmem, srcmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * copyrules��Ʊ����������$dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣$srcmem�ˤĤ��Ƥϲ��Ⲿ�ꤷ�ʤ���*/
	public static final int LOCALCOPYRULES = LOCAL + COPYRULES;
	static {setArgType(LOCALCOPYRULES, new ArgType(false, ARG_MEM, ARG_MEM));}

	/** clearrules [dstmem]
	 * <br>�ܥǥ�̿��<br>
	 * ��$dstmem�ˤ������ƤΥ롼���õ�롣*/
	public static final int CLEARRULES = 77;
	static {setArgType(CLEARRULES, new ArgType(false, ARG_MEM));}
	
	/** localclearrules [dstmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * clearrules��Ʊ����������$dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALCLEARRULES = LOCAL + CLEARRULES;
	static {setArgType(LOCALCLEARRULES, new ArgType(false, ARG_MEM));}

	/** loadmodule [dstmem, ruleset]
	 * <br>�ܥǥ�̿��<br>
	 * �롼�륻�å�ruleset����$dstmem�˥��ԡ����롣
	 */
	public static final int LOADMODULE = 78;
	static {setArgType(LOADMODULE, new ArgType(false, ARG_MEM, ARG_OBJ));}

    // ���դ��Ǥʤ��ץ���ʸ̮�򥳥ԡ��ޤ����Ѵ����뤿���̿�� (80--89)
	//  ----- recursivelock            [srcmem]
	//  ----- recursiveunlock          [srcmem]
	//  ----- copymem         [-dstmap, dstmem, srcmem]
	//  ----- dropmem                  [srcmem]
	
    /** recursivelock [srcmem]
     * <br>��ͽ�󤵤줿�˥�����̿��<br>
     * ��$srcmem�����Ƥλ�����Ф��ƺƵ�Ū�˥�å���������롣
     * <p>���դǤνи���1��Ǥʤ��ץ���ʸ̮���񤫤줿���դ�����Ф��ƻ��Ѥ���롣
     * <p><font color=red><b>
     * �ǥåɥ�å���������ʤ����Ȥ��ݾڤǤ���С�����̿��ϥ֥�å��󥰤ǹԤ��٤��Ǥ��롣
     * </b></font>*/
    public static final int RECURSIVELOCK = 80;
	// LOCALRECURSIVELOCK�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	static {setArgType(RECURSIVELOCK, new ArgType(false, ARG_MEM));}

    /** recursiveunlock [srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * ��$srcmem�����Ƥλ�����Ф��ƺƵ�Ū�˥�å���������롣
     * ��Ϥ����������륿�����μ¹��쥹���å��˺Ƶ�Ū���Ѥޤ�롣
     * <p>�Ƶ�Ū���Ѥ���ˡ�ϡ�����ͤ��롣
     * @see unlockmem */
    public static final int RECURSIVEUNLOCK = 81;
	// LOCALRECURSIVEUNLOCK�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	static {setArgType(RECURSIVEUNLOCK, new ArgType(false, ARG_MEM));}

    /** copycells [-dstmap, dstmem, srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * �Ƶ�Ū�˥�å����줿��$srcmem�����ƤΥ��ԡ��������,��$dstmem�������.
     * ���κݡ�����褬�������(�����ޤ��)���̵�����ȥ�ξ����
     * ���ԡ�����륢�ȥ४�֥������� -> ���ԡ����줿���ȥ४�֥�������
     * (2005/01/13 �����Atom.id����λ��Ȥ��ѹ�)
     * �Ȥ���Map���֥������ȤȤ���,dstmap�������.
     **/
    public static final int COPYCELLS = 82;
	// LOCALCOPYMEM�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	static {setArgType(COPYCELLS, new ArgType(true, ARG_VAR, ARG_MEM, ARG_MEM));}

	/** dropmem [srcmem]
	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
	 * �Ƶ�Ū�˥�å����줿��$srcmem���˴����롣
	 * ��������¹�����롼����Ȥ��륿�����϶�����λ���롣*/
	public static final int DROPMEM = 83;
	// LOCALDROPMEM�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	static {setArgType(DROPMEM, new ArgType(false, ARG_MEM));}

	/** lookuplink [-dstlink, srcmap, srclink]
	 * srclink�Υ����Υ��ȥ�Υ��ԡ���$srcmap������ơ�
	 * ���Υ��ȥ������Ȥ���-dstlink���ä��֤���
	 */
	public static final int LOOKUPLINK = 84;
	static {setArgType(LOOKUPLINK, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}

	/** insertconnectors[-dstset,linklist,mem]
	 * $linklist�ꥹ�Ȥγ��ѿ��ֹ�ˤϥ�󥯥��֥������Ȥ���Ǽ����Ƥ��롣
	 * �����Υ�󥯥��֥������ȤΥ�����$mem��Υ��ȥ�Ǥ��롣
	 * �����Υ�󥯥��֥������Ȥ����Ƥ��Ȥ߹�碌���Ф���buddy�δط��ˤ��뤫�ɤ����򸡺�����
	 * ���ξ��ˤ�'='���ȥ���������롣
	 * �����������ȥ��$dstset���ɲä��롣
	 */
	public static final int INSERTCONNECTORS = 85;
	static {setArgType(INSERTCONNECTORS, new ArgType(true, ARG_VAR, ARG_VARS, ARG_MEM));}
	
	/** deleteconnectors[srcset,srcmap,srcmem]
	 * $srcset�˴ޤޤ��'='���ȥ�򥳥ԡ��������ȥ��$srcmap�������ơ�
	 * ���������󥯤�Ĥʤ��ʤ�����$srcmem�ϥ��ԡ�����졣
	 * 
	 * (ñ��$srcmem�˴ޤޤ��'='��������褦�ˤ���Ф褤����)
	 */
	public static final int DELETECONNECTORS = 86;
	static {setArgType(DELETECONNECTORS, new ArgType(false, ARG_VAR,ARG_VAR,ARG_MEM));}

//	/** * [srcatom,pos]
//	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
//	 * ���ȥ�srcatom����1�����Υ����Υ����Υ��ȥ����pos�����ȡ�
//	 * srcatom����1�����δ֤���������󥯤�Ž�롣��ͳ��󥯴������ȥ��ɤ����뤫��
//	 * <p>�ץ���ʸ̮�Υ��ԡ����˻��Ѥ���롣*/
//	public static final int * = err;

	// ���ȥླྀ�ĤΥ���ѥ���ˤϡ�findatom��run��Ȥ��Ȥ������⤷��ʤ������Ǥ��ʤ����⤷��ʤ���

	// ���ԡ��������relink���롣

	// ͽ�� (90--99)

	//////////////////////////////////////////////////////////////////
	
	// 200�ְʹߤ�̿��ˤ�LOCAL�����Ǥ�¸�ߤ��ʤ�
	
	// ����̿�� (200--209)
	//  -----  react       [ruleref,         [memargs...], [atomargs...], [varargs...]]
	//  -----  jump        [instructionlist, [memargs...], [atomargs...], [varargs...]]
	//  -----  commit      [ruleref]
	//  -----  resetvars   [[memargs...], [atomargs...], [varargs...]]
	//  -----  changevars  [[memargs...], [atomargs...], [varargs...]]
	//  -----  spec        [formals,locals]
	//  -----  proceed
	//  -----  stop 
	//  -----  branch      [instructionlist]
	//  -----  loop        [[instructions...]]
	//  -----  run         [[instructions...]]
	//  -----  not         [instructionslist]

	/** react [ruleref, [memargs...], [atomargs...], [varargs...]]
	 * <br>���Ԥ��ʤ�������̿��<br>
	 * �롼��ruleref���Ф���ޥå��󥰤������������Ȥ�ɽ����
	 * �����ϤϤ��Υ롼��Υܥǥ���ƤӽФ��ʤ���Фʤ�ʤ���
	 * <p>spec        [formals, locals];
	 *    resetvars   [[memargs...], [atomargs...], [varargs...]];
	 *    branch      [body] ��Ʊ����
	 * ������body��ruleref�Υܥǥ�̿����ǡ���Ƭ��̿���spec[formals,locals]
	 * <p>��̤����̿���*/
	public static final int REACT = 1200;
	static {setArgType(REACT, new ArgType(false, ARG_OBJ, ARG_VARS, ARG_VARS, ARG_VARS));}

	/** jump [instructionlist, [memargs...], [atomargs...], [varargs...]]
     * <br>����̿��<br>
     * ����ΰ�����ǥ�٥��դ�̿����instructionlist��ƤӽФ���
     * <p>
     * ���ꤷ��̿����μ¹Ԥ˼��Ԥ���ȡ�����̿�᤬���Ԥ��롣
     * ���ꤷ��̿����μ¹Ԥ���������ȡ������ǽ�λ���롣
     * <p>spec        [formals, locals];
     *    resetvars   [[memargs...], [atomargs...], [varargs...]];
     *    branch      [body] ��Ʊ����
     * ������body��instructionlist��̿����ǡ���Ƭ��̿���spec[formals,locals]*/
    public static final int JUMP = 200;
	static {setArgType(JUMP, new ArgType(false, ARG_LABEL, ARG_VARS, ARG_VARS, ARG_VARS));}

	/** commit [ruleref]
	 * <br>̵�뤵����Ŭ���Ѥ���ӥȥ졼����̿��<br>
	 * ���ߤμ°����٥����ǥ롼��ruleref���Ф���ޥå��󥰤������������Ȥ�ɽ����
	 * �����Ϥϡ�����̿�����ã����ޤǤ˹Ԥä����Ƥ�ʬ�������˺�Ѥ��Ƥ褤��*/
	public static final int COMMIT = 201;
	static {setArgType(COMMIT, new ArgType(false, ARG_OBJ));}

	/** resetvars [[memargs...], [atomargs...], [varargs...]]
	 * <br>���Ԥ��ʤ�������̿�ᤪ��Ӻ�Ŭ���ѥܥǥ�̿��<br>
	 * �ѿ��٥��������Ƥ��������롣�������ѿ��ֹ�ϡ��졢���ȥࡢ����¾�ν��֤�0���鿶��ľ����롣
	 * <b>���</b>��memargs[0]�����줬ͽ�󤷤Ƥ��뤿���ѹ����ƤϤʤ�ʤ���
	 */
	public static final int RESETVARS = 202;
	static {setArgType(RESETVARS, new ArgType(false, ARG_VARS, ARG_VARS, ARG_VARS));}

	/** changevars [[memargs...], [atomargs...], [varargs...]]
	 * <br>���Ԥ��ʤ�������̿�ᤪ��Ӻ�Ŭ���ѥܥǥ�̿��<br>
	 * �ѿ��٥��������Ƥ����Ĺ�����������롣
	 * �������ѿ��ֹ�ϡ��졢���ȥࡢ����¾�Τ������0���鿶��ľ����롣
	 * ������null�����äƤ������Ǥ�̵�뤵��롣
	 * Ʊ���ֹ�˰ۤʤ����Υ��֥������Ȥ������ʤ��褦����դ��뤳�ȡ�
	 * <b>���</b>��memargs[0]�����줬ͽ�󤷤Ƥ��뤿���ѹ����ƤϤʤ�ʤ���
	 * <p>��̤����̿���
	 */
	public static final int CHANGEVARS = 1202;
	static {setArgType(CHANGEVARS, new ArgType(false, ARG_VARS, ARG_VARS, ARG_VARS));}

    /** spec [formals, locals]
     * <br>����̿��<br>
     * �������ȶɽ��ѿ��θĿ���������롣
     * �ɽ��ѿ��θĿ�����­���Ƥ����硢�ѿ��٥������ĥ���롣*/
    public static final int SPEC = 203;
	static {setArgType(SPEC, new ArgType(false, ARG_INT, ARG_INT));}

	/** proceed
	 * <br>�ܥǥ�̿��<br>
	 * ����proceed̿�᤬��°����̿����μ¹Ԥ������������Ȥ�ɽ����
	 * <p>�ȥåץ�٥�̿����ǻ��Ѥ��줿��硢�롼���Ŭ�Ѥ�����ꡢ
	 * �����Ѥ��줿���Ƥ���Υ�å���������������������Ƥ����������������Ȥ�ɽ����
	 * <p><b>���</b>��proceed�ʤ���̿����ν�ü�ޤǿʤ����硢
	 * ����̿����μ¹Ԥϼ��Ԥ�����ΤȤ�����ͤ����Ѥ��줿��*/
	public static final int PROCEED = 204;
	static {setArgType(PROCEED, new ArgType(false));}

//	/** stop 
//	 * <br>��ͽ�󤵤줿�˼��Ԥ��ʤ�������̿��<br>
//	 * proceed��Ʊ�����ޥå��󥰤ȥܥǥ���̿�᤬���礵�줿���Ȥ�ȼ�ä��ѻߤ����ͽ�ꡣ
//	 * <p>ŵ��Ū�ˤϡ�������˥ޥå��������Ȥ�ɽ������˻��Ѥ���롣
//	 */
//	public static final int STOP = 205;
//	static {setArgType(STOP, new ArgType(false));}

    /** branch [instructionlist]
     * <br>��¤��̿��<br>
     * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����
     * �����¹���˼��Ԥ�����硢�����¹���˼���������å����������branch�μ���̿��˿ʤࡣ
     * �����¹����proceed̿���¹Ԥ�����硢�����ǽ�λ���롣*/
    public static final int BRANCH = 206;
	static {setArgType(BRANCH, new ArgType(false, ARG_LABEL));}

	/** loop [[instructions...]]
	 * <br>��¤��̿��<br>
	 * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����
     * �����¹���˼��Ԥ�����硢�����¹���˼���������å����������loop�μ���̿��˿ʤࡣ
     * �����¹����proceed̿���¹Ԥ�����硢����loop̿��μ¹Ԥ򷫤��֤���*/
	public static final int LOOP = 207;
	static {setArgType(LOOP, new ArgType(false, ARG_INSTS));}

	/** run [[instructions...]]
	 * <br>��ͽ�󤵤줿�˹�¤��̿��<br>
	 * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����������ϥ�å���������ƤϤʤ�ʤ���
	 * �����¹���˼��Ԥ�����硢run�μ���̿��˿ʤࡣ
	 * �����¹����proceed̿���¹Ԥ�����硢����̿��˿ʤࡣ
	 * <p>���衢����Ū�ʰ����դ��Υץ���ʸ̮�Υ���ѥ���˻��Ѥ��뤿���ͽ��*/
	public static final int RUN = 208;
	static {setArgType(RUN, new ArgType(false, ARG_INSTS));}

	/** not [instructionlist]
	 * <br>��ͽ�󤵤줿�˹�¤��̿��<br>
	 * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����������ϥ�å���������ƤϤʤ�ʤ���
	 * �����¹���˼��Ԥ�����硢not�μ���̿��˿ʤࡣ
	 * �����¹����proceed̿���¹Ԥ�����硢����̿�᤬���Ԥ��롣
	 * <p>���衢������Υ���ѥ���˻��Ѥ��뤿���ͽ��*/
	public static final int NOT = 209;
	static {setArgType(NOT, new ArgType(false, ARG_LABEL));}

	// �Ȥ߹��ߵ�ǽ�˴ؤ���̿��ʲ��� (210--215)
	//  -----  inline  [atom, inlineref]
	//  -----  builtin [class, method, [links...]]

	/** inline [atom, string, inlineref]
	 * <br>������̿�ᡢ�ܥǥ�̿��<br>
	 * ���ȥ�$atom���Ф��ơ�inlineref�����ꤹ�륤��饤��̿���Ŭ�Ѥ����������뤳�Ȥ��ǧ���롣
	 * <p>inlineref�ˤϸ��ߡ�����饤���ֹ���Ϥ����ȤˤʤäƤ��뤬��
	 * <p>�ܥǥ��ǸƤФ���硢ŵ��Ū�ˤϡ����ƤΥ�󥯤�ĥ��ľ����ľ��˸ƤФ�롣*/
	public static final int INLINE = 210;
	static {setArgType(INLINE, new ArgType(false, ARG_ATOM, ARG_OBJ, ARG_INT));}

	/** builtin [class, method, [links...]]
	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��
	 * ��󥯻���$(links[i])�������ͣ��ΰ����Ȥ���Java�Υ��饹�᥽�åɤ�ƤӽФ���
	 * <p>���󥿥ץ꥿ư���Ȥ����Ȥ߹��ߵ�ǽ���󶡤��뤿��˻��Ѥ��롣
	 * <p>�̾�ϡ�$builtin(class,method):(X1,��,Xn)���б����������μ���ˤ�äƼ��Τ�Τ��Ϥ���롣
	 * ���դ��ץ���ʸ̮�ϡ��줫�������إåɽи��ʤޤ��ϥ���������������Ρˤ��Ϥ���롣
	 * �إåɤȥܥǥ���1�󤺤Ľи������󥯤ϡ��إåɤǤΥ�󥯽и����Ϥ���롣
	 * �ܥǥ���2��и������󥯤ϡ�X=X�ǽ�������줿�塢�ƽи���إåɤǤνи��ȸ��ʤ����Ϥ���롣*/
	public static final int BUILTIN = 211;
	static {setArgType(BUILTIN, new ArgType(false, ARG_OBJ, ARG_OBJ, ARG_OBJ));} // ���äƤ롣

	///////////////////////////////////////////////////////////////////////

	// ���դ��ץ���ʸ̮�򰷤�������ɲ�̿�� (216--219)	

	/** eqground [link1,link2]
	 * <br>��ͽ�󤵤줿�˳�ĥ������̿��<br>
	 * �ʤɤ��餫�������ץ�����ؤ��Ȥ狼�äƤ����
	 * 2�ĤΥ��link1��link2���Ф��ơ�
	 * ����餬Ʊ�������δ����ץ����Ǥ��뤳�Ȥ��ǧ���롣
	 * @see isground */
	public static final int EQGROUND = 216;
	static {setArgType(EQGROUND, new ArgType(false, ARG_VAR, ARG_VAR));}

	/** copyground [-dstlink, srclink, dstmem]
	 * �ʴ����ץ�����ؤ��˥��$srclink��$dstmem��ʣ������$dstlink�˳�Ǽ���롣
	 * @see isground */
	public static final int COPYGROUND = 217;
	static {setArgType(COPYGROUND, new ArgType(true, ARG_VAR, ARG_VAR, ARG_MEM));}
		
	/** removeground [srclink,srcmem]
	 * $srcmem��°����ʴ����ץ�����ؤ��˥��$srclink�򸽺ߤ��줫����Ф���
	 * �¹ԥ��ȥॹ���å������ʤ���
	 */
	public static final int REMOVEGROUND = 218;
	static {setArgType(REMOVEGROUND, new ArgType(false, ARG_VAR, ARG_MEM));}
	
	/** freeground [srclink]
	 * �����ץ���$srclink���ɤ���ˤ�°���������ĥ����å����Ѥޤ�Ƥ��ʤ����Ȥ�ɽ����
	 */
	public static final int FREEGROUND = 219;
	static {setArgType(FREEGROUND, new ArgType(false, ARG_VAR));}
	
	// �������Τ���Υ�����̿�� (220--229)	

	/** isground [-natomsfunc, link, srcset]
	 * <br>��ͽ�󤵤줿�˥�å����������ĥ������̿��<br>
	 * ���$link�λؤ��褬�����ץ����Ǥ��뤳�Ȥ��ǧ���롣
	 * ���ʤ��������褫�����餺�ˡ���ã��ǽ�ʥ��ȥब���Ƥ������¸�ߤ��Ƥ��뤳�Ȥ��ǧ���롣
	 * ��������$srcset����Ͽ���줿���ȥ����ã�����鼺�Ԥ��롣
	 * ���Ĥ��ä������ץ����������뤳����Υ��ȥ�θĿ��ʤ��åפ���Integer�ˤ�$natoms�˳�Ǽ���롣
     * 
     * <p>natoms��nmems�����礷��̿����ꡢ$natoms�����¤�������Ϥ��褦�ˤ��롣
     * ����θĿ��ξȹ�ϡ����줬��å����Ƥ��ʤ�����θĿ���0�Ĥ��ɤ���Ĵ�٤�Ф褤��
     * ���������줬��å��������ɤ�����Ĵ�٤�ᥫ�˥��ब��������äƤ��ʤ����ᡢ��α��
     * 
     * ground�ˤ���ϴޤޤ�ʤ����Ȥˤʤä��Τǡ��嵭������
     * */
	public static final int ISGROUND = 220;
	static {setArgType(ISGROUND, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	
	/** isunary [atom]
	 * <br>������̿��<br>
	 * ���ȥ�$atom��1�����Υ��ȥ�Ǥ��뤳�Ȥ��ǧ���롣*/
	public static final int ISUNARY     = 221;
	public static final int ISUNARYFUNC = ISUNARY + OPT;
	static {setArgType(ISUNARY, new ArgType(false, ARG_ATOM));}
	static {setArgType(ISUNARYFUNC, new ArgType(false, ARG_ATOM));}

	/** isint [atom]
	 * <br>������̿��<br>
	 * ���ȥ�$atom���������ȥ�Ǥ��뤳�Ȥ��ǧ���롣*/
	public static final int ISINT    = 225;
	public static final int ISFLOAT  = 226;
	public static final int ISSTRING = 227;
	static {setArgType(ISINT, new ArgType(false, ARG_ATOM));}
	static {setArgType(ISFLOAT, new ArgType(false, ARG_ATOM));}
	static {setArgType(ISSTRING, new ArgType(false, ARG_ATOM));}

	/** isintfunc [func]
	 * <br>��Ŭ���ѥ�����̿��<br>
	 * �ե��󥯥�$func�������ե��󥯥��Ǥ��뤳�Ȥ��ǧ���롣*/
	public static final int ISINTFUNC    = ISINT    + OPT;
	public static final int ISFLOATFUNC  = ISFLOAT  + OPT;
	public static final int ISSTRINGFUNC = ISSTRING + OPT;
	static {setArgType(ISINTFUNC, new ArgType(false, ARG_VAR));}
	static {setArgType(ISFLOATFUNC, new ArgType(false, ARG_VAR));}
	static {setArgType(ISSTRINGFUNC, new ArgType(false, ARG_VAR));}

	/** getclass [-stringatom, atom]
	 * <br>���Ϥ��륬����̿��<br>
	 * ���ȥ�$atom��ObjectFunctor�ޤ��Ϥ��Υ��֥��饹�Υե��󥯥�����Ĥ��Ȥ��ǧ����
	 * ��Ǽ���줿���֥������ȤΥ��饹�δ�������̾ʸ�����ɽ���ե��󥯥�����ĥ��ȥ����������
	 * $stringatom���������롣
	 * ��������Translator �����Ѥ�����硢Ʊ�쥽������Inline�����ɤ�������줿���饹�˴ؤ��Ƥ�ñ��̾��������롣(2005/10/17 Mizuno )*/
	public static final int GETCLASS = 228;
	static {setArgType(GETCLASS, new ArgType(true, ARG_ATOM, ARG_ATOM));}
	/** getclassfunc [-stringfunc, func]
	 * <br>���Ϥ��륬����̿��<br>
	 * �ե��󥯥�$func��ObjectFunctor�ޤ��Ϥ��Υ��֥��饹�Ǥ��뤳�Ȥ��ǧ����
	 * ��Ǽ���줿���֥������ȤΥ��饹�δ�������ʽ�����̾ʸ�����ɽ���ե��󥯥�����������
	 * $stringfunc���������롣
	 * ��������Translator �����Ѥ�����硢Ʊ�쥽������Inline�����ɤ�������줿���饹�˴ؤ��Ƥ�ñ��̾��������롣(2005/10/17 Mizuno )*/
	public static final int GETCLASSFUNC = 228 + OPT;
	static {setArgType(GETCLASSFUNC, new ArgType(true, ARG_VAR, ARG_VAR));}

	///////////////////////////////////////////////////////////////////////

	// ʬ����ĥ�Ѥ�̿�� (230--239)
	/** getruntime [-dstatom, srcmem]
	 * <br>���Ԥ��ʤ�ʬ����ĥ�ѥ�����̿��<br>
	 * ��$srcmem�ʤ�������륿�����ˤ���°����Ρ��ɤ�ɽ��ʸ����ե��󥯥������
	 * ��°�������ʤ�ʸ���󥢥ȥ����������$dstatom���������롣
	 * �������嵭�λ��ͤϥ롼����ΤȤ��Τߡ��롼����Ǥʤ�����Ф��Ƥ϶�ʸ���������롣
	 * <p>�롼��κ��դ�{..}@H������Ȥ��˻��Ѥ���롣ʸ�����Ȥ��Τϲ����ͤ��������餯�Ѥ��ʤ���*/
	public static final int GETRUNTIME = 230;
	static {setArgType(GETRUNTIME, new ArgType(true, ARG_ATOM, ARG_MEM));}
	
	/** connectruntime [srcatom]
	 * 	 	 * <br>ʬ����ĥ�ѥ�����̿��<br>
	 * ���ȥ�$srcatom��ʸ����ե��󥯥�����Ĥ��Ȥ��ǧ����
	 * ����ʸ����ɽ���Ρ��ɤ���³�Ǥ��뤳�Ȥ��ǧ���롣
	 * <p>ʸ���󤬶��ΤȤ��ϤĤͤ��������롣
	 * <p>�롼��α��դ�{..}@H������Ȥ��˻��Ѥ���롣ʸ�����Ȥ��Τϲ����ͤ��������餯�Ѥ��ʤ���
	 *
	 * �ɵ�(nakajima: 2004-01-12) 
	 * ��ˡ7��ʬ���ǡˤǤϡ�����ۥ��Ȥ�VM��̵���ä����롣���ä�����¸��ǧ��
	 * ������1�Ǥϡ���¸��ǧ�Τߡ����������ϥܥǥ�̿��˰�ư��
	 * */
	public static final int CONNECTRUNTIME = 231;
	static {setArgType(CONNECTRUNTIME, new ArgType(false, ARG_ATOM));}

	/////////////////////////////////////////////////////////////////////////
	
	// ���ȥॻ�åȤ����뤿���̿�� ( 240--249)
	
	/** newset [-dstset]
	 * ���������ȥॻ�åȤ���
	 */
	public static final int NEWSET = 240;
	static {setArgType(NEWSET, new ArgType(true,ARG_VAR));}
	
	/** addatomtoset [srcset, atom]
	 * $atom�򥢥ȥॻ�å�$srcset���ɲä���
	 */
	public static final int ADDATOMTOSET = 241;
	static {setArgType(ADDATOMTOSET, new ArgType(false, ARG_VAR, ARG_ATOM));}


	///////////////////////////////////////////////////////////////////////

	// �����Ѥ��Ȥ߹��ߥܥǥ�̿�� (400--419+OPT)
	/** iadd [-dstintatom, intatom1, intatom2]
	 * <br>�����Ѥ��Ȥ߹���̿��<br>
	 * �������ȥ�βû���̤�ɽ����°�������ʤ��������ȥ����������$dstintatom���������롣
	 * <p>idiv�����imod�˸¤꼺�Ԥ��롣*/
	public static final int IADD = 400;
	public static final int ISUB = 401;
	public static final int IMUL = 402;
	public static final int IDIV = 403;
	public static final int INEG = 404;
	public static final int IMOD = 405;
	public static final int INOT = 410;
	public static final int IAND = 411;
	public static final int IOR  = 412;
	public static final int IXOR = 413;
	static {setArgType(IADD, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(ISUB, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(IMUL, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(IDIV, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(INEG, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(IMOD, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(INOT, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(IAND, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(IOR, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(IXOR, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	
	/** isal [-dstintatom, intatom1, intatom2]
	 * <br>�����Ѥ��Ȥ߹���̿��<br>
	 * $intatom1��$intatom2�ӥå�ʬ���Ĥ�(����)�����եȤ�����̤�ɽ����°�������ʤ��������ȥ����������$dstintatom���������롣*/
	public static final int ISAL = 414;
	static {setArgType(ISAL, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	/** isar [-dstintatom, intatom1, intatom2]
	 * <br>�����Ѥ��Ȥ߹���̿��<br>
	 * $intatom1��$intatom2�ӥå�ʬ���Ĥ�(����)�����եȤ�����̤�ɽ����°�������ʤ��������ȥ����������$dstintatom���������롣*/
	public static final int ISAR = 415;
	static {setArgType(ISAR, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	/** ishr [-dstintatom, intatom1, intatom2]
	 * <br>�����Ѥ��Ȥ߹���̿��<br>
	 * $intatom1��$intatom2�ӥå�ʬ���������եȤ�����̤�ɽ����°�������ʤ��������ȥ����������$dstintatom���������롣*/
	public static final int ISHR = 416;
	static {setArgType(ISHR, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}

	/** iaddfunc [-dstintfunc, intfunc1, intfunc2]
	 * <br>�����Ѥκ�Ŭ�����Ȥ߹���̿��<br>
	 * �����ե��󥯥��βû���̤�ɽ�������ե��󥯥�����������$dstintfunc���������롣
	 * <p>idivfunc�����imodfunc�˸¤꼺�Ԥ��롣*/
	public static final int IADDFUNC = IADD + OPT;
	public static final int ISUBFUNC = ISUB + OPT;
	public static final int IMULFUNC = IMUL + OPT;
	public static final int IDIVFUNC = IDIV + OPT;
	public static final int INEGFUNC = INEG + OPT;
	public static final int IMODFUNC = IMOD + OPT;
	public static final int INOTFUNC = INOT + OPT;
	public static final int IANDFUNC = IAND + OPT;
	public static final int IORFUNC  = IOR  + OPT;
	public static final int IXORFUNC = IXOR + OPT;
	public static final int ISALFUNC = ISAL + OPT;
	public static final int ISARFUNC = ISAR + OPT;
	public static final int ISHRFUNC = ISHR + OPT;
	static {setArgType(IADDFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(ISUBFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(IMULFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(IDIVFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(INEGFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(IMODFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(INOTFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(IANDFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(IORFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(IXORFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(ISALFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(ISARFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(ISHRFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}

	// �����Ѥ��Ȥ߹��ߥ�����̿�� (420--429+OPT)

	/** ilt [intatom1, intatom2]
	 * <br>�����Ѥ��Ȥ߹��ߥ�����̿��<br>
	 * �������ȥ���ͤ��羮��Ӥ�����Ω�Ĥ��Ȥ��ǧ���롣*/
	public static final int ILT = 420;
	public static final int ILE = 421;
	public static final int IGT = 422;
	public static final int IGE = 423;	
	public static final int IEQ = 424;	
	public static final int INE = 425;	
	static {setArgType(ILT, new ArgType(false, ARG_ATOM, ARG_ATOM));}
	static {setArgType(ILE, new ArgType(false, ARG_ATOM, ARG_ATOM));}
	static {setArgType(IGT, new ArgType(false, ARG_ATOM, ARG_ATOM));}
	static {setArgType(IGE, new ArgType(false, ARG_ATOM, ARG_ATOM));}
	static {setArgType(IEQ, new ArgType(false, ARG_ATOM, ARG_ATOM));}
	static {setArgType(INE, new ArgType(false, ARG_ATOM, ARG_ATOM));}

	/** iltfunc [intfunc1, intfunc2]
	 * <br>�����Ѥκ�Ŭ�����Ȥ߹��ߥ�����̿��<br>
	 * �����ե��󥯥����ͤ��羮��Ӥ�����Ω�Ĥ��Ȥ��ǧ���롣*/
	public static final int ILTFUNC = ILT + OPT;
	public static final int ILEFUNC = ILE + OPT;
	public static final int IGTFUNC = IGT + OPT;
	public static final int IGEFUNC = IGE + OPT;
	static {setArgType(ILTFUNC, new ArgType(false, ARG_VAR, ARG_VAR));}
	static {setArgType(ILEFUNC, new ArgType(false, ARG_VAR, ARG_VAR));}
	static {setArgType(IGTFUNC, new ArgType(false, ARG_VAR, ARG_VAR));}
	static {setArgType(IGEFUNC, new ArgType(false, ARG_VAR, ARG_VAR));}

	// ��ư���������Ѥ��Ȥ߹��ߥܥǥ�̿�� (600--619+OPT)
	public static final int FADD = 600;
	public static final int FSUB = 601;
	public static final int FMUL = 602;
	public static final int FDIV = 603;
	public static final int FNEG = 604;
	static {setArgType(FADD, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(FSUB, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(FMUL, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(FDIV, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	static {setArgType(FNEG, new ArgType(true, ARG_ATOM, ARG_ATOM, ARG_ATOM));}
	
	public static final int FADDFUNC = FADD + OPT;
	public static final int FSUBFUNC = FSUB + OPT;
	public static final int FMULFUNC = FMUL + OPT;
	public static final int FDIVFUNC = FDIV + OPT;
	public static final int FNEGFUNC = FNEG + OPT;
	static {setArgType(FADDFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(FSUBFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(FMULFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(FDIVFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	static {setArgType(FNEGFUNC, new ArgType(true, ARG_VAR, ARG_VAR, ARG_VAR));}
	
	// ��ư���������Ѥ��Ȥ߹��ߥ�����̿�� (620--629+OPT)
	public static final int FLT = 620;
	public static final int FLE = 621;
	public static final int FGT = 622;
	public static final int FGE = 623;	
	public static final int FEQ = 624;	
	public static final int FNE = 625;	
	static {setArgType(FLT, new ArgType(false, ARG_ATOM, ARG_ATOM));}
	static {setArgType(FLE, new ArgType(false, ARG_ATOM, ARG_ATOM));}
	static {setArgType(FGT, new ArgType(false, ARG_ATOM, ARG_ATOM));}
	static {setArgType(FGE, new ArgType(false, ARG_ATOM, ARG_ATOM));}
	static {setArgType(FEQ, new ArgType(false, ARG_ATOM, ARG_ATOM));}
	static {setArgType(FNE, new ArgType(false, ARG_ATOM, ARG_ATOM));}

	public static final int FLTFUNC = FLT + OPT;
	public static final int FLEFUNC = FLE + OPT;
	public static final int FGTFUNC = FGT + OPT;
	public static final int FGEFUNC = FGE + OPT;
	static {setArgType(FLTFUNC, new ArgType(false, ARG_VAR, ARG_VAR));}
	static {setArgType(FLEFUNC, new ArgType(false, ARG_VAR, ARG_VAR));}
	static {setArgType(FGTFUNC, new ArgType(false, ARG_VAR, ARG_VAR));}
	static {setArgType(FGEFUNC, new ArgType(false, ARG_VAR, ARG_VAR));}

	// ������BUILTIN ̿��ˤ��٤��Ǥ��롣
	public static final int FLOAT2INT = 630;
	public static final int INT2FLOAT = 631;
	static {setArgType(FLOAT2INT, new ArgType(true, ARG_ATOM, ARG_ATOM));}
	static {setArgType(INT2FLOAT, new ArgType(true, ARG_ATOM, ARG_ATOM));}
	public static final int FLOAT2INTFUNC = FLOAT2INT + OPT;
	public static final int INT2FLOATFUNC = INT2FLOAT + OPT;
	static {setArgType(FLOAT2INTFUNC, new ArgType(true, ARG_VAR, ARG_VAR));}
	static {setArgType(INT2FLOATFUNC, new ArgType(true, ARG_VAR, ARG_VAR));}
	

	// TODO �ʳ�ĥ�������BUILTIN ̿���Ȥ������褤�Ȼפ���
//	public static final int FSIN = 640;
//	public static final int FCOS = 641;
//	public static final int FTAN = 642;
    
    //���롼�ײ��˴ؤ���̿��
    /** group [subinsts]
     * subinsts ������̿����
     * sakurai
     */
    public static final int GROUP = 2000;
	static {setArgType(GROUP, new ArgType(false, ARG_INSTS));}
    
   
    /** ̿��μ����������롣*/
	public int getKind() {
		return kind;
	}
	/**@deprecated*/
	public int getID() {
		return getKind();
	}
	public int getIntArg(int pos) {
		return ((Integer)data.get(pos)).intValue();
	}
	public int getIntArg1() {
		return ((Integer)data.get(0)).intValue();
	}
	public int getIntArg2() {
		return ((Integer)data.get(1)).intValue();
	}
	public int getIntArg3() {
		return ((Integer)data.get(2)).intValue();
	}
	public int getIntArg4() {
		return ((Integer)data.get(3)).intValue();
	}
	public int getIntArg5() {
		return ((Integer)data.get(4)).intValue();
	}
	public int getIntArg6(){
		return ((Integer)data.get(5)).intValue();
	}
	public Object getArg(int pos) {
		return data.get(pos);
	}
	public Object getArg1() {
		return data.get(0);
	}
	public Object getArg2() {
		return data.get(1);
	}
	public Object getArg3() {
		return data.get(2);
	}
	public Object getArg4() {
		return data.get(3);
	}
	public Object getArg5() {
		return data.get(4);
	}
	public Object getArg6() {
		return data.get(5);
	}
	/**@deprecated*/
	public void setArg(int pos, Object arg) {
		data.set(pos,arg);
	}
	/**@deprecated*/
	public void setArg1(Object arg) {
		data.set(0,arg);
	}
	/**@deprecated*/
	public void setArg2(Object arg) {
		data.set(1,arg);
	}
	/**@deprecated*/
	public void setArg3(Object arg) {
		data.set(2,arg);
	}
	/**@deprecated*/
	public void setArg4(Object arg) {
		data.set(3,arg);
	}
	/**@deprecated*/
	public void setArg5(Object arg) {
		data.set(4,arg);
	}

    ////////////////////////////////////////////////////////////////

    /**
     * �������ɲä���ޥ���
     * @param o ���֥������ȷ��ΰ���
     */
	public final void add(Object o) { data.add(o); }
	
    /**
     * �������ɲä���ޥ���
     * @param n int ���ΰ���
     */
    public final void add(int n) { data.add(new Integer(n)); }
	
	////////////////////////////////////////////////////////////////

    /**
     * ���ߡ�̿����������롣
     * ���������ä������᥽�åɤ��ޤ��Ǥ��Ƥʤ�̿��Ϥ����Ȥ�
     * @param s �����Ѥ�ʸ����
     * @deprecated
     */
    public static Instruction dummy(String s) {
		Instruction i = new Instruction(DUMMY);
		i.add(s);
		return i;
    }
    
    /**
     * react ̿����������롣
     * @param r ȿ���Ǥ���롼�륪�֥�������
     * @param actual �°�����
     * @deprecated
     */
    public static Instruction react(Rule r, List actual) {
		Instruction i = new Instruction(REACT);
		i.add(r);
		i.add(actual);
		return i;
    }
	/**
	 * react ̿����������롣
	 * @param r ȿ���Ǥ���롼�륪�֥�������
	 * @param memactuals ��°����Υꥹ�ȡ�����ѿ��ֹ椫��ʤ롣
	 * @param atomactuals ���ȥ�°����Υꥹ�ȡ����ȥ���ѿ��ֹ椫��ʤ롣
	 * @deprecated
	 */
	public static Instruction react(Rule r, List memactuals, List atomactuals) {
		Instruction i = new Instruction(REACT);
		i.add(r);
		i.add(memactuals);
		i.add(atomactuals);
		i.add(new ArrayList());
		return i;
	}
	/**
	 * react ̿����������롣
	 * @param r ȿ���Ǥ���롼�륪�֥�������
	 * @param memactuals ��°����Υꥹ�ȡ�����ѿ��ֹ椫��ʤ롣
	 * @param atomactuals ���ȥ�°����Υꥹ�ȡ����ȥ���ѿ��ֹ椫��ʤ롣
	 * @param varactuals ����¾�μ°����Υꥹ�ȡ�����¾���ѿ��ֹ椫��ʤ롣
	 */
	public static Instruction react(Rule r, List memactuals, List atomactuals, List varactuals) {
		Instruction i = new Instruction(REACT);
		i.add(r);
		i.add(memactuals);
		i.add(atomactuals);
		i.add(varactuals);
		return i;
	}
	/**
	 * jump ̿����������롣
	 * @param insts ��������Υ�٥��դ�̿����
	 * @param memactuals ��°����Υꥹ�ȡ�����ѿ��ֹ椫��ʤ롣
	 * @param atomactuals ���ȥ�°����Υꥹ�ȡ����ȥ���ѿ��ֹ椫��ʤ롣
	 * @param varactuals ����¾�μ°����Υꥹ�ȡ�����¾���ѿ��ֹ椫��ʤ롣
	 */
	public static Instruction jump(InstructionList insts, List memactuals, List atomactuals, List varactuals) {
		Instruction i = new Instruction(JUMP);
		i.add(insts);
		i.add(memactuals);
		i.add(atomactuals);
		i.add(varactuals);
		return i;
	}
	/**
	 * commit ̿����������롣
	 * @param r ȿ������롼�륪�֥�������
	 */
	public static Instruction commit(Rule r) {
		Instruction i = new Instruction(COMMIT);
		i.add(r);
		return i;
	}
    /** resetvars ̿����������� */
    public static Instruction resetvars(List memargs, List atomargs, List varargs) {
    	Instruction i = new Instruction(RESETVARS);
    	i.add(memargs);
    	i.add(atomargs);
    	i.add(varargs);
    	return i;
    }
    /** @deprecated */
    public static Instruction findatom(int dstatom, List srcmem, Functor func) {
		Instruction i = new Instruction(FINDATOM);
		i.add(dstatom);
		i.add(srcmem);
		i.add(func);
		return i;
    }
    
    //
    
	/** findatom ̿����������� */
	public static Instruction findatom(int dstatom, int srcmem, Functor func) {
		return new Instruction(FINDATOM,dstatom,srcmem,func);
	}	
    /** anymem ̿����������� */
    public static Instruction anymem(int dstmem, int srcmem) {
		return new Instruction(ANYMEM,dstmem,srcmem);
    }
    /** newatom ̿����������� */
    public static Instruction newatom(int dstatom, int srcmem, Functor func) {
		return new Instruction(NEWATOM,dstatom,srcmem,func);
    }	
    /** spec ̿����������� */
    public static Instruction spec(int formals, int locals) {
		Instruction i = new Instruction(SPEC);
		i.add(formals);
		i.add(locals);
		return i;
    }
	/** newmem ̿����������� */
	public static Instruction newmem(int ret, int srcmem) {
		return new Instruction(NEWMEM,ret,srcmem);
	}
//	/** newlink ̿�����������
//	 * @deprecated */
//	public static Instruction newlink(int atom1, int pos1, int atom2, int pos2) {
//		return new Instruction(NEWLINK,atom1,pos1,atom2,pos2);
//	}
	/** newlink ̿����������� */
	public static Instruction newlink(int atom1, int pos1, int atom2, int pos2, int mem1) {
		return new Instruction(NEWLINK,atom1,pos1,atom2,pos2,mem1);
	}
    /** loadruleset ̿����������� */
    public static Instruction loadruleset(int mem, Ruleset rs) {
		return new Instruction(LOADRULESET,mem,rs);
    }
    /** getmem ̿����������� */
    public static Instruction getmem(int ret, int atom) {
		return new Instruction(GETMEM,ret,atom);
    }	
    /** removeatom ̿����������� @deprecated*/
	public static Instruction removeatom(int atom) {
		return new Instruction(REMOVEATOM,atom);
	}
	/** removeatom ̿�����������*/
	public static Instruction removeatom(int atom, int mem, Functor func) {
		return new Instruction(REMOVEATOM,atom,mem,func);
	}
	/** dequeueatom ̿�����������*/
	public static Instruction dequeueatom(int atom) {
		return new Instruction(DEQUEUEATOM,atom);
	}
    
    /** fail����̿����������� */
	public static Instruction fail() {
		InstructionList label = new InstructionList();
		label.add(new Instruction(PROCEED));
		return new Instruction(Instruction.NOT, label);
	}
		
		
	// ���󥹥ȥ饯��
	
    /** ̵̾̿����롣*/
    public Instruction() {
    }
	
    /**
     * ���ꤵ�줿̿���Ĥ��롣������private�ˤ���Ȥ����Τ����Τ�ʤ���
     * @param kind
     */
    public Instruction(int kind) {
    	this.kind = kind;
    }
	public Instruction(int kind, int arg1) {
		this.kind = kind;
		add(arg1);
	}
	public Instruction(int kind, Object arg1) {
		this.kind = kind;
		add(arg1);
	}
	public Instruction(int kind, int arg1, int arg2) {
		this.kind = kind;
		add(arg1);
		add(arg2);
	}
	public Instruction(int kind, int arg1, Object arg2) {
		this.kind = kind;
		add(arg1);
		add(arg2);
	}
	public Instruction(int kind, Object arg1, int arg2) {
		this.kind = kind;
		add(arg1);
		add(arg2);
	}
	public Instruction(int kind, int arg1, int arg2, int arg3) {
		this.kind = kind;
		add(arg1);
		add(arg2);
		add(arg3);
	}
	public Instruction(int kind, int arg1, Object arg2, int arg3) {
		this.kind = kind;
		add(arg1);
		add(arg2);
		add(arg3);
	}
	public Instruction(int kind, int arg1, int arg2, Object arg3) {
		this.kind = kind;
		add(arg1);
		add(arg2);
		add(arg3);
	}
	public Instruction(int kind, int arg1, int arg2, int arg3, int arg4) {
		this.kind = kind;
		add(arg1);
		add(arg2);
		add(arg3);
		add(arg4);
	}
	public Instruction(int kind, int arg1, int arg2, int arg3, int arg4, int arg5) {
		this.kind = kind;
		add(arg1);
		add(arg2);
		add(arg3);
		add(arg4);
		add(arg5);
	}
	
	/** �ѡ������ѥ��󥹥ȥ饯�� */
	public Instruction(String name, List data) {
		try {
	    	Field f = Instruction.class.getField(name.toUpperCase());
	    	this.kind = f.getInt(null);
	    	this.data = data;
	    	return;
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}
		//�㳰ȯ����
		throw new RuntimeException("invalid instruction name : " + name);
    }

    public Object clone() {
		Instruction c = new Instruction();
		c.kind = this.kind;
		Iterator it = this.data.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof ArrayList) {
				c.data.add(((ArrayList)o).clone());
			} else if (kind != Instruction.JUMP && o instanceof InstructionList) {
				c.data.add(((InstructionList)o).clone());
			} else {
				c.data.add(o);
			}
		}
		return c;
	}
	
	//////////////////////////////////
	// ��Ŭ���郎�Ȥ���̿����񤭴����Τ���Υ��饹�᥽�å�
	// @author Mizuno
	
	// todo argtype �� signature ��̾���ѹ�����Ȥ褤
	
	private static void setArgType(int kind, ArgType argtype) {	
		if (argTypeTable.containsKey(new Integer(kind))) {
			throw new RuntimeException("setArgType for '" + kind + "' was called more than once");
		}
		argTypeTable.put(new Integer(kind), argtype);
	}
	private static class ArgType {
		
		boolean output;
		int[] type;
		ArgType(boolean output) {
			this.output = output;
			type = new int[0];
		}
		ArgType(boolean output, int arg1) {
			this.output = output;
			type = new int[] {arg1};
		}
		ArgType(boolean output, int arg1, int arg2) {
			this.output = output;
			type = new int[] {arg1, arg2};
		}
		ArgType(boolean output, int arg1, int arg2, int arg3) {
			this.output = output;
			type = new int[] {arg1, arg2, arg3};
		}
		ArgType(boolean output, int arg1, int arg2, int arg3, int arg4) {
			this.output = output;
			type = new int[] {arg1, arg2, arg3, arg4};
		}
		ArgType(boolean output, int arg1, int arg2, int arg3, int arg4, int arg5) {
			this.output = output;
			type = new int[] {arg1, arg2, arg3, arg4, arg5};
		}
		ArgType(boolean output, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6){
			this.output = output;
			type = new int[] {arg1, arg2, arg3, arg4, arg5, arg6};
		}
	}

	public ArrayList getVarArgs() {
		ArrayList ret = new ArrayList();
		ArgType argtype = (ArgType)argTypeTable.get(new Integer(getKind()));
		int i = 0;
		if (getOutputType() != -1) {
			i = 1;
		}
		for (; i < data.size(); i++) {
			switch (argtype.type[i]) {
				case ARG_ATOM:
				case ARG_MEM:
				case ARG_VAR:
					ret.add(getArg(i));
			}
		}
		return ret;
	}
	/**
	 * Ϳ����줿�б�ɽ�ˤ�äơ��ܥǥ�̿������Υ��ȥ��ѿ���񤭴����롣<br>
	 * ̿��������ѿ������б�ɽ�Υ����˽и������硢�б������ͤ˽񤭴����ޤ���
	 * 
	 * @param list �񤭴�����̿����
	 * @param map �ѿ����б�ɽ��
	 */
	public static void changeAtomVar(List list, Map map) {
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			ArgType argtype = (ArgType)argTypeTable.get(new Integer(inst.getKind()));
			for (int i = 0; i < inst.data.size(); i++) {
				switch (argtype.type[i]) {
					case ARG_ATOM:
						changeArg(inst, i+1, map);
						break;
				}
			}
		}
	}
	/**
	 * Ϳ����줿�б�ɽ�ˤ�äơ��ܥǥ�̿����������ѿ���񤭴����롣<br>
	 * ̿��������ѿ������б�ɽ�Υ����˽и������硢�б������ͤ˽񤭴����ޤ���
	 * 
	 * @param list �񤭴�����̿����
	 * @param map �ѿ����б�ɽ��
	 */
	public static void changeMemVar(List list, Map map) {
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			ArgType argtype = (ArgType)argTypeTable.get(new Integer(inst.getKind()));
			for (int i = 0; i < inst.data.size(); i++) {
				switch (argtype.type[i]) {
					case ARG_MEM:
						changeArg(inst, i+1, map);
						break;
				}
			}
		}
	}
	/**
	 * Ϳ����줿�б�ɽ�ˤ�äơ��ܥǥ�̿������Υ��ȥࡦ��ʳ����ѿ���񤭴����롣<br>
	 * ̿��������ѿ������б�ɽ�Υ����˽и������硢�б������ͤ˽񤭴����ޤ���
	 * 
	 * @param list �񤭴�����̿����
	 * @param map �ѿ����б�ɽ��
	 */
	public static void changeOtherVar(List list, Map map) {
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			ArgType argtype = (ArgType)argTypeTable.get(new Integer(inst.getKind()));
			for (int i = 0; i < inst.data.size(); i++) {
				switch (argtype.type[i]) {
					case ARG_VAR:
						changeArg(inst, i+1, map);
						break;
				}
			}
		}
	}
	/**
	 * Ϳ����줿�б�ɽ�ˤ�äơ��ܥǥ�̿������Τ��٤Ƥ��ѿ���񤭴����롣<br>
	 * ̿��������ѿ������б�ɽ�Υ����˽и������硢�б������ͤ˽񤭴����ޤ���
	 * 
	 * @param list �񤭴�����̿����
	 * @param map �ѿ����б�ɽ��
	 */
	public static void applyVarRewriteMap(List list, Map map) {
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			ArgType argtype = (ArgType)argTypeTable.get(new Integer(inst.getKind()));
			for (int i = 0; i < inst.data.size(); i++) {
				switch (argtype.type[i]) {
					case ARG_MEM:
					case ARG_ATOM:
					case ARG_VAR:
						changeArg(inst, i+1, map);
						break;
					case ARG_LABEL:
						if (inst.getKind() == JUMP) break; // JUMP̿���LABEL�����Ϥ����Υ�٥�ʤΤǽ���
						applyVarRewriteMap( ((InstructionList)inst.data.get(i)).insts, map);
						break;
					case ARG_INSTS:
						applyVarRewriteMap( (List)inst.data.get(i), map);
						break;
					case ARG_VARS:
						ListIterator li = ((List)inst.data.get(i)).listIterator();
						while (li.hasNext()) {
							Object varnum = li.next();
							if (map.containsKey(varnum)) li.set(map.get(varnum));
						}
						break;
				}
			}
			if (inst.getKind() == RESETVARS || inst.getKind() == CHANGEVARS) break;
		}
	}
	/** ̿�����Ⱦ��ʬ���Ф����ѿ��ֹ���դ��ؤ��� */
	public static void applyVarRewriteMapFrom(List list, Map map, int start) {
		applyVarRewriteMap( list.subList(start, list.size()), map );
	}

	////////////////////////////////////////////////////////////////
	
	/**
	 * �б�ɽ�ˤ�äư�����񤭴����롣
	 * @param inst �񤭴�����̿��
	 * @param pos �񤭴���������ֹ�
	 * @param map �񤭴����ޥå�
	 */
	private static void changeArg(Instruction inst, int pos, Map map) {
		Integer id = (Integer)inst.data.get(pos - 1);
		if (map.containsKey(id)) {
			inst.data.set(pos - 1, map.get(id));
		}
	}

	/** ���ꤵ�줿̿��������ѿ������Ȥ���������֤��������ϴޤޤʤ���
	 * @param list   ̿����
	 * @param varnum �ѿ��ֹ�
	 * @author n-kato */
	public static int getVarUseCount(List list, Integer varnum) {
		int count = 0;
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			ArgType argtype = (ArgType)argTypeTable.get(new Integer(inst.getKind()));
			int i = 0;
			if (argtype.output) i++;
			for (; i < inst.data.size(); i++) {
				switch (argtype.type[i]) {
					case ARG_MEM:
					case ARG_ATOM:
					case ARG_VAR:
						if (inst.data.get(i).equals(varnum)) count++;
						break;
					case ARG_LABEL:
						if (inst.getKind() == JUMP) break; // JUMP̿���LABEL�����Ϥ����Υ�٥�ʤΤǽ���
						count += getVarUseCount( ((InstructionList)inst.data.get(i)).insts, varnum);
						break;
					case ARG_INSTS:
						count += getVarUseCount( (List)inst.data.get(i), varnum);
						break;
					case ARG_VARS:
						Iterator it2 = ((List)inst.data.get(i)).iterator();
						while (it2.hasNext()) {
							if (it2.next().equals(varnum)) count++;
						}
						break;
				}
			}
		}
		return count;
	}
	/** ���ꤵ�줿̿�����Ⱦ��ʬ���ѿ������Ȥ���������֤��������ϴޤޤʤ���
	 * @param list   ̿����
	 * @param varnum �ѿ��ֹ�
	 * @param start  ���ϰ���
	 * @see getVarUseCount */
	public static int getVarUseCountFrom(List list, Integer varnum, int start) {
		return getVarUseCount( list.subList(start, list.size()), varnum );
	}
	
	////////////////////////////////////////////////////////////////
	/**
	 * ����̿�᤬����̿��ξ�硢���Ϥμ�����֤���
	 * �����Ǥʤ���硢-1���֤���
	 */
	public int getOutputType() {
		ArgType argtype = (ArgType)argTypeTable.get(new Integer(kind));
		if (argtype.output) {
			return argtype.type[0];
		} else {
			return -1;
		}
	}
	/** ����̿�᤬�����Ѥ���Ĳ�ǽ�������뤫�ɤ������֤��������ʾ��true���֤��ʤ���Фʤ�ʤ���
	 * ��������Υ�å������������ѤȤϸ��ʤ��ʤ���
	 * <p>�ɤ���顢����֥�����̿��פȸƤ�Ǥ�����Τ���������餷����*/
	public boolean hasSideEffect() {
		// todo ��������Τ��ä����������ˤ�����������������ͽ��
		switch (getKind()) {
			case Instruction.DEREF:
			case Instruction.DEREFATOM:
			case Instruction.DEREFLINK:
			case Instruction.DEREFFUNC:
			case Instruction.GETFUNC:
			case Instruction.FUNC:
			case Instruction.EQATOM:
			case Instruction.SAMEFUNC:
			case Instruction.GETMEM:
			case Instruction.GETPARENT:
			case Instruction.LOADFUNC:
			case Instruction.GETLINK:
			case Instruction.ALLOCLINK:
			case Instruction.ALLOCMEM:
			case Instruction.LOOKUPLINK:
			case Instruction.LOCK:
			case Instruction.GETRUNTIME:
			case Instruction.ISINT: case Instruction.ISUNARY:
			case Instruction.IADD: case Instruction.IADDFUNC:
			case Instruction.IEQ: case Instruction.ILT: case Instruction.ILE:
			case Instruction.IGT: case Instruction.IGE: case Instruction.INE:
			case Instruction.FEQ: case Instruction.FLT: case Instruction.FLE:
			case Instruction.FGT: case Instruction.FGE: case Instruction.FNE:
				return false;
		}
		return true;
	}
	/** ����̿�᤬����ư��򤹤��ǽ�������뤫�ɤ������֤���
	 * ����ư��Ȥϼ��Ԥ�ȿ���ʤɤ�ɽ���������ʾ��true���֤��ʤ���Фʤ�ʤ���*/
	public boolean hasControlEffect() {
		// todo ��������Τ��ä����������ˤ�����������������ͽ��
		switch (getKind()) {
			case Instruction.DEREFATOM:
			case Instruction.DEREFFUNC:
			case Instruction.GETFUNC:
			case Instruction.GETMEM:
			case Instruction.LOADFUNC:
			case Instruction.GETLINK:
			case Instruction.ALLOCLINK:
			case Instruction.ALLOCMEM:
			case Instruction.LOOKUPLINK:
			case Instruction.IADD: case Instruction.IADDFUNC:
				return false;
			case Instruction.DEREF:
			case Instruction.DEREFLINK:
			case Instruction.IDIV: case Instruction.IDIVFUNC:
			case Instruction.LOCK:
			case Instruction.GETPARENT:
				return true;
		}
		return true;
	}
	
	//////////////////////////////////
	//
	// �ǥХå���ɽ���᥽�å�
	//


	/** Integer�ǥ�åפ��줿̿���ֹ椫��̿��̾�ؤΥϥå��塣
	 * <p>�����ϳ�ȯ����«�������ˡ���äȸ�Ψ�Τ褤�̤ι�¤���֤������Ƥ�褤�� */
	static Hashtable instructionTable = new Hashtable();
	
	//���󥹥����������˥����å������С��ե��򵯤������Τǽ������ޤ����� by Mizuno
	//ExceptionInInitializerError �������Ƥ��Τǽ��� by hara
    static {
		try {
			Instruction inst = new Instruction();
			Field[] fields = inst.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				// �ɲá�hara
				if(! f.getType().isPrimitive()) continue;
				if (f.getName().startsWith("ARG_")) continue; //added by mizuno
				int kind = f.getInt(inst);
				if (kind != LOCAL
				 && f.getType().getName().equals("int") && Modifier.isStatic(f.getModifiers())) {
					Integer idobj = new Integer(kind);
					if (instructionTable.containsKey(idobj)) {
						System.out.println("WARNING: collision detected on instruction kind = " 
							+ idobj.intValue());
					}
					instructionTable.put(idobj, f.getName().toLowerCase());
				}
			}
		}
		catch(java.lang.SecurityException e)		{ e.printStackTrace(); }
		catch(java.lang.IllegalAccessException e)	{ e.printStackTrace(); }
    }


	/**
	 * �ǥХå���ɽ���᥽�åɡ�
	 * ̿���kind (int)��Ϳ����ȡ���������̿���̾�� (String) ���֤��Ƥ���롣
	 *
	 * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
	 * @return String
	 * 
	 */
	public static String getInstructionString(int kind){
		String answer = "";
		answer = (String)instructionTable.get(new Integer(kind));
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
    public String toString() {
		//nakajima��2004-01-21
		StringBuffer buffer = new StringBuffer("               ");
		String tmp = getInstructionString(kind);
		ArgType argtype = (ArgType)argTypeTable.get(new Integer(kind));
		int indent = 14;
		if (argtype.output) {
			indent -= data.get(0).toString().length() + 2;
			buffer.delete(indent,14);
		}
		if( tmp.length() > indent ) {
			buffer.replace(0, indent, tmp.substring(0, indent - 2) + "..");
		} else {
			buffer.replace(0, tmp.length(), tmp);
		}
		if (data.size() == 1 && data.get(0) instanceof ArrayList) {
			ArrayList arg1 = (ArrayList)data.get(0);
			if (arg1.size() == 1 && arg1.get(0) instanceof ArrayList) {
				ArrayList insts = (ArrayList)arg1.get(0);
				if(insts.size() == 0) {
					buffer.append("[[]]");
				} else {
					buffer.append("[[\n");
					int i;
					for(i = 0; i < insts.size()-1; i++){
						buffer.append("                  ");
						buffer.append(insts.get(i));
						buffer.append(", \n");
					}
					buffer.append("                  ");
					buffer.append(insts.get(i));
					buffer.append(" ]]");
					return buffer.toString();
				}
			}
		}
		
		if (kind != Instruction.JUMP && data.size() >= 1 && data.get(0) instanceof InstructionList) {
			List insts = ((InstructionList)data.get(0)).insts;
			if(insts.size() == 0) {
				buffer.append("[]");
			} else {
				if(Env.compileonly) buffer.append("[[\n");
				else buffer.append("[\n");
				int i;
				for(i = 0; i < insts.size()-1; i++){
					//���ȥ��Ƴ�ƥ��Ȥ�̿����򸫤䤹��(?)���� sakurai
//					if(((Instruction)insts.get(i)).getKind() == Instruction.GROUP
//						|| ((Instruction)insts.get(i)).getKind() == Instruction.COMMIT){
//						buffer.append("\n");
//					}
					buffer.append("                  ");
					buffer.append(insts.get(i));
					//TODO ���ϰ������ä��饤��ǥ�Ȥ򲼤���.
					buffer.append(", \n");
				}
				buffer.append("                  ");
				buffer.append(insts.get(i));
				for(int j = 1; j < data.size(); j++){
					buffer.append("                  ");
					buffer.append("     ");
					buffer.append(", " + data.get(j));
				}
				buffer.append(" ]]");
				return buffer.toString();
			}
		}

		buffer.append(data.toString());

		return buffer.toString();
    }

    
    /** spec̿��ΰ����ͤ򿷤����ͤ˹�������ʻ���Ū���֡�*/
    public void updateSpec(int formals, int locals) {
    	if (getKind() == SPEC) {
			data.set(0,new Integer(formals));
			data.set(1,new Integer(locals));
    	}
    }
    
	private void writeObject(java.io.ObjectOutputStream out) throws IOException{
		out.writeInt(kind);
		out.writeInt(data.size());
		Iterator it = data.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof Ruleset) {
				out.writeObject("Ruleset");
				((Ruleset)o).serialize(out);
			} else {
				out.writeObject("Other");
				out.writeObject(o);
			}
		}
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		kind = in.readInt();
		data = new ArrayList();
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			String argtype = (String)in.readObject();
			if (argtype.equals("Ruleset")) {
				data.add(Ruleset.deserialize(in));
			} else {
				data.add(in.readObject());
			}
		}
	}
}
