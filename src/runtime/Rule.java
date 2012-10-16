package runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Rule
{
	/** ���ȥ��Ƴ�롼��Ŭ�Ѥ�̿�����atomMatchLabel.insts��
	 * ��Ƭ��̿���spec[2,*]�Ǥʤ���Фʤ�ʤ���*/
	public List<Instruction> atomMatch;
	/** ���Ƴ�롼��Ŭ�Ѥ�̿�����memMatchLabel.insts��
	 * ��Ƭ��̿���spec[1,*]�Ǥʤ���Фʤ�ʤ���*/
	public List<Instruction> memMatch;
	/** ����ѥ�����������Ƴ�롼��Ŭ�Ѥ�̿���� */
	public List<Instruction> tempMatch;

	/** ������̿�����guardLabel.insts�ˤޤ���null��
	 * ��Ƭ��̿���spec[*,*]�Ǥʤ���Фʤ�ʤ���*/
	public List<Instruction> guard;
	/** �ܥǥ�̿�����bodyLabel.insts�ˤޤ���null��
	 * ��Ƭ��̿���spec[*,*]�Ǥʤ���Фʤ�ʤ���*/
	public List<Instruction> body;

	/** ��٥��դ����ȥ��Ƴ�롼��Ŭ��̿���� */
	public InstructionList atomMatchLabel;
	/** ��٥��դ����Ƴ�롼��Ŭ��̿���� */
	public InstructionList memMatchLabel;	
	/** ��٥��դ�������̿����ޤ���null */
	public InstructionList guardLabel;
	/** ��٥��դ��ܥǥ�̿����ޤ���null */
	public InstructionList bodyLabel;
	/** ���Υ롼���ɽ����ʸ���� */
	public String text = "";
	/** ���Υ롼���ɽ����ʸ����ʾ�ά�ʤ��� */
	public String fullText = "";

	/** ����åɤ��ȤΥ٥���ޡ������ **/
	public Map<Thread, Benchmark> bench;

	/** �롼��̾ */
	public String name;

	/** ���ֹ� by inui */
	public int lineno;

	/** uniq�������Ĥ��ɤ��� */
	public boolean hasUniq = false;

	// todo ������4�ĤȤ�InstructionList���ݻ�����褦�ˤ���List���ѻߤ��롣

	/**
	 * �դĤ��Υ��󥹥ȥ饯����
	 */
	public Rule()
	{
		this("");
	}

	/**
	 * �롼��ʸ����Ĥ����󥹥ȥ饯��
	 * @param text �롼���ʸ����ɽ��
	 */
	public Rule(String text)
	{
		this(text, "");
	}

	/**
	 * �롼��ʸ����ʾ�ά�ʤ��ˤĤ����󥹥ȥ饯��
	 * @param text �롼���ʸ����ɽ��
	 * @param fullText �롼���ʸ����ɽ���ʾ�ά�ʤ���
	 */
	public Rule(String text, String fullText)
	{
		this.text = text;
		this.fullText = fullText;
		atomMatchLabel = new InstructionList();
		memMatchLabel = new InstructionList();
		atomMatch = atomMatchLabel.insts;
		memMatch = memMatchLabel.insts;
		bench = new HashMap<Thread, Benchmark>();
	}

	/**
	 * �ѡ����������Ѥ��륳�󥹥ȥ饯��
	 */
	public Rule(InstructionList atomMatchLabel, InstructionList memMatchLabel, InstructionList guardLabel, InstructionList bodyLabel)
	{
		this.atomMatchLabel = atomMatchLabel;
		this.memMatchLabel = memMatchLabel;
		this.guardLabel = guardLabel;
		this.bodyLabel = bodyLabel;
		atomMatch = atomMatchLabel.insts;
		memMatch = memMatchLabel.insts;
		if (guardLabel != null)
			guard = guardLabel.insts;
		if (bodyLabel != null)
			body = bodyLabel.insts;
	}

	/**
	 * ̿����ξܺ٤���Ϥ���
	 */
	public void showDetail()
	{
		if (Env.debug == 0 && !Env.compileonly) return;

		if (hasUniq && Env.slimcode)
		{
			Env.p("Compiled Uniq Rule " + this);
		}
		else
		{
			Env.p("Compiled Rule " + this);
		}

		Env.p("--atommatch:", 1);
		printInstructions(atomMatch);

		Env.p("--memmatch:", 1);
		printInstructions(memMatch);

		if (guard != null)
		{
			Env.p("--guard:" + guardLabel + ":", 1);
			printInstructions(guard);
		}

		if (body != null)
		{
			Env.p("--body:" + bodyLabel + ":", 1);
			printInstructions(body);
		}

		Env.p("");
	}

	private static void printInstructions(Iterable<Instruction> insts)
	{
		for (Instruction inst : insts)
		{
			Env.p(inst, 2);
		}
	}

	public String toString()
	{
		if (Env.compileonly) return "";
		return name != null && !name.equals("") ? name : text;
	}

	/**
	 * @return fullText �롼��Υ���ѥ����ǽ��ʸ����ɽ��
	 */
	public String getFullText()
	{
		return fullText;
	}

	///////////////////////////////////////////////////////////////////

	/* ���ȥ��ư�ƥ��Ȥλ�Բ�� */
	public long atomapply = 0;
	/* ���ȥ��ư�ƥ��Ȥ�������� */
	public long atomsucceed = 0;
	/* ���ȥ��ư�ƥ��Ȥι�׻��� */
	public long atomtime = 0;
	/* ���ư�ƥ��Ȥλ�Բ�� */
	public long memapply = 0;
	/* ���ư�ƥ��Ȥ�������� */
	public long memsucceed = 0;
	/* ���ư�ƥ��Ȥι�׻��� */
	public long memtime = 0;
	/* �롼��Ŭ�ѤΥХå��ȥ�å���� */
	public long backtracks = 0;
	/* �롼��Ŭ�ѻ������å����Ԥβ�� */
	public long lockfailure = 0;

	public void incAtomApply(Thread thread)
	{
		if (bench.containsKey(thread))
		{
			bench.get(thread).atomapply++;
		}
		else
		{
			Benchmark benchmark = new Benchmark(thread);
			benchmark.atomapply++;
			bench.put(thread, benchmark);
		}
	}

	public void incAtomSucceed(Thread thread)
	{
		if (bench.containsKey(thread))
		{
			bench.get(thread).atomsucceed++;
		}
		else
		{
			Benchmark benchmark = new Benchmark(thread);
			benchmark.atomsucceed++;
			bench.put(thread, benchmark);
		}
	}

	public void setAtomTime(long value, Thread thread)
	{
		if (bench.containsKey(thread))
		{
			bench.get(thread).atomtime += value;
		}
		else
		{
			Benchmark benchmark = new Benchmark(thread);
			benchmark.atomtime += value;
			bench.put(thread, benchmark);
		}
	}

	public void incMemApply(Thread thread)
	{
		if (bench.containsKey(thread))
		{
			bench.get(thread).memapply++;
		}
		else
		{
			Benchmark benchmark = new Benchmark(thread);
			benchmark.memapply++;
			bench.put(thread, benchmark);
		}
	}

	public void incMemSucceed(Thread thread)
	{
		if (bench.containsKey(thread))
		{
			bench.get(thread).memsucceed++;
		}
		else
		{
			Benchmark benchmark = new Benchmark(thread);
			benchmark.memsucceed++;
			bench.put(thread, benchmark);
		}
	}

	public void setMemTime(long value, Thread thread)
	{
		if (bench.containsKey(thread))
		{
			bench.get(thread).memtime += value;
		}
		else
		{
			Benchmark benchmark = new Benchmark(thread);
			benchmark.memtime += value;
			bench.put(thread, benchmark);
		}
	}

	public void setBackTracks(long value, Thread thread)
	{
		if (bench.containsKey(thread))
		{
			bench.get(thread).backtracks += value;
		}
		else
		{
			Benchmark benchmark = new Benchmark(thread);
			benchmark.backtracks += value;
			bench.put(thread, benchmark);
		}
	}

	public void setLockFailure(long value, Thread thread)
	{
		if (bench.containsKey(thread))
		{
			bench.get(thread).lockfailure += value;
		}
		else
		{
			Benchmark benchmark = new Benchmark(thread);
			benchmark.lockfailure += value;
			bench.put(thread, benchmark);
		}
	}

	public long allAtomApplys()
	{
		long apply = 0;
		for (Benchmark b : bench.values())
		{
			apply += b.atomapply;
		}
		return apply;
	}

	public long allMemApplys()
	{
		long apply = 0;
		for (Benchmark b : bench.values())
		{
			apply += b.memapply;
		}
		return apply;
	}

	public long allAtomSucceeds()
	{
		long succeed = 0;
		for (Benchmark b : bench.values())
		{
			succeed += b.atomsucceed;
		}
		return succeed;
	}

	public long allMemSucceeds()
	{
		long succeed = 0;
		for (Benchmark b : bench.values())
		{
			succeed += b.memsucceed;
		}
		return succeed;
	}

	public long allAtomTimes()
	{
		long time = 0;
		for (Benchmark b : bench.values())
		{
			time += b.atomtime;
		}
		return time;
	}

	public long allMemTimes()
	{
		long time = 0;
		for (Benchmark b : bench.values())
		{
			time += b.memtime;
		}
		return time;
	}

	public long allApplys()
	{
		return allAtomApplys() + allMemApplys();
	}

	public long allSucceeds()
	{
		return allAtomSucceeds() + allMemSucceeds();
	}

	public long allTimes()
	{
		return allAtomTimes() + allMemTimes();
	}

	public long allBackTracks()
	{
		long backtracks = 0;
		for (Benchmark b : bench.values())
		{
			backtracks += b.backtracks;
		}
		return backtracks;
	}

	public long allLockFailures()
	{
		long lockfailure = 0;
		for (Benchmark b : bench.values())
		{
			lockfailure += b.lockfailure;
		}
		return lockfailure;
	}
}

class Benchmark {

	/* ����åɤ�ID */
	public long threadid;
	/* ���ȥ��ư�ƥ��Ȥλ�Բ�� */
	public long atomapply = 0;
	/* ���ȥ��ư�ƥ��Ȥ�������� */
	public long atomsucceed = 0;
	/* ���ȥ��ư�ƥ��Ȥι�׻��� */
	public long atomtime = 0;
	/* ���ư�ƥ��Ȥλ�Բ�� */
	public long memapply = 0;
	/* ���ư�ƥ��Ȥ�������� */
	public long memsucceed = 0;
	/* ���ư�ƥ��Ȥι�׻��� */
	public long memtime = 0;
	/* �롼��Ŭ�ѤΥХå��ȥ�å���� */
	public long backtracks = 0;
	/* �롼��Ŭ�ѻ������å����Ԥβ�� */
	public long lockfailure = 0;

	Benchmark(Thread thread) {
		this.threadid = (Env.majorVersion == 1 && Env.minorVersion > 4) 
		? thread.getId() : thread.hashCode();
	}
}
