package exportacion;

import calculos.EstadoCurso;
import calculos.EvaluadorCurso;
import calculos.NotasEfectivas;
import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import evaluaciones.TrabajoContinua;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;

/**
 * Exporta el reporte de un estudiante a un archivo CSV.
 * Una fila por evaluación; debajo de cada continua, una fila por trabajo;
 * al final de cada curso, su promedio y su estado; luego el promedio del ciclo
 * y, al final del archivo, el promedio acumulado.
 * Usa ';' como separador porque Excel en español lo abre en columnas.
 */
public class ExportadorCSV implements Exportable {

    private static final String SEP = ";";

    @Override
    public void exportar(Estudiante estudiante, String ruta) throws IOException {
        if (estudiante == null) {
            throw new IllegalArgumentException("No hay estudiante para exportar.");
        }
        File destino = new File(ruta);
        // Si la carpeta no existe, la creamos
        File carpeta = destino.getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }
        EvaluadorCurso evaluador = new EvaluadorCurso();

        try (PrintWriter out = new PrintWriter(new FileWriter(destino, StandardCharsets.UTF_8))) {
            // BOM: hace que Excel muestre bien las tildes
            out.print("\uFEFF");

            // 1. Encabezado
            escribir(out, "Ciclo", "Curso", "Tipo", "Evaluacion", "Peso (%)", "Estado", "Nota");

            // 2. Ciclos -> cursos -> evaluaciones (y trabajos debajo de cada continua)
            for (Ciclo ciclo : estudiante.getCiclos()) {
                for (Curso curso : ciclo.getCursos()) {
                    for (Evaluacion ev : curso.getEvaluaciones()) {
                        // Si no se rindió, la nota queda vacía en vez de 0
                        String estado = ev.isRendida() ? "Rendida" : "Pendiente";
                        String nota = ev.isRendida() ? String.valueOf(ev.getNota()) : "";
                        escribir(out, ciclo.getNombre(), curso.getNombre(), ev.getTipo(),
                                ev.getNombre(), String.valueOf(ev.getPeso()), estado, nota);
                        if (ev instanceof Continua) {
                            for (TrabajoContinua t : ((Continua) ev).getTrabajos()) {
                                escribir(out, ciclo.getNombre(), curso.getNombre(), "Trabajo",
                                        t.getNombre(), String.valueOf(t.getPeso()),
                                        t.isRendido() ? "Rendida" : "Pendiente",
                                        t.isRendido() ? String.valueOf(t.getNota()) : "");
                            }
                        }
                    }
                    // Resumen del curso: promedio (parcial o final) y estado
                    String promedio = curso.tieneNotas() ? formato(curso.calcularPromedio()) : "";
                    escribir(out, ciclo.getNombre(), curso.getNombre(), "Resumen",
                            "Promedio del curso", "", esParcial(curso) ? "Parcial" : "Final", promedio);
                    EstadoCurso estadoCurso = evaluador.estadoDe(curso);
                    escribir(out, ciclo.getNombre(), curso.getNombre(), "Resumen",
                            "Estado del curso", "", estadoCurso == null ? "Sin notas" : estadoCurso.getTexto(), "");
                }
                // Resumen del ciclo
                escribir(out, ciclo.getNombre(), "", "Resumen", "Promedio del ciclo", "", "",
                        formato(ciclo.calcularPromedio()));
            }

            // 3. Promedio acumulado de todo el estudiante
            escribir(out, "", "", "Resumen", "Promedio acumulado", "", "", formato(estudiante.calcularPromedio()));

            // PrintWriter no lanza excepciones: se revisan aquí para avisar si algo falló al escribir
            if (out.checkError()) {
                throw new IOException("No se pudo escribir el archivo " + ruta);
            }
        }
    }

    // El promedio es parcial si falta rendir algo (sin contar el sustitutorio)
    // o si hay una continua con trabajos por agregar o por calificar.
    // Sin "private" para que ExportadorHTML (mismo paquete) use la misma regla.
    static boolean esParcial(Curso curso) {
        for (Evaluacion ev : curso.getEvaluaciones()) {
            if (NotasEfectivas.esOpcional(ev)) {
                continue;
            }
            if (!ev.isRendida()) {
                return true;
            }
            if (ev instanceof Continua && !((Continua) ev).isCompleta()) {
                return true;
            }
        }
        return false;
    }

    // Dos decimales con punto, igual que en las ventanas
    static String formato(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    // Une los campos con ';' y escribe la línea
    private void escribir(PrintWriter out, String... campos) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                linea.append(SEP);
            }
            linea.append(proteger(campos[i]));
        }
        out.println(linea.toString());
    }

    // Si el texto trae ';', comillas o saltos de línea, lo encierra entre comillas (regla del CSV)
    private String proteger(String texto) {
        if (texto.contains(SEP) || texto.contains("\"") || texto.contains("\n")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }
}