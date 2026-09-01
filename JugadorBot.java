import java.util.Comparator;

public class JugadorBot extends Jugador {

    public JugadorBot(String nombre) {
        super(nombre);
    }

    @Override
    public Carta jugarCarta() {
        mano.sort(Comparator.comparingInt(Carta::getJerarquiaTruco));
        return mano.remove(0);
    }
}