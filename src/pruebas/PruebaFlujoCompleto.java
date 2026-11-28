package pruebas;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

import app.GestorNotas;
import calculos.EstadoCurso;
import calculos.EvaluadorCurso;
import calculos.SimuladorNotas;
import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import evaluaciones.Examen;
import evaluaciones.ExamenSustitutorio;
import evaluaciones.TrabajoContinua;
import excepciones.NotaInvalidaException;
import exportacion.ExportadorCSV;
import exportacion.ExportadorHTML;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;
import persistencia.RepositorioArchivo;
import persistencia.RepositorioObjetos;
import plan.PlanEstudios;

/**
 * Prueba del flujo completo, sin ventanas: registrar, ciclo, cursos, notas, trabajos,
 * sustitutorio, los cuatro estados, nota necesaria, guardar y cargar (texto y objetos),
 * archivo con una línea dañada, reportes CSV y HTML y errores de GestorNotas.
 * Los valores esperados son cuentas hechas a mano (se explican en cada caso).
 */
public class PruebaFlujoCompleto {

    private static int fallos = 0;

    private static void verificar(String descripcion, double esperado, double obtenido) {
        boolean ok = Math.abs(esperado - obtenido) < 0.0001;
        if (!ok) fallos++;
        System.out.println((ok ? "OK    " : "FALLO ") + descripcion
                + " -> esperado " + esperado + ", obtenido " + obtenido);
    }

    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        boolean ok = esperado.equals(obtenido);
        if (!ok) fallos++;
        System.out.println((ok ? "OK    " : "FALLO ") + descripcion
                + " -> esperado " + esperado + ", obtenido " + obtenido);
    }

    // Curso de prueba con exámenes 16/16/18 y continuas 16/16/18 (suman 100)
    private static Curso nuevoCurso(String codigo) throws Exception {
        return PlanEstudios.crearCurso(codigo, "Curso " + codigo, 4, 14,
                new double[]{16, 16, 18}, new double[]{16, 16, 18});
    }

    private static Examen examen(Curso curso, int numero) {
        for (Evaluacion e : curso.getEvaluaciones()) {
            if (e instanceof Examen && ((Examen) e).getNumero() == numero) {
                return (Examen) e;
            }
        }
        throw new IllegalStateException("No se encontró Examen " + numero);
    }

    private static Continua continua(Curso curso, int numero) {
        for (Evaluacion e : curso.getEvaluaciones()) {
            if (e instanceof Continua && ((Continua) e).getNumero() == numero) {
                return (Continua) e;
            }
        }
        throw new IllegalStateException("No se encontró Continua " + numero);
    }

    private static void borrarSiExiste(String ruta) {
        File f = new File(ruta);
        if (f.exists()) {
            f.delete();
        }
    }

    private static String leerTodo(File archivo) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Scanner in = new Scanner(new FileReader(archivo, StandardCharsets.UTF_8))) {
            while (in.hasNextLine()) {
                sb.append(in.nextLine()).append("\n");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        String rutaTexto = "datos/flujo_completo_temp.txt";
        String rutaObjetos = "datos/flujo_completo_temp.dat";
        String rutaDaniada = "datos/flujo_daniado_temp.txt";
        borrarSiExiste(rutaTexto);
        borrarSiExiste(rutaTexto + ".bak");
        borrarSiExiste(rutaObjetos);
        borrarSiExiste(rutaObjetos + ".bak");
        borrarSiExiste(rutaDaniada);

        EvaluadorCurso evaluador = new EvaluadorCurso();
        SimuladorNotas simulador = new SimuladorNotas();

        try {
            // 1. Registrar estudiante, ciclo y cursos predeterminados
            GestorNotas gestor = new GestorNotas(new RepositorioArchivo(rutaTexto));
            Estudiante est = new Estudiante("Ayelen", "Choquecota", "71234567",
                    "achoquecota@unsa.edu.pe", "20261234", "Ingeniería de Sistemas");
            gestor.agregarEstudiante(est);
            verificar("estudiante registrado", 1, gestor.getEstudiantes().size());

            Ciclo ciclo = new Ciclo("2026-B", 2026);
            est.agregarCiclo(ciclo);
            verificar("ciclo agregado", 1, est.getCiclos().size());

            int agregados = ciclo.agregarCursosPredeterminados(14);
            verificar("cursos predeterminados agregados", 7, agregados);
            verificar("cada curso predeterminado trae 6 evaluaciones", 6,
                    ciclo.getCursos().get(0).getEvaluaciones().size());

            // 2. Estados del curso (exámenes 16/16/18, continuas 16/16/18)
            Curso sinNotas = nuevoCurso("S0");
            verificar("sin notas no hay estado", true, evaluador.estadoDe(sinNotas) == null);

            // Examen 1 = 15 y nada más: gana 15 x 16 / 100 = 2.4; aún pueden llegar 2.4 + 84 x 20 / 100 = 19.2
            Curso vas = nuevoCurso("S1");
            gestor.asignarNota(vas, examen(vas, 1), 15);
            verificar("Examen 1 = 15: promedio de lo rendido", 15.0, vas.calcularPromedio());
            verificar("Examen 1 = 15: estado Vas aprobando", EstadoCurso.VAS_APROBANDO, evaluador.estadoDe(vas));

            // Examen 1 = 8: promedio 8 (< 10.5) pero aún puede llegar a 1.28 + 16.8 = 18.08
            Curso riesgo = nuevoCurso("S2");
            gestor.asignarNota(riesgo, examen(riesgo, 1), 8);
            verificar("Examen 1 = 8: estado En riesgo", EstadoCurso.EN_RIESGO, evaluador.estadoDe(riesgo));

            // Los tres exámenes en 0: ganó 0 y lo máximo que puede llegar es 48 x 20 / 100 = 9.6 (< 10.5)
            Curso desaprobado = nuevoCurso("S3");
            for (int i = 1; i <= 3; i++) {
                gestor.asignarNota(desaprobado, examen(desaprobado, i), 0);
            }
            verificar("exámenes en 0: estado Desaprobado", EstadoCurso.DESAPROBADO, evaluador.estadoDe(desaprobado));

            // Exámenes en 20 (10 puntos) + Continua 1 con un trabajo de 100% en 20 (3.2 puntos) = 13.2 >= 10.5
            Curso aprobado = nuevoCurso("S4");
            for (int i = 1; i <= 3; i++) {
                gestor.asignarNota(aprobado, examen(aprobado, i), 20);
            }
            TrabajoContinua trabajoAprobado = new TrabajoContinua("Trabajo único", 100, false);
            continua(aprobado, 1).agregarTrabajo(trabajoAprobado);
            gestor.asignarNotaTrabajo(trabajoAprobado, 20);
            verificar("13.2 puntos asegurados: estado Aprobado", EstadoCurso.APROBADO, evaluador.estadoDe(aprobado));

            // 3. Sustitutorio (solo reemplaza la nota más baja entre Examen 1 y 2, y solo si mejora)
            // Notas 8, 12, 10: (8x16 + 12x16 + 10x18) / 50 = 10.0
            Curso conSusti = nuevoCurso("S5");
            gestor.asignarNota(conSusti, examen(conSusti, 1), 8);
            gestor.asignarNota(conSusti, examen(conSusti, 2), 12);
            gestor.asignarNota(conSusti, examen(conSusti, 3), 10);
            verificar("promedio sin sustitutorio", 10.0, conSusti.calcularPromedio());
            ExamenSustitutorio susti = conSusti.obtenerOCrearSustitutorio();
            gestor.asignarNota(conSusti, susti, 14);
            // Sustitutorio 14 reemplaza el 8: (14x16 + 12x16 + 10x18) / 50 = 11.92
            verificar("promedio con sustitutorio 14 (reemplaza el 8)", 11.92, conSusti.calcularPromedio());
            gestor.asignarNota(conSusti, susti, 5);
            verificar("sustitutorio 5 no mejora: el promedio no baja", 10.0, conSusti.calcularPromedio());

            // Notas 12, 14, 5 y sustitutorio 18: reemplaza el 12 (nunca el Examen 3)
            // (18x16 + 14x16 + 5x18) / 50 = 12.04
            Curso nuncaExamen3 = nuevoCurso("S6");
            gestor.asignarNota(nuncaExamen3, examen(nuncaExamen3, 1), 12);
            gestor.asignarNota(nuncaExamen3, examen(nuncaExamen3, 2), 14);
            gestor.asignarNota(nuncaExamen3, examen(nuncaExamen3, 3), 5);
            gestor.asignarNota(nuncaExamen3, nuncaExamen3.obtenerOCrearSustitutorio(), 18);
            verificar("sustitutorio nunca reemplaza el Examen 3", 12.04, nuncaExamen3.calcularPromedio());

            // 4. Nota necesaria con una continua incompleta
            // Continua 10% con un trabajo de 60% en 18 y Examen 90%: gana 18 x 6 / 100 = 1.08;
            // pendiente = 4 + 90 = 94; necesaria para meta 14 = (1400 - 108) / 94 = 13.7447
            Curso incompleto = new Curso("S7", "Curso S7", 3, 14);
            Continua c10 = new Continua(1, 10);
            incompleto.agregarEvaluacion(c10);
            incompleto.agregarEvaluacion(new Examen(1, 90));
            TrabajoContinua t60 = new TrabajoContinua("Trabajo 1", 60, false);
            c10.agregarTrabajo(t60);
            gestor.asignarNotaTrabajo(t60, 18);
            verificar("continua con un trabajo no está completa", false, c10.isCompleta());
            verificar("nota necesaria con continua incompleta", 13.7447, simulador.notaNecesaria(incompleto, 14));

            // 5. Guardar y cargar en texto y en objetos
            Curso curso = ciclo.getCursos().get(0);
            gestor.asignarNota(curso, examen(curso, 1), 15);
            gestor.asignarNota(curso, examen(curso, 2), 9);
            Continua c1 = continua(curso, 1);
            TrabajoContinua tg = new TrabajoContinua("Informe grupal", 40, true);
            TrabajoContinua ti = new TrabajoContinua("Práctica", 60, false);
            c1.agregarTrabajo(tg);
            c1.agregarTrabajo(ti);
            gestor.asignarNotaTrabajo(tg, 16);
            gestor.asignarNotaTrabajo(ti, 12);
            gestor.asignarNota(curso, curso.obtenerOCrearSustitutorio(), 13);
            examen(curso, 1).setFecha(LocalDate.of(2026, 11, 20));

            double promedioAntes = curso.calcularPromedio();
            int evaluacionesAntes = curso.getEvaluaciones().size();

            gestor.guardar();
            GestorNotas cargadoTexto = new GestorNotas(new RepositorioArchivo(rutaTexto));
            cargadoTexto.cargar();
            verificar("texto: sin advertencias", 0, cargadoTexto.getAdvertenciasCarga().size());
            Curso deTexto = cargadoTexto.buscarEstudiante("20261234").getCiclos().get(0).getCursos().get(0);
            verificar("texto: mismo promedio", promedioAntes, deTexto.calcularPromedio());
            verificar("texto: misma cantidad de evaluaciones", evaluacionesAntes, deTexto.getEvaluaciones().size());
            verificar("texto: el sustitutorio se conserva", 13.0, deTexto.getSustitutorio().getNota());
            verificar("texto: la fecha se conserva", LocalDate.of(2026, 11, 20), examen(deTexto, 1).getFecha());
            verificar("texto: sin fecha sigue sin fecha", true, examen(deTexto, 2).getFecha() == null);

            gestor.setRepositorio(new RepositorioObjetos(rutaObjetos));
            gestor.guardar();
            GestorNotas cargadoObjetos = new GestorNotas(new RepositorioObjetos(rutaObjetos));
            cargadoObjetos.cargar();
            Curso deObjetos = cargadoObjetos.buscarEstudiante("20261234").getCiclos().get(0).getCursos().get(0);
            verificar("objetos: mismo promedio", promedioAntes, deObjetos.calcularPromedio());
            verificar("objetos: misma cantidad de evaluaciones", evaluacionesAntes, deObjetos.getEvaluaciones().size());
            verificar("objetos: la fecha se conserva", LocalDate.of(2026, 11, 20), examen(deObjetos, 1).getFecha());

            // 6. Archivo con una línea dañada
            try (PrintWriter pw = new PrintWriter(new FileWriter(rutaDaniada, StandardCharsets.UTF_8))) {
                pw.println("ESTUDIANTE;Ana;Torres;74445555;atorres@unsa.edu.pe;20265555;Ingeniería de Sistemas");
                pw.println("CICLO;2026-A;2026");
                pw.println("CURSO;2501111;Curso Roto;NO_ES_NUMERO;14.0");
                pw.println("CURSO;2501222;Curso Sano;3;15.0");
            }
            GestorNotas daniado = new GestorNotas(new RepositorioArchivo(rutaDaniada));
            daniado.cargar();
            verificar("línea dañada: una advertencia", 1, daniado.getAdvertenciasCarga().size());
            verificar("línea dañada: el curso sano se carga", 1,
                    daniado.buscarEstudiante("20265555").getCiclos().get(0).getCursos().size());

            // 7. Exportar CSV y HTML
            File csv = File.createTempFile("flujo", ".csv");
            File html = File.createTempFile("flujo", ".html");
            try {
                new ExportadorCSV().exportar(est, csv.getPath());
                new ExportadorHTML().exportar(est, html.getPath());
                String textoCsv = leerTodo(csv);
                String textoHtml = leerTodo(html);
                verificar("csv: tiene filas de Resumen", true, textoCsv.contains(";Resumen;"));
                verificar("csv: trae el trabajo grupal", true, textoCsv.contains("Informe grupal"));
                verificar("html: tiene una tabla", true, textoHtml.contains("<table"));
                verificar("html: trae el nombre del curso", true, textoHtml.contains(curso.getNombre()));
            } finally {
                csv.delete();
                html.delete();
            }

            // 8. Errores de GestorNotas
            Estudiante cuiRepetido = new Estudiante("Otra", "Persona", "70000001",
                    "opersona@unsa.edu.pe", "20261234", "Ingeniería de Sistemas");
            boolean rechazoCui = false;
            try {
                gestor.agregarEstudiante(cuiRepetido);
            } catch (IllegalArgumentException e) {
                rechazoCui = true;
            }
            verificar("CUI repetido se rechaza", true, rechazoCui);

            Estudiante dniRepetido = new Estudiante("Otra", "Persona", "71234567",
                    "opersona@unsa.edu.pe", "20269999", "Ingeniería de Sistemas");
            boolean rechazoDni = false;
            try {
                gestor.agregarEstudiante(dniRepetido);
            } catch (IllegalArgumentException e) {
                rechazoDni = true;
            }
            verificar("DNI repetido se rechaza", true, rechazoDni);
            verificar("los repetidos no se agregaron", 1, gestor.getEstudiantes().size());

            Curso paraNota = nuevoCurso("S8");
            boolean rechazoNota = false;
            try {
                gestor.asignarNota(paraNota, examen(paraNota, 1), 25);
            } catch (NotaInvalidaException e) {
                rechazoNota = true;
            }
            verificar("nota 25 se rechaza", true, rechazoNota);
            verificar("la nota 25 no deja el examen como rendido", false, examen(paraNota, 1).isRendida());
        } finally {
            borrarSiExiste(rutaTexto);
            borrarSiExiste(rutaTexto + ".bak");
            borrarSiExiste(rutaObjetos);
            borrarSiExiste(rutaObjetos + ".bak");
            borrarSiExiste(rutaDaniada);
        }

        System.out.println();
        System.out.println(fallos == 0 ? "TODO OK" : "HAY " + fallos + " FALLO(S)");
    }
}
