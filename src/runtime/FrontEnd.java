/*
 * ������: 2003/10/22
 */
package runtime;

import java.io.FileInputStream;
import java.io.SequenceInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.FileNotFoundException;
import java.lang.SecurityException;
import java.util.*;

import compile.*;
import compile.parser.*;

/**
 * LMNtal �Υᥤ��
 * 
 * 
 * ������: 2003/10/22
 */
public class FrontEnd {
	/**
	 * ���ƤλϤޤꡣ
	 * 
	 * <pre>
	 * ���ޥ�ɥ饤�����
	 *   �ʤ�                       �� REPL ��ư
	 *   �ե�����̾                 �� �ե��������Ȥ�¹Ԥ��ƽ����
	 *   -e [LMNtal oneliner]       �� [LMNtal oneliner] �ʣ���ʸ�ˤ�¹Ԥ��ƽ����
	 *   -d                         �� �ǥХå��⡼��
	 *   --help                     �� �إ�פ�ɽ��
	 * </pre>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FileInputStream fis = null;
		InputStream is = null;
		Reader src = null;
		/**
		 * ���ޥ�ɥ饤����������ä���ե��������Ȥ���¹�
		 */
		for(int i = 0; i < args.length;i++){
			// ɬ��length>0, '-'�ʤ饪�ץ����
			if(args[i].charAt(0) == '-'){
				if(args[i].length() < 2){ // '-'�Τߤλ�
					System.out.println("�����ʥ��ץ����:" + args[i]);
					System.exit(-1);					
				}else{ // ���ץ��������
					switch(args[i].charAt(1)){
					case 'd':
						System.out.println("debug mode");
						Env.debug = 1;
						break;
					case 't':
						Env.fTrace = true;
						break;
					case 's':
						Env.fRandom = true;
						break;
					case 'e':
						// lmntal -e 'a,(a:-b)'
						// �����Ǽ¹ԤǤ���褦�ˤ��롣like perl
						// ���ν����ϱ����������뤱��...��by Hara
						REPL.processLine(args[i+1]);
						System.exit(-1);
						break;
					case '-': // ʸ���󥪥ץ����
						if(args[i].equals("--help")){
							System.out.println("usage: FrontEnd [-d] filename");
						}else{
							System.out.println("�����ʥ��ץ����:" + args[i]);
							System.exit(-1);
						}
						break;
					default:
						System.out.println("�����ʥ��ץ����:" + args[i]);
						System.exit(-1);						
					}							
				}
			}else{ // '-'�ʳ��ǻϤޤ��Τϥե�����Ȥߤʤ�
				try{
					fis = new FileInputStream(args[i]);
				}catch(FileNotFoundException e){
					System.out.println("�ե����뤬���Ĥ���ޤ���:" + args[i]);
					System.exit(-1);
				}catch(SecurityException e){
					System.out.println("�ե����뤬�����ޤ���:" + args[i]);
					System.exit(-1);
				}
				if(is == null) is = fis;
				else is = new SequenceInputStream(is, fis); // �������ե������Ϣ��
			}
		}
		
		// �������ʤ��ʤ�REPL, ����ʤ饽��������¹ԡ�
		if(is == null){
			REPL.run();
		}else{			
			run( new BufferedReader(new InputStreamReader(is)) );
		}
	}
	
	/**
	 * Ϳ����줿�������ˤĤ��ơ���Ϣ�μ¹Ԥ�Ԥ���
	 * @param src Reader ����ɽ���줿������
	 */
	public static void run(Reader src) {
		try{
			LMNParser lp = new LMNParser(src);
				
			compile.structure.Membrane m = lp.parse();
			Env.d("");
			Env.d( "After parse   : "+m );
			
			compile.structure.Membrane root = RulesetCompiler.runStartWithNull(m);
			InterpretedRuleset ir = (InterpretedRuleset)root.rulesets.get(0);
			Env.d( "After compile : "+ir );
			root.showAllRule();
			
			// �¹�
			LMNtalRuntime rt = new LMNtalRuntime();
			ir.react(rt.getGlobalRoot());
			if (Env.fTrace) {
				Env.d( "Before execute : " );
				Env.p( Dumper.dump(rt.getGlobalRoot()) );
			}
			rt.exec();
			if (!Env.fTrace) {
				Env.d( "After execute : " );
				Env.p( Dumper.dump(rt.getGlobalRoot()) );
			}
		} catch (Exception e) {
			Env.e("!! catch !! "+e.getMessage()+"\n"+Env.parray(Arrays.asList(e.getStackTrace()), "\n"));
		}
	}
}
