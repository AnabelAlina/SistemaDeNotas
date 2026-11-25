package modelo;

import java.io.Serializable;

public abstract class Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    // private = encapsulamiento: solo se accede con getters y setters
    private String nombre;
    private String apellido;
    private String dni;
    private String correo;

    public Persona(String nombre, String apellido, String dni, String correo) {
        // Se usan los setters para que la validación se aplique también aquí
        setNombre(nombre);
        setApellido(apellido);
        setDni(dni);
        setCorreo(correo);
    }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public String getCorreo() { return correo; }

    public void setNombre(String nombre) {
        this.nombre = validarTexto(nombre, "nombre");
    }

    public void setApellido(String apellido) {
        this.apellido = validarTexto(apellido, "apellido");
    }

    public void setDni(String dni) {
        this.dni = validarDni(dni);
    }

    public void setCorreo(String correo) {
        this.correo = validarCorreo(correo);
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    // Cada hijo decide su rol (polimorfismo)
    public abstract String getRol();

    // Método de ayuda reutilizable: rechaza textos nulos o vacíos
    protected static String validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " no puede estar vacío.");
        }
        return valor.trim();
    }

    // DNI peruano: exactamente 8 dígitos. Es public static para que los menús
    // puedan validar el dato ANTES de crear el objeto.
    public static String validarDni(String dni) {
        String limpio = validarTexto(dni, "dni");
        if (!limpio.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos numéricos.");
        }
        return limpio;
    }

    // Formato general de correo: algo@dominio.ext (sin espacios)
    public static String validarCorreo(String correo) {
        String limpio = validarTexto(correo, "correo");
        if (!limpio.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido (ej. usuario@unsa.edu.pe).");
        }
        return limpio;
    }
}