public class SchemeString extends SchemeObject
{
    String value;

    public SchemeString(String s)
    {
	value = s;
    }
    
    public String toString()
    {
	return value;
    }
}
