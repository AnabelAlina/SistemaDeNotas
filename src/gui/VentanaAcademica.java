package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import app.GestorNotas;
import excepciones.PesoInvalidoException;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;
import modelo.Persona;

/**
 * Ventana de gestión académica: registrar estudiantes, agregar ciclos,
 * cargar cursos (predeterminados o manuales) y corregir (quitar, cambiar meta).
 * Reemplaza al MenuAcademico de la consola.
 */
public class VentanaAcademica extends JFrame {

    private GestorNotas gestor;
    private SelectorCurso selector;

    // Formulario del estudiante
    private JTextField txtNombres, txtApellidos, txtDni, txtCui, txtCorreo, txtCarrera;

    // Ciclo
    private JTextField txtCicloNombre, txtCicloAnio;

    // Cursos predeterminados
    private JTextField txtMetaPredeterminada;

    // Curso manual
    private JTextField txtCodigo, txtNombreCurso, txtCreditos, txtMetaManual;
    private JTextField txtCont1, txtExam1, txtCont2, txtExam2, txtCont3, txtExam3;

    // Corregir
    private JTextField txtNuevaMeta;

    public VentanaAcademica(GestorNotas gestor) {
        super("Gestión académica");
        if (gestor == null) {
            throw new IllegalArgumentException("El gestor no puede ser nulo.");
        }
        this.gestor = gestor;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Estilo.COLOR_FONDO);

        Escucha escucha = new Escucha();

        // NORTH: selector de estudiante, ciclo y curso
        selector = new SelectorCurso(gestor);
        add(selector, BorderLayout.NORTH);

        // WEST: formulario del estudiante
        add(crearPanelEstudiante(escucha), BorderLayout.WEST);

        // CENTER: ciclos, cursos y correcciones
        JPanel centro = new JPanel(new BorderLayout(5, 5));
        centro.setBackground(Estilo.COLOR_FONDO);

        JPanel arriba = new JPanel(new GridLayout(0, 1, 5, 5));
        arriba.setBackground(Estilo.COLOR_FONDO);
        arriba.add(crearPanelCiclo(escucha));
        arriba.add(crearPanelPredeterminados(escucha));
        centro.add(arriba, BorderLayout.NORTH);
        centro.add(crearPanelCursoManual(escucha), BorderLayout.CENTER);
        centro.add(crearPanelCorregir(escucha), BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        setVisible(true);
    }

    // ---------- Construcción de paneles ----------

    private JPanel crearPanelEstudiante(ActionListener escucha) {
        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        panel.setBackground(Estilo.COLOR_FONDO);
        panel.setBorder(BorderFactory.createTitledBorder("Registrar estudiante"));

        txtNombres = new JTextField(12);
        txtApellidos = new JTextField(12);
        txtDni = new JTextField(12);
        txtCui = new JTextField(12);
        txtCorreo = new JTextField(12);
        txtCarrera = new JTextField(12);

        agregarFila(panel, "Nombres:", txtNombres);
        agregarFila(panel, "Apellidos:", txtApellidos);
        agregarFila(panel, "DNI (8 dígitos):", txtDni);
        agregarFila(panel, "CUI (8 cifras):", txtCui);
        agregarFila(panel, "Correo (@unsa.edu.pe):", txtCorreo);
        agregarFila(panel, "Carrera:", txtCarrera);

        JButton registrar = new JButton("Registrar");
        registrar.setActionCommand("Registrar");
        registrar.addActionListener(escucha);
        JButton limpiar = new JButton("Limpiar");
        limpiar.setActionCommand("Limpiar");
        limpiar.addActionListener(escucha);
        panel.add(registrar);
        panel.add(limpiar);
        return panel;
    }

    private JPanel crearPanelCiclo(ActionListener escucha) {
        JPanel panel = new JPanel(new GridLayout(1, 5, 5, 5));
        panel.setBackground(Estilo.COLOR_FONDO);
        panel.setBorder(BorderFactory.createTitledBorder("Ciclo (del estudiante elegido arriba)"));

        txtCicloNombre = new JTextField(8);
        txtCicloAnio = new JTextField(6);
        panel.add(etiqueta("Nombre (ej. 2026-B):"));
        panel.add(txtCicloNombre);
        panel.add(etiqueta("Año:"));
        panel.add(txtCicloAnio);
        panel.add(crearBoton("Agregar ciclo", escucha));
        return panel;
    }

    private JPanel crearPanelPredeterminados(ActionListener escucha) {
        JPanel panel = new JPanel(new GridLayout(1, 3, 5, 5));
        panel.setBackground(Estilo.COLOR_FONDO);
        panel.setBorder(BorderFactory.createTitledBorder("Cursos predeterminados (del ciclo elegido arriba)"));

        txtMetaPredeterminada = new JTextField("14", 5);
        panel.add(etiqueta("Meta de promedio (0 a 20):"));
        panel.add(txtMetaPredeterminada);
        panel.add(crearBoton("Cargar cursos predeterminados", escucha));
        return panel;
    }

    private JPanel crearPanelCursoManual(ActionListener escucha) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Estilo.COLOR_FONDO);
        panel.setBorder(BorderFactory.createTitledBorder("Curso manual (los 6 porcentajes deben sumar 100)"));

        txtCodigo = new JTextField(8);
        txtNombreCurso = new JTextField(12);
        txtCreditos = new JTextField(4);
        txtMetaManual = new JTextField("14", 4);
        txtCont1 = new JTextField(4);
        txtExam1 = new JTextField(4);
        txtCont2 = new JTextField(4);
        txtExam2 = new JTextField(4);
        txtCont3 = new JTextField(4);
        txtExam3 = new JTextField(4);

        JPanel datos = new JPanel(new GridLayout(2, 4, 5, 5));
        datos.setBackground(Estilo.COLOR_FONDO);
        agregarCelda(datos, "Código:", txtCodigo);
        agregarCelda(datos, "Nombre:", txtNombreCurso);
        agregarCelda(datos, "Créditos (1 a 10):", txtCreditos);
        agregarCelda(datos, "Meta (0 a 20):", txtMetaManual);

        JPanel porcentajes = new JPanel(new GridLayout(2, 6, 5, 5));
        porcentajes.setBackground(Estilo.COLOR_FONDO);
        porcentajes.add(etiqueta("Continua 1 %"));
        porcentajes.add(etiqueta("Examen 1 %"));
        porcentajes.add(etiqueta("Continua 2 %"));
        porcentajes.add(etiqueta("Examen 2 %"));
        porcentajes.add(etiqueta("Continua 3 %"));
        porcentajes.add(etiqueta("Examen 3 %"));
        porcentajes.add(txtCont1);
        porcentajes.add(txtExam1);
        porcentajes.add(txtCont2);
        porcentajes.add(txtExam2);
        porcentajes.add(txtCont3);
        porcentajes.add(txtExam3);

        JPanel boton = new JPanel(new GridLayout(1, 1));
        boton.setBackground(Estilo.COLOR_FONDO);
        boton.add(crearBoton("Agregar curso manual", escucha));

        panel.add(datos, BorderLayout.NORTH);
        panel.add(porcentajes, BorderLayout.CENTER);
        panel.add(boton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelCorregir(ActionListener escucha) {
        JPanel panel = new JPanel(new GridLayout(1, 5, 5, 5));
        panel.setBackground(Estilo.COLOR_FONDO);
        panel.setBorder(BorderFactory.createTitledBorder("Corregir (usa el curso y ciclo elegidos arriba)"));

        txtNuevaMeta = new JTextField(4);
        panel.add(crearBoton("Quitar curso", escucha));
        panel.add(crearBoton("Quitar ciclo", escucha));
        panel.add(etiqueta("Nueva meta:"));
        panel.add(txtNuevaMeta);
        panel.add(crearBoton("Cambiar meta", escucha));
        return panel;
    }

    // ---------- Ayudas de construcción ----------

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.FUENTE_NORMAL);
        l.setForeground(Estilo.COLOR_TEXTO);
        return l;
    }

    private JButton crearBoton(String texto, ActionListener escucha) {
        JButton b = new JButton(texto);
        b.setActionCommand(texto);
        b.addActionListener(escucha);
        return b;
    }

    private void agregarFila(JPanel panel, String texto, JTextField campo) {
        panel.add(etiqueta(texto));
        panel.add(campo);
    }

    private void agregarCelda(JPanel panel, String texto, JTextField campo) {
        panel.add(etiqueta(texto));
        panel.add(campo);
    }

    // ---------- Mensajes ----------

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    // Muestra el error y deja el cursor en el campo que falló (los demás conservan lo escrito)
    private void errorEnCampo(String mensaje, JTextField campo) {
        mostrarError(mensaje);
        campo.requestFocus();
    }

    // Acepta coma o punto decimal. Lanza NumberFormatException si no es número
    private double leerNumero(JTextField campo) {
        String texto = campo.getText().trim().replace(',', '.');
        return Double.parseDouble(texto);
    }

    // ---------- Acciones ----------

    private void registrarEstudiante() {
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String dni = txtDni.getText().trim();
        String cui = txtCui.getText().trim();
        String correo = txtCorreo.getText().trim();
        String carrera = txtCarrera.getText().trim();

        // 1. DNI
        try {
            dni = Persona.validarDni(dni);
        } catch (IllegalArgumentException e) {
            errorEnCampo(e.getMessage(), txtDni);
            return;
        }
        if (gestor.existeDni(dni)) {
            errorEnCampo("Ya existe un estudiante con el DNI " + dni + ".", txtDni);
            return;
        }

        // 2. CUI
        try {
            cui = Estudiante.validarCui(cui);
        } catch (IllegalArgumentException e) {
            errorEnCampo(e.getMessage(), txtCui);
            return;
        }
        if (gestor.buscarEstudiante(cui) != null) {
            errorEnCampo("Ya existe un estudiante con el CUI/código " + cui + ".", txtCui);
            return;
        }

        // 3. Correo
        try {
            correo = Estudiante.validarCorreoInstitucional(correo);
        } catch (IllegalArgumentException e) {
            errorEnCampo(e.getMessage(), txtCorreo);
            return;
        }

        // 4. Textos que no pueden estar vacíos
        if (nombres.isEmpty()) {
            errorEnCampo("Los nombres no pueden estar vacíos.", txtNombres);
            return;
        }
        if (apellidos.isEmpty()) {
            errorEnCampo("Los apellidos no pueden estar vacíos.", txtApellidos);
            return;
        }
        if (carrera.isEmpty()) {
            errorEnCampo("La carrera no puede estar vacía.", txtCarrera);
            return;
        }

        // 5. Todo válido: se crea y se registra
        try {
            Estudiante estudiante = new Estudiante(nombres, apellidos, dni, correo, cui, carrera);
            gestor.agregarEstudiante(estudiante);
            mostrarInfo("Estudiante registrado: " + estudiante.getNombreCompleto()
                    + " (CUI " + estudiante.getCodigo() + ").");
            limpiarFormulario();
            selector.recargar();
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtNombres.setText("");
        txtApellidos.setText("");
        txtDni.setText("");
        txtCui.setText("");
        txtCorreo.setText("");
        txtCarrera.setText("");
        txtNombres.requestFocus();
    }

    private void agregarCiclo() {
        Estudiante estudiante = selector.getEstudiante();
        if (estudiante == null) {
            mostrarError("Primero registra o elige un estudiante.");
            return;
        }
        int anio;
        try {
            anio = Integer.parseInt(txtCicloAnio.getText().trim());
        } catch (NumberFormatException e) {
            errorEnCampo("El año debe ser un número entero (ej. 2026).", txtCicloAnio);
            return;
        }
        try {
            Ciclo ciclo = new Ciclo(txtCicloNombre.getText(), anio);
            estudiante.agregarCiclo(ciclo);
            txtCicloNombre.setText("");
            txtCicloAnio.setText("");
            selector.recargar();
            mostrarInfo("Ciclo " + ciclo.getNombre() + " agregado. Elígelo en el selector de arriba para cargarle cursos.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cargarPredeterminados() {
        Ciclo ciclo = selector.getCiclo();
        if (ciclo == null) {
            mostrarError("Primero elige un estudiante y un ciclo.");
            return;
        }
        double meta;
        try {
            meta = leerNumero(txtMetaPredeterminada);
        } catch (NumberFormatException e) {
            errorEnCampo("La meta debe ser un número entre 0 y 20.", txtMetaPredeterminada);
            return;
        }
        if (meta < 0 || meta > 20) {
            errorEnCampo("La meta debe estar entre 0 y 20.", txtMetaPredeterminada);
            return;
        }
        try {
            int agregados = ciclo.agregarCursosPredeterminados(meta);
            if (agregados == 0) {
                mostrarInfo("Ese ciclo ya tenía todos los cursos.");
            } else {
                mostrarInfo("Se cargaron " + agregados + " cursos.");
            }
            selector.recargar();
        } catch (PesoInvalidoException | IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    private void agregarCursoManual() {
        Ciclo ciclo = selector.getCiclo();
        if (ciclo == null) {
            mostrarError("Primero elige un estudiante y un ciclo.");
            return;
        }
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombreCurso.getText().trim();
        if (codigo.isEmpty()) {
            errorEnCampo("El código del curso no puede estar vacío.", txtCodigo);
            return;
        }
        if (nombre.isEmpty()) {
            errorEnCampo("El nombre del curso no puede estar vacío.", txtNombreCurso);
            return;
        }

        int creditos;
        try {
            creditos = Integer.parseInt(txtCreditos.getText().trim());
        } catch (NumberFormatException e) {
            errorEnCampo("Los créditos deben ser un número entero.", txtCreditos);
            return;
        }
        if (creditos < 1 || creditos > 10) {
            errorEnCampo("Los créditos deben estar entre 1 y 10.", txtCreditos);
            return;
        }

        double meta;
        try {
            meta = leerNumero(txtMetaManual);
        } catch (NumberFormatException e) {
            errorEnCampo("La meta debe ser un número entre 0 y 20.", txtMetaManual);
            return;
        }
        if (meta < 0 || meta > 20) {
            errorEnCampo("La meta debe estar entre 0 y 20.", txtMetaManual);
            return;
        }

        JTextField[] camposContinua = {txtCont1, txtCont2, txtCont3};
        JTextField[] camposExamen = {txtExam1, txtExam2, txtExam3};
        double[] pesosContinua = new double[3];
        double[] pesosExamen = new double[3];
        for (int i = 0; i < 3; i++) {
            try {
                pesosContinua[i] = leerNumero(camposContinua[i]);
            } catch (NumberFormatException e) {
                errorEnCampo("El porcentaje de la Continua " + (i + 1) + " debe ser un número.", camposContinua[i]);
                return;
            }
            try {
                pesosExamen[i] = leerNumero(camposExamen[i]);
            } catch (NumberFormatException e) {
                errorEnCampo("El porcentaje del Examen " + (i + 1) + " debe ser un número.", camposExamen[i]);
                return;
            }
        }

        try {
            Curso curso = ciclo.agregarCursoManual(codigo, nombre, creditos, meta, pesosExamen, pesosContinua);
            mostrarInfo("Curso agregado: " + curso.getCodigo() + " - " + curso.getNombre() + ".");
            limpiarCursoManual();
            selector.recargar();
        } catch (PesoInvalidoException | IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    private void limpiarCursoManual() {
        JTextField[] campos = {txtCodigo, txtNombreCurso, txtCreditos,
            txtCont1, txtExam1, txtCont2, txtExam2, txtCont3, txtExam3};
        for (JTextField c : campos) {
            c.setText("");
        }
    }

    private void quitarCurso() {
        Ciclo ciclo = selector.getCiclo();
        Curso curso = selector.getCurso();
        if (ciclo == null || curso == null) {
            mostrarError("Elige primero el curso que quieres quitar.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "¿Quitar el curso " + curso.getCodigo() + " - " + curso.getNombre()
                + "? Se perderán sus notas.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            ciclo.quitarCurso(curso.getCodigo());
            selector.recargar();
        }
    }

    private void quitarCiclo() {
        Estudiante estudiante = selector.getEstudiante();
        Ciclo ciclo = selector.getCiclo();
        if (estudiante == null || ciclo == null) {
            mostrarError("Elige primero el ciclo que quieres quitar.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "¿Quitar el ciclo " + ciclo.getNombre() + " con todos sus cursos y notas?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            estudiante.quitarCiclo(ciclo.getNombre());
            selector.recargar();
        }
    }

    private void cambiarMeta() {
        Curso curso = selector.getCurso();
        if (curso == null) {
            mostrarError("Elige primero un curso.");
            return;
        }
        double meta;
        try {
            meta = leerNumero(txtNuevaMeta);
        } catch (NumberFormatException e) {
            errorEnCampo("La meta debe ser un número entre 0 y 20.", txtNuevaMeta);
            return;
        }
        try {
            curso.setMetaPromedio(meta);
            mostrarInfo("Meta de " + curso.getCodigo() + " cambiada a " + meta + ".");
            txtNuevaMeta.setText("");
        } catch (IllegalArgumentException e) {
            errorEnCampo(e.getMessage(), txtNuevaMeta);
        }
    }

    // ---------- Un solo listener para todos los botones ----------

    private class Escucha implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String comando = e.getActionCommand();
            if (comando.equals("Registrar")) {
                registrarEstudiante();
            } else if (comando.equals("Limpiar")) {
                limpiarFormulario();
            } else if (comando.equals("Agregar ciclo")) {
                agregarCiclo();
            } else if (comando.equals("Cargar cursos predeterminados")) {
                cargarPredeterminados();
            } else if (comando.equals("Agregar curso manual")) {
                agregarCursoManual();
            } else if (comando.equals("Quitar curso")) {
                quitarCurso();
            } else if (comando.equals("Quitar ciclo")) {
                quitarCiclo();
            } else if (comando.equals("Cambiar meta")) {
                cambiarMeta();
            }
        }
    }

    // ---------- main TEMPORAL para probar la ventana sola (Karol lo quita en el PR 6) ----------
    public static void main(String[] args) {
        GestorNotas gestor = new GestorNotas(new persistencia.RepositorioArchivo("datos/notas.txt"));
        new VentanaAcademica(gestor);
    }
}
