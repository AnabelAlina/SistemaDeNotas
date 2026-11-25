package persistencia;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Bitácora de auditoría en modo añadir (PrintWriter sobre FileWriter con append = true).
 * Registra cada acción relevante con fecha y hora sin lanzar excepciones.
 */
public class Bitacora {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String rutaArchivo;

    public Bitacora(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta de la bitácora no puede estar vacía.");
        }
        this.rutaArchivo = rutaArchivo.trim();
    }

    public boolean registrar(String mensaje) {
        try {
            File archivo = new File(rutaArchivo);
            File carpetaPadre = archivo.getParentFile();
            if (carpetaPadre != null && !carpetaPadre.exists()) {
                carpetaPadre.mkdirs();
            }
            String marcaTiempo = LocalDateTime.now().format(FORMATO);
            String texto = (mensaje == null) ? "" : mensaje.replace("\n", " ").replace("\r", " ");
            try (PrintWriter out = new PrintWriter(new FileWriter(archivo, StandardCharsets.UTF_8, true))) {
                out.println(marcaTiempo + " | " + texto);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}