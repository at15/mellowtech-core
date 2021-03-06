/**
 * 
 */
package org.mellowtech.core.bytestorable;

import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author msvens
 *
 */
public class UtfUtil {

  public static final int utfLength(final String str) {
    int len = str.length();
    int c;
    int utflen = 0;
    for (int i = 0; i < len; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        utflen++;
      } else if (c > 0x07FF) {
        utflen += 3;
      } else {
        utflen += 2;
      }
    }
    return utflen;
  }
  
  public static final int utfLength(final char[] str){
    return utfLength(str, 0, str.length);
  }

  public static final int utfLength(final char[] str, int offset, int length) {
    int c;
    int utflen = 0;
    for (int i = 0; i < length; i++) {
      c = str[offset++];
      if ((c >= 0x0001) && (c <= 0x007F)) {
        utflen++;
      } else if (c > 0x07FF) {
        utflen += 3;
      } else {
        utflen += 2;
      }
    }
    return utflen;
  }

  public static final byte[] encode(final String str) {
    byte b[] = new byte[utfLength(str)];
    encode(str, b, 0);
    return b;
  }
  
  public static final byte[] encode(final char[] str) {
    byte b[] = new byte[utfLength(str)];
    encode(str, b, 0);
    return b;
  }

  public static final int encode(final String str, final byte[] b,
      final int offset) {
    int len = str.length();
    int c, count = offset;

    int i = 0;
    for (i = 0; i < len; i++) {
      c = str.charAt(i);
      if (!((c >= 0x0001) && (c <= 0x007F)))
        break;
      b[count++] = (byte) c;
    }

    // difficult case:
    for (; i < len; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        b[count++] = (byte) c;

      } else if (c > 0x07FF) {
        b[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
        b[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
        b[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      } else {
        b[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
        b[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      }
    }
    return count - offset;
  }
  
  public static final int encode(final char[] str, final byte[] b,
      final int offset) {
    int len = str.length;
    int c, count = offset;

    int i = 0;
    for (i = 0; i < len; i++) {
      c = str[i];
      if (!((c >= 0x0001) && (c <= 0x007F)))
        break;
      b[count++] = (byte) c;
    }

    // difficult case:
    for (; i < len; i++) {
      c = str[i];
      if ((c >= 0x0001) && (c <= 0x007F)) {
        b[count++] = (byte) c;

      } else if (c > 0x07FF) {
        b[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
        b[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
        b[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      } else {
        b[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
        b[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      }
    }
    return count - offset;
  }
  
  public static final void encode(final String str, final ByteBuffer b) {
    /*if(b.hasArray()){
      b.position(b.position()+encode(str, b.array(), b.position()));
      return;
    }*/
    int len = str.length();
    int c;

    int i = 0;
    for (i = 0; i < len; i++) {
      c = str.charAt(i);
      if (!((c >= 0x0001) && (c <= 0x007F)))
        break;
      b.put((byte) c);
    }

    // difficult case:
    for (; i < len; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        b.put((byte) c);

      } else if (c > 0x07FF) {
        b.put((byte) (0xE0 | ((c >> 12) & 0x0F)));
        b.put((byte) (0x80 | ((c >> 6) & 0x3F)));
        b.put((byte) (0x80 | ((c >> 0) & 0x3F)));
      } else {
        b.put((byte) (0xC0 | ((c >> 6) & 0x1F)));
        b.put((byte) (0x80 | ((c >> 0) & 0x3F)));
      }
    }
  }
  
  public static final void encode(final char[] str, final ByteBuffer b) {
    /*if(b.hasArray()){
      encode(str, b.array(), b.position());
      return;
    }*/
    int len = str.length;
    int c;

    int i = 0;
    for (i = 0; i < len; i++) {
      c = str[i];
      if (!((c >= 0x0001) && (c <= 0x007F)))
        break;
      b.put((byte) c);
    }

    // difficult case:
    for (; i < len; i++) {
      c = str[i];
      if ((c >= 0x0001) && (c <= 0x007F)) {
        b.put((byte) c);

      } else if (c > 0x07FF) {
        b.put((byte) (0xE0 | ((c >> 12) & 0x0F)));
        b.put((byte) (0x80 | ((c >> 6) & 0x3F)));
        b.put((byte) (0x80 | ((c >> 0) & 0x3F)));
      } else {
        b.put((byte) (0xC0 | ((c >> 6) & 0x1F)));
        b.put((byte) (0x80 | ((c >> 0) & 0x3F)));
      }
    }
  }
  
  public static final String decode(final byte[] b) throws Exception{
    return decode(b, 0, b.length);
  }
  
  public static final char[] decodeChars(final byte[] b) throws Exception{
    return decodeChars(b, 0, b.length);
  }

  public static final String decode(final byte[] b, int offset, int length){
    int count = offset, c_count = 0;
    int c, char2, char3;
    char arr[] = new char[length];
    int to = offset + length;
    while (count < to) {
      c = (int) b[count] & 0xff;
      if (c > 127)
        break;
      count++;
      arr[c_count++] = (char) c;
    }

    // difficult case:
    while (count < to) {
      c = (int) b[count] & 0xff;
      switch (c >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++;
        arr[c_count++] = (char) c;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        if (count > to)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b[count - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + count);
        arr[c_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        if (count > to)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b[count - 2];
        char3 = (int) b[count - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        arr[c_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count+" "+length+" "+to+" "+offset+" "+(c >> 4)+" "+CBUtil.decodeInt(b, count)+" "+b[count-1]+" "+b[count-2]+" "+b[count-3]);
      }
    }
    // The number of chars produced may be less than length
    return new String(arr, 0, c_count);
  }
  
  public static final char[] decodeChars(final byte[] b, int offset, int length){
    int count = offset, c_count = 0;
    int c, char2, char3;
    char arr[] = new char[length];
    int to = offset + length;
    while (count < to) {
      c = (int) b[count] & 0xff;
      if (c > 127)
        break;
      count++;
      arr[c_count++] = (char) c;
    }

    // difficult case:
    while (count < length) {
      c = (int) b[count] & 0xff;
      switch (c >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++;
        arr[c_count++] = (char) c;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b[count - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + count);
        arr[c_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b[count - 2];
        char3 = (int) b[count - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        arr[c_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
    }
    // The number of chars produced may be less than length
    return Arrays.copyOfRange(arr, 0, c_count);
  }
  
  public static final String decode(final ByteBuffer b, int length){
    /*if(b.hasArray()){
      String toRet = decode(b.array(), b.position(), length);
      b.position(b.position()+length);
      System.out.println(toRet);
      return toRet;
    }*/
    
    int count = 0, c_count = 0;
    int c, char2, char3;
    char arr[] = new char[length];
    
    while (count < length) {
      c = (int) b.get(b.position()) & 0xff;
      if (c > 127)
        break;
      b.get();
      count++;
      arr[c_count++] = (char) c;
    }

    // difficult case:
    while (count < length) {
      c = (int) b.get() & 0xff;
      switch (c >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++;
        arr[c_count++] = (char) c;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b.get();
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + count);
        arr[c_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b.get();
        char3 = (int) b.get();
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        arr[c_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
    }
    // The number of chars produced may be less than length
    return new String(arr, 0, c_count);
  }
  
  public static final char[] decodeChars(final ByteBuffer b, int length){
    if(b.hasArray()){
      return decodeChars(b.array(), b.position(), length);
    }
    
    int count = 0, c_count = 0;
    int c, char2, char3;
    char arr[] = new char[length];
    
    while (count < length) {
      c = (int) b.get(b.position()) & 0xff;
      if (c > 127)
        break;
      b.get();
      arr[c_count++] = (char) c;
    }

    // difficult case:
    while (count < length) {
      c = (int) b.get() & 0xff;
      switch (c >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++;
        arr[c_count++] = (char) c;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b.get();
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + count);
        arr[c_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        if (count > length)
          throw new Error(
              "malformed input: partial character at end");
        char2 = (int) b.get();
        char3 = (int) b.get();
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        arr[c_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
    }
    // The number of chars produced may be less than length
    return Arrays.copyOfRange(arr, 0, c_count);
  }
  
  //compare
  public static final int compare(byte[] b1, int o1, byte[] b2, int o2){
    int length1, length2, c1,c2, num = 0, i = 0;
    
    // length1
    c1 = (b1[o1++] & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b1[o1++] & 0xFF);
      i++;
    }
    length1 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    // length2
    num = 0;
    i = 0;
    c1 = (b2[o2++] & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b2[o2++] & 0xFF);
      i++;
    }
    length2 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    //System.out.println(length1+" "+length2);
    int min = Math.min(length1, length2);
    int count = 0;
    while(count < min){
      c1 = b1[o1] & 0xFF;
      c2 = b2[o2] & 0xFF;
      if(c1 > 127 || c2 > 127)
        break;
      if(c1 != c2)
        return c1 - c2;
      o1++;
      o2++;
      count++;
    }
    //difficult case
    //you only have to update count for the char from the first string
    //since it should be exactly the same as long as the chars are the same
    char cmp1, cmp2;
    int char2, char3;
    while(count < min){
      //first char
      c1 = (int) b1[o1] & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++; o1++;
        cmp1 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        o1 += 2;
        char2 = (int) b1[o1 - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o1);
        cmp1 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        o1 += 3;
        char2 = (int) b1[o1 - 2];
        char3 = (int) b1[o1 - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (o1 - 1));
        cmp1 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + o1);
      }
      
      //second char
      c1 = (int) b2[o2] & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        o2++;
        cmp2 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        o2 += 2;
        char2 = (int) b2[o2 - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o2);
        cmp2 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        o2 += 3;
        char2 = (int) b2[o2 - 2];
        char3 = (int) b2[o2 - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        cmp2 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
      if(cmp1 != cmp2)
        return cmp1 - cmp2;
    }
    //the string starts the same (or are actually the same)
    return length1 - length2;
  }
  
  public static final int compare(ByteBuffer b1, int o1, ByteBuffer b2, int o2){
    /*if(b1.hasArray() && b2.hasArray()){
      return compare(b1.array(), o1, b2.array(), o2);
    }*/
    
    int length1, length2, c1,c2, num = 0, i = 0;
    
    // length1
    c1 = (b1.get(o1++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b1.get(o1++) & 0xFF);
      i++;
    }
    length1 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    // length2
    num = 0;
    i = 0;
    c1 = (b2.get(o2++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b2.get(o2++) & 0xFF);
      i++;
    }
    length2 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    
    int min = Math.min(length1, length2);
    int count = 0;
    while(count < min){
      c1 = b1.get(o1) & 0xFF;
      c2 = b2.get(o2) & 0xFF;
      if(c1 > 127 || c2 > 127)
        break;
      if(c1 != c2)
        return c1 - c2;
      o1++;
      o2++;
      count++;
    }
    //difficult case
    //you only have to update count for the char from the first string
    //since it should be exactly the same as long as the chars are the same
    char cmp1, cmp2;
    int char2, char3;
    while(count < min){
      //first char
      c1 = (int) b1.get(o1) & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++; o1++;
        cmp1 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        o1 += 2;
        char2 = (int) b1.get(o1 - 1);
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o1);
        cmp1 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        o1 += 3;
        char2 = (int) b1.get(o1 - 2);
        char3 = (int) b1.get(o2 - 1);
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (o1 - 1));
        cmp1 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + o1);
      }
      
      //second char
      c1 = (int) b2.get(o2) & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        o2++;
        cmp2 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        o2 += 2;
        char2 = (int) b2.get(o2 - 1);
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o2);
        cmp2 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        o2 += 3;
        char2 = (int) b2.get(o2 - 2);
        char3 = (int) b2.get(o2 - 1);
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        cmp2 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
      if(cmp1 != cmp2)
        return cmp1 - cmp2;
    }
    //the string starts the same (or are actually the same)
    return length1 - length2;
  }
  
  public static final int compare(byte[] b1, int o1, byte[] b2, int o2, char[] map){
    int length1, length2, c1,c2, num = 0, i = 0;
    
    // length1
    c1 = (b1[o1++] & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b1[o1++] & 0xFF);
      i++;
    }
    length1 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    // length2
    num = 0;
    i = 0;
    c1 = (b2[o2++] & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b2[o2++] & 0xFF);
      i++;
    }
    length2 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    //System.out.println(length1+" "+length2);
    int min = Math.min(length1, length2);
    int count = 0;
    while(count < min){
      c1 = b1[o1] & 0xFF;
      c2 = b2[o2] & 0xFF;
      if(c1 > 127 || c2 > 127)
        break;
      if(c1 != c2)
        return map[c1] - map[c2];
      o1++;
      o2++;
      count++;
    }
    //difficult case
    //you only have to update count for the char from the first string
    //since it should be exactly the same as long as the chars are the same
    char cmp1, cmp2;
    int char2, char3;
    while(count < min){
      //first char
      c1 = (int) b1[o1] & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++; o1++;
        cmp1 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        o1 += 2;
        char2 = (int) b1[o1 - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o1);
        cmp1 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        o1 += 3;
        char2 = (int) b1[o1 - 2];
        char3 = (int) b1[o1 - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (o1 - 1));
        cmp1 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + o1);
      }
      
      //second char
      c1 = (int) b2[o2] & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        o2++;
        cmp2 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        o2 += 2;
        char2 = (int) b2[o2 - 1];
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o2);
        cmp2 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        o2 += 3;
        char2 = (int) b2[o2 - 2];
        char3 = (int) b2[o2 - 1];
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        cmp2 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
      if(cmp1 != cmp2)
        return map[cmp1] - map[cmp2];
    }
    //the string starts the same (or are actually the same)
    return length1 - length2;
  }
  
  public static final int compare(ByteBuffer b1, int o1, ByteBuffer b2, int o2, char[] map){
    /*if(b1.hasArray() && b2.hasArray()){
      return compare(b1.array(), o1, b2.array(), o2, map);
    }*/
    
    int length1, length2, c1,c2, num = 0, i = 0;
    
    // length1
    c1 = (b1.get(o1++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b1.get(o1++) & 0xFF);
      i++;
    }
    length1 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    // length2
    num = 0;
    i = 0;
    c1 = (b2.get(o2++) & 0xFF);
    while ((c1 & 0x80) == 0) {
      num |= (c1 << (7 * i));
      c1 = (b2.get(o2++) & 0xFF);
      i++;
    }
    length2 = (num |= ((c1 & ~(0x80)) << (7 * i)));
    
    int min = Math.min(length1, length2);
    int count = 0;
    while(count < min){
      c1 = b1.get(o1) & 0xFF;
      c2 = b2.get(o2) & 0xFF;
      if(c1 > 127 || c2 > 127)
        break;
      if(c1 != c2)
        return map[c1] - map[c2];
      o1++;
      o2++;
      count++;
    }
    //difficult case
    //you only have to update count for the char from the first string
    //since it should be exactly the same as long as the chars are the same
    char cmp1, cmp2;
    int char2, char3;
    while(count < min){
      //first char
      c1 = (int) b1.get(o1) & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        count++; o1++;
        cmp1 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        count += 2;
        o1 += 2;
        char2 = (int) b1.get(o1 - 1);
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o1);
        cmp1 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        count += 3;
        o1 += 3;
        char2 = (int) b1.get(o1 - 2);
        char3 = (int) b1.get(o2 - 1);
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (o1 - 1));
        cmp1 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + o1);
      }
      
      //second char
      c1 = (int) b2.get(o2) & 0xff;
      switch (c1 >> 4) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        /* 0xxxxxxx */
        o2++;
        cmp2 = (char) c1;
        break;
      case 12:
      case 13:
        /* 110x xxxx 10xx xxxx */
        o2 += 2;
        char2 = (int) b2.get(o2 - 1);
        if ((char2 & 0xC0) != 0x80)
          throw new Error("malformed input around byte "
              + o2);
        cmp2 = (char) (((c1 & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        /* 1110 xxxx 10xx xxxx 10xx xxxx */
        o2 += 3;
        char2 = (int) b2.get(o2 - 2);
        char3 = (int) b2.get(o2 - 1);
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
          throw new Error("malformed input around byte "
              + (count - 1));
        cmp2 = (char) (((c1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
        break;
      default:
        /* 10xx xxxx, 1111 xxxx */
        throw new Error("malformed input around byte " + count);
      }
      if(cmp1 != cmp2)
        return map[cmp1] - map[cmp2];
    }
    //the string starts the same (or are actually the same)
    return length1 - length2;
  }

}
