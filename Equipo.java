import java.util.ArrayList;
import java.util.List;

public class Equipo {
    private final String nombre;
    private final List<Jugador> integrantes;
    private int puntos;

    public Equipo(String nombre) {
        this.nombre = nombre;
        this.integrantes = new ArrayList<>();
        this.puntos = 0;
    }

    public void agregarJugador(Jugador j) {
        integrantes.add(j);
    }

    public List<Jugador> getIntegrantes() {
        return integrantes;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntos() {
        return puntos;
    }

    public void sumarPuntos(int pts) {
        this.puntos += pts;
    }

    public boolean tieneJugador(Jugador j) {
        return integrantes.contains(j);
    }

    public int getMejorTantoEnvido() {
        int max = 0;
        for (Jugador j : integrantes) {
            max = Math.max(max, CalculadorEnvido.calcular(j.getMano()));
        }
        return max;
    }

    public int getMejorTantoFlor() {
        int max = 0;
        for (Jugador j : integrantes) {
            if (CalculadorFlor.tieneFlor(j.getMano())) {
                max = Math.max(max, CalculadorFlor.calcularTantoFlor(j.getMano()));
            }
        }
        return max;
    }

    public boolean tieneAlgunaFlor() {
        for (Jugador j : integrantes) {
            if (CalculadorFlor.tieneFlor(j.getMano())) return true;
        }
        return false;
    }
}