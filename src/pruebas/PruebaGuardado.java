package pruebas;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import app.GestorNotas;
import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import evaluaciones.Examen;
import evaluaciones.ExamenSustitutorio;
import evaluaciones.TrabajoContinua;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;
import persistencia.Bitacora;
import persistencia.RepositorioArchivo;
import persistencia.RepositorioObjetos;
import plan.PlanEstudios;
import plan.PlanEstudios.CursoPlan;

/**
 * Pruebas de persistencia (texto y objetos), bitácora y validaciones de GestorNotas.
 */
public class PruebaGuardado {

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

    private static Evaluacion buscarEval(Curso c, String tipo, int numero) {
        for (Evaluacion e : c.getEvaluaciones()) {
            if (e instanceof Continua && tipo.equals("C") && ((Continua) e).getNumero() == numero) return e;
            if (e instanceof Examen && tipo.equals("E") && ((Examen) e).getNumero() == numero) return e;
        }
        throw new IllegalStateException("No se encontró " + tipo + numero);
    }

    private static void borrarSiExiste(String ruta) {
        File f = new File(ruta);
        if (f.exists()) {
            f.delete();
        }
    }

    private static ArrayList<String> leerLineas(String ruta) throws Exception {
        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(new FileReader(ruta, StandardCharsets.UTF_8))) {
            while (sc.hasNextLine()) {
                lineas.add(sc.nextLine());
            }
        }
        return lineas;
    }

    private static String leerContenido(String ruta) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String l : leerLineas(ruta)) {
            sb.append(l).append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        String rutaPrueba = "datos/prueba_guardado_temp.txt";
        String rutaBak = rutaPrueba + ".bak";
        String rutaDaniada = "datos/prueba_daniada_temp.txt";
        String rutaRespaldo = "datos/prueba_respaldo_temp.txt";
        String rutaRespaldoBak = rutaRespaldo + ".bak";
        String rutaObjetos = "datos/prueba_objetos_temp.dat";
        String rutaObjetosBak = rutaObjetos + ".bak";
        String rutaBitacora = "datos/prueba_bitacora_temp.txt";

        borrarSiExiste(rutaPrueba);
        borrarSiExiste(rutaBak);
        borrarSiExiste(rutaDaniada);
        borrarSiExiste(rutaRespaldo);
        borrarSiExiste(rutaRespaldoBak);
        borrarSiExiste(rutaObjetos);
        borrarSiExiste(rutaObjetosBak);
        borrarSiExiste(rutaBitacora);

        try {
            RepositorioArchivo repo = new RepositorioArchivo(rutaPrueba);
            GestorNotas gestor = new GestorNotas(repo);

            // 1. Unicidad de CUI y DNI en GestorNotas
            Estudiante est1 = new Estudiante("Karol", "Quispe; Mamani", "71234567",
                    "kmamani@unsa.edu.pe", "20261234", "Ingeniería de Sistemas");
            verificar("registrar primer estudiante", true, gestor.registrarEstudiante(est1));

            Estudiante dupCodigo = new Estudiante("Ana", "Perez", "79999999",
                    "aperez@unsa.edu.pe", "20261234", "Ingeniería de Sistemas");
            verificar("rechazar CUI/código duplicado", false, gestor.registrarEstudiante(dupCodigo));

            Estudiante dupDni = new Estudiante("Luis", "Gomez", "71234567",
                    "lgomez@unsa.edu.pe", "20269999", "Ingeniería de Sistemas");
            verificar("rechazar DNI duplicado", false, gestor.registrarEstudiante(dupDni));

            verificar("existeDni detecta DNI registrado", true, gestor.existeDni("71234567"));
            verificar("existeDni devuelve false para DNI libre", false, gestor.existeDni("70000000"));

            // 2. Armar ciclo con curso UNSA, trabajos de continua, exámenes y sustitutorio
            Ciclo ciclo = new Ciclo("2026-B", 2026);
            est1.agregarCiclo(ciclo);

            CursoPlan planDsoo = null;
            for (CursoPlan p : PlanEstudios.getCursosPrimerAnioSegundoSemestre()) {
                if (p.getCodigo().equals("2501211")) {
                    planDsoo = p;
                    break;
                }
            }
            Curso dsoo = PlanEstudios.crearCurso(planDsoo, 15.0);

            Continua c1 = (Continua) buscarEval(dsoo, "C", 1);
            TrabajoContinua t1 = new TrabajoContinua("Laboratorio 1", 60, false);
            t1.setNota(14.0);
            TrabajoContinua t2 = new TrabajoContinua("Proyecto Avance", 40, true);
            t2.setNota(18.0);
            c1.agregarTrabajo(t1);
            c1.agregarTrabajo(t2);

            buscarEval(dsoo, "E", 1).setNota(8.0);
            buscarEval(dsoo, "E", 2).setNota(15.0);

            ExamenSustitutorio susti = new ExamenSustitutorio();
            susti.setNota(16.0);
            dsoo.getEvaluaciones().add(susti);
            ciclo.agregarCurso(dsoo);

            Curso manual = PlanEstudios.crearCurso("2501999", "Inglés Técnico", 2, 14.0,
                    new double[] {20, 20, 20}, new double[] {10, 15, 15});
            buscarEval(manual, "E", 1).setNota(17.5);
            ciclo.agregarCurso(manual);

            double promedioAntes = est1.calcularPromedio();
            double promedioDsooAntes = dsoo.calcularPromedio();

            // 3. Guardar y volver a cargar en texto
            verificar("guardarTodo devuelve true", true, gestor.guardarTodo());

            GestorNotas gestorRecargado = new GestorNotas(new RepositorioArchivo(rutaPrueba));
            verificar("cargarTodo devuelve true", true, gestorRecargado.cargarTodo());
            verificar("cantidad de estudiantes cargados", 1, gestorRecargado.getEstudiantes().size());

            Estudiante cargado = gestorRecargado.buscarEstudiante("20261234");
            verificar("estudiante cargado existe", true, cargado != null);
            verificar("apellido con ';' conservado", "Quispe; Mamani", cargado.getApellido());
            verificar("DNI conservado", "71234567", cargado.getDni());
            verificar("ciclos conservados", 1, cargado.getCiclos().size());
            verificar("cursos conservados (UNSA + manual)", 2, cargado.getCiclos().get(0).getCursos().size());

            Curso dsooCargado = cargado.getCiclos().get(0).getCursos().get(0);
            Continua c1Cargada = (Continua) buscarEval(dsooCargado, "C", 1);
            verificar("trabajos de Continua 1 conservados", 2, c1Cargada.getTrabajos().size());
            verificar("nota de Continua 1 recalculada igual", c1.getNota(), c1Cargada.getNota());
            verificar("Continua 1 sigue completa", true, c1Cargada.isCompleta());
            verificar("promedio de DSOO (con sustitutorio) igual tras cargar",
                    promedioDsooAntes, dsooCargado.calcularPromedio());
            verificar("promedio acumulado del estudiante igual tras cargar",
                    promedioAntes, cargado.calcularPromedio());

            // 4. Archivo con línea dañada
            try (PrintWriter pw = new PrintWriter(new FileWriter(rutaDaniada, StandardCharsets.UTF_8))) {
                pw.println("ESTUDIANTE;Ana;Torres;74445555;atorres@unsa.edu.pe;20265555;Ingeniería de Sistemas");
                pw.println("CICLO;2026-A;2026");
                pw.println("CURSO;2501111;Curso Roto;NO_ES_NUMERO;14.0");
                pw.println("CURSO;2501222;Curso Sano;3;15.0");
            }
            GestorNotas gestorDaniado = new GestorNotas(new RepositorioArchivo(rutaDaniada));
            gestorDaniado.cargar();
            verificar("advertencias tras línea dañada", 1, gestorDaniado.getAdvertenciasCarga().size());
            Estudiante estSano = gestorDaniado.buscarEstudiante("20265555");
            verificar("estudiante con línea dañada se carga", true, estSano != null);
            verificar("curso sano cargado tras ignorar el dañado", 1,
                    estSano.getCiclos().get(0).getCursos().size());

            // 5. Respaldo .bak en texto
            try (PrintWriter pw = new PrintWriter(new FileWriter(rutaRespaldo, StandardCharsets.UTF_8))) {
                pw.println("CONTENIDO_INICIAL_DE_RESPALDO");
            }
            RepositorioArchivo repoRespaldo = new RepositorioArchivo(rutaRespaldo);
            GestorNotas gestorRespaldo = new GestorNotas(repoRespaldo);
            gestorRespaldo.agregarEstudiante(est1);
            gestorRespaldo.guardar();

            File fileBak = new File(rutaRespaldoBak);
            verificar("primer guardado crea archivo .bak", true, fileBak.exists());
            String contenidoBak1 = leerContenido(rutaRespaldoBak);
            verificar("contenido .bak corresponde al archivo previo",
                    "CONTENIDO_INICIAL_DE_RESPALDO\n", contenidoBak1);

            gestorRespaldo.agregarEstudiante(new Estudiante("Pedro", "Soto", "73332222",
                    "psoto@unsa.edu.pe", "20267777", "Ingeniería de Sistemas"));
            gestorRespaldo.guardar();
            String contenidoBak2 = leerContenido(rutaRespaldoBak);
            verificar("segundo guardado conserva intacto el .bak original",
                    contenidoBak1, contenidoBak2);

            // 6. RepositorioObjetos: guardar y cargar (.dat)
            gestor.setRepositorio(new RepositorioObjetos(rutaObjetos));
            gestor.guardar();

            GestorNotas gestorObj = new GestorNotas(new RepositorioObjetos(rutaObjetos));
            gestorObj.cargar();
            Estudiante estObj = gestorObj.buscarEstudiante("20261234");
            verificar("objetos: estudiante cargado existe", true, estObj != null);
            Curso dsooObj = estObj.getCiclos().get(0).getCursos().get(0);
            Continua c1Obj = (Continua) buscarEval(dsooObj, "C", 1);
            verificar("objetos: trabajos de Continua 1 conservados", 2, c1Obj.getTrabajos().size());
            verificar("objetos: sustitutorio conservado", true, dsooObj.getSustitutorio() != null);
            verificar("objetos: nota sustitutorio conservada", 16.0, dsooObj.getSustitutorio().getNota());
            verificar("objetos: meta del curso conservada", 15.0, dsooObj.getMetaPromedio());
            verificar("objetos: promedio del curso igual", promedioDsooAntes, dsooObj.calcularPromedio());

            // 7. Bitacora en modo añadir
            Bitacora bitacora = new Bitacora(rutaBitacora);
            verificar("bitacora: primer registro devuelve true", true, bitacora.registrar("Primera accion"));
            verificar("bitacora: segundo registro devuelve true", true, bitacora.registrar("Segunda accion"));
            ArrayList<String> lineasBit = leerLineas(rutaBitacora);
            verificar("bitacora: conserva ambas líneas sin sobrescribir", 2, lineasBit.size());

        } finally {
            borrarSiExiste(rutaPrueba);
            borrarSiExiste(rutaBak);
            borrarSiExiste(rutaDaniada);
            borrarSiExiste(rutaRespaldo);
            borrarSiExiste(rutaRespaldoBak);
            borrarSiExiste(rutaObjetos);
            borrarSiExiste(rutaObjetosBak);
            borrarSiExiste(rutaBitacora);
        }

        System.out.println();
        System.out.println(fallos == 0 ? "TODO OK" : "HAY " + fallos + " FALLO(S)");
    }
}