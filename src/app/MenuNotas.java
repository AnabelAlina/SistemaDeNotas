package app;

import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import evaluaciones.Examen;
import evaluaciones.ExamenSustitutorio;
import evaluaciones.TrabajoContinua;
import excepciones.PesoInvalidoException;
import java.util.ArrayList;
import modelo.Curso;
import modelo.Estudiante;

/**
 * Submenú de notas: registro de exámenes, trabajos de continua y sustitutorio.
 */
public class MenuNotas {

    private GestorNotas gestor;
    private Entrada entrada;

    public MenuNotas(GestorNotas gestor, Entrada entrada) {
        this.gestor = gestor;
        this.entrada = entrada;
    }

    public void mostrar() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- REGISTRO DE NOTAS ---");
            System.out.println(" 1. Registrar nota de un Examen");
            System.out.println(" 2. Agregar trabajos a una Continua");
            System.out.println(" 3. Registrar nota de un trabajo");
            System.out.println(" 4. Registrar examen sustitutorio");
            System.out.println(" 0. Volver");
            int opcion = entrada.leerEntero("Elige una opción: ", 0, 4);
            switch (opcion) {
                case 1: registrarNotaExamen(); break;
                case 2: agregarTrabajos(); break;
                case 3: registrarNotaTrabajo(); break;
                case 4: registrarSustitutorio(); break;
                case 0: volver = true; break;
            }
        }
    }

    // ===================== EXÁMENES =====================

    private void registrarNotaExamen() {
        Curso curso = pedirCurso();
        if (curso == null) return;

        ArrayList<Examen> examenes = new ArrayList<>();
        for (Evaluacion e : curso.getEvaluaciones()) {
            if (e instanceof Examen) {
                examenes.add((Examen) e);
            }
        }
        if (examenes.isEmpty()) {
            System.out.println("Ese curso no tiene exámenes.");
            return;
        }
        for (int i = 0; i < examenes.size(); i++) {
            System.out.println((i + 1) + ") " + examenes.get(i));
        }
        Examen examen = examenes.get(entrada.leerEntero("Elige el examen: ", 1, examenes.size()) - 1);
        double nota = entrada.leerDouble("Nota de " + examen.getNombre() + " (0-20): ", 0, 20);

        if (gestor.registrarNota(curso, examen, nota)) {
            System.out.println("✔ Nota guardada: " + examen.getNombre() + " = " + nota);
            mostrarPromedio(curso);
        }
    }

    // ===================== CONTINUAS Y TRABAJOS =====================

    private void agregarTrabajos() {
        Curso curso = pedirCurso();
        if (curso == null) return;
        Continua continua = elegirContinua(curso);
        if (continua == null) return;

        System.out.println(continua.getNombre() + " - peso interno usado: " + continua.getPesoInterno() + "%");
        while (100 - continua.getPesoInterno() > 0.0001) {
            double restante = 100 - continua.getPesoInterno();
            String nombre = entrada.leerTexto("Nombre del trabajo: ");
            double peso = entrada.leerDouble("Peso dentro de la continua (máx. "
                    + String.format("%.1f", restante) + "%): ", 0.01, 100);
            boolean grupal = entrada.leerSiNo("¿Es grupal?");
            try {
                continua.agregarTrabajo(new TrabajoContinua(nombre, peso, grupal));
                System.out.println("✔ Trabajo agregado. Peso interno: " + continua.getPesoInterno() + "%");
            } catch (PesoInvalidoException | IllegalArgumentException e) {
                System.out.println("No se pudo agregar: " + e.getMessage());
            }
            if (100 - continua.getPesoInterno() > 0.0001 && !entrada.leerSiNo("¿Agregar otro trabajo?")) {
                break;
            }
        }
        if (continua.getPesoInterno() >= 99.9999) {
            System.out.println("La " + continua.getNombre() + " ya suma 100%.");
        }
    }

    private void registrarNotaTrabajo() {
        Curso curso = pedirCurso();
        if (curso == null) return;
        Continua continua = elegirContinua(curso);
        if (continua == null) return;

        ArrayList<TrabajoContinua> trabajos = continua.getTrabajos();
        if (trabajos.isEmpty()) {
            System.out.println("Esa continua no tiene trabajos. Agrégalos con la opción 2.");
            return;
        }
        for (int i = 0; i < trabajos.size(); i++) {
            System.out.println((i + 1) + ") " + trabajos.get(i));
        }
        TrabajoContinua trabajo = trabajos.get(entrada.leerEntero("Elige el trabajo: ", 1, trabajos.size()) - 1);
        double nota = entrada.leerDouble("Nota de " + trabajo.getNombre() + " (0-20): ", 0, 20);

        if (gestor.registrarNotaTrabajo(trabajo, nota)) {
            System.out.println("✔ Nota guardada: " + trabajo.getNombre() + " = " + nota);
            System.out.printf("Nota actual de la %s: %.2f%s%n", continua.getNombre(), continua.getNota(),
                    continua.isCompleta() ? "" : " (todavía incompleta)");
            mostrarPromedio(curso);
        }
    }

    // ===================== SUSTITUTORIO =====================

    private void registrarSustitutorio() {
        Curso curso = pedirCurso();
        if (curso == null) return;

        System.out.println("El sustitutorio reemplaza la nota más baja entre el Examen 1 y el 2, solo si la mejora.");
        double nota = entrada.leerDouble("Nota del sustitutorio (0-20): ", 0, 20);
        ExamenSustitutorio sustitutorio = curso.obtenerOCrearSustitutorio();

        if (gestor.registrarNota(curso, sustitutorio, nota)) {
            System.out.println("✔ Sustitutorio registrado: " + nota);
            mostrarPromedio(curso);
        }
    }

    // ===================== AYUDAS =====================

    // Estudiante -> ciclo -> curso (devuelve null si algo falta)
    private Curso pedirCurso() {
        Estudiante est = entrada.seleccionarEstudiante(gestor);
        if (est == null) return null;
        return entrada.elegirCurso(est);
    }

    private Continua elegirContinua(Curso curso) {
        ArrayList<Continua> continuas = new ArrayList<>();
        for (Evaluacion e : curso.getEvaluaciones()) {
            if (e instanceof Continua) {
                continuas.add((Continua) e);
            }
        }
        if (continuas.isEmpty()) {
            System.out.println("Ese curso no tiene continuas.");
            return null;
        }
        for (int i = 0; i < continuas.size(); i++) {
            System.out.println((i + 1) + ") " + continuas.get(i).getNombre()
                    + " (" + continuas.get(i).getPeso() + "% del curso)");
        }
        return continuas.get(entrada.leerEntero("Elige la continua: ", 1, continuas.size()) - 1);
    }

    // Muestra el promedio actual para que se vea el efecto de la nota registrada
    private void mostrarPromedio(Curso curso) {
        System.out.printf("Promedio actual de %s: %.2f%n", curso.getNombre(), curso.calcularPromedio());
    }
}