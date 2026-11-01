package my.fim_security_suite;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
 * The Swing window for the File Integrity Monitor.
 *
 * It does the same three things as the console menu (choose folder, create
 * baseline, run check) and only talks to FIMController, so none of the core
 * code knows the window exists. Everything is built from plain JLabels,
 * JPanels and JButtons in code, so there is no form designer file to merge
 * when we all work on the project.
 *
 * The changed files list and the recent activity list both scroll, so
 * nothing gets cut off when a check finds a lot of changes or the log
 * gets long. Recent activity only covers the current run of the program, oldest line first.
 *
 * Group 35 - SDN260S
 */
public class MainDashBoard extends JFrame {

    // colours, same palette as the rest of the design
    private static final Color BODY = new Color(14, 14, 16);
    private static final Color PANEL = new Color(42, 53, 72);
    private static final Color DARK_PANEL = new Color(30, 39, 57);
    private static final Color TEAL = new Color(13, 175, 169);
    private static final Color DISABLED = new Color(55, 65, 81);
    private static final Color GREY = new Color(160, 170, 185);
    private static final Color LIGHT = new Color(204, 204, 204);
    private static final Color LINE = new Color(64, 78, 102);

    private static final int ROW_HEIGHT = 24;
    // true: newest line at the top. false: like a normal log, newest at the bottom and scrolled down.
    private static final boolean NEWEST_FIRST = false;
    private static final int LOG_LIMIT = 500;   // newest log entries loaded into the list
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd MMM HH:mm:ss");

    // Recent activity only shows what happened since the program was started.
    // The log file keeps the full history, but every new run begins with a blank list.
    // (Started from the console menu this is when the console started, not the window.)
    private static final LocalDateTime SESSION_START = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime()),
            ZoneId.systemDefault()).withNano(0);

    private final FIMController controller;

    private final JLabel folderValueLabel = new JLabel();
    private final JLabel baselineValueLabel = new JLabel();
    private final JLabel statusLabel = new JLabel("Ready");

    private final JButton selectFolderButton = makeButton("Select Folder");
    private final JButton createBaselineButton = makeButton("Create Baseline");
    private final JButton runCheckButton = makeButton("Run Check");

    // the legend doubles as the summary counts after a check
    private final JLabel unchangedLabel = new JLabel("Unchanged");
    private final JLabel modifiedLabel = new JLabel("Modified");
    private final JLabel newLabel = new JLabel("New");
    private final JLabel missingLabel = new JLabel("Missing");

    // true while the changed files list shows a real check result (not a hint message)
    private boolean showingResults = false;

    // the two scrolling lists: rows get added to these panels
    private final ListPanel changedRows = new ListPanel();
    private final ListPanel activityRows = new ListPanel();
    private final JScrollPane changedScroll = makeScrollPane(changedRows);
    private final JScrollPane activityScroll = makeScrollPane(activityRows);

    /** Standalone start (Run File on this class): makes its own controller. */
    public MainDashBoard() {
        this(new FIMController());
    }

    /** Start from the CLI (option 5): shares the controller so both views see the same state. */
    public MainDashBoard(FIMController controller) {
        super("FIM Security Suite");
        this.controller = controller;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(buildContent());
        setSize(1000, 680);
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(null);

        selectFolderButton.addActionListener(e -> onSelectFolder());
        createBaselineButton.addActionListener(e -> onCreateBaseline());
        runCheckButton.addActionListener(e -> onRunCheck());

        showMessage("Choose a folder to start");
        refreshView();
    }

    // ------------------------------------------------------------------
    // button actions
    // ------------------------------------------------------------------

    private void onSelectFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;   // user cancelled
        }
        try {
            controller.selectFolder(chooser.getSelectedFile().toPath());
            showMessage("Folder selected. Create a baseline next.");   // result belonged to the old folder
            statusLabel.setText("Folder selected");
            refreshView();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void onCreateBaseline() {
        try {
            Baseline baseline = controller.createBaseline();
            // only replace the hint text; a result from an earlier check stays on screen
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

    // ------------------------------------------------------------------
    // updating what's on screen
    // ------------------------------------------------------------------

    /** Re-reads the controller: folder, baseline, which buttons work, activity list. */
    private void refreshView() {
        Path folder = controller.getSelectedFolder();
        folderValueLabel.setText(folder == null ? "(none chosen yet)" : folder.toString());

        // this run's log entries (the logger gives them oldest first)
        List<LogEntry> entries = new ArrayList<>();
        for (LogEntry entry : controller.getRecentLog(LOG_LIMIT)) {
            if (!entry.getTimestamp().isBefore(SESSION_START)) {   // skip earlier runs
                entries.add(entry);
            }
        }

        // The baseline file stays on disk between runs, so "file exists" isn't enough.
        // Like the activity list, only a baseline made during this run counts.
        boolean baselineThisRun = false;
        for (LogEntry entry : entries) {
            if ("BASELINE".equals(entry.getAction())) {
                baselineThisRun = true;
            }
        }
        baselineValueLabel.setText(baselineThisRun && controller.hasBaseline() ? "saved" : "none");

        // a button only works when its step is possible
        setButtonEnabled(createBaselineButton, folder != null);
        setButtonEnabled(runCheckButton, folder != null && baselineThisRun && controller.hasBaseline());

        // order depends on NEWEST_FIRST (the logger gives the entries oldest first)
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
        // newest at the top: stay at the top. newest at the bottom: follow it down.
        finishList(activityRows, activityScroll, !NEWEST_FIRST);
    }

    /** Fills the list with one row per changed file and the legend with the counts. */
    private void showResult(ScanResult result) {
        List<FileChange> changed = new ArrayList<>();
        for (FileChange c : result.getChanges()) {
            if (c.getStatus() != FileStatus.UNCHANGED) {   // same as the CLI, only list changes
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

    /** One message in the list (before any check) and the legend goes back to plain names. */
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

    /** Redraws a list after its rows changed, then jumps to the top or to the last row. */
    private static void finishList(ListPanel rows, JScrollPane scroll, boolean toBottom) {
        rows.revalidate();
        rows.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scroll.getVerticalScrollBar();
            bar.setValue(toBottom ? bar.getMaximum() : 0);
        });
    }

    // ------------------------------------------------------------------
    // rows
    // ------------------------------------------------------------------

    /** One row of the changed files table: status, file, old hash, new hash. */
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

    /** One line of the activity list: time, coloured action, message. */
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

    /** Keeps every row the same height so rows don't stretch when there are only a few. */
    private static JPanel fixedHeight(JPanel row) {
        row.setPreferredSize(new Dimension(10, ROW_HEIGHT));
        row.setMinimumSize(new Dimension(10, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        return row;
    }

    /** First 8 characters of a hash, or "-" when there isn't one (new / missing files). */
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

    /** The log actions that are file statuses get the same colour as in the table. */
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

    // ------------------------------------------------------------------
    // building the window
    // ------------------------------------------------------------------

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BODY);

        // top: title bar + folder / baseline info
        JPanel top = new JPanel(new BorderLayout());
        top.add(buildHeader(), BorderLayout.NORTH);
        top.add(buildInfo(), BorderLayout.CENTER);
        root.add(top, BorderLayout.NORTH);

        // middle: buttons, legend, then the two cards
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(BODY);
        body.setBorder(BorderFactory.createEmptyBorder(16, 22, 12, 22));

        JPanel controls = new JPanel(new GridLayout(2, 1, 0, 10));
        controls.setOpaque(false);
        controls.add(buildButtonRow());
        controls.add(buildLegendRow());
        body.add(controls, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 1, 0, 12));
        cards.setOpaque(false);
        cards.add(buildChangedCard());
        cards.add(buildActivityCard());
        body.add(cards, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        // bottom: status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(DARK_PANEL);
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));
        statusLabel.setForeground(LIGHT);
        statusBar.add(statusLabel, BorderLayout.WEST);
        root.add(statusBar, BorderLayout.SOUTH);

        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JPanel title = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        title.setOpaque(false);
        JLabel fim = new JLabel("FIM");
        fim.setFont(new Font("Segoe UI", Font.BOLD, 22));
        fim.setForeground(TEAL);
        JLabel suite = new JLabel("Security Suite");
        suite.setFont(new Font("Segoe UI", Font.BOLD, 22));
        suite.setForeground(Color.WHITE);
        title.add(fim);
        title.add(suite);

        JLabel group = new JLabel("Group 35");
        group.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        group.setForeground(LIGHT);

        header.add(title, BorderLayout.WEST);
        header.add(group, BorderLayout.EAST);
        return header;
    }

    private JPanel buildInfo() {
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 6));
        info.setBackground(DARK_PANEL);
        info.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        folderValueLabel.setForeground(Color.WHITE);
        baselineValueLabel.setForeground(Color.WHITE);
        info.add(infoRow("Folder:", folderValueLabel));
        info.add(infoRow("Baseline:", baselineValueLabel));
        return info;
    }

    private JPanel infoRow(String name, JLabel value) {
        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(GREY);
        nameLabel.setPreferredSize(new Dimension(75, 20));
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(nameLabel, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    /** A row of components side by side with a fixed gap, starting flush left. */
    private static JPanel sideBySide(int gap, java.awt.Component... items) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                row.add(Box.createHorizontalStrut(gap));
            }
            row.add(items[i]);
        }
        return row;
    }

    private JPanel buildButtonRow() {
        return sideBySide(14, selectFolderButton, createBaselineButton, runCheckButton);
    }

    private JPanel buildLegendRow() {
        return sideBySide(28,
                legendItem(FileStatus.UNCHANGED, unchangedLabel),
                legendItem(FileStatus.MODIFIED, modifiedLabel),
                legendItem(FileStatus.NEW, newLabel),
                legendItem(FileStatus.MISSING, missingLabel));
    }

    /** A coloured dot next to its label. */
    private JPanel legendItem(FileStatus status, JLabel text) {
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dot.setForeground(colorFor(status));
        text.setForeground(Color.WHITE);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return sideBySide(6, dot, text);
    }

    private JPanel buildChangedCard() {
        // grey column titles stay in place while the rows scroll underneath
        JPanel headings = new JPanel(new GridLayout(1, 4, 12, 0));
        headings.setBackground(PANEL);
        headings.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                BorderFactory.createEmptyBorder(0, 0, 5, 0)));
        String[] names = {"STATUS", "FILE", "OLD HASH", "NEW HASH"};
        for (String name : names) {
            headings.add(cell(name, GREY, new Font("Segoe UI", Font.PLAIN, 12)));
        }
        changedScroll.setColumnHeaderView(headings);
        changedScroll.getColumnHeader().setBackground(PANEL);
        return card("CHANGED FILES", changedScroll);
    }

    private JPanel buildActivityCard() {
        return card("RECENT ACTIVITY", activityScroll);
    }

    /** A dark box with a small grey title. */
    private JPanel card(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(PANEL);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 12));
        JLabel heading = new JLabel(title);
        heading.setForeground(GREY);
        heading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        card.add(heading, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    /** Flat teal button; works the same whatever look and feel Java picks. */
    private static JButton makeButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(TEAL);
        button.setContentAreaFilled(false);   // we paint the flat colour ourselves
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /** Scroll pane with no border, the card colour behind it and the dark scrollbar. */
    private static JScrollPane makeScrollPane(ListPanel rows) {
        JScrollPane scroll = new JScrollPane(rows,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(PANEL);

        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setUI(new DarkScrollBarUI());
        bar.setPreferredSize(new Dimension(12, 0));
        bar.setBackground(PANEL);
        bar.setUnitIncrement(ROW_HEIGHT);
        return scroll;
    }

    public static void main(String[] args) {
        // the normal look of the operating system (native file chooser on Windows)
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // stay with the default look
        }
        java.awt.EventQueue.invokeLater(() -> new MainDashBoard().setVisible(true));
    }

    // ------------------------------------------------------------------
    // small helper classes for the scrolling lists
    // ------------------------------------------------------------------

    /**
     * A panel that stacks rows from the top and follows the width of the scroll
     * pane, so the columns stretch with the window instead of getting a sideways scrollbar.
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
}