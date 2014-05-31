package archstudio.comp.booleannotation;

/* Generated By:JJTree: Do not edit this line. BPLessEqualNode.java */

import edu.uci.ics.xarchutils.*;

/**
 * Relational less-or-equal expression.
 *
 * @author Rob Egelink (egelink@ics.uci.edu)
 */
public class BPLessEqualNode extends SimpleNode
{

    public BPLessEqualNode( int id )
    {
        super( id );
    }

    public BPLessEqualNode( Boolean p, int id )
    {
        super( p, id );
    }

    /**
     * Returns the less-or-equal expression stored in an object of type
     * BooleanExp.
     * @param context A boolguard context.
     * @param xarch An XArchADT proxy.
     * @return less-or-equal expression
     */
    public ObjRef toXArch( ObjRef context, XArchFlatInterface xarch )
    {
        ObjRef lessequal = xarch.create( context, "LessThanOrEquals" );

        // The left operand can only be a symbol (i.e. variable)
        ObjRef left = xarch.create( context, "Symbol" );
        xarch.set( left, "Value", leftOp );
        xarch.set( lessequal, "Symbol", left );

        // The right operand can be a symbol or a value
        if ( BPUtilities.isValue( rightOp ) )
        {
            ObjRef right = xarch.create( context, "Value" );
            xarch.set( right, "Value", rightOp );
            xarch.set( lessequal, "Value", right );
        }
        else
        {
            ObjRef right = xarch.create( context, "Symbol" );
            xarch.set( right, "Value", rightOp );
            xarch.set( lessequal, "Symbol2", right );
        }

        ObjRef boolexp = xarch.create( context, "BooleanExp" );
        xarch.set( boolexp, "LessThanOrEquals", lessequal );

        return boolexp;
    }

    /**
     * Returns a human readable representation of the expression stored
     * in a less-or-equal node.
     * @return less-or-equal expression.
     */
    public String toString()
    {
        return new String( leftOp + " <= " + rightOp );
    }

    protected String leftOp;
    protected String rightOp;

}
