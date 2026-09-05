import java.util.List;

public class CalculadorFlor {

    public static boolean tieneFlor(List<Carta> mano) {
        if (mano == null || mano.size() < 3) return false;
        Palo p0 = mano.get(0).getPalo();
        Palo p1 = mano.get(1).getPalo();
        Palo p2 = mano.get(2).getPalo();
        return p0.equals(p1) && p1.equals(p2);
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
