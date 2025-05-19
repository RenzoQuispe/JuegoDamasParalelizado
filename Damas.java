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
        System.out.println("¡Bienvenido al juego de damas!");
        System.out.println("El juego comienza...");
        // Juego
    }
}
