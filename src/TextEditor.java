import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {

    private final Vector<Integer> foundStart = new Vector<>();
    private final Vector<Integer> foundLength = new Vector<>();
    private int foundIterator;
    private int foundCounter;

    private void selectWord(JTextArea textArea, int textInd, int textLength) {
        textArea.setCaretPosition(textInd + textLength);
        textArea.select(textInd, textInd + textLength);
        textArea.grabFocus();
    }

    public TextEditor() {

        // Frame settings ---------------------------------------------------------------

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setTitle("Text Editor");

        // File chooser -----------------------------------------------------------------

        JFileChooser fileChooser = new JFileChooser(
                FileSystemView.getFileSystemView().getHomeDirectory()
        );
        fileChooser.setName("FileChooser");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        //  Text area -------------------------------------------------------------------

        JTextArea textArea = new JTextArea();
        textArea.setName("TextArea");

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        );
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );

        add(scrollPane, BorderLayout.CENTER);

        // File panel -------------------------------------------------------------------

        JPanel filePanel = new JPanel();

        // Search field -----------------------------------------------------------------

        JTextField searchField = new JTextField(15);
        searchField.setName("SearchField");

        // Action events ----------------------------------------------------------------

        ActionListener openFileAction = actionEvent -> {
            int returnValue = fileChooser.showOpenDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                textArea.setText("");

                File selectedFile = fileChooser.getSelectedFile();
                try {
                    textArea.setText(
                            new String(
                                    Files.readAllBytes(
                                            Paths.get(selectedFile.getPath())
                                    )
                            )
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        ActionListener saveFileAction = actionEvent -> {
            int returnValue = fileChooser.showSaveDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try (FileWriter writer = new FileWriter(selectedFile)) {
                    writer.write(textArea.getText());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        ActionListener searchTextAction = actionEvent -> {

            String textToFind = searchField.getText();

            new Thread(() -> {
                foundStart.clear();
                foundLength.clear();

                String textBuffer = textArea.getText();

                if(!"".equals(textBuffer) && !"".equals(textToFind)) {
                    Pattern pattern;

                    if (((JCheckBox) filePanel.getComponent(6)).isSelected()) {
                        pattern = Pattern.compile(textToFind);
                    } else {
                        pattern = Pattern.compile(textToFind, Pattern.CASE_INSENSITIVE);
                    }

                    Matcher matcher = pattern.matcher(textBuffer);
                    while (matcher.find()) {
                        foundStart.add(matcher.start());
                        foundLength.add(matcher.end() - matcher.start());
                    }
                }

                if (!foundStart.isEmpty()) {
                    foundIterator = 0;
                    foundCounter = foundStart.size() - 1;
                    selectWord(textArea, foundStart.get(0), foundLength.get(0));
                }
            }).start();
        };

        ActionListener previousMatchAction = actionEvent -> {
            foundIterator--;
            if (foundIterator < 0) {
                foundIterator = foundCounter;
            }
            selectWord(textArea, foundStart.get(foundIterator), foundLength.get(foundIterator));
        };

        ActionListener nextMatchAction = actionEvent -> {
            foundIterator++;
            if (foundIterator > foundCounter) {
                foundIterator = 0;
            }
            selectWord(textArea, foundStart.get(foundIterator), foundLength.get(foundIterator));
        };

        // Buttons ----------------------------------------------------------------------

        JButton openButton = new JButton("Load");
        openButton.setName("OpenButton");
        openButton.addActionListener(openFileAction);

        JButton saveButton = new JButton("Save");
        saveButton.setName("SaveButton");
        saveButton.addActionListener(saveFileAction);

        JButton startSearchButton = new JButton("Search");
        startSearchButton.setName("StartSearchButton");
        startSearchButton.addActionListener(searchTextAction);

        JButton previousMatchButton = new JButton("<");
        previousMatchButton.setName("PreviousMatchButton");
        previousMatchButton.addActionListener(previousMatchAction);

        JButton nextMatchButton = new JButton(">");
        nextMatchButton.setName("NextMatchButton");
        nextMatchButton.addActionListener(nextMatchAction);

        // Check box --------------------------------------------------------------------

        JCheckBox useRegExCheckbox = new JCheckBox("Use regex");
        useRegExCheckbox.setName("UseRegExCheckbox");

        // File panel add items ---------------------------------------------------------

        // Buttons
        filePanel.add(openButton);
        filePanel.add(saveButton);

        // Search field
        filePanel.add(searchField);

        // Buttons
        filePanel.add(startSearchButton);
        filePanel.add(previousMatchButton);
        filePanel.add(nextMatchButton);

        // Checkbox
        filePanel.add(useRegExCheckbox);

        add(filePanel, BorderLayout.NORTH);

        // Menu -------------------------------------------------------------------------

        // File

        JMenu menuFile = new JMenu("File");
        menuFile.setName("MenuFile");

        JMenuItem menuOpen = new JMenuItem("Load");
        menuOpen.setName("MenuOpen");
        menuOpen.addActionListener(openFileAction);

        JMenuItem menuSave = new JMenuItem("Save");
        menuSave.setName("MenuSave");
        menuSave.addActionListener(saveFileAction);

        JMenuItem menuExit = new JMenuItem("Exit");
        menuExit.setName("MenuExit");
        menuExit.addActionListener(actionEvent -> dispose());

        menuFile.add(menuOpen);
        menuFile.add(menuSave);
        menuFile.addSeparator();
        menuFile.add(menuExit);

        // Search

        JMenu menuSearch = new JMenu("Search");
        menuSearch.setName("MenuSearch");

        JMenuItem menuStartSearch = new JMenuItem("Start search");
        menuStartSearch.setName("MenuStartSearch");
        menuStartSearch.addActionListener(searchTextAction);

        JMenuItem menuPreviousMatch = new JMenuItem("Previous match");
        menuPreviousMatch.setName("MenuPreviousMatch");
        menuPreviousMatch.addActionListener(previousMatchAction);

        JMenuItem menuNextMatch = new JMenuItem("Next match");
        menuNextMatch.setName("MenuNextMatch");
        menuNextMatch.addActionListener(nextMatchAction);

        JMenuItem menuUseRegExp = new JMenuItem("Use regular expressions");
        menuUseRegExp.setName("MenuUseRegExp");
        menuUseRegExp.addActionListener(
                actionEvent -> useRegExCheckbox.setSelected(!useRegExCheckbox.isSelected())
        );

        menuSearch.add(menuStartSearch);
        menuSearch.add(menuPreviousMatch);
        menuSearch.add(menuNextMatch);
        menuSearch.add(menuUseRegExp);

        //////////

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menuFile);
        menuBar.add(menuSearch);

        setJMenuBar(menuBar);

        // ------------------------------------------------------------------------------

        setVisible(true);
    }
}
