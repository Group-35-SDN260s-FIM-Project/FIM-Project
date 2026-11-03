package my.fim_security_suite.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicScrollBarUI;
import my.fim_security_suite.controller.FIMController;
import my.fim_security_suite.model.Baseline;
import my.fim_security_suite.model.FileChange;
import my.fim_security_suite.model.FileStatus;
import my.fim_security_suite.model.LogEntry;
import my.fim_security_suite.model.ScanResult;

/**
 * The main window of the FIM Security Suite.
 * The layout comes from the NetBeans Design tab (MainDashBoard.form),
 * the colours, fonts and what the buttons do are written by hand below.
 */
public class MainDashBoard extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainDashBoard.class.getName());

    // Everything below is written by hand. The designer only puts the
    // components in place. The colours, the behaviour and the two scrolling
    // lists are done here, and like the console menu this window only
    // talks to FIMController.

    // colours
    private static final Color BODY = new Color(14, 14, 16);
    private static final Color PANEL = new Color(42, 53, 72);
    private static final Color DARK_PANEL = new Color(30, 39, 57);
    private static final Color TEAL = new Color(13, 175, 169);
    private static final Color DISABLED = new Color(55, 65, 81);
    private static final Color GREY = new Color(160, 170, 185);
    private static final Color LIGHT = new Color(204, 204, 204);
    private static final Color LINE = new Color(64, 78, 102);

    private static final int ROW_HEIGHT = 24;
    // true = newest line at the top. false = like a normal log, newest at the bottom and scrolled down
    private static final boolean NEWEST_FIRST = false;
    private static final int LOG_LIMIT = 500;   // how many of the newest log entries go in the list
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd MMM HH:mm:ss");

    // Recent activity only shows what happened since the program started.
    // The log file keeps everything, but each run starts with an empty list.
    private static final LocalDateTime SESSION_START = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime()),
            ZoneId.systemDefault()).withNano(0);

    private final FIMController controller;

    // true while the changed files list shows a real check result and not a hint
    private boolean showingResults = false;

    // the two scrolling lists, the rows are added to these panels
    private final ListPanel changedRows = new ListPanel();
    private final ListPanel activityRows = new ListPanel();

    /** Start on its own (Run File on this class): makes its own controller. */
    public MainDashBoard() {
        this(new FIMController());
    }

    /** Start from FIM_Security_Suite or the console menu: uses the same controller. */
    public MainDashBoard(FIMController controller) {
        this.controller = controller;
        initComponents();   // the layout from the designer
        applyStyle();       // colours, fonts, borders and scroll panes
        setLocationRelativeTo(null);

        showMessage("Choose a folder to start");
        refreshView();
    }

    /** Colours, fonts and borders for the components from the designer. */
    private void applyStyle() {
        setTitle("FIM Security Suite");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 560));
        setSize(1000, 680);
        getContentPane().setBackground(BODY);

        // header
        headerPanel.setBackground(PANEL);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        titlePanel.setOpaque(false);
        styleLabel(fimLabel, TEAL, new Font("Segoe UI", Font.BOLD, 22));
        styleLabel(suiteLabel, Color.WHITE, new Font("Segoe UI", Font.BOLD, 22));
        styleLabel(groupLabel, LIGHT, new Font("Segoe UI", Font.PLAIN, 14));

        // folder and baseline info: two rows, the name on the left has a fixed width and the value takes the rest
        infoPanel.setBackground(DARK_PANEL);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        folderRow.setOpaque(false);
        baselineRow.setOpaque(false);
        folderNameLabel.setPreferredSize(new Dimension(75, 20));
        baselineNameLabel.setPreferredSize(new Dimension(75, 20));
        styleLabel(folderNameLabel, GREY, new Font("Segoe UI", Font.PLAIN, 12));
        styleLabel(baselineNameLabel, GREY, new Font("Segoe UI", Font.PLAIN, 12));
        styleLabel(folderValueLabel, Color.WHITE, new Font("Segoe UI", Font.PLAIN, 12));
        styleLabel(baselineValueLabel, Color.WHITE, new Font("Segoe UI", Font.PLAIN, 12));

        // middle: buttons, legend and the two cards
        bodyPanel.setBackground(BODY);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(16, 22, 12, 22));
        controlsPanel.setOpaque(false);
        // the designer's FlowLayout rows start 14 px in, so pull them back to line up with the cards
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(0, -14, 0, 0));
        buttonRow.setOpaque(false);
        legendRow.setOpaque(false);
        legendRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));   // a bit of space under the buttons
        cardsPanel.setOpaque(false);
        styleButton(selectFolderButton);
        styleButton(createBaselineButton);
        styleButton(runCheckButton);

        // legend: a coloured dot and text, the text shows the counts after a check
        styleLegend(unchangedLabel, FileStatus.UNCHANGED);
        styleLegend(modifiedLabel, FileStatus.MODIFIED);
        styleLegend(newLabel, FileStatus.NEW);
        styleLegend(missingLabel, FileStatus.MISSING);

        styleCard(changedCard, changedTitle, "CHANGED FILES");
        styleCard(activityCard, activityTitle, "RECENT ACTIVITY");

        // the grey column titles stay put while the rows scroll under them
        JPanel headings = new JPanel(new GridLayout(1, 4, 12, 0));
        headings.setBackground(PANEL);
        headings.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                BorderFactory.createEmptyBorder(0, 0, 5, 0)));
        String[] names = {"STATUS", "FILE", "OLD HASH", "NEW HASH"};
        for (String name : names) {
            headings.add(cell(name, GREY, new Font("Segoe UI", Font.PLAIN, 12)));
        }
        styleScroll(changedScroll, changedRows);
        changedScroll.setColumnHeaderView(headings);
        changedScroll.getColumnHeader().setBackground(PANEL);
        styleScroll(activityScroll, activityRows);

        // gaps are set here as well in case the form gets regenerated without them
        gaps(titlePanel, 10, 0);
        gaps(infoPanel, 0, 6);
        gaps(folderRow, 10, 0);
        gaps(baselineRow, 10, 0);
        gaps(bodyPanel, 0, 12);
        gaps(controlsPanel, 0, 10);
        gaps(buttonRow, 14, 0);
        gaps(legendRow, 14, 0);
        gaps(cardsPanel, 0, 12);
        gaps(changedCard, 0, 10);
        gaps(activityCard, 0, 10);

        // status bar
        statusBar.setBackground(DARK_PANEL);
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));
        styleLabel(statusLabel, LIGHT, new Font("Segoe UI", Font.PLAIN, 12));
    }

    /** Sets the gaps of a panel, works with Border, Grid and Flow layouts. */
    private static void gaps(JPanel panel, int h, int v) {
        java.awt.LayoutManager lm = panel.getLayout();
        if (lm instanceof BorderLayout) {
            ((BorderLayout) lm).setHgap(h); ((BorderLayout) lm).setVgap(v);
        } else if (lm instanceof GridLayout) {
            ((GridLayout) lm).setHgap(h); ((GridLayout) lm).setVgap(v);
        } else if (lm instanceof java.awt.FlowLayout) {
            ((java.awt.FlowLayout) lm).setHgap(h); ((java.awt.FlowLayout) lm).setVgap(v);
        }
    }

    private static void styleLabel(JLabel label, Color color, Font font) {
        label.setForeground(color);
        label.setFont(font);
    }

    /** Flat teal button, looks the same with any look and feel. */
    private static void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(TEAL);
        button.setContentAreaFilled(false);   // we paint the flat colour ourselves
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Coloured dot (a small drawn icon) before the legend text. */
    private static void styleLegend(JLabel label, FileStatus status) {
        label.setIcon(new DotIcon(colorFor(status)));
        label.setIconTextGap(7);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 14));
    }

    /** Dark box with a small grey title. */
    private static void styleCard(JPanel card, JLabel title, String text) {
        card.setBackground(PANEL);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 12));
        title.setText(text);
        styleLabel(title, GREY, new Font("Segoe UI", Font.PLAIN, 12));
    }

    /** Puts the rows panel in the designer's scroll pane: no border and a dark scrollbar. */
    private static void styleScroll(JScrollPane scroll, ListPanel rows) {
        scroll.setViewportView(rows);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(PANEL);

        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setUI(new DarkScrollBarUI());
        bar.setPreferredSize(new Dimension(12, 0));
        bar.setBackground(PANEL);
        bar.setUnitIncrement(ROW_HEIGHT);
    }

    // ---- button actions ----

    private void onSelectFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;   // user cancelled
        }
        try {
            controller.selectFolder(chooser.getSelectedFile().toPath());
            showMessage("Folder selected. Create a baseline next.");   // that result was for the old folder
            statusLabel.setText("Folder selected");
            refreshView();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void onCreateBaseline() {
        try {
            Baseline baseline = controller.createBaseline();
            // only change the hint text, a result from an earlier check stays on screen
            if (!showingResults) {
                showMessage("Baseline saved (" + baseline.size() + " files). Run a check to compare.");
            }
            statusLabel.setText("Baseline created (" + baseline.size() + " files)");
            refreshView();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void onRunCheck() {
        try {
            showResult(controller.runCheck());
            refreshView();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    // ---- updating the screen ----

    /** Reads the controller again: folder, baseline, which buttons work and the activity list. */
    private void refreshView() {
        Path folder = controller.getSelectedFolder();
        folderValueLabel.setText(folder == null ? "(none chosen yet)" : folder.toString());

        // log entries of this run (the logger gives them oldest first)
        List<LogEntry> entries = new ArrayList<>();
        for (LogEntry entry : controller.getRecentLog(LOG_LIMIT)) {
            if (!entry.getTimestamp().isBefore(SESSION_START)) {   // skip older runs
                entries.add(entry);
            }
        }

        // The baseline file stays on disk between runs so "file exists" is not enough.
        // Same as the activity list, only a baseline made in this run counts.
        boolean baselineThisRun = false;
        for (LogEntry entry : entries) {
            if ("BASELINE".equals(entry.getAction())) {
                baselineThisRun = true;
            }
        }
        baselineValueLabel.setText(baselineThisRun && controller.hasBaseline() ? "saved" : "none");

        // a button only works when that step is possible
        setButtonEnabled(createBaselineButton, folder != null);
        setButtonEnabled(runCheckButton, folder != null && baselineThisRun && controller.hasBaseline());

        // the order depends on NEWEST_FIRST (the logger gives the entries oldest first)
        activityRows.removeAll();
        if (entries.isEmpty()) {
            activityRows.add(messageRow("(nothing yet this session)"));
        }
        if (NEWEST_FIRST) {
            for (int i = entries.size() - 1; i >= 0; i--) {
                activityRows.add(activityRow(entries.get(i)));
            }
        } else {
            for (LogEntry entry : entries) {
                activityRows.add(activityRow(entry));
            }
        }
        // newest at the top: stay at the top. newest at the bottom: scroll down with it.
        finishList(activityRows, activityScroll, !NEWEST_FIRST);
    }

    /** Fills the list with a row for each changed file and puts the counts in the legend. */
    private void showResult(ScanResult result) {
        List<FileChange> changed = new ArrayList<>();
        for (FileChange c : result.getChanges()) {
            if (c.getStatus() != FileStatus.UNCHANGED) {   // same as the CLI, only the changes are listed
                changed.add(c);
            }
        }

        unchangedLabel.setText(result.countOf(FileStatus.UNCHANGED) + " Unchanged");
        modifiedLabel.setText(result.countOf(FileStatus.MODIFIED) + " Modified");
        newLabel.setText(result.countOf(FileStatus.NEW) + " New");
        missingLabel.setText(result.countOf(FileStatus.MISSING) + " Missing");

        showingResults = true;
        changedRows.removeAll();
        if (changed.isEmpty()) {
            changedRows.add(changedRow("● CLEAN", "All files unchanged", "", "",
                    colorFor(FileStatus.UNCHANGED)));
        }
        for (FileChange c : changed) {
            changedRows.add(changedRow("● " + c.getStatus(), c.getPath(),
                    shortHash(c.getOldHash()), shortHash(c.getNewHash()), colorFor(c.getStatus())));
        }
        finishList(changedRows, changedScroll, false);

        statusLabel.setText(changed.isEmpty() ? "Check complete: all clean"
                : "Check complete: " + changed.size() + " file(s) changed");
    }

    /** Shows one message in the list (before any check) and the legend goes back to plain names. */
    private void showMessage(String message) {
        unchangedLabel.setText("Unchanged");
        modifiedLabel.setText("Modified");
        newLabel.setText("New");
        missingLabel.setText("Missing");

        showingResults = false;
        changedRows.removeAll();
        changedRows.add(changedRow("", message, "", "", Color.WHITE));
        finishList(changedRows, changedScroll, false);
    }

    /** Redraws a list after its rows changed and then jumps to the top or the last row. */
    private static void finishList(ListPanel rows, JScrollPane scroll, boolean toBottom) {
        rows.revalidate();
        rows.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scroll.getVerticalScrollBar();
            bar.setValue(toBottom ? bar.getMaximum() : 0);
        });
    }

    // ---- rows ----

    /** One row of the changed files table: status, file, old hash and new hash. */
    private static JPanel changedRow(String status, String file, String oldHash, String newHash,
                                     Color statusColor) {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.add(cell(status, statusColor, new Font("Segoe UI", Font.BOLD, 13)));
        row.add(cell(file, Color.WHITE, new Font(Font.MONOSPACED, Font.PLAIN, 13)));
        row.add(cell(oldHash, Color.WHITE, new Font(Font.MONOSPACED, Font.PLAIN, 13)));
        row.add(cell(newHash, Color.WHITE, new Font(Font.MONOSPACED, Font.PLAIN, 13)));
        return fixedHeight(row);
    }

    /** One line of the activity list: time, coloured action and message. */
    private static JPanel activityRow(LogEntry entry) {
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 13);
        JLabel time = cell(TIME.format(entry.getTimestamp()), GREY, mono);
        time.setPreferredSize(new Dimension(125, ROW_HEIGHT));
        JLabel action = cell(entry.getAction(), colorForAction(entry.getAction()),
                new Font(Font.MONOSPACED, Font.BOLD, 13));
        action.setPreferredSize(new Dimension(105, ROW_HEIGHT));

        JPanel right = new JPanel(new BorderLayout(14, 0));
        right.setOpaque(false);
        right.add(action, BorderLayout.WEST);
        right.add(cell(entry.getMessage(), Color.WHITE, mono), BorderLayout.CENTER);

        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.add(time, BorderLayout.WEST);
        row.add(right, BorderLayout.CENTER);
        return fixedHeight(row);
    }

    private static JPanel messageRow(String message) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(cell(message, Color.WHITE, new Font(Font.MONOSPACED, Font.PLAIN, 13)), BorderLayout.CENTER);
        return fixedHeight(row);
    }

    private static JLabel cell(String text, Color color, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(font);
        return label;
    }

    /** Gives every row the same height so they don't stretch when there are only a few. */
    private static JPanel fixedHeight(JPanel row) {
        row.setPreferredSize(new Dimension(10, ROW_HEIGHT));
        row.setMinimumSize(new Dimension(10, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        return row;
    }

    /** First 8 characters of a hash, or "-" if there is none (new / missing files). */
    private static String shortHash(String hash) {
        if (hash == null) {
            return "-";
        }
        return hash.length() > 8 ? hash.substring(0, 8) + "..." : hash;
    }

    private static Color colorFor(FileStatus status) {
        switch (status) {
            case MODIFIED: return new Color(250, 204, 21);   // yellow
            case NEW:      return new Color(59, 130, 246);   // blue
            case MISSING:  return new Color(239, 68, 68);    // red
            default:       return new Color(34, 197, 94);    // green (unchanged)
        }
    }

    /** Log actions that are file statuses get the same colour as in the table. */
    private static Color colorForAction(String action) {
        switch (action) {
            case "MODIFIED":  return colorFor(FileStatus.MODIFIED);
            case "NEW":       return colorFor(FileStatus.NEW);
            case "MISSING":   return colorFor(FileStatus.MISSING);
            case "BASELINE":  return TEAL;
            default:          return LIGHT;   // CHECK_RUN, SELECT ...
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, String.valueOf(message),
                "FIM Security Suite", JOptionPane.ERROR_MESSAGE);
    }

    private static void setButtonEnabled(JButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setBackground(enabled ? TEAL : DISABLED);
    }

    // ---- helper classes for the scrolling lists ----

    /**
     * A panel that stacks the rows from the top and follows the width of the scroll
     * pane, so the columns stretch with the window and there is no sideways scrollbar.
     */
    private static class ListPanel extends JPanel implements Scrollable {
        ListPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
            return ROW_HEIGHT;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return Math.max(ROW_HEIGHT, visible.height - ROW_HEIGHT);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Thin dark scrollbar with a rounded thumb and no arrow buttons. */
    private static class DarkScrollBarUI extends BasicScrollBarUI {
        private static final Color TRACK = new Color(36, 46, 64);
        private static final Color THUMB = new Color(82, 98, 124);
        private static final Color THUMB_ACTIVE = new Color(112, 130, 160);

        @Override
        protected void configureScrollBarColors() {
            thumbColor = THUMB;
            trackColor = TRACK;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return noButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return noButton();
        }

        private JButton noButton() {
            JButton button = new JButton();
            Dimension none = new Dimension(0, 0);
            button.setPreferredSize(none);
            button.setMinimumSize(none);
            button.setMaximumSize(none);
            return button;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
            g.setColor(TRACK);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
            if (bounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDragging || isThumbRollover() ? THUMB_ACTIVE : THUMB);
            g2.fillRoundRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4, 8, 8);
            g2.dispose();
        }
    }

    /** Small filled circle for the legend dot. */
    private static class DotIcon implements Icon {
        private final Color color;

        DotIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, 10, 10);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 10;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        titlePanel = new javax.swing.JPanel();
        fimLabel = new javax.swing.JLabel();
        suiteLabel = new javax.swing.JLabel();
        groupLabel = new javax.swing.JLabel();
        infoPanel = new javax.swing.JPanel();
        folderRow = new javax.swing.JPanel();
        folderNameLabel = new javax.swing.JLabel();
        folderValueLabel = new javax.swing.JLabel();
        baselineRow = new javax.swing.JPanel();
        baselineNameLabel = new javax.swing.JLabel();
        baselineValueLabel = new javax.swing.JLabel();
        bodyPanel = new javax.swing.JPanel();
        controlsPanel = new javax.swing.JPanel();
        buttonRow = new javax.swing.JPanel();
        selectFolderButton = new javax.swing.JButton();
        createBaselineButton = new javax.swing.JButton();
        runCheckButton = new javax.swing.JButton();
        legendRow = new javax.swing.JPanel();
        unchangedLabel = new javax.swing.JLabel();
        modifiedLabel = new javax.swing.JLabel();
        newLabel = new javax.swing.JLabel();
        missingLabel = new javax.swing.JLabel();
        cardsPanel = new javax.swing.JPanel();
        changedCard = new javax.swing.JPanel();
        changedTitle = new javax.swing.JLabel();
        changedScroll = new javax.swing.JScrollPane();
        activityCard = new javax.swing.JPanel();
        activityTitle = new javax.swing.JLabel();
        activityScroll = new javax.swing.JScrollPane();
        statusBar = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FIM Security Suite");
        getContentPane().setLayout(new java.awt.BorderLayout());

        topPanel.setLayout(new java.awt.BorderLayout());
        headerPanel.setBackground(new java.awt.Color(42, 53, 72));
        headerPanel.setLayout(new java.awt.BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        fimLabel.setForeground(new java.awt.Color(13, 175, 169));
        fimLabel.setText("FIM");

        titlePanel.add(fimLabel);
        suiteLabel.setForeground(new java.awt.Color(255, 255, 255));
        suiteLabel.setText("Security Suite");

        titlePanel.add(suiteLabel);

        headerPanel.add(titlePanel, java.awt.BorderLayout.WEST);
        groupLabel.setForeground(new java.awt.Color(204, 204, 204));
        groupLabel.setText("Group 35");

        headerPanel.add(groupLabel, java.awt.BorderLayout.EAST);

        topPanel.add(headerPanel, java.awt.BorderLayout.NORTH);
        infoPanel.setBackground(new java.awt.Color(30, 39, 57));
        infoPanel.setLayout(new java.awt.GridLayout(2, 1, 0, 6));
        folderRow.setOpaque(false);
        folderRow.setLayout(new java.awt.BorderLayout(10, 0));
        folderNameLabel.setForeground(new java.awt.Color(160, 170, 185));
        folderNameLabel.setText("Folder:");

        folderRow.add(folderNameLabel, java.awt.BorderLayout.WEST);
        folderValueLabel.setForeground(new java.awt.Color(255, 255, 255));
        folderValueLabel.setText("(none chosen yet)");

        folderRow.add(folderValueLabel, java.awt.BorderLayout.CENTER);

        infoPanel.add(folderRow);
        baselineRow.setOpaque(false);
        baselineRow.setLayout(new java.awt.BorderLayout(10, 0));
        baselineNameLabel.setForeground(new java.awt.Color(160, 170, 185));
        baselineNameLabel.setText("Baseline:");

        baselineRow.add(baselineNameLabel, java.awt.BorderLayout.WEST);
        baselineValueLabel.setForeground(new java.awt.Color(255, 255, 255));
        baselineValueLabel.setText("none");

        baselineRow.add(baselineValueLabel, java.awt.BorderLayout.CENTER);

        infoPanel.add(baselineRow);

        topPanel.add(infoPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        bodyPanel.setBackground(new java.awt.Color(14, 14, 16));
        bodyPanel.setLayout(new java.awt.BorderLayout(0, 12));
        controlsPanel.setOpaque(false);
        controlsPanel.setLayout(new java.awt.GridLayout(2, 1, 0, 10));
        buttonRow.setOpaque(false);
        buttonRow.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 14, 0));
        selectFolderButton.setBackground(new java.awt.Color(13, 175, 169));
        selectFolderButton.setForeground(new java.awt.Color(255, 255, 255));
        selectFolderButton.setText("Select Folder");
        selectFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFolderButtonActionPerformed(evt);
            }
        });

        buttonRow.add(selectFolderButton);
        createBaselineButton.setBackground(new java.awt.Color(13, 175, 169));
        createBaselineButton.setForeground(new java.awt.Color(255, 255, 255));
        createBaselineButton.setText("Create Baseline");
        createBaselineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createBaselineButtonActionPerformed(evt);
            }
        });

        buttonRow.add(createBaselineButton);
        runCheckButton.setBackground(new java.awt.Color(13, 175, 169));
        runCheckButton.setForeground(new java.awt.Color(255, 255, 255));
        runCheckButton.setText("Run Check");
        runCheckButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runCheckButtonActionPerformed(evt);
            }
        });

        buttonRow.add(runCheckButton);

        controlsPanel.add(buttonRow);
        legendRow.setOpaque(false);
        legendRow.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 14, 0));
        unchangedLabel.setForeground(new java.awt.Color(255, 255, 255));
        unchangedLabel.setText("Unchanged");

        legendRow.add(unchangedLabel);
        modifiedLabel.setForeground(new java.awt.Color(255, 255, 255));
        modifiedLabel.setText("Modified");

        legendRow.add(modifiedLabel);
        newLabel.setForeground(new java.awt.Color(255, 255, 255));
        newLabel.setText("New");

        legendRow.add(newLabel);
        missingLabel.setForeground(new java.awt.Color(255, 255, 255));
        missingLabel.setText("Missing");

        legendRow.add(missingLabel);

        controlsPanel.add(legendRow);

        bodyPanel.add(controlsPanel, java.awt.BorderLayout.NORTH);
        cardsPanel.setOpaque(false);
        cardsPanel.setLayout(new java.awt.GridLayout(2, 1, 0, 12));
        changedCard.setBackground(new java.awt.Color(42, 53, 72));
        changedCard.setLayout(new java.awt.BorderLayout(0, 10));
        changedTitle.setForeground(new java.awt.Color(160, 170, 185));
        changedTitle.setText("CHANGED FILES");

        changedCard.add(changedTitle, java.awt.BorderLayout.NORTH);

        changedCard.add(changedScroll, java.awt.BorderLayout.CENTER);

        cardsPanel.add(changedCard);
        activityCard.setBackground(new java.awt.Color(42, 53, 72));
        activityCard.setLayout(new java.awt.BorderLayout(0, 10));
        activityTitle.setForeground(new java.awt.Color(160, 170, 185));
        activityTitle.setText("RECENT ACTIVITY");

        activityCard.add(activityTitle, java.awt.BorderLayout.NORTH);

        activityCard.add(activityScroll, java.awt.BorderLayout.CENTER);

        cardsPanel.add(activityCard);

        bodyPanel.add(cardsPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(bodyPanel, java.awt.BorderLayout.CENTER);

        statusBar.setBackground(new java.awt.Color(30, 39, 57));
        statusBar.setLayout(new java.awt.BorderLayout());
        statusLabel.setForeground(new java.awt.Color(204, 204, 204));
        statusLabel.setText("Ready");

        statusBar.add(statusLabel, java.awt.BorderLayout.WEST);

        getContentPane().add(statusBar, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectFolderButtonActionPerformed
        onSelectFolder();
    }//GEN-LAST:event_selectFolderButtonActionPerformed

    private void createBaselineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createBaselineButtonActionPerformed
        onCreateBaseline();
    }//GEN-LAST:event_createBaselineButtonActionPerformed

    private void runCheckButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runCheckButtonActionPerformed
        onRunCheck();
    }//GEN-LAST:event_runCheckButtonActionPerformed

    public static void main(String args[]) {
        // use the look of the operating system (gives the Windows file chooser)
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainDashBoard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel activityCard;
    private javax.swing.JScrollPane activityScroll;
    private javax.swing.JLabel activityTitle;
    private javax.swing.JLabel baselineNameLabel;
    private javax.swing.JPanel baselineRow;
    private javax.swing.JLabel baselineValueLabel;
    private javax.swing.JPanel bodyPanel;
    private javax.swing.JPanel buttonRow;
    private javax.swing.JPanel cardsPanel;
    private javax.swing.JPanel changedCard;
    private javax.swing.JScrollPane changedScroll;
    private javax.swing.JLabel changedTitle;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JButton createBaselineButton;
    private javax.swing.JLabel fimLabel;
    private javax.swing.JLabel folderNameLabel;
    private javax.swing.JPanel folderRow;
    private javax.swing.JLabel folderValueLabel;
    private javax.swing.JLabel groupLabel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JPanel legendRow;
    private javax.swing.JLabel missingLabel;
    private javax.swing.JLabel modifiedLabel;
    private javax.swing.JLabel newLabel;
    private javax.swing.JButton runCheckButton;
    private javax.swing.JButton selectFolderButton;
    private javax.swing.JPanel statusBar;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel suiteLabel;
    private javax.swing.JPanel titlePanel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JLabel unchangedLabel;
    // End of variables declaration//GEN-END:variables
}
