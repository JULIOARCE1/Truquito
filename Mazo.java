import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mazo {
    private final List<Carta> cartas;

    public Mazo() {
        cartas = new ArrayList<>();
        reiniciar();
    }

    public final void reiniciar() {
        cartas.clear();
        for (Palo p : Palo.values()) {
            for (int n = 1; n <= 12; n++) {
                if (n == 8 || n == 9) continue;
                cartas.add(new Carta(n, p));
            }
        }
        Collections.shuffle(cartas);
    }

    public Carta robar() {
        if (cartas.isEmpty()) {
            throw new IllegalStateException("El mazo se quedó sin cartas.");
        }
        return cartas.remove(0);
    }
}