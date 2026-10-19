# FIM Security Suite

**SDN260S – Software Design II · Group 35 · Topic 3: File Integrity Monitor**

A Java tool that records a "fingerprint" (SHA-256 hash) of every file in a folder, then re-scans that folder later to detect tampering or unwanted changes. Files are reported as **UNCHANGED**, **MODIFIED**, **NEW** or **MISSING**, and all activity is written to a log.

## Why it matters

Silent changes to files (malware, accidental edits, unauthorised tampering) are hard to spot by eye. Comparing cryptographic hashes against a trusted baseline makes any change to a file's content detectable, even when its name, size or timestamp looks the same.

## How it works

1. **Create baseline** – scan a chosen folder recursively, hash every file and save the snapshot to disk (serialised).
2. **Run check** – scan the same folder again and compare the new snapshot against the saved baseline.
3. **Report** – list every change by status and record the result in the activity log.

## Requirements

- JDK 24 (the project compiles with `--enable-preview`)
- Apache NetBeans 31 (includes Maven)
- Windows, macOS or Linux

## Getting started

```bash
git clone https://github.com/Group-35-SDN260s-FIM-Project/FIM-Project.git
```

1. In NetBeans choose **File → Open Project** and select the cloned folder.
2. Make sure the project uses JDK 24 (**Project Properties → Build → Compile / Run**).
3. Run the project (**F6**). The main class is `com.mycompany.fim_security_suite.FIM_Security_Suite`.

## Branches

| Branch | Contents |
| --- | --- |
| `main` | Maven project with the Swing GUI (`MainDashBoard`, `AlertFramePage`, `FolderSelectionDialog`) |
| `demoFIM` | Console version with the core logic (scanner, hashing, baseline, integrity checker, CLI menu) |

Work is done on personal branches and merged into `main` through pull requests.

## Design overview

The design follows the group UML diagram and is split into packages:

| Package | Classes |
| --- | --- |
| *(root)* | `FIM_Security_Suite` |
| `cli` | `CliMenu` |
| `controller` | `FIMController` |
| `core` | `FileScanner`, `HashUtil`, `BaselineManager`, `IntegrityChecker`, `ActivityLogger` |
| `model` | `FileRecord`, `Baseline`, `FileChange`, `FileStatus`, `ScanResult`, `LogEntry` |
| `demo` | `SandboxTool` |
| `test` | `CoreTest`, `IntegrityCheckerTest` |

## Team – Group 35

| Member | Classes |
| --- | --- |
| Wahine Hlulani Njoku | `FIM_Security_Suite`, `FIMController` |
| Sandiswa Mchunu | `IntegrityChecker`, `FileChange`, `FileStatus` |
| Thamsanqa Temle | `ActivityLogger`, `LogEntry` |
| Mohlodi Molelekeng | `HashUtil`, `IntegrityCheckerTest` |
| Khotofelo Robyn Rabababela | `FileScanner`, `FileRecord` |
| Olona Mlanjeni | `BaselineManager`, `Baseline` |
| Rebecca Mandiadi | `CliMenu`, `ScanResult` |
| Mixo Rikhotso | `SandboxTool`, `CoreTest` |