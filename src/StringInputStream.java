import java.io.*;
public class StringInputStream extends InputStream
{
   private byte[] data;
   private int length;
   private int cursor;

   public StringInputStream(String in)
   { 
      data = in.getBytes();
      length = in.length();
      cursor=0;
   }

   public int read()
   {
      if (cursor >= length)
        return -1;

      return (int)data[cursor++];
   }
}
