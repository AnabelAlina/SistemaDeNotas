package app;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import calculos.EstadisticasNotas;
import calculos.SimuladorNotas;
import evaluaciones.Evaluacion;
import evaluaciones.ExamenFinal;
import evaluaciones.Parcial;
import evaluaciones.Practica;
import evaluaciones.Trabajo;
import excepciones.NotaInvalidaException;
import excepciones.PesoInvalidoException;
import exportacion.Exportable;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;

/**
 * Menú de consola. Solo habla con el usuario: la lógica real está en
 * GestorNotas y en las clases del modelo y los cálculos.
 * Atrapa los errores de entrada (letras donde va un número, opciones fuera
 * de rango, fechas mal escritas) y vuelve a pedir el dato.
 */
public class MenuPrincipal {

    private Scanner sc;
    private GestorNotas gestor;
    private Exportable exportador;                       // solo la interfaz
    private SimuladorNotas simulador = new SimuladorNotas();

    public MenuPrincipal(GestorNotas gestor, Exportable exportador) {
        this.sc = new Scanner(System.in);
        this.gestor = gestor;
        this.exportador = exportador;
    }

    // ===================== BUCLE PRINCIPAL =====================

    public void iniciar() {
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = leerEntero("Elige una opción: ", 1, 11);
            try {
                salir = ejecutar(opcion);
            } catch (IllegalArgumentException e) {
                // Red de seguridad: los constructores del modelo lanzan este error
                System.out.println("Dato inválido: " + e.getMessage());
            }
        }
        System.out.println("¡Hasta pronto!");
    }

    public void mostrarMenu() {
        System.out.println("\n===== SISTEMA DE NOTAS =====");
        Estudiante actual = gestor.getActual();
        if (actual != null) {
            System.out.println("Estudiante actual: " + actual.getNombreCompleto()
                    + " (" + actual.getCodigo() + ")");
        }
        System.out.println(" 1. Registrar estudiante");
        System.out.println(" 2. Agregar ciclo");
        System.out.println(" 3. Agregar curso al ciclo");
        System.out.println(" 4. Agregar evaluación al curso");
        System.out.println(" 5. Registrar una nota");
        System.out.println(" 6. Ver promedio del curso, del ciclo y acumulado");
        System.out.println(" 7. Ver estadísticas");
        System.out.println(" 8. Simular una nota");
        System.out.println(" 9. Ver nota necesaria para la meta");
        System.out.println("10. Exportar reporte");
        System.out.println("11. Guardar y salir");
    }

    // Devuelve true si hay que salir del programa
    private boolean ejecutar(int opcion) {
        switch (opcion) {
            case 1:  registrarEstudiante(); break;
            case 2:  agregarCiclo(); break;
            case 3:  agregarCurso(); break;
            case 4:  agregarEvaluacion(); break;
            case 5:  registrarNota(); break;
            case 6:  verPromedios(); break;
            case 7:  verEstadisticas(); break;
            case 8:  simularNota(); break;
            case 9:  verNotaNecesaria(); break;
            case 10: exportarReporte(); break;
            case 11:
                if (gestor.guardarTodo()) {
                    System.out.println("Datos guardados.");
                }
                return true;
        }
        return false;
    }

    // ===================== OPCIONES =====================

    private void registrarEstudiante() {
        String nombre = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        String dni = leerTexto("DNI: ");
        String correo = leerTexto("Correo: ");
        String codigo = leerTexto("Código de estudiante: ");
        String carrera = leerTexto("Carrera: ");
        Estudiante e = new Estudiante(nombre, apellido, dni, correo, codigo, carrera);
        if (gestor.registrarEstudiante(e)) {
            System.out.println("Estudiante registrado.");
        }
    }

    private void agregarCiclo() {
        Estudiante est = seleccionarEstudiante();
        if (est == null) return;
        String nombre = leerTexto("Nombre del ciclo (ej. 2026-A): ");
        int anio = leerEntero("Año: ", 1, 3000);
        est.agregarCiclo(new Ciclo(nombre, anio));
        System.out.println("Ciclo agregado.");
    }

    private void agregarCurso() {
        Estudiante est = seleccionarEstudiante();
        if (est == null) return;
        Ciclo ciclo = elegirCiclo(est);
        if (ciclo == null) return;
        String codigo = leerTexto("Código del curso: ");
        String nombre = leerTexto("Nombre del curso: ");
        int creditos = leerEntero("Créditos: ", 1, 30);
        double meta = leerDouble("Meta de promedio (0 a 20): ", 0, 20);
        // agregarCurso lanza IllegalArgumentException si el código se repite
        ciclo.agregarCurso(new Curso(codigo, nombre, creditos, meta));
        System.out.println("Curso agregado.");
    }

    private void agregarEvaluacion() {
        Estudiante est = seleccionarEstudiante();
        if (est == null) return;
        Curso curso = elegirCurso(est);
        if (curso == null) return;

        System.out.println("Tipo: 1) Práctica  2) Parcial  3) Examen final  4) Trabajo");
        int tipo = leerEntero("Elige el tipo: ", 1, 4);
        String nombre = leerTexto("Nombre de la evaluación: ");
        double peso = leerDouble("Peso en % (ej. 20): ", 0.01, 100);
        LocalDate fecha = leerFecha();

        Evaluacion ev;
        switch (tipo) {
            case 1:
                ev = new Practica(nombre, peso, fecha, leerEntero("Número de práctica: ", 1, 99));
                break;
            case 2:
                ev = new Parcial(nombre, peso, fecha, leerSiNo("¿Tiene recuperación?"));
                break;
            case 3:
                ev = new ExamenFinal(nombre, peso, fecha, leerSiNo("¿Es sustitutorio?"));
                break;
            default:
                ev = new Trabajo(nombre, peso, fecha, leerSiNo("¿Es grupal?"));
        }
        try {
            curso.agregarEvaluacion(ev); // valida que los pesos no pasen de 100%
            System.out.println("Evaluación agregada. Peso total del curso: " + curso.getPesoTotal() + "%");
        } catch (PesoInvalidoException e) {
            System.out.println("No se agregó: " + e.getMessage());
        }
    }

    private void registrarNota() {
        Estudiante est = seleccionarEstudiante();
        if (est == null) return;
        Curso curso = elegirCurso(est);
        if (curso == null) return;
        Evaluacion ev = elegirEvaluacion(curso);
        if (ev == null) return;
        double nota = leerDouble("Nota (0 a 20): ");
        // El gestor valida el rango y muestra el mensaje si la nota es inválida
        if (gestor.registrarNota(curso, ev, nota)) {
            System.out.println("Nota registrada.");
        }
    }

    private void verPromedios() {
        Estudiante est = seleccionarEstudiante();
        if (est == null) return;
        Ciclo ciclo = elegirCiclo(est);
        if (ciclo == null) return;
        Curso curso = elegirCursoDe(ciclo);
        if (curso == null) return;

        if (curso.tieneNotas()) {
            System.out.printf("Promedio del curso %s: %.2f (%s)%n", curso.getNombre(),
                    curso.calcularPromedio(), curso.estaAprobado() ? "aprobado" : "desaprobado");
        } else {
            System.out.println("El curso aún no tiene notas rendidas.");
        }
        System.out.printf("Promedio del ciclo %s: %.2f%n", ciclo.getNombre(), ciclo.calcularPromedio());
        System.out.printf("Promedio acumulado: %.2f%n", est.calcularPromedio());
    }

    private void verEstadisticas() {
        Estudiante est = seleccionarEstudiante();
        if (est == null) return;
        Curso curso = elegirCurso(est);
        if (curso == null) return;

        EstadisticasNotas stats = new EstadisticasNotas();
        stats.calcularFrecuencias(curso.getEvaluaciones()); // SIEMPRE primero
        if (!stats.hayDatos()) {
            System.out.println("Este curso no tiene notas rendidas todavía.");
            return;
        }
        System.out.println("--- Estadísticas de " + curso.getNombre() + " ---");
        System.out.printf("Máxima: %.2f%n", stats.getMaxima());
        System.out.printf("Mínima: %.2f%n", stats.getMinima());
        System.out.printf("Mediana: %.2f%n", stats.getMediana());
        System.out.printf("Moda: %.2f (aparece %d veces)%n", stats.getModa(), stats.getFrecuenciaModa());
        System.out.printf("Desviación: %.2f%n", stats.getDesviacion());
        System.out.println("Tendencia: " + stats.getTendencia());
        System.out.println("Frecuencias:");
        for (Map.Entry<Double, Integer> f : stats.getFrecuencias().entrySet()) {
            System.out.println("  nota " + f.getKey() + " -> " + f.getValue() + " vez/veces");
        }
    }

    private void simularNota() {
        Estudiante est = seleccionarEstudiante();
        if (est == null) return;
        Curso curso = elegirCurso(est);
        if (curso == null) return;
        if (!simulador.hayEvaluacionesPendientes(curso)) {
            System.out.println("No hay evaluaciones pendientes para simular.");
            return;
        }
        double nota = leerDouble("¿Qué nota sacarías en la próxima evaluación pendiente? ");
        try {
            double nuevo = simulador.simularNota(curso, nota);
            System.out.printf("Promedio actual: %.2f%n", curso.calcularPromedio());
            System.out.printf("Promedio si sacas %.2f: %.2f%n", nota, nuevo);
        } catch (NotaInvalidaException e) {
            System.out.println("Nota inválida: " + e.getMessage());
        }
    }

    private void verNotaNecesaria() {
        Estudiante est = seleccionarEstudiante();
        if (est == null) return;
        Curso curso = elegirCurso(est);
        if (curso == null) return;
        if (!simulador.hayEvaluacionesPendientes(curso)) {
            System.out.println("No quedan evaluaciones pendientes en este curso.");
            return;
        }
        double meta = curso.getMetaPromedio();
        double necesaria = curso.notaNecesaria(); // delega en SimuladorNotas
        System.out.println("Meta del curso: " + meta);
        if (necesaria > Evaluacion.NOTA_MAXIMA) {
            System.out.printf("Imposible llegar a la meta: necesitarías %.2f (el máximo es 20).%n", necesaria);
        } else if (necesaria == 0) {
            System.out.println("¡Ya alcanzaste la meta! No necesitas una nota mínima.");
        } else {
            System.out.printf("Necesitas %.2f en promedio en las evaluaciones pendientes.%n", necesaria);
        }
        if (!simulador.esPosibleAprobar(curso)) {
            System.out.println("Atención: ya no es posible aprobar el curso.");
        }
    }

    private void exportarReporte() {
        Estudiante est = seleccionarEstudiante();
        if (est == null) return;
        String porDefecto = "datos/reporte_" + est.getCodigo() + ".csv";
        System.out.print("Ruta del archivo (Enter para " + porDefecto + "): ");
        String ruta = sc.nextLine().trim();
        if (ruta.isEmpty()) {
            ruta = porDefecto;
        }
        try {
            exportador.exportar(est, ruta);
            System.out.println("Reporte exportado en: " + ruta);
        } catch (IOException e) {
            System.out.println("No se pudo exportar: " + e.getMessage());
        }
    }

    // ===================== SELECCIÓN DE OBJETOS =====================

    // Con un solo estudiante lo usa directo; con varios pide el código (Enter = el actual)
    private Estudiante seleccionarEstudiante() {
        ArrayList<Estudiante> todos = gestor.getEstudiantes();
        if (todos.isEmpty()) {
            System.out.println("Primero registra un estudiante (opción 1).");
            return null;
        }
        if (todos.size() == 1) {
            gestor.setActual(todos.get(0));
            return todos.get(0);
        }
        while (true) {
            System.out.print("Código del estudiante (Enter para el actual): ");
            String codigo = sc.nextLine().trim();
            if (codigo.isEmpty() && gestor.getActual() != null) {
                return gestor.getActual();
            }
            Estudiante e = gestor.buscarEstudiante(codigo);
            if (e != null) {
                gestor.setActual(e);
                return e;
            }
            System.out.println("No existe un estudiante con ese código.");
        }
    }

    private Ciclo elegirCiclo(Estudiante est) {
        ArrayList<Ciclo> ciclos = est.getCiclos();
        if (ciclos.isEmpty()) {
            System.out.println("Este estudiante no tiene ciclos (opción 2).");
            return null;
        }
        for (int i = 0; i < ciclos.size(); i++) {
            System.out.println((i + 1) + ") " + ciclos.get(i).getNombre());
        }
        return ciclos.get(leerEntero("Elige el ciclo: ", 1, ciclos.size()) - 1);
    }

    private Curso elegirCursoDe(Ciclo ciclo) {
        ArrayList<Curso> cursos = ciclo.getCursos();
        if (cursos.isEmpty()) {
            System.out.println("Ese ciclo no tiene cursos (opción 3).");
            return null;
        }
        for (int i = 0; i < cursos.size(); i++) {
            System.out.println((i + 1) + ") " + cursos.get(i).getCodigo() + " - " + cursos.get(i).getNombre());
        }
        return cursos.get(leerEntero("Elige el curso: ", 1, cursos.size()) - 1);
    }

    private Curso elegirCurso(Estudiante est) {
        Ciclo ciclo = elegirCiclo(est);
        return ciclo == null ? null : elegirCursoDe(ciclo);
    }

    private Evaluacion elegirEvaluacion(Curso curso) {
        ArrayList<Evaluacion> evs = curso.getEvaluaciones();
        if (evs.isEmpty()) {
            System.out.println("Ese curso no tiene evaluaciones (opción 4).");
            return null;
        }
        for (int i = 0; i < evs.size(); i++) {
            System.out.println((i + 1) + ") " + evs.get(i));
        }
        return evs.get(leerEntero("Elige la evaluación: ", 1, evs.size()) - 1);
    }

    // ===================== LECTURA SEGURA DE DATOS =====================
    // Todas repiten la pregunta hasta que el dato sea válido.

    private String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String t = sc.nextLine().trim();
            if (!t.isEmpty()) {
                return t;
            }
            System.out.println("No puede estar vacío.");
        }
    }

    private int leerEntero(String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            try {
                int n = Integer.parseInt(sc.nextLine().trim());
                if (n >= min && n <= max) {
                    return n;
                }
                System.out.println("Debe estar entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número entero.");
            }
        }
    }

    // Sin rango: deja que el gestor valide (así se ve el mensaje de NotaInvalidaException)
    private double leerDouble(String mensaje) {
        return leerDouble(mensaje, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    private double leerDouble(String mensaje, double min, double max) {
        while (true) {
            System.out.print(mensaje);
            try {
                // Acepta coma o punto como separador decimal
                double n = Double.parseDouble(sc.nextLine().trim().replace(',', '.'));
                if (Double.isNaN(n) || Double.isInfinite(n)) {
                    System.out.println("Escribe un número válido.");
                } else if (n >= min && n <= max) {
                    return n;
                } else {
                    System.out.println("Debe estar entre " + min + " y " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número (ej. 14.5).");
            }
        }
    }

    // Fecha opcional: Enter la deja vacía (null)
    private LocalDate leerFecha() {
        while (true) {
            System.out.print("Fecha AAAA-MM-DD (Enter si no la sabes): ");
            String t = sc.nextLine().trim();
            if (t.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(t);
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Ejemplo: 2026-11-20");
            }
        }
    }

    private boolean leerSiNo(String mensaje) {
        while (true) {
            System.out.print(mensaje + " (s/n): ");
            String t = sc.nextLine().trim().toLowerCase();
            if (t.equals("s") || t.equals("si") || t.equals("sí")) return true;
            if (t.equals("n") || t.equals("no")) return false;
            System.out.println("Responde s o n.");
        }
    }
}