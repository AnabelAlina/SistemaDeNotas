package exportacion;

import calculos.EstadoCurso;
import calculos.EvaluadorCurso;
import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import evaluaciones.TrabajoContinua;
import gui.Estilo;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;

/**
 * Exporta el reporte de un estudiante como una página HTML:
 * una tabla por curso (evaluación, peso, estado, nota) y el estado del curso
 * pintado con el mismo color que se ve en la ventana de análisis.
 */
public class ExportadorHTML implements Exportable {

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
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"es\">");
            out.println("<head>");
            out.println("<meta charset=\"UTF-8\">");
            out.println("<title>Reporte de " + escapar(estudiante.getNombreCompleto()) + "</title>");
            out.println("<style>");
            out.println("body { font-family: sans-serif; background: " + hex(Estilo.COLOR_FONDO)
                    + "; color: " + hex(Estilo.COLOR_TEXTO) + "; margin: 24px; }");
            out.println("table { border-collapse: collapse; margin-bottom: 8px; }");
            out.println("th, td { border: 1px solid; padding: 4px 10px; text-align: left; }");
            out.println(".estado { color: white; padding: 2px 8px; border-radius: 4px; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Reporte de notas</h1>");
            out.println("<p>Estudiante: <b>" + escapar(estudiante.getNombreCompleto())
                    + "</b> &middot; CUI: " + escapar(estudiante.getCodigo()) + "</p>");

            for (Ciclo ciclo : estudiante.getCiclos()) {
                out.println("<h2>Ciclo " + escapar(ciclo.getNombre()) + "</h2>");
                for (Curso curso : ciclo.getCursos()) {
                    out.println("<h3>" + escapar(curso.getNombre()) + "</h3>");
                    out.println("<table>");
                    out.println("<tr><th>Evaluación</th><th>Peso (%)</th><th>Estado</th><th>Nota</th></tr>");
                    for (Evaluacion ev : curso.getEvaluaciones()) {
                        fila(out, ev.getNombre(), String.valueOf(ev.getPeso()),
                                ev.isRendida() ? "Rendida" : "Pendiente",
                                ev.isRendida() ? String.valueOf(ev.getNota()) : "");
                        if (ev instanceof Continua) {
                            for (TrabajoContinua t : ((Continua) ev).getTrabajos()) {
                                fila(out, "&nbsp;&nbsp;– " + escapar(t.getNombre()), String.valueOf(t.getPeso()),
                                        t.isRendido() ? "Rendida" : "Pendiente",
                                        t.isRendido() ? String.valueOf(t.getNota()) : "");
                            }
                        }
                    }
                    out.println("</table>");

                    String parcial = ExportadorCSV.esParcial(curso) ? "parcial" : "final";
                    String promedio = curso.tieneNotas() ? ExportadorCSV.formato(curso.calcularPromedio()) : "sin notas";
                    out.println("<p>Promedio del curso: <b>" + promedio + "</b> (" + parcial + ")</p>");

                    EstadoCurso estado = evaluador.estadoDe(curso);
                    if (estado == null) {
                        out.println("<p>Estado del curso: sin notas</p>");
                    } else {
                        out.println("<p>Estado del curso: <span class=\"estado\" style=\"background: "
                                + hex(colorDe(estado)) + "\">" + escapar(estado.getTexto()) + "</span></p>");
                    }
                }
                out.println("<p>Promedio del ciclo " + escapar(ciclo.getNombre()) + ": <b>"
                        + ExportadorCSV.formato(ciclo.calcularPromedio()) + "</b></p>");
            }
            out.println("<h2>Promedio acumulado: " + ExportadorCSV.formato(estudiante.calcularPromedio()) + "</h2>");
            out.println("</body>");
            out.println("</html>");

            // PrintWriter no lanza excepciones: se revisan aquí para avisar si algo falló al escribir
            if (out.checkError()) {
                throw new IOException("No se pudo escribir el archivo " + ruta);
            }
        }
    }

    // Una fila de la tabla. El primer texto ya viene escapado cuando trae "&nbsp;"
    private void fila(PrintWriter out, String evaluacion, String peso, String estado, String nota) {
        String primero = evaluacion.startsWith("&nbsp;") ? evaluacion : escapar(evaluacion);
        out.println("<tr><td>" + primero + "</td><td>" + peso + "</td><td>" + estado
                + "</td><td>" + nota + "</td></tr>");
    }

    // Mismos colores de Estilo que usa la ventana de análisis
    private Color colorDe(EstadoCurso estado) {
        if (estado == EstadoCurso.APROBADO) {
            return Estilo.COLOR_APROBADO;
        }
        if (estado == EstadoCurso.VAS_APROBANDO) {
            return Estilo.COLOR_VAS_APROBANDO;
        }
        if (estado == EstadoCurso.EN_RIESGO) {
            return Estilo.COLOR_EN_RIESGO;
        }
        return Estilo.COLOR_DESAPROBADO;
    }

    // Color de Java -> "#rrggbb"
    private String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // Evita que un nombre con & < > rompa la página
    private String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}