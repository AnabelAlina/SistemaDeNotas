package modelo;
public abstract class Persona {
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
        this.dni = validarTexto(dni, "dni");
    }
    public void setCorreo(String correo) {
        this.correo = validarTexto(correo, "correo");
    }
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    // Cada hijo decide su rol (polimorfismo)
    public abstract String getRol();
    // Método de ayuda reutilizable: rechaza textos nulos o vacíos
    protected static String validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor.trim();
    }
}