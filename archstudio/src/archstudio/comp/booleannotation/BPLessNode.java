package archstudio.comp.booleannotation;

/* Generated By:JJTree: Do not edit this line. BPLessNode.java */

import edu.uci.ics.xarchutils.*;

/**
 * Relational less-than expression.
 *
 * @author Rob Egelink (egelink@ics.uci.edu)
 */
public class BPLessNode extends SimpleNode
{

    public BPLessNode( int id )
    {
        super( id );
    }

    public BPLessNode( Boolean p, int id )
    {
        super( p, id );
    }

    /**
     * Returns the less-than expression stored in an object of type
     * BooleanExp.
     * @param context A boolguard context.
     * @param xarch An XArchADT proxy.
     * @return less-than expression
     */
    public ObjRef toXArch( ObjRef context, XArchFlatInterface xarch )
    {
        ObjRef lessthan = xarch.create( context, "LessThan" );

        // The left operand can only be a symbol (i.e. variable)
        ObjRef left = xarch.create( context, "Symbol" );
        xarch.set( left, "Value", leftOp );
        xarch.set( lessthan, "Symbol", left );

        // The right operand can be a symbol or a value
        if ( BPUtilities.isValue( rightOp ) )
        {
            ObjRef right = xarch.create( context, "Value" );
            xarch.set( right, "Value", rightOp );
            xarch.set( lessthan, "Value", right );
        }
        else
        {
            ObjRef right = xarch.create( context, "Symbol" );
            xarch.set( right, "Value", rightOp );
            xarch.set( lessthan, "Symbol2", right );
        }

        ObjRef boolexp = xarch.create( context, "BooleanExp" );
        xarch.set( boolexp, "LessThan", lessthan );

        return boolexp;
    }

    /**
     * Returns a human readable representation of the expression stored
     * in a less-than node.
     * @return less-than expression.
     */
    public String toString()
    {
        return new String( leftOp + " < " + rightOp );
    }

    protected String rightOp;
    protected String leftOp;
}
