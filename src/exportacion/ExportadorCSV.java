package exportacion;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import evaluaciones.Evaluacion;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;

/**
 * Exporta el reporte de un estudiante a un archivo CSV:
 * una fila por evaluación, con ciclo, curso, tipo, peso y nota.
 * Usa ';' como separador porque Excel en español lo abre en columnas.
 */
public class ExportadorCSV implements Exportable {

    private static final String SEP = ";";

    @Override
    public void exportar(Estudiante estudiante, String ruta) throws IOException {
        if (estudiante == null) {
            throw new IllegalArgumentException("No hay estudiante para exportar.");
        }
        Path destino = Paths.get(ruta);
        // Si la carpeta no existe, la creamos
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        try (BufferedWriter out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            // BOM: hace que Excel muestre bien las tildes
            out.write("\uFEFF");

            // 1. Encabezado
            escribir(out, "Ciclo", "Curso", "Tipo", "Evaluacion", "Peso (%)", "Estado", "Nota");

            // 2. Una fila por evaluación, recorriendo ciclos -> cursos -> evaluaciones
            for (Ciclo ciclo : estudiante.getCiclos()) {
                for (Curso curso : ciclo.getCursos()) {
                    for (Evaluacion ev : curso.getEvaluaciones()) {
                        // Si no se rindió, la nota queda vacía en vez de 0
                        String estado = ev.isRendida() ? "Rendida" : "Pendiente";
                        String nota = ev.isRendida() ? String.valueOf(ev.getNota()) : "";
                        escribir(out, ciclo.getNombre(), curso.getNombre(), ev.getTipo(),
                                ev.getNombre(), String.valueOf(ev.getPeso()), estado, nota);
                    }
                }
            }
        }
    }

    // Une los campos con ';' y escribe la línea
    private void escribir(BufferedWriter out, String... campos) throws IOException {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                linea.append(SEP);
            }
            linea.append(proteger(campos[i]));
        }
        out.write(linea.toString());
        out.newLine();
    }

    // Si el texto trae ';', comillas o saltos de línea, lo encierra entre comillas (regla del CSV)
    private String proteger(String texto) {
        if (texto.contains(SEP) || texto.contains("\"") || texto.contains("\n")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }
}