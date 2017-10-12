package no.nav.pensjon.presys.utils.ebcdic.converters;

public class BitTabell2BoolArray {

    private BitTabell2BoolArray(){
    }

    public static boolean[] toBoolean(byte[] bittabell){
        boolean[] bool = new boolean[bittabell.length*8];
        for(int i = 0;i<bittabell.length;i++)
            System.arraycopy(toBoolean(bittabell[i]), 0, bool, i*8, 8);
        return bool;
    }

    private static boolean[] toBoolean(byte bittabell){
        boolean[] bool = new boolean[8];
        for(int j = 0;j<bool.length;j++)
            bool[j] = ((bittabell >> (7-j)) & 1) == 1;
        return bool;
    }
}
