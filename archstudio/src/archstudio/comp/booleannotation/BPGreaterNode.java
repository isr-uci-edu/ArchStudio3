package archstudio.comp.booleannotation;

/* Generated By:JJTree: Do not edit this line. BPGreaterNode.java */

import edu.uci.ics.xarchutils.*;

/**
 * Relational greater-than expression.
 *
 * @author Rob Egelink (egelink@ics.uci.edu)
 */
public class BPGreaterNode extends SimpleNode
{

    public BPGreaterNode( int id )
    {
        super( id );
    }

    public BPGreaterNode( Boolean p, int id )
    {
        super( p, id );
    }

    /**
     * Returns the greater-than expression stored in an object of type
     * BooleanExp.
     * @param context A boolguard context.
     * @param xarch An XArchADT proxy.
     * @return greater-than expression
     */
    public ObjRef toXArch( ObjRef context, XArchFlatInterface xarch )
    {
        ObjRef greater = xarch.create( context, "GreaterThan" );

        // The left operand can only be a symbol (i.e. variable)
        ObjRef left = xarch.create( context, "Symbol" );
        xarch.set( left, "Value", leftOp );
        xarch.set( greater, "Symbol", left );

        // The right operand can be a symbol or a value
        if ( BPUtilities.isValue( rightOp ) )
        {
            ObjRef right = xarch.create( context, "Value" );
            xarch.set( right, "Value", rightOp );
            xarch.set( greater, "Value", right );
        }
        else
        {
            ObjRef right = xarch.create( context, "Symbol" );
            xarch.set( right, "Value", rightOp );
            xarch.set( greater, "Symbol2", right );
        }

        ObjRef boolexp = xarch.create( context, "BooleanExp" );
        xarch.set( boolexp, "GreaterThan", greater );

        return boolexp;
    }

    /**
     * Returns a human readable representation of the expression stored
     * in a greater-than node.
     * @return greater-than expression.
     */
    public String toString()
    {
        return new String( leftOp + " > " + rightOp );
    }

    protected String leftOp;
    protected String rightOp;

}
