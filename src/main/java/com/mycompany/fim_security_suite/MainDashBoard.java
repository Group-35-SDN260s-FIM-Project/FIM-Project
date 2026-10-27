
package com.mycompany.fim_security_suite;

public class MainDashBoard extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainDashBoard.class.getName());

    // One controller shared by every window (and by the CLI when launched from there),
    // so the selected folder survives switching between Dashboard and Alerts.
    private static my.fim_security_suite.controller.FIMController sharedController;

    private final my.fim_security_suite.controller.FIMController controller;

    public static synchronized my.fim_security_suite.controller.FIMController getSharedController() {
        if (sharedController == null) {
            sharedController = new my.fim_security_suite.controller.FIMController();
        }
        return sharedController;
    }

    /**
     * Creates new form MainDashBoard
     */
    public MainDashBoard() {
        this(getSharedController());
    }

    public MainDashBoard(my.fim_security_suite.controller.FIMController controller) {
        this.controller = controller;
        initComponents();
        jLcreateBaseLine.addActionListener(evt -> createBaseline());
        jLRunCheck.addActionListener(evt -> runCheck());
        setUpRecentActivity();
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{MonitoredFilesValue, AlertsValue,
                LastScanValue, unchangedValue, modifiedValue, newValue, missingValue}) {
            l.setText("-");
        }
        refreshStatus(null);
    }

    private void createBaseline() {
        try {
            my.fim_security_suite.model.Baseline baseline = controller.createBaseline();
            refreshStatus(null);
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Baseline saved: " + baseline.size() + " files recorded.");
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    private void runCheck() {
        try {
            refreshStatus(controller.runCheck());
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    private javax.swing.JTextArea activityArea;

    /** Puts a scrolling log view inside the Recent Activity panel. */
    private void setUpRecentActivity() {
        activityArea = new javax.swing.JTextArea();
        activityArea.setEditable(false);
        activityArea.setBackground(new java.awt.Color(38, 52, 69));
        activityArea.setForeground(java.awt.Color.WHITE);
        activityArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(activityArea);
        scroll.setBorder(null);
        RecentActivityPanel.setLayout(new java.awt.BorderLayout(0, 6));
        RecentActivityPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        RecentActivityPanel.add(jLRecentActivities, java.awt.BorderLayout.NORTH);
        RecentActivityPanel.add(scroll, java.awt.BorderLayout.CENTER);
    }

    private void refreshActivity() {
        StringBuilder sb = new StringBuilder();
        for (my.fim_security_suite.model.LogEntry entry : controller.getRecentLog(10)) {
            sb.append(entry.toLine()).append('\n');
        }
        activityArea.setText(sb.length() == 0 ? "(no activity yet)" : sb.toString());
        activityArea.setCaretPosition(0);
    }

    private void showError(RuntimeException e) {
        javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "FIM",
                javax.swing.JOptionPane.WARNING_MESSAGE);
    }

    /** Updates the dashboard labels; result may be null when no check has just run. */
    private void refreshStatus(my.fim_security_suite.model.ScanResult result) {
        refreshActivity();
        java.awt.Color white = java.awt.Color.WHITE;
        SystemStatusValue.setForeground(white);
        MonitoredFilesValue.setForeground(white);
        AlertsValue.setForeground(white);
        LastScanValue.setForeground(white);
        unchangedValue.setForeground(white);
        modifiedValue.setForeground(white);
        newValue.setForeground(white);
        missingValue.setForeground(white);

        java.nio.file.Path folder = controller.getSelectedFolder();
        SystemStatusValue.setText(folder == null ? "No folder"
                : controller.hasBaseline() ? "Ready" : "No baseline");
        if (folder != null) {
            MonitoredFilesValue.setToolTipText(folder.toString());
        }
        if (result == null) {
            return;
        }
        showResult(result);
    }

    private void showResult(my.fim_security_suite.model.ScanResult result) {
        unchangedValue.setText(String.valueOf(result.countOf(my.fim_security_suite.model.FileStatus.UNCHANGED)));
        modifiedValue.setText(String.valueOf(result.countOf(my.fim_security_suite.model.FileStatus.MODIFIED)));
        newValue.setText(String.valueOf(result.countOf(my.fim_security_suite.model.FileStatus.NEW)));
        missingValue.setText(String.valueOf(result.countOf(my.fim_security_suite.model.FileStatus.MISSING)));
        MonitoredFilesValue.setText(String.valueOf(result.size()));
        AlertsValue.setText(String.valueOf(result.size()
                - result.countOf(my.fim_security_suite.model.FileStatus.UNCHANGED)));
        LastScanValue.setText(java.time.LocalTime.now().withNano(0).toString());
        SystemStatusValue.setText(result.isClean() ? "Clean" : "Changes found");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backGroundPanel = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        FIMlogo = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        alertsButton = new javax.swing.JButton();
        DashboardButton = new javax.swing.JButton();
        actionPanel = new javax.swing.JPanel();
        SystemStatusPanel = new javax.swing.JPanel();
        jLSystemStatus = new javax.swing.JLabel();
        SystemStatusValue = new javax.swing.JLabel();
        MonitoredFilesPanel = new javax.swing.JPanel();
        jLMonitoredFiles = new javax.swing.JLabel();
        MonitoredFilesValue = new javax.swing.JLabel();
        alertsPanel = new javax.swing.JPanel();
        jLAlerts = new javax.swing.JLabel();
        AlertsValue = new javax.swing.JLabel();
        LastScanPanel = new javax.swing.JPanel();
        jLLastScan = new javax.swing.JLabel();
        LastScanValue = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        jBSelectFolder = new javax.swing.JButton();
        jLcreateBaseLine = new javax.swing.JButton();
        jLRunCheck = new javax.swing.JButton();
        FileStatusBreakDown = new javax.swing.JPanel();
        jLFileSatusBreakedown = new javax.swing.JLabel();
        GreenDot = new javax.swing.JLabel();
        YellowDot = new javax.swing.JLabel();
        BlueDot = new javax.swing.JLabel();
        RedDot = new javax.swing.JLabel();
        jLUnchanged = new javax.swing.JLabel();
        jLModified = new javax.swing.JLabel();
        jLNew = new javax.swing.JLabel();
        jLMissing = new javax.swing.JLabel();
        unchangedValue = new javax.swing.JLabel();
        modifiedValue = new javax.swing.JLabel();
        newValue = new javax.swing.JLabel();
        missingValue = new javax.swing.JLabel();
        RecentActivityPanel = new javax.swing.JPanel();
        jLRecentActivities = new javax.swing.JLabel();
        dataPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(70, 85, 105));
        setSize(new java.awt.Dimension(1280, 720));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        backGroundPanel.setBackground(new java.awt.Color(9, 13, 18));
        backGroundPanel.setMinimumSize(new java.awt.Dimension(1200, 750));
        backGroundPanel.setPreferredSize(new java.awt.Dimension(1200, 750));

        headerPanel.setBackground(new java.awt.Color(38, 52, 69));

        FIMlogo.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        FIMlogo.setForeground(new java.awt.Color(0, 153, 153));
        FIMlogo.setText("   FIM");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("FIM Security suite - Group 35");

        alertsButton.setBackground(new java.awt.Color(0, 153, 153));
        alertsButton.setText("Alerts");
        alertsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alertsButtonActionPerformed(evt);
            }
        });

        DashboardButton.setBackground(new java.awt.Color(0, 153, 153));
        DashboardButton.setText("Dashboard");
        DashboardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DashboardButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(FIMlogo, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(DashboardButton)
                .addGap(18, 18, 18)
                .addComponent(alertsButton)
                .addGap(72, 72, 72))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(FIMlogo, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(alertsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(DashboardButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        actionPanel.setBackground(new java.awt.Color(9, 13, 18));

        SystemStatusPanel.setBackground(new java.awt.Color(38, 52, 69));

        jLSystemStatus.setForeground(new java.awt.Color(150, 163, 175));
        jLSystemStatus.setText("SYSTEM STATUS");

        SystemStatusValue.setText("jLabel2");

        javax.swing.GroupLayout SystemStatusPanelLayout = new javax.swing.GroupLayout(SystemStatusPanel);
        SystemStatusPanel.setLayout(SystemStatusPanelLayout);
        SystemStatusPanelLayout.setHorizontalGroup(
            SystemStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SystemStatusPanelLayout.createSequentialGroup()
                .addGap(75, 75, 75)
                .addGroup(SystemStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SystemStatusValue, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLSystemStatus))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        SystemStatusPanelLayout.setVerticalGroup(
            SystemStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SystemStatusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLSystemStatus)
                .addGap(18, 18, 18)
                .addComponent(SystemStatusValue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        MonitoredFilesPanel.setBackground(new java.awt.Color(38, 52, 69));

        jLMonitoredFiles.setForeground(new java.awt.Color(150, 163, 175));
        jLMonitoredFiles.setText("MONITORED FILES");

        MonitoredFilesValue.setText("jLabel2");

        javax.swing.GroupLayout MonitoredFilesPanelLayout = new javax.swing.GroupLayout(MonitoredFilesPanel);
        MonitoredFilesPanel.setLayout(MonitoredFilesPanelLayout);
        MonitoredFilesPanelLayout.setHorizontalGroup(
            MonitoredFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MonitoredFilesPanelLayout.createSequentialGroup()
                .addGap(79, 79, 79)
                .addGroup(MonitoredFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(MonitoredFilesValue, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLMonitoredFiles))
                .addContainerGap(84, Short.MAX_VALUE))
        );
        MonitoredFilesPanelLayout.setVerticalGroup(
            MonitoredFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MonitoredFilesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLMonitoredFiles)
                .addGap(18, 18, 18)
                .addComponent(MonitoredFilesValue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        alertsPanel.setBackground(new java.awt.Color(38, 52, 69));

        jLAlerts.setForeground(new java.awt.Color(150, 163, 175));
        jLAlerts.setText("ALERTS");

        AlertsValue.setText("jLabel2");

        javax.swing.GroupLayout alertsPanelLayout = new javax.swing.GroupLayout(alertsPanel);
        alertsPanel.setLayout(alertsPanelLayout);
        alertsPanelLayout.setHorizontalGroup(
            alertsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alertsPanelLayout.createSequentialGroup()
                .addGap(79, 79, 79)
                .addComponent(jLAlerts)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, alertsPanelLayout.createSequentialGroup()
                .addContainerGap(118, Short.MAX_VALUE)
                .addComponent(AlertsValue, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48))
        );
        alertsPanelLayout.setVerticalGroup(
            alertsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alertsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLAlerts)
                .addGap(18, 18, 18)
                .addComponent(AlertsValue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        LastScanPanel.setBackground(new java.awt.Color(38, 52, 69));

        jLLastScan.setForeground(new java.awt.Color(150, 163, 175));
        jLLastScan.setText("LAST SCAN");

        LastScanValue.setText("jLabel2");

        javax.swing.GroupLayout LastScanPanelLayout = new javax.swing.GroupLayout(LastScanPanel);
        LastScanPanel.setLayout(LastScanPanelLayout);
        LastScanPanelLayout.setHorizontalGroup(
            LastScanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LastScanPanelLayout.createSequentialGroup()
                .addContainerGap(114, Short.MAX_VALUE)
                .addGroup(LastScanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LastScanPanelLayout.createSequentialGroup()
                        .addComponent(jLLastScan)
                        .addGap(68, 68, 68))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LastScanPanelLayout.createSequentialGroup()
                        .addComponent(LastScanValue, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46))))
        );
        LastScanPanelLayout.setVerticalGroup(
            LastScanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LastScanPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLLastScan)
                .addGap(18, 18, 18)
                .addComponent(LastScanValue, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout actionPanelLayout = new javax.swing.GroupLayout(actionPanel);
        actionPanel.setLayout(actionPanelLayout);
        actionPanelLayout.setHorizontalGroup(
            actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionPanelLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(SystemStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
                .addComponent(MonitoredFilesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(73, 73, 73)
                .addComponent(alertsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(81, 81, 81)
                .addComponent(LastScanPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        actionPanelLayout.setVerticalGroup(
            actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, actionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(SystemStatusPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(MonitoredFilesPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(alertsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(LastScanPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        buttonPanel.setBackground(new java.awt.Color(9, 13, 18));

        jBSelectFolder.setBackground(new java.awt.Color(30, 41, 59));
        jBSelectFolder.setForeground(new java.awt.Color(243, 244, 246));
        jBSelectFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/fim_security_suite/icons/folder_21dp_E3E3E3_FILL0_wght400_GRAD0_opsz20.png"))); // NOI18N
        jBSelectFolder.setText("Select folder");
        jBSelectFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBSelectFolderActionPerformed(evt);
            }
        });

        jLcreateBaseLine.setBackground(new java.awt.Color(0, 153, 153));
        jLcreateBaseLine.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/fim_security_suite/icons/fingerprint_21dp_E3E3E3_FILL0_wght400_GRAD0_opsz20.png"))); // NOI18N
        jLcreateBaseLine.setText("Create baseline");

        jLRunCheck.setBackground(new java.awt.Color(30, 41, 59));
        jLRunCheck.setForeground(new java.awt.Color(243, 244, 246));
        jLRunCheck.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mycompany/fim_security_suite/icons/refresh_21dp_E3E3E3_FILL0_wght400_GRAD0_opsz20 (1).png"))); // NOI18N
        jLRunCheck.setText("Run check");

        FileStatusBreakDown.setBackground(new java.awt.Color(38, 52, 69));

        jLFileSatusBreakedown.setForeground(new java.awt.Color(150, 163, 175));
        jLFileSatusBreakedown.setText(" FILE STATUS BREAKDOWN");

        GreenDot.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        GreenDot.setForeground(new java.awt.Color(0, 204, 0));
        GreenDot.setText("●");

        YellowDot.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        YellowDot.setForeground(new java.awt.Color(255, 255, 0));
        YellowDot.setText("●");

        BlueDot.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        BlueDot.setForeground(new java.awt.Color(0, 0, 153));
        BlueDot.setText("●");

        RedDot.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        RedDot.setForeground(new java.awt.Color(255, 0, 0));
        RedDot.setText("●");

        jLUnchanged.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLUnchanged.setForeground(new java.awt.Color(255, 255, 255));
        jLUnchanged.setText("Unchanged");

        jLModified.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLModified.setForeground(new java.awt.Color(255, 255, 255));
        jLModified.setText("Modified");

        jLNew.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLNew.setForeground(new java.awt.Color(255, 255, 255));
        jLNew.setText("New");

        jLMissing.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLMissing.setForeground(new java.awt.Color(255, 255, 255));
        jLMissing.setText("Missing");

        unchangedValue.setText("jLabel2");

        modifiedValue.setText("jLabel2");

        newValue.setText("jLabel2");

        missingValue.setText("jLabel2");

        javax.swing.GroupLayout FileStatusBreakDownLayout = new javax.swing.GroupLayout(FileStatusBreakDown);
        FileStatusBreakDown.setLayout(FileStatusBreakDownLayout);
        FileStatusBreakDownLayout.setHorizontalGroup(
            FileStatusBreakDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FileStatusBreakDownLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLFileSatusBreakedown, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(308, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FileStatusBreakDownLayout.createSequentialGroup()
                .addGroup(FileStatusBreakDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FileStatusBreakDownLayout.createSequentialGroup()
                        .addComponent(GreenDot)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLUnchanged))
                    .addGroup(FileStatusBreakDownLayout.createSequentialGroup()
                        .addComponent(YellowDot)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLModified))
                    .addGroup(FileStatusBreakDownLayout.createSequentialGroup()
                        .addComponent(BlueDot)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLNew))
                    .addGroup(FileStatusBreakDownLayout.createSequentialGroup()
                        .addComponent(RedDot)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLMissing)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(FileStatusBreakDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modifiedValue)
                    .addComponent(unchangedValue)
                    .addComponent(newValue)
                    .addComponent(missingValue))
                .addGap(31, 31, 31))
        );
        FileStatusBreakDownLayout.setVerticalGroup(
            FileStatusBreakDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FileStatusBreakDownLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLFileSatusBreakedown, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addGroup(FileStatusBreakDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(GreenDot)
                    .addComponent(jLUnchanged)
                    .addComponent(unchangedValue))
                .addGap(18, 18, 18)
                .addGroup(FileStatusBreakDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(YellowDot)
                    .addComponent(jLModified)
                    .addComponent(modifiedValue))
                .addGap(18, 18, 18)
                .addGroup(FileStatusBreakDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BlueDot)
                    .addComponent(jLNew)
                    .addComponent(newValue))
                .addGap(18, 18, 18)
                .addGroup(FileStatusBreakDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RedDot)
                    .addComponent(jLMissing)
                    .addComponent(missingValue))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        RecentActivityPanel.setBackground(new java.awt.Color(38, 52, 69));

        jLRecentActivities.setForeground(new java.awt.Color(150, 163, 175));
        jLRecentActivities.setText("RECENT ACTIVITY");

        javax.swing.GroupLayout RecentActivityPanelLayout = new javax.swing.GroupLayout(RecentActivityPanel);
        RecentActivityPanel.setLayout(RecentActivityPanelLayout);
        RecentActivityPanelLayout.setHorizontalGroup(
            RecentActivityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RecentActivityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLRecentActivities)
                .addContainerGap(398, Short.MAX_VALUE))
        );
        RecentActivityPanelLayout.setVerticalGroup(
            RecentActivityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RecentActivityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLRecentActivities)
                .addContainerGap(278, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(buttonPanelLayout.createSequentialGroup()
                        .addComponent(FileStatusBreakDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(RecentActivityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20))
                    .addGroup(buttonPanelLayout.createSequentialGroup()
                        .addComponent(jBSelectFolder)
                        .addGap(55, 55, 55)
                        .addComponent(jLcreateBaseLine)
                        .addGap(66, 66, 66)
                        .addComponent(jLRunCheck)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBSelectFolder)
                    .addComponent(jLcreateBaseLine)
                    .addComponent(jLRunCheck))
                .addGap(18, 18, 18)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(FileStatusBreakDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                        .addComponent(RecentActivityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        dataPanel.setBackground(new java.awt.Color(9, 13, 18));

        javax.swing.GroupLayout dataPanelLayout = new javax.swing.GroupLayout(dataPanel);
        dataPanel.setLayout(dataPanelLayout);
        dataPanelLayout.setHorizontalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        dataPanelLayout.setVerticalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout backGroundPanelLayout = new javax.swing.GroupLayout(backGroundPanel);
        backGroundPanel.setLayout(backGroundPanelLayout);
        backGroundPanelLayout.setHorizontalGroup(
            backGroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backGroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backGroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(actionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(backGroundPanelLayout.createSequentialGroup()
                        .addGap(1139, 1139, 1139)
                        .addComponent(dataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backGroundPanelLayout.setVerticalGroup(
            backGroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backGroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(actionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50)
                .addComponent(dataPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(backGroundPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1360, 790));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBSelectFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBSelectFolderActionPerformed
        // Create a native file chooser instance
    javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
    
    // Set it to select directories/folders only instead of individual files
    fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
    
    // Open the clean, native popup window centered over your dashboard
    int result = fileChooser.showOpenDialog(this);
    
    // Check if the user successfully selected a folder and clicked "Open"
    if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
        java.io.File selectedFolder = fileChooser.getSelectedFile();
        try {
            controller.selectFolder(selectedFolder.toPath());
            refreshStatus(null);
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    }//GEN-LAST:event_jBSelectFolderActionPerformed

    private void alertsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alertsButtonActionPerformed
         // 1. Create the new Alerts page instance
    AlertFramePage alertWindow = new AlertFramePage();
    
    // 2. Open it in the exact same desktop position as this current window
    alertWindow.setLocationRelativeTo(this);
    
    // 3. Make the Alerts page visible
    alertWindow.setVisible(true);
    
    // 4. Close this Dashboard window completely so it disappears underneath
    this.dispose();
    }//GEN-LAST:event_alertsButtonActionPerformed

    private void DashboardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DashboardButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_DashboardButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    try {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
        logger.log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(() -> new MainDashBoard().setVisible(true));
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AlertsValue;
    private javax.swing.JLabel BlueDot;
    private javax.swing.JButton DashboardButton;
    private javax.swing.JLabel FIMlogo;
    private javax.swing.JPanel FileStatusBreakDown;
    private javax.swing.JLabel GreenDot;
    private javax.swing.JPanel LastScanPanel;
    private javax.swing.JLabel LastScanValue;
    private javax.swing.JPanel MonitoredFilesPanel;
    private javax.swing.JLabel MonitoredFilesValue;
    private javax.swing.JPanel RecentActivityPanel;
    private javax.swing.JLabel RedDot;
    private javax.swing.JPanel SystemStatusPanel;
    private javax.swing.JLabel SystemStatusValue;
    private javax.swing.JLabel YellowDot;
    private javax.swing.JPanel actionPanel;
    private javax.swing.JButton alertsButton;
    private javax.swing.JPanel alertsPanel;
    private javax.swing.JPanel backGroundPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JButton jBSelectFolder;
    private javax.swing.JLabel jLAlerts;
    private javax.swing.JLabel jLFileSatusBreakedown;
    private javax.swing.JLabel jLLastScan;
    private javax.swing.JLabel jLMissing;
    private javax.swing.JLabel jLModified;
    private javax.swing.JLabel jLMonitoredFiles;
    private javax.swing.JLabel jLNew;
    private javax.swing.JLabel jLRecentActivities;
    private javax.swing.JButton jLRunCheck;
    private javax.swing.JLabel jLSystemStatus;
    private javax.swing.JLabel jLUnchanged;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton jLcreateBaseLine;
    private javax.swing.JLabel missingValue;
    private javax.swing.JLabel modifiedValue;
    private javax.swing.JLabel newValue;
    private javax.swing.JLabel unchangedValue;
    // End of variables declaration//GEN-END:variables
}
