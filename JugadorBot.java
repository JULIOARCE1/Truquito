import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class JugadorBot extends Jugador {
    private final Random azar;

    public JugadorBot(String nombre) {
        super(nombre);
        this.azar = new Random();
    }

    @Override
    public Carta jugarCarta() {
        mano.sort(Comparator.comparingInt(Carta::getJerarquiaTruco));
        
        if (mano.size() > 1 && azar.nextInt(100) < 8) {
            Carta c = mano.remove(0);
            c.setTapada(true);
            return c;
        }

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

        if (azar.nextInt(100) < 15) {
            if (nivelActual == 1 && fuerza < 16) return true;
        }
        return false;
    }

    public int responderTruco(int nivelActual) {
        int fuerza = calcularFuerzaMano();
        int prob = azar.nextInt(100);

        if (nivelActual == 2) {
            if (fuerza >= 24) return 3;
            if (prob < 10) return 3;
            if (fuerza >= 17) return 1;
            return 2;
        } else if (nivelActual == 3) {
            if (fuerza >= 20) return 3;
            if (prob < 6) return 3;
            if (fuerza >= 14) return 1;
            return 2;
        } else if (nivelActual == 4) {
            return (fuerza >= 12) ? 1 : 2;
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
        return (tantoFlor >= 34) ? 2 : 1;
    }

    public int responderContraflorAlResto(int tantoFlor) {
        return (tantoFlor >= 33) ? 1 : 2;
    }
}