package compile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import runtime.Env;

import compile.Translator.TranslatorWriter;

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
public class Ejector
{
	///////////////////////////////////////////////////////////////////////////
	// ������

	/** �����ե�����ξ�¥����� */
	private static final int MAX_BUF = 51200;

	///////////////////////////////////////////////////////////////////////////

	private static int serial;

	/** �Ѵ������ե�����򤪤��ǥ��쥯�ȥ� */
	private File dir;
	/** �Ѵ������ե�����Υѥå�����̾ */
	private String packageName;
	private String className;
	private File outputFile;
	private StringBuilder buf = new StringBuilder(MAX_BUF);
	private StringBuilder tmpbuf = new StringBuilder(MAX_BUF);

	///////////////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��

	/**
	 * @param cn �������륯�饹̾
	 * @param d �ե��������������ǥ��쥯�ȥ�
	 * @param p �������륯�饹�Υѥå�����̾
	 */
	public Ejector(String cn, File d, String p)
	{
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
	public void commit(TranslatorWriter myWriter)
	{
		if (buf.length() + tmpbuf.length() < MAX_BUF)
		{
			// ����������¤�ã���Ƥʤ���ХХåե����ɲ�
			buf.append(tmpbuf);
			tmpbuf = new StringBuilder(MAX_BUF / 10);
		}
		else
		{
			// ����������¤�ã���Ƥ�г����ե�����������Хåե����ɲ�
			makeOutput(myWriter);
			buf = new StringBuilder(MAX_BUF);
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
	public void write(String data)
	{
		tmpbuf.append(data);
	}

	/**
	 * �����ե��������������
	 */
	private void makeOutput(TranslatorWriter myWriter)
	{
		if (buf.length() == 0)
		{
			return;
		}

		try
		{
			serial++;
			myWriter.superWrite("\t\t\t//"+ className + "_" + serial +"\n");
			myWriter.superWrite("\t\t\t" + className + "_" + serial + ".exec(var, f);\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		outputFile = new File(dir, className + "_" + serial + ".java");
		try
		{
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile), MAX_BUF));
			writer.println("package " + packageName + ";");
			writer.println("import runtime.*;");
			writer.println("import java.util.*;");
			writer.println("import java.io.*;");
//			writer.println("import daemon.IDConverter;\n");
			writer.println("import module.*;");
			if (Env.profile == Env.PROFILE_ALL)
			{
				writer.println("import util.Util;");
			}
			writer.println("@SuppressWarnings(\"unused\")");
			writer.println("public class " + className + "_" + serial + " {");
			writer.println("\tstatic public void exec(Object[] var, Functor[] f) {");
			writer.println("\t\tAtom atom;");
			writer.println("\t\tFunctor func;");
			writer.println("\t\tLink link;");
			writer.println("\t\tMembrane mem;");
			writer.println("\t\tint x, y;");
			writer.println("\t\tdouble u, v;");
			writer.println("\t\tint isground_ret;");
			writer.println("\t\tboolean eqground_ret;");
			writer.println("\t\tboolean guard_inline_ret;");
			writer.println("\t\tArrayList guard_inline_gvar2;");
			writer.println("\t\tIterator it_guard_inline;");
			writer.println("\t\tSet insset;");
			writer.println("\t\tSet delset;");
			writer.println("\t\tMap srcmap;");
			writer.println("\t\tMap delmap;");
			writer.println("\t\tAtom orig;");
			writer.println("\t\tAtom copy;");
			writer.println("\t\tLink a;");
			writer.println("\t\tLink b;");
			writer.println("\t\tIterator it_deleteconnectors;");

			writer.flush();
			// �ǡ����񤭽Ф�
			writer.write(buf.toString());
			writer.flush();

			writer.println("\t}");
			writer.println("}");
			writer.flush();
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * �Ǹ�ν�����
	 * �Хåե��˻ĤäƤ���ǡ�����񤭽Ф���
	 */
	public void close(TranslatorWriter myWriter)
	{
		if (buf.length() <= 0)
		{
			return;
		}
		makeOutput(myWriter);
		buf = new StringBuilder(MAX_BUF);
		tmpbuf = new StringBuilder(MAX_BUF / 10);
	}
}
