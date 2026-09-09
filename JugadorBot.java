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
        
        // Mentira ocasional (10% de probabilidad): tirar una carta baja tapada
        if (mano.size() > 1 && azar.nextInt(100) < 10) {
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

    // Decisión de cantar Truco con probabilidad de mentira (Bluff)
    public boolean quiereCantarTruco(int nivelActual) {
        int fuerza = calcularFuerzaMano();

        // 1. Por fuerza real de cartas
        if (nivelActual == 1 && fuerza >= 22) return true;
        if (nivelActual == 2 && fuerza >= 16 && mano.size() <= 2) return true;
        if (nivelActual == 3 && fuerza >= 12 && mano.size() == 1) return true;

        // 2. Mentira: canta con mano floja para asustar (18% de probabilidad)
        boolean mentir = azar.nextInt(100) < 18;
        if (mentir) {
            if (nivelActual == 1 && fuerza < 16) return true;
            if (nivelActual == 2 && fuerza < 12 && mano.size() <= 2) return true;
        }

        return false;
    }

    // Decisión al recibir un canto de Truco
    // Retorna: 1 (Quiero), 2 (No quiero), 3 (Subir apuesta)
    public int responderTruco(int nivelActual) {
        int fuerza = calcularFuerzaMano();
        int probabilidadBluff = azar.nextInt(100);

        if (nivelActual == 2) { // Le cantaron Truco
            if (fuerza >= 24) return 3; // Retruco real
            if (probabilidadBluff < 12) return 3; // Mentira: mete Retruco de la nada
            if (fuerza >= 17) return 1; // Quiero
            return 2; // No quiero
        } else if (nivelActual == 3) { // Le cantaron Retruco
            if (fuerza >= 20) return 3; // Vale 4 real
            if (probabilidadBluff < 8) return 3; // Mentira audaz: clava Vale Cuatro
            if (fuerza >= 14) return 1; // Quiero
            return 2; // No quiero
        } else if (nivelActual == 4) { // Le cantaron Vale Cuatro
            if (fuerza >= 12) return 1;
            return 2;
        }
        return 1;
    }

    // Evaluación para abrir el envido (con chance de mentir con poco tanto)
    public boolean quiereAbrirEnvido(int tanto) {
        if (tanto >= 26) return true;
        // Mentira (15% de probabilidad si tiene menos de 24 de tanto)
        return tanto <= 24 && azar.nextInt(100) < 15;
    }

    // Respuesta a envido cantado por el rival
    // Retorna: 1 (Quiero), 2 (No quiero), 3 (Subir apuesta)
    public int responderEnvido(int tanto, int tipoApuesta) {
        int suerte = azar.nextInt(100);

        if (tipoApuesta == 1) { // Envido simple
            if (tanto >= 31) return 3; // Eleva a Real Envido
            if (suerte < 10 && tanto <= 23) return 3; // Mentira: sube a Real Envido con nada
            if (tanto >= 26) return 1; // Quiero
            return 2; // No quiero
        } else if (tipoApuesta == 2) { // Real Envido
            if (tanto >= 32) return 3; // Eleva a Falta Envido
            if (tanto >= 29) return 1; // Quiero
            return 2; // No quiero
        } else { // Falta Envido
            return (tanto >= 31) ? 1 : 2;
        }
    }

    public int cantarFrenteAFlor(int tantoFlor) {
        if (tantoFlor >= 34) return 2; // Contraflor al resto
        return 1; // Flor
    }

    public int responderContraflorAlResto(int tantoFlor) {
        return (tantoFlor >= 33) ? 1 : 2;
    }
}