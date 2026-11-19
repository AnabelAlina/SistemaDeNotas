package pruebas;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import app.GestorNotas;
import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import evaluaciones.Examen;
import evaluaciones.ExamenSustitutorio;
import evaluaciones.TrabajoContinua;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;
import persistencia.RepositorioArchivo;
import plan.PlanEstudios;
import plan.PlanEstudios.CursoPlan;

/**
 * Estudiante 3 (Karol Mamani) - Pruebas de persistencia (guardar y volver a cargar
 * el modelo UNSA) y validación de unicidad de DNI y CUI en GestorNotas.
 */
public class PruebaGuardado {

    private static int fallos = 0;

    private static void verificar(String descripcion, double esperado, double obtenido) {
        boolean ok = Math.abs(esperado - obtenido) < 0.0001;
        if (!ok) fallos++;
        System.out.println((ok ? "OK    " : "FALLO ") + descripcion + " -> esperado " + esperado + ", obtenido " + obtenido);
    }

    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        boolean ok = esperado.equals(obtenido);
        if (!ok) fallos++;
        System.out.println((ok ? "OK    " : "FALLO ") + descripcion + " -> esperado " + esperado + ", obtenido " + obtenido);
    }

    private static Evaluacion buscarEval(Curso c, String tipo, int numero) {
        for (Evaluacion e : c.getEvaluaciones()) {
            if (e instanceof Continua && tipo.equals("C") && ((Continua) e).getNumero() == numero) return e;
            if (e instanceof Examen && tipo.equals("E") && ((Examen) e).getNumero() == numero) return e;
        }
        throw new IllegalStateException("No se encontró " + tipo + numero);
    }

    public static void main(String[] args) throws Exception {
        String rutaPrueba = "datos/prueba_guardado_temp.txt";
        Path pathPrueba = Paths.get(rutaPrueba);
        Files.deleteIfExists(pathPrueba);

        try {
            RepositorioArchivo repo = new RepositorioArchivo(rutaPrueba);
            GestorNotas gestor = new GestorNotas(repo);

            // --- 1. Unicidad de CUI y DNI en GestorNotas ---
            Estudiante est1 = new Estudiante("Karol", "Mamani", "71234567", "kmamani@unsa.edu.pe", "20261234", "Ingeniería de Sistemas");
            verificar("registrar primer estudiante", true, gestor.registrarEstudiante(est1));

            Estudiante dupCodigo = new Estudiante("Ana", "Perez", "79999999", "aperez@unsa.edu.pe", "20261234", "Ingeniería de Sistemas");
            verificar("rechazar CUI/código duplicado", false, gestor.registrarEstudiante(dupCodigo));

            Estudiante dupDni = new Estudiante("Luis", "Gomez", "71234567", "lgomez@unsa.edu.pe", "20269999", "Ingeniería de Sistemas");
            verificar("rechazar DNI duplicado", false, gestor.registrarEstudiante(dupDni));
            verificar("existeDni detecta DNI registrado", true, gestor.existeDni("71234567"));
            verificar("existeDni devuelve false para DNI libre", false, gestor.existeDni("70000000"));

            // --- 2. Armar ciclo con curso UNSA, trabajos de continua, exámenes y sustitutorio ---
            Ciclo ciclo = new Ciclo("2026-B", 2026);
            est1.agregarCiclo(ciclo);

            // Curso predeterminado: DSOO (2501211)
            CursoPlan planDsoo = null;
            for (CursoPlan p : PlanEstudios.getCursosPrimerAnioSegundoSemestre()) {
                if (p.getCodigo().equals("2501211")) {
                    planDsoo = p;
                    break;
                }
            }
            Curso dsoo = PlanEstudios.crearCurso(planDsoo, 15.0);

            // Continua 1 con 2 trabajos (60% y 40%)
            Continua c1 = (Continua) buscarEval(dsoo, "C", 1);
            TrabajoContinua t1 = new TrabajoContinua("Laboratorio 1", 60, false);
            t1.setNota(14.0);
            TrabajoContinua t2 = new TrabajoContinua("Proyecto Avance", 40, true);
            t2.setNota(18.0);
            c1.agregarTrabajo(t1);
            c1.agregarTrabajo(t2);

            // Examen 1 con nota baja (8.0) y Examen 2 (15.0)
            buscarEval(dsoo, "E", 1).setNota(8.0);
            buscarEval(dsoo, "E", 2).setNota(15.0);

            // Sustitutorio con 16.0 (reemplaza Examen 1 de 8.0)
            ExamenSustitutorio susti = new ExamenSustitutorio();
            susti.setNota(16.0);
            dsoo.getEvaluaciones().add(susti);

            ciclo.agregarCurso(dsoo);

            // Curso manual adicional
            Curso manual = PlanEstudios.crearCurso("2501999", "Inglés Técnico", 2, 14.0,
                    new double[]{20, 20, 20}, new double[]{10, 15, 15});
            buscarEval(manual, "E", 1).setNota(17.5);
            ciclo.agregarCurso(manual);

            double promedioAntes = est1.calcularPromedio();
            double promedioDsooAntes = dsoo.calcularPromedio();

            // --- 3. Guardar y volver a cargar en un GestorNotas nuevo ---
            verificar("guardarTodo devuelve true", true, gestor.guardarTodo());

            GestorNotas gestorRecargado = new GestorNotas(new RepositorioArchivo(rutaPrueba));
            verificar("cargarTodo devuelve true", true, gestorRecargado.cargarTodo());
            verificar("cantidad de estudiantes cargados", 1, gestorRecargado.getEstudiantes().size());

            Estudiante cargado = gestorRecargado.buscarEstudiante("20261234");
            verificar("estudiante cargado existe", true, cargado != null);
            verificar("DNI conservado", "71234567", cargado.getDni());
            verificar("ciclos conservados", 1, cargado.getCiclos().size());
            verificar("cursos conservados (UNSA + manual)", 2, cargado.getCiclos().get(0).getCursos().size());

            Curso dsooCargado = cargado.getCiclos().get(0).getCursos().get(0);
            Continua c1Cargada = (Continua) buscarEval(dsooCargado, "C", 1);
            verificar("trabajos de Continua 1 conservados", 2, c1Cargada.getTrabajos().size());
            verificar("nota de Continua 1 recalculada igual", c1.getNota(), c1Cargada.getNota());
            verificar("Continua 1 sigue completa", true, c1Cargada.isCompleta());
            verificar("promedio de DSOO (con sustitutorio) igual tras cargar", promedioDsooAntes, dsooCargado.calcularPromedio());
            verificar("promedio acumulado del estudiante igual tras cargar", promedioAntes, cargado.calcularPromedio());

        } finally {
            Files.deleteIfExists(pathPrueba);
        }

        System.out.println();
        System.out.println(fallos == 0 ? "TODO OK" : "HAY " + fallos + " FALLO(S)");
    }
}