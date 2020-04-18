//не работает tab по элементам
//не работает автоочищение
//нет контроля выполнения processbuilder.start
//нет проверки на допустимость ввода размеров раздела
//удалять пустые директории
// stop если флешка не пустая

package rav;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import java.awt.SystemColor;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;


public class MainFrame {
	static MainFrame window;
	JFrame mainForm;
	JButton btnClearCF;
	JCheckBox chckbxAutoClear;
	WatchDir threadWatchDir;
	DeviceUSB devUSB;
	int numDev = 0;

	private JCheckBox checkBoxFormat;
	private JPanel panelFormat;
	private JTextField tfSizeFormat;
	private JPanel panelLeft;
	private JTextArea txtDev;
	private JTextArea txtDevMsg;

	// -------------------------------------------------------------------------------------------
	public static void main(String[] args) throws IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new MainFrame();
					window.mainForm.setVisible(true);

					window.devUSB = new DeviceUSB();
					window.updateDevUSB();
					
					window.threadWatchDir = new WatchDir(window, Paths.get("/media"));
					window.threadWatchDir.start();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public MainFrame() {
		initialize();
	}

	// -------------------------------------------------------------------------------------------
	public void btnClearClick() {
		numDev++;
		mainForm.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		window.updateDevUSB();
		System.out.println("\n" + numDev + " " + new Date().toString() + "очистка носителя " + devUSB.getDevMedia()
				+ " объемом " + devUSB.getDevSize());
		int count = devUSB.devShred(); /// сделать проверку корректности выполнения shred
		if (count > 0) {
			System.out.println("   ___________________");
			System.out.println("   Удалено файлов: " + count);
		} else {
			System.out.println("   На носителе нет файлов ");
		}
		window.updateDevUSB();
		if (checkBoxFormat.isSelected()) {
			if (devUSB.devFormat(tfSizeFormat.getText())) {
				System.out.println("   Носитель " + devUSB.getDevMedia() + " форматирован на " + devUSB.getDevSize());
				txtDevMsg.setForeground(Color.BLUE);
				txtDevMsg.setText("READY");
			} else {
				txtDevMsg.setSelectedTextColor(Color.RED);
				txtDevMsg.setText("ERROR");
			}
		}
		mainForm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		window.updateDevUSB();
	}

	// -------------------------------------------------------------------------------------------

	public boolean updateDevUSB() {
		long count = 0;
		if (devUSB.isDevMounted()) {

			txtDev.setText("Подключен носитель:\n   " + devUSB.getDevSda() + "\n   " + devUSB.getDevMedia()
					+ "\n   объем " + devUSB.getDevSize());
			if (count > 1) {
				txtDev.setText(txtDev.getText() + "\n\nВНИМАНИЕ! НОСИТЕЛЬ НЕ ПУСТОЙ! " + (count - 1));
			}

			btnClearCF.setEnabled(true);
			btnClearCF.requestFocus();
			return true;
		} else {
			txtDev.setText("Носитель не найден");
			btnClearCF.setEnabled(false);
			txtDevMsg.setText("");
			return false;
		}
	}
	// ------------------------------------------------------------------------------------------

	private void initialize() {
		mainForm = new JFrame();
		mainForm.setTitle("DelCF");
		mainForm.setBounds(100, 100, 660, 400);
		mainForm.setMinimumSize(new Dimension(640, 400));
		mainForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 10, 220, 320, 5 };
		gridBagLayout.rowHeights = new int[] { 5, 135, 5, 90, 5 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 2.0, 0.0 };
		mainForm.getContentPane().setLayout(gridBagLayout);

		panelLeft = new JPanel();
		panelLeft.setMaximumSize(new Dimension(320, 135));
		panelLeft.setMinimumSize(new Dimension(320, 130));
		panelLeft.setPreferredSize(new Dimension(310, 135));
		GridBagConstraints gbc_panelLeft = new GridBagConstraints();

		gbc_panelLeft.anchor = GridBagConstraints.NORTHWEST;
		gbc_panelLeft.insets = new Insets(0, 0, 5, 5);
		gbc_panelLeft.gridx = 1;
		gbc_panelLeft.gridy = 1;
		mainForm.getContentPane().add(panelLeft, gbc_panelLeft);

		JPanel panelRight = new JPanel();
		panelRight.setMaximumSize(new Dimension(320, 150));
		panelRight.setMinimumSize(new Dimension(320, 130));
		panelRight.setPreferredSize(new Dimension(310, 130));
		GridBagConstraints gbc_panelRight = new GridBagConstraints();

		gbc_panelRight.anchor = GridBagConstraints.NORTHEAST;
		gbc_panelRight.insets = new Insets(0, 0, 5, 5);
		gbc_panelRight.gridx = 2;
		gbc_panelRight.gridy = 1;
		mainForm.getContentPane().add(panelRight, gbc_panelRight);

		txtDev = new JTextArea();
		txtDev.setEditable(false);
		txtDev.setRequestFocusEnabled(false);
		txtDev.setPreferredSize(new Dimension(300, 110));
		txtDev.setOpaque(false);
		txtDev.setMinimumSize(new Dimension(220, 110));
		txtDev.setMaximumSize(new Dimension(300, 110));
		txtDev.setFont(new Font("Ubuntu", Font.BOLD, 14));
		panelLeft.add(txtDev);

		txtDevMsg = new JTextArea();
		txtDevMsg.setEditable(false);
		txtDevMsg.setForeground(new Color(0, 128, 0));
		txtDevMsg.setOpaque(false);
		txtDevMsg.setRequestFocusEnabled(false);
		txtDevMsg.setPreferredSize(new Dimension(300, 25));
		txtDevMsg.setMinimumSize(new Dimension(220, 20));
		txtDevMsg.setMaximumSize(new Dimension(300, 25));
		txtDevMsg.setFont(new Font("Ubuntu", Font.BOLD, 14));
		panelLeft.add(txtDevMsg);

		btnClearCF = new JButton("Очистить носитель");
		btnClearCF.setMaximumSize(new Dimension(310, 25));

		btnClearCF.setFocusCycleRoot(true);

		btnClearCF.setEnabled(false);
		btnClearCF.setForeground(SystemColor.controlDkShadow);

		btnClearCF.setFont(new Font("Ubuntu", Font.PLAIN, 14));
		btnClearCF.setMinimumSize(new Dimension(300, 20));
		btnClearCF.setHorizontalTextPosition(SwingConstants.CENTER);
		btnClearCF.setPreferredSize(new Dimension(300, 25));
		panelRight.add(btnClearCF);

		chckbxAutoClear = new JCheckBox("Очищать автоматически при подключении");
		chckbxAutoClear.setVisible(false);
		chckbxAutoClear.setRequestFocusEnabled(false);
		chckbxAutoClear.setFocusable(false);
		chckbxAutoClear.setFocusTraversalKeysEnabled(false);
		chckbxAutoClear.setFocusPainted(false);
		chckbxAutoClear.setForeground(SystemColor.controlDkShadow);
		chckbxAutoClear.setFont(new Font("Ubuntu", Font.PLAIN, 14));
		chckbxAutoClear.setMinimumSize(new Dimension(290, 20));
		chckbxAutoClear.setPreferredSize(new Dimension(310, 25));
		panelRight.add(chckbxAutoClear);

		checkBoxFormat = new JCheckBox("Форматировать при очистке");
		checkBoxFormat.setRequestFocusEnabled(false);
		checkBoxFormat.setFocusable(false);
		checkBoxFormat.setFocusTraversalKeysEnabled(false);
		checkBoxFormat.setFocusPainted(false);
		checkBoxFormat.setFont(new Font("Ubuntu", Font.PLAIN, 14));
		checkBoxFormat.setMinimumSize(new Dimension(290, 20));
		checkBoxFormat.setPreferredSize(new Dimension(310, 25));
		panelRight.add(checkBoxFormat);

		panelFormat = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelFormat.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panelFormat.setFocusTraversalKeysEnabled(false);
		panelFormat.setFocusable(false);
		panelFormat.setVisible(false);
		panelFormat.setMinimumSize(new Dimension(290, 20));
		panelFormat.setPreferredSize(new Dimension(310, 25));
		panelRight.add(panelFormat);

		JLabel lblMb = new JLabel("Размер раздела MB или %:     ");
		lblMb.setPreferredSize(new Dimension(200, 15));
		lblMb.setFont(new Font("Ubuntu", Font.PLAIN, 14));
		panelFormat.add(lblMb);

		tfSizeFormat = new JTextField();
		tfSizeFormat.setColumns(8);
		tfSizeFormat.setText("100%");
		panelFormat.add(tfSizeFormat);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(100, 200));
		scrollPane.setMinimumSize(new Dimension(100, 200));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 3;
		mainForm.getContentPane().add(scrollPane, gbc_scrollPane);

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setCaretColor(SystemColor.controlShadow);
		textArea.setForeground(Color.DARK_GRAY);
		textArea.setFocusTraversalKeysEnabled(false);
		textArea.setColumns(1);
		textArea.setFont(new Font("Ubuntu", Font.PLAIN, 14));
		scrollPane.setViewportView(textArea);

		new RedirectSystemOut(textArea);

// button wipe device --------------------------------------------------------------------------

		btnClearCF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnClearClick();
			}
		});

// check auto wipe device-----------------------------------------------------------------------------
		chckbxAutoClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});

// check format device-----------------------------------------------------------------------------
		checkBoxFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelFormat.setVisible(checkBoxFormat.isSelected());
				if (checkBoxFormat.isSelected()) {
					btnClearCF.setText("Очистить и форматировать носитель");
				} else {
					btnClearCF.setText("Очистить носитель");
				}
			}
		});
	}
}
//
//
//
//
//
//
//
//
//
//
//
//
//
//end