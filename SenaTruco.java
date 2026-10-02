import java.util.List;

public enum SenaTruco {
    ANCHO_ESPADA("Guiñar el ojo", "1 de Espada", 14),
    ANCHO_BASTO("Mover la comisura de la boca hacia un lado", "1 de Basto", 13),
    SIETE_ESPADA("Levantar las dos cejas", "7 de Espada", 12),
    SIETE_ORO("Fruncir la nariz", "7 de Oro", 11),
    TRES("Trompita / Tirar un beso", "Un 3 cualquiera", 10),
    DOS("Abrir la boca / Morderse el labio", "Un 2 cualquiera", 9),
    CIEGO("Cerrar los ojos brevemente", "Sin juego relevante", 0),
    NINGUNA("No hacer señas", "Ninguna", -1);

    private final String accion;
    private final String significado;
    private final int jerarquiaMinima;

    SenaTruco(String accion, String significado, int jerarquiaMinima) {
        this.accion = accion;
        this.significado = significado;
        this.jerarquiaMinima = jerarquiaMinima;
    }

    public String getAccion() {
        return accion;
    }

    public String getSignificado() {
        return significado;
    }

    public int getJerarquiaMinima() {
        return jerarquiaMinima;
    }

    public static SenaTruco determinarMejorSena(List<Carta> mano) {
        if (mano == null || mano.isEmpty()) return CIEGO;

        for (Carta c : mano) {
            if (c.getNumero() == 1 && c.getPalo() == Palo.ESPADA) return ANCHO_ESPADA;
        }
        for (Carta c : mano) {
            if (c.getNumero() == 1 && c.getPalo() == Palo.BASTO) return ANCHO_BASTO;
        }
        for (Carta c : mano) {
            if (c.getNumero() == 7 && c.getPalo() == Palo.ESPADA) return SIETE_ESPADA;
        }
        for (Carta c : mano) {
            if (c.getNumero() == 7 && c.getPalo() == Palo.ORO) return SIETE_ORO;
        }
        for (Carta c : mano) {
            if (c.getNumero() == 3) return TRES;
        }
        for (Carta c : mano) {
            if (c.getNumero() == 2) return DOS;
        }
        return CIEGO;
    }
}