import java.util.Hashtable;

public class SchemeSymbol extends SchemeObject
{
    static Hashtable<String,SchemeSymbol> symbolTable = new Hashtable<String,SchemeSymbol>();
    
    String value;

    private SchemeSymbol(String str)
    {
	value = str;
    }
	
    public static SchemeSymbol getSymbol(String str)
    {
	// if in table, return it.
	if (symbolTable.containsKey(str))
	    return symbolTable.get(str);

	// if not add it and return it.
	SchemeSymbol sym = new SchemeSymbol(str);
	symbolTable.put(str,sym);
	return sym;
    }

    public String toString() { return value; }
}





