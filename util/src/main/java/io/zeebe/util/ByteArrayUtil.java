package io.zeebe.util;

public class ByteArrayUtil {
  private ByteArrayUtil() {}

  public static byte[] concat(byte[]... buffers) {
    int length = 0;
    for (byte[] buffer : buffers) {
      length += buffer.length;
    }

    return concatTo(new byte[length], buffers);
  }

  public static byte[] concatTo(byte[] dest, byte[]... buffers) {
    int offset = 0;
    for (byte[] buffer : buffers) {
      System.arraycopy(buffer, 0, dest, offset, buffer.length);
      offset += buffer.length;
    }

    return dest;
  }
}
