package compile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import compile.Translator.TranslatorWriter;

import runtime.Env;

/**
 * Translator���Ǥ�������������ʾ�ˤʤä����˳������饹�Ȥ���
 * ʬΥ���뤿��Υ��饹��
 * <p>
 * Translator���Ǥ��������Υ᥽�å���ʬ����Ĺ���ʤꤹ���ʤ��褦�ˡ�
 * �������饹�Ȥ���ʬΥ���롣��������ʬΥ�Ǥ���̿���ʬΥ�Ǥ��ʤ�̿�᤬
 * ����Τǡ�ʬΥ�Ǥ��ʤ�̿�᤬�и����������ǰ�ö�Ǥ��Ф���
 * </p>
 * @author Nakano
 *
 */
public class Ejector{
	///////////////////////////////////////////////////////////////////////////
	// ������
	
	/** �����ե�����ξ�¥����� */
	static final
	private int MAX_BUF = 51200;
	
	///////////////////////////////////////////////////////////////////////////

	/** �Ѵ������ե�����򤪤��ǥ��쥯�ȥ� */
	private File dir;
	/** �Ѵ������ե�����Υѥå�����̾ */
	private String packageName;
	private String className;
	private File outputFile;
	static
	private int serial;
	private StringBuffer buf = new StringBuffer(MAX_BUF);
	private StringBuffer tmpbuf = new StringBuffer(MAX_BUF);
	
	///////////////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	
	/**
	 * @param cn �������륯�饹̾
	 * @param d �ե��������������ǥ��쥯�ȥ�
	 * @param p �������륯�饹�Υѥå�����̾
	 */
	public Ejector(String cn, File d, String p){
		packageName = p;
		className = cn;
		dir = d;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * ����(ʬΥ)����̿��˰�ö���ڤ���դ��롣
	 * @param myWriter ʬΥ����븵�Υե�����ص��Ҥ��뤿���writer��
	 * 
	 * <p>
	 * ʬΥ����̿��˰�ö���ڤ��Ĥ��������ե�����Ȥ��ƽ��Ϥ��뤫�ɤ���
	 * Ƚ�Ǥ��롣Ejecotr�ΥХåե��˰���ʾ�ί�äƤ���С������ե������
	 * ���Ϥ����������Ϥ�����դ��������Ԥ���
	 * </p>
	 */
	public void commit(TranslatorWriter myWriter){
		/** ����������¤�ã���Ƥʤ���ХХåե����ɲ� */
		if(buf.length() + tmpbuf.length() < MAX_BUF){
			buf.append(tmpbuf);
			tmpbuf = new StringBuffer(MAX_BUF / 10);
		}
		/** ����������¤�ã���Ƥ�г����ե�����������Хåե����ɲ� */
		else{
			makeOutput(myWriter);
			buf = new StringBuffer(MAX_BUF);
			commit(myWriter);
		}
		
	}
	
	
	/**
	 * �񤭽Ф�����������Ϥ���
	 * @param data �񤭽Ф���������
	 * 
	 * <p>
	 * commit�᥽�åɤ��ƤФ줿���˽��Ϥ���륯�饹�˵��Ҥ����
	 * ̿��������롣
	 * </p>
	 */
	public void write(String data){
		tmpbuf.append(data);
	}
	
	/**
	 * �����ե��������������
	 */
	private void makeOutput(TranslatorWriter myWriter){
		if(buf.length() == 0){ return; }
		
		try {
			serial++;
			myWriter.superWrite("			//"+ className + "_" + serial +"\n");
			myWriter.superWrite("			" + className + "_" + serial + ".exec(var, f);\n");
		} catch (IOException e) {
			// TODO ��ư�������줿 catch �֥�å�
			e.printStackTrace();
		}
		
		outputFile = new File(dir, className + "_" + serial + ".java");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile), MAX_BUF);
			writer.write("package " + packageName + ";\n");
			writer.write("import runtime.*;\n");
			writer.write("import java.util.*;\n");
			writer.write("import java.io.*;\n");
			writer.write("import daemon.IDConverter;\n");
			writer.write("import module.*;\n");
			if(Env.profile == Env.PROFILE_ALL){ writer.write("import util.Util;\n"); }
			writer.write("public class " + className + "_" + serial + " {\n");
			writer.write("	static public void exec(Object[] var, Functor[] f) {\n");
			writer.write("		Atom atom;\n");
			writer.write("		Functor func;\n");
			writer.write("		Link link;\n");
			writer.write("		AbstractMembrane mem;\n");
			writer.write("		int x, y;\n");
			writer.write("		double u, v;\n");
			writer.write("		int isground_ret;\n");
			writer.write("		boolean eqground_ret;\n");
			writer.write("		boolean guard_inline_ret;\n");
			writer.write("		ArrayList guard_inline_gvar2;\n");
			writer.write("		Iterator it_guard_inline;\n");
			writer.write("		Set insset;\n");
			writer.write("		Set delset;\n");
			writer.write("		Map srcmap;\n");
			writer.write("		Map delmap;\n");
			writer.write("		Atom orig;\n");
			writer.write("		Atom copy;\n");
			writer.write("		Link a;\n");
			writer.write("		Link b;\n");
			writer.write("		Iterator it_deleteconnectors;\n");
			
			writer.flush();
			// �ǡ����񤭽Ф�
			writer.write(buf.toString(), 0, buf.length());
			writer.flush();
			
			writer.write("	}\n");
			writer.write("}\n");
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			// TODO ��ư�������줿 catch �֥�å�
			e.printStackTrace();
		}
	}
	
	/**
	 * �Ǹ�ν�����
	 * �Хåե��˻ĤäƤ���ǡ�����񤭽Ф���
	 *
	 */
	public void close(TranslatorWriter myWriter){
		if(buf.length() <= 0){
			return;
		}
		makeOutput(myWriter);
		buf = new StringBuffer(MAX_BUF);
		tmpbuf = new StringBuffer(MAX_BUF / 10);
		
	}
}