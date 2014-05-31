package archstudio.comp.booleannotation;


import edu.uci.ics.xarchutils.*;

public interface IBooleanGuard
{
    public ObjRef toXArch( ObjRef context, XArchFlatInterface xarch );

    public String toString();
}
