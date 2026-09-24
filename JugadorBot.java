import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class JugadorBot extends Jugador {
    private final Random random = new Random();

    public JugadorBot(String nombre) {
        super(nombre);
    }

    // Selección de carta asistida por Monte Carlo
    public Carta jugarCartaInteligente(
            Carta cartaMesa,
            boolean esPrimeraDeRonda,
            boolean ganaCompanero,
            List<Carta> cartasVisiblesMesa,
            int rondaActual,
            int[] victoriasPrevias,
            boolean botEsMano
    ) {
        List<Carta> mano = getMano();
        if (mano.isEmpty()) return null;
        if (mano.size() == 1) return mano.remove(0);

        // Si mi compañero ya tiene la baza ganada en 2v2, tiro la carta más baja
        if (ganaCompanero) {
            mano.sort(Comparator.comparingInt(Carta::getJerarquiaTruco));
            return mano.remove(0);
        }

        Carta mejorCarta = null;
        double maxProb = -1.0;

        for (Carta c : mano) {
            List<Carta> resto = new ArrayList<>(mano);
            resto.remove(c);

            double prob = SimuladorMonteCarlo.estimarProbabilidadVictoria(
                    c, resto, cartasVisiblesMesa, rondaActual, victoriasPrevias, botEsMano
            );

            // Si hay carta en mesa del rival y esta carta la mata, le damos una bonificación táctica
            if (cartaMesa != null && !cartaMesa.isTapada()) {
                if (c.getJerarquiaTruco() > cartaMesa.getJerarquiaTruco()) {
                    prob += 0.05;
                }
            }

            if (prob > maxProb) {
                maxProb = prob;
                mejorCarta = c;
            }
        }

        if (mejorCarta != null) {
            mano.remove(mejorCarta);
            return mejorCarta;
        }

        return mano.remove(0);
    }

    // Sobrecarga retrocompatible
    public Carta jugarCartaInteligente(Carta cartaMesa, boolean esPrimeraDeRonda, boolean ganaCompanero) {
        return jugarCartaInteligente(cartaMesa, esPrimeraDeRonda, ganaCompanero, new ArrayList<>(), 1, new int[3], false);
    }

    public Carta jugarCartaInteligente(Carta cartaMesa, boolean esPrimeraDeRonda) {
        return jugarCartaInteligente(cartaMesa, esPrimeraDeRonda, false);
    }

    // Decisión de apertura de Envido
    public boolean quiereAbrirEnvido(int tanto, boolean esMano) {
        if (esMano) {
            if (tanto >= 27) return true;
            // Farol criollo: 10% de chances con tanto bajo
            return (tanto < 24 && random.nextInt(100) < 10);
        } else {
            return tanto >= 29;
        }
    }

    // Respuesta a Envido
    public int responderEnvido(int tanto, int tipoEnvido, boolean soyMano) {
        int bonusMano = soyMano ? 1 : 0;
        int umbral = (tipoEnvido == 1) ? 26 : 29;

        if ((tanto + bonusMano) >= 31) {
            if (tipoEnvido == 1 && random.nextInt(100) < 65) return 3; // Real Envido
            return 1; // Quiero
        }

        if ((tanto + bonusMano) >= umbral) return 1;
        if (tanto >= 24 && random.nextInt(100) < 8) return 1; // Farol de aceptación

        return 2; // No quiero
    }

    // Decisión de cantar Truco basada en Monte Carlo
    public boolean quiereCantarTruco(
            int nivelActual,
            List<Carta> cartasVisibles,
            int rondaActual,
            int[] vics,
            boolean botEsMano
    ) {
        double prob = SimuladorMonteCarlo.estimarProbabilidadGlobal(
                getMano(), cartasVisibles, rondaActual, vics, botEsMano
        );

        if (nivelActual == 1) {
            if (prob >= 0.58) return true;
            // Farol con pocas probabilidades: 10% de chances
            return (prob <= 0.25 && random.nextInt(100) < 10);
        }
        if (nivelActual == 2) return prob >= 0.68;
        if (nivelActual == 3) return prob >= 0.82;

        return false;
    }

    public boolean quiereCantarTruco(int nivelActual) {
        return quiereCantarTruco(nivelActual, new ArrayList<>(), 1, new int[3], false);
    }

    // Respuesta a Truco basada en Valor Esperado (EV)
    public int responderTruco(
            int nivelPropuesto,
            int puntosQueridos,
            int puntosNoQueridos,
            List<Carta> cartasVisibles,
            int rondaActual,
            int[] vics,
            boolean botEsMano
    ) {
        double pWin = SimuladorMonteCarlo.estimarProbabilidadGlobal(
                getMano(), cartasVisibles, rondaActual, vics, botEsMano
        );

        // EV = (P_win * puntosGanados) - ((1 - P_win) * puntosPerdidos)
        double evQuiero = (pWin * puntosQueridos) - ((1.0 - pWin) * puntosQueridos);
        double evNoQuiero = -puntosNoQueridos;

        // Si tiene ventaja aplastante, intenta redoblar (Retruco / Vale Cuatro)
        if (pWin >= 0.78 && nivelPropuesto < 4 && random.nextInt(100) < 55) {
            return 3;
        }

        if (evQuiero >= evNoQuiero) {
            return 1; // Quiero
        }

        // Farol de supervivencia: 8%
        if (random.nextInt(100) < 8) return 1;

        return 2; // No quiero
    }

    public int responderTruco(int nivelPropuesto) {
        int queridos = nivelPropuesto;
        int noQueridos = (nivelPropuesto == 2) ? 1 : nivelPropuesto - 1;
        return responderTruco(nivelPropuesto, queridos, noQueridos, new ArrayList<>(), 1, new int[3], false);
    }

    public int cantarFrenteAFlor(int tanto) {
        return (tanto >= 34) ? 2 : 1;
    }

    public int responderContraflorAlResto(int tanto) {
        return (tanto >= 33) ? 1 : 2;
    }
}