package gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Colores y fuentes compartidos por todas las ventanas.
 * Es el ÚNICO lugar donde se definen: las ventanas solo usan estas constantes.
 */
public final class Estilo {

    // Estado del curso
    public static final Color COLOR_APROBADO = new Color(46, 125, 50);     // verde
    public static final Color COLOR_EN_RIESGO = new Color(255, 160, 0);    // ámbar
    public static final Color COLOR_DESAPROBADO = new Color(198, 40, 40);  // rojo

    // Fondo y texto general
    public static final Color COLOR_FONDO = new Color(245, 247, 250);
    public static final Color COLOR_TEXTO = new Color(33, 33, 33);

    // Fuentes
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FUENTE_NORMAL = new Font("SansSerif", Font.PLAIN, 14);

    private Estilo() {
        // No se instancia: solo contiene constantes
    }
}
