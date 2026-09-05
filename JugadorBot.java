import java.util.Comparator;
import java.util.List;

public class JugadorBot extends Jugador {

    public JugadorBot(String nombre) {
        super(nombre);
    }

    @Override
    public Carta jugarCarta() {
        mano.sort(Comparator.comparingInt(Carta::getJerarquiaTruco));
        return mano.remove(0);
    }

    public int calcularFuerzaMano() {
        int suma = 0;
        for (Carta c : mano) {
            suma += c.getJerarquiaTruco();
        }
        return suma;
    }

    public boolean quiereCantarTruco(int nivelActual) {
        int fuerza = calcularFuerzaMano();
        if (nivelActual == 1 && fuerza >= 22) return true;
        if (nivelActual == 2 && fuerza >= 16 && mano.size() <= 2) return true;
        if (nivelActual == 3 && fuerza >= 12 && mano.size() == 1) return true;
        return false;
    }

    public int responderTruco(int nivelActual) {
        int fuerza = calcularFuerzaMano();
        if (nivelActual == 2) {
            if (fuerza >= 24) return 3;
            if (fuerza >= 18) return 1;
            return 2;
        } else if (nivelActual == 3) {
            if (fuerza >= 20) return 3;
            if (fuerza >= 14) return 1;
            return 2;
        } else if (nivelActual == 4) {
            if (fuerza >= 12) return 1;
            return 2;
        }
        return 1;
    }

    public int responderEnvido(int tanto, int tipoApuesta) {
        if (tipoApuesta == 1) {
            if (tanto >= 31) return 3;
            if (tanto >= 26) return 1;
            return 2;
        } else if (tipoApuesta == 2) {
            if (tanto >= 32) return 3;
            if (tanto >= 29) return 1;
            return 2;
        } else {
            return (tanto >= 31) ? 1 : 2;
        }
    }

    public int cantarFrenteAFlor(int tantoFlor) {
        if (tantoFlor >= 34) return 2; // Contraflor al resto
        return 1; // Flor
    }

    public int responderContraflorAlResto(int tantoFlor) {
        return (tantoFlor >= 33) ? 1 : 2; // 1: Con flor quiero, 2: Con flor me achico
    }
}