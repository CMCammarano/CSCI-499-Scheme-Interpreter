import java.math.BigInteger;

public class SchemeInteger extends SchemeNumber
{
    BigInteger i;

    public SchemeInteger(BigInteger bigInt)
    {
	i = bigInt;
    }

    public SchemeInteger(int i)
    {
	this(BigInteger.valueOf(i));
    }

    public boolean equals(Object o)
    {
	if (o instanceof SchemeInteger)
	    return i.equals(((SchemeInteger)o).i);
	else
	    return false;
    }

    public String toString() { return i.toString(); }

    public SchemeNumber add(SchemeNumber n)
    {
	if (n instanceof SchemeInteger)
	    return new SchemeInteger(i.add(((SchemeInteger)n).i));
	else 
	    return n.add(this);
    }

    public SchemeNumber multiply(SchemeNumber n)
    {
	if (n instanceof SchemeInteger)
	    return new SchemeInteger(i.multiply(((SchemeInteger)n).i));
	else 
	    return n.multiply(this);
    }

    public SchemeNumber subtract(SchemeNumber n)
    {
	return add(n.negate());
    }

    public SchemeNumber negate()
    {
	return new SchemeInteger(i.negate());
    }

    public SchemeNumber divide(SchemeNumber n)
    {
	double d = i.doubleValue();

	return new SchemeDouble(d / n.doubleValue());
    }

    public double doubleValue()
    {
	return i.doubleValue();
    }

    public int compareTo(SchemeNumber n)
    {
	if (n instanceof SchemeInteger)
	    return i.compareTo(((SchemeInteger)n).i);
	else
	    return (- n.compareTo(this));
    }
}




