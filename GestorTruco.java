public class GestorTruco {
    public enum Nivel {
        NADA(1, 1, "TRUCO"),
        TRUCO(2, 1, "RETRUCO"),
        RETRUCO(3, 2, "VALE CUATRO"),
        VALE_CUATRO(4, 3, null);

        private final int puntosQuerido;
        private final int puntosNoQuerido;
        private final String siguienteCanto;

        Nivel(int querido, int noQuerido, String siguiente) {
            this.puntosQuerido = querido;
            this.puntosNoQuerido = noQuerido;
            this.siguienteCanto = siguiente;
        }

        public int getPuntosQuerido() { return puntosQuerido; }
        public int getPuntosNoQuerido() { return puntosNoQuerido; }
        public String getSiguienteCanto() { return siguienteCanto; }
    }

    private Nivel nivelActual;
    private Equipo equipoQueTieneElQuiero; // Equipo habilitado a subir la apuesta
    private Equipo equipoProponente;        // Último equipo que cantó y espera respuesta

    public GestorTruco() {
        reiniciar();
    }

    public void reiniciar() {
        this.nivelActual = Nivel.NADA;
        this.equipoQueTieneElQuiero = null;
        this.equipoProponente = null;
    }

    public Nivel getNivelActual() {
        return nivelActual;
    }

    public boolean puedeCantar(Equipo equipo) {
        if (nivelActual == Nivel.VALE_CUATRO) return false;
        // Si nadie cantó aún, cualquiera puede cantar Truco
        if (nivelActual == Nivel.NADA) return true;
        // Si ya hay un nivel, sólo el equipo que tiene el "quiero" puede subir
        return equipoQueTieneElQuiero == equipo;
    }

    public void proponerAumento(Equipo proponente) {
        this.equipoProponente = proponente;
    }

    public void aceptarAumento() {
        if (nivelActual == Nivel.NADA) {
            nivelActual = Nivel.TRUCO;
        } else if (nivelActual == Nivel.TRUCO) {
            nivelActual = Nivel.RETRUCO;
        } else if (nivelActual == Nivel.RETRUCO) {
            nivelActual = Nivel.VALE_CUATRO;
        }
        // El derecho a redoblar pasa al equipo contrario al proponente
        this.equipoQueTieneElQuiero = null; // Quien respondió "quiero" ahora tiene la palabra
    }

    public void setEquipoConElQuiero(Equipo equipo) {
        this.equipoQueTieneElQuiero = equipo;
    }

    public int getPuntosEnJuego() {
        return nivelActual.getPuntosQuerido();
    }

    public int getPuntosRechazo() {
        return nivelActual.getPuntosNoQuerido();
    }

    public String getProximoCanto() {
        return nivelActual.getSiguienteCanto();
    }

    public boolean estaActivo() {
        return nivelActual != Nivel.NADA;
    }
}