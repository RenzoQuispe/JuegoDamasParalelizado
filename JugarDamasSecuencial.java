import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class JugarDamasSecuencial extends JFrame implements JugarDamas{

    private char colorJugador;
    private char colorComputadora;
    private char[][] tablero = new char[8][8];
    /*
    'B': Fichas blancas
    'N': Fichas negras
    ' ' y '*' : casillas vacía(pero solo pueden moverse en las csaillas donde es *)
    '0' : dama negra
    '1' : dama blanca
    */
    
    public JugarDamasSecuencial(char colorJugador, char colorComputadora) {
        
        this.colorJugador = colorJugador;
        this.colorComputadora = colorComputadora;
        inicializarTablero();

        setTitle("Damas - Jugador con fichas " + (colorJugador == 'B' ? "BLANCAS" : "NEGRAS"));
        setSize(400, 370);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla

        // Establecer el ícono de la ventana
        Image icono = Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/icono.png"));
        setIconImage(icono);
        
        // Tablero
        JTextArea areaTexto = new JTextArea(generarTextoTablero());
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 22));
        add(new JScrollPane(areaTexto));

        setVisible(true);
    }

    private void inicializarTablero() {
        // Colocar piezas en filas iniciales
        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                if ((fila + col) % 2 == 1) {
                    if (fila < 3) {
                        tablero[fila][col] = 'N'; // negras arriba
                    } else if (fila > 4) {
                        tablero[fila][col] = 'B'; // blancas abajo
                    } else {
                        tablero[fila][col] = '*';
                    }
                } else {
                    tablero[fila][col] = ' ';
                }
            }
        }
    }

    public String generarTextoTablero() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n  --- TABLERO DE DAMAS ---\n");

        if (colorJugador == 'B') {
            for (int fila = 0; fila < 8; fila++) {
                sb.append("  ");
                for (int col = 0; col < 8; col++) {
                    sb.append("[").append(tablero[fila][col]).append("]");
                }
                sb.append(" ").append(8 - fila).append("\n");
            }
            sb.append("  ");
            for (char c = 'A'; c <= 'H'; c++) {
                sb.append(" ").append(c).append(" ");
            }
            sb.append("\n");

        } else if (colorJugador == 'N') {
            for (int fila = 7; fila >= 0; fila--) {
                sb.append("  ");
                for (int col = 7; col >= 0; col--) {
                    sb.append("[").append(tablero[fila][col]).append("]");
                }
                sb.append(" ").append(8 -fila).append("\n");
            }
            sb.append("  ");
            for (char c = 'H'; c >= 'A'; c--) {
                sb.append(" ").append(c).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public boolean moverJugador(String movimiento) {
        List<String> movimientos = generarMovimientosPosibles(colorJugador);
        List<String> movimientosProcesados = new ArrayList<>();
        for (String mov : movimientos) {
            String movProcesado = convertirCoordenadasAPosicionTablero(mov);
            movimientosProcesados.add(movProcesado);
        }
        if(movimientosProcesados.contains(movimiento)){//A3 a B4
            //hacer movimiento, actualizar tablero
            int filaOrigen = 8 - Character.getNumericValue(movimiento.charAt(1));
            int columnaOrigen = movimiento.charAt(0)-'A';
            int filaDestino = 8 - Character.getNumericValue(movimiento.charAt(6));
            int columnaDestino = movimiento.charAt(5)-'A';
            
            String movimientoCoordenadas = String.valueOf(filaOrigen)+String.valueOf(columnaOrigen)+ " a "+String.valueOf(filaDestino)+String.valueOf(columnaDestino);
            tablero = aplicarMovimiento(tablero, movimientoCoordenadas,colorJugador);
            //actualizar vista del tablero
            actualizarVista();
            return true;
        }else{
            return false;
        }
    }

    public String moverComputadora() {
        List<String> movimientos = generarMovimientosPosibles(colorComputadora);
        if (movimientos.isEmpty()) return "No hay movimientos posibles";
    
        MovimientoEvaluado mejor = null;
    
        for (String mov : movimientos) {
            System.out.println("Evaluando :" + convertirCoordenadasAPosicionTablero(mov));
    
            // Simular el movimiento
            char[][] copiaTablero = copiarTablero(tablero);
            char[][] movSimulacion = aplicarMovimiento(copiaTablero, mov, colorComputadora);
    
            // Evaluar puntaje y riesgo
            int puntaje = evaluarTablero(movSimulacion, colorComputadora);
            System.out.println(" - Diferencia puntaje: " + puntaje);
    
            int riesgo = evaluarRiesgo(movSimulacion, colorComputadora);
            System.out.println(" - Riesgo: " + riesgo);
    
            double puntajeFinal = puntaje - 1.5 * riesgo;
            System.out.println(" - Puntaje final: " + puntajeFinal);
    
            // Guardar si es el mejor hasta el momento
            if (mejor == null || puntajeFinal > mejor.getScore()) {
                mejor = new MovimientoEvaluado(mov, puntajeFinal);
            }
        }
    
        if (mejor == null) return "No hay movimientos posibles";
    
        // Aplicar el mejor movimiento al tablero real
        tablero = aplicarMovimiento(tablero, mejor.getMovimiento(), colorComputadora);
        actualizarVista();
    
        return convertirCoordenadasAPosicionTablero(mejor.getMovimiento());
    }    

    public String convertirCoordenadasAPosicionTablero(String movimiento) { // para convertir los movimientos de tipo "21 a 32 a 45" o "54 a 56" a ubicacion de tablero (A1,C3,F2,D6,etc)
        List<String> listaCoordenadas = new ArrayList<>();
        String[] partes = movimiento.split("\\s*a\\s*"); // divide por 'a' con posibles espacios
        for (String parte : partes) {
            if (parte.matches("\\d{2}")) { // asegura que tenga 2 dígitos
                listaCoordenadas.add(parte);
            }
        }
        //Procesar esos numeros
        List<String>  listaCoordenadasProcesada = new ArrayList<>();
        for(String coordenadas: listaCoordenadas){
            int fila = 8 - Character.getNumericValue(coordenadas.charAt(0));
            char columna =(char) ( 'A' + Character.getNumericValue(coordenadas.charAt(1)));
            String coordenadaProcesada = "" + columna + fila;
            listaCoordenadasProcesada.add(coordenadaProcesada); 
        }
        
        //Concatenarlos
        StringBuilder stringFinal = new StringBuilder();
        for (int i = 0; i < listaCoordenadasProcesada.size(); i++) {
            stringFinal.append(listaCoordenadasProcesada.get(i));
            if (i < listaCoordenadasProcesada.size() - 1) {
                stringFinal.append(" a ");
            }
        }
        return stringFinal.toString();
    }
    private void actualizarVista() {
        JTextArea area = (JTextArea)((JScrollPane)getContentPane().getComponent(0)).getViewport().getView();
        area.setText(generarTextoTablero());
    }
    /*
    Funciones para evaluar los movimientos posibles 
    */
    public int evaluarTablero(char[][] tablero, char colorComputadora) { //medicion de la ventaja material
        int score = 0;
        char fichaNormal = colorComputadora;
        char dama = (colorComputadora == 'B') ? '1' : '0';
        char fichaNormalJugador = (colorComputadora == 'B') ? 'N' : 'B';
        char damaJugador = (colorComputadora == 'B') ? '0' : '1';
    
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char p = tablero[i][j];
                if (p == fichaNormal) score += 3;
                else if (p == dama) score += 5;
                else if (p == fichaNormalJugador) score -= 3;
                else if (p == damaJugador) score -= 5;
            }
        }
        return score;
    }
    public int evaluarRiesgo(char[][] tablero, char colorComputadora) {
        int riesgo = 0;
        char enemigo = (colorComputadora == 'B') ? 'N' : 'B';
        char damaEnemiga = (enemigo == 'B') ? '1' : '0';
        
        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                char pieza = tablero[fila][col];
                if (pieza == enemigo || pieza == damaEnemiga) {
                    if (pieza == enemigo) {
                        riesgo += buscarCapturasNormalesDesde(tablero, fila, col, colorComputadora).size();
                    } else {
                        riesgo += buscarCapturasDamaDesde(tablero, fila, col, colorComputadora).size();
                    }
                }
            }
        }

        return riesgo;
    }
    private List<String> buscarCapturasNormalesDesde(char[][] tab, int fila, int col, char objetivo) {
        List<String> capturas = new ArrayList<>();
        int[] dFila = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};
        char damaObjetivo = (objetivo == 'B') ? '1' : '0';

        for (int i = 0; i < 4; i++) {
            int midFila = fila + dFila[i];
            int midCol = col + dCol[i];
            int destFila = fila + 2 * dFila[i];
            int destCol = col + 2 * dCol[i];

            if (dentroTablero(destFila, destCol)
                && (tab[midFila][midCol] == objetivo || tab[midFila][midCol] == damaObjetivo)
                && tab[destFila][destCol] == '*') {
                capturas.add("" + fila + col + " a " + destFila + destCol);
            }
        }
        return capturas;
    }

    private List<String> buscarCapturasDamaDesde(char[][] tab, int fila, int col, char objetivo) {
        List<String> capturas = new ArrayList<>();
        char damaObjetivo = (objetivo == 'B') ? '1' : '0';
        int[] dFila = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};

        for (int dir = 0; dir < 4; dir++) {
            int f = fila + dFila[dir];
            int c = col + dCol[dir];
            boolean enemigoVisto = false;

            while (dentroTablero(f, c)) {
                if (tab[f][c] == '*') {
                    if (enemigoVisto) {
                        capturas.add("" + fila + col + " a " + f + c);
                        break;
                    }
                } else if (tab[f][c] == objetivo || tab[f][c] == damaObjetivo) {
                    if (enemigoVisto) break;
                    enemigoVisto = true;
                } else break;

                f += dFila[dir];
                c += dCol[dir];
            }
        }

        return capturas;
    }
    // funcion para aplicar movimiento a un tablero(original o copia)
    public char[][] aplicarMovimiento(char[][] tablero, String mov, char color) {
        char[][] copia = copiarTablero(tablero);
        String[] partes = mov.split(" a ");
        
        for (int i = 0; i < partes.length - 1; i++) {
            String desde = partes[i];
            String hacia = partes[i + 1];

            int filaDesde = Character.getNumericValue(desde.charAt(0));
            int colDesde = Character.getNumericValue(desde.charAt(1));
            int filaHacia = Character.getNumericValue(hacia.charAt(0));
            int colHacia = Character.getNumericValue(hacia.charAt(1));

            char ficha = copia[filaDesde][colDesde];
            copia[filaDesde][colDesde] = '*';

            // si es una captura, eliminar ficha intermedia
            if (Math.abs(filaDesde - filaHacia) == 2) {
                int filaMedio = (filaDesde + filaHacia) / 2;
                int colMedio = (colDesde + colHacia) / 2;
                copia[filaMedio][colMedio] = '*';
            }

            copia[filaHacia][colHacia] = ficha;
            // posible coronacion negras
            if(filaHacia==7 && color == 'N'){
                copia[filaHacia][colHacia] = '0';
            }
            // posible coronacion blancas
            if (filaHacia==0 && color == 'B') {
                copia[filaHacia][colHacia] = '1';
            }
        }

        return copia;
    }

    /*
        Funciones para generar todos los movimiento posibles de la computadora o jugador
    */
    public List<String> generarMovimientosPosibles(char color) {
        List<String> capturas = new ArrayList<>();
        List<String> movimientosSimples = new ArrayList<>();

        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                char ficha = tablero[fila][col];
                boolean esDama = (ficha == '0' && color == 'N') || (ficha == '1' && color == 'B');

                if (ficha == color || (esDama)) {
                    if (esDama) { // encuentra una dama
                        capturas.addAll(capturasDamaMaximas(fila,col,color,tablero));
                        if (capturas.isEmpty()) {
                            movimientosSimples.addAll(buscarMovimientosDama(fila, col,color));
                        }
                    } else {// encuentra ficha normal
                        capturas.addAll(buscarCapturasNormales(fila, col,color));
                        if (capturas.isEmpty()) {
                            movimientosSimples.addAll(buscarMovimientosNormales(fila, col,color));
                        }
                    }
                }
            }
        }
        return !capturas.isEmpty() ? capturas : movimientosSimples;
    }

    private List<String> buscarMovimientosNormales(int fila, int col,char color) {
        List<String> movimientos = new ArrayList<>();
        int[] dFila = {1, 1}; // usa fichas negras
        if (color == 'B') { // usa fichas blancas
            dFila = new int[] {-1, -1}; 
        }
        int[] dCol = {-1, 1};

        for (int i = 0; i < 2; i++) {
            int nuevaFila = fila + dFila[i];
            int nuevaCol = col + dCol[i];
            if (dentroTablero(nuevaFila, nuevaCol) && tablero[nuevaFila][nuevaCol] == '*') {
                movimientos.add("" + fila + col + " a " + nuevaFila + nuevaCol);
            }
        }
        return movimientos;
    }

    private List<String> buscarCapturasNormales(int fila, int col,char color) {
        List<String> capturas = new ArrayList<>();
        buscarCapturasRec(fila, col,new boolean[8][8], "", capturas,color);
        return capturas;
    }

    private void buscarCapturasRec(int fila, int col, boolean[][] visitado,String camino, List<String> capturas,char color) {
        boolean capturaEncontrada = false;
        int[] dFila = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};
        char oponente = (color== 'B') ? 'N' : 'B';
        char damaOponente = (color == 'B') ? '0' : '1';

        for (int i = 0; i < 4; i++) {
            int filaMid = fila + dFila[i];
            int colMid = col + dCol[i];
            int filaDestino = fila + 2 * dFila[i];
            int colDestino = col + 2 * dCol[i];

            if (dentroTablero(filaDestino, colDestino) &&
                (tablero[filaMid][colMid] == oponente || tablero[filaMid][colMid] == damaOponente) &&
                tablero[filaDestino][colDestino] == '*' && !visitado[filaMid][colMid]) {

                visitado[filaMid][colMid] = true;

                char[][] copia = copiarTablero(tablero);
                copia[fila][col] = '*';
                copia[filaMid][colMid] = '*';
                copia[filaDestino][colDestino] = color;

                String nuevoCamino = camino.isEmpty() ?
                    ("" + fila + col + " a " + filaDestino + colDestino) :
                    (camino + " a " + filaDestino + colDestino);

                buscarCapturasRec(filaDestino, colDestino, visitado, nuevoCamino, capturas,color);
                capturaEncontrada = true;
                visitado[filaMid][colMid] = false;
            }
        }

        if (!capturaEncontrada && !camino.isEmpty()) {
            capturas.add(camino);
        }
    }

    private List<String> buscarMovimientosDama(int fila, int col,char color) {
        List<String> movimientos = new ArrayList<>();
        int[] dFila = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};
        if ((tablero[fila][col] == '0' && color== 'N') || (tablero[fila][col] == '1' && color== 'B')) {
            for (int i = 0; i < 4; i++) {
                int f = fila + dFila[i];
                int c = col + dCol[i];
                while (dentroTablero(f, c) && tablero[f][c] == '*') {
                    movimientos.add("" + fila + col + " a " + f + c);
                    f += dFila[i];
                    c += dCol[i];
                }
            }            
        }
        return movimientos;
    }

    // capturas posibles de damas
    public List<String> capturasDamaMaximas(int fila, int col, char color, char[][] tablero) {
        List<String> todas = buscarCapturasDamaMultiples(fila, col, color, "", tablero);
    
        int maxCapturas = -1;
        List<String> filtradas = new ArrayList<>();
    
        for (String s : todas) {
            int capturas = s.split(" a ").length - 1;
            if (capturas > maxCapturas) {
                maxCapturas = capturas;
                filtradas.clear();
                filtradas.add(s);
            } else if (capturas == maxCapturas) {
                filtradas.add(s);
            }
        }
    
        return filtradas;
    }
    private List<String> buscarCapturasDamaMultiples(int fila, int col, char color, String camino, char[][] tableroActual) {
        List<String> secuencias = new ArrayList<>();
        char oponente = (color == 'B') ? 'N' : 'B';
        char damaOponente = (color == 'B') ? '0' : '1';
        int[] dFila = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};
        boolean capturaRealizada = false;
    
        for (int dir = 0; dir < 4; dir++) {
            int f = fila + dFila[dir];
            int c = col + dCol[dir];
    
            // buscar primer enemigo adyacente
            if (!dentroTablero(f, c)) continue;
            if (tableroActual[f][c] != oponente && tableroActual[f][c] != damaOponente) continue;
    
            // buscar casillas vacias luego del enemigo
            int f2 = f + dFila[dir];
            int c2 = c + dCol[dir];
    
            while (dentroTablero(f2, c2) && tableroActual[f2][c2] == '*') {
                // simular movimiento sobre copia del tablero
                char[][] nuevoTablero = copiarTablero(tableroActual);
                nuevoTablero[fila][col] = '*';
                nuevoTablero[f][c] = '*';
                nuevoTablero[f2][c2] = tableroActual[fila][col];
    
                String nuevoCamino = (camino.isEmpty() ? fila + "" + col : camino) + " a " + f2 + c2;
    
                // recursion - continuar desde nueva posición
                List<String> continuaciones = buscarCapturasDamaMultiples(f2, c2, color, nuevoCamino, nuevoTablero);
                if (continuaciones.isEmpty()) {
                    secuencias.add(nuevoCamino);
                } else {
                    secuencias.addAll(continuaciones);
                }
    
                capturaRealizada = true;
                f2 += dFila[dir];
                c2 += dCol[dir];
            }
        }
    
        if (!capturaRealizada && !camino.isEmpty()) {
            secuencias.add(camino);
        }
    
        return secuencias;
    }
            
    //Verifica si una posicion esta dentro del tablero
    private boolean dentroTablero(int fila, int col) {
        return fila >= 0 && fila < 8 && col >= 0 && col < 8;
    }

    private char[][] copiarTablero(char[][] original) {
        char[][] copia = new char[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(original[i], 0, copia[i], 0, 8);
        }
        return copia;
    }

}
    