public abstract class SchemeNumber extends SchemeObject implements Comparable<SchemeNumber>
{
    public abstract SchemeNumber add(SchemeNumber n);
    public abstract SchemeNumber subtract(SchemeNumber n);
    public abstract SchemeNumber multiply(SchemeNumber n);
    public abstract SchemeNumber negate();
    public abstract SchemeNumber divide(SchemeNumber n);
    public abstract double doubleValue();
    public abstract int compareTo(SchemeNumber n);    
}
