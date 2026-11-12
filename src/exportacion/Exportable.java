package exportacion;

import java.io.IOException;
import modelo.Estudiante;

/**
 * Contrato para exportar el reporte de un estudiante a un archivo
 * (CSV ahora, PDF si hay tiempo). Cada formato implementa esta interfaz.
 */
public interface Exportable {

    // Genera el reporte del estudiante en la ruta indicada
    void exportar(Estudiante estudiante, String ruta) throws IOException;
}