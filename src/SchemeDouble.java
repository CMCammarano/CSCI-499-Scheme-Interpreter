public class SchemeDouble extends SchemeNumber
{
    double d;

    public double doubleValue()
    {
	return d;
    }

    public SchemeDouble(double dIn)
    {
	d = dIn;
    }

    public boolean equals(Object o)
    {
	if (o instanceof SchemeNumber)
	    return d == ((SchemeNumber)o).doubleValue();
	else
	    return false;
    }

    public String toString()
    {
	return Double.toString(d);
    }

    public SchemeNumber add(SchemeNumber n)
    {
	return new SchemeDouble(d + n.doubleValue());
    }

    public SchemeNumber multiply(SchemeNumber n)
    {
	return new SchemeDouble(d * n.doubleValue());
    }

    public SchemeNumber subtract(SchemeNumber n)
    {
	return add(n.negate());
    }

    public SchemeNumber negate()
    {
	return new SchemeDouble(-d);
    }

    public SchemeNumber divide(SchemeNumber n)
    {
	return new SchemeDouble(d / n.doubleValue());
    }

    public int compareTo(SchemeNumber n)
    {
	return (new Double(d)).compareTo(new Double(n.doubleValue()));
    }

}



