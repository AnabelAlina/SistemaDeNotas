package app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import evaluaciones.Evaluacion;
import evaluaciones.TrabajoContinua;
import excepciones.NotaInvalidaException;
import modelo.Curso;
import modelo.Estudiante;
import persistencia.Bitacora;
import persistencia.Repositorio;

/**
 * Coordina la aplicación: guarda los estudiantes en memoria, registra notas
 * y delega el guardado/carga en un Repositorio.
 */
public class GestorNotas {

    private HashMap<String, Estudiante> estudiantes;
    private Estudiante actual;
    private Repositorio repositorio;
    private Bitacora bitacora;

    public GestorNotas(Repositorio repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio no puede ser nulo.");
        }
        this.repositorio = repositorio;
        this.estudiantes = new LinkedHashMap<>();
        this.actual = null;
        this.bitacora = null;
    }

    public void setRepositorio(Repositorio repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio no puede ser nulo.");
        }
        this.repositorio = repositorio;
    }

    public void setBitacora(Bitacora bitacora) {
        this.bitacora = bitacora;
    }

    private void log(String mensaje) {
        if (bitacora != null) {
            bitacora.registrar(mensaje);
        }
    }

    // MÉTODOS NUEVOS (para las ventanas)

    public void agregarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no puede ser nulo.");
        }
        if (estudiantes.containsKey(estudiante.getCodigo())) {
            throw new IllegalArgumentException(
                    "Ya existe un estudiante con el CUI/código " + estudiante.getCodigo() + ".");
        }
        if (existeDni(estudiante.getDni())) {
            throw new IllegalArgumentException(
                    "Ya existe un estudiante con el DNI " + estudiante.getDni() + ".");
        }
        estudiantes.put(estudiante.getCodigo(), estudiante);
        actual = estudiante;
        log("Estudiante registrado: " + estudiante.getNombreCompleto() + " (CUI " + estudiante.getCodigo() + ")");
    }

    public void asignarNota(Curso curso, Evaluacion evaluacion, double nota) throws NotaInvalidaException {
        if (curso == null || evaluacion == null) {
            throw new IllegalArgumentException("Debes elegir un curso y una evaluación.");
        }
        if (!curso.getEvaluaciones().contains(evaluacion)) {
            throw new IllegalArgumentException(
                    "Esa evaluación no pertenece al curso " + curso.getCodigo() + ".");
        }
        try {
            evaluacion.setNota(nota);
            log("Nota asignada en " + curso.getCodigo() + " (" + evaluacion.getNombre() + "): " + nota);
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void asignarNotaTrabajo(TrabajoContinua trabajo, double nota) throws NotaInvalidaException {
        if (trabajo == null) {
            throw new IllegalArgumentException("Debes elegir un trabajo.");
        }
        trabajo.setNota(nota);
        log("Nota de trabajo asignada (" + trabajo.getNombre() + "): " + nota);
    }

    public boolean quitarEstudiante(String cui) {
        if (cui == null) {
            return false;
        }
        Estudiante quitado = estudiantes.remove(cui.trim());
        if (quitado == null) {
            return false;
        }
        if (actual == quitado) {
            actual = null;
            for (Estudiante e : estudiantes.values()) {
                actual = e;
            }
        }
        return true;
    }

    public void guardar() throws IOException {
        repositorio.guardar(new ArrayList<>(estudiantes.values()));
        log("Datos guardados (" + repositorio.getClass().getSimpleName() + ")");
    }

    public void cargar() throws IOException {
        ArrayList<Estudiante> cargados = repositorio.cargar();
        estudiantes.clear();
        for (Estudiante e : cargados) {
            estudiantes.put(e.getCodigo(), e);
        }
        actual = cargados.isEmpty() ? null : cargados.get(cargados.size() - 1);
        log("Datos cargados (" + repositorio.getClass().getSimpleName() + ", " + cargados.size() + " estudiante(s))");
    }

    public ArrayList<String> getAdvertenciasCarga() {
        return repositorio.getAdvertencias();
    }

    // MÉTODOS VIEJOS (consola)

    public boolean registrarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no puede ser nulo.");
        }
        try {
            agregarEstudiante(estudiante);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean existeDni(String dni) {
        if (dni == null) {
            return false;
        }
        String limpio = dni.trim();
        for (Estudiante e : estudiantes.values()) {
            if (e.getDni().equals(limpio)) {
                return true;
            }
        }
        return false;
    }

    public Estudiante buscarEstudiante(String codigo) {
        if (codigo == null) {
            return null;
        }
        return estudiantes.get(codigo.trim());
    }

    public boolean registrarNota(Curso curso, Evaluacion evaluacion, double nota) {
        try {
            asignarNota(curso, evaluacion, nota);
            return true;
        } catch (NotaInvalidaException | IllegalArgumentException e) {
            System.out.println("No se pudo registrar la nota: " + e.getMessage());
            return false;
        }
    }

    public boolean registrarNotaTrabajo(TrabajoContinua trabajo, double nota) {
        try {
            asignarNotaTrabajo(trabajo, nota);
            return true;
        } catch (NotaInvalidaException | IllegalArgumentException e) {
            System.out.println("No se pudo registrar la nota: " + e.getMessage());
            return false;
        }
    }

    public boolean guardarTodo() {
        try {
            guardar();
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo guardar: " + e.getMessage());
            return false;
        }
    }

    public boolean cargarTodo() {
        try {
            cargar();
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo cargar: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<Estudiante> getEstudiantes() {
        return new ArrayList<>(estudiantes.values());
    }

    public Estudiante getActual() { return actual; }

    public void setActual(Estudiante estudiante) { this.actual = estudiante; }
}