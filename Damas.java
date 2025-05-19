import java.util.Scanner;
import javax.swing.SwingUtilities;

public class Damas {

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
        System.out.println("  -help , -h         Muestra esta ayuda");
        System.out.println("  -version , -v      Muestra la versión del programa");
    }

    private static void mostrarVersion() {
        System.out.println("Damas versión 1.0");
    }

    private static void iniciarJuego() {
        System.out.println("Escoge tus fichas: ");
        Scanner scanner = new Scanner(System.in);
        char colorJugador = elegirColor(scanner);
        System.out.println("Has elegido jugar con las fichas " + (colorJugador == 'B' ? "BLANCAS" : "NEGRAS"));
        System.out.println("El juego comienza...");
        SwingUtilities.invokeLater(() -> {
            new JugarDamas(colorJugador);
        });
        System.out.println("¡Juego Iniciado!");


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
//javac Damas.java JugarDamas.java
//jar cfe Damas.jar Damas Damas.class JugarDamas.class icono.png