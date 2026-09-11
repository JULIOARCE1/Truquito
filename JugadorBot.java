import java.util.ArrayList;
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
        return jugarCartaInteligente(null, true);
    }

    // Elige carta según si juega de mano o respondiendo a la carta de la mesa
    public Carta jugarCartaInteligente(Carta cartaMesa, boolean esMano) {
        mano.sort(Comparator.comparingInt(Carta::getJerarquiaTruco));

        if (mano.isEmpty()) return null;

        // Si el bot va de mano en la baza
        if (cartaMesa == null || esMano) {
            // Si le quedan 3 cartas, tira una intermedia o baja
            if (mano.size() == 3) {
                // 30% de abrir con su carta media para presionar primera
                if (azar.nextInt(100) < 30) {
                    return mano.remove(1);
                }
            }
            return mano.remove(0);
        }

        // El rival ya tiró una carta: busca ganar la baza con lo justo
        int jerarquiaRival = cartaMesa.isTapada() ? 0 : cartaMesa.getJerarquiaTruco();
        List<Carta> matadoras = new ArrayList<>();

        for (Carta c : mano) {
            if (c.getJerarquiaTruco() > jerarquiaRival) {
                matadoras.add(c);
            }
        }

        if (!matadoras.isEmpty()) {
            // Mata con la menor de las que le ganan (para no quemar cartas de más)
            matadoras.sort(Comparator.comparingInt(Carta::getJerarquiaTruco));
            Carta elegida = matadoras.get(0);
            mano.remove(elegida);
            return elegida;
        }

        // Si no puede ganar, tira su carta más baja
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

        // Bluff
        return azar.nextInt(100) < 16 && fuerza < 16 && nivelActual == 1;
    }

    public int responderTruco(int nivelActual) {
        int fuerza = calcularFuerzaMano();
        int prob = azar.nextInt(100);

        if (nivelActual == 2) {
            if (fuerza >= 24) return 3; // Retruco
            if (prob < 12) return 3;     // Retruco mentiroso
            if (fuerza >= 16 || prob < 25) return 1; // Quiero
            return 2;
        } else if (nivelActual == 3) {
            if (fuerza >= 20) return 3;
            if (prob < 8) return 3;
            if (fuerza >= 14) return 1;
            return 2;
        } else if (nivelActual == 4) {
            return (fuerza >= 12) ? 1 : 2;
        }
        return 1;
    }

    // Apertura y respuesta al Envido dinámica (desconfía de bluffs)
    public boolean quiereAbrirEnvido(int tanto, boolean esMano) {
        if (tanto >= 26) return true;
        if (esMano && tanto >= 24 && azar.nextInt(100) < 35) return true;
        // Mentira con poco tanto
        return tanto <= 23 && azar.nextInt(100) < 18;
    }

    public int responderEnvido(int tanto, int tipoApuesta, boolean esMano) {
        int chanceDesconfiar = azar.nextInt(100);

        if (tipoApuesta == 1) { // Envido
            if (tanto >= 31) return 3; // Sube a Real Envido
            if (tanto >= 27) return 1; // Quiero sólido
            if (tanto >= 24 && (esMano || chanceDesconfiar < 45)) return 1; // Paga desconfiando de bluff
            if (chanceDesconfiar < 12) return 3; // Sube mintiendo
            return 2;
        } else if (tipoApuesta == 2) { // Real Envido
            if (tanto >= 32) return 3;
            if (tanto >= 29) return 1;
            if (tanto >= 26 && chanceDesconfiar < 30) return 1;
            return 2;
        } else { // Falta Envido
            if (tanto >= 31) return 1;
            if (tanto >= 28 && chanceDesconfiar < 20) return 1;
            return 2;
        }
    }

    public int cantarFrenteAFlor(int tantoFlor) {
        return (tantoFlor >= 34) ? 2 : 1;
    }

    public int responderContraflorAlResto(int tantoFlor) {
        return (tantoFlor >= 33) ? 1 : 2;
    }
}