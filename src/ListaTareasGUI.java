import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ListaTareasGUI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DefaultListModel<Tarea> modeloLista;
    private JList<Tarea> listaTareas;
    private JTextField campoNuevaTarea;
    private JButton botonAgregar, botonEliminar, botonCompletar;
    private final String ARCHIVO_DATOS = "tareas.txt";

    public ListaTareasGUI() {
        // Configuración de la ventana principal
        setTitle("Lista de Tareas");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicialización de componentes
        modeloLista = new DefaultListModel<>();
        listaTareas = new JList<>(modeloLista);
        listaTareas.setCellRenderer(new TareaCellRenderer());
        
        campoNuevaTarea = new JTextField(20);
        botonAgregar = new JButton("Agregar Tarea");
        botonEliminar = new JButton("Eliminar Tarea");
        botonCompletar = new JButton("Marcar como Completada");

        // Panel para nueva tarea
        JPanel panelNuevaTarea = new JPanel();
        panelNuevaTarea.add(new JLabel("Nueva Tarea:"));
        panelNuevaTarea.add(campoNuevaTarea);
        panelNuevaTarea.add(botonAgregar);

        // Panel de botones
        JPanel panelBotones = new JPanel();
        panelBotones.add(botonEliminar);
        panelBotones.add(botonCompletar);

        // Organización de paneles en la ventana
        setLayout(new BorderLayout());
        add(new JScrollPane(listaTareas), BorderLayout.CENTER);
        add(panelNuevaTarea, BorderLayout.NORTH);
        add(panelBotones, BorderLayout.SOUTH);

        // Cargar tareas existentes
        cargarTareas();

        // Configuración de listeners
        configurarEventos();
    }

    private void configurarEventos() {
        // Agregar tarea
        botonAgregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String textoTarea = campoNuevaTarea.getText().trim();
                if (!textoTarea.isEmpty()) {
                    Tarea nuevaTarea = new Tarea(textoTarea);
                    modeloLista.addElement(nuevaTarea);
                    campoNuevaTarea.setText("");
                    guardarTareas();
                }
            }
        });

        // Agregar tarea presionando Enter
        campoNuevaTarea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    botonAgregar.doClick();
                }
            }
        });

        // Eliminar tarea
        botonEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int indiceSeleccionado = listaTareas.getSelectedIndex();
                if (indiceSeleccionado != -1) {
                    modeloLista.remove(indiceSeleccionado);
                    guardarTareas();
                } else {
                    JOptionPane.showMessageDialog(ListaTareasGUI.this, 
                        "Por favor, seleccione una tarea para eliminar.", 
                        "Ninguna tarea seleccionada", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Marcar tarea como completada
        botonCompletar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int indiceSeleccionado = listaTareas.getSelectedIndex();
                if (indiceSeleccionado != -1) {
                    Tarea tarea = modeloLista.getElementAt(indiceSeleccionado);
                    tarea.toggleCompletada();
                    listaTareas.repaint();
                    guardarTareas();
                } else {
                    JOptionPane.showMessageDialog(ListaTareasGUI.this, 
                        "Por favor, seleccione una tarea para marcar como completada.", 
                        "Ninguna tarea seleccionada", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Guardar al cerrar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                guardarTareas();
            }
        });
    }

    private void cargarTareas() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(ARCHIVO_DATOS));
            String linea;
            while ((linea = reader.readLine()) != null) {
                // Formato: "descripción:completada"
                String[] partes = linea.split(":", 2);
                if (partes.length == 2) {
                    Tarea tarea = new Tarea(partes[0]);
                    if (Boolean.parseBoolean(partes[1])) {
                        tarea.toggleCompletada();
                    }
                    modeloLista.addElement(tarea);
                }
            }
        } catch (IOException e) {
            // Si el archivo no existe, simplemente comenzará con una lista vacía
            System.out.println("Archivo de tareas no encontrado. Se creará uno nuevo al guardar.");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void guardarTareas() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(ARCHIVO_DATOS));
            for (int i = 0; i < modeloLista.getSize(); i++) {
                Tarea tarea = modeloLista.getElementAt(i);
                writer.write(tarea.getDescripcion() + ":" + tarea.isCompletada());
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al guardar las tareas: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class TareaCellRenderer extends DefaultListCellRenderer {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Component getListCellRendererComponent(JList<?> list, Object value, 
            int index, boolean isSelected, boolean cellHasFocus) {
        
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value instanceof Tarea) {
            Tarea tarea = (Tarea) value;
            setText(tarea.getDescripcion());
            
            if (tarea.isCompletada()) {
                setFont(getFont().deriveFont(Font.ITALIC));
                setForeground(Color.GRAY);
                setText("✓ " + tarea.getDescripcion());
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
                setForeground(list.getForeground());
            }
        }
        
        return c;
    }
}