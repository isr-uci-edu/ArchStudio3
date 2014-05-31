package archstudio.comp.booleannotation;


import edu.uci.ics.xarchutils.*;

public class BPUtilities
{
    public static boolean isValue( String s )
    {
        char c = s.charAt( 0 );

        return ( c == '1' || c == '2' || c == '3' || c == '4' || c == '5' ||
                 c == '6' || c == '7' || c == '8' || c == '9' || c == '0' ||
                 c == '#' || c == '"' );
    }

		/*
    public static final XArchFlatInterface xarch = new XArchFlatImpl();

    private static final ObjRef doc = xarch.createXArch( "urn:XXX" );

    public static final ObjRef booleanContext = xarch.createContext( doc, "boolguard" );
    public static final ObjRef optionsContext = xarch.createContext( doc, "options" );
    public static final ObjRef typesContext = xarch.createContext( doc, "types" );
    */
}

