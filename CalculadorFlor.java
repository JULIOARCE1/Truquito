import java.util.List;

public class CalculadorFlor {

    public static boolean tieneFlor(List<Carta> mano) {
        if (mano == null || mano.size() < 3) return false;
        return mano.get(0).getPalo() == mano.get(1).getPalo() &&
               mano.get(1).getPalo() == mano.get(2).getPalo();
    }

    public static int calcularTantoFlor(List<Carta> mano) {
        if (!tieneFlor(mano)) return 0;
        int suma = 20;
        for (Carta c : mano) {
            suma += c.getValorEnvido();
        }
        return suma;
    }
}
