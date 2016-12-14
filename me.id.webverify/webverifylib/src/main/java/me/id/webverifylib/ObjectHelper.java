package me.id.webverifylib;

import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class ObjectHelper {
  private ObjectHelper() {
  }

  /**
   * Converts an object to their string byte array representation.
   *
   * @param object an object
   * @return the {@code string} byte array representation of the object
   */
  public static String toStringByteArray(Object object) {
    ObjectOutputStream objectOutput;
    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
    try {
      objectOutput = new ObjectOutputStream(arrayOutputStream);
      objectOutput.writeObject(object);
      byte[] data = arrayOutputStream.toByteArray();
      objectOutput.close();
      arrayOutputStream.close();

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Base64OutputStream b64 = new Base64OutputStream(out, Base64.DEFAULT);
      b64.write(data);
      b64.close();
      out.close();
      return new String(out.toByteArray());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Converts a string byte array representation of an object to the object
   *
   * @param stringByteArray a string byte array representation of the object
   * @return the object
   */
  public static <T> T fromStringByteArray(String stringByteArray) {
    if (stringByteArray == null) {
      return null;
    }
    byte[] bytes = stringByteArray.getBytes();
    if (bytes.length == 0) {
      return null;
    }
    ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
    Base64InputStream base64InputStream = new Base64InputStream(byteArray, Base64.DEFAULT);
    ObjectInputStream in;
    try {
      in = new ObjectInputStream(base64InputStream);
      //noinspection unchecked
      return (T) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Checks equality of two objects.
   *
   * @param a an object
   * @param b an object
   * @return {@code true} if objects are equals, {@code false} otherwise
   */
  public static boolean equals(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
  }
}
