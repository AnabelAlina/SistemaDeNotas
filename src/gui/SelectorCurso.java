package gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import app.GestorNotas;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;

/**
 * Panel con tres JComboBox encadenados: estudiante, ciclo y curso.
 * Al elegir un estudiante se llenan sus ciclos; al elegir un ciclo, sus cursos.
 * Reemplaza a Entrada.seleccionar* de la consola.
 */
public class SelectorCurso extends JPanel {

    private GestorNotas gestor;

    private JComboBox<String> comboEstudiante;
    private JComboBox<String> comboCiclo;
    private JComboBox<String> comboCurso;

    // Listas paralelas: la posición i de cada lista corresponde a la opción i del combo
    private ArrayList<Estudiante> estudiantes;
    private ArrayList<Ciclo> ciclos;
    private ArrayList<Curso> cursos;

    // Quienes quieren enterarse cuando cambia la selección
    private ArrayList<ActionListener> oyentes;

    // true mientras se rellenan los combos por código: evita avisos repetidos
    private boolean cargando;

    public SelectorCurso(GestorNotas gestor) {
        if (gestor == null) {
            throw new IllegalArgumentException("El gestor no puede ser nulo.");
        }
        this.gestor = gestor;
        this.estudiantes = new ArrayList<>();
        this.ciclos = new ArrayList<>();
        this.cursos = new ArrayList<>();
        this.oyentes = new ArrayList<>();
        this.cargando = false;

        setLayout(new GridLayout(1, 3, 5, 5));
        setBackground(Estilo.COLOR_FONDO);

        comboEstudiante = new JComboBox<>();
        comboCiclo = new JComboBox<>();
        comboCurso = new JComboBox<>();
        comboEstudiante.setFont(Estilo.FUENTE_NORMAL);
        comboCiclo.setFont(Estilo.FUENTE_NORMAL);
        comboCurso.setFont(Estilo.FUENTE_NORMAL);

        Escucha escucha = new Escucha();
        comboEstudiante.addActionListener(escucha);
        comboCiclo.addActionListener(escucha);
        comboCurso.addActionListener(escucha);

        add(comboEstudiante);
        add(comboCiclo);
        add(comboCurso);

        // Primera carga (sin avisar a nadie: todavía no hay oyentes)
        cargando = true;
        llenarEstudiantes(null, null, null);
        cargando = false;
    }

    // ---------- Lo que devuelven los combos ----------

    public Estudiante getEstudiante() {
        int i = comboEstudiante.getSelectedIndex();
        return (i >= 0 && i < estudiantes.size()) ? estudiantes.get(i) : null;
    }

    public Ciclo getCiclo() {
        int i = comboCiclo.getSelectedIndex();
        return (i >= 0 && i < ciclos.size()) ? ciclos.get(i) : null;
    }

    public Curso getCurso() {
        int i = comboCurso.getSelectedIndex();
        return (i >= 0 && i < cursos.size()) ? cursos.get(i) : null;
    }

    // ---------- Recargar ----------

    // Vuelve a leer del gestor y trata de conservar lo que estaba elegido
    public void recargar() {
        Estudiante e = getEstudiante();
        Ciclo c = getCiclo();
        Curso cu = getCurso();
        String cui = (e == null) ? null : e.getCodigo();
        String nombreCiclo = (c == null) ? null : c.getNombre();
        String codigoCurso = (cu == null) ? null : cu.getCodigo();

        cargando = true;
        try {
            llenarEstudiantes(cui, nombreCiclo, codigoCurso);
        } finally {
            cargando = false;
        }
        avisar();
    }

    // ---------- Oyentes ----------

    public void addActionListener(ActionListener l) {
        if (l != null) {
            oyentes.add(l);
        }
    }

    private void avisar() {
        ActionEvent evento = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "seleccion");
        for (ActionListener l : new ArrayList<>(oyentes)) {
            l.actionPerformed(evento);
        }
    }

    // ---------- Llenado encadenado ----------
    // Estos tres métodos se llaman con "cargando" en true.

    private void llenarEstudiantes(String cui, String nombreCiclo, String codigoCurso) {
        estudiantes = gestor.getEstudiantes();
        comboEstudiante.removeAllItems();
        int elegido = 0;
        for (int i = 0; i < estudiantes.size(); i++) {
            Estudiante e = estudiantes.get(i);
            comboEstudiante.addItem(e.getNombreCompleto() + " (" + e.getCodigo() + ")");
            if (cui != null && cui.equals(e.getCodigo())) {
                elegido = i;
            }
        }
        if (!estudiantes.isEmpty()) {
            comboEstudiante.setSelectedIndex(elegido);
        }
        llenarCiclos(nombreCiclo, codigoCurso);
    }

    private void llenarCiclos(String nombreCiclo, String codigoCurso) {
        Estudiante e = getEstudiante();
        ciclos = (e == null) ? new ArrayList<Ciclo>() : new ArrayList<>(e.getCiclos());
        comboCiclo.removeAllItems();
        int elegido = 0;
        for (int i = 0; i < ciclos.size(); i++) {
            Ciclo c = ciclos.get(i);
            comboCiclo.addItem(c.getNombre());
            if (nombreCiclo != null && nombreCiclo.equalsIgnoreCase(c.getNombre())) {
                elegido = i;
            }
        }
        if (!ciclos.isEmpty()) {
            comboCiclo.setSelectedIndex(elegido);
        }
        llenarCursos(codigoCurso);
    }

    private void llenarCursos(String codigoCurso) {
        Ciclo c = getCiclo();
        cursos = (c == null) ? new ArrayList<Curso>() : new ArrayList<>(c.getCursos());
        comboCurso.removeAllItems();
        int elegido = 0;
        for (int i = 0; i < cursos.size(); i++) {
            Curso cu = cursos.get(i);
            comboCurso.addItem(cu.getCodigo() + " - " + cu.getNombre());
            if (codigoCurso != null && codigoCurso.equalsIgnoreCase(cu.getCodigo())) {
                elegido = i;
            }
        }
        if (!cursos.isEmpty()) {
            comboCurso.setSelectedIndex(elegido);
        }
    }

    // ---------- Listener interno ----------

    private class Escucha implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (cargando) {
                return; // estamos rellenando por código: se avisa una sola vez al final
            }
            cargando = true;
            try {
                if (e.getSource() == comboEstudiante) {
                    llenarCiclos(null, null);
                } else if (e.getSource() == comboCiclo) {
                    llenarCursos(null);
                }
            } finally {
                cargando = false;
            }
            avisar();
        }
    }

    // ---------- main TEMPORAL para probar el selector solo (Karol lo quita en el PR 6) ----------
    public static void main(String[] args) throws Exception {
        GestorNotas gestor = new GestorNotas(new persistencia.RepositorioArchivo("datos/notas.txt"));
        Estudiante a = new Estudiante("Ana", "Pérez", "71234567", "ana@unsa.edu.pe", "20261111", "Sistemas");
        Estudiante b = new Estudiante("Luis", "Ramos", "72345678", "luis@unsa.edu.pe", "20262222", "Sistemas");
        a.agregarCiclo(new Ciclo("2026-A", 2026));
        a.agregarCiclo(new Ciclo("2026-B", 2026));
        a.getCicloActual().agregarCursosPredeterminados(14);
        b.agregarCiclo(new Ciclo("2026-B", 2026));
        gestor.agregarEstudiante(a);
        gestor.agregarEstudiante(b);

        javax.swing.JFrame f = new javax.swing.JFrame("Prueba SelectorCurso");
        SelectorCurso selector = new SelectorCurso(gestor);
        f.add(selector);
        f.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        f.setSize(700, 80);
        f.setVisible(true);
    }
}
