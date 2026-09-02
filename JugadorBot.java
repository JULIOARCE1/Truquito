import java.util.Comparator;
import java.util.List;

public class JugadorBot extends Jugador {

    public JugadorBot(String nombre) {
        super(nombre);
    }

    @Override
    public Carta jugarCarta() {
        // Juega la carta más baja posible para guardar potencia
        mano.sort(Comparator.comparingInt(Carta::getJerarquiaTruco));
        return mano.remove(0);
    }

    // Fuerza combinada de las cartas restantes (jerarquías van de 1 a 14)
    public int calcularFuerzaMano() {
        int suma = 0;
        for (Carta c : mano) {
            suma += c.getJerarquiaTruco();
        }
        return suma;
    }

    // Decisión de cantar Truco/Retruco/Vale 4
    public boolean quiereCantarTruco(int nivelActual) {
        int fuerza = calcularFuerzaMano();
        if (nivelActual == 1 && fuerza >= 22) return true; // Canta Truco
        if (nivelActual == 2 && fuerza >= 16 && mano.size() <= 2) return true; // Canta Retruco
        if (nivelActual == 3 && fuerza >= 12 && mano.size() == 1) return true; // Canta Vale 4
        return false;
    }

    // Decisión de aceptar o rechazar Truco
    // Retorna: 1 (Quiero), 2 (No quiero), 3 (Subir apuesta)
    public int responderTruco(int nivelActual) {
        int fuerza = calcularFuerzaMano();

        if (nivelActual == 2) { // Le cantaron Truco
            if (fuerza >= 24) return 3; // Retruco
            if (fuerza >= 18) return 1; // Quiero
            return 2; // No quiero
        } else if (nivelActual == 3) { // Le cantaron Retruco
            if (fuerza >= 20) return 3; // Vale Cuatro
            if (fuerza >= 14) return 1; // Quiero
            return 2; // No quiero
        } else if (nivelActual == 4) { // Le cantaron Vale Cuatro
            if (fuerza >= 12) return 1; // Quiero
            return 2; // No quiero
        }
        return 1;
    }

    // Decisión de respuesta ante Envido
    // Retorna: 1 (Quiero), 2 (No quiero), 3 (Subir apuesta)
    public int responderEnvido(int tanto, int tipoApuesta) {
        if (tipoApuesta == 1) { // Envido simple
            if (tanto >= 31) return 3; // Eleva a Real Envido o Falta
            if (tanto >= 26) return 1; // Quiero
            return 2; // No quiero
        } else if (tipoApuesta == 2) { // Real Envido
            if (tanto >= 32) return 3; // Eleva a Falta
            if (tanto >= 29) return 1; // Quiero
            return 2; // No quiero
        } else { // Falta Envido
            return (tanto >= 31) ? 1 : 2;
        }
    }
}