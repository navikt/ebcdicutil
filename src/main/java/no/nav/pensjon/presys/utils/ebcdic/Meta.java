package no.nav.pensjon.presys.utils.ebcdic;

import no.nav.pensjon.presys.utils.ebcdic.annotations.Felt;
import no.nav.pensjon.presys.utils.ebcdic.annotations.Segment;

@Segment(name="meta", length = Meta.META_SIZE)
public class Meta {

    public static final int META_SIZE = 21 + 8 + 6;

    @Felt(name="navn", length = 8, start = 6)
    private String segmentNavn;

    @Felt(name="datalendge", length = 2, start = 4)
    private int datalengde;

    @Felt(name="metalegde", length = 2, start = 2)
    private int metalengde;


    public String getSegmentNavn() {
        return segmentNavn;
    }

    public void setSegmentNavn(String segmentNavn) {
        this.segmentNavn = segmentNavn;
    }

    public int getDatalengde() {
        return datalengde;
    }

    public void setDatalengde(int datalengde) {
        this.datalengde = datalengde;
    }

    public int getMetalengde() {
        return metalengde;
    }

    public void setMetalengde(int metalengde) {
        this.metalengde = metalengde;
    }
}
