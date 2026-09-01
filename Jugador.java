import java.util.ArrayList;
import java.util.List;

public abstract class Jugador {
    protected String nombre;
    protected List<Carta> mano;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.mano = new ArrayList<>();
    }

    public void recibirCarta(Carta carta) {
        mano.add(carta);
    }

    public void limpiarMano() {
        mano.clear();
    }

    public List<Carta> getMano() {
        return mano;
    }

    public String getNombre() {
        return nombre;
    }

    public abstract Carta jugarCarta();
}