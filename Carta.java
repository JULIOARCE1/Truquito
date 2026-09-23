public class Carta {
    private final int numero;
    private final Palo palo;

    public Carta(int numero, Palo palo) {
        this.numero = numero;
        this.palo = palo;
    }

    public int getNumero() {
        return numero;
    }

    public Palo getPalo() {
        return palo;
    }

    @Override
    public String toString() {
        String color;
        switch (palo) {
            case ESPADA -> color = "\u001B[94m";  // Azul
            case BASTO  -> color = "\u001B[92m";  // Verde
            case ORO    -> color = "\u001B[93m";  // Amarillo
            case COPA   -> color = "\u001B[91m";  // Rojo
            default     -> color = "\u001B[0m";
        }
        String reset = "\u001B[0m";
        return color + numero + " de " + palo + reset;
    }
}