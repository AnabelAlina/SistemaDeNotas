package modelo;

import java.util.ArrayList;

public class Docente extends Persona {

    private static final long serialVersionUID = 1L;

    private String especialidad;
    private ArrayList<Curso> cursosDictados; // agregación: el curso existe sin el docente

    public Docente(String nombre, String apellido, String dni, String correo,
                   String especialidad) {
        super(nombre, apellido, dni, correo);
        this.especialidad = validarTexto(especialidad, "especialidad");
        this.cursosDictados = new ArrayList<>();
    }

    public String getEspecialidad() { return especialidad; }
    public ArrayList<Curso> getCursosDictados() { return cursosDictados; }

    public void agregarCurso(Curso curso) {
        if (curso == null) {
            throw new IllegalArgumentException("El curso no puede ser nulo.");
        }
        cursosDictados.add(curso);
    }

    @Override
    public String getRol() {
        return "Docente";
    }
}