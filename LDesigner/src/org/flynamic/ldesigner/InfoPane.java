package org.flynamic.ldesigner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.flynamic.ldesigner.DesignerPane.EntityPicker;
import org.flynamic.ldesigner.InfoPane.Inspector.Property;
import org.flynamic.ldesigner.InfoPane.Inspector.Tab;
import org.flynamic.ldesigner.util.ScrollablePanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

public class InfoPane extends JPanel {
	public DesignerPanel designerPanel;
	private Inspector inspector;
	private DefaultTableModel model;
	private JLabel inspectorLabel;
	private JScrollPane scrollPane;
	private ScrollablePanel infoTable;
	private JTabbedPane tabbedPane;
	private JPanel inspected;
	private JTextPane description;

	/**
	 * Create the panel.
	 */
	public InfoPane() {
		setBackground(UIManager.getColor("Button.background"));
		setLayout(new BorderLayout(0, 0));

		inspected = new JPanel();
		add(inspected, BorderLayout.NORTH);
		inspected.setLayout(new BorderLayout(0, 0));

		inspectorLabel = new JLabel("Inspected Element");
		inspected.add(inspectorLabel, BorderLayout.NORTH);
		inspectorLabel.setHorizontalAlignment(SwingConstants.CENTER);
		inspectorLabel.setForeground(Color.GRAY);
		inspectorLabel.setFont(new Font("Lucida Grande", Font.BOLD, 16));

		description = new JTextPane();
		description.setContentType("text/html");
		description.setBackground(UIManager.getColor("Button.background"));
		description.setEditable(false);
		description.setText("Description.");
		inspected.add(description, BorderLayout.CENTER);

		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		scrollPane = new JScrollPane();
		tabbedPane.addTab("New tab", null, scrollPane, null);
		scrollPane.setViewportBorder(null);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		infoTable = new ScrollablePanel();
		scrollPane.setViewportView(infoTable);
		scrollPane.getViewport().setBackground(infoTable.getBackground());
	}

	public void update() {
		if (getInspector() == null) {
			return;
		}
		this.getInspectorLabel()
				.setText(this.getInspector().getInspectorName());

		tabbedPane.removeAll();
		for (Tab tab : this.getInspector().getTabs()) {
			JScrollPane scrollPane = new JScrollPane();
			tabbedPane.addTab(tab.getTitle(), null, scrollPane, null);
			scrollPane.setViewportBorder(null);
			scrollPane.setBorder(BorderFactory.createEmptyBorder());

			ScrollablePanel infoTable = new ScrollablePanel();
			scrollPane.setViewportView(infoTable);
			scrollPane.getViewport().setBackground(infoTable.getBackground());

			tab.setInspector(getInspector());
			infoTable.add(getPropertiesPane(tab, tab.getProperties()));
		}
	}

	public JPanel getPropertiesPane(Tab tab,
			LinkedHashMap<String, Object> properties) {
		FormLayout form = new FormLayout("fill:min, 3dlu, min(60dlu; pref)");
		DefaultFormBuilder builder = new DefaultFormBuilder(form);
		builder.setLineGapSize(Sizes.pixel(10));
		CellConstraints cc = new CellConstraints();

		int row = 1;
		for (String key : properties.keySet()) {
			Object value = properties.get(key);
			final String propertyKey;

			if (!(value instanceof Inspector.PropertyType || value instanceof Inspector.Property)) {
				Property dataProperty = new Property(
						Inspector.PropertyType.Data, value);
				value = dataProperty;
			}

			if (value instanceof Inspector.Property) {
				if (((Inspector.Property) value).getInternalKey() != null) {
					propertyKey = ((Inspector.Property) value).getInternalKey();
				} else {
					propertyKey = key;
				}
			} else {
				propertyKey = key;
			}

			if (value instanceof Inspector.PropertyType
					|| value instanceof Inspector.Property) {
				if (value == Inspector.PropertyType.Seperator) {
					form.appendRow(FormFactory.DEFAULT_ROWSPEC);
					builder.addSeparator(key, cc.xyw(1, row, 3));
				}
				if (value instanceof Inspector.Property) {
					Inspector.Property prop = (Inspector.Property) value;

					if (prop.type == Inspector.PropertyType.Data) {
						Object data = prop.value;

						JPanel valuePanel = new JPanel();
						valuePanel.setLayout(new FlowLayout());
						((FlowLayout) valuePanel.getLayout())
								.setAlignment(FlowLayout.LEFT);

						if (data.getClass().isArray()) {
							for (int i = 0; i < Array.getLength(data); i++) {
								final int index = i;
								Object o = Array.get(data, i);
								JTextField textField = new JTextField();
								textField.setText(o.toString());
								textField.setColumns(3);
								textField.setCaretPosition(0);
								textField.addFocusListener(new FocusListener() {

									@Override
									public void focusGained(FocusEvent e) {
									}

									@Override
									public void focusLost(FocusEvent e) {
										setProperty(tab, propertyKey + "["
												+ index + "]",
												textField.getText());
									}

								});
								Action action = new AbstractAction() {
									@Override
									public void actionPerformed(ActionEvent e) {
										setProperty(tab, propertyKey + "["
												+ index + "]",
												textField.getText());
									}
								};
								textField.addActionListener(action);
								valuePanel.add(textField);
							}
						} else {
							if (data instanceof Boolean) {
								JCheckBox check = new JCheckBox();
								check.setSelected((Boolean) data);
								check.addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										setProperty(tab, propertyKey,
												check.isSelected());
									}

								});
								valuePanel.add(check);
							} else if (data instanceof Double || data instanceof Integer || data instanceof Float) {
								SpinnerNumberModel model = new SpinnerNumberModel();
								model.setValue(data);
								model.setStepSize(0.1);
								JSpinner spinner = new JSpinner();
								JSpinner.NumberEditor editor = (JSpinner.NumberEditor)spinner.getEditor();
								DecimalFormat format = editor.getFormat();
								format.setMinimumFractionDigits(3);
								editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
								Dimension d = spinner.getPreferredSize();
								d.width = 85;
								spinner.setPreferredSize(d);
								spinner.setValue(data);
								spinner.setEditor(editor);
								spinner.setSize(150, 20);
								spinner.setModel(model);
								spinner.addFocusListener(new FocusListener() {

									@Override
									public void focusGained(FocusEvent e) {
									}

									@Override
									public void focusLost(FocusEvent e) {
										setProperty(tab, propertyKey,
												spinner.getValue());
									}

								});
								spinner.addChangeListener(new ChangeListener() {

									@Override
									public void stateChanged(ChangeEvent e) {
										spinner.setValue(spinner.getModel().getValue());
										setProperty(tab, propertyKey,
												spinner.getModel().getValue());
									}
									
								});
								valuePanel.add(spinner);
							} else {
								JTextField textField = new JTextField();
								textField.setText(data.toString());
								textField.setColumns(8);
								textField.setCaretPosition(0);
								textField.addFocusListener(new FocusListener() {

									@Override
									public void focusGained(FocusEvent e) {
									}

									@Override
									public void focusLost(FocusEvent e) {
										setProperty(tab, propertyKey,
												textField.getText());
									}

								});
								Action action = new AbstractAction() {
									private static final long serialVersionUID = 1L;

									@Override
									public void actionPerformed(ActionEvent e) {
										setProperty(tab, propertyKey,
												textField.getText());
									}
								};
								textField.addActionListener(action);
								valuePanel.add(textField);
							}
						}
						form.appendRow(FormFactory.DEFAULT_ROWSPEC);
						builder.addLabel(key, cc.xy(1, row));
						builder.add(valuePanel, cc.xy(3, row));
					}
					if (prop.type == Inspector.PropertyType.ImageChooser) {
						JPanel panel = new JPanel(new BorderLayout());
						if (prop.value != null) {
							JPanel imagePanel = new JPanel(new BorderLayout());
							panel.add(imagePanel, BorderLayout.CENTER);

							Image img = (Image) prop.value;
							img = img.getScaledInstance(60, 60,
									Image.SCALE_SMOOTH);
							JLabel image = new JLabel(new ImageIcon(img));
							imagePanel.add(image, BorderLayout.CENTER);

							JButton remove = new JButton("X");
							remove.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									panel.remove(imagePanel);
									setProperty(tab, propertyKey, null);
								}

							});
							imagePanel.add(remove, BorderLayout.EAST);
						}
						JPanel buttons = new JPanel(new FlowLayout());
						panel.add(buttons, BorderLayout.SOUTH);

						JButton button = new JButton("Choose ...");
						button.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								final JFileChooser fc = new JFileChooser();
								fc.setDialogTitle("Choose Image");
								FileFilter imageFilter = new FileNameExtensionFilter(
										"Image files", ImageIO
												.getReaderFileSuffixes());
								fc.setFileFilter(imageFilter);
								fc.showDialog(designerPanel, "Choose");
								File file = fc.getSelectedFile();
								if (file != null) {
									setProperty(tab, propertyKey, file);
								}
							}

						});
						buttons.add(button);

						form.appendRow(FormFactory.DEFAULT_ROWSPEC);
						builder.addLabel(key, cc.xy(1, row));
						builder.add(panel, cc.xy(3, row));
					}
					if (prop.type == Inspector.PropertyType.Color) {
						JButton button = new JButton();
						button.setBackground((Color) prop.value);
						button.setOpaque(true);
						button.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								Color color = JColorChooser.showDialog(
										designerPanel, "Choose Color",
										(Color) prop.value);
								button.setBackground(color);
								setProperty(tab, propertyKey, color);
							}

						});

						form.appendRow(FormFactory.DEFAULT_ROWSPEC);
						builder.addLabel(key, cc.xy(1, row));
						builder.add(button, cc.xy(3, row));
					}
					if (prop.type == Inspector.PropertyType.Slider) {
						JSlider slider = new JSlider();
						slider.setMaximum(360);
						slider.setMinimum(0);
						slider.setValue((int) Math.round((double) prop.value));
						slider.addChangeListener(new ChangeListener() {

							@Override
							public void stateChanged(ChangeEvent e) {
								setProperty(tab, propertyKey, slider.getValue());
							}

						});

						form.appendRow(FormFactory.DEFAULT_ROWSPEC);
						builder.addLabel(key, cc.xy(1, row));
						builder.add(slider, cc.xy(3, row));
					}
					if (prop.type == Inspector.PropertyType.Toggle) {
						JToggleButton toggle = new JToggleButton();
						toggle.setSelected((Boolean) prop.value);
						toggle.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								setProperty(tab, propertyKey,
										toggle.isSelected());
							}

						});

						form.appendRow(FormFactory.DEFAULT_ROWSPEC);
						builder.addLabel(key, cc.xy(1, row));
						builder.add(toggle, cc.xy(3, row));
					}
					if (prop.type == Inspector.PropertyType.Entities) {
						JPanel listPane = new JPanel(new BorderLayout());
						DefaultListModel<DesignerEntity> model = new DefaultListModel<DesignerEntity>();
						JList<DesignerEntity> list = new JList<>(model);
						list.setCellRenderer(new EntityListRenderer());
						list.addFocusListener(new FocusListener() {

							@Override
							public void focusGained(FocusEvent e) {
								// TODO Auto-generated method stub

							}

							@Override
							public void focusLost(FocusEvent e) {
								list.clearSelection();
							}

						});
						list.addKeyListener(new KeyListener() {

							@Override
							public void keyTyped(KeyEvent e) {
								// TODO Auto-generated method stub

							}

							@Override
							public void keyPressed(KeyEvent e) {
								// TODO Auto-generated method stub

							}

							@Override
							public void keyReleased(KeyEvent e) {
								if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE
										|| e.getKeyCode() == KeyEvent.VK_DELETE) {
									List<DesignerEntity> selected = list
											.getSelectedValuesList();
									for (DesignerEntity object : selected) {
										model.removeElement(object);
									}
									LinkedList<DesignerEntity> elements = new LinkedList<DesignerEntity>();
									for (int i = 0; i < model.getSize(); i++) {
										elements.add(model.getElementAt(i));
									}
									setProperty(tab, propertyKey, elements);
								}
							}

						});

						LinkedList<? extends DesignerEntity> entities = null;
						final EntityPack pack;
						if (prop.value instanceof EntityPack) {
							pack = (EntityPack) prop.value;
							entities = pack.getEntities();
						} else {
							entities = (LinkedList<? extends DesignerEntity>) prop.value;
						}
						for (DesignerEntity entity : entities) {
							model.addElement(entity);
						}
						listPane.add(list, BorderLayout.CENTER);

						JPanel actionsPane = new JPanel(new FlowLayout());
						listPane.add(actionsPane, BorderLayout.SOUTH);

						JButton addButton = new JButton("+");
						addButton.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								addButton.setForeground(Color.BLUE);
								DesignerPane designerPane = designerPanel
										.getDesignerPane();
								designerPane
										.setEntityPicker(new EntityPicker() {

											@Override
											public void entityPicked(
													DesignerEntity entity) {
												model.addElement(entity);
												addButton
														.setForeground(Color.BLACK);
												LinkedList<DesignerEntity> elements = new LinkedList<DesignerEntity>();
												for (int i = 0; i < model
														.getSize(); i++) {
													elements.add(model
															.getElementAt(i));
												}
												setProperty(tab, propertyKey,
														elements);
											}

											@Override
											public void pickerCancelled() {
												addButton
														.setForeground(Color.BLACK);
											}

											@Override
											public boolean acceptsEntity(
													DesignerEntity entity) {
												if (prop.value instanceof EntityPack) {
													EntityPack p = (EntityPack) prop.value;
													if (p.getAcceptor() != null)
														return p.getAcceptor()
																.accepts(entity);
												}
												return true;
											}

										});
							}

						});
						actionsPane.add(addButton);

						form.appendRow(FormFactory.DEFAULT_ROWSPEC);
						builder.addLabel(key, cc.xy(1, row));
						builder.add(listPane, cc.xy(3, row));
					}
				}
			} else {

			}
			row++;
			builder.appendRow(builder.getLineGapSpec());
			row++;
		}

		JPanel panel = builder.getPanel();
		return panel;
	}

	public void setProperty(Tab tab, String key, Object value) {
		LinkedHashMap<String, Object> prop = tab.getProperties();
		prop.put(key, value);
		tab.getInspector().setProperties(tab, prop);
		designerPanel.getDesignerPane().repaint();
	}

	/**
	 * @return the inspector
	 */
	public Inspector getInspector() {
		return inspector;
	}

	/**
	 * @param inspector
	 *            the inspector to set
	 */
	public void setInspector(Inspector inspector) {
		this.inspector = inspector;
		this.update();

		String description = inspector.getDescription();
		Class<?>[] interfaces = inspector.getClass().getInterfaces();
		if (interfaces.length > 0) {
			description += "<br />Implements ";
		}
		int i = 0;
		for (Class<?> interfaceClass : interfaces) {
			String interfaceName = interfaceClass.getSimpleName();
			description += "<span style=\"color:rgb(50,20,200)\">"+interfaceName+"</span>";
			if (i + 1 == interfaces.length) {
				description += ".";
			} else {
				if (i + 2 == interfaces.length) {
					description += " and ";
				} else {
					description += ", ";
				}
			}
			i++;
		}
		getDescription().setText("<div style=\"font-family:Arial;text-align:center\">"+description+"</div>");
	}

	public interface Inspector {
		public enum PropertyType {
			Data, Seperator, ImageChooser, Slider, Color, Entities, Toggle
		}

		public String getInspectorName();

		public LinkedList<Tab> getTabs();

		public void setProperties(Tab tab,
				LinkedHashMap<String, Object> properties);

		public String getDescription();

		public class Tab {
			private String title = "";
			private LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
			private Inspector inspector = null;

			public Tab() {

			}

			public Tab(String title) {
				this();
				this.setTitle(title);
			}

			public Tab(String title, LinkedHashMap<String, Object> properties) {
				this(title);
				this.setProperties(properties);
			}

			/**
			 * @return the properties
			 */
			public LinkedHashMap<String, Object> getProperties() {
				return properties;
			}

			/**
			 * @param properties
			 *            the properties to set
			 */
			public void setProperties(LinkedHashMap<String, Object> properties) {
				this.properties = properties;
			}

			/**
			 * @return the title
			 */
			public String getTitle() {
				return title;
			}

			/**
			 * @param title
			 *            the title to set
			 */
			public void setTitle(String title) {
				this.title = title;
			}

			/**
			 * @return the inspector
			 */
			public Inspector getInspector() {
				return inspector;
			}

			/**
			 * @param inspector
			 *            the inspector to set
			 */
			public void setInspector(Inspector inspector) {
				this.inspector = inspector;
			}
		}

		public class Property {
			public PropertyType type;
			public Object value;
			private String internalKey;

			public Property(PropertyType type, Object value) {
				this.type = type;
				this.value = value;
			}

			public Property(String internalKey, PropertyType type, Object value) {
				this(type, value);
				this.setInternalKey(internalKey);
			}

			/**
			 * @return the internalKey
			 */
			public String getInternalKey() {
				return internalKey;
			}

			/**
			 * @param internalKey
			 *            the internalKey to set
			 */
			public void setInternalKey(String internalKey) {
				this.internalKey = internalKey;
			}
		}
	}

	protected JLabel getInspectorLabel() {
		return inspectorLabel;
	}

	protected JPanel getInfoTable() {
		return infoTable;
	}

	public class EntityListRenderer extends DefaultListCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8385713725415731407L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus);
			if (value instanceof DesignerEntity) {
				DesignerEntity object = (DesignerEntity) value;
				String name = object.getInspectorName();
				ImageIcon imageIcon = object.getMiniatureIcon(15, 15);

				setIcon(imageIcon);
				setText(name);
			}
			return c;
		}

	}

	public JTextPane getDescription() {
		return description;
	}
}