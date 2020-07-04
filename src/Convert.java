
public class Convert {
	public static void int2bytes(int n, byte[] buffer, int offset) {
		for (int i = 0; i < 4; i++) {
			buffer[offset + i] = (byte) (n % 256);
			n >>= 8;
		}
	}
	
	public static int bytes2int(byte[] bytes, int offset) {
		int n = 0;
		for (int i = 3; i >= 0; i--) {
			int b = bytes[offset + i];
			if (b < 0)
				b = b + 256;
			n = (n << 8) + b;
		}
		return n;

	}
}
