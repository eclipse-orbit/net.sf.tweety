/* Generated By:JJTree: Do not edit this line. ASTDefAtom.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package net.sf.tweety.lp.asp.parser;

public
class ASTDefAtom extends SimpleNode {
  boolean defNeg;
	public ASTDefAtom(int id) {
    super(id);
  }

  public ASTDefAtom(ASPParser p, int id) {
    super(p, id);
  }

  public void setDefNeg(boolean defNeg) {
	  this.defNeg = defNeg;
  }

  /** Accept the visitor. **/
  public Object jjtAccept(ASPParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=04d8af26ba71c8c2c50752c482a50682 (do not edit this line) */
