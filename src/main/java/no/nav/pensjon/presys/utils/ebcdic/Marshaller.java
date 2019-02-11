package no.nav.pensjon.presys.utils.ebcdic;

import no.nav.pensjon.presys.utils.ebcdic.annotations.Felt;
import no.nav.pensjon.presys.utils.ebcdic.annotations.PackedDecimal;
import no.nav.pensjon.presys.utils.ebcdic.annotations.Segment;
import no.nav.pensjon.presys.utils.ebcdic.annotations.SubSegment;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

import static no.nav.pensjon.presys.utils.ebcdic.ReflectionUtils.get;

class Marshaller {

    private Marshaller(){}

    static void writeSegment(Object o, OutputStream os) throws IOException {
        Meta m = new Meta();
        m.setMetalengde(Meta.META_SIZE);

        Segment seg = o.getClass().getAnnotation(Segment.class);
        m.setDatalengde(seg.length());
        m.setSegmentNavn(seg.name());

        os.write(asByte(m));
        os.write(asByte(o));
        if((m.getMetalengde() + m.getDatalengde()) % 2 == 1){
            os.write(0);
        }

        Field[] fields = o.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(SubSegment.class)) {
                List<Object> segments =  (List<Object>) get(f, o);
                for(Object segment : segments){
                    writeSegment(segment, os);
                }
            }
        }
    }

    private static byte[] asByte(Object o) throws UnsupportedEncodingException {
        Segment seg = o.getClass().getAnnotation(Segment.class);
        byte[] segmentBytes = new byte[seg.length()];
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(Felt.class)) {
                Felt ta = f.getAnnotation(Felt.class);
                byte[] feltData = getFieldAsBytes(o, f, ta);
                System.arraycopy(feltData, 0, segmentBytes, ta.start(), ta.length());
            }
        }

        return segmentBytes;
    }

    private static byte[] getFieldAsBytes(Object o, Field f, Felt ta) throws UnsupportedEncodingException {
        byte[] feltData = new byte[ta.length()];
        Object objectInField = get(f, o);

        if (f.isAnnotationPresent(PackedDecimal.class)) {
            feltData =  objectAsPackedDecimal(f, objectInField, ta);
        } else if (f.getType().equals(String.class)) {
            byte[] tmp = objectInField.toString().getBytes(EbcdicUtils.EBCDIC_CHARSET);
            for(int i = 0; i<feltData.length ;i++){
                feltData[i] = i< tmp.length ? tmp[i]: " ".getBytes(EbcdicUtils.EBCDIC_CHARSET)[0];
            }
        } else if (f.getType().equals(Integer.TYPE)) {
            ByteBuffer buf = ByteBuffer.allocate(2);
            int tmp = (int)objectInField;
            buf.putShort((short)tmp);
            feltData = buf.array();
        }
        return feltData;
    }

    private static byte[] objectAsPackedDecimal(Field f, Object o, Felt ta){
        BigDecimal value;
        if (f.getType().equals(String.class)) {
            if(o.toString().contains("."))
                value = BigDecimal.valueOf(Double.parseDouble(o.toString()));
            else
                value = BigDecimal.valueOf(Integer.parseInt(o.toString()));
        } else if (f.getType().equals(Integer.TYPE)) {
            value = new BigDecimal((int) o);
        } else{
            value = BigDecimal.ZERO;
        }
        return EbcdicUtils.pack(value, ta.length() * 2 - 1, f.getAnnotation(PackedDecimal.class).decimals() );

    }



}
