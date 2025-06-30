import java.util.List;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class Damas {
 
    //Colores
    public static final String RESET = "\u001B[0m";
    public static final String ROJO = "\u001B[31m";
    public static final String VERDE = "\u001B[32m";
    public static final String AZUL = "\u001B[34m";
    public static final String CELESTE = "\u001B[36m";
    public static final String AMARILLO = "\u001B[33m";

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("-help") || args[0].equals("-h")) {
            mostrarAyuda();
            return;
        }

        switch (args[0]) {
            case "-jugar":
            case "-j":
                iniciarJuego();
                break;
            case "-notacion":
            case "-n":
                mostrarNotacion();
                break;    
            case "-version":
            case "-v":
                mostrarVersion();
                break;

            default:
                System.out.println("Opción no reconocida: " + args[0]);
                System.out.println("Usa -help para ver las opciones disponibles.");
        }
    }

    private static void mostrarAyuda() {
        System.out.println("¡Bienvenido al juego de damas!");
        System.out.println("Uso:");
        System.out.println("  java -jar Damas.jar [OPCIÓN]");
        System.out.println();
        System.out.println("Opciones:");
        System.out.println("  -jugar , -j        Inicia el juego");
        System.out.println("  -notacion , -n     Ver notacion de fichas");
        System.out.println("  -help , -h         Muestra esta ayuda");
        System.out.println("  -version , -v      Muestra la versión del programa");
    }

    private static void mostrarVersion() {
        System.out.println(" Juego Damas por consola - version 1.0");
    }
    private static void mostrarNotacion() {
        System.out.println("----------NOTACION----------");
        System.out.println("'B': Fichas blancas");
        System.out.println("'N': Fichas negras");
        System.out.println("' ' y '*' : casillas vacias (pero solo pueden moverse en las casillas donde es *)");
        System.out.println("'0' : Dama negra");
        System.out.println("'1' : Dama blanca");
    }

    private static void iniciarJuego() {
        System.out.println(VERDE+"----------JUGAR CHECKERS----------"+RESET);
        System.out.println("Escoge tus fichas: ");
        Scanner scanner = new Scanner(System.in);
        char colorJugador = elegirColor(scanner);
        char colorComputadora = (colorJugador == 'B') ? 'N' : 'B';
        System.out.println("Has elegido jugar con las fichas " + (colorJugador == 'B' ? "BLANCAS" : "NEGRAS"));
        System.out.println("Formato para mover: H3 a G4");
        System.out.println("Escribe 'salir' para terminar.");
        System.out.println(VERDE+"El juego comienza..."+RESET);
        final JugarDamas[] juego = new JugarDamas[1];   // CLASE DONDE ESTA LA LOGICA DE JUEGO
        SwingUtilities.invokeLater(() -> {
            juego[0] = new JugarDamas(colorJugador,colorComputadora);
        });

        // Esperar a que la GUI esté lista
        while (juego[0] == null) {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }

        // Si jugador es negro, computadora hace el primer movimiento
        if (colorJugador == 'N') {
            System.out.println("-------------------------------------------------");
            System.out.println("Movimientos posibles de computadora:");
            for(String mov: juego[0].generarMovimientosPosibles(colorComputadora)) {
                System.out.println(juego[0].convertirCoordenadasAPosicionTablero(mov));
            }
            System.out.println("-------------------------------------------------");
            System.out.println(CELESTE+"Computadora piensa..."+RESET);
            long inicio = System.currentTimeMillis();
            String movimientoComputadora = juego[0].moverComputadora();
            long fin = System.currentTimeMillis();     // Termina el cronómetro
            long tiempoEnMilisegundos = fin - inicio;
            System.out.println("-------------------------------------------------");
            if (movimientoComputadora != null) {
                System.out.println(CELESTE + "Movimiento de la Computadora: " + movimientoComputadora + RESET);
                System.out.println(CELESTE + "Tiempo pensado: " + tiempoEnMilisegundos +" ms" + RESET);
            } else {
                System.out.println(ROJO+"Computadora no pudo mover."+RESET);
            }
        }

        while (true) {
            // Verficar si hay movimientos posibles para el jugador
            List<String> movimientosJugador = juego[0].generarMovimientosPosibles(colorJugador);
            if(movimientosJugador.isEmpty()){
                System.out.println(ROJO+"Gano la Computadora"+RESET);
                break;
            }
            // Movimientos posibles del jugador
            System.out.println("-------------------------------------------------");
            System.out.println("Tus movimientos posibles:");
            for(String mov: juego[0].generarMovimientosPosibles(colorJugador)) {
                System.out.println(juego[0].convertirCoordenadasAPosicionTablero(mov));
            }
            System.out.println("-------------------------------------------------");
            // Movimiento del Jugador
            System.out.print("Ingresa tu movimiento: ");
            String movimientoJugador = scanner.nextLine().trim();
            if (movimientoJugador.equalsIgnoreCase("salir")) {
                System.out.println("Juego terminado.");
                break;
            }
            boolean exito = juego[0].moverJugador(movimientoJugador); // MOVIMIENTO
            if (!exito) {
                System.out.println(ROJO+"Movimiento inválido."+RESET);
                continue;
            }else{
                System.out.println(AMARILLO+"Tu movimiento es: "+movimientoJugador+RESET);
            }
            // Verificar si hay movimientos posibles para la computadora
            List<String> movimientosComputadora = juego[0].generarMovimientosPosibles(colorComputadora);
            if(movimientosComputadora.isEmpty()){
                System.out.println(ROJO+"Ganaste :D"+RESET);
                break;
            }
            // Movimientos posibles de Computadora
            System.out.println("-------------------------------------------------");
            System.out.println("Movimientos posibles de computadora:");
            for(String mov: juego[0].generarMovimientosPosibles(colorComputadora)) {
                System.out.println(juego[0].convertirCoordenadasAPosicionTablero(mov));
            }
            System.out.println("-------------------------------------------------");
            System.out.println(CELESTE+"Computadora piensa..."+RESET);
            //Computadora "piensa" su movimiento y toma la mejor decision      
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            long inicio_ = System.currentTimeMillis();
            String movimientoComputadora = juego[0].moverComputadora(); // MOVIMIENTO
            long fin_ = System.currentTimeMillis();
            long tiempoEnMilisegundos_ = fin_ - inicio_;
            System.out.println("-------------------------------------------------");
            if (movimientoComputadora != null) {
                System.out.println(CELESTE + "Movimiento de la Computadora: " + movimientoComputadora + RESET);
                System.out.println(CELESTE + "Tiempo pensado: " + tiempoEnMilisegundos_ +" ms" + RESET);
            } else {
                System.out.println(ROJO+"Computadora no pudo mover."+RESET);
            }
        }
    }

    private static char elegirColor(Scanner scanner) {
        char eleccion;
        while (true) {
            System.out.print("¿Con qué fichas quieres jugar? (B para blancas / N para negras): ");
            String entrada = scanner.nextLine().trim().toUpperCase();
            if (entrada.equals("B") || entrada.equals("N")) {
                eleccion = entrada.charAt(0);
                break;
            } else {
                System.out.println("Entrada inválida. Por favor escribe 'B' o 'N'.");
            }
        }
        return eleccion;
    }
}