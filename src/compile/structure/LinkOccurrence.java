package compile.structure;

/** 
 * ソースコード中のリンクまたはリンク束の各出現を表すクラス。<br>
 * runtime.Link と違って、LinkOccurrence.atom はこちら側のアトムオブジェクトが入っている。
 */
public final class LinkOccurrence
{
	/**
	 * リンク名
	 */
	public String name;

	/**
	 * 所属するアトムオブジェクト（こちら側）
	 */
	public Atomic atom;

	/**
	 * （こちら側の）アトムでのリンク位置
	 */
	public int pos;

	/** 2回しか出現しない場合に、もう片方の出現を保持する */
	public LinkOccurrence buddy = null;

	/**
	 * リンク出現を生成する。
	 * @param name リンク名
	 * @param atom 所属するアトム
	 * @param pos 所属するアトムでの場所
	 */
	public LinkOccurrence(String name, Atomic atom, int pos)
	{
		this.name = name;
		this.atom = atom;
		this.pos = pos;
	}

	public String toString()
	{
		return name.replaceAll("~", "_");
	}

	public boolean equals(Object o)
	{
		return o == this
			|| o instanceof LinkOccurrence && equals((LinkOccurrence)o);
	}

	public boolean equals(LinkOccurrence l)
	{
		return atom == l.atom && pos == l.pos;
	}

	public int hashCode()
	{
		return atom.hashCode() ^ (17 * pos);
	}
}
