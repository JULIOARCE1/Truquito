public class GestorEnvido {
    private int puntosApostados;
    private int puntosNoQuiero;
    private boolean faltaEnvidoCantada;
    private boolean resuelto;

    public GestorEnvido() {
        reiniciar();
    }

    public void reiniciar() {
        this.puntosApostados = 0;
        this.puntosNoQuiero = 1;
        this.faltaEnvidoCantada = false;
        this.resuelto = false;
    }

    // Registra un canto inicial o escalonado (1: Envido, 2: Real Envido, 3: Falta Envido)
    public void registrarCanto(int tipo) {
        if (tipo == 1) { // Envido (+2)
            if (puntosApostados == 0) {
                puntosApostados = 2;
                puntosNoQuiero = 1;
            } else {
                puntosNoQuiero = puntosApostados;
                puntosApostados += 2;
            }
        } else if (tipo == 2) { // Real Envido (+3)
            if (puntosApostados == 0) {
                puntosApostados = 3;
                puntosNoQuiero = 1;
            } else {
                puntosNoQuiero = puntosApostados;
                puntosApostados += 3;
            }
        } else if (tipo == 3) { // Falta Envido
            puntosNoQuiero = (puntosApostados == 0) ? 1 : puntosApostados;
            faltaEnvidoCantada = true;
        }
    }

    public int calcularPuntosQueridos(int puntosEq1, int puntosEq2, int limitePuntos) {
        if (!faltaEnvidoCantada) {
            return puntosApostados;
        }
        // Falta Envido tradicional:
        // Si se juega a 30:
        // - Si ambos están en las malas (< 15): puntos que le faltan al líder para llegar a 15 (cerrar las malas).
        // - Si alguno está en las buenas (>= 15): puntos que le faltan al que va ganando para llegar a 30 (ganar el chico).
        int lider = Math.max(puntosEq1, puntosEq2);
        if (limitePuntos == 30) {
            if (puntosEq1 < 15 && puntosEq2 < 15) {
                return 15 - lider;
            }
            return 30 - lider;
        } else {
            return limitePuntos - lider;
        }
    }

    public int getPuntosNoQuiero() {
        return puntosNoQuiero;
    }

    public boolean isFaltaEnvido() {
        return faltaEnvidoCantada;
    }

    public boolean isResuelto() {
        return resuelto;
    }

    public void marcarResuelto() {
        this.resuelto = true;
    }
}