import java.io.*;
import java.math.BigInteger;

public abstract class SchemeObject
{

    /**
     * Dealing Java's statically-typed variables is not convenient 
     * when manipulating SchemeObjects, because Scheme is dynamically
     * typed.  To make manipulating objects and lists more convenient,
     * all SchemeObjects provide a getCar() and getCdr() function.
     * Objects which aren't SchemePairs should throw a ClassCastException.
     * The following are equivalent:
     * 
     * SchemeObject o = ...;
     * System.out.println(o.getCar());
     * 
     * and 
     * 
     * SchemeObject o = ...;
     * Scheme.out.println(((SchemePair)o).getCar());
     *
     * Returns the car of a SchemePair.
     * Throws ClassCastException if the object does not override this function.
     **/
    public SchemeObject getCar() { throw new ClassCastException("Took car of " + this); }

    /**
     * The following are equivalent:
     * 
     * SchemeObject o = ...;
     * System.out.println(o.getCdr());
     * 
     * and 
     * 
     * SchemeObject o = ...;
     * Scheme.out.println(((SchemePair)o).getCdr());
     * 
     * Returns the cdr of a SchemePair.
     * Throws ClassCastException if the object does not override this function.
     **/
    public SchemeObject getCdr()
    {
	throw new RuntimeException("Took cdr of " + this);
    }
  
    public SchemeObject car() { return getCar(); }
    public SchemeObject cdr() { return getCdr(); }
    public SchemeObject cadr() { return getCdr().getCar(); }
    public SchemeObject caddr() { return getCdr().getCdr().getCar(); }
    public SchemeObject cddr() { return getCdr().getCdr(); }
    public SchemeObject cadddr() { return getCdr().getCdr().getCdr().getCar(); }
    public SchemeObject cdar() { return getCar().getCdr(); }

    /**
     * This reads in a char from input and throws an IOException on EOF.
     **/
    private static char readChar(InputStream in) throws IOException
    {
      int ch = in.read();
      if (ch < 0)
	throw new EOFException();
      
      return ((char) ch);
    }

    /**
     * This reads in a SchemeObject from a String.
     */
    public static SchemeObject read(String s) throws IOException,EndOfSchemeListException
    {
	return read(new PushbackInputStream(new StringInputStream(s)));
    }

    /**
     * This reads in a SchemeObject from an InputStream.  Subsequent calls
     * to read with this InputStream are only guaranteed to work correctly if 
     * the InputStream is actually a PushbackInputStream.  To get a 
     * Use "new PushbackInputStream(System.in)" to get a PushbackInputStream from
     * an InputStream.
     *
     * Throws IOException if an there is an error reading from the stream.  
     *
     * Throws EndOfSchemeListException if there is an unexpected end
     * of a list expression.  For example: ")"
     **/
    public static SchemeObject read(InputStream inputStream) throws IOException, EndOfSchemeListException
    {
	char ch;

	// The read() methods below need a PushbackInputStream because
	// they need to put characters already read back into the
	// InputStream.
	PushbackInputStream in;
	if (inputStream instanceof PushbackInputStream)
	    in = (PushbackInputStream)inputStream;
	else
	    in = new PushbackInputStream(inputStream,1);

	// skip whitespace.
	for(ch = readChar(in); Character.isWhitespace(ch); ch = readChar(in))
	    ;

        if (ch == '(')                  // beginning of a list
	    return readList(in);
	if (ch == '"')                  // beginning of a string.
	    return readString(in);
	if (ch == '\'')                 // quoted expression.
	    {
		SchemeObject o = read(in);
		return new SchemePair(SchemeSymbol.getSymbol("quote"),new SchemePair(o,SchemeNull.getNull()));
	    }
	if (ch == '#')                  // beginning of boolean or character
	    {                           // (characters are not supported)
		String s = readSymbol(in);
		if ("t".equalsIgnoreCase(s))
		    return SchemeBoolean.getTrue();
		if ("f".equalsIgnoreCase(s))
		    return SchemeBoolean.getFalse();
		// if (s.charAt(0) == '\\')
		//    return SchemeChar.getChar(s.charAt(1));
		return SchemeSymbol.getSymbol("#" + s);
	    }
	if (ch == ')')                   // unexpected ')', error!
	    throw new EndOfSchemeListException();
	if (ch == ';')                   // comment, ignore rest of line.
	{
	    // skip line
	    for (ch = (char)readChar(in); ch != '\n'; ch = (char)readChar(in))
		;
	    return read(in);
	}
	else                             // some sort of symbol or number.
	    {
		in.unread(ch);           // pushback the character read.
		String s = readSymbol(in);
		
		/*
		 * parse symbol as symbol or number.  */
		if (s != ".")
		    {
			try
			    {
				return new SchemeInteger(new BigInteger(s));
			    }
			catch (NumberFormatException e)
			    {}

			try
			    {
				return new SchemeDouble(Double.parseDouble(s));
			    }
			catch (NumberFormatException e)
			    {}
		    }
		
		return SchemeSymbol.getSymbol(s);
	    }
    }


    /**
     * This reads in a list expression assuming that the initial (
     * has already been read.  It returns a SchemeList that is either a
     * SchemePair or SchemeNull.
     * 
     * Throws IOException if an there is an error reading from the
     * stream.  
     *
     * Throws EndOfSchemeListException if there is an unexpected end
     * of a list expression.  For example: "1 2 ')".
     **/     
private static SchemeList readList(PushbackInputStream in) throws IOException, EndOfSchemeListException 
    { 
	try 
	    {
		
		SchemeObject o = read(in);

		SchemeObject p = readList(in);
		
		if (p != SchemeNull.getNull() && SchemeSymbol.getSymbol(".") == p.getCar())
		    p = p.cadr();

		return new SchemePair(o,p);
	    }
	catch(EndOfSchemeListException e)
	    {
		return SchemeNull.getNull();
	    }
    
    }

    /**
     * This reads in a string expression assuming that the initial "
     * has already been read.  It returns a SchemeString.
     *
     * Throws IOException if an there is an error reading from the
     * stream.  
     **/     
    private static SchemeString readString(PushbackInputStream in) throws IOException
    {
	StringBuffer sb = new StringBuffer();
	
	for (char ch = readChar(in); ch != '"'; ch = readChar(in))
	    {
		if (ch == '\\')
		    {
			ch = readChar(in);
			switch (ch)
			    {
			    case 'n': 
				ch = '\n';
				break;
			    case 't':
				ch = '\t';
				break;
			    case 'r':
				ch = '\r';
				break;
			    }
			sb.append(ch);
		    }
		else
		    sb.append(ch);
	    }
	return new SchemeString(sb.toString());
    }

    /**
     * This reads in a symbol expression and returns it as a String.
     *
     * This may be a number.  If so, it should be converted to a SchemeNumber.
     *
     * Throws IOException if there is an error reading from the stream.  
     **/     
    private static String readSymbol(PushbackInputStream in) throws IOException
    {
	StringBuffer sb = new StringBuffer();

	for(char ch = readChar(in); !Character.isWhitespace(ch); ch = readChar(in))
	    {
		if (ch == ')' || ch == '(')
		{
		    in.unread(ch);
		    break;
		}
		
		sb.append(ch);
	    }

	return sb.toString();
    }
}








