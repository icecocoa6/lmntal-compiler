/*
 * ������: 2003/10/21
 *
 */
package runtime;

import java.util.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;

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
 * <li>�����������줬��⡼�����¾�Υ���������ξ�硢���Ū�ʼ¹ԥ����å����ꡢ
 * �����˿���¦�����Ѥ�Ǥ�����
 * ��⡼�ȤΥ롼����Υ�å������������ȡ��¹��쥹���å�����Ƭ�˴ݤ��Ȱ�ư����롣
 * ����ˤ�äƼ¹��쥹���å������Ƥ��줬���ȥߥå����Ѥޤ�뤳�Ȥˤʤ롣
 * </ul>
 * TODO ����ѥ���ϼ��Υ����ɤ���Ϥ��롧addmem��newroot������ϡ��롼��¹Խ�λ���ˡʻ���¦������֤ˡ�unlockmem��¹Ԥ��롣
 */
 
/**
 * 1 �Ĥ�̿����ݻ����롣�̾�ϡ�Instruction��ArrayList�Ȥ����ݻ����롣
 * 
 * �ǥХå���ɽ���᥽�åɤ������롣
 *
 * @author hara, nakajima, n-kato
 */
public class Instruction implements Cloneable {
	
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
    /** ���դ����ȥ���Ф���̿�᤬��󥯤ǤϤʤ����ե��󥯥����оݤˤ��Ƥ��뤳�Ȥ�ɽ�������� */
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
	//  -----  findatom  [-dstatom, srcmem, funcref]
	//	-----  getlink   [-link,    atom, pos]

    /** deref [-dstatom, srcatom, srcpos, dstpos]
     * <br><strong><font color="#ff0000">���Ϥ��륬����̿��</font></strong><br>
     * ���ȥ�$srcatom����srcpos�����Υ���褬��dstpos��������³���Ƥ��뤳�Ȥ��ǧ�����顢
     * �����Υ��ȥ�ؤλ��Ȥ�$dstatom���������롣*/
	public static final int DEREF = 1;
	// LOCALDEREF������

	/** derefatom [-dstatom, srcatom, srcpos]
	 * <br>���Ϥ��뼺�Ԥ��ʤ���Ŭ���ѡܷ��դ���ĥ�ѥ�����̿��<br>
	 * ���ȥ�$srcatom����srcpos�����Υ����Υ��ȥ�ؤλ��Ȥ�$dstatom���������롣
	 * <p>����³��$dstatom����ñ�ॢ�ȥ�������ʤɤ�ޤ�ˤ伫ͳ��󥯴������ȥ��
	 * �ޥå����뤫�ɤ�������������˻��Ѥ��뤳�Ȥ��Ǥ��롣*/
	public static final int DEREFATOM = 2;
	// LOCALDEREFATOM������

	/** findatom [-dstatom, srcmem, funcref]
	 * <br>ȿ�����륬����̿��<br>
	 * ��$srcmem�ˤ��äƥե��󥯥�funcref����ĥ��ȥ�ؤλ��Ȥ򼡡���$dstatom���������롣*/
	public static final int FINDATOM = 3;
	// LOCALFINDATOM������

	/** getlink [-link, atom, pos]
	 * <br>���Ϥ��뼺�Ԥ��ʤ���ĥ������̿�ᡢ��Ŭ���ѥܥǥ�̿��<br>
	 * ���ȥ�$atom����pos�����˳�Ǽ���줿��󥯥��֥������Ȥؤλ��Ȥ�$link���������롣
	 * <p>ŵ��Ū�ˤϡ�$atom�ϥ롼��إåɤ�¸�ߤ��롣*/
	public static final int GETLINK = 4;
	// LOCALGETLINK������

//	/** dereflink [atom, link]
//	 * <br>���Ϥ��뼺�Ԥ��ʤ���ĥ������̿�ᡢ��Ŭ���ѥܥǥ�̿��<br>
//	 * ���$link���ؤ����ȥ�ؤλ��Ȥ�$atom���������롣*/
//	public static final int DEREFLINK = err;
//	// LOCALDEREFLINK������

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
    
    /** locallockmem [-dstmem, freelinkatom]
     * <br>��å����������Ŭ���ѥ�����̿��<br>
     * lockmem��Ʊ����������$freelinkatom�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALLOCKMEM = LOCAL + LOCKMEM;

    /** anymem [-dstmem, srcmem] 
     * <br>ȿ�������å��������륬����̿��<br>
     * ��$srcmem�λ���Τ����ޤ���å���������Ƥ��ʤ�����Ф��Ƽ����ˡ�
     * �Υ�֥�å��󥰤ǤΥ�å��������ߤ롣
     * �����ơ���å����������������ƻ���ؤλ��Ȥ�$dstmem���������롣
     * ����������å��ϡ���³��̿���󤬤�������Ф��Ƽ��Ԥ����Ȥ��˲�������롣
     * <p><b>���</b>����å������˼��Ԥ������ȡ������줬¸�ߤ��Ƥ��ʤ��ä����Ȥ϶��̤Ǥ��ʤ���*/
	public static final int ANYMEM = 6;
	
	/** localanymem [-dstmem, srcmem]
     * <br>ȿ�������å����������Ŭ���ѥ�����̿��<br>
	 * anymem��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣$dstmem�ˤĤ��Ƥϲ��Ⲿ�ꤵ��ʤ���*/
	public static final int LOCALANYMEM = LOCAL + ANYMEM;

	/** lock [srcmem]
	 * <br>��å��������륬����̿��<br>
	 * ��$srcmem���Ф��ơ��Υ�֥�å��󥰤ǤΥ�å���������ߤ롣
	 * ����������å��ϡ���³��̿���󤬼��Ԥ����Ȥ��˲�������롣
	 * <p>���ȥ��Ƴ�ƥ��Ȥǡ���Ƴ���륢�ȥ�ˤ�ä����ꤵ�줿��Υ�å���������뤿��˻��Ѥ���롣
	 * @see lockmem
	 * @see getmem */
	public static final int LOCK = 7;
	
	/** locallock [srcmem]
	 * <br>��å����������Ŭ���ѥ�����̿��<br>
	 * lock��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALLOCK = LOCAL + LOCK;

	/** getmem [-dstmem, srcatom]
	 * <br>������̿��<br>
	 * ���ȥ�$srcatom�ν�°��ؤλ��Ȥ��å�������$dstmem���������롣
	 * <p>���ȥ��Ƴ�ƥ��Ȥǻ��Ѥ���롣
	 * @see lock */
	public static final int GETMEM = 8;
	// LOCALGETMEM������
	
	/** getparent [-dstmem, srcmem]
	 * <br>������̿��<br>
	 * �ʥ�å����Ƥ��ʤ�����$srcmem�ο���ؤλ��Ȥ��å�������$dstmem���������롣
	 * <p>���ȥ��Ƴ�ƥ��Ȥǻ��Ѥ���롣*/
	public static final int GETPARENT = 9;
	// LOCALGETPARENT������

    // ��˴ط�������Ϥ��ʤ����ܥ�����̿�� (10--19)
	//  ----- testmem    [dstmem, srcatom]
	//  ----- norules    [srcmem] 
	//  ----- natoms     [srcmem, count]
	//  ----- nfreelinks [srcmem, count]
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

    /** norules [srcmem] 
     * <br>������̿��<br>
     * ��$srcmem�˥롼�뤬¸�ߤ��ʤ����Ȥ��ǧ���롣*/
    public static final int NORULES = 11;
    // LOCALNORULES������

    /** natoms [srcmem, count]
     * <br>������̿��<br>
     * ��$srcmem�μ�ͳ��󥯴������ȥ�ʳ��Υ��ȥ����count�Ǥ��뤳�Ȥ��ǧ���롣*/
    public static final int NATOMS = 12;
	// LOCALNATOMS������

    /** nfreelinks [srcmem, count]
     * <br>������̿��<br>
     * ��$srcmem�μ�ͳ��󥯿���count�Ǥ��뤳�Ȥ��ǧ���롣*/
    public static final int NFREELINKS = 13;
	// LOCALNFREELINKS������

    /** nmems [srcmem, count]
     * <br>������̿��<br>
     * ��$srcmem�λ���ο���count�Ǥ��뤳�Ȥ��ǧ���롣*/
    public static final int NMEMS = 14;
	// LOCALNMEMS������

    /** eqmem [mem1, mem2]
     * <br>������̿��<br>
     * $mem1��$mem2��Ʊ�����򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     * <p><b>���</b> Ruby�Ǥ�eq����ʬΥ */
    public static final int EQMEM = 15;
	// LOCALEQMEM������
	
    /** neqmem [mem1, mem2]
     * <br>������̿��<br>
     * $mem1��$mem2���ۤʤ���򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
     * <p><b>���</b> Ruby�Ǥ�neq����ʬΥ
     * <p><font color=red><b>����̿������פ����Τ�ʤ�</b></font> */
    public static final int NEQMEM = 16;
	// LOCALNEQMEM������

	/** stable [srcmem]
	 * <br>������̿��<br>
	 * ��$srcmem�Ȥ��λ�¹�����Ƥ���μ¹Ԥ���ߤ��Ƥ��뤳�Ȥ��ǧ���롣*/
	public static final int STABLE = 17;
	// LOCALSTABLE������
    
	// ���ȥ�˴ط�������Ϥ��ʤ����ܥ�����̿�� (20-24)
	//  -----  func    [srcatom, funcref]
	//  -----  eqatom  [atom1, atom2]
	//  -----  neqatom [atom1, atom2]

	/** func [srcatom, funcref]
	 * <br>������̿��<br>
	 * ���ȥ�$srcatom���ե��󥯥�funcref����Ĥ��Ȥ��ǧ���롣
	 * <p>getfunc[tmp,srcatom];loadfunc[func,funcref];eqfunc[tmp,func] ��Ʊ����*/
	public static final int FUNC = 20;
	// LOCALFUNC������

	/** eqatom [atom1, atom2]
	 * <br>������̿��<br>
	 * $atom1��$atom2��Ʊ��Υ��ȥ�򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
	 * <p><b>���</b> Ruby�Ǥ�eq����ʬΥ */
	public static final int EQATOM = 21;
	// LOCALEQATOM������

	/** neqatom [atom1, atom2]
	 * <br>������̿��<br>
	 * $atom1��$atom2���ۤʤ륢�ȥ�򻲾Ȥ��Ƥ��뤳�Ȥ��ǧ���롣
	 * <p><b>���</b> Ruby�Ǥ�neq����ʬΥ */
	public static final int NEQATOM = 22;
	// LOCALNEQATOM������

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

	/** getfunc [-func, atom]
	 * <br>���Ϥ��뼺�Ԥ��ʤ���ĥ������̿��<br>
	 * ���ȥ�$atom�Υե��󥯥��ؤλ��Ȥ�$func���������롣*/
	public static final int GETFUNC = 26;
	// LOCALGETFUNC������

	/** loadfunc [-func, funcref]
	 * <br>���Ϥ��뼺�Ԥ��ʤ���ĥ������̿��<br>
	 * �ե��󥯥�funcref�ؤλ��Ȥ�$func���������롣*/
	public static final int LOADFUNC = 27;
	// LOCALLOADFUNC������

	/** eqfunc [func1, func2]
	 * <br>���դ���ĥ�ѥ�����̿��<br>
	 * �ե��󥯥�$func1��$func2�����������Ȥ��ǧ���롣*/
	public static final int EQFUNC = 28;
	// LOCALEQFUNC������

	/** neqfunc [func1, func2]
	 * <br>���դ���ĥ�ѥ�����̿��<br>
	 * �ե��󥯥�$func1��$func2���ۤʤ뤳�Ȥ��ǧ���롣*/
	public static final int NEQFUNC = 29;
	// LOCALEQFUNC������

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
     * �¹ԥ����å������ʤ���
     * @see dequeueatom */
	public static final int REMOVEATOM = 30;
	
	/** localremoveatom [srcatom, srcmem, funcref]
     * <br>��Ŭ���ѥܥǥ�̿��<br>
     * removeatom��Ʊ����������$srcatom�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALREMOVEATOM = LOCAL + REMOVEATOM;

    /** newatom [-dstatom, srcmem, funcref]
     * <br>�ܥǥ�̿��<br>
     * ��$srcmem�˥ե��󥯥�funcref����Ŀ��������ȥ�����������Ȥ�$dstatom���������롣
     * ���ȥ�Ϥޤ��¹ԥ����å��ˤ��Ѥޤ�ʤ���
     * @see enqueueatom */
    public static final int NEWATOM = 31;

	/** newatomindirect [-dstatom, srcmem, func]
	 * <br>���դ���ĥ�ѥܥǥ�̿��<br>
	 * ��$srcmem�˥ե��󥯥�$func����Ŀ��������ȥ�����������Ȥ�$dstatom���������롣
	 * ���ȥ�Ϥޤ��¹ԥ����å��ˤ��Ѥޤ�ʤ���
	 * @see newatom */
	public static final int NEWATOMINDIRECT = 32;    
    
    /** localnewatom [-dstatom, srcmem, funcref]
     * <br>��Ŭ���ѥܥǥ�̿��<br>
     * newatom��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALNEWATOM = LOCAL + NEWATOM;
	
	/** localnewatomindirect [-dstatom, srcmem, func]
	 * <br>���դ���ĥ�Ѻ�Ŭ���ѥܥǥ�̿��<br>
	 * newatomindirect��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALNEWATOMINDIRECT = LOCAL + NEWATOMINDIRECT;

	/** enqueueatom [srcatom]
     * <br>�ܥǥ�̿��<br>
     * ���ȥ�$srcatom���°��μ¹ԥ����å����Ѥࡣ
     * <p>���Ǥ˼¹ԥ����å����Ѥޤ�Ƥ�������ư���̤����Ȥ��롣
     * <p>�����ƥ��֤��ɤ����ϴط��ʤ����ष����̿����Ѥޤ�륢�ȥब�����ƥ��֤Ǥ��롣*/
    public static final int ENQUEUEATOM = 33;
    
	/** localenqueueatom [srcatom]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * enqueueatom��Ʊ����������$srcatom��<B>�����Ʊ���������������������¸�ߤ���</B>��*/
	public static final int LOCALENQUEUEATOM = LOCAL + ENQUEUEATOM;

    /** dequeueatom [srcatom]
     * <br>��Ŭ���ѥܥǥ�̿��<br>
     * ���ȥ�$srcatom�����η׻��Ρ��ɤˤ���¹ԥ����å������äƤ���С��¹ԥ����å�������Ф���
     * <p><b>���</b>������̿��ϡ���������̤Υ�������︺���뤿���Ǥ�դ˻��Ѥ��뤳�Ȥ��Ǥ��롣
     * ���ȥ������Ѥ���Ȥ��ϡ����̴ط�����դ��뤳�ȡ�
     * <p>�ʤ���¾�η׻��Ρ��ɤˤ���¹ԥ����å������Ƥ����/�ѹ�����̿���¸�ߤ��ʤ���
     * <p>����̿��ϡ�Runtime.Atom.dequeue��ƤӽФ���*/
    public static final int DEQUEUEATOM = 34;
	// LOCALDEQUEUEATOM�Ϻ�Ŭ���θ��̤�̵������Ѳ�

	/** freeatom [srcatom]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * ���⤷�ʤ���
	 * <p>$srcatom���ɤ���ˤ�°���������Ĥ��η׻��Ρ�����μ¹ԥ����å����Ѥޤ�Ƥ��ʤ����Ȥ�ɽ����
	 * TODO ���ȥ��¾�η׻��Ρ��ɤ��Ѥ�Ǥ����硢͢��ɽ��������������פ�Ĵ�٤롣*/
	public static final int FREEATOM = 35;
	// LOCALFREEATOM������

	/** alterfunc [atom, funcref]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * �ʽ�°�����ġ˥��ȥ�$atom�Υե��󥯥���funcref�ˤ��롣
	 * �����θĿ����ۤʤ����ư���̤����Ȥ��롣*/
	public static final int ALTERFUNC = 36;

	/** localalterfunc [atom, funcref]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * alterfunc��Ʊ����������$atom�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALALTERFUNC = LOCAL + ALTERFUNC;

	/** alterfuncindirect [atom, func]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * alterfunc��Ʊ�����������ե��󥯥���$func�ˤ��롣*/
	public static final int ALTERFUNCINDIRECT = 37;
	
	/** localalterfuncindirect [atom, func]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * alterfuncindirect��Ʊ����������$atom�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALALTERFUNCINDIRECT = LOCAL + ALTERFUNCINDIRECT;

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

	/** allocatomindirect [-dstatom, func]
	 * <br>���դ���ĥ�Ѻ�Ŭ����̿��<br>
	 * �ե��󥯥�$func����Ľ�°�������ʤ����������ȥ������������Ȥ�$dstatom���������롣
	 * <p>�����ɸ����ǻȤ���������ȥ���������뤿��˻��Ѥ���롣*/
	public static final int ALLOCATOMINDIRECT = 41;
	// LOCALALLOCATOMINDIRECT������

	/** copyatom [-dstatom, mem, srcatom]
	 * <br>���դ���ĥ�ѥܥǥ�̿��
	 * ���ȥ�$srcatom��Ʊ��̾���Υ��ȥ����$mem����������$dstatom�����������֤���
	 * �¹ԥ����å������ʤ���
	 * <p>�ޥå��󥰤��������դ����ȥ�򥳥ԡ����뤿��˻��Ѥ��롣
	 * <p>getfunc[func,srcatom];newatomindirect[dstatom,mem,func]��Ʊ������ä��ѻߡ�
	 * copygroundterm�˰ܹԤ��٤����⤷��ʤ���*/
	public static final int COPYATOM = 42;

	/** localcopyatom [-dstatom, mem, srcatom]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * copyatom��Ʊ����������$mem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALCOPYATOM = LOCAL + COPYATOM;

	/** localaddatom [dstmem, atom]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * �ʽ�°�������ʤ��˥��ȥ�$atom����$dstmem�˽�°�����롣
	 * ������$dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALADDATOM = LOCAL + 43;
	// ���̤� ADDATOM ��¸�ߤ��ʤ���
	
	// ���������ܥܥǥ�̿�� (50--59)    
	// [local]removemem                [srcmem, parentmem]
	// [local]newmem          [-dstmem, srcmem]
	//  ----- newroot         [-dstmem, srcmem, node]
	//  ----- movecells                [dstmem, srcmem]
	//  ----- enqueueallatoms          [srcmem]
	//  ----- freemem                  [srcmem]
	// [local]addmem                   [dstmem, srcmem]
	// [local]unlockmem                [srcmem]

	/** removemem [srcmem, parentmem]
	 * <br>�ܥǥ�̿��<br>
	 * ��$srcmem������$parentmem�ˤ�����Ф���
	 * ��$srcmem�ϥ�å����˼¹��쥹���å���������Ƥ��뤿�ᡢ�¹��쥹���å������ʤ���
	 * @see removeproxies */
	public static final int REMOVEMEM = 50;

	/** localremovemem [srcmem, parentmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * removemem��Ʊ����������$srcmem�ο����$parentmem�ˤϤ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALREMOVEMEM = LOCAL + REMOVEMEM;

	/** newmem [-dstmem, srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * �ʳ��������줿����$srcmem�˿������ʥ롼����Ǥʤ��˻�����������$dstmem�������������������롣
	 * ���ξ��γ������ϡ�$srcmem��Ʊ���¹��쥹���å����Ѥळ�Ȥ��̣���롣
	 * @see newroot
	 * @see addmem */
	public static final int NEWMEM = 51;

	/** localnewmem [-dstmem, srcmem]
	* <br>��Ŭ���ѥܥǥ�̿��<br>
	* newmem��Ʊ����������$srcmem��<B>�����Ʊ���������ˤ�äƴ��������</B>��*/
	public static final int LOCALNEWMEM = LOCAL + NEWMEM;

	/** newroot [-dstmem, srcmem, node]
	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
	 * ��$srcmem�λ����ʸ����node�ǻ��ꤵ�줿�׻��Ρ��ɤǼ¹Ԥ���뿷������å����줿�롼������������
	 * ���Ȥ�$dstmem�����������ʥ�å������ޤޡ˳��������롣
	 * ���ξ��γ������ϡ����μ¹��쥹���å����Ѥळ�Ȥ��̣���롣
	 * <p>newmem�Ȱ㤤�����Υ롼����Υ�å�������Ū�˲������ʤ���Фʤ�ʤ���
	 * @see unlockmem */
	public static final int NEWROOT = 52;
	// LOCALNEWROOT�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	
	/** movecells [dstmem, srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * ��$srcmem�ˤ������ƤΥ��ȥ�Ȼ���ʥ�å���������Ƥ��ʤ��ˤ���$dstmem�˰�ư���롣
	 * �¹��쥹���å�����Ӽ¹ԥ����å������ʤ���
	 * <p>�¹Ը塢��$srcmem�Ϥ��Τޤ��Ѵ�����ʤ���Фʤ�ʤ���
	 * <p>�¹Ը塢��$dstmem�����ƤΥ����ƥ��֥��ȥ�򥨥󥭥塼��ľ���٤��Ǥ��롣
	 * <p><b>���</b>��Ruby�Ǥ�pour����̾���ѹ�
	 * @see enqueueallatoms */
	public static final int MOVECELLS = 53;
	// LOCALMOVECELLS�Ϻ�Ŭ���θ��̤�̵������Ѳ������뤤�Ϥ�����ò��������ͤˤ��롣

	/** enqueueallatoms [srcmem]
	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
	 * ���⤷�ʤ����ޤ��ϡ���$srcmem�ˤ������ƤΥ����ƥ��֥��ȥ�򤳤���μ¹ԥ����å����Ѥࡣ
	 * <p>���ȥब�����ƥ��֤��ɤ�����Ƚ�Ǥ���ˤϡ�
	 * �ե��󥯥���ưŪ����������ˡ�ȡ�2�ĤΥ��롼�פΥ��ȥब����Ȥ��ƽ�°�줬����������ˡ�����롣
	 * @see enqueueatom */
	public static final int ENQUEUEALLATOMS = 54;
	// LOCALENQUEUEALLATOMS�Ϻ�Ŭ���θ��̤�̵������Ѳ�

	/** freemem [srcmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * ���⤷�ʤ���
	 * <p>$srcmem���ɤ���ˤ�°���������ĥ����å����Ѥޤ�Ƥ��ʤ����Ȥ�ɽ����
	 * @see freeatom */
	public static final int FREEMEM = 55;
	// LOCALFREEMEM������

	/** addmem [dstmem, srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * ��å����줿�ʿ����̵������$srcmem��ʳ��������줿����$dstmem�˰�ư������å������ޤ޳��������롣
	 * ���ξ��γ������ϡ�$srcmem���롼����ξ�硢���μ¹��쥹���å����Ѥळ�Ȥ��̣����
	 * �롼����Ǥʤ���硢$dstmem��Ʊ���¹��쥹���å����Ѥळ�Ȥ��̣���롣
	 * <p>��$srcmem������Ѥ��뤿��˻��Ѥ���롣
	 * <p>newmem�Ȱ㤤��$srcmem�Υ�å�������Ū�˲������ʤ���Фʤ�ʤ���
	 * @see unlockmem */
	public static final int ADDMEM = 56;

	/** localaddmem [dstmem, srcmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * addmem��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣$dstmem�ˤĤ��Ƥϲ��Ⲿ�ꤷ�ʤ���*/
	public static final int LOCALADDMEM = LOCAL + ADDMEM;

	/** unlockmem [srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * �ʳ�������������$srcmem�Υ�å���������롣
	 * $srcmem���롼����ξ�硢���μ¹��쥹���å������Ƥ�¹��쥹���å������ž�����롣
	 * <p>addmem�ˤ�äƺ����Ѥ��줿�졢�����newroot�ˤ�äƥ롼��ǿ������������줿
	 * �롼������Ф��ơ��ʻ�¹������֤ˡ�ɬ���ƤФ�롣
	 * <p>�¹Ը塢$srcmem�ؤλ��Ȥ��Ѵ����ʤ���Фʤ�ʤ���*/
	public static final int UNLOCKMEM = 57;

	/** localunlockmem [srcmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * unlockmem��Ʊ����������$srcmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALUNLOCKMEM = LOCAL + UNLOCKMEM;

	// ͽ�� (60--64)
	
	// ��󥯤�����ܥǥ�̿�� (65--69)
	// [local]newlink     [atom1, pos1, atom2, pos2, mem1]
	// [local]relink      [atom1, pos1, atom2, pos2, mem]
	// [local]unify       [atom1, pos1, atom2, pos2]
	// [local]inheritlink [atom1, pos1, link2, mem]

	/** newlink [atom1, pos1, atom2, pos2, mem1]
	 * <br>�ܥǥ�̿��<br>
	 * ���ȥ�$atom1����$mem1�ˤ���ˤ���pos1�����ȡ�
	 * ���ȥ�$atom2����pos2�����δ֤�ξ������󥯤�ĥ�롣
	 * <p>ŵ��Ū�ˤϡ�$atom1��$atom2�Ϥ������롼��ܥǥ���¸�ߤ��롣
	 * <p><b>���</b>��Ruby�Ǥ���������������ѹ����줿 */
	public static final int NEWLINK = 65;

	/** localnewlink [atom1, pos1, atom2, pos2 (,mem1)]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * newlink��Ʊ������������$mem1�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALNEWLINK = LOCAL + NEWLINK;

	/** relink [atom1, pos1, atom2, pos2, mem]
	 * <br>�ܥǥ�̿��<br>
	 * ���ȥ�$atom1����$mem�ˤ���ˤ���pos1�����ȡ�
	 * ���ȥ�$atom2����pos2�����Υ�������$mem�ˤ���ˤΰ�������³���롣
	 * <p>ŵ��Ū�ˤϡ�$atom1�ϥ롼��ܥǥ��ˡ�$atom2�ϥ롼��إåɤ�¸�ߤ��롣
	 * <p>���դ��ץ���ʸ̮��̵���롼��Ǥϡ��Ĥͤ�$mem������ʤΤ�localrelink�����ѤǤ��롣
	 * <p>�¹Ը塢$atom2[pos2]�����Ƥ�̵���ˤʤ롣*/
	public static final int RELINK = 66;

	/** localrelink [atom1, pos1, atom2, pos2 (,mem)]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * relink��Ʊ������������$mem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALRELINK = LOCAL + RELINK;

	/** unify [atom1, pos1, atom2, pos2, mem]
	 * <br>�ܥǥ�̿��<br>
	 * ���ȥ�$atom1����pos1�����Υ�������$mem�ˤ���ˤΰ����ȡ�
	 * ���ȥ�$atom2����pos2�����Υ�������$mem�ˤ���ˤΰ�������³���롣
	 * <p>ŵ��Ū�ˤϡ�$atom1��$atom2�Ϥ������롼��إåɤ�¸�ߤ��롣
	 * <p>���դ��ץ���ʸ̮��̵���롼��Ǥϡ��Ĥͤ�$mem������ʤΤ�localunify�����ѤǤ��롣*/
	public static final int UNIFY = 67;

	/** localunify [atom1, pos1, atom2, pos2 (,mem)]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * unify��Ʊ������������$mem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALUNIFY = LOCAL + UNIFY;

	/** inheritlink [atom1, pos1, link2, mem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * ���ȥ�$atom1����$mem�ˤ���ˤ���pos1�����ȡ�
	 * ���$link2�Υ�������$mem�ˤ���ˤ���³���롣
	 * <p>ŵ��Ū�ˤϡ�$atom1�ϥ롼��ܥǥ���¸�ߤ���$link2�ϥ롼��إåɤ�¸�ߤ��롣relink�����ѡ�
	 * <p>���դ��ץ���ʸ̮��̵���롼��Ǥϡ��Ĥͤ�$mem������ʤΤ�inheritrelink�����ѤǤ��롣
	 * <p>$link2�Ϻ����Ѥ���뤿�ᡢ�¹Ը��$link2���Ѵ����ʤ���Фʤ�ʤ���
	 * @see getlink */
	public static final int INHERITLINK = 68;

	/** localinheritlink [atom1, pos1, link2 (,mem)]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * inheritlink��Ʊ������������$mem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALINHERITLINK = LOCAL + INHERITLINK;

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

    /** removetoplevelproxies [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��$srcmem������ˤ��̲ᤷ�Ƥ���̵�ط��ʼ�ͳ��󥯴������ȥ�����롣
	 * <p>removeproxies�����ƽ���ä���ǸƤФ�롣*/
    public static final int REMOVETOPLEVELPROXIES = 71;
	// LOCALREMOVETOPLEVELPROXIES�Ϻ�Ŭ���θ��̤�̵������Ѳ�

    /** insertproxies [parentmem,childmem]
     * <br>�ܥǥ�̿��<br>
     * ���ꤵ�줿��֤˼�ͳ��󥯴������ȥ��ư�������롣
     * <p>addmem�����ƽ���ä���ǸƤФ�롣*/
    public static final int INSERTPROXIES = 72;
	// LOCALINSERTPROXIES�Ϻ�Ŭ���θ��̤�̵������Ѳ�
	
    /** removetemporaryproxies [srcmem]
     * <br>�ܥǥ�̿��<br>
     * ��$srcmem������ˤ˻Ĥ��줿"star"���ȥ�����롣
     * <p>insertproxies�����ƽ���ä���ǸƤФ�롣*/
    public static final int REMOVETEMPORARYPROXIES = 73;
	// LOCALREMOVETEMPORARYPROXIES�Ϻ�Ŭ���θ��̤�̵������Ѳ�

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

	/** localloadruleset [dstmem, ruleset]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * loadruleset��Ʊ����������$dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALLOADRULESET = LOCAL + LOADRULESET;

	/** copyrules [dstmem, srcmem]
	 * <br>�ܥǥ�̿��<br>
	 * ��$srcmem�ˤ������ƤΥ롼�����$dstmem�˥��ԡ����롣
	 * <p><b>���</b>��Ruby�Ǥ�inheritrules����̾���ѹ� */
	public static final int COPYRULES = 76;

	/** localcopyrules [dstmem, srcmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * copyrules��Ʊ����������$dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣$srcmem�ˤĤ��Ƥϲ��Ⲿ�ꤷ�ʤ���*/
	public static final int LOCALCOPYRULES = LOCAL + COPYRULES;

	/** clearrules [dstmem]
	 * <br>�ܥǥ�̿��<br>
	 * ��$dstmem�ˤ������ƤΥ롼���õ�롣*/
	public static final int CLEARRULES = 77;
	
	/** localclearrules [dstmem]
	 * <br>��Ŭ���ѥܥǥ�̿��<br>
	 * clearrules��Ʊ����������$dstmem�Ϥ��η׻��Ρ��ɤ�¸�ߤ��롣*/
	public static final int LOCALCLEARRULES = LOCAL + CLEARRULES;

	/** loadmodule [dstmem, ruleset]
	 * <br>�ܥǥ�̿��<br>
	 * �롼�륻�å�ruleset����$dstmem�˥��ԡ����롣
	 */
	public static final int LOADMODULE = 78;

    // ���դ��Ǥʤ��ץ���ʸ̮�򥳥ԡ��ޤ����Ѵ����뤿���̿�� (80--89)
	//  ----- recursivelock            [srcmem]
	//  ----- recursiveunlock          [srcmem]
	//  ----- copymem         [-dstmem, srcmem]
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

    /** recursiveunlock [srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * ��$srcmem�����Ƥλ�����Ф��ƺƵ�Ū�˥�å���������롣
     * ��Ϥ����������륿�����μ¹��쥹���å��˺Ƶ�Ū���Ѥޤ�롣
     * <p>�Ƶ�Ū���Ѥ���ˡ�ϡ�����ͤ��롣
     * @see unlockmem */
    public static final int RECURSIVEUNLOCK = 81;
	// LOCALRECURSIVEUNLOCK�Ϻ�Ŭ���θ��̤�̵������Ѳ�

    /** copymem [-dstmem, srcmem]
     * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
     * �Ƶ�Ū�˥�å����줿��$srcmem�����ƤΥ��ԡ������������$dstmem������롣
     * $dstmem�μ�ͳ��󥯴������ȥ����1�����ϡ��б�����$srcmem����1������ؤ���󥯤ǽ�������뤬��
     * ¾�η׻��Ρ��ɤˤϽ���������Τ��ʤ���*/
    public static final int COPYMEM = 82;
	// LOCALCOPYMEM�Ϻ�Ŭ���θ��̤�̵������Ѳ�

	/** dropmem [srcmem]
	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
	 * �Ƶ�Ū�˥�å����줿��$srcmem���˴����롣
	 * ��������¹�����롼����Ȥ��륿�����϶�����λ���롣*/
	public static final int DROPMEM = 83;
	// LOCALDROPMEM�Ϻ�Ŭ���θ��̤�̵������Ѳ�

//	/** * [srcatom,pos]
//	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��<br>
//	 * ���ȥ�srcatom����1�����Υ����Υ����Υ��ȥ����pos�����ȡ�
//	 * srcatom����1�����δ֤���������󥯤�Ž�롣��ͳ��󥯴������ȥ��ɤ����뤫��
//	 * <p>�ץ���ʸ̮�Υ��ԡ����˻��Ѥ���롣*/
//	public static final int * = err;

	// ���ȥླྀ�ĤΥ���ѥ���ˤϡ�findatom��run��Ȥ��Ȥ������⤷��ʤ������Ǥ��ʤ����⤷��ʤ���

	// TODO �ץ���ʸ̮������Ū�ʰ�����ϡ���˰��Ū�ʥ��ȥ���äƴ������롣
	// ���ԡ��������relink���롣

	// ͽ�� (90--99)

	//////////////////////////////////////////////////////////////////
	
	// 200�ְʹߤ�̿��ˤ�LOCAL�����Ǥ�¸�ߤ��ʤ�
	
	// ����̿�� (200--209)
	//  -----  react       [ruleref, [memargs...], [atomargs...]]
	//  -----  inlinereact [ruleref, [memargs...], [atomargs...]]
	//  -----  resetvars   [[memargs...], [atomargs...], [varargs...]]
	//  -----  spec        [formals,locals]
	//  -----  proceed
	//  -----  stop 
	//  -----  branch      [[instructions...]]
	//  -----  loop        [[instructions...]]
	//  -----  run         [[instructions...]]
	//  -----  not         [[instructions...]]

    /** react [ruleref, [memargs...], [atomargs...]]
     * <br>���Ԥ��ʤ�������̿��<br>
     * �롼��ruleref���Ф���ޥå��󥰤������������Ȥ�ɽ����
     * �����ϤϤ��Υ롼��Υܥǥ���ƤӽФ��ʤ���Фʤ�ʤ���*/
    public static final int REACT = 200;

	/** inlinereact [ruleref, [memargs...], [atomargs...]]
	 * <br>̵�뤵���<br>
     * �롼��ruleref���Ф���ޥå��󥰤������������Ȥ�ɽ����
     * <p>�ȥ졼���ѡ�*/
	public static final int INLINEREACT = 201;

	/** reloadvars [[memargs...], [atomargs...], [varargs...]]
	 * <br>���Ԥ��ʤ�������̿�ᤪ��Ӻ�Ŭ���ѥܥǥ�̿��<br>
	 * �ѿ��٥��������Ƥ��������롣�������ѿ��ֹ�ϡ��졢���ȥࡢ����¾�ν��֤�0���鿶��ľ����롣
	 * <b>���</b>��memargs[0]�����줬ͽ�󤷤Ƥ��뤿���ѹ����ƤϤʤ�ʤ���
	 * <p>�ʻ��͸�Ƥ���̤����̿���
	 */
	public static final int RELOADVARS = 202;

    /** spec [formals, locals]
     * <br><strike>
     * spec [memformals,atomformals,memlocals,atomlocals,linklocals,funclocals]</strike>
     * <br>̵�뤵���<br>
     * �������ȶɽ��ѿ��θĿ���������롣*/
    public static final int SPEC = 203;

	/** proceed
	 * <br>�ܥǥ�̿��<br>
	 * ����proceed̿�᤬��°����̿����μ¹Ԥ������������Ȥ�ɽ����
	 * <p>�ȥåץ�٥�̿����ǻ��Ѥ��줿��硢�롼���Ŭ�Ѥ�����ꡢ
	 * �����Ѥ��줿���Ƥ���Υ�å���������������������Ƥ����������������Ȥ�ɽ����
	 * <p><b>���</b>��proceed�ʤ���̿����ν�ü�ޤǿʤ����硢
	 * ����̿����μ¹Ԥϼ��Ԥ�����ΤȤ�����ͤ����Ѥ��줿��*/
	public static final int PROCEED = 204;

	/** stop 
	 * <br>��ͽ�󤵤줿�˼��Ԥ��ʤ�������̿��<br>
	 * proceed��Ʊ�����ޥå��󥰤ȥܥǥ���̿�᤬���礵�줿���Ȥ�ȼ�ä��ѻߤ����ͽ�ꡣ
	 * <p>ŵ��Ū�ˤϡ�������˥ޥå��������Ȥ�ɽ������˻��Ѥ���롣
	 */
	public static final int STOP = 205;

    /** branch [[instructions...]]
     * <br>��¤��̿��<br>
     * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����
     * �����¹���˼��Ԥ�����硢�����¹���˼���������å��������������̿��˿ʤࡣ
     * �����¹����proceed̿���¹Ԥ�����硢�����ǽ�λ���롣*/
    public static final int BRANCH = 206;

	/** loop [[instructions...]]
	 * <br>��¤��̿��<br>
	 * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����
     * �����¹���˼��Ԥ�����硢�����¹���˼���������å��������������̿��˿ʤࡣ
     * �����¹����proceed̿���¹Ԥ�����硢����loop̿��μ¹Ԥ򷫤��֤���*/
	public static final int LOOP = 207;

	/** run [[instructions...]]
	 * <br>��ͽ�󤵤줿�˹�¤��̿��<br>
	 * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����������ϥ�å���������ƤϤʤ�ʤ���
	 * �����¹���˼��Ԥ�����硢����̿��˿ʤࡣ
	 * �����¹����proceed̿���¹Ԥ�����硢����̿��˿ʤࡣ
	 * <p>���衢����Ū�ʰ����դ��Υץ���ʸ̮�Υ���ѥ���˻��Ѥ��뤿���ͽ��*/
	public static final int RUN = 208;

	/** not [[instructions...]]
	 * <br>��ͽ�󤵤줿�˹�¤��̿��<br>
	 * ������̿�����¹Ԥ��뤳�Ȥ�ɽ����������ϥ�å���������ƤϤʤ�ʤ���
	 * �����¹���˼��Ԥ�����硢����̿��˿ʤࡣ
	 * �����¹����proceed̿���¹Ԥ�����硢����̿�᤬���Ԥ��롣
	 * <p>���衢������Υ���ѥ���˻��Ѥ��뤿���ͽ��*/
	public static final int NOT = 209;

	// �Ȥ߹��ߵ�ǽ�˴ؤ���̿��ʲ��� (210--219)
	//  -----  inline  [atom, inlineref]
	//  -----  builtin [class, method, [links...]]

	/** inline [atom, inlineref]
	 * <br>������̿�ᡢ�ܥǥ�̿��<br>
	 * ���ȥ�$atom���Ф��ơ�inlineref�����ꤹ�륤��饤��̿���Ŭ�Ѥ����������뤳�Ȥ��ǧ���롣
	 * <p>inlineref�ˤϸ��ߡ�����饤���ֹ���Ϥ����ȤˤʤäƤ��뤬��
	 * �����Ϥ�inlineref��̵�뤷��$atom�Υե��󥯥����饤��饤��̿�����ꤷ�Ƥ褤��
	 * ���ͤϾ����ѹ�����뤫�⤷��ʤ���
	 * <p>�ܥǥ��ǸƤФ����ŵ��Ū�ˤϡ����ƤΥ�󥯤�ĥ��ʤ�����ľ��˸ƤФ�롣*/
	public static final int INLINE = 210;

	/** builtin [class, method, [links...]]
	 * <br>��ͽ�󤵤줿�˥ܥǥ�̿��
	 * ��󥯻���$(links[i])�������ͣ��ΰ����Ȥ���Java�Υ��饹�᥽�åɤ�ƤӽФ���
	 * <p>���󥿥ץ꥿ư���Ȥ����Ȥ߹��ߵ�ǽ���󶡤��뤿��˻��Ѥ��롣
	 * <p>�̾�ϡ�$builtin(class,method):(X1,��,Xn)���б����������μ���ˤ�äƼ��Τ�Τ��Ϥ���롣
	 * ���դ��ץ���ʸ̮�ϡ��줫�������إåɽи��ʤޤ��ϥ���������������Ρˤ��Ϥ���롣
	 * �إåɤȥܥǥ���1�󤺤Ľи������󥯤ϡ��إåɤǤΥ�󥯽и����Ϥ���롣
	 * �ܥǥ���2��и������󥯤ϡ�X=X�ǽ�������줿�塢�ƽи���إåɤǤνи��ȸ��ʤ����Ϥ���롣*/
	public static final int BUILTIN = 211;

	///////////////////////////////////////////////////////////////////////

	// ���դ��ץ���ʸ̮�򰷤�������ɲ�̿�� (216--219)	

	/** eqground [groundlink1,groundlink2]
	 * <br>��ͽ�󤵤줿�˷��դ���ĥ�ѥ�����̿��<br>
	 * �����ץ�����ؤ�2�ĤΥ��link1��link2���Ф��ơ�
	 * ����餬Ʊ�������δ����ץ����Ǥ��뤳�Ȥ��ǧ���롣
	 * @see isground */
	public static final int EQGROUND = 216;
    	
	// �������Τ���Υ�����̿�� (220--229)	

	/** isground [link]
	 * <br>��ͽ�󤵤줿�˷��դ���ĥ�ѥ�����̿��<br>
	 * ���$link�λؤ��褬�����ץ����Ǥ��뤳�Ȥ��ǧ���롣
	 * ���ʤ��������褫�����餺�ˡ���ã��ǽ�ʥ��ȥब���Ƥ������¸�ߤ��Ƥ��뤳�Ȥ��ǧ���롣
	 * @see getlink */
	public static final int ISGROUND = 220;
	
	/** isunary [atom]
	 * <br>���դ���ĥ�ѥ�����̿��<br>
	 * ���ȥ�$atom��1�����Υ��ȥ�Ǥ��뤳�Ȥ��ǧ���롣*/
	public static final int ISUNARY     = 221;
	public static final int ISUNARYFUNC = ISUNARY + OPT;

	/** isint [atom]
	 * <br>���դ���ĥ�ѥ�����̿��<br>
	 * ���ȥ�$atom���������ȥ�Ǥ��뤳�Ȥ��ǧ���롣*/
	public static final int ISINT    = 225;
	public static final int ISFLOAT  = 226;
	public static final int ISSTRING = 227;

	/** isintfunc [func]
	 * <br>���դ���ĥ�Ѻ�Ŭ���ѥ�����̿��<br>
	 * �ե��󥯥�$func�������ե��󥯥��Ǥ��뤳�Ȥ��ǧ���롣*/
	public static final int ISINTFUNC    = ISINT    + OPT;
	public static final int ISFLOATFUNC  = ISFLOAT  + OPT;
	public static final int ISSTRINGFUNC = ISSTRING + OPT;

	// �����Ѥ��Ȥ߹��ߥܥǥ�̿�� (400--419+OPT)
	/** iadd [-dstintatom, intatom1, intatom2]
	 * <br>�����Ѥ��Ȥ߹���̿��<br>
	 * �������ȥ�βû���̤�ɽ����°�������ʤ��������ȥ����������$dstintatom���������롣*/
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
	/** iadd [-dstintatom, intatom1, intatom2]
	 * <br>�����Ѥ��Ȥ߹���̿��<br>
	 * intatom1��intatom2�ӥå�ʬ���Ĥ�(����)�����եȤ�����̤�ɽ����°�������ʤ��������ȥ����������$dstintatom���������롣*/
	public static final int ISAL = 414;
	/** iadd [-dstintatom, intatom1, intatom2]
	 * <br>�����Ѥ��Ȥ߹���̿��<br>
	 * intatom1��intatom2�ӥå�ʬ���Ĥ�(����)�����եȤ�����̤�ɽ����°�������ʤ��������ȥ����������$dstintatom���������롣*/
	public static final int ISAR = 415;
	/** iadd [-dstintatom, intatom1, intatom2]
	 * <br>�����Ѥ��Ȥ߹���̿��<br>
	 * intatom1��intatom2�ӥå�ʬ���������եȤ�����̤�ɽ����°�������ʤ��������ȥ����������$dstintatom���������롣*/
	public static final int ISHR = 416;

	/** iaddfunc [-dstintfunc, intfunc1, intfunc2]
	 * <br>�����Ѥκ�Ŭ�����Ȥ߹���̿��<br>
	 * �����ե��󥯥��βû���̤�ɽ�������ե��󥯥�����������$dstintfunc���������롣*/
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

	// �����Ѥ��Ȥ߹��ߥ�����̿�� (420--429+OPT)

	/** ilt [intatom1, intatom2]
	 * <br>�����Ѥ��Ȥ߹��ߥ�����̿��<br>
	 * �������ȥ���ͤ��羮��Ӥ�����Ω�Ĥ��Ȥ��ǧ���롣*/
	public static final int ILT = 420;
	public static final int ILE = 421;
	public static final int IGT = 422;
	public static final int IGE = 423;	

	/** iltfunc [intfunc1, intfunc2]
	 * <br>�����Ѥκ�Ŭ�����Ȥ߹��ߥ�����̿��<br>
	 * �����ե��󥯥����ͤ��羮��Ӥ�����Ω�Ĥ��Ȥ��ǧ���롣*/
	public static final int ILTFUNC = ILT + OPT;
	public static final int ILEFUNC = ILE + OPT;
	public static final int IGTFUNC = IGT + OPT;
	public static final int IGEFUNC = IGE + OPT;


	// ��ư���������Ѥ��Ȥ߹��ߥܥǥ�̿�� (600--619+OPT)
	public static final int FADD = 600;
	public static final int FSUB = 601;
	public static final int FMUL = 602;
	public static final int FDIV = 603;
	public static final int FNEG = 604;
	public static final int FMOD = 605;
	public static final int FNOT = 610;
	public static final int FAND = 611;
	public static final int FOR  = 612;
	public static final int FXOR = 613;
	public static final int FSAL = 614;
	public static final int FSAR = 615;
	public static final int FSHR = 616;
	
	public static final int FADDFUNC = FADD + OPT;
	public static final int FSUBFUNC = FSUB + OPT;
	public static final int FMULFUNC = FMUL + OPT;
	public static final int FDIVFUNC = FDIV + OPT;
	public static final int FNEGFUNC = FNEG + OPT;
	public static final int FMODFUNC = FMOD + OPT;
	public static final int FNOTFUNC = FNOT + OPT;
	public static final int FANDFUNC = FAND + OPT;
	public static final int FORFUNC  = FOR  + OPT;
	public static final int FXORFUNC = FXOR + OPT;
	public static final int FSALFUNC = FSAL + OPT;
	public static final int FSARFUNC = FSAR + OPT;
	public static final int FSHRFUNC = FSHR + OPT;
	
	// ��ư���������Ѥ��Ȥ߹��ߥ�����̿�� (620--629+OPT)
	public static final int FLT = 620;
	public static final int FLE = 621;
	public static final int FGT = 622;
	public static final int FGE = 623;	

	public static final int FLTFUNC = FLT + OPT;
	public static final int FLEFUNC = FLE + OPT;
	public static final int FGTFUNC = FGT + OPT;
	public static final int FGEFUNC = FGE + OPT;

	////////////////////////////////////////////////////////////////

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
     * @param actual �°���
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
     */
    public static Instruction react(Rule r, List memactuals, List atomactuals) {
		Instruction i = new Instruction(REACT);
		i.add(r);
		i.add(memactuals);
		i.add(atomactuals);
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
	/** newlink ̿�����������
	 * @deprecated */
	public static Instruction newlink(int atom1, int pos1, int atom2, int pos2) {
		return new Instruction(NEWLINK,atom1,pos1,atom2,pos2);
	}
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

	public Object clone() {
		Instruction c = new Instruction();
		c.kind = this.kind;
		Iterator it = this.data.iterator();
		while (it.hasNext()) {
			c.data.add(it.next());
		}
		return c;
	}
	
	//////////////////////////////////
	// ��Ŭ���郎�Ȥ���̿����񤭴����Τ���Υ��饹�᥽�å�
	// @author Mizuno
	

	/**
	 * Ϳ����줿�б�ɽ�ˤ�äơ��ܥǥ�̿��������ѿ���񤭴����롣<br>
	 * ̿��������ѿ������б�ɽ�Υ����˽и������硢�б������ͤ˽񤭴����ޤ���
	 * 
	 * @param list �񤭴�����̿����
	 * @param map �ѿ����б�ɽ��
	 */
	public static void changeVar(List list, Map map) {
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) { // TODO ������̿��䡢�����ѿ���񤭴�����
				case Instruction.DEREF:
					changeArg(inst, 1, map);
					changeArg(inst, 2, map);
					break;
				case Instruction.FUNC:
					changeArg(inst, 1, map);
					break;
				case Instruction.NEWATOM:
				case Instruction.LOCALNEWATOM:
				case Instruction.NEWATOMINDIRECT:
				case Instruction.LOCALNEWATOMINDIRECT:
					changeArg(inst, 1, map);
					changeArg(inst, 2, map);
					break;
				case Instruction.NEWLINK:
				case Instruction.LOCALNEWLINK:
					changeArg(inst, 1, map);
					changeArg(inst, 3, map);
//					changeArg(inst, 5, map);
					break;
				case Instruction.GETLINK:
					changeArg(inst, 1, map);
					changeArg(inst, 2, map);
					break;
				case Instruction.INHERITLINK:
				case Instruction.LOCALINHERITLINK:
					changeArg(inst, 1, map);
					changeArg(inst, 3, map);
					changeArg(inst, 4, map);
					break;
				case Instruction.ENQUEUEATOM:
					changeArg(inst, 1, map);
					break;
				case Instruction.DEQUEUEATOM:
					changeArg(inst, 1, map);
					break;
				case Instruction.REMOVEATOM:
				case Instruction.LOCALREMOVEATOM:
					changeArg(inst, 2, map);
					break;
				case Instruction.COPYATOM:
				case Instruction.LOCALCOPYATOM:
					changeArg(inst, 2, map);
					break;
				case Instruction.LOCALADDATOM:
					changeArg(inst, 1, map);
					break;
				case Instruction.REMOVEMEM:
					changeArg(inst, 1, map);
					changeArg(inst, 2, map);
					break;
				case Instruction.NEWMEM:
					changeArg(inst, 2, map);
					break;
				case Instruction.NEWROOT:
					changeArg(inst, 2, map);
					break;
				case Instruction.MOVECELLS:
					changeArg(inst, 1, map);
					changeArg(inst, 2, map);
					break;
				case Instruction.ENQUEUEALLATOMS:
					changeArg(inst, 1, map);
					break;
				case Instruction.FREEMEM:
					changeArg(inst, 1, map);
					break;
				case Instruction.ADDMEM:
				case Instruction.LOCALADDMEM:
					changeArg(inst, 1, map);
					changeArg(inst, 2, map);
					break;
				case Instruction.UNLOCKMEM:
				case Instruction.LOCALUNLOCKMEM:
					changeArg(inst, 1, map);
					break;
				case Instruction.REMOVEPROXIES:
				case Instruction.REMOVETOPLEVELPROXIES:
				case Instruction.REMOVETEMPORARYPROXIES:
					changeArg(inst, 1, map);
					break;
				case Instruction.INSERTPROXIES:
					changeArg(inst, 1, map);
					changeArg(inst, 2, map);
					break;
				case Instruction.LOADRULESET:
				case Instruction.LOCALLOADRULESET:
					changeArg(inst, 1, map);
					break;
				case Instruction.COPYRULES:
				case Instruction.LOCALCOPYRULES:
					changeArg(inst, 1, map);
					changeArg(inst, 2, map);
					break;
				case Instruction.CLEARRULES:
				case Instruction.LOCALCLEARRULES:
					changeArg(inst, 1, map);
					break;
				case Instruction.RECURSIVELOCK:
					changeArg(inst, 1, map);
					break;
				case Instruction.RECURSIVEUNLOCK:
					changeArg(inst, 1, map);
					break;
				case Instruction.COPYMEM:
					changeArg(inst, 1, map);
					changeArg(inst, 2, map);
					break;
				case Instruction.DROPMEM:
					changeArg(inst, 1, map);
					break;
			//
				case RELINK:
				case LOCALRELINK:
					changeArg(inst, 5, map);
					break;
			}
		}
	}
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

	//////////////////////////////////
	//
	// �ǥХå���ɽ���᥽�å�
	//


    /**
     * �ǥХå���ɽ���᥽�åɡ�
     * ̿���ʸ����(String)��Ϳ����ȡ���������̿���int���֤��Ƥ����
     *
     * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
     * @return int
     * 
     */
//     public static int getInstructionInteger(String instructionString){
// 	int answer = -1;
// 	Object tmp;

// 	try {
// 	    answer = ((Integer)table.get(instructionString.toUpperCase())).intValue();
// 	} catch (NullPointerException e){
// 	    System.out.println(e);
// 	    System.exit(1);
// 	}
// 	return answer;
//     }

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
	 * ̿���id (int)��Ϳ����ȡ���������̿���String���֤��Ƥ���롣
	 *
	 * @author NAKAJIMA Motomu <nakajima@ueda.info.waseda.ac.jp>
	 * @return String
	 * 
	 */
	public static String getInstructionString(int kind){
		String answer = "";
		answer = (String)instructionTable.get(new Integer(kind));
		return answer;
		

	/* 
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
	*/
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
		
		if( tmp.length() > 14 ) {
			buffer.replace(0, 14, tmp.substring(0,14));
		} else {
			buffer.replace(0, tmp.length(), tmp);
		}

		if(tmp.equals("loop") && data.size() == 1 && data.get(0) instanceof ArrayList && ((ArrayList)data.get(0)).size() == 1) {
			ArrayList list = (ArrayList)((ArrayList)data.get(0)).get(0);
			if(list.size() == 0) {
				buffer.append("[[[]]]\n");
			} else {
				buffer.append("[[[\n");
				int i;
				for(i = 0; i < list.size()-1; i++){
					buffer.append("  ");
					buffer.append(list.get(i));
					buffer.append(", \n");
				}
				buffer.append("  ");
				buffer.append(list.get(i));
				buffer.append("]]]\n");
			}
		} else {
			buffer.append(data.toString());
		}

		//�����Ǥ�ʬ��
		//instanceOf
		//ArrayList�Ǥ��äơ����Ǥʤ��ơ�����cast������Ƭ���Ǥ������줿�Τ�instanceOf��Integer�Ǥʤ��Ȥ���
		//�������̿����ʤΤǥ���ǥ��+2���餤�ǺƵ�Ū�ˡ�
//		for (int i =0; i < data.size(); i++){
//			ArrayList hoge = (ArrayList)data.get(i);
//
//			if(!hoge.isEmpty()){
//				if(!(hoge.get(0) instanceof Integer)){
//					buffer.append("\n  ");
//					buffer.append((String)hoge.get(0));
//				} else {
//					buffer.append(hoge.toString());
//					continue;
//				}
//			} else {
//				buffer.append(hoge.toString());			
//			}
//		}
		
		return buffer.toString();

	//n-kato��2004-01-21�ޤǻȤäƤޤ���
	//return getInstructionString(kind)+"\t"+data.toString();

	//�Ť���2003ǯ�Τ��Ĥ���nakajima�ǤΥ�����
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

//�ʲ����ѻߤ��줿̿������

//* lock [srcmem]
//* <br>���ѻߤ��줿�˥�����̿��<br>
//* ��$srcmem���Ф���Υ�֥�å��󥰤ǤΥ�å��������ߤ롣
//* ��å���������������С�������Ϥޤ����Ȥ�ʡ��å���˼������Ƥ��ʤ��ä���Ǥ���
//* <p>srcmem��memof��ˡ���ѻߤ��줿���ᡢ��å���lockmem�ǹԤ����������ä�lock���ѻߤ��줿��*/
//public static final int LOCK = err;
