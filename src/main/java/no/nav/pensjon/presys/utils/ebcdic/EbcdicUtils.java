package no.nav.pensjon.presys.utils.ebcdic;

import com.ibm.as400.access.AS400PackedDecimal;

import java.io.*;
import java.math.BigDecimal;

public class EbcdicUtils {

    public static final String EBCDIC_CHARSET = "Cp1142";

    public static BigDecimal unpack(byte[] packed, int unpackedLength, int decimals){
        return (BigDecimal) new AS400PackedDecimal(unpackedLength, decimals).toObject(packed);
    }

    public static byte[] pack(BigDecimal unpacked, int length) {
        AS400PackedDecimal packedDecimal = new AS400PackedDecimal(length, 0);
        return packedDecimal.toBytes(unpacked);
    }

    public static String getString(byte[] Cp1047bytes){
        try {
            return new String(Cp1047bytes, EBCDIC_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
