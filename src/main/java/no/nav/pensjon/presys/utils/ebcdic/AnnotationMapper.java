package no.nav.pensjon.presys.utils.ebcdic;

import java.io.IOException;
import java.io.OutputStream;

public class AnnotationMapper {
    private AnnotationMapper(){
    }

    static void writeSegment(Object o, OutputStream os) throws IOException {
       Marshaller.writeSegment(o, os);
    }
    public static <T> T les(ScrollableArray data, Class<T> segmentToMap) {
        return Unmarshaller.les(data, segmentToMap);
    }

}
