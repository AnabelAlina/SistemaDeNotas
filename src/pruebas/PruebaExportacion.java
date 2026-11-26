package pruebas;

import evaluaciones.Continua;
import evaluaciones.TrabajoContinua;
import exportacion.ExportadorCSV;
import exportacion.ExportadorHTML;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;
import plan.PlanEstudios;

/**
 * Pruebas de los reportes: CSV (trabajos y filas de resumen) y HTML (tabla y texto escapado).
 */
public class PruebaExportacion {
    private static int fallos = 0;

    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        boolean ok = esperado.equals(obtenido);
        if (!ok) fallos++;
        System.out.println((ok ? "OK    " : "FALLO ") + descripcion + " -> esperado " + esperado + ", obtenido " + obtenido);
    }

    // Lee todas las líneas de un archivo con Scanner y FileReader (UTF-8)
    private static ArrayList<String> leerLineas(File archivo) throws IOException {
        ArrayList<String> lineas = new ArrayList<>();
        Scanner lector = new Scanner(new FileReader(archivo, StandardCharsets.UTF_8));
        while (lector.hasNextLine()) {
            lineas.add(lector.nextLine());
        }
        lector.close();
        return lineas;
    }

    // Cuenta las filas del CSV cuyo tercer campo (Tipo) es "tipo"
    private static int contarTipo(ArrayList<String> lineas, String tipo) {
        int cantidad = 0;
        for (String linea : lineas) {
            String[] campos = linea.split(";", -1);
            if (campos.length > 2 && campos[2].equals(tipo)) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // Estudiante de ejemplo: Examen 1 = 15 y una continua con 2 trabajos (solo el primero calificado)
    private static Estudiante estudianteEjemplo(String nombre) throws Exception {
        Estudiante estudiante = new Estudiante(nombre, "Quispe", "71234567", "ana@unsa.edu.pe", "20261111", "Sistemas");
        Ciclo ciclo = new Ciclo("2026-B", 2026);
        Curso curso = PlanEstudios.crearCurso("C1", "Curso de prueba", 4, 14,
            new double[]{25, 25, 20}, new double[]{10, 10, 10});
        curso.getEvaluaciones().get(1).setNota(15); // Examen 1
        Continua continua = (Continua) curso.getEvaluaciones().get(0);
        TrabajoContinua t1 = new TrabajoContinua("Tarea 1", 60, false);
        TrabajoContinua t2 = new TrabajoContinua("Tarea 2", 40, false);
        continua.agregarTrabajo(t1);
        continua.agregarTrabajo(t2);
        t1.setNota(18);
        ciclo.agregarCurso(curso);
        estudiante.agregarCiclo(ciclo);
        return estudiante;
    }

    public static void main(String[] args) throws Exception {
        // --- CSV ---
        Estudiante ana = estudianteEjemplo("Ana");
        File csv = File.createTempFile("reporte", ".csv");
        new ExportadorCSV().exportar(ana, csv.getPath());
        ArrayList<String> lineas = leerLineas(csv);

        verificar("csv: empieza con el BOM", true, lineas.get(0).startsWith("\uFEFF"));
        verificar("csv: encabezado de 7 columnas", 7, lineas.get(0).split(";", -1).length);
        verificar("csv: 2 filas de Trabajo", 2, contarTipo(lineas, "Trabajo"));
        verificar("csv: 4 filas de Resumen (curso x2, ciclo, acumulado)", 4, contarTipo(lineas, "Resumen"));
        verificar("csv: el trabajo va debajo de su continua", true,
            lineas.get(1).contains("Continua 1") && lineas.get(2).contains("Tarea 1"));
        verificar("csv: promedio parcial", true, lineas.contains("2026-B;Curso de prueba;Resumen;Promedio del curso;;Parcial;15.86"));
        verificar("csv: estado del curso", true, lineas.contains("2026-B;Curso de prueba;Resumen;Estado del curso;;Vas aprobando;"));
        verificar("csv: promedio acumulado al final", true, lineas.get(lineas.size() - 1).startsWith(";;Resumen;Promedio acumulado"));
        csv.delete();

        // --- HTML ---
        Estudiante conSigno = estudianteEjemplo("Ana & Luz");
        File html = File.createTempFile("reporte", ".html");
        new ExportadorHTML().exportar(conSigno, html.getPath());
        StringBuilder todo = new StringBuilder();
        for (String linea : leerLineas(html)) {
            todo.append(linea).append("\n");
        }
        String pagina = todo.toString();

        verificar("html: declara UTF-8", true, pagina.contains("<meta charset=\"UTF-8\">"));
        verificar("html: tiene una tabla", true, pagina.contains("<table"));
        verificar("html: el & sale como &amp;", true, pagina.contains("Ana &amp; Luz"));
        verificar("html: no queda el & sin escapar", false, pagina.contains("Ana & Luz"));
        verificar("html: estado con su color (turquesa)", true, pagina.contains("#009688") && pagina.contains("Vas aprobando"));
        verificar("html: trae el trabajo", true, pagina.contains("Tarea 1"));
        html.delete();

        System.out.println();
        System.out.println(fallos == 0 ? "TODO OK" : "HAY " + fallos + " FALLO(S)");
    }
}
