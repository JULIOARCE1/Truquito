import java.util.ArrayList;
import java.util.List;

public class Mesa {
    private final List<Jugador> asientos;
    private final Equipo equipo1;
    private final Equipo equipo2;
    private int indiceMano;

    public Mesa(Equipo e1, Equipo e2) {
        this.equipo1 = e1;
        this.equipo2 = e2;
        this.asientos = new ArrayList<>();
        this.indiceMano = 0;

        int maxJugadores = Math.max(e1.getIntegrantes().size(), e2.getIntegrantes().size());
        for (int i = 0; i < maxJugadores; i++) {
            if (i < e1.getIntegrantes().size()) asientos.add(e1.getIntegrantes().get(i));
            if (i < e2.getIntegrantes().size()) asientos.add(e2.getIntegrantes().get(i));
        }
    }

    public List<Jugador> getAsientos() {
        return asientos;
    }

    public int getTotalJugadores() {
        return asientos.size();
    }

    public Jugador getJugador(int indice) {
        return asientos.get(indice % asientos.size());
    }

    public Equipo getEquipoDe(Jugador j) {
        return equipo1.tieneJugador(j) ? equipo1 : equipo2;
    }

    public Equipo getEquipoRival(Jugador j) {
        return equipo1.tieneJugador(j) ? equipo2 : equipo1;
    }

    public Equipo getEquipo1() {
        return equipo1;
    }

    public Equipo getEquipo2() {
        return equipo2;
    }

    public int getIndiceMano() {
        return indiceMano;
    }

    public void rotarMano() {
        indiceMano = (indiceMano + 1) % asientos.size();
    }

    public void setIndiceMano(int indice) {
        this.indiceMano = indice % asientos.size();
    }
}