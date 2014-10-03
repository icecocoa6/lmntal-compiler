package compile.parser;

/**
 * ソースファイル中のリンク表現
 */
class SrcHyperLink extends SrcLink
{
    protected SrcName attr;

	/**
	 * 指定された名前のリンクを作成します
	 * @param name リンク名
	 */
    public SrcHyperLink(String name, SrcName attr)
	{
	    this(name, attr, -1);
	}

	/**
	 * 指定された名前と行番号のリンクを作成します
	 * @param name リンク名
	 * @param lineno 行番号
	 */
    public SrcHyperLink(String name, SrcName attr, int lineno)
	{
	        super(name, lineno);
		this.attr = attr;
	}

	public String getQualifiedName()
	{
		return " " + name;
	}
}
