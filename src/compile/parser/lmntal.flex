/**
	This is LMNtal Lex file.
*/

/* --------------------------Usercode Section------------------------ */
package compile.parser;
import java_cup.runtime.Symbol;

%%

/* -----------------Options and Declarations Section----------------- */
%class Lexer

%{
	private final boolean _DEBUG = true;
	StringBuffer string = new StringBuffer();

    /* To create a new java_cup.runtime.Symbol with information about
       the current token, the token will have no value in this
       case. */
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    
    /* Also creates a new java_cup.runtime.Symbol with information
       about the current token, but this object has a value. */
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

%line
%column
%unicode
%cup

LineTerminator = \r|\n|\r\n
InputCharacter = ([^\r\n]|Character)
WhiteSpace     = {LineTerminator} | [ \t\f]

LinkName       = [A-Z_][A-Za-z_0-9]*

////////////////////////////////////////////////////////////////
//
// ���ȥ�̾�ط�

//AtomName = [a-z0-9][A-Za-z_0-9]*
AtomName = [a-ln-z0-9][A-Za-z_0-9]* | m([A-Za-np-z_0-9]([A-Za-ce-z_0-9][A-Za-z_0-9]*)?)?

// AtomName�˲ä���0�����ǥ��ȥ�̾�Ȥʤ�ʸ���󤽤Σ���AtomName����¾Ū�Ǥʤ���Фʤ�ʤ���
NumberName = [0-9]*\.[0-9]+ | [0-9]*\.?[0-9]+ [Ee][+-]?[0-9]+

// AtomName�˲ä���0�����ǥ��ȥ�̾�Ȥʤ�ʸ���󤽤Σ�������
// ��:�ι�ʸ���Ϥ����ꤹ��ޤǤδ֡�:�⤳���Ǽ����ࡣ:��ľ����.���ִ�������
// ����ա˥⥸�塼��̾����Ƭʸ���� a-z �˸��ꤷ�Ƥ���ޤ�
PathedAtomName = [a-z][A-Za-z_0-9]* [\.:] [a-z0-9][A-Za-z_0-9]*

// ��
SymbolName = "'" [^'\r\n]+ "'" | "'" [^'\r\n]* ("''" [^'\r\n]*)+ "'"
// ��
String = "\"" ("\\\"" | [^\"\r\n]* | "\\"[\r]?"\n" )* "\""

// Inline = "[[" [^*] ~"]]"

%state QUOTED

////////////////////////////////////////////////////////////////

RelativeOp = "=" | "==" | "!=" | "::" | "?" | {IntegerRelativeOp} | {FloatingRelativeOp}
IntegerRelativeOp  = "<" | ">" | ">=" | "=<" | "=:=" | "=\\="
FloatingRelativeOp = "<."| ">."| ">=."| "=<."| "=:=."| "=\\=."

Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment = "/*" [^*] ~"*/"
EndOfLineComment = ("//"|"%") {InputCharacter}* {LineTerminator}?

%% 

/* ------------------------Lexical Rules Section---------------------- */

<YYINITIAL> {
	","					{ return symbol(sym.COMMA); }
	"("					{ return symbol(sym.LPAREN); }
	")"					{ return symbol(sym.RPAREN); }
	"{"					{ return symbol(sym.LBRACE); }
	"}"					{ return symbol(sym.RBRACE); }
	":"					{ return symbol(sym.COLON); }
	":-"				{ return symbol(sym.RULE); }
	"."					{ return symbol(sym.PERIOD); }
	"|"					{ return symbol(sym.GUARD); }
	{RelativeOp}		{ return symbol(sym.RELOP, yytext()); }
	"*."				{ return symbol(sym.ASTERISK_DOT); }
	"/."				{ return symbol(sym.SLASH_DOT); }
	"-."				{ return symbol(sym.MINUS_DOT); }
	"+."				{ return symbol(sym.PLUS_DOT); }
	"^"					{ return symbol(sym.HAT); }
	"~"					{ return symbol(sym.TILDE); }
	"**"				{ return symbol(sym.ASTERISK_ASTERISK); }
	"$"					{ return symbol(sym.PROCVAR); }
	"@"					{ return symbol(sym.RULEVAR); }
	"*"					{ return symbol(sym.ASTERISK); }
	"/"					{ return symbol(sym.SLASH); }
	"+"					{ return symbol(sym.PLUS); }
	"-"					{ return symbol(sym.MINUS); }
	"["					{ return symbol(sym.LBRACKET); }
	"]"					{ return symbol(sym.RBRACKET); }
	"mod" 				{ return symbol(sym.MOD); }
	"[[" 				{ string.setLength(0); yybegin(QUOTED); }
	"\\+"				{ return symbol(sym.NEGATIVE); }
	{LinkName}			{ return symbol(sym.LINK_NAME, yytext()); }
	{NumberName}		{ return symbol(sym.NUMBER_NAME, yytext()); }
	{SymbolName}		{ return symbol(sym.SYMBOL_NAME, yytext()); }
	{String}			{ return symbol(sym.STRING, yytext()); }
	{PathedAtomName}	{ return symbol(sym.PATHED_ATOM_NAME, yytext()); }
	{AtomName}			{ return symbol(sym.ATOM_NAME, yytext()); }
	{WhiteSpace}		{ /* just skip */ }
	{Comment}			{ /* just skip */ }
}

<QUOTED> {
	"]]"                { yybegin(YYINITIAL); return symbol(sym.QUOTED_STRING, string.toString()); }
	.|{LineTerminator}  { string.append( yytext() ); }
}


/* No token was found for the input so through an error.  Print out an
   Illegal character message with the illegal character that was found. */
[^]                    { throw new Error("Illegal character <"+yytext()+"> at line:"+yyline); }
