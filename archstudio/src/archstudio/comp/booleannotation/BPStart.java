package archstudio.comp.booleannotation;
/* Generated By:JJTree: Do not edit this line. BPStart.java */

import edu.uci.ics.xarchutils.*;

public class BPStart extends SimpleNode implements IBooleanGuard
{

    public BPStart( int id )
    {
        super( id );
    }

    public BPStart( Boolean p, int id )
    {
        super( p, id );
    }

    public ObjRef toXArch( ObjRef context , XArchFlatInterface xarch )
    {
        ObjRef boolguard = xarch.create( context, "BooleanGuard" );
	ObjRef child = ( (SimpleNode)jjtGetChild( 0 ) ).toXArch( context, xarch );
        xarch.set( boolguard, "BooleanExp", child );

        return boolguard;
    }

    public String toString()
    {
        return jjtGetChild( 0 ).toString();
    }

}
