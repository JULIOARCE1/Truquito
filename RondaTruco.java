import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RondaTruco {
    private final Mesa mesa;
    private final GestorTruco gestorTruco;
    private final GestorEnvido gestorEnvido;
    private final ArbitroEnvido arbitroEnvido;
    private final VistaJuego vista;

    public RondaTruco(Mesa mesa, GestorTruco gestorTruco, GestorEnvido gestorEnvido, ArbitroEnvido arbitroEnvido, VistaJuego vista) {
        this.mesa = mesa;
        this.gestorTruco = gestorTruco;
        this.gestorEnvido = gestorEnvido;
        this.arbitroEnvido = arbitroEnvido;
        this.vista = vista;
    }

    public void jugarRondas() {
        int[] victorias = new int[3];
        int victEq1 = 0, victEq2 = 0;
        int turnoLanzador = mesa.getIndiceMano();
        boolean humanoTiroCarta = false;
        int totalJugadores = mesa.getTotalJugadores();

        for (int ronda = 1; ronda <= 3; ronda++) {
            vista.mostrarMensaje("\n-- RONDA " + ronda + " --");
            Carta mejorCartaRonda = null;
            int ganadorDeRonda = -1;

            for (int k = 0; k < totalJugadores; k++) {
                int actual = (turnoLanzador + k) % totalJugadores;
                Jugador jActual = mesa.getJugador(actual);
                Equipo eqActual = mesa.getEquipoDe(jActual);

                // --- FASE DE ENVIDO EN RONDA 1 ---
                if (ronda == 1 && !gestorEnvido.isResuelto()) {
                    if (totalJugadores == 2 && (k == 0 || k == 1)) {
                        if (arbitroEnvido.gestionarEnvido1v1(k)) return;
                    } else if (totalJugadores == 4 && k == 2) {
                        if (arbitroEnvido.gestionarEnvidoParejas(actual, (turnoLanzador + 3) % totalJugadores)) return;
                    }
                }

                // --- INICIATIVA DE TRUCO ANTES DE TIRAR CARTA ---
                if (jActual instanceof JugadorHumano) {
                    if (gestorTruco.puedeCantar(mesa.getEquipo1())) {
                        int c = vista.pedirOpcion("¿Deseás cantar " + gestorTruco.getProximoCanto() + " antes de tirar?",
                                Arrays.asList("[1] Sí", "[0] No"), 0, 1);
                        if (c == 1) {
                            if (!procesarCantoTrucoHumano(ronda)) return;
                            gestorEnvido.marcarResuelto();
                        }
                    }
                } else if (jActual instanceof JugadorBot botActual) {
                    int nivelNum = gestorTruco.getNivelActual().getPuntosQuerido();
                    if (gestorTruco.puedeCantar(eqActual) && botActual.quiereCantarTruco(nivelNum)) {
                        vista.mostrarMensaje("\n¡" + botActual.getNombre() + " (" + eqActual.getNombre() + ") canta " + gestorTruco.getProximoCanto() + "!");
                        
                        // Si el bot es de mi equipo, el canto va dirigido a los rivales
                        if (eqActual == mesa.getEquipo1()) {
                            if (!procesarCantoCompaneroBot(ronda)) return;
                        } else {
                            // Si el bot es rival, le canta a nuestro equipo (humano responde)
                            if (!procesarCantoTrucoBot(eqActual, ronda)) return;
                        }
                        gestorEnvido.marcarResuelto();
                    }
                }

                // --- TIRADA DE CARTA ---
                Carta c;
                if (jActual instanceof JugadorHumano) {
                    c = jActual.jugarCarta();
                    if (c == null) {
                        irseAlMazo(humanoTiroCarta, mesa.getEquipo2());
                        return;
                    }
                    humanoTiroCarta = true;
                } else {
                    JugadorBot botActual = (JugadorBot) jActual;
                    c = botActual.jugarCartaInteligente(mejorCartaRonda, (k == 0));
                    vista.mostrarMensaje("  " + botActual.getNombre() + " tira: " + c);
                }

                if (mejorCartaRonda == null) {
                    mejorCartaRonda = c;
                    ganadorDeRonda = actual;
                } else {
                    int comp = ArbitroRonda.compararCartas(c, mejorCartaRonda);
                    if (comp == 1) {
                        mejorCartaRonda = c;
                        ganadorDeRonda = actual;
                    } else if (comp == 0) {
                        if (mesa.getEquipoDe(jActual) != mesa.getEquipoDe(mesa.getJugador(ganadorDeRonda))) {
                            ganadorDeRonda = -1;
                        }
                    }
                }
            }

            if (ronda == 1) gestorEnvido.marcarResuelto();

            if (ganadorDeRonda == -1) {
                vista.mostrarAlerta("Baza parda.");
                victorias[ronda - 1] = 0;
            } else {
                Equipo eqG = mesa.getEquipoDe(mesa.getJugador(ganadorDeRonda));
                vista.mostrarAlerta("Ronda para " + eqG.getNombre() + " (" + mesa.getJugador(ganadorDeRonda).getNombre() + ")");
                int cod = (eqG == mesa.getEquipo1()) ? 1 : 2;
                victorias[ronda - 1] = cod;
                if (cod == 1) victEq1++; else victEq2++;
                turnoLanzador = ganadorDeRonda;
            }

            if (victEq1 == 2 || victEq2 == 2) break;
            if (ronda == 2 && victorias[0] == 0 && (victEq1 == 1 || victEq2 == 1)) break;
        }

        boolean manoEsEq1 = (mesa.getEquipoDe(mesa.getJugador(mesa.getIndiceMano())) == mesa.getEquipo1());
        int ganadorFinal = ArbitroRonda.definirGanadorMano(victorias, victEq1, victEq2, manoEsEq1 ? 1 : 2);
        int pts = gestorTruco.getPuntosEnJuego();

        if (ganadorFinal == 1) {
            vista.mostrarMensaje("\n>>> Ganó la mano del Truco " + mesa.getEquipo1().getNombre() + " (+" + pts + " pts).");
            mesa.getEquipo1().sumarPuntos(pts);
        } else {
            vista.mostrarMensaje("\n>>> Ganó la mano del Truco " + mesa.getEquipo2().getNombre() + " (+" + pts + " pts).");
            mesa.getEquipo2().sumarPuntos(pts);
        }
    }

    private void irseAlMazo(boolean tiroCarta, Equipo ganador) {
        int pts = gestorTruco.estaActivo() ? gestorTruco.getPuntosEnJuego() : (tiroCarta ? 1 : 2);
        vista.mostrarMensaje("\nTe fuiste al mazo.");
        vista.mostrarAlerta(ganador.getNombre() + " gana la mano (+" + pts + " pts).");
        ganador.sumarPuntos(pts);
    }

    // Canto iniciado por el Humano hacia los Rivales
    private boolean procesarCantoTrucoHumano(int ronda) {
        gestorTruco.proponerAumento(mesa.getEquipo1());
        JugadorBot r = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
        int t2 = mesa.getEquipo2().getMejorTantoEnvido();
        int t1 = mesa.getEquipo1().getMejorTantoEnvido();

        if (ronda == 1 && !gestorEnvido.isResuelto() && r.quiereAbrirEnvido(t2, false)) {
            vista.mostrarMensaje("Rival responde: ¡EL ENVIDO ESTÁ PRIMERO! Cantó: ¡ENVIDO!");
            arbitroEnvido.resolverIniciadoPorRival(t1, t2);
            gestorEnvido.marcarResuelto();
            vista.mostrarMensaje("\nQuedó pendiente tu canto de " + gestorTruco.getProximoCanto() + "...");
        }

        int proximoNivelNum = gestorTruco.getNivelActual().getPuntosQuerido() + 1;
        int resp = r.responderTruco(proximoNivelNum);

        if (resp == 1) {
            vista.mostrarMensaje("Rival responde: ¡QUIERO!");
            gestorTruco.aceptarAumento();
            gestorTruco.setEquipoConElQuiero(mesa.getEquipo2());
            return true;
        } else if (resp == 2) {
            vista.mostrarMensaje("Rival responde: ¡NO QUIERO!");
            int pts = gestorTruco.getPuntosRechazo();
            vista.mostrarMensaje("\n>>> " + mesa.getEquipo1().getNombre() + " gana los puntos del Truco por rechazo (+" + pts + " pts).");
            mesa.getEquipo1().sumarPuntos(pts);
            return false;
        } else {
            gestorTruco.aceptarAumento();
            vista.mostrarMensaje("Rival responde: ¡QUIERO Y " + gestorTruco.getProximoCanto() + "!");
            gestorTruco.proponerAumento(mesa.getEquipo2());
            int op = vista.pedirOpcion("¿Aceptás?", Arrays.asList("[1] Quiero", "[2] No Quiero"), 1, 2);
            if (op == 1) {
                gestorTruco.aceptarAumento();
                gestorTruco.setEquipoConElQuiero(mesa.getEquipo1());
                return true;
            } else {
                int pts = gestorTruco.getPuntosRechazo();
                vista.mostrarMensaje("\n>>> " + mesa.getEquipo2().getNombre() + " gana los puntos por rechazo (+" + pts + " pts).");
                mesa.getEquipo2().sumarPuntos(pts);
                return false;
            }
        }
    }

    // Canto iniciado por el Compañero Bot hacia los Rivales
    private boolean procesarCantoCompaneroBot(int ronda) {
        gestorTruco.proponerAumento(mesa.getEquipo1());
        JugadorBot r = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
        int proximoNivelNum = gestorTruco.getNivelActual().getPuntosQuerido() + 1;
        int resp = r.responderTruco(proximoNivelNum);

        if (resp == 1) {
            vista.mostrarMensaje("Rival responde: ¡QUIERO!");
            gestorTruco.aceptarAumento();
            gestorTruco.setEquipoConElQuiero(mesa.getEquipo2());
            return true;
        } else if (resp == 2) {
            vista.mostrarMensaje("Rival responde: ¡NO QUIERO!");
            int pts = gestorTruco.getPuntosRechazo();
            vista.mostrarMensaje("\n>>> " + mesa.getEquipo1().getNombre() + " gana los puntos del Truco por rechazo (+" + pts + " pts).");
            mesa.getEquipo1().sumarPuntos(pts);
            return false;
        } else {
            gestorTruco.aceptarAumento();
            vista.mostrarMensaje("Rival responde: ¡QUIERO Y " + gestorTruco.getProximoCanto() + "!");
            gestorTruco.proponerAumento(mesa.getEquipo2());
            int op = vista.pedirOpcion("El rival redobló. ¿Aceptás para tu equipo?", Arrays.asList("[1] Quiero", "[2] No Quiero"), 1, 2);
            if (op == 1) {
                gestorTruco.aceptarAumento();
                gestorTruco.setEquipoConElQuiero(mesa.getEquipo1());
                return true;
            } else {
                int pts = gestorTruco.getPuntosRechazo();
                vista.mostrarMensaje("\n>>> " + mesa.getEquipo2().getNombre() + " gana los puntos por rechazo (+" + pts + " pts).");
                mesa.getEquipo2().sumarPuntos(pts);
                return false;
            }
        }
    }

    // Canto iniciado por un Rival hacia nosotros (Humano responde por el equipo)
    private boolean procesarCantoTrucoBot(Equipo eqBot, int ronda) {
        gestorTruco.proponerAumento(eqBot);
        int t1 = mesa.getEquipo1().getMejorTantoEnvido();
        int t2 = mesa.getEquipo2().getMejorTantoEnvido();

        boolean opcionEnvidoPrimero = (ronda == 1 && !gestorEnvido.isResuelto());
        String proximoCanto = gestorTruco.getProximoCanto();
        boolean puedeSubir = (gestorTruco.getNivelActual() != GestorTruco.Nivel.RETRUCO && 
                              gestorTruco.getNivelActual() != GestorTruco.Nivel.VALE_CUATRO);

        List<String> opciones = new ArrayList<>();
        opciones.add("[1] Quiero");
        opciones.add("[2] No Quiero");
        if (puedeSubir) opciones.add("[3] " + proximoCanto);
        if (opcionEnvidoPrimero) opciones.add("[4] ¡El Envido está primero!");

        int maxOp = opcionEnvidoPrimero ? 4 : (puedeSubir ? 3 : 2);
        int r = vista.pedirOpcion("¿Qué respondés?", opciones, 1, maxOp);

        if (opcionEnvidoPrimero && r == 4) {
            vista.mostrarMensaje("Cantaste: ¡EL ENVIDO ESTÁ PRIMERO!");
            arbitroEnvido.resolverIniciadoPorHumano(1, t1, t2);
            gestorEnvido.marcarResuelto();

            List<String> opsPost = new ArrayList<>(Arrays.asList("[1] Quiero", "[2] No Quiero"));
            if (puedeSubir) opsPost.add("[3] " + gestorTruco.getProximoCanto());
            r = vista.pedirOpcion("\nAhora debés responder al Truco del rival:", opsPost, 1, puedeSubir ? 3 : 2);
        }

        if (r == 1) {
            gestorTruco.aceptarAumento();
            gestorTruco.setEquipoConElQuiero(mesa.getEquipo1());
            return true;
        } else if (r == 2) {
            int pts = gestorTruco.getPuntosRechazo();
            vista.mostrarMensaje("\n>>> " + eqBot.getNombre() + " gana los puntos del Truco por rechazo (+" + pts + " pts).");
            eqBot.sumarPuntos(pts); // Puntos para el bot rival, NO para nosotros
            return false;
        } else {
            gestorTruco.aceptarAumento();
            vista.mostrarMensaje("Cantaste: ¡" + gestorTruco.getProximoCanto() + "!");
            gestorTruco.proponerAumento(mesa.getEquipo1());
            JugadorBot rBot = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
            int proximoNivelNum = gestorTruco.getNivelActual().getPuntosQuerido() + 1;

            if (rBot.responderTruco(proximoNivelNum) == 1) {
                vista.mostrarMensaje("Rival responde: ¡QUIERO!");
                gestorTruco.aceptarAumento();
                gestorTruco.setEquipoConElQuiero(mesa.getEquipo2());
                return true;
            } else {
                vista.mostrarMensaje("Rival responde: ¡NO QUIERO!");
                int pts = gestorTruco.getPuntosRechazo();
                vista.mostrarMensaje("\n>>> " + mesa.getEquipo1().getNombre() + " gana los puntos por rechazo (+" + pts + " pts).");
                mesa.getEquipo1().sumarPuntos(pts);
                return false;
            }
        }
    }
}