package app.util.rssnotifier.database;

public class UnicodeToAscii {
	public static final String UNICODE_STRING
		= "àÀảẢãÃáÁạẠăĂằẰẳẲẵẴắẮặẶâÂầẦẩẨẫẪấẤậẬđĐèÈẻẺẽẼéÉẹẸêÊềỀểỂễỄếẾệỆìÌỉỈĩĨíÍịỊòÒỏỎõÕóÓọỌôÔồỒổỔỗỖốỐộỘơƠờỜởỞỡỠớỚợỢùÙủỦũŨúÚụỤưƯừỪửỬữỮứỨựỰýÝ :+\\<>\"*,!?%$=@#~[]`|^";
	public static final String ASCII_STRING
		= "aAaAaAaAaAaAaAaAaAaAaAaAaAaAaAaAaAdDeEeEeEeEeEeEeEeEeEeEeEiIiIiIiIiIoOoOoOoOoOoOoOoOoOoOoOoOoOoOoOoOoOuUuUuUuUuUuUuUuUuUuUuUyY____\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";
	
	public static final char[] UNICODE_CHARS = UNICODE_STRING.toCharArray();
	public static final char[] ASCII_CHARS = ASCII_STRING.toCharArray();
	
	public static String convertToLatin(String unicodeString) {
		char [] unicodeChars = unicodeString.toCharArray();
		char [] asciiChars = new char[unicodeChars.length];
		boolean check;
		
		for (int i = 0; i < unicodeChars.length; i++) {
			check = false;
			char unicodeChar = unicodeChars[i];
			
			if (unicodeChar != ' ')
				for (int j = 0; j < UNICODE_CHARS.length; j++) {
					if (unicodeChar == UNICODE_CHARS[j]) {
						asciiChars[i] = ASCII_CHARS[j];
						check = true;
						break;
					}
				}
			if (check == false)
				asciiChars[i] = unicodeChar;	
		}
		return new String(asciiChars);
	}
}