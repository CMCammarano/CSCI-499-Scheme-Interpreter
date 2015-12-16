/**
 * This class implements Scheme boolean objects.  
 * This class can have only two instances: #t and #f.
 **/
public class SchemeBoolean extends SchemeObject
{
    public static final SchemeBoolean TRUE = new SchemeBoolean(true);
    public static final SchemeBoolean FALSE = new SchemeBoolean(false);

    boolean value;


    private SchemeBoolean(boolean val)
    {
	value = val;
    }

    /**
     * Get the #t object.
     **/
    public static SchemeBoolean getTrue()
    {
	return TRUE;
    }

    /**
     * Get the #f object.
     **/
    public static SchemeBoolean getFalse()
    {
	return FALSE;
    }

    /**
     * Get the #t or #f object.
     **/
    public static SchemeBoolean getBoolean(boolean b)
    {
    	if (b)
           return TRUE;
	else
	   return FALSE;
    }

    /**
     * get the value of this boolean object.
     **/
    public boolean getValue()
    {
	return value;
    }

    /**
     * Convert the boolean object to a printable string.
     **/
    public String toString()
    {
	return value?"#t":"#f";
    }
}




