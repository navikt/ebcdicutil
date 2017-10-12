package no.nav.pensjon.presys.utils.ebcdic.error;

public class CouldNotUnmarshallSegment extends RuntimeException {
    public CouldNotUnmarshallSegment(String canonicalName, ReflectiveOperationException e) {
        super(canonicalName, e);
    }

    public CouldNotUnmarshallSegment(String canonicalName, CouldNotUnmarshallField e) {
        super(canonicalName + "["  + e.getMessage() + "]", e);
    }
}
