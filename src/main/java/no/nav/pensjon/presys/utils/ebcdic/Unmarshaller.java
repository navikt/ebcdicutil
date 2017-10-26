package no.nav.pensjon.presys.utils.ebcdic;

import no.nav.pensjon.presys.utils.ebcdic.annotations.*;
import no.nav.pensjon.presys.utils.ebcdic.converters.BitTabell2BoolArray;
import no.nav.pensjon.presys.utils.ebcdic.error.CouldNotUnmarshallField;
import no.nav.pensjon.presys.utils.ebcdic.error.CouldNotUnmarshallSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static no.nav.pensjon.presys.utils.ebcdic.ReflectionUtils.*;
import static no.nav.pensjon.presys.utils.ebcdic.StringUtils.padLeft;


class Unmarshaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(Unmarshaller.class);

    private Unmarshaller(){}

    private static <E> E mapData(byte[] values, Class<E> clazz)  {
        try {
            E o = clazz.newInstance();
            Arrays.stream(clazz.getDeclaredFields())
                .filter(f->f.isAnnotationPresent(Felt.class))
                .forEach(f->createObjectFromField(values, f).ifPresent(value-> setObjectOnField(o, f, value)));

        return o;
        } catch (CouldNotUnmarshallField e) {
            throw new CouldNotUnmarshallSegment(clazz.getCanonicalName(), e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CouldNotUnmarshallSegment(clazz.getCanonicalName(), e);
        }
    }

    private static Optional<?> createObjectFromField(byte[] values, Field f) {
        Object valueToSet = null;
        Felt ta = f.getAnnotation(Felt.class);
        byte[] feltData = Arrays.copyOfRange(values, ta.start(), ta.start() + ta.length());
        if (f.isAnnotationPresent(PackedDecimal.class)) {
            valueToSet = mapPackedDecimalToObject(feltData, ta, f.getAnnotation(PackedDecimal.class), f.getType());
        } else if (f.isAnnotationPresent(BitTabell.class)) {
            valueToSet = BitTabell2BoolArray.toBoolean(feltData);
        } else if (f.getType().equals(String.class)) {
            if(feltData.length == 1 && feltData[0] == 0){
                valueToSet="";
            }else {
                valueToSet = EbcdicUtils.getString(feltData);
            }
        } else if (f.getType().equals(Integer.TYPE)) {
            valueToSet = ByteBuffer.wrap(feltData).getShort();
        }
        return Optional.ofNullable(valueToSet);
    }

    private static Object mapPackedDecimalToObject(byte[] feltData, Felt ta, PackedDecimal pda, Class<?> type) {
        int unpackedWith = (ta.length() * 2) - 1;
        BigDecimal unpacked;
        try{
            unpacked = EbcdicUtils.unpack(feltData, unpackedWith, pda.decimals());
        } catch (NumberFormatException nfe){
            LOGGER.warn("Feil under lesing av Packed Decimal", new CouldNotUnmarshallField(ta.name() , nfe));
            unpacked = BigDecimal.ZERO;
        }
        if (type.equals(String.class)) {
            return padLeft(unpacked.toString(), unpackedWith);
        } else if (type.equals(Integer.TYPE)) {
            return unpacked.intValue();
        } else if (type.equals(BigDecimal.class)){
            return unpacked;
        }
        return null;
    }

    private static Meta lesMetadata(ScrollableArray data, boolean consume) {
        return consume ? mapData(data.read(Meta.META_SIZE), Meta.class):mapData(data.peekAhead(0, Meta.META_SIZE), Meta.class);
    }

    static <T> T les(ScrollableArray data, Class<T> segmentToMap) {

        Meta m = lesMetadata(data, true);
        T o = mapData(data.read(m.getDatalengde()), segmentToMap);
        Segment seg = segmentToMap.getAnnotation(Segment.class);
        if(seg.length() != m.getDatalengde()){
            String segmentname = seg.name();
            LOGGER.warn("Avvik på datalengde i Segment:%s. Metadata:%o Segmentbeskrivelse:%o", segmentname,m.getDatalengde(), seg.length());
        }

        if((m.getMetalengde() + m.getDatalengde()) % 2 == 1){
            data.read(1);
        }

        List<Field> subSegmentFields = Arrays.stream(segmentToMap.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(SubSegment.class))
                .collect(Collectors.toList());

        if(subSegmentFields.isEmpty()){
            return o;
        }
        AtomicBoolean end = new AtomicBoolean(false);
        while (data.bytesLeft() > Meta.META_SIZE && !end.get()) {
            Meta mNext = lesMetadata(data, false);
            end.set(true);
            subSegmentFields.stream()
                    .filter(field -> findSubType(field).getAnnotation(Segment.class).name().equals(mNext.getSegmentNavn()))
                    .findAny()
                    .ifPresent(field -> {
                        Class<?> subTypeClass = findSubType(field);
                            Object subTypeObject = les(data, subTypeClass);
                            if(field.getType().equals(List.class)){
                                List l = (List) get(field, o);
                                l.add(subTypeObject);
                            } else {
                                set(subTypeObject, field, o);
                            }
                            end.set(false);
                    });
        }
        return o;
    }
}
