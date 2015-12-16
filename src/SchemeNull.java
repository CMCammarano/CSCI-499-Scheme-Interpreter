/**
 * Thus is the null SchemeObject.  Only one instance exists.
 **/
public class SchemeNull extends SchemeList
{
    public static final SchemeNull NULL = new SchemeNull();
    
    private SchemeNull() {}

    public static SchemeNull getNull()
    {
	return NULL;
    }

    public String toString() 
    {
	return "()";
    }    
}
