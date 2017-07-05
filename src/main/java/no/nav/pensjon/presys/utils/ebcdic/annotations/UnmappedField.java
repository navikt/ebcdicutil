package no.nav.pensjon.presys.utils.ebcdic.annotations;

import java.lang.annotation.*;

@Repeatable(UnmappedFields.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UnmappedField {
    String name();
    int start();
    int length();
}
