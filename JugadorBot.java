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

    public Carta jugarCartaInteligente(Carta cartaMesa, boolean esMano) {
        mano.sort(Comparator.comparingInt(Carta::getJerarquiaTruco));
        if (mano.isEmpty()) return null;

        // 1. Si el bot abre la ronda (va de mano en la baza)
        if (cartaMesa == null || esMano) {
            if (mano.size() == 3) {
                // Abre con carta media el 35% de las veces, si no con la baja
                if (azar.nextInt(100) < 35) {
                    return mano.remove(1);
                }
            }
            return mano.remove(0);
        }

        // 2. El rival ya tiró una carta: evaluar si conviene matar
        int jerarquiaRival = cartaMesa.isTapada() ? 0 : cartaMesa.getJerarquiaTruco();
        
        List<Carta> matadoras = new ArrayList<>();
        for (Carta c : mano) {
            if (c.getJerarquiaTruco() > jerarquiaRival) {
                matadoras.add(c);
            }
        }

        // Si no puede matar la carta de la mesa, descarta la más baja
        if (matadoras.isEmpty()) {
            return mano.remove(0);
        }

        // Ordenamos las que pueden ganar de menor a mayor
        matadoras.sort(Comparator.comparingInt(Carta::getJerarquiaTruco));
        Carta candidata = matadoras.get(0);

        // EVALUACIÓN TÁCTICA EN PRIMERA RONDA (mano.size() == 3)
        if (mano.size() == 3) {
            // Si el rival tiró una carta muy baja (ej: 4 o 5, jerarquía <= 2)
            if (jerarquiaRival <= 2) {
                // Si para matar tiene que gastar una carta brava (jerarquía >= 11: 7 de oro, anchos, etc.)
                // y no tiene una intermedia para cubrir, prefiere no quemarla y ceder primera
                if (candidata.getJerarquiaTruco() >= 11) {
                    return mano.remove(0); // Descarta la baja y guarda la potencia
                }

                // Especulación ocasional: 25% de chances de entregar primera a propósito
                if (azar.nextInt(100) < 25) {
                    return mano.remove(0);
                }
            }
        }

        // Mata con la menor posible que supere al rival
        mano.remove(candidata);
        return candidata;
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

        return azar.nextInt(100) < 16 && fuerza < 16 && nivelActual == 1;
    }

    public int responderTruco(int nivelActual) {
        int fuerza = calcularFuerzaMano();
        int prob = azar.nextInt(100);

        if (nivelActual == 2) {
            if (fuerza >= 24) return 3;
            if (prob < 12) return 3;
            if (fuerza >= 16 || prob < 25) return 1;
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

    public boolean quiereAbrirEnvido(int tanto, boolean esMano) {
        if (tanto >= 26) return true;
        if (esMano && tanto >= 24 && azar.nextInt(100) < 35) return true;
        return tanto <= 23 && azar.nextInt(100) < 18;
    }

    public int responderEnvido(int tanto, int tipoApuesta, boolean esMano) {
        int chanceDesconfiar = azar.nextInt(100);

        if (tipoApuesta == 1) {
            if (tanto >= 31) return 3;
            if (tanto >= 27) return 1;
            if (tanto >= 24 && (esMano || chanceDesconfiar < 45)) return 1;
            if (chanceDesconfiar < 12) return 3;
            return 2;
        } else if (tipoApuesta == 2) {
            if (tanto >= 32) return 3;
            if (tanto >= 29) return 1;
            if (tanto >= 26 && chanceDesconfiar < 30) return 1;
            return 2;
        } else {
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