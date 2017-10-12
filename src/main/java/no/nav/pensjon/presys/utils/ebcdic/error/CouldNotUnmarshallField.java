package no.nav.pensjon.presys.utils.ebcdic.error;

public class CouldNotUnmarshallField extends RuntimeException {


    public CouldNotUnmarshallField(String fieldName, Throwable cause){
        super(fieldName, cause);
    }



}
