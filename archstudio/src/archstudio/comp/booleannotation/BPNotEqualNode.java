package archstudio.comp.booleannotation;

/* Generated By:JJTree: Do not edit this line. BPNotEqualNode.java */

import edu.uci.ics.xarchutils.*;

/**
 * Relational not-equal expression.
 *
 * @author Rob Egelink (egelink@ics.uci.edu)
 */
public class BPNotEqualNode extends SimpleNode
{

    public BPNotEqualNode( int id )
    {
        super( id );
    }

    public BPNotEqualNode( Boolean p, int id )
    {
        super( p, id );
    }

    /**
     * Returns the not-equal expression stored in an object of type
     * BooleanExp.
     * @param context A boolguard context.
     * @param xarch An XArchADT proxy.
     * @return not-equal expression
     */
    public ObjRef toXArch( ObjRef context, XArchFlatInterface xarch )
    {
        ObjRef notequals = xarch.create( context, "NotEquals" );

        // The left operand can only be a symbol (i.e. variable)
        ObjRef left = xarch.create( context, "Symbol" );
        xarch.set( left, "Value", leftOp );
        xarch.set( notequals, "Symbol", left );

        // The right operand can be a symbol or a value
        if ( BPUtilities.isValue( rightOp ) )
        {
            ObjRef right = xarch.create( context, "Value" );
            xarch.set( right, "Value", rightOp );
            xarch.set( notequals, "Value", right );
        }
        else
        {
            ObjRef right = xarch.create( context, "Symbol" );
            xarch.set( right, "Value", rightOp );
            xarch.set( notequals, "Symbol2", right );
        }

        ObjRef boolexp = xarch.create( context, "BooleanExp" );
        xarch.set( boolexp, "NotEquals", notequals );

        return boolexp;
    }

    /**
     * Returns a human readable representation of the expression stored
     * in a not-equal node.
     * @return not-equal expression.
     */
    public String toString()
    {
        return new String( leftOp + " != " + rightOp );
    }

    protected String leftOp;
    protected String rightOp;

}