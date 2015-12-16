public class SchemePair extends SchemeList
{
    SchemeObject car = null;
    SchemeObject cdr = null;
    
    public SchemePair(SchemeObject theCar, SchemeObject theCdr)
    {
	car = theCar;
	cdr = theCdr;
    }

    public SchemePair()
    {
	this(null,null);
    }

    public SchemeObject getCar() { return car; }
    public SchemeObject getCdr() { return cdr; }
    public SchemePair getNext() {return (SchemePair)cdr;} 
    public void setCar(SchemeObject newCar) { car = newCar; }
    public void setCdr(SchemeObject newCdr) { cdr = newCdr; }

    public String toString()
    {
	return "(" + inListToString();
    }

    public String inListToString()
    {
	String rest;
	if (cdr instanceof SchemePair)
	    rest = " " + ((SchemePair)cdr).inListToString();
	else if (cdr instanceof SchemeNull)
	    rest = ")";
	else
	    rest = " . " + cdr.toString() + ")";
	return car.toString() + rest;
    }
}







