package app;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

import evaluaciones.Evaluacion;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;

/**
 * Clase utilitaria compartida para lectura segura por consola y selección
 * de entidades (estudiante, ciclo, curso, evaluación).
 */
public class Entrada {

    private Scanner sc;

    public Entrada() {
        this.sc = new Scanner(System.in);
    }

    // ===================== SELECCIÓN DE OBJETOS =====================

    // Con un solo estudiante lo usa directo; con varios pide el código (Enter = el actual)
    public Estudiante seleccionarEstudiante(GestorNotas gestor) {
        ArrayList<Estudiante> todos = gestor.getEstudiantes();
        if (todos.isEmpty()) {
            System.out.println("Primero registra un estudiante.");
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

    public Ciclo elegirCiclo(Estudiante est) {
        ArrayList<Ciclo> ciclos = est.getCiclos();
        if (ciclos.isEmpty()) {
            System.out.println("Este estudiante no tiene ciclos registrados.");
            return null;
        }
        for (int i = 0; i < ciclos.size(); i++) {
            System.out.println((i + 1) + ") " + ciclos.get(i).getNombre());
        }
        return ciclos.get(leerEntero("Elige el ciclo: ", 1, ciclos.size()) - 1);
    }

    public Curso elegirCursoDe(Ciclo ciclo) {
        ArrayList<Curso> cursos = ciclo.getCursos();
        if (cursos.isEmpty()) {
            System.out.println("Ese ciclo no tiene cursos registrados.");
            return null;
        }
        for (int i = 0; i < cursos.size(); i++) {
            System.out.println((i + 1) + ") " + cursos.get(i).getCodigo() + " - " + cursos.get(i).getNombre());
        }
        return cursos.get(leerEntero("Elige el curso: ", 1, cursos.size()) - 1);
    }

    public Curso elegirCurso(Estudiante est) {
        Ciclo ciclo = elegirCiclo(est);
        return ciclo == null ? null : elegirCursoDe(ciclo);
    }

    public Evaluacion elegirEvaluacion(Curso curso) {
        ArrayList<Evaluacion> evs = curso.getEvaluaciones();
        if (evs.isEmpty()) {
            System.out.println("Ese curso no tiene evaluaciones.");
            return null;
        }
        for (int i = 0; i < evs.size(); i++) {
            System.out.println((i + 1) + ") " + evs.get(i));
        }
        return evs.get(leerEntero("Elige la evaluación: ", 1, evs.size()) - 1);
    }

    // ===================== LECTURA SEGURA DE DATOS =====================

    public String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String t = sc.nextLine().trim();
            if (!t.isEmpty()) {
                return t;
            }
            System.out.println("No puede estar vacío.");
        }
    }

    // Útil para campos donde Enter significa "mantener actual" o "vacío"
    public String leerLinea(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }

    public int leerEntero(String mensaje, int min, int max) {
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

    public double leerDouble(String mensaje) {
        return leerDouble(mensaje, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public double leerDouble(String mensaje, double min, double max) {
        while (true) {
            System.out.print(mensaje);
            try {
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

    public LocalDate leerFecha() {
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

    public boolean leerSiNo(String mensaje) {
        while (true) {
            System.out.print(mensaje + " (s/n): ");
            String t = sc.nextLine().trim().toLowerCase();
            if (t.equals("s") || t.equals("si") || t.equals("sí")) return true;
            if (t.equals("n") || t.equals("no")) return false;
            System.out.println("Responde s o n.");
        }
    }
}