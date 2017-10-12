package no.nav.pensjon.presys.utils.ebcdic;

import com.ibm.as400.access.AS400PackedDecimal;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.IllegalCharsetNameException;

class EbcdicUtils {
    static final String EBCDIC_CHARSET = "Cp1142";

    private EbcdicUtils(){
    }

    static BigDecimal unpack(byte[] packed, int unpackedLength, int decimals){
        return (BigDecimal) new AS400PackedDecimal(unpackedLength, decimals).toObject(packed);
    }

    static byte[] pack(BigDecimal unpacked, int length, int decimals) {
        AS400PackedDecimal packedDecimal = new AS400PackedDecimal(length, decimals);
        return packedDecimal.toBytes(unpacked);
    }

    static String getString(byte[] cp1047bytes){
        try {
            return new String(cp1047bytes, EBCDIC_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
