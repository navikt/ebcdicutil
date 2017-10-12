package no.nav.pensjon.presys.utils.ebcdic;

class StringUtils {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private StringUtils(){
    }

    static String padLeft(String oldString, int newLength){
        StringBuilder sb = new StringBuilder();
        while(sb.length() + oldString.length() < newLength){
            sb.append("0");
        }
        sb.append(oldString);
        return sb.toString();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
